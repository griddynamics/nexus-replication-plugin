package com.griddynamics.cd.nrp.internal.uploading.impl.factories;

import com.griddynamics.cd.nrp.internal.model.api.ArtifactMetaInfo;
import com.griddynamics.cd.nrp.internal.uploading.ConfigurationsManager;
import com.griddynamics.cd.nrp.internal.uploading.impl.FileBlockingQueue;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Singleton
public class FileBlockingQueueFactory {

    private final ConfigurationsManager configurationsManager;

    @Inject
    public FileBlockingQueueFactory(ConfigurationsManager configurationsManager) {
        this.configurationsManager = configurationsManager;
    }

    public FileBlockingQueue getFileBlockingQueue() {
        BlockingQueue<ArtifactMetaInfo> blockingQueue =
                new LinkedBlockingQueue<>(configurationsManager.
                        getConfiguration().getRequestsQueueSize());
        String blockingQueueDumpFileName = configurationsManager.getConfiguration().getQueueDumpFileName();

        return new FileBlockingQueue(blockingQueue,
                blockingQueueDumpFileName);
    }

}
