package com.griddynamics.cd.internal.uploading.impl;

import com.griddynamics.cd.internal.model.config.ReplicationPluginConfiguration;
import com.griddynamics.cd.internal.uploading.ConfigurationsManager;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

/**
 * Class provides access to replication plugin configurations
 * parsed from {@link com.griddynamics.cd.internal.uploading.impl.ConfigurationsManagerImpl#CONFIG_FILENAME} file
 */
@Component(role = ConfigurationsManager.class, hint = "configurationsManager")
public class ConfigurationsManagerImpl extends ComponentSupport implements ConfigurationsManager {

    /**
     * Filename of the XML configuration file
     */
    private static final String CONFIG_FILENAME = "replication-plugin.xml";

    /**
     * Bean provides nexus server configurations
     */
    @Requirement
    private NexusConfiguration nexusConfiguration;

    /**
     * DTO contains plugin configurations
     */
    private ReplicationPluginConfiguration config;

    /**
     * Loads configurations
     * from {@link com.griddynamics.cd.internal.uploading.impl.ConfigurationsManagerImpl#CONFIG_FILENAME} file
     */
    @PostConstruct
    public void init() {
        log.trace("Initializing plugin configurations");
        reloadConfigurations();
    }

    /**
     * Provides access to plugin configurations DTO
     * @return Plugin configurations
     */
    @Override
    public ReplicationPluginConfiguration getConfiguration() {
        if (config == null) {
            reloadConfigurations();
        }
        return config;
    }

    /**
     * Reloads {@link com.griddynamics.cd.internal.uploading.impl.ConfigurationsManagerImpl#config}
     * from XML plugin configurations file
     */
    public void reloadConfigurations() {
        File file = getConfigurationFile();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(ReplicationPluginConfiguration.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            config = (ReplicationPluginConfiguration) jaxbUnmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            log.error("Can not deserialize xml configuration file: " + file.getAbsolutePath());
        }
    }

    /**
     * Returns XML plugin configurations file
     */
    private File getConfigurationFile() {
        return new File(nexusConfiguration.getConfigurationDirectory(), CONFIG_FILENAME);
    }
}
