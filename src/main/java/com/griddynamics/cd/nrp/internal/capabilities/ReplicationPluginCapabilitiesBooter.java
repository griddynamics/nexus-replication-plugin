package com.griddynamics.cd.nrp.internal.capabilities;

import javax.inject.Named;

import org.sonatype.nexus.plugins.capabilities.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.support.CapabilityBooterSupport;

import org.eclipse.sisu.EagerSingleton;

import java.util.Collections;

@Named
@EagerSingleton
public class ReplicationPluginCapabilitiesBooter
    extends CapabilityBooterSupport
{

  @Override
  protected void boot(final CapabilityRegistry registry)
      throws Exception
  {
    maybeAddCapability(
            registry,
            ReplicationPluginCapabilityDescriptor.TYPE,
            true, // enabled
            null, // no notes
            Collections.<String,String>emptyMap());
  }

}
