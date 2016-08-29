/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.cmake.core.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.launch.LaunchConfigurationTargetedDelegate;

public class CMakeLaunchConfigurationDelegate extends LaunchConfigurationTargetedDelegate
		implements ILaunchConfigurationDelegate {

	public static final String TYPE_ID = "org.eclipse.cdt.cmake.core.launchConfigurationType"; //$NON-NLS-1$

	private ICBuildConfigurationManager configManager = Activator.getService(ICBuildConfigurationManager.class);
	private IToolChainManager tcManager = Activator.getService(IToolChainManager.class);

	private IProject getProject(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getMappedResources()[0].getProject();
	}

	@Override
	public boolean buildForLaunch(ILaunchConfiguration configuration, String mode, ILaunchTarget target,
			IProgressMonitor monitor) throws CoreException {
		// Set active build config based on toolchain for target
		Map<String, String> properties = new HashMap<>();
		String os = target.getAttribute(ILaunchTarget.ATTR_OS, ""); //$NON-NLS-1$
		if (!os.isEmpty()) {
			properties.put(IToolChain.ATTR_OS, os);
		}
		String arch = target.getAttribute(ILaunchTarget.ATTR_ARCH, ""); //$NON-NLS-1$
		if (!arch.isEmpty()) {
			properties.put(IToolChain.ATTR_ARCH, arch);
		}
		Collection<IToolChain> tcs = tcManager.getToolChainsMatching(properties);
		if (!tcs.isEmpty()) {
			IToolChain toolChain = tcs.iterator().next();

			IProject project = getProject(configuration);
			ICBuildConfiguration config = configManager.createBuildConfiguration(project, toolChain, "run", monitor); //$NON-NLS-1$

			if (config != null) {
				IProjectDescription desc = project.getDescription();
				desc.setActiveBuildConfig(config.getBuildConfiguration().getName());
				project.setDescription(desc, monitor);
			}
		}

		// proceed with the build
		return superBuildForLaunch(configuration, mode, monitor);
	}

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		// TODO need to find the binary and launch it.
		// Though, more likely, need to have launch configs per binary.
	}

	@Override
	protected IProject[] getBuildOrder(ILaunchConfiguration configuration, String mode) throws CoreException {
		// 1. Extract project from configuration
		// TODO dependencies too.
		IProject project = getProject(configuration);
		return new IProject[] { project };
	}

}
