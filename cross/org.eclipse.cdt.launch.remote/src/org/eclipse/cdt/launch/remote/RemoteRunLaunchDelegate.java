/*******************************************************************************
 * Copyright (c) 2006, 2009 PalmSource, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Ewa Matejska     (PalmSource) - Adapted from LocalRunLaunchDelegate
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Martin Oberhuber (Wind River) - [226301][api] IShellService should throw SystemMessageException on error
 * Anna Dushistova  (MontaVista) - [234490][remotecdt] Launching with disconnected target fails
 * Anna Dushistova  (MontaVista) - [235298][remotecdt] Further improve progress reporting and cancellation of Remote CDT Launch
 * Anna Dushistova  (MontaVista) - [244173][remotecdt][nls] Externalize Strings in RemoteRunLaunchDelegate
 * Anna Dushistova  (MontaVista) - [181517][usability] Specify commands to be run before remote application launch
 * Nikita Shulga (EmbeddedAlley) - [265236][remotecdt] Wait for RSE to initialize before querying it for host list
 * Anna Dushistova  (MontaVista) - [267951] [remotecdt] Support systemTypes without files subsystem
 *******************************************************************************/

package org.eclipse.cdt.launch.remote;

import java.io.File;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.ICDIDebugger2;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.mi.core.GDBServerCDIDebugger2;
import org.eclipse.cdt.debug.mi.core.IGDBServerMILaunchConfigurationConstants;
import org.eclipse.cdt.internal.launch.remote.Messages;
import org.eclipse.cdt.launch.AbstractCLaunchDelegate;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.services.clientserver.messages.SystemOperationCancelledException;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.shells.HostShellProcessAdapter;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IShellService;

public class RemoteRunLaunchDelegate extends AbstractCLaunchDelegate {

