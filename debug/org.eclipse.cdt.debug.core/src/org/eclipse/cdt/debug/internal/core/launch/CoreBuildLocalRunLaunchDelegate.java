/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.launch;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.utils.Platform;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.launch.ITargetedLaunch;
import org.eclipse.launchbar.core.target.launch.LaunchConfigurationTargetedDelegate;

public class CoreBuildLocalRunLaunchDelegate extends LaunchConfigurationTargetedDelegate {

	public static final String TYPE_ID = "org.eclipse.cdt.cmake.core.launchConfigurationType"; //$NON-NLS-1$

	private ICBuildConfigurationManager configManager = CDebugCorePlugin.getService(ICBuildConfigurationManager.class);
	private IToolChainManager tcManager = CDebugCorePlugin.getService(IToolChainManager.class);

	private IProject getProject(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getMappedResources()[0].getProject();
	}

	@Override
	public boolean buildForLaunch(ILaunchConfiguration configuration, String mode, ILaunchTarget target,
			IProgressMonitor monitor) throws CoreException {
		// Set active build config based on toolchain for target
		Map<String, String> properties = new HashMap<>();
		properties.put(IToolChain.ATTR_OS, Platform.getOS());
		properties.put(IToolChain.ATTR_ARCH, Platform.getOSArch());
		// TODO should really use real architecture of platform, not what Eclipse is using.
		// Also on 64-bit platforms, try 32-bit if toolchains not found
		Collection<IToolChain> tcs = tcManager.getToolChainsMatching(properties);
		if (!tcs.isEmpty()) {
			IToolChain toolChain = tcs.iterator().next();

			IProject project = getProject(configuration);
			ICBuildConfiguration config = configManager.getBuildConfiguration(project, toolChain, "run", monitor); //$NON-NLS-1$

			if (config != null) {
				IProjectDescription desc = project.getDescription();
				desc.setActiveBuildConfig(config.getBuildConfiguration().getName());
				project.setDescription(desc, monitor);

				Map<String, String> buildProps = configuration.getAttribute("COREBUILD_" + mode, new HashMap<>()); //$NON-NLS-1$
				if (!buildProps.isEmpty()) {
					config.setProperties(buildProps);
				}
			}
		}

		// proceed with the build
		return superBuildForLaunch(configuration, mode, monitor);
	}

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		IProject project = getProject(configuration);
		ILaunchTarget target = ((ITargetedLaunch) launch).getLaunchTarget();

		ICBuildConfiguration buildConfig = getBuildConfiguration(project, mode, target, monitor);
		IBinary[] binaries = buildConfig.getBuildOutput();
		IBinary exeFile = null;
		for (IBinary binary : binaries) {
			if (binary.isExecutable()) {
				exeFile = binary;
				break;
			}
		}
		if (exeFile == null) {
			throw new CoreException(new Status(IStatus.ERROR, CDebugCorePlugin.PLUGIN_ID, "No binaries"));
		}

		try {
			ProcessBuilder builder = new ProcessBuilder(Paths.get(exeFile.getLocationURI()).toString());
			buildConfig.setBuildEnvironment(builder.environment());
			Process process = builder.start();
			DebugPlugin.newProcess(launch, process, exeFile.getPath().lastSegment());
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, CDebugCorePlugin.PLUGIN_ID, "Error launching", e));
		}
	}

	@Override
	protected IProject[] getBuildOrder(ILaunchConfiguration configuration, String mode) throws CoreException {
		// 1. Extract project from configuration
		// TODO dependencies too.
		IProject project = getProject(configuration);
		return new IProject[] { project };
	}

	private ICBuildConfiguration getBuildConfiguration(IProject project, String mode, ILaunchTarget target,
			IProgressMonitor monitor) throws CoreException {
		// Set active build config based on toolchain for target
		Map<String, String> properties = new HashMap<>();
		properties.put(IToolChain.ATTR_OS, Platform.getOS());
		properties.put(IToolChain.ATTR_ARCH, Platform.getOSArch());
		Collection<IToolChain> tcs = tcManager.getToolChainsMatching(properties);
		if (!tcs.isEmpty()) {
			IToolChain toolChain = tcs.iterator().next();
			return configManager.getBuildConfiguration(project, toolChain, "run", monitor); //$NON-NLS-1$
		} else {
			return null;
		}
	}

}
