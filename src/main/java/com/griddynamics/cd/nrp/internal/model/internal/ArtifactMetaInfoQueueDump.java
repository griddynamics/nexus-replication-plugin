/*
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
package com.griddynamics.cd.nrp.internal.model.internal;

import com.griddynamics.cd.nrp.internal.model.api.ArtifactMetaInfo;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashSet;
import java.util.Set;

/**
 * DTO Class encapsulates artifact replication queue
 */
@XmlRootElement(name = "artifactMetaInfoBlockingQueueDump")
public class ArtifactMetaInfoQueueDump {
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
