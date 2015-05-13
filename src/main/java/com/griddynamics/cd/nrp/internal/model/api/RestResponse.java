/**
 * Copyright 2014, Grid Dynamics International, Inc.
 * Licensed under the Apache License, Version 2.0.
 * Classification level: Public
 */
package com.griddynamics.cd.nrp.internal.model.api;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * DTO Class encapsulates response data sent from replication nexus servers back
 */
@Data
@NoArgsConstructor
@RequiredArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = RestResponse.NAME)
@XStreamAlias(value = RestResponse.NAME)
public class RestResponse implements Serializable {
    public static final String NAME = "rest-status";

    @NonNull
    private boolean isSuccess;
    @NonNull
    private String message;
}
