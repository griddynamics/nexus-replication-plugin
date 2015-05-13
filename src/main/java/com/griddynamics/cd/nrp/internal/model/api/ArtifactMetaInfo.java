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

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.*;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * DTO Class encapsulates data sent to replication nexus servers
 */
@ToString
@EqualsAndHashCode
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = ArtifactMetaInfo.NAME)
@XStreamAlias(value = ArtifactMetaInfo.NAME)
public class ArtifactMetaInfo implements Serializable {
    public static final String NAME = "artifact-meta-info";

    @Getter
    @NonNull
    private final String groupId;
    @Getter
    @NonNull
    private final String artifactId;
    @Getter
    @NonNull
    private final String version;
    @Getter
    @Setter
    private String packaging;
    @Getter
    @Setter
    private String classifier;
    @Getter
    @NonNull
    private final String repositoryId;
    @Getter
    @Setter
    private String extension;
    @Getter
    @NonNull
    private final String nexusUrl;

    public ArtifactMetaInfo() {
        this(null, null, null, null, null);
    }

    public ArtifactMetaInfo(String nexusUrl, String groupId, String artifactId, String version, String repositoryId) {
        this.nexusUrl = nexusUrl;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.repositoryId = repositoryId;
    }

    public boolean isValid() {
        return nexusUrl != null && groupId != null && artifactId != null && version != null && repositoryId != null;
    }
}
