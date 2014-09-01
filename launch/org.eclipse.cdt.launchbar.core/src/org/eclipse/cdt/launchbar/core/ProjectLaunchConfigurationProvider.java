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
package org.eclipse.cdt.launchbar.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

/**
 * A root launch configuration provider that can be used with project launch descriptors.
 * Takes ownership of configurations we've created that map to the the project.
 */
public abstract class ProjectLaunchConfigurationProvider extends LaunchConfigurationProvider {

	@Override
	protected void populateConfiguration(ILaunchConfigurationWorkingCopy workingCopy, ILaunchDescriptor descriptor) throws CoreException {
		super.populateConfiguration(workingCopy, descriptor);

		// Add our project to the mapped resources
		IProject project = (IProject) descriptor.getAdapter(IProject.class);
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

	/**
	 * Extract the project from the launch configuration. Used when checking if we own it.
	 * 
	 * @param configuration
	 * @return project for launch configuration.
	 * @throws CoreException
	 */
	protected IProject getProject(ILaunchConfiguration configuration) throws CoreException {
		// by default return the first project in the mapped resources
		for (IResource resource : configuration.getMappedResources()) {
			if (resource instanceof IProject) {
				return (IProject) resource;
			}
		}

		return null;
	}

	@Override
	public Object launchConfigurationAdded(ILaunchConfiguration configuration) throws CoreException {
		if (!ownsConfiguration(configuration)) {
			return null;
		}

		IProject project = getProject(configuration);
		if (project == null) {
			// The user must have changed project. We don't own it any more in that case.
			return null;
		}

		return project;
	}

	@Override
	public boolean launchConfigurationRemoved(ILaunchConfiguration configuration) throws CoreException {
		return ownsConfiguration(configuration);
	}

}
