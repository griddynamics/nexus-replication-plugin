/**
 * Copyright 2014, Grid Dynamics International, Inc.
 * Licensed under the Apache License, Version 2.0.
 * Classification level: Public
 */
package com.griddynamics.cd.nrp.internal.uploading;

import com.griddynamics.cd.nrp.internal.model.api.ArtifactMetaInfo;

public interface ArtifactUpdateApiClient {
    void sendRequest(ArtifactMetaInfo metaInfo);
}
