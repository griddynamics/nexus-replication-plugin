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
package com.griddynamics.cd.nrp.plugin;

import com.google.common.base.Preconditions;
import com.griddynamics.cd.nrp.internal.uploading.UploadEventListener;
import com.griddynamics.cd.nrp.internal.uploading.impl.UploadEventListenerImpl;
import org.eclipse.sisu.EagerSingleton;
import org.jetbrains.annotations.NonNls;
import org.sonatype.nexus.plugin.PluginIdentity;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import javax.inject.Inject;
import javax.inject.Named;

@Named
@EagerSingleton
public class ReplicationPlugin extends PluginIdentity {

    /**
     * Prefix for ID-like things.
     */
    @NonNls
    public static final String ID_PREFIX = "replication";

    /**
     * Expected groupId for plugin artifact.
     */
    @NonNls
    public static final String GROUP_ID = "com.griddynamics.cd";

    /**
     * Expected artifactId for plugin artifact.
     */
    @NonNls
    public static final String ARTIFACT_ID = "nexus-" + ID_PREFIX + "-plugin";

    /**
     * Initializes plugin and registers deploy event handler
     * @param eventBus Global nexus event bus
     * @param uploadEventListener Deploy event handler
     */
    @Inject
    public ReplicationPlugin(EventBus eventBus, @Named(UploadEventListenerImpl.ID) UploadEventListener uploadEventListener) throws Exception {
        super(GROUP_ID, ARTIFACT_ID);
        Preconditions.checkNotNull(eventBus).register(Preconditions.checkNotNull(uploadEventListener));
    }
}
