package org.eclipse.cdt.launchbar.core;

import org.eclipse.cdt.launchbar.core.internal.Activator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;

public abstract class LaunchConfigurationProvider implements ILaunchConfigurationProvider {

	// Used to make sure this is the config we've created
	private static final String ORIGINAL_NAME = Activator.PLUGIN_ID + ".originalName";

	@Override
	public ILaunchConfiguration createLaunchConfiguration(ILaunchDescriptor descriptor) throws CoreException {
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		String name = manager.generateLaunchConfigurationName(getConfigurationName(descriptor));
		ILaunchConfigurationWorkingCopy wc = getLaunchConfigurationType().newInstance(null, name);
		wc.setAttribute(ORIGINAL_NAME, name);
		populateConfiguration(wc, descriptor);
		return wc.doSave();
	}

	protected String getConfigurationName(ILaunchDescriptor descriptor) {
		// by default, use the descriptor name
		return descriptor.getName();
	}

	protected void populateConfiguration(ILaunchConfigurationWorkingCopy workingCopy, ILaunchDescriptor descriptor) throws CoreException {
		// by default, nothing to add
	}

	protected boolean createdConfiguration(ILaunchConfiguration configuration) throws CoreException {
		// we created it if it has the same name we created it with 
		return configuration.getAttribute(ORIGINAL_NAME, "").equals(configuration.getName());
	}

}
