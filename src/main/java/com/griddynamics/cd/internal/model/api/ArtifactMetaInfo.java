package com.griddynamics.cd.internal.model.api;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * DTO Class encapsulates data sent to replication nexus servers
 */
@SuppressWarnings( "all" )
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
    /**
     * Default value jar
     */
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
        this.packaging = "jar";
    }

    public boolean isValid() {
        return nexusUrl != null && groupId != null && artifactId != null && version != null && repositoryId != null;
    }
}
