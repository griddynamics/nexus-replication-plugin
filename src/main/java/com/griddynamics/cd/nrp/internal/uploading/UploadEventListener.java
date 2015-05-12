package com.griddynamics.cd.nrp.internal.uploading;

import org.sonatype.nexus.proxy.events.RepositoryItemEventStore;

public interface UploadEventListener {
    void onArtifactUploading(RepositoryItemEventStore event);
}
