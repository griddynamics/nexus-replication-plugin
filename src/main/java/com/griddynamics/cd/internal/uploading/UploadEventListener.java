package com.griddynamics.cd.internal.uploading;

import org.sonatype.nexus.proxy.events.RepositoryItemEventStore;

public interface UploadEventListener {
    void onArtifactUploading(RepositoryItemEventStore event);
}
