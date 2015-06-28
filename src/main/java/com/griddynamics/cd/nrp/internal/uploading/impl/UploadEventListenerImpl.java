/*
 * Copyright 2015, Grid Dynamics International, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.griddynamics.cd.nrp.internal.uploading.impl;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.griddynamics.cd.nrp.internal.model.api.ArtifactMetaInfo;
import com.griddynamics.cd.nrp.internal.model.api.ArtifactStatus;
import com.griddynamics.cd.nrp.internal.uploading.ArtifactUpdateApiClient;
import com.griddynamics.cd.nrp.internal.uploading.ConfigurationsManager;
import com.griddynamics.cd.nrp.internal.uploading.UploadEventListener;
import org.sonatype.nexus.client.core.subsystem.repository.maven.MavenProxyRepository;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStore;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.gav.Gav;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Artifact stored event hook
 */
@Singleton
@Named(UploadEventListenerImpl.ID)
public class UploadEventListenerImpl extends ComponentSupport implements UploadEventListener {

    public static final String ID = "uploadEventListener";

    /**
     * Provides access to plugin the configurations
     */
    private ConfigurationsManager configurationsManager;

    private ArtifactUpdateApiClient artifactUpdateApiClient;

    private Map<ArtifactMetaInfo, ArtifactStatus> receivedArtifacts = new ConcurrentHashMap<>();

    @Inject
    public UploadEventListenerImpl(@Named(value = ConfigurationsManagerImpl.ID) ConfigurationsManager configurationsManager,
                                   @Named(value = ArtifactUpdateApiClientImpl.ID) ArtifactUpdateApiClient artifactUpdateApiClient) {
        this.configurationsManager = configurationsManager;
        this.artifactUpdateApiClient = artifactUpdateApiClient;
    }

    /**
     * Fired when new artifact deployed to nexus (proxy repositories are ignored)
     */
    @Subscribe
    @AllowConcurrentEvents
    public void onArtifactUploading(RepositoryItemEventStore event) {
        if (event.getRepository() instanceof MavenRepository &&
                !(event.getRepository() instanceof MavenProxyRepository)) {
            MavenRepository repo = (MavenRepository) event.getRepository();
            Gav gav = repo.getGavCalculator().pathToGav(event.getItemUid().getPath());
            if (null != gav) {
                ArtifactMetaInfo metaInfo = new ArtifactMetaInfo(configurationsManager.getConfiguration().getMyUrl(), gav.getGroupId(), gav.getArtifactId(), gav.getVersion(), repo.getId());
                metaInfo.setClassifier(gav.getClassifier());
                metaInfo.setExtension(gav.getExtension());
                ArtifactStatus artifactStatus = null;
                if (!gav.isSignature() && !gav.isHash()) {
                    artifactStatus = getArtifactStatus(metaInfo);
                    artifactStatus.setFileReceived(true);
                    log.debug("File received: " + metaInfo.toString());
                } else if (gav.isHash() && gav.getHashType().equals(Gav.HashType.sha1)) {
                    artifactStatus = getArtifactStatus(metaInfo);
                    artifactStatus.setSha1Received(true);
                    log.debug(gav.getHashType().name() + " hash file received for: " + metaInfo.toString());
                }
                if (null != artifactStatus) {
                    updateArtifactStatus(metaInfo, artifactStatus);
                    if (artifactStatus.isReadyForReplication()) {
                        log.debug("File with hashes received for: " + metaInfo.toString() + " Sending request");
                        artifactUpdateApiClient.offerRequest(metaInfo);
                        clearStatus(metaInfo);
                    }
                }
            }
        }
    }

    /**
     * Returns if binary / checksum files were deployed
     * @param metaInfo Meta info of the deployed artifact
     */
    private ArtifactStatus getArtifactStatus(ArtifactMetaInfo metaInfo) {
        if (!receivedArtifacts.containsKey(metaInfo)) {
            receivedArtifacts.put(metaInfo, new ArtifactStatus(metaInfo));
        }
        return receivedArtifacts.get(metaInfo);
    }

    /**
     * Executed when new file (binary or checksum) received
     * @param artifactMetaInfo Meta info of the deployed artifact
     * @param artifactStatus Information about received files for deployed artifact
     */
    private void updateArtifactStatus(ArtifactMetaInfo artifactMetaInfo, ArtifactStatus artifactStatus) {
        receivedArtifacts.put(artifactMetaInfo, artifactStatus);
    }

    /**
     * Executed when bin and sha1 files received and nexus peer notifications sent
     * @param metaInfo Meta info of the deployed artifact
     */
    private void clearStatus(ArtifactMetaInfo metaInfo) {
        receivedArtifacts.remove(metaInfo);
    }
}
