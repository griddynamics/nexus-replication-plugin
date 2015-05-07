package com.griddynamics.cd.internal.uploading;

import com.griddynamics.cd.internal.model.api.ArtifactMetaInfo;

public interface ArtifactUpdateApiClient {
    void sendRequest(ArtifactMetaInfo metaInfo);
}
