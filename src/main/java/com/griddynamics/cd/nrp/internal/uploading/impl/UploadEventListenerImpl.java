package com.griddynamics.cd.nrp.internal.uploading.impl;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.griddynamics.cd.nrp.internal.model.api.ArtifactMetaInfo;
import com.griddynamics.cd.nrp.internal.model.api.ArtifactStatus;
import com.griddynamics.cd.nrp.internal.uploading.ArtifactUpdateApiClient;
import com.griddynamics.cd.nrp.internal.uploading.ConfigurationsManager;
import com.griddynamics.cd.nrp.internal.uploading.UploadEventListener;
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
     * Fired when new artifact deployed to nexus
     */
    @Subscribe
    @AllowConcurrentEvents
    public void onArtifactUploading(RepositoryItemEventStore event) {
        if (event.getRepository() instanceof MavenRepository) {
            MavenRepository repo = (MavenRepository) event.getRepository();
            Gav gav = repo.getGavCalculator().pathToGav(event.getItemUid().getPath());
            if (null != gav) {
                ArtifactMetaInfo metaInfo = new ArtifactMetaInfo(configurationsManager.getConfiguration().getMeUrl(), gav.getGroupId(), gav.getArtifactId(), gav.getVersion(), repo.getId());
                metaInfo.setClassifier(gav.getClassifier());
                metaInfo.setExtension(gav.getExtension());
                ArtifactStatus artifactStatus = getArtifactStatus(metaInfo);
                if (!gav.isSignature() && !gav.isHash()) {
                    artifactStatus.setFileReceived(true);
                    log.debug("File received: " + metaInfo.toString());
                } else if (gav.isHash()) {
                    if (gav.getHashType().equals(Gav.HashType.md5)) {
                        artifactStatus.setMd5Received(true);
                    } else if (gav.getHashType().equals(Gav.HashType.sha1)) {
                        artifactStatus.setSha1Received(true);
                    }
                    log.debug(gav.getHashType().name() + " hash file received for: " + metaInfo.toString());
                }
                updateArtifactStatus(metaInfo, artifactStatus);
                if (artifactStatus.isReadyForReplication()) {
                    log.debug("File with hashes received for: " + metaInfo.toString() + " Sending request");
                    artifactUpdateApiClient.sendRequest(metaInfo);
                    clearStatus(metaInfo);
                }
            }
        }
    }

    private ArtifactStatus getArtifactStatus(ArtifactMetaInfo metaInfo) {
        if (!receivedArtifacts.containsKey(metaInfo)) {
            receivedArtifacts.put(metaInfo, new ArtifactStatus(metaInfo));
        }
        return receivedArtifacts.get(metaInfo);
    }

    private void updateArtifactStatus(ArtifactMetaInfo artifactMetaInfo, ArtifactStatus artifactStatus) {
        receivedArtifacts.put(artifactMetaInfo, artifactStatus);
    }

    private void clearStatus(ArtifactMetaInfo metaInfo) {
        receivedArtifacts.remove(metaInfo);
    }
}
