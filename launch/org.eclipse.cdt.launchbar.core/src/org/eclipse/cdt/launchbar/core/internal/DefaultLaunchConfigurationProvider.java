package org.eclipse.cdt.launchbar.core.internal;

import org.eclipse.cdt.launchbar.core.ILaunchBarManager;
import org.eclipse.cdt.launchbar.core.ILaunchConfigurationProvider;
import org.eclipse.cdt.launchbar.core.ILaunchDescriptor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;

public class DefaultLaunchConfigurationProvider implements ILaunchConfigurationProvider {

	@Override
	public void init(ILaunchBarManager manager) throws CoreException {
		// nothing to do
	}

	@Override
	public boolean launchConfigurationAdded(ILaunchConfiguration configuration) throws CoreException {
		// We may own it but return false to let it percolate through to the descriptor type.
		return false;
	}
	
	@Override
	public ILaunchConfiguration getLaunchConfiguration(ILaunchDescriptor descriptor) throws CoreException {
		if (descriptor instanceof DefaultLaunchDescriptor) {
			return ((DefaultLaunchDescriptor) descriptor).getConfig();
		}
		return null;
	}

	@Override
	public ILaunchConfigurationType getLaunchConfigurationType(ILaunchDescriptor descriptor) throws CoreException {
		if (descriptor instanceof DefaultLaunchDescriptor) {
			return ((DefaultLaunchDescriptor) descriptor).getConfig().getType();
		}
		return null;
	}

	@Override
	public boolean launchConfigurationRemoved(ILaunchConfiguration configation) throws CoreException {
		return false;
	}
	
}
