/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia - Initial API and implementationn
 *******************************************************************************/
package org.eclipse.cdt.launchbar.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;

public abstract class ProjectBasedLaunchConfigurationProvider extends ConfigBasedLaunchConfigurationProvider {
	public ProjectBasedLaunchConfigurationProvider(String launchConfigurationTypeId) {
		super(launchConfigurationTypeId);
	}

	@Override
	public boolean launchConfigurationAdded(ILaunchConfiguration configuration) throws CoreException {
		if (!super.launchConfigurationAdded(configuration)) return false;
		IProject project = getProject(configuration);
		getManager().launchObjectChanged(project);
		return true;
	}

	@Override
	public boolean launchConfigurationRemoved(ILaunchConfiguration configuration) throws CoreException {
		if (!ownsConfiguration(configuration))
			return false;
		IProject project = (IProject) configMap.get(configuration); // cannot use getters from configuration, it is deleted
		if (!super.launchConfigurationRemoved(configuration)) return false;
		if (project != null)
			getManager().launchObjectChanged(project);
		return true;
	}

	protected void rememberConfiguration(ILaunchConfiguration configuration) {
		configMap.put(configuration, getProject(configuration));
	}

	protected abstract IProject getProject(ILaunchConfiguration llc);
}