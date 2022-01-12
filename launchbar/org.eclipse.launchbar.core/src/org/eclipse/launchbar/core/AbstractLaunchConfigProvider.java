/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Doug Schaefer
 *******************************************************************************/
package org.eclipse.launchbar.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.launchbar.core.internal.Activator;
import org.eclipse.launchbar.core.target.ILaunchTarget;

/**
 * Common launch config provider. Manages creating launch configurations and
 * ensuring duplicates are managed properly.
 */
public abstract class AbstractLaunchConfigProvider implements ILaunchConfigurationProvider {

	private static final String ATTR_ORIGINAL_NAME = Activator.PLUGIN_ID + ".originalName"; //$NON-NLS-1$
	private static final String ATTR_PROVIDER_CLASS = Activator.PLUGIN_ID + ".providerClass"; //$NON-NLS-1$

	protected ILaunchConfiguration createLaunchConfiguration(ILaunchDescriptor descriptor, ILaunchTarget target)
			throws CoreException {
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		String name = launchManager.generateLaunchConfigurationName(descriptor.getName());
		ILaunchConfigurationType type = getLaunchConfigurationType(descriptor, target);
		ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(null, name);

		populateLaunchConfiguration(descriptor, target, workingCopy);

		return workingCopy.doSave();
	}

	protected void populateLaunchConfiguration(ILaunchDescriptor descriptor, ILaunchTarget target,
			ILaunchConfigurationWorkingCopy workingCopy) throws CoreException {
		// Leave our breadcrumb
		workingCopy.setAttribute(ATTR_ORIGINAL_NAME, workingCopy.getName());
		workingCopy.setAttribute(ATTR_PROVIDER_CLASS, getClass().getName());
	}

	@Override
	public boolean launchDescriptorMatches(ILaunchDescriptor descriptor, ILaunchConfiguration configuration,
			ILaunchTarget target) throws CoreException {
		ILaunchConfiguration lc = descriptor.getAdapter(ILaunchConfiguration.class);
		if (lc == null)
			return false;
		return configuration.getName().equals(lc.getName());
	}

	protected boolean ownsLaunchConfiguration(ILaunchConfiguration configuration) throws CoreException {
		if (!configuration.exists()) {
			// can't own it if it doesn't exist
			return false;
		}

		if (configuration.getAttribute(ATTR_PROVIDER_CLASS, "").equals(getClass().getName())) { //$NON-NLS-1$
			// We provided the configuration but we need to check if this is a duplicate and
			// not own it. Check the original name and if there is still a config with that
			// name, this is the duplicate. Otherwise it's simply a rename
			String origName = configuration.getAttribute(ATTR_ORIGINAL_NAME, ""); //$NON-NLS-1$
			return origName.equals(configuration.getName())
					|| !DebugPlugin.getDefault().getLaunchManager().isExistingLaunchConfigurationName(origName);
		} else {
			return false;
		}
	}

}
