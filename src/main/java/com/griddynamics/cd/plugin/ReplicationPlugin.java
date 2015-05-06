package com.griddynamics.cd.plugin;

import com.google.common.base.Preconditions;
import com.griddynamics.cd.internal.uploading.UploadEventListener;
import com.griddynamics.cd.internal.uploading.impl.UploadEventListenerImpl;
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
