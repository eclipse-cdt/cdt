/*******************************************************************************
 * Copyright (c) 2005, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Marc Khouzam (Ericsson) - Modified to only handle Run mode and modernized (Bug 464636)
 *     Marc Khouzam (Ericsson) - Show exit code in console when doing a Run (Bug 463975)
 *******************************************************************************/
package org.eclipse.cdt.launch.internal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.AbstractCLaunchDelegate2;
import org.eclipse.cdt.launch.internal.ui.LaunchMessages;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.cdt.utils.CommandLineUtil;
import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;

import com.ibm.icu.text.DateFormat;

/**
 * The launch delegate for Run mode.
 */
public class LocalRunLaunchDelegate extends AbstractCLaunchDelegate2 {
	public LocalRunLaunchDelegate() {
		// We support project-less run
		super(false);
	}

	@Override
	public void launch(ILaunchConfiguration config, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		// This delegate is only for Run mode
		assert mode.equals(ILaunchManager.RUN_MODE);

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		if (mode.equals(ILaunchManager.RUN_MODE)) {
			runLocalApplication(config, launch, monitor);
		}
	}

	private void runLocalApplication(ILaunchConfiguration config, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		monitor.beginTask(LaunchMessages.LocalCDILaunchDelegate_0, 10);
		if (monitor.isCanceled()) {
			return;
		}
		monitor.worked(1);
		try {
			IPath exePath = checkBinaryDetails(config);

			File wd = verifyWorkingDirectory(config);
			if (wd == null) {
				wd = new File(System.getProperty("user.home", ".")); //$NON-NLS-1$ //$NON-NLS-2$
			}

			String args = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, ""); //$NON-NLS-1$
			if (args.length() != 0) {
				args = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(args);
			}

			String[] arguments = CommandLineUtil.argumentsToArray(args);
			ArrayList<String> command = new ArrayList<>(1 + arguments.length);
			command.add(exePath.toOSString());
			command.addAll(Arrays.asList(arguments));
			monitor.worked(2);

			String[] commandArray = command.toArray(new String[command.size()]);
			String[] environment = getLaunchEnvironment(config);
			Process process = exec(commandArray, environment, wd);
			monitor.worked(6);

			String timestamp = DateFormat.getInstance().format(new Date(System.currentTimeMillis()));
			String processLabel = String.format("%s (%s)", commandArray[0], timestamp); //$NON-NLS-1$

			DebugPlugin.newProcess(launch, process, processLabel, createProcessAttributes());
		} finally {
			monitor.done();
		}
	}

	/**
	 * Gets the CDT environment from the CDT project's configuration referenced
	 * by the launch
	 *
	 * This code matches what
	 * org.eclipse.cdt.dsf.gdb.launching.GdbLaunch.getLaunchEnvironment() and
	 * org.eclipse.cdt.dsf.gdb.service.DebugNewProcessSequence.stepSetEnvironmentVariables(RequestMonitor)
	 * do. In the GDB case the former is used as the environment for launching
	 * GDB and the latter for launching the inferior. In the case of run we need
	 * to combine the two environments as that is what the GDB inferior sees.
	 */
	protected String[] getLaunchEnvironment(ILaunchConfiguration config) throws CoreException {
		// Get the project
		String projectName = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String) null);
		IProject project = null;
		if (projectName == null) {
			IResource[] resources = config.getMappedResources();
			if (resources != null && resources.length > 0 && resources[0] instanceof IProject) {
				project = (IProject) resources[0];
			}
		} else {
			projectName = projectName.trim();
			if (!projectName.isEmpty()) {
				project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			}
		}

		HashMap<String, String> envMap = new HashMap<>();

		// If the launch configuration is the only environment the inferior should see, just use that
		boolean append = config.getAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, true);
		boolean environmentCollectedFromProject = false;

		if (append && project != null && project.isAccessible()) {
			ICProjectDescription projDesc = CoreModel.getDefault().getProjectDescription(project, false);
			if (projDesc != null) {
				environmentCollectedFromProject = true;

				String buildConfigID = config
						.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_BUILD_CONFIG_ID, ""); //$NON-NLS-1$
				ICConfigurationDescription cfg = null;
				if (buildConfigID.length() != 0) {
					cfg = projDesc.getConfigurationById(buildConfigID);
				}

				// if configuration is null fall-back to active
				if (cfg == null) {
					cfg = projDesc.getActiveConfiguration();
				}

				// Environment variables and inherited vars
				IEnvironmentVariable[] vars = CCorePlugin.getDefault().getBuildEnvironmentManager().getVariables(cfg,
						true);
				for (IEnvironmentVariable var : vars) {
					envMap.put(var.getName(), var.getValue());
				}

				// Add variables from build info
				ICdtVariableManager manager = CCorePlugin.getDefault().getCdtVariableManager();
				ICdtVariable[] buildVars = manager.getVariables(cfg);
				for (ICdtVariable var : buildVars) {
					try {
						// The project_classpath variable contributed by JDT is
						// useless for running C/C++ binaries, but it can be
						// lethal if it has a very large value that exceeds
						// shell limit. See
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
		}

		if (!environmentCollectedFromProject) {
			// we haven't collected any environment variables from the project settings,
			// therefore simply use the launch settings
			return DebugPlugin.getDefault().getLaunchManager().getEnvironment(config);
		}

		// Now that we have the environment from the project, update it with
		// the environment settings the user has explicitly set in the launch
		// configuration. There is no API in the launch manager to do this,
		// so we create a temp copy with append = false to get around that.
		ILaunchConfigurationWorkingCopy wc = config.copy(""); //$NON-NLS-1$
		// Don't save this change, it is just temporary, and in just a
		// copy of our launchConfig.
		wc.setAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, false);
		String[] properties = DebugPlugin.getDefault().getLaunchManager().getEnvironment(wc);
		if (properties != null) {
			for (String env : properties) {
				String[] parts = env.split("=", 2); //$NON-NLS-1$
				if (parts.length == 2) {
					envMap.put(parts[0], parts[1]);
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

	/**
	 * Method used to check that the project and program are correct. Can be
	 * overridden to avoid checking certain things.
	 */
	protected IPath checkBinaryDetails(final ILaunchConfiguration config) throws CoreException {
		// First verify we are dealing with a proper project.
		ICProject project = verifyCProject(config);
		// Now verify we know the program to run.
		IPath exePath = verifyProgramPath(config, project);
		return exePath;
	}

	/**
	 * Performs a runtime exec on the given command line in the context of the
	 * specified working directory, and returns the resulting process.
	 *
	 * @param cmdLine
	 *            the command line
	 * @param environ
	 * @param workingDirectory
	 *            the working directory, or <code>null</code>
	 * @return the resulting process or <code>null</code> if the exec is
	 *         cancelled
	 * @see Runtime
	 * @since 4.7
	 */
	protected Process exec(String[] cmdLine, String[] environ, File workingDirectory) throws CoreException {
		try {
			if (PTY.isSupported()) {
				return ProcessFactory.getFactory().exec(cmdLine, environ, workingDirectory, new PTY());
			} else {
				return ProcessFactory.getFactory().exec(cmdLine, environ, workingDirectory);
			}
		} catch (IOException e) {
			abort(LaunchMessages.LocalCDILaunchDelegate_8, e, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		}
		return null;
	}

	@Override
	public boolean preLaunchCheck(ILaunchConfiguration config, String mode, IProgressMonitor monitor)
			throws CoreException {
		// Setup default Process Factory
		setDefaultProcessFactory(config);

		return super.preLaunchCheck(config, mode, monitor);
	}

	/**
	 * Modify the ILaunchConfiguration to set the
	 * DebugPlugin.ATTR_PROCESS_FACTORY_ID attribute, so as to specify the
	 * process factory to use.
	 *
	 * This attribute should only be set if it is not part of the configuration
	 * already, to allow other code to set it to something else.
	 */
	protected void setDefaultProcessFactory(ILaunchConfiguration config) throws CoreException {
		if (!config.hasAttribute(DebugPlugin.ATTR_PROCESS_FACTORY_ID)) {
			ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
			// Use the debug process factory as it provides extra features for
			// the program
			// that is being debugged or in this case run.
			// Effectively, we want to use InferiorRuntimeProcess when doing
			// this Run launch.
			wc.setAttribute(DebugPlugin.ATTR_PROCESS_FACTORY_ID, "org.eclipse.cdt.dsf.gdb.GdbProcessFactory"); //$NON-NLS-1$
			wc.doSave();
		}
	}

	@Override
	protected String getPluginID() {
		return LaunchUIPlugin.getUniqueIdentifier();
	}
}
