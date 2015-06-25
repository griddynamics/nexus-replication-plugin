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

import com.griddynamics.cd.nrp.internal.model.api.ArtifactMetaInfo;
import com.griddynamics.cd.nrp.internal.model.api.RestResponse;
import com.griddynamics.cd.nrp.internal.model.config.NexusServer;
import com.griddynamics.cd.nrp.internal.uploading.ArtifactUpdateApiClient;
import com.griddynamics.cd.nrp.internal.uploading.ConfigurationsManager;
import com.sun.jersey.api.client.AsyncWebResource;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.async.ITypeListener;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.util.concurrent.*;

@Singleton
@Named(ArtifactUpdateApiClientImpl.ID)
public class ArtifactUpdateApiClientImpl extends ComponentSupport implements ArtifactUpdateApiClient {

    public static final String ID = "artifactUpdateApiClient";

    /**
     * Provides access to the plugin configurations
     */
    private ConfigurationsManager configurationsManager;

    /**
     * Queue for async requests
     */
    private BlockingQueue<Runnable> queue;

    /**
     * ExecutorService shares between clients. All treads are created in the same executor
     */
    private ExecutorService asyncRequestsExecutorService;

    @Inject
    public ArtifactUpdateApiClientImpl(ConfigurationsManager configurationsManager) {
        this.configurationsManager = configurationsManager;
        this.queue = new LinkedBlockingQueue<>(configurationsManager.getConfiguration().getRequestsQueueSize());
        this.asyncRequestsExecutorService = new ThreadPoolExecutor(
                configurationsManager.getConfiguration().getRequestsSendingThreadsCount(),
                configurationsManager.getConfiguration().getRequestsSendingThreadsCount(),
                30, TimeUnit.SECONDS, queue);
    }

    /**
     * Sends replication requests to all nexus servers configured in XML file
     * @param metaInfo Artifact information
     */
    @Override
    public void sendRequest(ArtifactMetaInfo metaInfo) {
        for (NexusServer server : configurationsManager.getConfiguration().getServers()) {
            AsyncWebResource.Builder service = getService(server.getUrl(), server.getUser(), server.getPassword());
            try {
                service.post(new ITypeListener<RestResponse>() {
                    @Override
                    public void onComplete(Future<RestResponse> future) throws InterruptedException {
                        RestResponse response = null;
                        try {
                            response = future.get();
                        } catch (ExecutionException e) {
                            log.error("Can not get REST response", e);
                        }
                        if (response != null && !response.isSuccess()) {
                            log.error("Can not send replication request: " + response.getMessage());
                        }
                    }

                    @Override
                    public Class<RestResponse> getType() {
                        return RestResponse.class;
                    }

                    @Override
                    public GenericType<RestResponse> getGenericType() {
                        return null;
                    }

                }, metaInfo);
            } catch (RejectedExecutionException e) {
                log.warn("Requests queue is full. Request to " + server.getUrl() + " is rejected");
            }
        }
    }

    /**
     * Returns jersey HTTP resource to access to the remote replication servers
     * @param nexusUrl URL of the remote server
     * @param login Username on the remote server
     * @param password User's password
     * @return Jersey HTTP client
     */
    private AsyncWebResource.Builder getService(String nexusUrl, String login, String password) {
        Client client = getClient(login, password);
        AsyncWebResource webResource = client.asyncResource(UriBuilder.fromUri(nexusUrl).build());
        webResource = webResource.path("service").path("local").path("artifact").path("maven").path("update");
        return webResource.accept(MediaType.APPLICATION_XML_TYPE)
                .type(MediaType.APPLICATION_XML_TYPE);
    }

    /**
     * Creates jersey HTTP client
     * @param login Username on the remote server
     * @param password User's password
     * @return HTTP client
     */
    private Client getClient(String login, String password) {
        ClientConfig config = new DefaultClientConfig();
        Client client = Client.create(config);
        client.setExecutorService(asyncRequestsExecutorService);
        if (login != null && !login.isEmpty() && password != null) {
            log.debug("Creating HTTP client with authorized HTTPBasicAuthFilter.");
            client.addFilter(new HTTPBasicAuthFilter(login, password));
        } else {
            log.debug("Creating HTTP client with anonymous HTTPBasicAuthFilter.");
        }
        return client;
    }
}
