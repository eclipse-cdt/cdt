/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.core.launch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.internal.qt.core.Activator;
import org.eclipse.cdt.qt.core.IQtBuildConfiguration;
import org.eclipse.cdt.qt.core.QtLaunchConfigurationDelegate;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
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

		// set up the environment
		List<String> env = new ArrayList<>();

		for (IEnvironmentVariable var : CCorePlugin.getDefault().getBuildEnvironmentManager()
				.getVariables(qtBuildConfig.getBuildConfiguration(), true)) {
			env.add(var.getName() + '=' + var.getValue());
		}

		Map<String, String> configEnv = configuration.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES,
				(Map<String, String>) null);
		if (configEnv != null) {
			for (Map.Entry<String, String> entry : configEnv.entrySet()) {
				env.add(entry.getKey() + '=' + entry.getValue());
			}
		}

		ICommandLauncher commandLauncher = new CommandLauncher();
		Process process = commandLauncher.execute(qtBuildConfig.getProgramPath(), new String[0],
				env.toArray(new String[env.size()]),
				qtBuildConfig.getBuildConfiguration().getProject().getLocation().toFile().toPath(), monitor);
		DebugPlugin.newProcess(launch, process, "main");
	}

}
