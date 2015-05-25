package com.griddynamics.cd.nrp.internal.rest;

import com.griddynamics.cd.nrp.internal.model.api.ArtifactMetaInfo;
import com.griddynamics.cd.nrp.internal.model.api.RestResponse;
import com.thoughtworks.xstream.XStream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.restlet.data.Request;
import org.sonatype.nexus.proxy.maven.ArtifactStoreHelper;
import org.sonatype.nexus.proxy.maven.ArtifactStoreRequest;
import org.sonatype.nexus.proxy.maven.maven2.M2Repository;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;

import java.util.ArrayList;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ArtifactUpdatePlexusResource.class)
public class ArtifactUpdatePlexusResourceTest {

    private final String REQUEST_URI = "/artifact/maven/update";

    private ArtifactUpdatePlexusResource artifactUpdatePlexusResource;
    private ArtifactMetaInfo artifactMetaInfo;

    @Before
    public void setUp() throws Exception {
        artifactUpdatePlexusResource = PowerMockito.spy(new ArtifactUpdatePlexusResource());
        artifactMetaInfo = new ArtifactMetaInfo("http://localhost:8081/nexus", "com.griddynamics.cd", "nexus-replication-plugin", "1.0", "snapshots");
        artifactMetaInfo.setExtension("jar");
    }

    @Test
    public void testGetResourceUri() throws Exception {
        assertEquals(artifactUpdatePlexusResource.getResourceUri(), REQUEST_URI);
    }

    @Test
    public void testGetResourceProtection() throws Exception {
        PathProtectionDescriptor resourceProtection = artifactUpdatePlexusResource.getResourceProtection();
        assertEquals("Incorrect request URI in security configuration", resourceProtection.getPathPattern(), REQUEST_URI);
        assertEquals("Incorrect permissions for API", resourceProtection.getFilterExpression(), "authcBasic,perms[nexus:artifact]");
    }

    @Test
    public void testGetPayloadInstance() throws Exception {
        Object instance = artifactUpdatePlexusResource.getPayloadInstance();
        assertNotNull(REQUEST_URI + " resource should be configured to return ArtifactMetaInfo as request body DTO. Method returns null.", instance);
        assertTrue(REQUEST_URI + " resource should be configured to return ArtifactMetaInfo as request body DTO. Method returns incorrect type.", instance instanceof ArtifactMetaInfo);
    }

    @Test
    public void testConfigureXStream() throws Exception {
        XStream xstream = Mockito.mock(XStream.class);
        artifactUpdatePlexusResource.configureXStream(xstream);
        Mockito.verify(xstream, Mockito.times(1)).processAnnotations(ArtifactMetaInfo.class);
        Mockito.verify(xstream, Mockito.times(1)).processAnnotations(RestResponse.class);
    }

    @Test
    public void testPostForEmptyRepositoriesList() throws Exception {
        // Mocks initialization
        Request request = Mockito.mock(Request.class);
        PowerMockito.when(artifactUpdatePlexusResource, "getRepositoryRegistry").thenReturn(PowerMockito.mock(RepositoryRegistry.class));

        // Tested method invocation
        RestResponse response = (RestResponse) artifactUpdatePlexusResource.post(null, request, null, artifactMetaInfo);
        // Asserts
        assertEquals("Method should return message that no proxies found", response.getMessage(), "No proxies for this artifact.");
        assertFalse("Method should return error status because of no proxies found", response.isSuccess());
    }

