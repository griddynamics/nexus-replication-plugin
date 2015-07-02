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
import java.util.concurrent.atomic.AtomicReference;

@Named(value = "replicationPluginConfigurationStorage")
@Singleton
public class ReplicationPluginConfigurationStorage {

    Logger logger =
            LoggerFactory.getLogger(ReplicationPluginConfigurationStorage.class);

    private final AtomicReference<String> masterServerURLPrefix = new AtomicReference<>();
    private final AtomicReference<Integer> requestQueueSize = new AtomicReference<>();
    private final AtomicReference<Integer> requestSendingThreadCount = new AtomicReference<>();
    private final AtomicReference<String> requestQueueDumpFileName = new AtomicReference<>();;
    private final AtomicReference<Set<NexusServer>> servers = new AtomicReference<>();

    @Inject
    public ReplicationPluginConfigurationStorage() {
        servers.set(new LinkedHashSet<NexusServer>());
    }

    public String getMasterServerURLPrefix() {
        String retVal = masterServerURLPrefix.get();
        if(retVal == null){
            throw new RuntimeException("Replication plugin configuration not ready yet!");
        }
        return retVal;
    }

    public void setMasterServerURLPrefix(String masterServerURLPrefix) {
        this.masterServerURLPrefix.set(masterServerURLPrefix);
        logger.info(toString());
    }

    public String getRequestQueueDumpFileName() {
        String retVal =  requestQueueDumpFileName.get();
        if(retVal == null){
            throw new RuntimeException("Replication plugin configuration not ready yet!");
        }
        return retVal;
    }

    public void setRequestQueueDumpFileName(String requestQueueDumpFileName) {
        this.requestQueueDumpFileName.set(requestQueueDumpFileName);
        logger.info(toString());
    }

    public Set<NexusServer> getServers() {
        Set<NexusServer> retVal = servers.get();
        if(retVal == null){
            throw new RuntimeException("Replication plugin configuration not ready yet!");
        }
        return retVal;
    }

    public void setServers(Set<NexusServer> nexusServers) {
        servers.get().clear();
        servers.get().addAll(nexusServers);
        logger.info(toString());
    }

    public int getRequestSendingThreadCount() {
        Integer retVal = requestSendingThreadCount.get();
        if(retVal == null){
            throw new RuntimeException("Replication plugin configuration not ready yet!");
        }
        return retVal;
    }

    public void setRequestSendingThreadCount(int requestSendingThreadCount) {
        this.requestSendingThreadCount.set(requestSendingThreadCount);
        logger.info(toString());
    }

    public int getRequestQueueSize() {
        Integer retVal = requestQueueSize.get();
        if(retVal == null){
            throw new RuntimeException("Replication plugin configuration not ready yet!");
        }
        return retVal;
    }

    public void setRequestQueueSize(int requestQueueSize) {
        this.requestQueueSize.set(requestQueueSize);
        logger.info(toString());
    }

    @Override
    public String toString() {
        return "ReplicationPluginConfigurationStorage{" +
                "masterServerURLPrefix='" + masterServerURLPrefix + '\'' +
                ", requestQueueSize=" + requestQueueSize.get() +
                ", requestSendingThreadCount=" + requestSendingThreadCount.get() +
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
