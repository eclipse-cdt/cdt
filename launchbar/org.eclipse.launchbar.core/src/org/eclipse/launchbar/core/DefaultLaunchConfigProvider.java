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
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;

/**
 * The launch config provider for the default descriptor which is the launch
 * config itself.
 *
 * Override this class and register an extension if you want to support targets
 * other than the local connection.
 */
public class DefaultLaunchConfigProvider implements ILaunchConfigurationProvider {
	@Override
	public boolean supports(ILaunchDescriptor descriptor, ILaunchTarget target) throws CoreException {
		// Only supports Local connection
		return target != null && target.getTypeId().equals(ILaunchTargetManager.localLaunchTargetTypeId);
	}

	@Override
	public ILaunchConfigurationType getLaunchConfigurationType(ILaunchDescriptor descriptor, ILaunchTarget target)
			throws CoreException {
		return descriptor.getAdapter(ILaunchConfiguration.class).getType();
	}

	@Override
	public ILaunchConfiguration getLaunchConfiguration(ILaunchDescriptor descriptor, ILaunchTarget target)
			throws CoreException {
		return descriptor.getAdapter(ILaunchConfiguration.class);
	}

	@Override
	public boolean launchConfigurationAdded(ILaunchConfiguration configuration) throws CoreException {
		// return false so that the configuration can become a launch object
		return false;
	}

	@Override
	public boolean launchConfigurationChanged(ILaunchConfiguration configuration) throws CoreException {
		return false;
	}

	@Override
	public boolean launchConfigurationRemoved(ILaunchConfiguration configuration) throws CoreException {
		return false;
	}

	@Override
	public void launchDescriptorRemoved(ILaunchDescriptor descriptor) throws CoreException {
		// nothing to do
	}

	@Override
	public void launchTargetRemoved(ILaunchTarget target) throws CoreException {
		// nothing to do
	}

	@Override
	public boolean launchDescriptorMatches(ILaunchDescriptor descriptor, ILaunchConfiguration configuration,
			ILaunchTarget target) throws CoreException {
		ILaunchConfiguration lc = descriptor.getAdapter(ILaunchConfiguration.class);
		if (lc == null)
			return false;
		return configuration.getName().equals(lc.getName());
	}
}
