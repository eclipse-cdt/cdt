/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.launch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.launch.CoreBuildLaunchConfigDelegate;
import org.eclipse.cdt.debug.internal.core.InternalDebugCoreMessages;
import org.eclipse.cdt.utils.CommandLineUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.launch.ITargetedLaunch;

public class CoreBuildLocalRunLaunchDelegate extends CoreBuildLaunchConfigDelegate {

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		ILaunchTarget target = ((ITargetedLaunch) launch).getLaunchTarget();
		ICBuildConfiguration buildConfig = getBuildConfiguration(configuration, mode, target, monitor);
		IBinary exeFile = getBinary(buildConfig);

		try {
			String args = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, ""); //$NON-NLS-1$
			if (args.length() != 0) {
				args = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(args);
			}

			String[] arguments = CommandLineUtil.argumentsToArray(args);
			List<String> command = new ArrayList<>(1 + arguments.length);
			command.add(Paths.get(exeFile.getLocationURI()).toString());
			command.addAll(Arrays.asList(arguments));

			ProcessBuilder builder = new ProcessBuilder(command);

			String workingDirectory = configuration
					.getAttribute(ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, ""); //$NON-NLS-1$
			if (!workingDirectory.isBlank()) {
				workingDirectory = VariablesPlugin.getDefault().getStringVariableManager()
						.performStringSubstitution(workingDirectory);
				builder.directory(new File(workingDirectory));
			}

			buildConfig.setBuildEnvironment(builder.environment());
			Process process = builder.start();
			DebugPlugin.newProcess(launch, process, exeFile.getPath().lastSegment());
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, CDebugCorePlugin.PLUGIN_ID,
					InternalDebugCoreMessages.CoreBuildLocalRunLaunchDelegate_ErrorLaunching, e));
		}
	}

}
