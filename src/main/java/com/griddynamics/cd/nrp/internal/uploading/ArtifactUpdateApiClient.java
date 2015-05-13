package com.griddynamics.cd.nrp.internal.uploading;

import com.griddynamics.cd.nrp.internal.model.api.ArtifactMetaInfo;

public interface ArtifactUpdateApiClient {
    void sendRequest(ArtifactMetaInfo metaInfo);
}
