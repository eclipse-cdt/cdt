/*******************************************************************************
 * Copyright (c) 2015, 2016 Red Hat and others.
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
package org.eclipse.cdt.internal.docker.launcher;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.docker.launcher.ContainerTargetTypeProvider;
import org.eclipse.cdt.docker.launcher.DockerLaunchUIPlugin;
import org.eclipse.cdt.docker.launcher.IContainerLaunchTarget;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunchDelegate;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
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
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.linuxtools.docker.core.Activator;
import org.eclipse.linuxtools.docker.core.IDockerContainerInfo;
import org.eclipse.linuxtools.docker.core.IDockerNetworkSettings;
import org.eclipse.linuxtools.docker.core.IDockerPortBinding;
import org.eclipse.linuxtools.docker.ui.launch.ContainerLauncher;
import org.eclipse.linuxtools.docker.ui.launch.IContainerLaunchListener;

public class ContainerLaunchConfigurationDelegate extends GdbLaunchDelegate {

	private ContainerLauncher launcher;

	private class StartGdbServerJob extends Job implements IContainerLaunchListener {

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
			if (output.contains(Messages.Gdbserver_up) || output.contains("gdbserver:")) { //$NON-NLS-1$
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

		public Map<String, List<IDockerPortBinding>> getPorts() {
			if (info != null) {
				IDockerNetworkSettings networkSettings = info.networkSettings();
				return networkSettings.ports();
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
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {

		IPath commandPath = getCommandPath(configuration);
		if (commandPath != null) {
			// create some labels to allow user to filter out such Containers if
			// kept
			HashMap<String, String> labels = new HashMap<>();
			labels.put("org.eclipse.cdt.container-launch", ""); //$NON-NLS-1$ //$NON-NLS-2$
			String projectName = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, ""); //$NON-NLS-1$
			labels.put("org.eclipse.cdt.project-name", projectName); //$NON-NLS-1$
			if (mode.equals(ILaunchManager.RUN_MODE)) {
				String commandDir = commandPath.removeLastSegments(1).toPortableString();
				String commandString = commandPath.toPortableString();

				if (commandPath.getDevice() != null) {
					commandDir = "/" + commandDir.replace(':', '/'); //$NON-NLS-1$
					commandString = "/" + commandString.replace(':', '/'); //$NON-NLS-1$
				}

				StringBuilder b = new StringBuilder();
				b.append(commandString);

				String arguments = getProgramArguments(configuration);
				if (arguments.trim().length() > 0) {
					b.append(" "); //$NON-NLS-1$
					b.append(arguments);
				}

				String command = b.toString();

				String workingDir = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY,
						(String) null);
				// if we don't have a working directory, the default is to use
				// the project
				if (workingDir == null && projectName != null) {
					IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
					workingDir = project.getLocation().toOSString();
				}

				if (workingDir != null) {
					IPath workingPath = new Path(workingDir);
					if (workingPath.getDevice() != null) {
						workingDir = "/" + workingPath.toPortableString() //$NON-NLS-1$
								.replace(':', '/');
					}
				}
				Map<String, String> envMap = configuration.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES,
						(Map<String, String>) null);
				Map<String, String> origEnv = null;
				boolean appendEnv = configuration.getAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, false);
				if (appendEnv) {
					origEnv = System.getenv();
				}
				List<String> additionalDirs = configuration.getAttribute(ILaunchConstants.ATTR_ADDITIONAL_DIRS,
						(List<String>) null);
				if (additionalDirs != null) {
					List<String> dirs = new ArrayList<>();
					for (String additionalDir : additionalDirs) {
						IPath path = new Path(additionalDir);
						String dir = path.toPortableString();
						if (path.getDevice() != null) {
							dir = "/" + dir.replace(':', '/');
						}
						dirs.add(dir);
					}
					additionalDirs = dirs;
				}

				List<String> ports = new ArrayList<>();
				List<String> portInfos = configuration.getAttribute(ILaunchConstants.ATTR_EXPOSED_PORTS,
						Collections.emptyList());
				for (String portInfo : portInfos) {
					ExposedPortModel m = ExposedPortModel.createPortModel(portInfo);
					if (m.getSelected()) {
						StringBuilder b1 = new StringBuilder();
						if (m.getHostAddress() != null && !m.getHostAddress().isEmpty()) {
							b1.append(m.getHostAddress());
							b1.append(":"); //$NON-NLS-1$
						}
						if (m.getHostPort() != null && !m.getHostPort().isEmpty()) {
							b1.append(m.getHostPort());
						}
						// regardless if we have a host port or not,
						// we may need to add a separator so we can determine
						// the case where we don't have a host port vs where we
						// don't have a host address
						if (b1.length() > 0) {
							b1.append(":"); //$NON-NLS-1$
						}
						String containerPort = m.getContainerPort() + "/" //$NON-NLS-1$
								+ m.getPortType();
						b1.append(containerPort);
						ports.add(b1.toString());
					}
				}

				String image = configuration.getAttribute(ILaunchConstants.ATTR_IMAGE, (String) null);
				String connectionUri = configuration.getAttribute(ILaunchConstants.ATTR_CONNECTION_URI, "");
				boolean keepContainer = configuration.getAttribute(ILaunchConstants.ATTR_KEEP_AFTER_LAUNCH, false);

				boolean supportStdin = configuration.getAttribute(ILaunchConstants.ATTR_STDIN_SUPPORT, false);

				boolean privilegedMode = configuration.getAttribute(ILaunchConstants.ATTR_PRIVILEGED_MODE, false);

				launcher.launch(DockerLaunchUIPlugin.PLUGIN_ID, null, connectionUri, image, command, commandDir,
						workingDir, additionalDirs, origEnv, envMap, ports.isEmpty() ? null : ports, keepContainer,
						supportStdin, privilegedMode, labels);
			} else if (mode.equals(ILaunchManager.DEBUG_MODE)) {
				String gdbserverPortNumber = configuration.getAttribute(ILaunchConstants.ATTR_GDBSERVER_PORT,
						ILaunchConstants.ATTR_GDBSERVER_PORT_DEFAULT);

				List<String> ports = new ArrayList<>();
				List<String> portInfos = configuration.getAttribute(ILaunchConstants.ATTR_EXPOSED_PORTS,
						Collections.emptyList());
				String gdbserverPort = gdbserverPortNumber + "/tcp"; //$NON-NLS-1$
				boolean gdbserverPortSpecified = false;
				for (String portInfo : portInfos) {
					ExposedPortModel m = ExposedPortModel.createPortModel(portInfo);
					if (m.getSelected()) {
						StringBuilder b = new StringBuilder();
						if (m.getHostAddress() != null && !m.getHostAddress().isEmpty()) {
							b.append(m.getHostAddress());
							b.append(":"); //$NON-NLS-1$
						}
						if (m.getHostPort() != null && !m.getHostPort().isEmpty()) {
							b.append(m.getHostPort());
						}
						// regardless if we have a host port or not,
						// we may need to add a separator so we can determine
						// the case where we don't have a host port vs where we
						// don't have a host address
						if (b.length() > 0) {
							b.append(":"); //$NON-NLS-1$
						}
						String containerPort = m.getContainerPort() + "/" + m.getPortType(); //$NON-NLS-1$
						b.append(containerPort);
						if (gdbserverPort.equals(containerPort)) {
							gdbserverPortSpecified = true;
						}
						ports.add(b.toString());
					}
				}
				// if user hasn't already specified gdbserver port, we need to add it by default
				if (!gdbserverPortSpecified) {
					ports.add(gdbserverPortNumber + "/tcp"); //$NON-NLS-1$
				}
				String gdbserverCommand = configuration.getAttribute(ILaunchConstants.ATTR_GDBSERVER_COMMAND,
						ILaunchConstants.ATTR_GDBSERVER_COMMAND_DEFAULT);

				String commandString = commandPath.toPortableString();
				String commandDir = commandPath.removeLastSegments(1).toPortableString();

				if (commandPath.getDevice() != null) {
					commandDir = "/" + commandDir.replace(':', '/'); //$NON-NLS-1$
					commandString = "/" + commandString.replace(':', '/'); //$NON-NLS-1$
				}

				String commandArguments = ":" + gdbserverPortNumber + " " //$NON-NLS-1$ //$NON-NLS-2$
						+ spaceEscapify(commandString);

				StringBuilder b = new StringBuilder();

				b.append(gdbserverCommand).append(' ').append(commandArguments); //$NON-NLS-1$

				String arguments = getProgramArguments(configuration);
				if (arguments.trim().length() > 0) {
					b.append(" "); //$NON-NLS-1$
					b.append(arguments);
				}

				String command = b.toString();
				String workingDir = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY,
						(String) null);
				// if we don't have a working directory, the default is to use
				// the project
				if (workingDir == null && projectName != null) {
					IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
					workingDir = project.getLocation().toOSString();
				}
				if (workingDir != null) {
					IPath workingPath = new Path(workingDir);
					if (workingPath.getDevice() != null) {
						workingDir = "/" + workingPath.toPortableString() //$NON-NLS-1$
								.replace(':', '/');
					}
				}

				Map<String, String> envMap = configuration.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES,
						(Map<String, String>) null);
				Map<String, String> origEnv = null;
				boolean appendEnv = configuration.getAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, false);
				if (appendEnv) {
					origEnv = System.getenv();
				}
				List<String> additionalDirs = configuration.getAttribute(ILaunchConstants.ATTR_ADDITIONAL_DIRS,
						(List<String>) null);
				if (additionalDirs != null) {
					List<String> dirs = new ArrayList<>();
					for (String additionalDir : additionalDirs) {
						IPath path = new Path(additionalDir);
						String dir = path.toPortableString();
						if (path.getDevice() != null) {
							dir = "/" + dir.replace(':', '/');
						}
						dirs.add(dir);
					}
					additionalDirs = dirs;
				}

				String image = configuration.getAttribute(ILaunchConstants.ATTR_IMAGE, (String) null);
				String connectionUri = configuration.getAttribute(ILaunchConstants.ATTR_CONNECTION_URI, "");
				boolean isLocalConnection = true;
				try {
					Pattern ipaddrPattern = Pattern.compile("[a-z]*://([0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+)[:]*[0-9]*");
					Matcher m = ipaddrPattern.matcher(connectionUri);
					if (m.matches()) {
						String ipaddr = m.group(1);
						InetAddress addr = InetAddress.getByName(ipaddr);
						if (addr.isAnyLocalAddress() || addr.isLoopbackAddress()) {
							isLocalConnection = true;
						} else {
							// Check if the address is defined on any interface
							try {
								isLocalConnection = NetworkInterface.getByInetAddress(addr) != null;
							} catch (SocketException e) {
								isLocalConnection = false;
							}
						}
					}
				} catch (UnknownHostException e) {
					// should not happen
					Activator.log(e);
				}

				boolean keepContainer = configuration.getAttribute(ILaunchConstants.ATTR_KEEP_AFTER_LAUNCH, false);

				boolean supportStdin = configuration.getAttribute(ILaunchConstants.ATTR_STDIN_SUPPORT, false);

				boolean privilegedMode = configuration.getAttribute(ILaunchConstants.ATTR_PRIVILEGED_MODE, false);

				StartGdbServerJob job = new StartGdbServerJob(Messages.Gdbserver_start);
				job.schedule();
				launcher.launch(DockerLaunchUIPlugin.PLUGIN_ID, job, connectionUri, image, command, commandDir,
						workingDir, additionalDirs, origEnv, envMap, ports, keepContainer, supportStdin, privilegedMode,
						labels, "seccomp:unconfined"); //$NON-NLS-1$

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
				if (job.getResult() == Status.OK_STATUS && job.getIpAddress() != null) {
					// Let debugger know how gdbserver was started on the remote
					// container
					ILaunchConfigurationWorkingCopy wc = configuration.getWorkingCopy();
					wc.setAttribute(IGDBLaunchConfigurationConstants.ATTR_REMOTE_TCP, true);
					wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
							IGDBLaunchConfigurationConstants.DEBUGGER_MODE_REMOTE);
					Map<String, List<IDockerPortBinding>> hostPorts = new HashMap<>();
					if (job.getPorts() != null && isLocalConnection) {
						hostPorts = job.getPorts();
					}
					List<IDockerPortBinding> bindingList = hostPorts.get(gdbserverPortNumber + "/tcp"); //$NON-NLS-1$
					if (bindingList != null && !bindingList.isEmpty()) {
						IDockerPortBinding firstBinding = bindingList.get(0);
						wc.setAttribute(IGDBLaunchConfigurationConstants.ATTR_HOST, "localhost"); //$NON-NLS-1$
						wc.setAttribute(IGDBLaunchConfigurationConstants.ATTR_PORT, firstBinding.hostPort());
					} else {
						wc.setAttribute(IGDBLaunchConfigurationConstants.ATTR_HOST, job.getIpAddress());
						wc.setAttribute(IGDBLaunchConfigurationConstants.ATTR_PORT, gdbserverPortNumber);
					}
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
	private String getProgramArguments(ILaunchConfiguration config) throws CoreException {
		String args = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, "");
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
		String projectName = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
		if (projectName.length() > 0) {
			IProject project = CCorePlugin.getWorkspace().getRoot().getProject(projectName);
			if (project == null)
				return null;

			String name = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, "");

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

	/**
	 * @since 1.2
	 */
	protected ICBuildConfigurationManager configManager = CDebugCorePlugin
			.getService(ICBuildConfigurationManager.class);
	/**
	 * @since 1.2
	 */
	protected IToolChainManager toolChainManager = CDebugCorePlugin.getService(IToolChainManager.class);

	/*
	 * @since 1.2
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

	@Override
	public boolean buildForLaunch(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
			throws CoreException {
		IProject project = getProject(configuration);
		String name = configuration.getName();
		Pattern p = Pattern.compile(".*?\\[([^\\]]+)\\](.*)"); //$NON-NLS-1$
		Matcher m = p.matcher(name);
		if (m.matches()) {
			ILaunchTargetManager targetManager = CCorePlugin.getService(ILaunchTargetManager.class);
			ILaunchTarget target = null;
			ILaunchTarget[] targets = targetManager.getLaunchTargetsOfType(ContainerTargetTypeProvider.TYPE_ID);
			for (ILaunchTarget t : targets) {
				if (t.getAttribute(IContainerLaunchTarget.ATTR_IMAGE_ID, "").replaceAll(":", "_").equals(m.group(1))) {
					target = t;
					break;
				}
			}
			if (target != null) {
				ICBuildConfiguration cconfig = getBuildConfiguration(configuration, mode, target, monitor);
				if (cconfig != null) {
					CoreModel model = CoreModel.getDefault();
					synchronized (model) {
						IProjectDescription desc = project.getDescription();
						desc.setActiveBuildConfig(cconfig.getBuildConfiguration().getName());
						project.setDescription(desc, monitor);
					}
				}
			}
		}

		return super.buildForLaunch(configuration, mode, monitor);
	}

	@Override
	public boolean finalLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
			throws CoreException {
		IProject project = getProject(configuration);
		ILaunchTargetManager targetManager = CCorePlugin.getService(ILaunchTargetManager.class);
		ILaunchTarget target = null;
		ILaunchTarget[] targets = targetManager.getLaunchTargetsOfType(ContainerTargetTypeProvider.TYPE_ID);
		String image = configuration.getAttribute(IContainerLaunchTarget.ATTR_IMAGE_ID, (String) null);
		String connection = configuration.getAttribute(IContainerLaunchTarget.ATTR_CONNECTION_URI, (String) null);
		for (ILaunchTarget t : targets) {
			if (t.getAttribute(IContainerLaunchTarget.ATTR_IMAGE_ID, "").equals(image)) {
				target = t;
				break;
			}
		}
		String program = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, (String) null);
		if (program == null) {
			ICBuildConfiguration cconfig = getBuildConfiguration(configuration, mode, target, monitor);
			if (cconfig != null) {
				IBinary[] binaries = cconfig.getBuildOutput();
				for (IBinary b : binaries) {
					if (b.isExecutable() && b.getElementName().contains(project.getName())) {
						ILaunchConfigurationWorkingCopy wc = configuration.getWorkingCopy();
						wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME,
								b.getResource().getProjectRelativePath().toString());
						wc.setMappedResources(new IResource[] { b.getResource(), b.getResource().getProject() });
						wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, (String) null); // default is the project
																													// directory
						wc.setAttribute(ILaunchConstants.ATTR_CONNECTION_URI, connection);
						wc.setAttribute(ILaunchConstants.ATTR_IMAGE, image);

						wc.doSave();
						break;
					}
				}
			}
		}
		return super.finalLaunchCheck(configuration, mode, monitor);
	}

	@Override
	public boolean preLaunchCheck(ILaunchConfiguration config, String mode, IProgressMonitor monitor)
			throws CoreException {
		String projectName = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String) null);
		IProject project = null;
		if (projectName == null) {
			IResource[] resources = config.getMappedResources();
			if (resources != null && resources.length > 0 && resources[0] instanceof IProject) {
				project = (IProject) resources[0];
			}
			ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, project.getName());
			wc.doSave();
		} else {
			projectName = projectName.trim();
			if (!projectName.isEmpty()) {
				project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			}
		}

		return super.preLaunchCheck(config, mode, monitor);
	}

	@Override
	protected String getPluginID() {
		return DockerLaunchUIPlugin.PLUGIN_ID;
	}
}
