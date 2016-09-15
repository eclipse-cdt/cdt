/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.core.launch;

import java.io.IOException;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.internal.qt.core.Activator;
import org.eclipse.cdt.qt.core.IQtBuildConfiguration;
import org.eclipse.cdt.qt.core.QtLaunchConfigurationDelegate;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.launch.ITargetedLaunch;

public class QtLocalRunLaunchConfigDelegate extends QtLaunchConfigurationDelegate {

	public static final String TYPE_ID = Activator.ID + ".launchConfigurationType"; //$NON-NLS-1$

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		ILaunchTarget target = ((ITargetedLaunch) launch).getLaunchTarget();
		IQtBuildConfiguration qtBuildConfig = getQtBuildConfiguration(configuration, mode, target, monitor);

		IBuildConfiguration buildConfig = qtBuildConfig.getBuildConfiguration();
		ProcessBuilder processBuilder = new ProcessBuilder(qtBuildConfig.getProgramPath().toString())
				.directory(buildConfig.getProject().getLocation().toFile());
		
		Map<String, String> env = processBuilder.environment();
		for (IEnvironmentVariable var : CCorePlugin.getDefault().getBuildEnvironmentManager()
				.getVariables(qtBuildConfig.getBuildConfiguration(), true)) {
			env.put(var.getName(), var.getValue());
		}

		Map<String, String> configEnv = configuration.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES,
				(Map<String, String>) null);
		if (configEnv != null) {
			for (Map.Entry<String, String> entry : configEnv.entrySet()) {
				env.put(entry.getKey(), entry.getValue());
			}
		}

		try {
			Process process = processBuilder.start();
			DebugPlugin.newProcess(launch, process, qtBuildConfig.getProgramPath().toString());
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.ID, "Launching", e));
		}
	}

}
