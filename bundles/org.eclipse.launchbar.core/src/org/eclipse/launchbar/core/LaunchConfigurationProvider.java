/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer
 *******************************************************************************/
package org.eclipse.launchbar.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.launchbar.core.internal.Activator;

/**
 * A root class for launch configuration providers. Provides the ability to detect launch
 * configurations that it has created.
 */
public abstract class LaunchConfigurationProvider implements ILaunchConfigurationProvider {

	// Used to make sure this is the config we've created
	protected static final String ORIGINAL_NAME = Activator.PLUGIN_ID + ".originalName"; //$NON-NLS-1$

	@Override
	public ILaunchConfiguration createLaunchConfiguration(ILaunchManager launchManager, ILaunchDescriptor descriptor) throws CoreException {
		String name = launchManager.generateLaunchConfigurationName(getConfigurationName(descriptor));
		ILaunchConfigurationWorkingCopy wc = getLaunchConfigurationType().newInstance(null, name);
		wc.setAttribute(ORIGINAL_NAME, name);
		populateConfiguration(wc, descriptor);
		return wc.doSave();
	}

	/**
	 * Potential name for new configurations. Names are still put through the launch manager
	 * to ensure they are unique.
	 * 
	 * @param descriptor the launch descriptor triggering the configuration creation
	 * @return candidate configuration name
	 */
	protected String getConfigurationName(ILaunchDescriptor descriptor) {
		// by default, use the descriptor name
		return descriptor.getName();
	}

	/**
	 * Populate the new configuration with attributes and resources.
	 * 
	 * @param workingCopy working copy for the new configuration
	 * @param descriptor the launch descriptor that triggered the new configuration
	 * @throws CoreException
	 */
	protected void populateConfiguration(ILaunchConfigurationWorkingCopy workingCopy, ILaunchDescriptor descriptor) throws CoreException {
		// by default, nothing to add
	}

	/**
	 * Determines if we created this launch configuration. Generally used by the launch configuration
	 * add handler to determine if the incoming launch configuration is ours.
	 * 
	 * @param configuration
	 * @return do we own this launch configuration
	 * @throws CoreException
	 */
	protected boolean ownsConfiguration(ILaunchConfiguration configuration) throws CoreException {
		// must be the same config type
		if (!configuration.getType().equals(getLaunchConfigurationType()))
			return false;

		// we created it if it has the same name we created it with 
		return configuration.getAttribute(ORIGINAL_NAME, "").equals(configuration.getName()); //$NON-NLS-1$
	}

}
