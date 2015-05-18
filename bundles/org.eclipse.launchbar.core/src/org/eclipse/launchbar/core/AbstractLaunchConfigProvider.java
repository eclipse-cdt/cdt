package org.eclipse.launchbar.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.launchbar.core.internal.Activator;
import org.eclipse.remote.core.IRemoteConnection;

/**
 * Common launch config provider. Manages creating launch configurations and ensuring
 * duplicates are managed properly.
 */
public abstract class AbstractLaunchConfigProvider implements ILaunchConfigurationProvider {

	private static final String ORIGINAL_NAME = Activator.PLUGIN_ID + ".originalName"; //$NON-NLS-1$

	protected ILaunchConfiguration createLaunchConfiguration(ILaunchDescriptor descriptor, IRemoteConnection target) throws CoreException {
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		String name = launchManager.generateLaunchConfigurationName(descriptor.getName());
		ILaunchConfigurationWorkingCopy workingCopy = getLaunchConfigurationType(descriptor, target).newInstance(null, name);

		populateLaunchConfiguration(descriptor, workingCopy);

		return workingCopy.doSave();
	}
	
	protected void populateLaunchConfiguration(ILaunchDescriptor descriptor, ILaunchConfigurationWorkingCopy workingCopy)
			throws CoreException {
		// Leave our breadcrumb
		workingCopy.setAttribute(ORIGINAL_NAME, workingCopy.getName());
	}

	@Override
	public boolean ownsLaunchConfiguration(ILaunchConfiguration configuration) throws CoreException {
		// We created it if it has the same name we created it with.
		// This covers the case when the config was duplicated.
		// We can own only one, the original one.
		return configuration.getAttribute(ORIGINAL_NAME, "").equals(configuration.getName()); //$NON-NLS-1$
	}

}
