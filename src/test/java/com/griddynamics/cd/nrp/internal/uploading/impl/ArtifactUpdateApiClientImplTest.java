package com.griddynamics.cd.nrp.internal.uploading.impl;

import com.griddynamics.cd.nrp.internal.uploading.ArtifactUpdateApiClient;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandler;
import com.sun.jersey.api.client.TerminatingClientHandler;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import org.junit.Assert;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.reflect.Whitebox;

public class ArtifactUpdateApiClientImplTest {
    @Test
    public void testGetClientAuth() throws Exception {
        testAuthUser("admin", "admin123", "Basic YWRtaW46YWRtaW4xMjM=");
    }

    @Test
    public void testGetClientAuthEmptyPassword() throws Exception {
        testAuthUser("admin", "", "Basic YWRtaW46");
    }

    @Test
    public void testGetClientEmptyUser() throws Exception {
        testAnonymousUser("", "admin123");
        testAnonymousUser("", null);
    }

    @Test
    public void testGetClientNullUser() throws Exception {
        testAnonymousUser(null, "somePass");
        testAnonymousUser(null, null);
    }

    private void testAuthUser(String admin, String admin123, String expectedAuthentication) throws Exception {
        ArtifactUpdateApiClient artifactUpdateApiClient = new ArtifactUpdateApiClientImpl(PowerMockito.mock(ConfigurationsManagerImpl.class));
        Client client = Whitebox.invokeMethod(artifactUpdateApiClient, "getClient", admin, admin123);
        ClientHandler current = Whitebox.getInternalState(client, "head");
        while (!(current instanceof TerminatingClientHandler)) {
            if (current instanceof HTTPBasicAuthFilter) {
                HTTPBasicAuthFilter authFilter = (HTTPBasicAuthFilter) current;
                Assert.assertEquals(expectedAuthentication, Whitebox.getInternalState(authFilter, "authentication"));
            }
            if (current instanceof ClientFilter) {
                current = ((ClientFilter) current).getNext();
            }
        }
    }

    private void testAnonymousUser(String login, String password) throws Exception {
        ArtifactUpdateApiClient artifactUpdateApiClient = new ArtifactUpdateApiClientImpl(PowerMockito.mock(ConfigurationsManagerImpl.class));
        Client client = Whitebox.invokeMethod(artifactUpdateApiClient, "getClient", login, password);
        ClientHandler current = Whitebox.getInternalState(client, "head");
        while (!(current instanceof TerminatingClientHandler)) {
            if (current instanceof HTTPBasicAuthFilter) {
                Assert.fail("Request should not contain HTTPBasicAuthFilter for anonymous user");
            }
            if (current instanceof ClientFilter) {
                current = ((ClientFilter) current).getNext();
            }
        }
    }
}