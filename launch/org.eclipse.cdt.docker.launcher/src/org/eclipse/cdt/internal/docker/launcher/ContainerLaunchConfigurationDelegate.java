/*******************************************************************************
 * Copyright (c) 2015, 2016 Red Hat and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.cdt.internal.docker.launcher;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.docker.launcher.DockerLaunchUIPlugin;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunchDelegate;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.linuxtools.docker.core.IDockerContainerInfo;
import org.eclipse.linuxtools.docker.core.IDockerNetworkSettings;
import org.eclipse.linuxtools.docker.ui.launch.ContainerLauncher;
import org.eclipse.linuxtools.docker.ui.launch.IContainerLaunchListener;

public class ContainerLaunchConfigurationDelegate extends GdbLaunchDelegate
		implements ILaunchConfigurationDelegate {

	private ContainerLauncher launcher;

	private class StartGdbServerJob extends Job implements
			IContainerLaunchListener {

		private boolean started;
		private boolean done;
		private IDockerContainerInfo info;

		public StartGdbServerJob(String name) {
			super(name);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask(getName(), IProgressMonitor.UNKNOWN);

			while (!done) {
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				if (started && getIpAddress() != null)
					done = true;
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					monitor.done();
					return Status.CANCEL_STATUS;
				}
			}
			monitor.done();
			return Status.OK_STATUS;
		}

		@Override
		public void newOutput(String output) {
			if (output.contains(Messages.Gdbserver_up)) {
				started = true;
			}

		}

		public String getIpAddress() {
			if (info != null) {
				IDockerNetworkSettings networkSettings = info.networkSettings();
				return networkSettings.ipAddress();
			}
			return null;
		}

		public IDockerContainerInfo getContainerInfo() {
			return info;
		}

		@Override
		public void done() {
			done = true;
		}

		@Override
		public void containerInfo(IDockerContainerInfo info) {
			this.info = info;
		}
	}

	public ContainerLaunchConfigurationDelegate() {
		super();
		launcher = new ContainerLauncher();
	}

	@Override
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {

		IPath commandPath = getCommandPath(configuration);
		if (commandPath != null) {
			// create some labels to allow user to filter out such Containers if
			// kept
			HashMap<String, String> labels = new HashMap<>();
			labels.put("org.eclipse.cdt.container-launch", ""); //$NON-NLS-1$ //$NON-NLS-2$
			String projectName = configuration.getAttribute(
					ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, ""); //$NON-NLS-1$
			labels.put("org.eclipse.cdt.project-name", projectName); //$NON-NLS-1$
			if (mode.equals(ILaunchManager.RUN_MODE)) {
				String commandDir = commandPath.removeLastSegments(1)
						.toString();

				StringBuilder b = new StringBuilder();
				b.append(commandPath.toString().trim());

				String arguments = getProgramArguments(configuration);
				if (arguments.trim().length() > 0) {
					b.append(" "); //$NON-NLS-1$
					b.append(arguments);
				}

				String command = b.toString();

				String workingDir = configuration
						.getAttribute(
								ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY,
								(String) null);
				Map<String, String> envMap = configuration.getAttribute(
						ILaunchManager.ATTR_ENVIRONMENT_VARIABLES,
						(Map<String, String>) null);
				Map<String, String> origEnv = null;
				boolean appendEnv = configuration
						.getAttribute(
								ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES,
								false);
				if (appendEnv) {
					origEnv = System.getenv();
				}
				List<String> additionalDirs = configuration.getAttribute(
						ILaunchConstants.ATTR_ADDITIONAL_DIRS,
						(List<String>) null);
				String image = configuration.getAttribute(
						ILaunchConstants.ATTR_IMAGE, (String) null);
				String connectionUri = configuration.getAttribute(
						ILaunchConstants.ATTR_CONNECTION_URI, (String) "");
				boolean keepContainer = configuration.getAttribute(
						ILaunchConstants.ATTR_KEEP_AFTER_LAUNCH, false);

				boolean supportStdin = configuration.getAttribute(
						ILaunchConstants.ATTR_STDIN_SUPPORT, false);

				boolean privilegedMode = configuration.getAttribute(
						ILaunchConstants.ATTR_PRIVILEGED_MODE, false);

				launcher.launch(DockerLaunchUIPlugin.PLUGIN_ID, null,
						connectionUri,
						image, command,
						commandDir, workingDir, additionalDirs, origEnv,
						envMap, null, keepContainer, supportStdin,
						privilegedMode, labels);
			} else if (mode.equals(ILaunchManager.DEBUG_MODE)) {
				String gdbserverPortNumber = configuration.getAttribute(
						ILaunchConstants.ATTR_GDBSERVER_PORT,
						ILaunchConstants.ATTR_GDBSERVER_PORT_DEFAULT);
				List<String> ports = Arrays
						.asList(gdbserverPortNumber + "/tcp"); //$NON-NLS-1$
				String gdbserverCommand = configuration.getAttribute(
						ILaunchConstants.ATTR_GDBSERVER_COMMAND,
						ILaunchConstants.ATTR_GDBSERVER_COMMAND_DEFAULT);
				String commandArguments = ":" + gdbserverPortNumber + " " //$NON-NLS-1$ //$NON-NLS-2$
						+ spaceEscapify(commandPath.toString());

				String commandDir = commandPath.removeLastSegments(1)
						.toString();

				StringBuilder b = new StringBuilder();

				b.append(gdbserverCommand).append(' ').append(commandArguments); //$NON-NLS-1$

				String arguments = getProgramArguments(configuration);
				if (arguments.trim().length() > 0) {
					b.append(" "); //$NON-NLS-1$
					b.append(arguments);
				}

				String command = b.toString();
				String workingDir = configuration
						.getAttribute(
								ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY,
								(String) null);
				Map<String, String> envMap = configuration.getAttribute(
						ILaunchManager.ATTR_ENVIRONMENT_VARIABLES,
						(Map<String, String>) null);
				Map<String, String> origEnv = null;
				boolean appendEnv = configuration
						.getAttribute(
								ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES,
								false);
				if (appendEnv) {
					origEnv = System.getenv();
				}
				List<String> additionalDirs = configuration.getAttribute(
						ILaunchConstants.ATTR_ADDITIONAL_DIRS,
						(List<String>) null);
				String image = configuration.getAttribute(
						ILaunchConstants.ATTR_IMAGE, (String) null);
				String connectionUri = configuration.getAttribute(
						ILaunchConstants.ATTR_CONNECTION_URI, (String) "");
				boolean keepContainer = configuration.getAttribute(
						ILaunchConstants.ATTR_KEEP_AFTER_LAUNCH, false);

				boolean supportStdin = configuration.getAttribute(
						ILaunchConstants.ATTR_STDIN_SUPPORT, false);

				boolean privilegedMode = configuration.getAttribute(
						ILaunchConstants.ATTR_PRIVILEGED_MODE, false);

				StartGdbServerJob job = new StartGdbServerJob(
						Messages.Gdbserver_start);
				job.schedule();
				launcher.launch(DockerLaunchUIPlugin.PLUGIN_ID, job,
						connectionUri,
						image, command,
						commandDir, workingDir, additionalDirs, origEnv,
						envMap, ports, keepContainer, supportStdin,
						privilegedMode, labels, "seccomp:unconfined"); //$NON-NLS-1$

				// wait until gdbserver is started successfully and we have its
				// ip address or
				// gdbserver has failed
				try {
					job.join();
				} catch (InterruptedException e) {
					// ignore
				}

				// if gdbserver started successfully and we have its ip address,
				// launch the debugger
				if (job.getResult() == Status.OK_STATUS
						&& job.getIpAddress() != null) {
					// Let debugger know how gdbserver was started on the remote
					// container
					ILaunchConfigurationWorkingCopy wc = configuration
							.getWorkingCopy();
					wc.setAttribute(
							IGDBLaunchConfigurationConstants.ATTR_REMOTE_TCP,
							true);
					wc.setAttribute(
							ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
							IGDBLaunchConfigurationConstants.DEBUGGER_MODE_REMOTE);
					wc.setAttribute(IGDBLaunchConfigurationConstants.ATTR_HOST,
							job.getIpAddress());
					wc.setAttribute(IGDBLaunchConfigurationConstants.ATTR_PORT,
							gdbserverPortNumber);
					wc.doSave();
					try {
						super.launch(configuration, mode, launch, monitor);
					} catch (CoreException ex) {
						// launch failed, need to cleanup any container we
						// created for the gdbserver
						launcher.cleanup(connectionUri, job.getContainerInfo());
						throw ex;
					} finally {
						monitor.done();
					}
				}
			}
		}
	}

	/**
	 * Get the program arguments and perform substitution.
	 * 
	 * @param config
	 *            launch configuration
	 * @return argument String
	 * @throws CoreException
	 */
	private String getProgramArguments(ILaunchConfiguration config)
			throws CoreException {
		String args = config.getAttribute(
				ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
				(String) "");
		if (args != null && args.length() > 0) {
			args = VariablesPlugin.getDefault().getStringVariableManager()
					.performStringSubstitution(args);
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
	private IPath getCommandPath(ILaunchConfiguration configuration)
			throws CoreException {
		String projectName = configuration.getAttribute(
				ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
		if (projectName.length() > 0) {
			IProject project = CCorePlugin.getWorkspace().getRoot()
					.getProject(projectName);
			if (project == null)
				return null;

			String name = configuration.getAttribute(
					ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, "");

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

	@Override
	protected String getPluginID() {
		return DockerLaunchUIPlugin.PLUGIN_ID;
	}
}
