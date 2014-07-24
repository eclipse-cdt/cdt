package org.eclipse.cdt.launchbar.core.internal;

import org.eclipse.cdt.launchbar.core.AbstarctLaunchDescriptorType;
import org.eclipse.cdt.launchbar.core.DefaultLaunchDescriptor;
import org.eclipse.cdt.launchbar.core.ILaunchDescriptor;
import org.eclipse.cdt.launchbar.core.ILaunchDescriptorType;
import org.eclipse.debug.core.ILaunchConfiguration;

public class DefaultLaunchDescriptorType extends AbstarctLaunchDescriptorType implements ILaunchDescriptorType {
	public static final String ID = "org.eclipse.cdt.launchbar.core.descriptor.default";

	@Override
	public String getId() {
		return ID;
	}
	@Override
	public boolean ownsLaunchObject(Object element) {
		return element instanceof ILaunchConfiguration;
	}

	@Override
	public ILaunchDescriptor getDescriptor(Object element) {
		return new DefaultLaunchDescriptor(this, (ILaunchConfiguration) element);
	}
}
