/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems. All Rights Reserved.
 *
 * You must obtain a written license from and pay applicable license fees to QNX
 * Software Systems before you may reproduce, modify or distribute this software,
 * or any work that includes all or part of this software.   Free development
 * licenses are available for evaluation and non-commercial purposes.  For more
 * information visit [http://licensing.qnx.com] or email licensing@qnx.com.
 *
 * This file may contain contributions from others.  Please review this entire
 * file for other proprietary rights or license notices, as well as the QNX
 * Development Suite License Guide at [http://licensing.qnx.com/license-guide/]
 * for other information.
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