/*******************************************************************************
 * Copyright (c) 2017 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.debug.core.launch;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.internal.core.Messages;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * Delegate for Generic Launches over the Generic Launch Target. Can be
 * overriden to support launch customization for similar targets.
 *
 * @since 8.3
 */
public class CoreBuildGenericLaunchConfigDelegate extends CoreBuildLaunchConfigDelegate {

	public static final String TYPE_ID = "org.eclipse.cdt.debug.core.genericLaunchConfigType"; //$NON-NLS-1$

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		IStringVariableManager varManager = VariablesPlugin.getDefault().getStringVariableManager();

		String location = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_LOCATION, ""); //$NON-NLS-1$
		if (location.isEmpty()) {
			launch.addProcess(new NullProcess(launch, Messages.CoreBuildGenericLaunchConfigDelegate_NoAction));
			return;
		} else {
			String substLocation = varManager.performStringSubstitution(location);
			if (substLocation.isEmpty()) {
				throw new CoreException(new Status(IStatus.ERROR, CDebugCorePlugin.PLUGIN_ID,
						String.format(Messages.CoreBuildGenericLaunchConfigDelegate_SubstitutionFailed, location)));
			}
			location = substLocation;
		}

		if (!new File(location).canExecute()) {
			throw new CoreException(new Status(IStatus.ERROR, CDebugCorePlugin.PLUGIN_ID,
					String.format(Messages.CoreBuildGenericLaunchConfigDelegate_CommandNotValid, location)));
		}

		List<String> commands = new ArrayList<>();
		commands.add(location);

		String arguments = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_TOOL_ARGUMENTS, ""); //$NON-NLS-1$
		if (!arguments.isEmpty()) {
			commands.addAll(Arrays.asList(varManager.performStringSubstitution(arguments).split(" "))); //$NON-NLS-1$
		}

		String workingDirectory = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY,
				""); //$NON-NLS-1$
		File workingDir;
		if (workingDirectory.isEmpty()) {
			workingDir = new File(getProject(configuration).getLocationURI());
		} else {
			workingDir = new File(varManager.performStringSubstitution(workingDirectory));
			if (!workingDir.isDirectory()) {
				throw new CoreException(new Status(IStatus.ERROR, CDebugCorePlugin.PLUGIN_ID,
						String.format(Messages.CoreBuildGenericLaunchConfigDelegate_WorkingDirNotExists, location)));
			}
		}

		String[] envp = DebugPlugin.getDefault().getLaunchManager().getEnvironment(configuration);

		Process p = DebugPlugin.exec(commands.toArray(new String[0]), workingDir, envp);
		DebugPlugin.newProcess(launch, p, String.join(" ", commands)); //$NON-NLS-1$
	}

}
