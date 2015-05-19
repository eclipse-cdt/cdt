/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.launchbar.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.remote.core.IRemoteConnection;

public abstract class ProjectPerTargetLaunchConfigProvider extends PerTargetLaunchConfigProvider {

	@Override
	public boolean supports(ILaunchDescriptor descriptor, IRemoteConnection target) throws CoreException {
		return descriptor.getAdapter(IProject.class) != null;
	}

	@Override
	protected void populateLaunchConfiguration(ILaunchDescriptor descriptor, IRemoteConnection target, 
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

}
