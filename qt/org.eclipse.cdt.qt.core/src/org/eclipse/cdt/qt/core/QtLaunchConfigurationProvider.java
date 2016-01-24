/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.qt.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.cdt.internal.qt.core.launch.QtLaunchDescriptor;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.launchbar.core.AbstractLaunchConfigProvider;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.target.ILaunchTarget;

public abstract class QtLaunchConfigurationProvider extends AbstractLaunchConfigProvider {

	private Map<IProject, ILaunchConfiguration> configs = new HashMap<>();

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
		QtLaunchDescriptor qtDesc = (QtLaunchDescriptor) descriptor;
		workingCopy.setMappedResources(new IResource[] { qtDesc.getProject() });
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