	private final static String EXIT_CMD = "exit"; //$NON-NLS-1$
	private final static String CMD_DELIMITER = ";"; //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate#launch
	 */
	public void launch(ILaunchConfiguration config, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {

		IBinaryObject exeFile = null;
		IPath exePath = verifyProgramPath(config);
		ICProject project = verifyCProject(config);
		if (exePath != null) {
			exeFile = verifyBinary(project, exePath);
			String arguments = getProgramArguments(config);
			String remoteExePath = config.getAttribute(
					IRemoteConnectionConfigurationConstants.ATTR_REMOTE_PATH,
					""); //$NON-NLS-1$
			String prelaunchCmd = config
					.getAttribute(
							IRemoteConnectionConfigurationConstants.ATTR_PRERUN_COMMANDS,
							""); //$NON-NLS-1$

			if (monitor == null)
				monitor = new NullProgressMonitor();

			if (!RSECorePlugin.isInitComplete(RSECorePlugin.INIT_MODEL)) {
				monitor.subTask(Messages.RemoteRunLaunchDelegate_10);
				try {
					RSECorePlugin
							.waitForInitCompletion(RSECorePlugin.INIT_MODEL);
				} catch (InterruptedException e) {
					throw new CoreException(new Status(IStatus.ERROR,
							getPluginID(), IStatus.OK, e.getLocalizedMessage(),
							e));
				}
			}
			if (mode.equals(ILaunchManager.DEBUG_MODE)) {
				monitor.beginTask(Messages.RemoteRunLaunchDelegate_0, 100);
				setDefaultSourceLocator(launch, config);
				String debugMode = config
						.getAttribute(
								ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
								ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN);
				if (debugMode
						.equals(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN)) {
					Process remoteShellProcess = null;
					ICDISession dsession = null;
					try {
						// Download the binary to the remote before debugging.
						monitor.setTaskName(Messages.RemoteRunLaunchDelegate_2);
						remoteFileDownload(config, launch, exePath.toString(),
								remoteExePath, new SubProgressMonitor(monitor,
										80));

						// Automatically start up the gdbserver. In the future
						// this should be expanded to launch
						// an arbitrary remote daemon.
						String gdbserver_port_number = config
								.getAttribute(
										IRemoteConnectionConfigurationConstants.ATTR_GDBSERVER_PORT,
										IRemoteConnectionConfigurationConstants.ATTR_GDBSERVER_PORT_DEFAULT);
						String gdbserver_command = config
								.getAttribute(
										IRemoteConnectionConfigurationConstants.ATTR_GDBSERVER_COMMAND,
										IRemoteConnectionConfigurationConstants.ATTR_GDBSERVER_COMMAND_DEFAULT);
						String command_arguments = ":" + gdbserver_port_number + " " //$NON-NLS-1$ //$NON-NLS-2$
								+ spaceEscapify(remoteExePath);
						if (arguments != null && !arguments.equals("")) //$NON-NLS-1$
							command_arguments += " " + arguments; //$NON-NLS-1$
						monitor.setTaskName(Messages.RemoteRunLaunchDelegate_9);
						remoteShellProcess = remoteShellExec(config,
								prelaunchCmd, gdbserver_command,
								command_arguments, new SubProgressMonitor(
										monitor, 5));
						DebugPlugin.newProcess(launch, remoteShellProcess,
								Messages.RemoteRunLaunchDelegate_RemoteShell);

						// Pre-set configuration constants for the
						// GDBSERVERCDIDebugger to indicate how the gdbserver
						// was automatically started on the remote.
						// GDBServerCDIDebugger uses these to figure out how
						// to connect to the remote gdbserver.
						ILaunchConfigurationWorkingCopy wc = config
								.getWorkingCopy();
						wc
								.setAttribute(
										IGDBServerMILaunchConfigurationConstants.ATTR_REMOTE_TCP,
										true);
						wc
								.setAttribute(
										IGDBServerMILaunchConfigurationConstants.ATTR_HOST,
										getRemoteHostname(config));
						wc
								.setAttribute(
										IGDBServerMILaunchConfigurationConstants.ATTR_PORT,
										gdbserver_port_number);
						wc.doSave();

						// Default to using the GDBServerCDIDebugger.
						GDBServerCDIDebugger2 debugger = new GDBServerCDIDebugger2();
						dsession = ((ICDIDebugger2) debugger).createSession(
								launch, exePath.toFile(),
								new SubProgressMonitor(monitor, 15));

						boolean stopInMain = config
								.getAttribute(
										ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN,
										false);
						String stopSymbol = null;
						if (stopInMain)
							stopSymbol = launch
									.getLaunchConfiguration()
									.getAttribute(
											ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL,
											ICDTLaunchConfigurationConstants.DEBUGGER_STOP_AT_MAIN_SYMBOL_DEFAULT);

						ICDITarget[] targets = dsession.getTargets();
						for (int i = 0; i < targets.length; i++) {
							Process process = targets[i].getProcess();
							IProcess iprocess = null;
							if (process != null) {
								iprocess = DebugPlugin.newProcess(launch,
										process, renderProcessLabel(exePath
												.toOSString()),
										getDefaultProcessMap());
							}
							CDIDebugModel.newDebugTarget(launch, project
									.getProject(),
									targets[i],
									renderProcessLabel("gdbserver debugger"), //$NON-NLS-1$
									iprocess, exeFile, true, false, stopSymbol,
									true);
						}
					} catch (CoreException e) {
						try {
							if (dsession != null)
								dsession.terminate();
							if (remoteShellProcess != null)
								remoteShellProcess.destroy();
						} catch (CDIException e1) {
							// ignore
						}
						throw e;
					} finally {
						monitor.done();
					}
				}

			} else if (mode.equals(ILaunchManager.RUN_MODE)) {
				monitor.beginTask(Messages.RemoteRunLaunchDelegate_0, 100);
				Process remoteProcess = null;
				try {
					// Download the binary to the remote before debugging.
					monitor.setTaskName(Messages.RemoteRunLaunchDelegate_2);
					remoteFileDownload(config, launch, exePath.toString(),
							remoteExePath, new SubProgressMonitor(monitor, 80));
					// Use a remote shell to launch the binary.
					monitor.setTaskName(Messages.RemoteRunLaunchDelegate_12);
					remoteProcess = remoteShellExec(config, prelaunchCmd,
							remoteExePath, arguments, new SubProgressMonitor(
									monitor, 20));
					DebugPlugin.newProcess(launch, remoteProcess,
							renderProcessLabel(exePath.toOSString()));
				} catch (CoreException e) {
					throw e;
				} finally {
					monitor.done();
				}

			} else {
				IStatus status = new Status(IStatus.ERROR, getPluginID(),
						IStatus.OK, NLS.bind(
								Messages.RemoteRunLaunchDelegate_1, mode), null);
				throw new CoreException(status);
			}
		}
	}

	private String spaceEscapify(String inputString) {
		if (inputString == null)
			return null;

		return inputString.replaceAll(" ", "\\\\ "); //$NON-NLS-1$ //$NON-NLS-2$
	}

	protected IHost getCurrentConnection(ILaunchConfiguration config)
			throws CoreException {
		String remoteConnection = config.getAttribute(
				IRemoteConnectionConfigurationConstants.ATTR_REMOTE_CONNECTION,
				""); //$NON-NLS-1$
		IHost connection = RSEHelper
				.getRemoteConnectionByName(remoteConnection);
		if (connection == null) {
			abort(Messages.RemoteRunLaunchDelegate_13, null,
					ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		}
		return connection;
	}

	protected Process remoteFileDownload(ILaunchConfiguration config,
			ILaunch launch, String localExePath, String remoteExePath,
			IProgressMonitor monitor) throws CoreException {

		boolean skipDownload = config
				.getAttribute(
						IRemoteConnectionConfigurationConstants.ATTR_SKIP_DOWNLOAD_TO_TARGET,
						false);

		if (skipDownload)
			// Nothing to do. Download is skipped.
			return null;
		monitor.beginTask(Messages.RemoteRunLaunchDelegate_2, 100);
		IFileService fileService;
		try {
			fileService = (IFileService) RSEHelper
					.getConnectedRemoteFileService(
							getCurrentConnection(config),
							new SubProgressMonitor(monitor, 10));
			File file = new File(localExePath);
			Path remotePath = new Path(remoteExePath);
			fileService.upload(file, remotePath.removeLastSegments(1)
					.toString(), remotePath.lastSegment(), true, null, null,
					new SubProgressMonitor(monitor, 85));
			// Need to change the permissions to match the original file
			// permissions because of a bug in upload
			Process p = remoteShellExec(
					config,
					"", "chmod", "+x " + spaceEscapify(remotePath.toString()), new SubProgressMonitor(monitor, 5)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			Thread.sleep(500);
			p.destroy();
		} catch (SystemOperationCancelledException e) {
			cancel(e.getLocalizedMessage(), IStatus.CANCEL);
		} catch (Exception e) {
			abort(Messages.RemoteRunLaunchDelegate_6, e,
					ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		} finally {
			monitor.done();
		}
		return null;
	}

	protected String getRemoteHostname(ILaunchConfiguration config)
			throws CoreException {
		IHost currentConnection = getCurrentConnection(config);
		return currentConnection.getHostName();
	}

	protected Process remoteShellExec(ILaunchConfiguration config,
			String prelaunchCmd, String remoteCommandPath, String arguments,
			IProgressMonitor monitor) throws CoreException {
		// The exit command is called to force the remote shell to close after
		// our command
		// is executed. This is to prevent a running process at the end of the
		// debug session.
		// See Bug 158786.
		monitor.beginTask(NLS.bind(Messages.RemoteRunLaunchDelegate_8,
				remoteCommandPath, arguments), 10);
		String real_remote_command = arguments == null ? spaceEscapify(remoteCommandPath)
				: spaceEscapify(remoteCommandPath) + " " + arguments; //$NON-NLS-1$

		String remote_command = real_remote_command + CMD_DELIMITER + EXIT_CMD;

		if (!prelaunchCmd.trim().equals("")) //$NON-NLS-1$
			remote_command = prelaunchCmd + CMD_DELIMITER + remote_command;

		IShellService shellService;
		Process p = null;
		try {
			shellService = (IShellService) RSEHelper
					.getConnectedRemoteShellService(
							getCurrentConnection(config),
							new SubProgressMonitor(monitor, 7));

			// This is necessary because runCommand does not actually run the
			// command right now.
			String env[] = new String[0];
			try {
				IHostShell hostShell = shellService.launchShell(
						"", env, new SubProgressMonitor(monitor, 3)); //$NON-NLS-1$
				hostShell.writeToShell(remote_command);
				p = new HostShellProcessAdapter(hostShell);
			} catch (Exception e) {
				if (p != null) {
					p.destroy();
				}
				abort(Messages.RemoteRunLaunchDelegate_7, e,
						ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			abort(e1.getMessage(), e1,
					ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		}

		monitor.done();
		return p;
	}

	protected String getPluginID() {
		return "org.eclipse.rse.internal.remotecdt"; //$NON-NLS-1$
	}
}
