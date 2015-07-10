package com.griddynamics.cd.nrp.internal.uploading.impl.factories;

import com.sun.jersey.api.client.AsyncWebResource;
import com.sun.jersey.api.client.Client;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

@Singleton
public class AsyncWebResourceBuilderFactory {

    private final JerseyClientFactory jerseyClientFactory;

    @Inject
    public AsyncWebResourceBuilderFactory(JerseyClientFactory jerseyClientFactory) {
        this.jerseyClientFactory = jerseyClientFactory;
    }


    /**
     * Returns jersey HTTP resource to access to the remote replication servers
     *
     * @param nexusUrl URL of the remote server
     * @param login    Username on the remote server
     * @param password User's password
     * @return Jersey HTTP client
     */
    public AsyncWebResource.Builder getAsyncWebResourceBuilder(String nexusUrl, String login, String password) {
        Client client = jerseyClientFactory.getClient(login, password);
        AsyncWebResource webResource = client.asyncResource(UriBuilder.fromUri(nexusUrl).build());
        webResource = webResource.path("service").path("local").path("artifact").path("maven").path("update");
        return webResource.accept(MediaType.APPLICATION_XML_TYPE)
                .type(MediaType.APPLICATION_XML_TYPE);
    }

}
