/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.launch;

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

public class CoreBuildLocalLaunchConfigProvider extends AbstractLaunchConfigProvider {

	private static final String TYPE_ID = "org.eclipse.cdt.debug.core.localCoreBuildLaunchConfigType"; //$NON-NLS-1$

	private Map<IProject, ILaunchConfiguration> configs = new HashMap<>();

	@Override
	public boolean supports(ILaunchDescriptor descriptor, ILaunchTarget target) throws CoreException {
		return ILaunchTargetManager.localLaunchTargetTypeId.equals(target.getTypeId());
	}

	@Override
	public ILaunchConfigurationType getLaunchConfigurationType(ILaunchDescriptor descriptor, ILaunchTarget target)
			throws CoreException {
		return DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(TYPE_ID);
	}

	@Override
	public ILaunchConfiguration getLaunchConfiguration(ILaunchDescriptor descriptor, ILaunchTarget target)
			throws CoreException {
		ILaunchConfiguration config = null;
		IProject project = descriptor.getAdapter(IProject.class);
		if (project != null) {
			config = configs.get(project);
			if (config == null) {
				config = createLaunchConfiguration(descriptor, target);
				// launch config added will get called below to add it to the
				// configs map
			}
		}
		return config;
	}

	@Override
	protected void populateLaunchConfiguration(ILaunchDescriptor descriptor, ILaunchTarget target,
			ILaunchConfigurationWorkingCopy workingCopy) throws CoreException {
		super.populateLaunchConfiguration(descriptor, target, workingCopy);

		// Set the project and the connection
		IProject project = descriptor.getAdapter(IProject.class);
		workingCopy.setMappedResources(new IResource[] { project });
	}

	@Override
	public boolean launchConfigurationAdded(ILaunchConfiguration configuration) throws CoreException {
		if (ownsLaunchConfiguration(configuration)) {
			IProject project = configuration.getMappedResources()[0].getProject();
			configs.put(project, configuration);
			return true;
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
