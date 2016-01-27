/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.qt.core;

import org.eclipse.cdt.internal.qt.core.build.QtBuildConfigurationFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.launch.LaunchConfigurationTargetedDelegate;

public abstract class QtLaunchConfigurationDelegate extends LaunchConfigurationTargetedDelegate {

	@Override
	public boolean buildForLaunch(ILaunchConfiguration configuration, String mode, ILaunchTarget target,
			IProgressMonitor monitor) throws CoreException {
		QtBuildConfiguration qtBuildConfig = getQtBuildConfiguration(configuration, mode, target, monitor);

		// Set it as active
		if (qtBuildConfig != null) {
			IProject project = qtBuildConfig.getProject();
			IProjectDescription desc = project.getDescription();
			desc.setActiveBuildConfig(qtBuildConfig.getBuildConfiguration().getName());
			project.setDescription(desc, monitor);
		}

		// And build
		return superBuildForLaunch(configuration, mode, monitor);
	}

	@Override
	protected IProject[] getBuildOrder(ILaunchConfiguration configuration, String mode) throws CoreException {
		// 1. Extract project from configuration
		// TODO dependencies too.
		IProject project = configuration.getMappedResources()[0].getProject();
		return new IProject[] { project };
	}

	protected QtBuildConfiguration getQtBuildConfiguration(ILaunchConfiguration configuration, String mode,
			ILaunchTarget target, IProgressMonitor monitor) throws CoreException {
		// Find the Qt build config
		IProject project = configuration.getMappedResources()[0].getProject();
		return QtBuildConfigurationFactory.getConfig(project, mode, target, monitor);
	}

}
