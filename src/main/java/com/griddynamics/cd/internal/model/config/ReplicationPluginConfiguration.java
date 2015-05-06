package com.griddynamics.cd.internal.model.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashSet;
import java.util.Set;

/**
 * DTO Class encapsulates replication plugin configurations
 */
@NoArgsConstructor
@RequiredArgsConstructor
@XmlRootElement(name = "configurations")
public class ReplicationPluginConfiguration {
    @Getter
    @XmlElement(name = "server")
    @XmlElementWrapper(name = "servers")
    private final Set<NexusServer> servers = new HashSet<>();
    @Getter
    @NonNull
    @XmlAttribute(name = "meUrl")
    private String meUrl;

    public void addServer(NexusServer server) {
        servers.add(server);
    }
}
