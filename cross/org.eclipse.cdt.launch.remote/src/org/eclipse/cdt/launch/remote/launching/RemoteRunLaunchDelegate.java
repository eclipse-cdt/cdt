/*******************************************************************************
 * Copyright (c) 2006, 2012 PalmSource, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Ewa Matejska     (PalmSource)      - Adapted from LocalRunLaunchDelegate
 * Martin Oberhuber (Wind River)      - [186128] Move IProgressMonitor last in all API
 * Martin Oberhuber (Wind River)      - [186773] split ISystemRegistryUI from ISystemRegistry
 * Martin Oberhuber (Wind River)      - [226301][api] IShellService should throw SystemMessageException on error
 * Anna Dushistova  (MontaVista)      - [234490][remotecdt] Launching with disconnected target fails
 * Anna Dushistova  (MontaVista)      - [235298][remotecdt] Further improve progress reporting and cancellation of Remote CDT Launch
 * Anna Dushistova  (MontaVista)      - [244173][remotecdt][nls] Externalize Strings in RemoteRunLaunchDelegate
 * Anna Dushistova  (MontaVista)      - [181517][usability] Specify commands to be run before remote application launch
 * Nikita Shulga    (EmbeddedAlley)   - [265236][remotecdt] Wait for RSE to initialize before querying it for host list
 * Anna Dushistova  (MontaVista)      - [267951][remotecdt] Support systemTypes without files subsystem
 * Anna Dushistova  (Mentor Graphics) - [314659]Fixed deprecated methods
 * Anna Dushistova  (Mentor Graphics) - moved to org.eclipse.cdt.launch.remote.launching
 * Anna Dushistova  (MontaVista)      - [318051][remote debug] Terminating when "Remote shell" process is selected doesn't work
 * Anna Dushistova  (MontaVista)      - [368597][remote debug] if gdbserver fails to launch on target, launch doesn't get terminated
 *******************************************************************************/

package org.eclipse.cdt.launch.remote.launching;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.ICDIDebugger2;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.mi.core.GDBServerCDIDebugger2;
import org.eclipse.cdt.debug.mi.core.IGDBServerMILaunchConfigurationConstants;
import org.eclipse.cdt.internal.launch.remote.Activator;
import org.eclipse.cdt.internal.launch.remote.Messages;
import org.eclipse.cdt.launch.AbstractCLaunchDelegate;
import org.eclipse.cdt.launch.remote.IRemoteConnectionConfigurationConstants;
import org.eclipse.cdt.launch.remote.RSEHelper;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
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
import org.eclipse.rse.services.shells.HostShellProcessAdapter;
import org.eclipse.rse.services.shells.IHostOutput;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IHostShellChangeEvent;
import org.eclipse.rse.services.shells.IHostShellOutputListener;

public class RemoteRunLaunchDelegate extends AbstractCLaunchDelegate {

	private ICDISession dsession;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate#launch
	 */
	@Override
	public void launch(ILaunchConfiguration config, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {

		IBinaryObject exeFile = null;
		IPath exePath = CDebugUtils.verifyProgramPath(config);
		ICProject project = CDebugUtils.verifyCProject(config);
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
					dsession = null;
					try {
						// Download the binary to the remote before debugging.
						monitor.setTaskName(Messages.RemoteRunLaunchDelegate_2);
						RSEHelper.remoteFileDownload(config, launch, exePath.toString(),
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
						String gdbserver_options = config
								.getAttribute(
										IRemoteConnectionConfigurationConstants.ATTR_GDBSERVER_OPTIONS,
										IRemoteConnectionConfigurationConstants.ATTR_GDBSERVER_OPTIONS_DEFAULT);
						String command_arguments = gdbserver_options + " " //$NON-NLS-1$
								+ ":" + gdbserver_port_number + " " //$NON-NLS-1$ //$NON-NLS-2$
								+ RSEHelper.spaceEscapify(remoteExePath);
						if (arguments != null && !arguments.equals("")) //$NON-NLS-1$
							command_arguments += " " + arguments; //$NON-NLS-1$
						monitor.setTaskName(Messages.RemoteRunLaunchDelegate_9);
						IHostShell remoteShell = null;
						try {
							remoteShell = RSEHelper.execCmdInRemoteShell(config, prelaunchCmd,
									gdbserver_command, command_arguments,
									new SubProgressMonitor(monitor, 5));
						} catch (Exception e1) {
							RSEHelper.abort(e1.getMessage(), e1,
									ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
						}

						// We cannot use a global variable because multiple launches
						// could access them at the same time.  We need a different
						// variable for each launch, but we also need it be final.
						// Use a final array to do that.
						final boolean gdbServerReady[] = new boolean[1];
						gdbServerReady[0] = false;
						
						final Object lock = new Object();
						if (remoteShell != null) {
							remoteShell
							.addOutputListener(new IHostShellOutputListener() {

								public void shellOutputChanged(
										IHostShellChangeEvent event) {
									for (IHostOutput line : event
											.getLines()) {
										if (line.getString().contains(
												"Listening on port")) { //$NON-NLS-1$
											synchronized (lock) {
												gdbServerReady[0] = true;
												lock.notifyAll();
											}
											break;
										}
									}
								}
							});

							try {
								remoteShellProcess = new HostShellProcessAdapter(remoteShell) {

									@Override
									public synchronized void destroy() {
										ICDISession session = getSession();
										if (session != null) {
											try {
												session.terminate();
											} catch (CDIException e) {
											}
										}
										super.destroy();
									}
								};
							} catch (Exception e) {
								if (remoteShellProcess != null) {
									remoteShellProcess.destroy();
								}
								RSEHelper.abort(Messages.RemoteRunLaunchDelegate_7, e,
										ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
							}
							IProcess rsProcess = DebugPlugin
									.newProcess(
											launch,
											remoteShellProcess,
											Messages.RemoteRunLaunchDelegate_RemoteShell);

							// Now wait until gdbserver is up and running on the
							// remote host
							synchronized (lock) {
								while (gdbServerReady[0] == false) {
									if (monitor.isCanceled()
											|| rsProcess.isTerminated()) {
										RSEHelper.abort(Messages.RemoteGdbLaunchDelegate_gdbserverFailedToStartErrorMessage, null,
												ICDTLaunchConfigurationConstants.ERR_DEBUGGER_NOT_INSTALLED);
									}
									try {
										lock.wait(300);
									} catch (InterruptedException e) {
									}
								}
							}

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
									RSEHelper.getRemoteHostname(config));
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
					RSEHelper.remoteFileDownload(config, launch, exePath.toString(),
							remoteExePath, new SubProgressMonitor(monitor, 80));
					// Use a remote shell to launch the binary.
					monitor.setTaskName(Messages.RemoteRunLaunchDelegate_12);
					remoteProcess = RSEHelper.remoteShellExec(config, prelaunchCmd,
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

	@Override
	protected String getPluginID() {
		return Activator.PLUGIN_ID;
	}
	
	ICDISession getSession(){
		return dsession;
	}
}
