package com.griddynamics.cd.nrp.internal.capabilities;

import org.sonatype.nexus.capability.support.CapabilityDescriptorSupport;
import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.formfields.NumberTextFormField;
import org.sonatype.nexus.formfields.PasswordFormField;
import org.sonatype.nexus.formfields.StringTextFormField;
import org.sonatype.nexus.plugins.capabilities.*;
import org.sonatype.nexus.plugins.capabilities.support.validator.Validators;
import org.sonatype.sisu.goodies.i18n.I18N;
import org.sonatype.sisu.goodies.i18n.MessageBundle;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ArrayList;
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
    private static final Messages messages = I18N.create(Messages.class);
    private final Validators validators;
    private final List<FormField> formFields;

    @Inject
    public ReplicationPluginCapabilityDescriptor(final Validators validators) {
        this.validators = validators;

        List<FormField> formFields = new ArrayList<>();
        formFields.add(new StringTextFormField(
                ReplicationPluginCapabilityConfiguration.MASTER_SERVER_URL_PREFIX,
                messages.masterServerURLPrefixLabel(),
                messages.masterServerURLPrefixHelp(),
                FormField.OPTIONAL
        ).withInitialValue("http://localhost:8081/nexus"));
        formFields.add(new NumberTextFormField(ReplicationPluginCapabilityConfiguration.REQUESTS_QUEUE_SIZE,
                messages.requestQueueSizeLabel(),
                messages.requestQueueSizeHelp(),
                FormField.OPTIONAL
        ).withInitialValue(500));
        formFields.add(new NumberTextFormField(
                ReplicationPluginCapabilityConfiguration.REQUESTS_SENDING_THREADS_COUNT,
                messages.requestSendingThreadCountLabel(),
                messages.requestSendingThreadCountHelp(),
                FormField.OPTIONAL
        ).withInitialValue(1));
        formFields.add(new StringTextFormField(
                ReplicationPluginCapabilityConfiguration.QUEUE_DUMP_FILE_NAME,
                messages.requestQueueDumpFileNameLabel(),
                messages.requestQueueDumpFileNameHelp(),
                FormField.OPTIONAL
        ).withInitialValue("/tmp/nexus-replication-plugin-queue-backup"));

        for (int i = 0; i < 5; i++) {
            formFields.add(new StringTextFormField(
                    ReplicationPluginCapabilityConfiguration.SERVER_URL + "_" + i,
                    messages.serverUrlLabel() + " (Instance nr " + (i + 1) + ")",
                    messages.serverUrlHelp(),
                    FormField.OPTIONAL
            ).withInitialValue(""));
            formFields.add(new StringTextFormField(
                    ReplicationPluginCapabilityConfiguration.SERVER_LOGIN + "_" + i,
                    messages.serverLoginLabel() + " (Instance nr " + (i + 1) + ")",
                    messages.serverLoginHelp(),
                    FormField.OPTIONAL
            ).withInitialValue(""));
            formFields.add(new PasswordFormField(
                    ReplicationPluginCapabilityConfiguration.SERVER_PASSWORD + "_" + i,
                    messages.serverPasswordLabel() + " (Instance nr " + (i + 1) + ")",
                    messages.serverPasswordHelp(),
                    FormField.OPTIONAL
            ).withInitialValue(""));
        }
        this.formFields = formFields;
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

        @DefaultMessage("Peer server's url")
        String serverUrlLabel();

        @DefaultMessage("Peer server's url (like http://localhost:8083/nexus)")
        String serverUrlHelp();

        @DefaultMessage("Peer server's login")
        String serverLoginLabel();

        @DefaultMessage("Peer server's login (default: admin)")
        String serverLoginHelp();

        @DefaultMessage("Peer server's password")
        String serverPasswordLabel();

        @DefaultMessage("Peer server's password (default: admin123")
        String serverPasswordHelp();

    }
}
