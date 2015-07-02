package com.griddynamics.cd.nrp.internal.uploading.impl.factories;

import com.griddynamics.cd.nrp.internal.uploading.ConfigurationsManager;
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

    private Logger log = LoggerFactory.getLogger(JerseyClientFactory.class);

    private final ExecutorService executorService;

    @Inject
    public JerseyClientFactory(ConfigurationsManager configurationsManager) {
        this.executorService = new ThreadPoolExecutor(
                configurationsManager.getConfiguration().getRequestsSendingThreadsCount(),
                configurationsManager.getConfiguration().getRequestsSendingThreadsCount(),
                30,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(
                        configurationsManager.getConfiguration().getRequestsQueueSize()));
    }

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
