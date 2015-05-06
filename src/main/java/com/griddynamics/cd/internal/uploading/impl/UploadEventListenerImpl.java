package com.griddynamics.cd.internal.uploading.impl;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.griddynamics.cd.internal.model.api.ArtifactMetaInfo;
import com.griddynamics.cd.internal.model.api.RestResponse;
import com.griddynamics.cd.internal.model.config.NexusServer;
import com.griddynamics.cd.internal.uploading.ConfigurationsManager;
import com.griddynamics.cd.internal.uploading.UploadEventListener;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStore;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.gav.Gav;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

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

    @Inject
    public UploadEventListenerImpl(@Named(value = "configurationsManager") ConfigurationsManager configurationsManager) {
        this.configurationsManager = configurationsManager;
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
                sendRequest(metaInfo);
            }
        }
    }

    /**
     * Sends replication requests to all nexus servers configured in XML file
     * @param metaInfo
     */
    private void sendRequest(ArtifactMetaInfo metaInfo) {
        for (NexusServer server : configurationsManager.getConfiguration().getServers()) {
            WebResource.Builder service = getService(server.getUrl(), server.getUser(), server.getPassword());
            RestResponse response = service.post(RestResponse.class, metaInfo);
            if (!response.isSuccess()) {
                log.error("Can not send replication request: " + response.getMessage());
            }
        }
    }

    /**
     * Returns jersey HTTP client
     * @param nexusUrl URL of the remote server
     * @param login Username on the remote server
     * @param password User's password
     * @return Jersey HTTP client
     */
    private static WebResource.Builder getService(String nexusUrl, String login, String password) {
        ClientConfig config = new DefaultClientConfig();
        Client client = Client.create(config);
        client.addFilter(new HTTPBasicAuthFilter(login, password));
        WebResource webResource = client.resource(UriBuilder.fromUri(nexusUrl).build());
        webResource = webResource.path("service").path("local").path("artifact").path("maven").path("update");
        WebResource.Builder readyToRequest = webResource.accept(MediaType.APPLICATION_XML_TYPE)
                .type(MediaType.APPLICATION_XML_TYPE);
        return readyToRequest;
    }
}
