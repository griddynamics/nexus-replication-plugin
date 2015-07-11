package com.griddynamics.cd.nrp.internal.uploading.impl.factories;

import com.griddynamics.cd.nrp.internal.model.config.ReplicationPluginConfigurationStorage;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Singleton
public class JerseyClientFactory {

    private final ReplicationPluginConfigurationStorage
            replicationPluginConfigurationStorage;
    private Logger log = LoggerFactory.getLogger(JerseyClientFactory.class);
    private ExecutorService executorService;

    @Inject
    public JerseyClientFactory(
            ReplicationPluginConfigurationStorage
                    replicationPluginConfigurationStorage) {
        this.replicationPluginConfigurationStorage = replicationPluginConfigurationStorage;

    }

    public void onActivate() {
        int requestQueueSize = replicationPluginConfigurationStorage.getRequestQueueSize();
        this.executorService = new ThreadPoolExecutor(
                replicationPluginConfigurationStorage.getRequestSendingThreadCount(),
                replicationPluginConfigurationStorage.getRequestSendingThreadCount(),
                30,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(
                        requestQueueSize));
    }

    /**
     * @param login    Login to Nexus
     * @param password Password to Nexus
     * @return Jersey Client for provide login and password with connection timeout set to 1000 ms and read timeout set to 2000
     *          This particular values for timeouts were chosen to avoid potential deadlock in case of peer server load
     */

    public Client getClient(String login, String password) {
        ClientConfig config = new DefaultClientConfig();
        config.getProperties().put(ClientConfig.PROPERTY_CONNECT_TIMEOUT, 1000);
        config.getProperties().put(ClientConfig.PROPERTY_READ_TIMEOUT, 2000);
        Client client = Client.create(config);
        client.setExecutorService(executorService);
        if (login != null && !login.isEmpty() && password != null) {
            log.debug("Creating HTTP client with authorized HTTPBasicAuthFilter.");
            client.addFilter(new HTTPBasicAuthFilter(login, password));
        } else {
            log.debug("Creating HTTP client with anonymous HTTPBasicAuthFilter.");
        }
        return client;
    }

}
