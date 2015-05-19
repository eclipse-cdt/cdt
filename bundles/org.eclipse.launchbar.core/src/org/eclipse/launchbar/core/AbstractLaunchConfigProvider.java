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

	private static final String ATTR_ORIGINAL_NAME = Activator.PLUGIN_ID + ".originalName"; //$NON-NLS-1$
	private static final String ATTR_PROVIDER_CLASS = Activator.PLUGIN_ID + ".providerClass"; //$NON-NLS-1$

	protected ILaunchConfiguration createLaunchConfiguration(ILaunchDescriptor descriptor, IRemoteConnection target) throws CoreException {
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		String name = launchManager.generateLaunchConfigurationName(descriptor.getName());
		ILaunchConfigurationWorkingCopy workingCopy = getLaunchConfigurationType(descriptor, target).newInstance(null, name);

		populateLaunchConfiguration(descriptor, target, workingCopy);

		return workingCopy.doSave();
	}
	
	protected void populateLaunchConfiguration(ILaunchDescriptor descriptor, IRemoteConnection target,
			ILaunchConfigurationWorkingCopy workingCopy) throws CoreException {
		// Leave our breadcrumb
		workingCopy.setAttribute(ATTR_ORIGINAL_NAME, workingCopy.getName());
		workingCopy.setAttribute(ATTR_PROVIDER_CLASS, getClass().getName());
	}

	@Override
	public boolean ownsLaunchConfiguration(ILaunchConfiguration configuration) throws CoreException {
		// Check for our class name but also that the config name
		// matches what we originally set it to.
		// This covers the case when the config was duplicated.
		// We can own only one, the original one.
		return configuration.getAttribute(ATTR_PROVIDER_CLASS, "").equals(getClass().getName()) //$NON-NLS-1$
			&& configuration.getAttribute(ATTR_ORIGINAL_NAME, "").equals(configuration.getName()); //$NON-NLS-1$
	}

}
