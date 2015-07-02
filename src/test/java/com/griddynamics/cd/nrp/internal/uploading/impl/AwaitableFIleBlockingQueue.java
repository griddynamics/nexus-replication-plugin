package com.griddynamics.cd.nrp.internal.uploading.impl;

import com.griddynamics.cd.nrp.internal.model.api.ArtifactMetaInfo;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public class AwaitableFIleBlockingQueue extends FileBlockingQueue{

    private final CountDownLatch countDownLatch;

    public AwaitableFIleBlockingQueue(BlockingQueue<ArtifactMetaInfo> blockingQueue,
                                      String blockingQueueDumpFileName,
                                      int n) {
        super(blockingQueue, blockingQueueDumpFileName);
        this.countDownLatch = new CountDownLatch(n);
    }

    @Override
    public ArtifactMetaInfo take() throws InterruptedException {
        ArtifactMetaInfo retVal = super.take();
        countDownLatch.countDown();
        return retVal;
    }

    public void await() throws InterruptedException {
        countDownLatch.await();
    }

}
