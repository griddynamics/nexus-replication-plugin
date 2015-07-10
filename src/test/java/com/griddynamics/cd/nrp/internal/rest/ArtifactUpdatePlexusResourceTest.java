package com.griddynamics.cd.nrp.internal.rest;

import com.griddynamics.cd.nrp.internal.model.api.ArtifactMetaInfo;
import com.griddynamics.cd.nrp.internal.model.api.RestResponse;
import com.thoughtworks.xstream.XStream;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.restlet.data.Request;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.proxy.maven.ArtifactStoreHelper;
import org.sonatype.nexus.proxy.maven.ArtifactStoreRequest;
import org.sonatype.nexus.proxy.maven.maven2.M2Repository;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;

import java.util.ArrayList;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ArtifactUpdatePlexusResourceTest {

    private final String REQUEST_URI = "/artifact/maven/update";
    private final ArrayList<Repository> repositories = new ArrayList<>();
    ArtifactStoreHelper artifactStoreHelper = mock(ArtifactStoreHelper.class);
    ArtifactStoreRequest artifactStoreRequestMock = mock(ArtifactStoreRequest.class);

    private ArtifactUpdatePlexusResource artifactUpdatePlexusResource;
    private ArtifactMetaInfo artifactMetaInfo;

    @Before
    public void setUp() throws Exception {
        artifactUpdatePlexusResource = spy(new ArtifactUpdatePlexusResource() {
            @Override
            protected RepositoryRegistry getRepositoryRegistry() {
                RepositoryRegistry repositoryRegistry = mock(RepositoryRegistry.class);
                when(repositoryRegistry.getRepositories()).thenReturn(repositories);
                return repositoryRegistry;
            }

            @Override
            protected ArtifactStoreRequest getResourceStoreRequest(Request request, boolean localOnly, boolean remoteOnly, String repositoryId, String g, String a, String v, String p, String c, String e) throws ResourceException {
                return artifactStoreRequestMock;
            }
        });
        artifactMetaInfo = new ArtifactMetaInfo("http://localhost:8081/nexus", "com.griddynamics.cd", "nexus-replication-plugin", "1.0", "snapshots");
        artifactMetaInfo.setExtension("jar");
    }

    @After
    public void tearDown() {
        repositories.clear();
    }

    @Test
    public void shouldReturnCorrectResourceUri() throws Exception {
        assertEquals(artifactUpdatePlexusResource.getResourceUri(), REQUEST_URI);
    }

    @Test
    public void shouldReturnCorrectFilterExpression() throws Exception {
        PathProtectionDescriptor resourceProtection = artifactUpdatePlexusResource.getResourceProtection();
        assertEquals("Incorrect request URI in security configuration", resourceProtection.getPathPattern(), REQUEST_URI);
        assertEquals("Incorrect permissions for API", resourceProtection.getFilterExpression(), "authcBasic,perms[nexus:artifact]");
    }

    @Test
    public void shouldReturnCorrectPayload() throws Exception {
        Object instance = artifactUpdatePlexusResource.getPayloadInstance();
        assertNotNull(REQUEST_URI + " resource should be configured to return ArtifactMetaInfo as request body DTO. Method returns null.", instance);
        assertTrue(REQUEST_URI + " resource should be configured to return ArtifactMetaInfo as request body DTO. Method returns incorrect type.", instance instanceof ArtifactMetaInfo);
    }

    @Test
    public void shouldConfigureXStreamCorrectly() throws Exception {
        XStream xstream = mock(XStream.class);
        artifactUpdatePlexusResource.configureXStream(xstream);
        Mockito.verify(xstream, Mockito.times(1)).processAnnotations(ArtifactMetaInfo.class);
        Mockito.verify(xstream, Mockito.times(1)).processAnnotations(RestResponse.class);
    }

    @Test
    public void ifNoProxyIsConfiguredShouldReturnNoProxiesForThisArtifact() throws Exception {
        // Mocks initialization
        Request request = mock(Request.class);

        // Tested method invocation
        RestResponse response = (RestResponse) artifactUpdatePlexusResource.post(null, request, null, artifactMetaInfo);
        // Asserts
        assertEquals("Method should return message that no proxies found", response.getMessage(), "No proxies for this artifact.");
        assertFalse("Method should return error status because of no proxies found", response.isSuccess());
    }

    @Test
    public void ifProxyForArtifactIsConfiguredShouldReturnArtifactIsResolved() throws Exception {
        // Init mocks
        ArtifactStoreHelper artifactStoreHelper = mock(ArtifactStoreHelper.class);
        Request request = new Request();

        M2Repository repository = mock(M2Repository.class);
        when(repository.getRemoteUrl()).thenReturn("http://localhost:8081/nexus/content/repositories/snapshots/");
        when(repository.getId()).thenReturn("replica-1");
        when(repository.getArtifactStoreHelper()).thenReturn(artifactStoreHelper);
        repositories.add(repository);
        // Tested method invocation
        RestResponse response = (RestResponse) artifactUpdatePlexusResource.post(null, request, null, artifactMetaInfo);
        // Asserts
        Mockito.verify(artifactStoreHelper, Mockito.times(1)).retrieveArtifact(artifactStoreRequestMock);
        Assert.assertEquals("Request should return that artifact is resolved", "Artifact is resolved.", response.getMessage());
        Assert.assertTrue("Request should be success.", response.isSuccess());
    }

    private M2Repository provideMockM2Repository(String id, String remoteURL) {
        M2Repository repository = mock(M2Repository.class);
        when(repository.getRemoteUrl()).thenReturn(remoteURL);
        when(repository.getId()).thenReturn(id);
        when(repository.getArtifactStoreHelper()).thenReturn(artifactStoreHelper);
        return repository;
    }

    @Test
    public void if2Of3RepositoriesShouldSendOnlyToMatchningRepositories() throws Exception {
        // Init mocks

        repositories.add(provideMockM2Repository("replica-1", "http://localhost:8081/nexus/content/repositories/snapshots/"));
        repositories.add(provideMockM2Repository("replica-2", "http://localhost:8081/nexus/content/repositories/snapshots/"));
        repositories.add(provideMockM2Repository("replica-3", "http://localhost:8085/nexus/content/repositories/snapshots/"));

        Request request = new Request();
        // Tested method invocation
        RestResponse response = (RestResponse) artifactUpdatePlexusResource.post(null, request, null, artifactMetaInfo);
        // Asserts
        Mockito.verify(artifactStoreHelper, Mockito.times(2)).retrieveArtifact(artifactStoreRequestMock);
        Assert.assertEquals("Request should return that artifact is resolved", "Artifact is resolved.", response.getMessage());
        Assert.assertTrue("Request should be success.", response.isSuccess());
    }
}