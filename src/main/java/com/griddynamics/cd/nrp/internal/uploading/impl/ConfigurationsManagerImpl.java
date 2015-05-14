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

import com.griddynamics.cd.nrp.internal.model.config.ReplicationPluginConfiguration;
import com.griddynamics.cd.nrp.internal.uploading.ConfigurationsManager;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

/**
 * Class provides access to plugin configurations
 * parsed from {@link ConfigurationsManagerImpl#CONFIG_FILENAME} file
 */
@Singleton
@Named(value = ConfigurationsManagerImpl.ID)
public class ConfigurationsManagerImpl extends ComponentSupport implements ConfigurationsManager {

    public static final String ID = "configurationsManager";

    /**
     * Filename of the XML configuration file
     */
    private static final String CONFIG_FILENAME = "replication-plugin.xml";

    /**
     * Bean provides nexus server configurations
     */
    private NexusConfiguration nexusConfiguration;

    /**
     * DTO contains plugin configurations
     */
    private ReplicationPluginConfiguration config;

    @Inject
    public ConfigurationsManagerImpl(NexusConfiguration nexusConfiguration) {
        this.nexusConfiguration = nexusConfiguration;
    }

    /**
     * Loads configurations
     * from {@link ConfigurationsManagerImpl#CONFIG_FILENAME} file
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
            synchronized (this) {
                if (config == null) {
                    reloadConfigurations();
                }
            }
        }
        return config;
    }

    /**
     * Reloads {@link ConfigurationsManagerImpl#config}
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
     * Returns plugin configurations XML file
     */
    private File getConfigurationFile() {
        return new File(nexusConfiguration.getConfigurationDirectory(), CONFIG_FILENAME);
    }
}
