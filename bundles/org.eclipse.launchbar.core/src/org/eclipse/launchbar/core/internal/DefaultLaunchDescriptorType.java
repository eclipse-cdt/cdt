package org.eclipse.launchbar.core.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.ILaunchDescriptorType;

/**
 * A special descriptor type that managed configurations that aren't owned
 * by other descriptor types.
 */
public class DefaultLaunchDescriptorType implements ILaunchDescriptorType {

	public static final String ID = Activator.PLUGIN_ID + ".descriptorType.default"; //$NON-NLS-1$

	private Map<ILaunchConfiguration, DefaultLaunchDescriptor> descriptors = new HashMap<>();

	@Override
	public boolean ownsLaunchObject(Object element) {
		return element instanceof ILaunchConfiguration;
	}

	@Override
	public ILaunchDescriptor getDescriptor(Object element) {
		if (element instanceof ILaunchConfiguration) {
			ILaunchConfiguration config = (ILaunchConfiguration) element;
			try {
				if( config.getType() != null &&  config.getType().isPublic() 
						&& !(config.getAttribute(ILaunchManager.ATTR_PRIVATE, false))) {

					DefaultLaunchDescriptor descriptor = descriptors.get(config);
					if (descriptor == null) {
						descriptor = new DefaultLaunchDescriptor(this, config);
						descriptors.put(config, descriptor);
					}
					return descriptor;
				}
			} catch(CoreException ce) {
				Activator.log(ce.getStatus());
			} 
		}

		return null;
	}

}
