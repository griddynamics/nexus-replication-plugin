/**
 * Copyright 2014, Grid Dynamics International, Inc.
 * Licensed under the Apache License, Version 2.0.
 * Classification level: Public
 */
package com.griddynamics.cd.nrp.internal.model.api;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(exclude = {"isMd5Received", "isSha1Received", "isFileReceived"})
public class ArtifactStatus implements Serializable {
    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String classifier;
    private final String extension;
    private final String repositoryId;
    private final String nexusUrl;
    private boolean isMd5Received;
    private boolean isSha1Received;
    private boolean isFileReceived;

    public ArtifactStatus(ArtifactMetaInfo artifactMetaInfo) {
        this.groupId = artifactMetaInfo.getGroupId();
        this.artifactId = artifactMetaInfo.getArtifactId();
        this.version = artifactMetaInfo.getVersion();
        this.classifier = artifactMetaInfo.getClassifier();
        this.extension = artifactMetaInfo.getExtension();
        this.repositoryId = artifactMetaInfo.getRepositoryId();
        this.nexusUrl = artifactMetaInfo.getNexusUrl();
    }

    public boolean isReadyForReplication() {
        return isFileReceived && isSha1Received;
    }
}
