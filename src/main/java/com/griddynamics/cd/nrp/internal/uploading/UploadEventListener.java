/**
 * Copyright 2014, Grid Dynamics International, Inc.
 * Licensed under the Apache License, Version 2.0.
 * Classification level: Public
 */
package com.griddynamics.cd.nrp.internal.uploading;

import org.sonatype.nexus.proxy.events.RepositoryItemEventStore;

public interface UploadEventListener {
    void onArtifactUploading(RepositoryItemEventStore event);
}
