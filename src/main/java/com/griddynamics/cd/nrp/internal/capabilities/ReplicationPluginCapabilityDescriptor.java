package com.griddynamics.cd.nrp.internal.capabilities;

import com.google.common.collect.Lists;
import org.sonatype.nexus.capability.support.CapabilityDescriptorSupport;
import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.formfields.NumberTextFormField;
import org.sonatype.nexus.formfields.StringTextFormField;
import org.sonatype.nexus.formfields.TextAreaFormField;
import org.sonatype.nexus.plugins.capabilities.*;
import org.sonatype.nexus.plugins.capabilities.support.validator.Validators;
import org.sonatype.sisu.goodies.i18n.I18N;
import org.sonatype.sisu.goodies.i18n.MessageBundle;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.Set;

import static org.sonatype.nexus.plugins.capabilities.CapabilityType.capabilityType;
import static org.sonatype.nexus.plugins.capabilities.Tag.categoryTag;
import static org.sonatype.nexus.plugins.capabilities.Tag.tags;

@Singleton
@Named(ReplicationPluginCapabilityDescriptor.TYPE_ID)
public class ReplicationPluginCapabilityDescriptor
        extends CapabilityDescriptorSupport
        implements Taggable {
    public static final String TYPE_ID = "nexus-replication-plugin";

    public static final CapabilityType TYPE = capabilityType(TYPE_ID);

    private static interface Messages
            extends MessageBundle {
        @DefaultMessage("Nexus Replication Plugin Configuration")
        String name();

        @DefaultMessage("Master server's URL prefix")
        String masterServerURLPrefixLabel();

        @DefaultMessage("Master server's URL prefix (e.g. http://localhost:8081/nexus")
        String masterServerURLPrefixHelp();

        @DefaultMessage("Request queue size")
        String requestQueueSizeLabel();

        @DefaultMessage("Request queue size (default 500)")
        String requestQueueSizeHelp();

        @DefaultMessage("Request sending thread count")
        String requestSendingThreadCountLabel();

        @DefaultMessage("Request sending thread count (default 1)")
        String requestSendingThreadCountHelp();

        @DefaultMessage("Request queue dump file")
        String requestQueueDumpFileNameLabel();

        @DefaultMessage("Request queue dump file (default /tmp/nexus-replication-plugin-queue-backup)")
        String requestQueueDumpFileNameHelp();

        @DefaultMessage("Slave servers (in JSON)")
        String serversLabel();

        @DefaultMessage("Slave servers in JSON, like: {\"servers\":[{\"url\":\"http://localhost:8083/nexus\",\"user\":\"admin\",\"password\":\"admin123\"},{\"url\":\"http://localhost:8082/nexus\",\"user\":\"admin\",\"password\":\"admin123\"}]}")
        String serversHelp();

    }

    private static final Messages messages = I18N.create(Messages.class);

    private final Validators validators;

    private final List<FormField> formFields;

    @Inject
    public ReplicationPluginCapabilityDescriptor(final Validators validators) {
        this.validators = validators;

        this.formFields = Lists.<FormField>newArrayList(
                new StringTextFormField(
                        ReplicationPluginCapabilityConfiguration.MASTER_SERVER_URL_PREFIX,
                        messages.masterServerURLPrefixLabel(),
                        messages.masterServerURLPrefixHelp(),
                        FormField.OPTIONAL
                ).withInitialValue("http://localhost:8081/nexus"),
                new NumberTextFormField(
                        ReplicationPluginCapabilityConfiguration.REQUESTS_QUEUE_SIZE,
                        messages.requestQueueSizeLabel(),
                        messages.requestQueueSizeHelp(),
                        FormField.OPTIONAL
                ).withInitialValue(500),
                new NumberTextFormField(
                        ReplicationPluginCapabilityConfiguration.REQUESTS_SENDING_THREADS_COUNT,
                        messages.requestSendingThreadCountLabel(),
                        messages.requestSendingThreadCountHelp(),
                        FormField.OPTIONAL
                ).withInitialValue(1),
                new StringTextFormField(
                        ReplicationPluginCapabilityConfiguration.QUEUE_DUMP_FILE_NAME,
                        messages.requestQueueDumpFileNameLabel(),
                        messages.requestQueueDumpFileNameHelp(),
                        FormField.OPTIONAL
                ).withInitialValue("/tmp/nexus-replication-plugin-queue-backup"),
                new TextAreaFormField(
                        ReplicationPluginCapabilityConfiguration.SERVERS,
                        messages.serversLabel(),
                        messages.serversHelp(),
                        FormField.OPTIONAL
                ).withInitialValue("{\"servers\":[{\"url\":\"http://localhost:8083/nexus\",\"user\":\"admin\",\"password\":\"admin123\"},{\"url\":\"http://localhost:8082/nexus\",\"user\":\"admin\",\"password\":\"admin123\"}]}")
        );
    }

    @Override
    public Validator validator() {
        return validators.logical().and(
                validators.capability().uniquePer(TYPE)
        );
    }

    @Override
    public Validator validator(final CapabilityIdentity id) {
        return validators.logical().and(
                validators.capability().uniquePerExcluding(id, TYPE)
        );
    }

    @Override
    public CapabilityType type() {
        return TYPE;
    }

    @Override
    public String name() {
        return messages.name();
    }

    @Override
    public List<FormField> formFields() {
        return formFields;
    }

    @Override
    public Set<Tag> getTags() {
        return tags(categoryTag("Grid"));
    }
}
