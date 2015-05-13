/**
 * Copyright 2015, Grid Dynamics International, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
