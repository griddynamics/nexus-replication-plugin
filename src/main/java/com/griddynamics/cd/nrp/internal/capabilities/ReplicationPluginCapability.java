package com.griddynamics.cd.nrp.internal.capabilities;

import com.griddynamics.cd.nrp.internal.model.config.ReplicationPluginConfigurationStorage;
import com.griddynamics.cd.nrp.internal.uploading.ArtifactUpdateApiClient;
import com.griddynamics.cd.nrp.internal.uploading.impl.ArtifactUpdateApiClientImpl;
import com.griddynamics.cd.nrp.internal.uploading.impl.factories.JerseyClientFactory;
import org.jetbrains.annotations.NonNls;
import org.sonatype.nexus.capability.support.CapabilitySupport;
import org.sonatype.nexus.plugins.capabilities.Condition;
import org.sonatype.nexus.plugins.capabilities.Evaluable;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

@Named(ReplicationPluginCapabilityDescriptor.TYPE_ID)
public class ReplicationPluginCapability
        extends CapabilitySupport<ReplicationPluginCapabilityConfiguration> {
    @NonNls
    public static final String NL = System.getProperty("line.separator");

    private final ReplicationPluginConfigurationStorage gridRegistry;
    private final ArtifactUpdateApiClientImpl artifactUpdateApiClient;
    private final JerseyClientFactory jerseyClientFactory;

    @Inject
    public ReplicationPluginCapability(ReplicationPluginConfigurationStorage replicationPluginConfigurationStorage,
                                       ArtifactUpdateApiClientImpl artifactUpdateApiClient,
                                       JerseyClientFactory jerseyClientFactory) {
        this.gridRegistry = checkNotNull(replicationPluginConfigurationStorage);
        this.artifactUpdateApiClient = artifactUpdateApiClient;
        this.jerseyClientFactory = jerseyClientFactory;
    }

    @Override
    protected ReplicationPluginCapabilityConfiguration createConfig(final Map<String, String> properties) throws Exception {
        Map<String, String> newProperties = new HashMap<>(properties);
        return new ReplicationPluginCapabilityConfiguration(newProperties);
    }

    @Override
    public void configure(final ReplicationPluginCapabilityConfiguration config){
        gridRegistry.setMasterServerURLPrefix(config.getMasterServerURLPrefix());
        gridRegistry.setRequestQueueSize(config.getRequestQueueSize());
        gridRegistry.setRequestSendingThreadCount(config.getRequestSendingThreadCount());
        gridRegistry.setRequestQueueDumpFileName(config.getRequestQueueDumpFileName());
        gridRegistry.setServers(config.getNexusServers());
        jerseyClientFactory.onActivate();
        artifactUpdateApiClient.onActivate();
    }

    @Override
    public Condition activationCondition() {
        return conditions().capabilities().evaluable(
                new Evaluable() {
                    @Override
                    public boolean isSatisfied() {
                        return true;
                    }

                    @Override
                    public String explainSatisfied() {
                        return "\"createrepo\" and \"mergerepo\" are available";
                    }

                    @Override
                    public String explainUnsatisfied() {
                        return "";
                    }
                }
        );
    }

    @Override
    protected String renderStatus() {
        return "Sample Nexus replication plugin status";
    }
}
