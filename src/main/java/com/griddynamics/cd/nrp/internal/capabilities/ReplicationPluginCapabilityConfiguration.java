package com.griddynamics.cd.nrp.internal.capabilities;

import com.google.common.collect.Maps;
import com.griddynamics.cd.nrp.internal.model.config.ReplicationPluginConfigurationStorage;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ReplicationPluginCapabilityConfiguration {

    public static final String MASTER_SERVER_URL_PREFIX = "myUrl";
    public static final String REQUESTS_QUEUE_SIZE = "requestsQueueSize";
    public static final String REQUESTS_SENDING_THREADS_COUNT = "requestsSendingThreadsCount";
    public static final String QUEUE_DUMP_FILE_NAME = "queueDumpFileName";
    public static final String SERVERS = "servers";

    Logger logger =
            LoggerFactory.getLogger(ReplicationPluginCapabilityConfiguration.class);


    private final String masterServerURLPrefix;
    private final int requestQueueSize;
    private final int requestSendingThreadCount;
    private final String requestQueueDumpFileName;
    private final Set<ReplicationPluginConfigurationStorage.NexusServer> nexusServers;

    public ReplicationPluginCapabilityConfiguration(final Map<String, String> properties) {
        this.masterServerURLPrefix = properties.get(MASTER_SERVER_URL_PREFIX);
        this.requestQueueSize = Integer.parseInt(properties.get(REQUESTS_QUEUE_SIZE));
        this.requestSendingThreadCount = Integer.parseInt(properties.get(REQUESTS_SENDING_THREADS_COUNT));
        this.requestQueueDumpFileName = properties.get(QUEUE_DUMP_FILE_NAME);
        this.nexusServers = new LinkedHashSet<>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<Map> servers = (List<Map>) objectMapper.readValue(properties.get(SERVERS), Map.class).get("servers");
            for (Map server : servers) {
                this.nexusServers.add(
                        new ReplicationPluginConfigurationStorage.NexusServer(
                                (String) server.get("url"),
                                (String) server.get("user"),
                                (String) server.get("password")
                        )
                );
            }

        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
    }

    public Map<String, String> asMap() {
        final Map<String, String> props = Maps.newHashMap();
        props.put(MASTER_SERVER_URL_PREFIX, masterServerURLPrefix);
        props.put(REQUESTS_QUEUE_SIZE, String.valueOf(requestQueueSize));
        props.put(REQUESTS_SENDING_THREADS_COUNT, String.valueOf(requestSendingThreadCount));
        props.put(QUEUE_DUMP_FILE_NAME, requestQueueDumpFileName);
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
