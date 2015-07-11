package com.griddynamics.cd.nrp.internal.model.config;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * DTO Class for storing plugin's configuration:
 * - master server URL prefix
 * - request queue size
 * - request sending thread count
 * - request queue backup file
 * - peer servers
 * <p>
 * At first run, the default configuration is being created by ReplicationPluginCapabilitiesBooter, and passed to this class
 * <p>
 * Then every time Nexus is being restarted or plugin's settings changed, ReplicationPluginCapability passes new settings to this class
 */

@Named(value = "replicationPluginConfigurationStorage")
@Singleton
public class ReplicationPluginConfigurationStorage {

    Logger logger =
            LoggerFactory.getLogger(ReplicationPluginConfigurationStorage.class);

    private volatile String masterServerURLPrefix = null;
    private volatile Integer requestQueueSize = null;
    private volatile Integer requestSendingThreadCount = null;
    private volatile String requestQueueDumpFileName = null;
    private volatile Set<NexusServer> servers = null;

    @Inject
    public ReplicationPluginConfigurationStorage() {
    }

    public String getMasterServerURLPrefix() {
        checkNotNull(masterServerURLPrefix, "Replication plugin configuration not ready yet!");
        return this.masterServerURLPrefix;
    }

    public void setMasterServerURLPrefix(String masterServerURLPrefix) {
        checkNotNull(masterServerURLPrefix, "Replication plugin configuration parameter cannot be set to null");
        this.masterServerURLPrefix = masterServerURLPrefix;
        logger.info(toString());
    }

    public String getRequestQueueDumpFileName() {
        checkNotNull(requestQueueDumpFileName, "Replication plugin configuration not ready yet!");
        return requestQueueDumpFileName;
    }

    public void setRequestQueueDumpFileName(String requestQueueDumpFileName) {
        checkNotNull(requestQueueDumpFileName, "Replication plugin configuration parameter cannot be set to null");
        this.requestQueueDumpFileName = requestQueueDumpFileName;
        logger.info(toString());
    }

    public Set<NexusServer> getServers() {
        checkNotNull(servers, "Replication plugin configuration not ready yet!");
        return servers;
    }

    public void setServers(Set<NexusServer> nexusServers) {
        checkNotNull(nexusServers, "Replication plugin configuration parameter cannot be set to null");
        servers = new LinkedHashSet<>(nexusServers);
        logger.info(toString());
    }

    public int getRequestSendingThreadCount() {
        checkNotNull(requestSendingThreadCount, "Replication plugin configuration not ready yet!");
        return requestSendingThreadCount;
    }

    public void setRequestSendingThreadCount(Integer requestSendingThreadCount) {
        checkNotNull(requestSendingThreadCount, "Replication plugin configuration parameter cannot be set to null");
        this.requestSendingThreadCount = requestSendingThreadCount;
        logger.info(toString());
    }

    public int getRequestQueueSize() {
        checkNotNull(requestQueueSize, "Replication plugin configuration not ready yet!");
        return requestQueueSize;
    }

    public void setRequestQueueSize(Integer requestQueueSize) {
        checkNotNull(requestQueueSize, "Replication plugin configuration parameter cannot be set to null");
        this.requestQueueSize = requestQueueSize;
        logger.info(toString());
    }

    @Override
    public String toString() {
        return "ReplicationPluginConfigurationStorage{" +
                "masterServerURLPrefix='" + masterServerURLPrefix + '\'' +
                ", requestQueueSize=" + requestQueueSize +
                ", requestSendingThreadCount=" + requestSendingThreadCount +
                ", requestQueueDumpFileName='" + requestQueueDumpFileName + '\'' +
                ", servers=" + servers +
                '}';
    }

    @NoArgsConstructor
    @RequiredArgsConstructor
    @ToString
    public static class NexusServer {
        @NonNull
        private String url;
        @NonNull
        private String user;
        @NonNull
        private String password;

        public String getUrl() {
            return url;
        }

        public String getUser() {
            return user;
        }

        public String getPassword() {
            return password;
        }
    }
}
