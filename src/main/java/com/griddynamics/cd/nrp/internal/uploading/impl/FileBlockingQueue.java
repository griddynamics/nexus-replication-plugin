package com.griddynamics.cd.nrp.internal.uploading.impl;

import com.google.common.collect.Sets;
import com.griddynamics.cd.nrp.internal.model.api.ArtifactMetaInfo;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Stores artifacts that Plugin failed to send to another Nexus instance. If that happens, we don't want
 * to loose the history of updates and therefore we'll retry sending the requests even if Nexus Sender
 * was restarted.
 */
public class FileBlockingQueue {

    private final BlockingQueue<ArtifactMetaInfo> internalBlockingQueue;
    private final String blockingQueueDumpFileName;

    private final Logger log = LoggerFactory.getLogger(FileBlockingQueue.class);

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

    /**
     * DTO Class encapsulates artifact replication queue
     */
    @XmlRootElement(name = "artifactMetaInfoBlockingQueueDump")
    public static class ArtifactMetaInfoQueueDump {
        @XmlElement(name = "artifactMetaInfo")
        @XmlElementWrapper(name = "artifactMetaInfos")
        private final Set<ArtifactMetaInfo> artifactMetaInfos = new HashSet<>();

        public void addArtifactMetaInfo(ArtifactMetaInfo artifactMetaInfo) {
            artifactMetaInfos.add(artifactMetaInfo);
        }
        public void addAllArtifactMetaInfo(Set<ArtifactMetaInfo> artifactMetaInfo) {
            artifactMetaInfos.addAll(artifactMetaInfo);
        }

        public Set<ArtifactMetaInfo> getArtifactMetaInfos() {
            return artifactMetaInfos;
        }

    }
}
