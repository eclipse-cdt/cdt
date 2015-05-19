package org.eclipse.launchbar.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.remote.core.IRemoteConnection;

/**
 * The launch config provider for the default descriptor which is the launch config itself.
 * 
 * Override this class and register an extension if you want to support targets other than the local connection.
 */
public class DefaultLaunchConfigProvider implements ILaunchConfigurationProvider {

	@Override
	public boolean supports(ILaunchDescriptor descriptor, IRemoteConnection target) throws CoreException {
		// Only supports Local connection
		return target.getConnectionType().getId().equals("org.eclipse.remote.LocalServices"); //$NON-NLS-1$
	}

	@Override
	public ILaunchConfigurationType getLaunchConfigurationType(ILaunchDescriptor descriptor, IRemoteConnection target)
			throws CoreException {
		return descriptor.getAdapter(ILaunchConfiguration.class).getType();
	}

	@Override
	public ILaunchConfiguration getLaunchConfiguration(ILaunchDescriptor descriptor, IRemoteConnection target)
			throws CoreException {
		return descriptor.getAdapter(ILaunchConfiguration.class);
	}

	@Override
	public boolean ownsLaunchConfiguration(ILaunchConfiguration configuration) throws CoreException {
		// return false so that the config is added as a launch object
		return false;
	}

	@Override
	public boolean launchConfigurationRemoved(ILaunchConfiguration configuration) throws CoreException {
		// catch any left over configs
		return true;
	}

	@Override
	public void launchDescriptorRemoved(ILaunchDescriptor descriptor) throws CoreException {
		// nothing to do
	}

	@Override
	public void launchTargetRemoved(IRemoteConnection target) throws CoreException {
		// nothing to do
	}

}
