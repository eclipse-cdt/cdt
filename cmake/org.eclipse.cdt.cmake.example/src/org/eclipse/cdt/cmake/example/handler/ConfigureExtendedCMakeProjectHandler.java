/*******************************************************************************
 * Copyright (c) 2026 Renesas Electronics Europe and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.cmake.example.handler;

import java.io.IOException;

import org.eclipse.cdt.cmake.core.CMakeBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.launch.LaunchUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.launchbar.core.ILaunchBarManager;

public class ConfigureExtendedCMakeProjectHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ICBuildConfigurationManager configManager = CDebugCorePlugin.getService(ICBuildConfigurationManager.class);
		ILaunchBarManager launchBarManager = CDebugCorePlugin.getService(ILaunchBarManager.class);
		try {
			ILaunchConfiguration activeLaunchConfiguration;
			activeLaunchConfiguration = launchBarManager.getActiveLaunchConfiguration();
			IProject project = LaunchUtils.getProject(activeLaunchConfiguration);
			ICBuildConfiguration buildConfig = configManager.getBuildConfiguration(project.getActiveBuildConfig());
			if (buildConfig instanceof CMakeBuildConfiguration cbc) {
				WorkspaceJob job = new WorkspaceJob("Configuring Extended CMake Project...") { //$NON-NLS-1$
					@Override
					public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
						try {
							return cbc.configureCMakeBuildFiles(monitor);
						} catch (CoreException | IOException e) {
							ILog.of(ConfigureExtendedCMakeProjectHandler.class)
									.error("Failed to configure for extended CMake Project", e); //$NON-NLS-1$
						}
						return null;
					}
				};
				job.schedule();
			}
		} catch (CoreException e) {
			ILog.of(getClass()).error("Error getting CBuildConfiguration", e); //$NON-NLS-1$
		}
		return null;
	}

}
