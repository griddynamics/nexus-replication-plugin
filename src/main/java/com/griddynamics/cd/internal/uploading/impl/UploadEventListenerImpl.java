package com.griddynamics.cd.internal.uploading.impl;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.griddynamics.cd.internal.model.api.ArtifactMetaInfo;
import com.griddynamics.cd.internal.uploading.ArtifactUpdateApiClient;
import com.griddynamics.cd.internal.uploading.ConfigurationsManager;
import com.griddynamics.cd.internal.uploading.UploadEventListener;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStore;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.gav.Gav;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

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
            if (gav != null && !gav.isSignature() && !gav.isHash()) {
                ArtifactMetaInfo metaInfo = new ArtifactMetaInfo(configurationsManager.getConfiguration().getMeUrl(), gav.getGroupId(), gav.getArtifactId(), gav.getVersion(), repo.getId());
                metaInfo.setClassifier(gav.getClassifier());
                metaInfo.setExtension(gav.getExtension());
                artifactUpdateApiClient.sendRequest(metaInfo);
            }
        }
    }
}
