package com.griddynamics.cd.nrp.internal.uploading.impl.factories;

import com.griddynamics.cd.nrp.internal.model.config.ReplicationPluginConfigurationStorage;
import com.griddynamics.cd.nrp.internal.model.api.ArtifactMetaInfo;
import com.griddynamics.cd.nrp.internal.uploading.impl.FileBlockingQueue;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Singleton
public class FileBlockingQueueFactory {

    private final ReplicationPluginConfigurationStorage
            replicationPluginConfigurationStorage;

    @Inject
    public FileBlockingQueueFactory(ReplicationPluginConfigurationStorage replicationPluginConfigurationStorage) {
        this.replicationPluginConfigurationStorage = replicationPluginConfigurationStorage;
    }

    public FileBlockingQueue getFileBlockingQueue() {
        BlockingQueue<ArtifactMetaInfo> blockingQueue =
                new LinkedBlockingQueue<>(
                        replicationPluginConfigurationStorage.getRequestQueueSize());
        String blockingQueueDumpFileName =
                replicationPluginConfigurationStorage.getRequestQueueDumpFileName();

        return new FileBlockingQueue(blockingQueue,
                blockingQueueDumpFileName);
    }

}
