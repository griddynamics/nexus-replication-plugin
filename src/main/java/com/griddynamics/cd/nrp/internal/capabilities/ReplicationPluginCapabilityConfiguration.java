package com.griddynamics.cd.nrp.internal.capabilities;

import com.google.common.collect.Maps;
import com.griddynamics.cd.nrp.internal.model.config.ReplicationPluginConfigurationStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Strings.isNullOrEmpty;

public class ReplicationPluginCapabilityConfiguration {

    public static final String MASTER_SERVER_URL_PREFIX = "myUrl";
    public static final String REQUESTS_QUEUE_SIZE = "requestsQueueSize";
    public static final String REQUESTS_SENDING_THREADS_COUNT = "requestsSendingThreadsCount";
    public static final String QUEUE_DUMP_FILE_NAME = "queueDumpFileName";
    public static final String SERVER_URL = "server_url";
    public static final String SERVER_LOGIN = "server_user";
    public static final String SERVER_PASSWORD = "server_password";

    private final String masterServerURLPrefix;
    private final int requestQueueSize;
    private final int requestSendingThreadCount;
    private final String requestQueueDumpFileName;
    private final Set<ReplicationPluginConfigurationStorage.NexusServer> nexusServers;
    Logger logger =
            LoggerFactory.getLogger(ReplicationPluginCapabilityConfiguration.class);

    public ReplicationPluginCapabilityConfiguration(final Map<String, String> properties) {
        this.masterServerURLPrefix = properties.get(MASTER_SERVER_URL_PREFIX);
        this.requestQueueSize = Integer.parseInt(properties.get(REQUESTS_QUEUE_SIZE));
        this.requestSendingThreadCount = Integer.parseInt(properties.get(REQUESTS_SENDING_THREADS_COUNT));
        this.requestQueueDumpFileName = properties.get(QUEUE_DUMP_FILE_NAME);
        this.nexusServers = new LinkedHashSet<>();

        for (int i = 0; i < 5; i++) {
            String serverUrl = properties.get(SERVER_URL + "_" + i);
            String serverLogin = properties.get(SERVER_LOGIN + "_" + i);
            String serverPassword = properties.get(SERVER_PASSWORD + "_" + i);

            if (isNullOrEmpty(serverUrl) ||
                    isNullOrEmpty(serverLogin) ||
                    isNullOrEmpty(serverPassword)) {
                continue;
            }

            ReplicationPluginConfigurationStorage.NexusServer nexusServer = new ReplicationPluginConfigurationStorage.NexusServer(
                    serverUrl,
                    serverLogin,
                    serverPassword
            );
            nexusServers.add(nexusServer);
        }
    }

    public Map<String, String> asMap() {
        final Map<String, String> props = Maps.newHashMap();
        props.put(MASTER_SERVER_URL_PREFIX, masterServerURLPrefix);
        props.put(REQUESTS_QUEUE_SIZE, String.valueOf(requestQueueSize));
        props.put(REQUESTS_SENDING_THREADS_COUNT, String.valueOf(requestSendingThreadCount));
        props.put(QUEUE_DUMP_FILE_NAME, requestQueueDumpFileName);
        Iterator<ReplicationPluginConfigurationStorage.NexusServer> nexusServerIterator = nexusServers.iterator();
        for (int i = 0; nexusServerIterator.hasNext(); i++) {
            ReplicationPluginConfigurationStorage.NexusServer nexusServer = nexusServerIterator.next();
            props.put(SERVER_URL + "_" + i, nexusServer.getUrl());
            props.put(SERVER_LOGIN + "_" + i, nexusServer.getUser());
            props.put(SERVER_PASSWORD + "_" + i, nexusServer.getPassword());
        }
        return props;
    }

    public String getMasterServerURLPrefix() {
        return masterServerURLPrefix;
    }

    public int getRequestQueueSize() {
        return requestQueueSize;
    }

    public int getRequestSendingThreadCount() {
        return requestSendingThreadCount;
    }

    public String getRequestQueueDumpFileName() {
        return requestQueueDumpFileName;
    }

    public Set<ReplicationPluginConfigurationStorage.NexusServer> getNexusServers() {
        return nexusServers;
    }
}
