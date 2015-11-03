/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.core.launch;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.launchbar.core.AbstractLaunchConfigProvider;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;

/**
 * Launch config provider for Qt projects running on the Local connection.
 * Simply uses the C++ Application launch config type.
 */
public class QtLocalLaunchConfigProvider extends AbstractLaunchConfigProvider {

	private Map<IProject, ILaunchConfiguration> configs = new HashMap<>();

	@Override
	public boolean supports(ILaunchDescriptor descriptor, ILaunchTarget target) throws CoreException {
		return ILaunchTargetManager.localLaunchTargetTypeId.equals(target.getTypeId());
	}

	@Override
	public ILaunchConfigurationType getLaunchConfigurationType(ILaunchDescriptor descriptor, ILaunchTarget target)
			throws CoreException {
		return DebugPlugin.getDefault().getLaunchManager()
				.getLaunchConfigurationType(QtLocalRunLaunchConfigDelegate.TYPE_ID);
	}

	@Override
	public ILaunchConfiguration getLaunchConfiguration(ILaunchDescriptor descriptor, ILaunchTarget target)
			throws CoreException {
		ILaunchConfiguration config = configs.get(descriptor);
		if (config == null) {
			config = createLaunchConfiguration(descriptor, target);
			configs.put(descriptor.getAdapter(IProject.class), config);
		}
		return config;
	}

	@Override
	protected void populateLaunchConfiguration(ILaunchDescriptor descriptor, ILaunchTarget target,
			ILaunchConfigurationWorkingCopy workingCopy) throws CoreException {
		super.populateLaunchConfiguration(descriptor, target, workingCopy);

		// Set the project and the connection
		QtLaunchDescriptor qtDesc = (QtLaunchDescriptor) descriptor;
		workingCopy.setMappedResources(new IResource[] { qtDesc.getProject() });
	}

	@Override
	public boolean launchConfigurationAdded(ILaunchConfiguration configuration) throws CoreException {
		if (ownsLaunchConfiguration(configuration)) {

		}
		return false;
	}

	@Override
	public boolean launchConfigurationRemoved(ILaunchConfiguration configuration) throws CoreException {
		for (Entry<IProject, ILaunchConfiguration> entry : configs.entrySet()) {
			if (configuration.equals(entry.getValue())) {
				configs.remove(entry.getKey());
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean launchConfigurationChanged(ILaunchConfiguration configuration) throws CoreException {
		// TODO not sure I care
		return false;
	}

	@Override
	public void launchDescriptorRemoved(ILaunchDescriptor descriptor) throws CoreException {
		IProject project = descriptor.getAdapter(IProject.class);
		if (project != null) {
			configs.remove(project);
		}
	}

	@Override
	public void launchTargetRemoved(ILaunchTarget target) throws CoreException {
		// nothing to do since the Local connection can't be removed
	}

}
