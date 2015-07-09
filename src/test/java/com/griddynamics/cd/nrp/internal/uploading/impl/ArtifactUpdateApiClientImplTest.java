package com.griddynamics.cd.nrp.internal.uploading.impl;


import com.griddynamics.cd.nrp.internal.model.api.ArtifactMetaInfo;
import com.griddynamics.cd.nrp.internal.model.config.NexusServer;
import com.griddynamics.cd.nrp.internal.model.config.ReplicationPluginConfiguration;
import com.griddynamics.cd.nrp.internal.uploading.ArtifactUpdateApiClient;
import com.griddynamics.cd.nrp.internal.uploading.ConfigurationsManager;
import com.griddynamics.cd.nrp.internal.uploading.impl.factories.AsyncWebResourceBuilderFactory;
import com.griddynamics.cd.nrp.internal.uploading.impl.factories.FileBlockingQueueFactory;
import com.sun.jersey.api.client.AsyncWebResource;
import com.sun.jersey.api.client.async.ITypeListener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ArtifactUpdateApiClientImplTest {

    private AwaitableFIleBlockingQueue awaitableFIleBlockingQueue;
    @Mock
    private ConfigurationsManager configurationsManagerMock;
    @Mock
    private FileBlockingQueueFactory fileBlockingQueueFactory;
    @Mock
    private AsyncWebResourceBuilderFactory asyncWebResourceBuilderFactoryMock;
    @Mock
    AsyncWebResource.Builder asyncWebResourceBuilderMock;

    private ArtifactUpdateApiClient underTest;

    @Before
    public void setUp() {
        NexusServer nexusServer = new NexusServer("http://mock:8082","admin","admin123");
        NexusServer nexusServer2 = new NexusServer("http://mock:8083", "admin2", "admin456");
        awaitableFIleBlockingQueue = new AwaitableFIleBlockingQueue(
                new LinkedBlockingQueue<ArtifactMetaInfo>(), "/tmp/queueDumpForTests",
                2
        );
        ReplicationPluginConfiguration replicationPluginConfiguration =
                new ReplicationPluginConfiguration("http://mock:8081","/tmp/queueDumpForTests");
        replicationPluginConfiguration.addServer(nexusServer);
        replicationPluginConfiguration.addServer(nexusServer2);
        when(configurationsManagerMock.getConfiguration()).thenReturn(replicationPluginConfiguration);
        when(fileBlockingQueueFactory.getFileBlockingQueue()).thenReturn(awaitableFIleBlockingQueue);
        when(asyncWebResourceBuilderFactoryMock.getAsyncWebResourceBuilder(
                anyString(), anyString(), anyString())).
                thenReturn(asyncWebResourceBuilderMock);
        underTest = new ArtifactUpdateApiClientImpl(
                configurationsManagerMock,
                fileBlockingQueueFactory,
                asyncWebResourceBuilderFactoryMock);
    }

    @After
    public void tearDown() {
        underTest = null;
    }

    @Test
    public void if2ArtifactsSubmittedAnd2PeersConfiguredShouldSend2artifactsTo2Peers() throws InterruptedException {
        ArtifactMetaInfo artifactMetaInfo = new ArtifactMetaInfo(
                "http://mock:8081",
                "com.griddynamics",
                "nexus-replication-status",
                "1.0-SNAPSHOT",
                "local-master-nexus"
        );
        ArtifactMetaInfo artifactMetaInfo2 = new ArtifactMetaInfo(
                "http://mock:8081",
                "com.griddynamics",
                "nexus-replication-status",
                "2.0-SNAPSHOT",
                "local-master-nexus"
        );
        underTest.offerRequest(artifactMetaInfo);
        underTest.offerRequest(artifactMetaInfo2);
        awaitableFIleBlockingQueue.await();

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> loginCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> passwordCaptor = ArgumentCaptor.forClass(String.class);

        verify(asyncWebResourceBuilderFactoryMock, times(4)).
                getAsyncWebResourceBuilder(urlCaptor.capture(), loginCaptor.capture(), passwordCaptor.capture());

        List<String> allUrls = urlCaptor.getAllValues();
        List<String> allLogins = loginCaptor.getAllValues();
        List<String> allpasswowrds = passwordCaptor.getAllValues();

        assertEquals(4, allUrls.size());
        assertEquals("http://mock:8082", allUrls.get(0));
        assertEquals("http://mock:8083", allUrls.get(1));
        assertEquals("http://mock:8082", allUrls.get(2));
        assertEquals("http://mock:8083", allUrls.get(3));

        assertEquals(4, allLogins.size());
        assertEquals("admin", allLogins.get(0));
        assertEquals("admin2", allLogins.get(1));
        assertEquals("admin", allLogins.get(2));
        assertEquals("admin2", allLogins.get(3));

        assertEquals(4, allpasswowrds.size());
        assertEquals("admin123", allpasswowrds.get(0));
        assertEquals("admin456", allpasswowrds.get(1));
        assertEquals("admin123", allpasswowrds.get(2));
        assertEquals("admin456", allpasswowrds.get(3));

        ArgumentCaptor<ITypeListener> iTypeListenerArgumentCaptor = ArgumentCaptor.forClass(ITypeListener.class);

        ArgumentCaptor<ArtifactMetaInfo> artifactMetaInfoArgumentCaptor =
                ArgumentCaptor.forClass(ArtifactMetaInfo.class);

        verify(asyncWebResourceBuilderMock,times(4)).post(
                iTypeListenerArgumentCaptor.capture(),
                artifactMetaInfoArgumentCaptor.capture());


        assertEquals(artifactMetaInfo,artifactMetaInfoArgumentCaptor.getAllValues().get(0));
        assertEquals(artifactMetaInfo,artifactMetaInfoArgumentCaptor.getAllValues().get(1));
        assertEquals(artifactMetaInfo2,artifactMetaInfoArgumentCaptor.getAllValues().get(2));
        assertEquals(artifactMetaInfo2,artifactMetaInfoArgumentCaptor.getAllValues().get(3));

    }

}