package com.griddynamics.cd.nrp.internal.uploading.impl;

import com.griddynamics.cd.nrp.internal.model.config.NexusServer;
import com.griddynamics.cd.nrp.internal.model.config.ReplicationPluginConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonatype.nexus.configuration.application.NexusConfiguration;

import java.io.File;
import java.net.URL;
import java.util.Iterator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurationsManagerImplTest {

    private final String FIRST_CONFIGURATION_FILE = "replication-plugin.xml";
    private final String SECOND_CONFIGURATION_FILE = "replication-plugin-2.xml";

    @Test
    public void testGetConfiguration() throws Exception {
        NexusConfiguration nexusConfiguration = mock(NexusConfiguration.class);
        URL configFile = getClass().getClassLoader().getResource(FIRST_CONFIGURATION_FILE);
        when(nexusConfiguration.getConfigurationDirectory()).thenReturn(new File(configFile.getFile()).getParentFile());
        ConfigurationsManagerImpl configurationsManager = spy(new ConfigurationsManagerImpl(nexusConfiguration));

        ReplicationPluginConfiguration configuration = configurationsManager.getConfiguration();
        Assert.assertEquals("My url was written incorrectly", "http://localhost:8081/nexus", configuration.getMyUrl());
        Assert.assertEquals("Incorrect count of servers", 1, configuration.getServers().size());
        Iterator<NexusServer> iterator = configuration.getServers().iterator();
        if (iterator.hasNext()) {
            NexusServer nexusServer = iterator.next();
            Assert.assertEquals("Server url was written incorrectly", "http://localhost:8083/nexus", nexusServer.getUrl());
            Assert.assertEquals("User was written incorrectly", "admin", nexusServer.getUser());
            Assert.assertEquals("Password was written incorrectly", "admin123", nexusServer.getPassword());
        }
    }

    @Test
    public void testReloadConfigurations() throws Exception {
        NexusConfiguration nexusConfiguration = mock(NexusConfiguration.class);
        URL configFile = getClass().getClassLoader().getResource(FIRST_CONFIGURATION_FILE);
        when(nexusConfiguration.getConfigurationDirectory()).thenReturn(new File(configFile.getFile()).getParentFile());
        ConfigurationsManagerImpl configurationsManager = spy(new ConfigurationsManagerImpl(nexusConfiguration));

        ReplicationPluginConfiguration configuration = configurationsManager.getConfiguration();
        Assert.assertEquals("First file should contain one nexus server only", 1, configuration.getServers().size());

        configurationsManager.reloadConfigurations(SECOND_CONFIGURATION_FILE);
        configuration = configurationsManager.getConfiguration();
        Assert.assertEquals("Second file should contain two nexus servers", 2, configuration.getServers().size());
    }
}