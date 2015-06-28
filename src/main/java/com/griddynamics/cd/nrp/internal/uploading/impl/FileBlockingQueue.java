package com.griddynamics.cd.nrp.internal.uploading.impl;

import com.google.common.collect.Sets;
import com.griddynamics.cd.nrp.internal.model.api.ArtifactMetaInfo;
import com.griddynamics.cd.nrp.internal.model.internal.ArtifactMetaInfoQueueDump;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class FileBlockingQueue {

    private final BlockingQueue<ArtifactMetaInfo> internalBlockingQueue;
    private final String blockingQueueDumpFileName;

    private Logger log = LoggerFactory.getLogger(FileBlockingQueue.class);

    public FileBlockingQueue(BlockingQueue<ArtifactMetaInfo> blockingQueue, String blockingQueueDumpFileName) {
        this.internalBlockingQueue = blockingQueue;
        this.blockingQueueDumpFileName = blockingQueueDumpFileName;
    }

    public boolean offer(ArtifactMetaInfo e, long timeout, TimeUnit timeUnit) throws InterruptedException {
        synchronized (internalBlockingQueue) {
            boolean retVal = internalBlockingQueue.offer(e, timeout, timeUnit);
            saveQueueToFile();
            internalBlockingQueue.notify();
            return retVal;
        }
    }

    public ArtifactMetaInfo peek() throws InterruptedException {
        synchronized (internalBlockingQueue) {
            while (internalBlockingQueue.isEmpty()) {
                internalBlockingQueue.wait();
            }
            return internalBlockingQueue.peek();
        }
    }

    public ArtifactMetaInfo take() throws InterruptedException {
        synchronized (internalBlockingQueue) {
            while (internalBlockingQueue.isEmpty()) {
                internalBlockingQueue.wait();
            }
            ArtifactMetaInfo retVal = internalBlockingQueue.take();
            saveQueueToFile();
            return retVal;

        }
    }

    private synchronized void saveQueueToFile() {

        try {
            String backupBlockingQueueDumpFileName = blockingQueueDumpFileName + ".bak";
            File blockingQueueDumpFile = new File(blockingQueueDumpFileName);
            if (blockingQueueDumpFile.exists() && !blockingQueueDumpFile.isDirectory()) {
                FileUtils.copyFile(blockingQueueDumpFile, new File(backupBlockingQueueDumpFileName));
            }
            JAXBContext jaxbContext = JAXBContext.newInstance(ArtifactMetaInfoQueueDump.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            ArtifactMetaInfoQueueDump artifactMetaInfoBlockingQueueDump =
                    new ArtifactMetaInfoQueueDump();
            artifactMetaInfoBlockingQueueDump.addAllArtifactMetaInfo(Sets.newHashSet(internalBlockingQueue));
            marshaller.marshal(artifactMetaInfoBlockingQueueDump, blockingQueueDumpFile);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }


    }

}
