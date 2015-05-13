/**
 * Copyright 2014, Grid Dynamics International, Inc.
 * Licensed under the Apache License, Version 2.0.
 * Classification level: Public
 */
package com.griddynamics.cd.nrp.internal.model.config;

import lombok.*;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * DTO Class encapsulates replication nexus server api access configuration
 */
@NoArgsConstructor
@RequiredArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class NexusServer {
    @Getter
    @NonNull
    @XmlElement(name = "url")
    private String url;
    @Getter
    @NonNull
    @XmlElement(name = "user")
    private String user;
    @Getter
    @NonNull
    @XmlElement(name = "password")
    private String password;
}
