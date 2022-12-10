/*******************************************************************************
 * Copyright (c) 2020, 2021 Red Hat and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.launching;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.core.cdtvariables.ICdtVariable;
import org.eclipse.cdt.core.cdtvariables.ICdtVariableManager;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.IGDBFlatpakLaunchConstants;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.launching.LaunchMessages;
import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IProcess;

public class FlatpakLaunch {

	private final static String LOCAL_HOST = "localhost"; //$NON-NLS-1$
	private final static String FLATPAK_DEBUG_PROCESS_LABEL = "FlatpakDebugProcess_label"; //$NON-NLS-1$

	public FlatpakLaunch() {
	}

	private String[] getLaunchEnvironment(IProject project, ILaunchConfiguration config) throws CoreException {
		HashMap<String, String> envMap = new HashMap<>();
		ICProjectDescription projDesc = CoreModel.getDefault().getProjectDescription(project, false);
		if (projDesc != null) {
			String buildConfigID = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_BUILD_CONFIG_ID,
					""); //$NON-NLS-1$
			ICConfigurationDescription cfg = null;
			if (buildConfigID.length() != 0) {
				cfg = projDesc.getConfigurationById(buildConfigID);
			}

			// if configuration is null fall-back to active
			if (cfg == null) {
				cfg = projDesc.getActiveConfiguration();
			}

			// Environment variables and inherited vars
			IEnvironmentVariable[] vars = CCorePlugin.getDefault().getBuildEnvironmentManager().getVariables(cfg, true);
			for (IEnvironmentVariable var : vars) {
				envMap.put(var.getName(), var.getValue());
			}

			// Add variables from build info
			ICdtVariableManager manager = CCorePlugin.getDefault().getCdtVariableManager();
			ICdtVariable[] buildVars = manager.getVariables(cfg);
			for (ICdtVariable var : buildVars) {
				try {
					// The project_classpath variable contributed by JDT is
					// useless for running C/C++ binaries, but it can be lethal
					// if it has a very large value that exceeds shell limit. See
					// http://bugs.eclipse.org/bugs/show_bug.cgi?id=408522
					if (!"project_classpath".equals(var.getName())) {//$NON-NLS-1$
						String value = manager.resolveValue(var.getStringValue(), "", File.pathSeparator, cfg); //$NON-NLS-1$
						envMap.put(var.getName(), value);
					}
				} catch (CdtVariableException e) {
					// Some Eclipse dynamic variables can't be resolved
					// dynamically... we don't care.
				}
			}
		}

		// Turn it into an envp format
		List<String> strings = new ArrayList<>(envMap.size());
		for (Entry<String, String> entry : envMap.entrySet()) {
			StringBuilder buffer = new StringBuilder(entry.getKey());
			buffer.append('=').append(entry.getValue());
			strings.add(buffer.toString());
		}

		return strings.toArray(new String[strings.size()]);
	}

	protected Map<String, String> createProcessAttributes() {
		Map<String, String> attributes = new HashMap<>();

		// Specify that the process factory (GdbProcessFactory) should use
		// InferiorRuntimeProcess to wrap
		// the process that we are about to run.
		// Note that GdbProcessFactory is only used for launches created using
		// DSF-GDB not CDI
		attributes.put("org.eclipse.cdt.dsf.gdb.createProcessType" /* IGdbDebugConstants.PROCESS_TYPE_CREATION_ATTR */, //$NON-NLS-1$
				"org.eclipse.cdt.dsf.gdb.inferiorProcess" /* IGdbDebugConstants.INFERIOR_PROCESS_CREATION_VALUE */); //$NON-NLS-1$

		// Show the exit code of the process in the console title once it has
		// terminated
		attributes.put("org.eclipse.cdt.dsf.gdb.inferiorExited" /* IGdbDebugConstants.INFERIOR_EXITED_ATTR */, //$NON-NLS-1$
				""); //$NON-NLS-1$
		return attributes;
	}

	public int prelaunch(ILaunchConfiguration configuration, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {

		IPath commandPath = getCommandPath(configuration);
		if (commandPath != null) {
			String projectName = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, ""); //$NON-NLS-1$
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			String gdbserverPortNumber = configuration.getAttribute(IGDBFlatpakLaunchConstants.ATTR_GDBSERVER_PORT,
					IGDBFlatpakLaunchConstants.ATTR_GDBSERVER_PORT_DEFAULT);

			// Do not modify port string value by adding any suffix like `/tcp`, to the port string,
			// keeping it clean port number value on order to make NODE:PORT-like address string later
			// when a DGB launch is created
			// See: https://github.com/flathub/org.eclipse.Java/issues/36
			String gdbserverPort = gdbserverPortNumber;

			String gdbserverCommand = configuration.getAttribute(IGDBFlatpakLaunchConstants.ATTR_GDBSERVER_COMMAND,
					IGDBFlatpakLaunchConstants.ATTR_GDBSERVER_COMMAND_DEFAULT);

			String commandString = commandPath.toPortableString();
			String commandDir = commandPath.removeLastSegments(1).toPortableString();

			if (commandPath.getDevice() != null) {
				commandDir = "/" + commandDir.replace(':', '/'); //$NON-NLS-1$
				commandString = "/" + commandString.replace(':', '/'); //$NON-NLS-1$
			}

			String commandArguments = ":" + gdbserverPortNumber + " " //$NON-NLS-1$ //$NON-NLS-2$
					+ spaceEscapify(commandString);

			StringBuilder b = new StringBuilder();

			String[] commandArray = new String[3];
			commandArray[0] = "/bin/sh"; //$NON-NLS-1$
			commandArray[1] = "-c"; //$NON-NLS-1$
			commandArray[2] = b.append(gdbserverCommand).append(' ').append(commandArguments).toString();

			String arguments = getProgramArguments(configuration);
			if (arguments.trim().length() > 0) {
				b.append(" "); //$NON-NLS-1$
				b.append(arguments);
			}

			String workingDir = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY,
					(String) null);
			// if we don't have a working directory, the default is to use
			// the project
			if (workingDir == null && projectName != null) {
				workingDir = project.getLocation().toString();
			}
			if (workingDir != null) {
				IPath workingPath = new Path(workingDir);
				if (workingPath.getDevice() != null) {
					workingDir = "/" + workingPath.toPortableString() //$NON-NLS-1$
							.replace(':', '/');
				}
			}

			String[] envp = getLaunchEnvironment(project, configuration);
			boolean gdbserverStarted = false;
			try {
				Process p = ProcessFactory.getFactory().exec(commandArray, envp, new File(workingDir),
						new PTY(PTY.Mode.TERMINAL));
				IProcess gdbserver = DebugPlugin.newProcess(launch, p,
						LaunchMessages.getString(FLATPAK_DEBUG_PROCESS_LABEL), createProcessAttributes());
				Thread.sleep(200); // pause to allow gdbserver to start
				try {
					@SuppressWarnings("unused")
					int rc = gdbserver.getExitValue(); // verify gdbserver is started
				} catch (DebugException e2) {
					gdbserverStarted = true;
				}
			} catch (IOException e) {
				GdbPlugin.log(e);
			} catch (InterruptedException e) {
				DebugPlugin.log(e);
			}

			// if gdbserver started successfully launch the debugger
			if (gdbserverStarted) {
				// Let debugger know how gdbserver was started on the remote
				// container
				ILaunchConfigurationWorkingCopy wc = configuration.getWorkingCopy();
				wc.setAttribute(IGDBLaunchConfigurationConstants.ATTR_REMOTE_TCP, true);
				wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
						IGDBLaunchConfigurationConstants.DEBUGGER_MODE_REMOTE);
				wc.setAttribute(IGDBLaunchConfigurationConstants.ATTR_HOST, LOCAL_HOST);
				wc.setAttribute(IGDBLaunchConfigurationConstants.ATTR_PORT, gdbserverPort);
				wc.doSave();
				return 0;
			} else {
				return -1;
			}
		}
		return -1;
	}

	/**
	 * Get the program arguments and perform substitution.
	 *
	 * @param config
	 *            launch configuration
	 * @return argument String
	 * @throws CoreException
	 */
	private String getProgramArguments(ILaunchConfiguration config) throws CoreException {
		String args = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, ""); //$NON-NLS-1$
		if (args != null && args.length() > 0) {
			args = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(args);
		}
		return args;
	}

	/**
	 * Form command path using the project and program name.
	 *
	 * @param configuration
	 * @return command path
	 * @throws CoreException
	 */
	private IPath getCommandPath(ILaunchConfiguration configuration) throws CoreException {
		String projectName = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, ""); //$NON-NLS-1$
		if (projectName.length() > 0) {
			IProject project = CCorePlugin.getWorkspace().getRoot().getProject(projectName);
			if (project == null)
				return null;

			String name = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, ""); //$NON-NLS-1$

			if (name.length() == 0)
				return null;

			IPath exePath = new Path(name);
			if (!exePath.isAbsolute()) {
				IPath location = project.getLocation();
				if (location == null) {
					return null;
				}

				exePath = location.append(name);
				if (!exePath.toFile().exists()) {
					// Try the old way, which is required to support linked
					// resources.
					IFile projFile = null;
					try {
						projFile = project.getFile(name);
					} catch (IllegalArgumentException e) {
						// thrown if relative path that resolves to a root file
						// ("..\somefile")
					}
					if (projFile == null || !projFile.exists()) {
						return null;
					} else {
						exePath = projFile.getLocation();
					}
				}
			}
			if (!exePath.toFile().exists()) {
				return null;
			}

			if (!exePath.toFile().isFile()) {
				return null;
			}
			return exePath;
		} else {
			return null;
		}
	}

	private String spaceEscapify(String inputString) {
		if (inputString == null)
			return null;

		return inputString.replaceAll(" ", "\\\\ "); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static IProject getProject(ILaunchConfiguration configuration) throws CoreException {
		// TODO - make sure this is really the correct project
		return configuration.getMappedResources()[0].getProject();
	}

}
