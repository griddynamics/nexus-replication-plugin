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
import com.griddynamics.cd.nrp.internal.model.config.ReplicationPluginConfiguration;
import com.griddynamics.cd.nrp.internal.model.internal.ArtifactMetaInfoQueueDump;
import com.griddynamics.cd.nrp.internal.model.api.RestResponse;
import com.griddynamics.cd.nrp.internal.model.config.NexusServer;
import com.griddynamics.cd.nrp.internal.uploading.ArtifactUpdateApiClient;
import com.griddynamics.cd.nrp.internal.uploading.ConfigurationsManager;
import com.griddynamics.cd.nrp.internal.uploading.impl.factories.AsyncWebResourceBuilderFactory;
import com.griddynamics.cd.nrp.internal.uploading.impl.factories.FileBlockingQueueFactory;
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
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.concurrent.*;

/**
 * Responsible to send request to other Nexus instances to notify them about new artifacts.
 * There are 2 separate thread pools working along:
 * - Usual sending threads that work when new artifacts are uploaded to Nexus Sender
 * - Threads that read queue of artifacts from file. That file is filled with new
 * artifacts if Nexus Receiver was not available and our Nexus was
 * shut down. It then reads artifacts from the file and tries to send them to Receiver
 */

@Singleton
@Named(ArtifactUpdateApiClientImpl.ID)
public class ArtifactUpdateApiClientImpl extends ComponentSupport implements ArtifactUpdateApiClient {

    public static final String ID = "artifactUpdateApiClient";

    /**
     * Default value for request queue timeout
     * Timeout should be relatively low and should be lower than Jersey Client read timeout
     */
    public static final int QUEUE_TIMEOUT_IN_SECOND = 1;

    /**
     * Provides access to the plugin configurations
     */
    private final ConfigurationsManager configurationsManager;
    private final FileBlockingQueueFactory fileBlockingQueueFactory;
    private final AsyncWebResourceBuilderFactory asyncWebResourceBuilderFactory;
    /**
     * ExecutorService shares between clients. All treads are created in the same executor
     */
    private final FileBlockingQueue fileBlockingQueue;

    @Inject
    public ArtifactUpdateApiClientImpl(ConfigurationsManager configurationsManager,
                                       FileBlockingQueueFactory fileBlockingQueueFactory,
                                       AsyncWebResourceBuilderFactory asyncWebResourceBuilderFactory) {
        this.configurationsManager = configurationsManager;
        this.fileBlockingQueueFactory = fileBlockingQueueFactory;
        this.asyncWebResourceBuilderFactory = asyncWebResourceBuilderFactory;
        this.fileBlockingQueue = initFileBlockingQueue(configurationsManager.getConfiguration());
        initBackgroundWorkers(configurationsManager.getConfiguration());
    }

    private void initBackgroundWorkers(ReplicationPluginConfiguration replicationPluginConfiguration) {
        int requestsSendingThreadsCount = replicationPluginConfiguration.getRequestsSendingThreadsCount();
        ExecutorService executorService = Executors.newFixedThreadPool(requestsSendingThreadsCount);
        for (int i = 0; i < requestsSendingThreadsCount; i++) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    while (!Thread.currentThread().isInterrupted()) {
                        try {
                            ArtifactMetaInfo artifactMetaInfo = fileBlockingQueue.peek();
                            sendRequest(artifactMetaInfo);
                            fileBlockingQueue.take();
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    }
                }
            });
        }
    }

    private FileBlockingQueue initFileBlockingQueue(ReplicationPluginConfiguration replicationPluginConfiguration) {
        String queueFileName = replicationPluginConfiguration.getQueueDumpFileName();
        FileBlockingQueue retVal = fileBlockingQueueFactory.getFileBlockingQueue();
        try {
            File queueFile = new File(queueFileName);
            if (queueFile.exists()) {
                JAXBContext jaxbContext = JAXBContext.newInstance(ArtifactMetaInfoQueueDump.class);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                ArtifactMetaInfoQueueDump unmarshal = (ArtifactMetaInfoQueueDump) unmarshaller.unmarshal(queueFile);
                for (ArtifactMetaInfo artifactMetaInfo : unmarshal.getArtifactMetaInfos()) {
                    offerRequest(artifactMetaInfo);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return retVal;
    }

    @Override
    public void offerRequest(ArtifactMetaInfo artifactMetaInfo) {
        try {
            fileBlockingQueue.offer(artifactMetaInfo, QUEUE_TIMEOUT_IN_SECOND, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Sends replication requests to all nexus servers configured in XML file
     *
     * @param metaInfo Artifact information
     */
    public void sendRequest(ArtifactMetaInfo metaInfo) {
        for (NexusServer server : configurationsManager.getConfiguration().getServers()) {
            AsyncWebResource.Builder service =
                    asyncWebResourceBuilderFactory.getAsyncWebResourceBuilder(
                            server.getUrl(), server.getUser(), server.getPassword());
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

}
