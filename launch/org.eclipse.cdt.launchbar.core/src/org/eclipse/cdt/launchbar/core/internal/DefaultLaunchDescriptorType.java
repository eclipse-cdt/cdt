package org.eclipse.cdt.launchbar.core.internal;

import org.eclipse.cdt.launchbar.core.ILaunchBarManager;
import org.eclipse.cdt.launchbar.core.ILaunchDescriptor;
import org.eclipse.cdt.launchbar.core.ILaunchDescriptorType;
import org.eclipse.debug.core.ILaunchConfiguration;

public class DefaultLaunchDescriptorType implements ILaunchDescriptorType {

	public static final String ID = "org.eclipse.cdt.launchbar.core.descriptor.default";

	private ILaunchBarManager manager;

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void init(ILaunchBarManager manager) {
		this.manager = manager;
	}

	@Override
	public boolean ownsLaunchObject(Object element) {
		return element instanceof ILaunchConfiguration;
	}
	
	@Override
	public ILaunchDescriptor getDescriptor(Object element) {
		return new DefaultLaunchDescriptor(this, (ILaunchConfiguration) element);
	}

	@Override
	public ILaunchBarManager getManager() {
		return manager;
	}
	
}
