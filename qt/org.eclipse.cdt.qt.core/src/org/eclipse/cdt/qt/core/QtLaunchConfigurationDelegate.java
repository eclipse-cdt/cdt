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

import org.eclipse.cdt.core.build.ICBuildConfigurationManager;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.internal.qt.core.Activator;
import org.eclipse.cdt.internal.qt.core.build.QtBuildConfigurationProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.launch.LaunchConfigurationTargetedDelegate;

public abstract class QtLaunchConfigurationDelegate extends LaunchConfigurationTargetedDelegate {

	@Override
	public boolean buildForLaunch(ILaunchConfiguration configuration, String mode, ILaunchTarget target,
			IProgressMonitor monitor) throws CoreException {
		IQtBuildConfiguration qtBuildConfig = getQtBuildConfiguration(configuration, mode, target, monitor);

		// If found, set as active, if not just return
		if (qtBuildConfig != null) {
			IProject project = qtBuildConfig.getBuildConfiguration().getProject();
			IProjectDescription desc = project.getDescription();
			desc.setActiveBuildConfig(qtBuildConfig.getBuildConfiguration().getName());
			project.setDescription(desc, monitor);
			return superBuildForLaunch(configuration, mode, monitor);
		} else {
			return false;
		}
	}

	@Override
	protected IProject[] getBuildOrder(ILaunchConfiguration configuration, String mode) throws CoreException {
		// 1. Extract project from configuration
		// TODO dependencies too.
		IProject project = configuration.getMappedResources()[0].getProject();
		return new IProject[] { project };
	}

	protected void populateToolChainProperties(ILaunchTarget target, Map<String, String> properties) {
		String os = target.getAttribute(ILaunchTarget.ATTR_OS, null);
		if (os != null) {
			properties.put(IToolChain.ATTR_OS, os.toLowerCase());
		}
		String arch = target.getAttribute(ILaunchTarget.ATTR_ARCH, null);
		if (arch != null) {
			properties.put(IToolChain.ATTR_ARCH, arch.toLowerCase());
		}
	}

	protected IQtBuildConfiguration getQtBuildConfiguration(ILaunchConfiguration configuration, String mode,
			ILaunchTarget target, IProgressMonitor monitor) throws CoreException {
		// Find the Qt build config
		ICBuildConfigurationManager configManager = Activator.getService(ICBuildConfigurationManager.class);
		QtBuildConfigurationProvider provider = (QtBuildConfigurationProvider) configManager
				.getProvider(QtBuildConfigurationProvider.ID);
		IProject project = configuration.getMappedResources()[0].getProject();

		Map<String, String> properties = new HashMap<>();
		populateToolChainProperties(target, properties);

		IQtBuildConfiguration qtConfig = provider.getConfiguration(project, properties, mode, monitor);
		if (qtConfig != null) {
			return qtConfig;
		}

		// Couldn't find any
		throw new CoreException(new Status(IStatus.ERROR, Activator.ID,
				String.format("No suitable SDK found for target %s.", target.getId())));
	}

}