    @Test
    public void testPostForOneRepositoryList() throws Exception {
        // Init mocks
        ArtifactStoreHelper artifactStoreHelper = PowerMockito.mock(ArtifactStoreHelper.class);
        ArrayList<Repository> repositories = new ArrayList<>();
        M2Repository repository = PowerMockito.mock(M2Repository.class);
        PowerMockito.when(repository.getRemoteUrl()).thenReturn("http://localhost:8081/nexus/content/repositories/snapshots/");
        PowerMockito.when(repository.getId()).thenReturn("replica-1");
        PowerMockito.when(repository.getArtifactStoreHelper()).thenReturn(artifactStoreHelper);
        repositories.add(repository);

        Request request = new Request();
        RepositoryRegistry repositoryRegistry = PowerMockito.mock(RepositoryRegistry.class);
        PowerMockito.when(repositoryRegistry, "getRepositories").thenReturn(repositories);
        PowerMockito.when(artifactUpdatePlexusResource, "getRepositoryRegistry").thenReturn(repositoryRegistry);
        ArtifactStoreRequest storeRequest = PowerMockito.mock(ArtifactStoreRequest.class);
        PowerMockito.doReturn(storeRequest).when(artifactUpdatePlexusResource, "getResourceStoreRequest",
                Matchers.eq(request), Matchers.eq(false), Matchers.eq(false), Matchers.eq("replica-1"), Matchers.eq(artifactMetaInfo.getGroupId()),
                Matchers.eq(artifactMetaInfo.getArtifactId()), Matchers.eq(artifactMetaInfo.getVersion()), Matchers.eq(artifactMetaInfo.getPackaging()),
                Matchers.eq(artifactMetaInfo.getClassifier()), Matchers.eq(artifactMetaInfo.getExtension()));
        // Tested method invocation
        RestResponse response = (RestResponse) artifactUpdatePlexusResource.post(null, request, null, artifactMetaInfo);
        // Asserts
        Mockito.verify(artifactStoreHelper, Mockito.times(1)).retrieveArtifact(storeRequest);
        Assert.assertEquals("Request should return that artifact is resolved", "Artifact is resolved.", response.getMessage());
        Assert.assertTrue("Request should be success.", response.isSuccess());
    }

    @Test
    public void testPostForTwoMatchedRepositories() throws Exception {
        // Init mocks
        ArtifactStoreHelper artifactStoreHelper = PowerMockito.mock(ArtifactStoreHelper.class);
        ArrayList<Repository> repositories = new ArrayList<>();
        M2Repository repository = PowerMockito.mock(M2Repository.class);
        PowerMockito.when(repository.getRemoteUrl()).thenReturn("http://localhost:8081/nexus/content/repositories/snapshots/");
        PowerMockito.when(repository.getId()).thenReturn("replica-1");
        PowerMockito.when(repository.getArtifactStoreHelper()).thenReturn(artifactStoreHelper);
        repositories.add(repository);

        repository = PowerMockito.mock(M2Repository.class);
        PowerMockito.when(repository.getRemoteUrl()).thenReturn("http://localhost:8081/nexus/content/repositories/snapshots/");
        PowerMockito.when(repository.getId()).thenReturn("replica-2");
        PowerMockito.when(repository.getArtifactStoreHelper()).thenReturn(artifactStoreHelper);
        repositories.add(repository);

        repository = PowerMockito.mock(M2Repository.class);
        PowerMockito.when(repository.getRemoteUrl()).thenReturn("http://localhost:8085/nexus/content/repositories/releases/");
        PowerMockito.when(repository.getId()).thenReturn("replica-3");
        PowerMockito.when(repository.getArtifactStoreHelper()).thenReturn(artifactStoreHelper);
        repositories.add(repository);

        Request request = new Request();
        RepositoryRegistry repositoryRegistry = PowerMockito.mock(RepositoryRegistry.class);
        PowerMockito.when(repositoryRegistry, "getRepositories").thenReturn(repositories);
        PowerMockito.when(artifactUpdatePlexusResource, "getRepositoryRegistry").thenReturn(repositoryRegistry);
        ArtifactStoreRequest storeRequest = PowerMockito.mock(ArtifactStoreRequest.class);
        PowerMockito.doReturn(storeRequest).when(artifactUpdatePlexusResource, "getResourceStoreRequest",
                Matchers.eq(request), Matchers.eq(false), Matchers.eq(false), Matchers.any(), Matchers.eq(artifactMetaInfo.getGroupId()),
                Matchers.eq(artifactMetaInfo.getArtifactId()), Matchers.eq(artifactMetaInfo.getVersion()), Matchers.eq(artifactMetaInfo.getPackaging()),
                Matchers.eq(artifactMetaInfo.getClassifier()), Matchers.eq(artifactMetaInfo.getExtension()));

        // Tested method invocation
        RestResponse response = (RestResponse) artifactUpdatePlexusResource.post(null, request, null, artifactMetaInfo);
        // Asserts
        Mockito.verify(artifactStoreHelper, Mockito.times(2)).retrieveArtifact(storeRequest);
        Assert.assertEquals("Request should return that artifact is resolved", "Artifact is resolved.", response.getMessage());
        Assert.assertTrue("Request should be success.", response.isSuccess());
    }
}