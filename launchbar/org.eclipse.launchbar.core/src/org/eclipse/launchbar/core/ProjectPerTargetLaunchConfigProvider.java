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
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.launchbar.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.launchbar.core.internal.Activator;
import org.eclipse.launchbar.core.target.ILaunchTarget;

public abstract class ProjectPerTargetLaunchConfigProvider extends PerTargetLaunchConfigProvider {

	@Override
	public boolean supports(ILaunchDescriptor descriptor, ILaunchTarget target) throws CoreException {
		return (descriptor.getAdapter(IProject.class) != null);
	}

	@Override
	protected void populateLaunchConfiguration(ILaunchDescriptor descriptor, ILaunchTarget target,
			ILaunchConfigurationWorkingCopy workingCopy) throws CoreException {
		super.populateLaunchConfiguration(descriptor, target, workingCopy);

		// Add our project to the mapped resources
		IProject project = descriptor.getAdapter(IProject.class);
		IResource[] mappedResources = workingCopy.getMappedResources();
		if (mappedResources == null || mappedResources.length == 0) {
			workingCopy.setMappedResources(new IResource[] { project });
		} else {
			IResource[] newResources = new IResource[mappedResources.length + 1];
			System.arraycopy(mappedResources, 0, newResources, 0, mappedResources.length);
			newResources[mappedResources.length] = project;
			workingCopy.setMappedResources(newResources);
		}
	}

	@Override
	protected ILaunchDescriptor getLaunchDescriptor(ILaunchConfiguration configuration) throws CoreException {
		IResource[] mappedResources = configuration.getMappedResources();
		if (mappedResources == null) {
			return null;
		}

		IProject project = null;
		for (IResource resource : mappedResources) {
			if (resource instanceof IProject)
				project = (IProject) resource;
		}
		if (project == null) {
			return null;
		}

		ILaunchBarManager manager = Activator.getService(ILaunchBarManager.class);
		return manager.launchObjectAdded(project);
	}

}
