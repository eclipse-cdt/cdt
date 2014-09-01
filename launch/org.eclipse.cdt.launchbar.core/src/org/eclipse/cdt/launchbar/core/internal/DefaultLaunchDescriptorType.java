package org.eclipse.cdt.launchbar.core.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.launchbar.core.ILaunchDescriptor;
import org.eclipse.cdt.launchbar.core.ILaunchDescriptorType;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * A special descriptor type that managed configurations that aren't owned
 * by other descriptor types.
 */
public class DefaultLaunchDescriptorType implements ILaunchDescriptorType {

	public static final String ID = Activator.PLUGIN_ID + ".descriptorType.default";

	private Map<ILaunchConfiguration, DefaultLaunchDescriptor> descriptors = new HashMap<>();

	@Override
	public boolean ownsLaunchObject(Object element) {
		// This descriptor type doesn't own any launch objects
		return false;
	}

	@Override
	public ILaunchDescriptor getDescriptor(Object element) {
		if (element instanceof ILaunchConfiguration) {
			ILaunchConfiguration config = (ILaunchConfiguration) element;
			DefaultLaunchDescriptor descriptor = descriptors.get(config);
			if (descriptor == null) {
				descriptor = new DefaultLaunchDescriptor(this, config);
				descriptors.put(config, descriptor);
			}
			return descriptor;
		}

		return null;
	}

}
