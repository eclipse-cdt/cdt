/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.debug.core.launch;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.internal.core.InternalDebugCoreMessages;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.launch.LaunchConfigurationTargetedDelegate;

/**
 * Common launch delegate code for core build launches.
 * 
 * @since 8.1
 */
public abstract class CoreBuildLaunchConfigDelegate extends LaunchConfigurationTargetedDelegate {

	protected ICBuildConfigurationManager configManager = CDebugCorePlugin
			.getService(ICBuildConfigurationManager.class);
	protected IToolChainManager toolChainManager = CDebugCorePlugin.getService(IToolChainManager.class);

	public static IProject getProject(ILaunchConfiguration configuration) throws CoreException {
		// TODO - make sure this is really the correct project
		return configuration.getMappedResources()[0].getProject();
	}

	@Override
	protected IProject[] getProjectsForProblemSearch(ILaunchConfiguration configuration, String mode)
			throws CoreException {
		IProject project = getProject(configuration);
		return project != null ? new IProject[] { project } : new IProject[0];
	}

	/**
	 * @deprecated Use the version that takes the launch config so we can see if it
	 *             know what toolchain to use.
	 */
	@Deprecated
	protected ICBuildConfiguration getBuildConfiguration(IProject project, String mode, ILaunchTarget target,
			IProgressMonitor monitor) throws CoreException {
		// Pick build config based on toolchain for target
		Map<String, String> properties = new HashMap<>();
		properties.putAll(target.getAttributes());
		Collection<IToolChain> tcs = toolChainManager.getToolChainsMatching(properties);
		if (!tcs.isEmpty()) {
			IToolChain toolChain = tcs.iterator().next();
			return configManager.getBuildConfiguration(project, toolChain, mode, monitor);
		} else {
			return null;
		}
	}

	/**
	 * @since 8.3
	 */
	protected ICBuildConfiguration getBuildConfiguration(ILaunchConfiguration configuration, String mode,
			ILaunchTarget target, IProgressMonitor monitor) throws CoreException {
		IProject project = getProject(configuration);
		String toolchainId = configuration.getAttribute(ICBuildConfiguration.TOOLCHAIN_ID, (String) null);
		if (toolchainId != null) {
			String providerId = configuration.getAttribute(ICBuildConfiguration.TOOLCHAIN_TYPE, ""); //$NON-NLS-1$
			IToolChain toolchain = toolChainManager.getToolChain(providerId, toolchainId);
			if (toolchain != null) {
				return configManager.getBuildConfiguration(project, toolchain, mode, monitor);
			}
		}

		// Pick the first one that matches
		Map<String, String> properties = new HashMap<>();
		properties.putAll(target.getAttributes());
		for (IToolChain toolChain : toolChainManager.getToolChainsMatching(properties)) {
			ICBuildConfiguration buildConfig = configManager.getBuildConfiguration(project, toolChain, mode, monitor);
			if (buildConfig != null) {
				return buildConfig;
			}
		}

		return null;
	}

	protected IBinary getBinary(ICBuildConfiguration buildConfig) throws CoreException {
		IBinary[] binaries = buildConfig.getBuildOutput();
		IBinary exeFile = null;
		for (IBinary binary : binaries) {
			if (binary.isExecutable()) {
				exeFile = binary;
				break;
			}
		}
		if (exeFile == null) {
			throw new CoreException(new Status(IStatus.ERROR, CDebugCorePlugin.PLUGIN_ID, InternalDebugCoreMessages.CoreBuildLaunchConfigDelegate_noBinaries));
		}
		return exeFile;
	}
	
	@Override
	protected IProject[] getBuildOrder(ILaunchConfiguration configuration, String mode) throws CoreException {
		// 1. Extract project from configuration
		// TODO dependencies too.
		IProject project = getProject(configuration);
		return new IProject[] { project };
	}

	/**
	 * @deprecated Store build properties right on the build configs
	 */
	@Deprecated
	public static String getBuildAttributeName(String mode) {
		return "COREBUILD_" + mode; //$NON-NLS-1$
	}

	@Override
	public boolean buildForLaunch(ILaunchConfiguration configuration, String mode, ILaunchTarget target,
			IProgressMonitor monitor) throws CoreException {
		ICBuildConfiguration buildConfig = getBuildConfiguration(configuration, mode, target, monitor);
		if (buildConfig != null) {
			IProject project = getProject(configuration);
			IProjectDescription desc = project.getDescription();
			desc.setActiveBuildConfig(buildConfig.getBuildConfiguration().getName());
			project.setDescription(desc, monitor);
		}

		// proceed with the build
		return superBuildForLaunch(configuration, mode, monitor);
	}

}
