/*******************************************************************************
 * Copyright (c) 2010, 2016 Mentor Graphics Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Anna Dushistova (Mentor Graphics) - initial API and implementation
 * Anna Dushistova (Mentor Graphics) - moved to org.eclipse.cdt.launch.remote.launching
 * Anna Dushistova (MontaVista)      - [318051][remote debug] Terminating when "Remote shell" process is selected doesn't work
 * Anna Dushistova (MontaVista)      - [368597][remote debug] if gdbserver fails to launch on target, launch doesn't get terminated
 *******************************************************************************/
package org.eclipse.cdt.launch.remote.launching;

import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunchDelegate;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.internal.launch.remote.Activator;
import org.eclipse.cdt.internal.launch.remote.Messages;
import org.eclipse.cdt.launch.remote.IRemoteConnectionConfigurationConstants;
import org.eclipse.cdt.launch.remote.RemoteHelper;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.remote.core.IRemoteProcess;
import org.eclipse.remote.core.RemoteProcessAdapter;

public class RemoteGdbLaunchDelegate extends GdbLaunchDelegate {

	@Override
	public void launch(ILaunchConfiguration config, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {

		IPath exePath = checkBinaryDetails(config);
		Process remoteShellProcess = null;
		if (exePath != null) {
			// 1.Download binary if needed
			String remoteExePath = config.getAttribute(IRemoteConnectionConfigurationConstants.ATTR_REMOTE_PATH, ""); //$NON-NLS-1$
			monitor.setTaskName(Messages.RemoteRunLaunchDelegate_2);
			RemoteHelper.remoteFileDownload(config, launch, exePath.toString(), remoteExePath,
					new SubProgressMonitor(monitor, 80));
			// 2.Launch gdbserver on target
			String gdbserverPortNumber = config.getAttribute(
					IRemoteConnectionConfigurationConstants.ATTR_GDBSERVER_PORT,
					IRemoteConnectionConfigurationConstants.ATTR_GDBSERVER_PORT_DEFAULT);
			String gdbserverCommand = config.getAttribute(
					IRemoteConnectionConfigurationConstants.ATTR_GDBSERVER_COMMAND,
					IRemoteConnectionConfigurationConstants.ATTR_GDBSERVER_COMMAND_DEFAULT);
			String gdbserverOptions = config.getAttribute(
					IRemoteConnectionConfigurationConstants.ATTR_GDBSERVER_OPTIONS,
					IRemoteConnectionConfigurationConstants.ATTR_GDBSERVER_OPTIONS_DEFAULT);
			String commandArguments = gdbserverOptions + " " //$NON-NLS-1$
					+ ":" + gdbserverPortNumber + " " //$NON-NLS-1$ //$NON-NLS-2$
					+ RemoteHelper.spaceEscapify(remoteExePath);
			String arguments = getProgramArguments(config);
			String prelaunchCmd = config.getAttribute(IRemoteConnectionConfigurationConstants.ATTR_PRERUN_COMMANDS, ""); //$NON-NLS-1$

			if (arguments != null && !arguments.isEmpty())
				commandArguments += " " + arguments; //$NON-NLS-1$
			monitor.setTaskName(Messages.RemoteRunLaunchDelegate_9);

			// extending HostShellProcessAdapter here
			final GdbLaunch l = (GdbLaunch) launch;
			IRemoteProcess remoteShell = null;
			try {
				remoteShell = RemoteHelper.execCmdInRemoteShell(config, prelaunchCmd, gdbserverCommand,
						commandArguments, new SubProgressMonitor(monitor, 5));
			} catch (Exception e1) {
				RemoteHelper.abort(e1.getMessage(), e1, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);

			}

			// We cannot use a global variable because multiple launches
			// could access them at the same time.  We need a different
			// variable for each launch, but we also need it be final.
			// Use a final array to do that.
			final boolean gdbServerReady[] = new boolean[1];
			gdbServerReady[0] = false;

			final Object lock = new Object();
			if (remoteShell != null) {
				try {
					remoteShellProcess = new RemoteProcessAdapter(remoteShell) {

						@Override
						public synchronized void destroy() {
							final DsfSession session = l.getSession();
							if (session != null) {
								try {
									session.getExecutor().execute(new DsfRunnable() {
										@Override
										public void run() {
											DsfServicesTracker tracker = new DsfServicesTracker(
													Activator.getBundleContext(), session.getId());
											IGDBControl control = tracker.getService(IGDBControl.class);
											if (control != null) {
												control.terminate(new ImmediateRequestMonitor());
											}
											tracker.dispose();
										}
									});
								} catch (RejectedExecutionException e) {
									// Session disposed.
								}
							}
							super.destroy();
						}
					};
				} catch (Exception e) {
					if (remoteShellProcess != null) {
						remoteShellProcess.destroy();
					}
					RemoteHelper.abort(Messages.RemoteRunLaunchDelegate_7, e,
							ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
				}

				IProcess iProcess = DebugPlugin.newProcess(launch, remoteShellProcess,
						Messages.RemoteRunLaunchDelegate_RemoteShell);

				// Listen process' output to determine gdbserver is up and running.
				IStreamsProxy streams = iProcess.getStreamsProxy();
				IStreamMonitor monitorStream = streams.getOutputStreamMonitor();
				monitorStream.addListener(new IStreamListener() {
					@Override
					public void streamAppended(String text, IStreamMonitor monitor) {
						if (text.contains("Listening on port")) { //$NON-NLS-1$
							synchronized (lock) {
								gdbServerReady[0] = true;
								lock.notifyAll();
							}
							monitor.removeListener(this);
						}
					}
				});
				// Now wait until gdbserver is up and running on the remote host
				synchronized (lock) {
					while (gdbServerReady[0] == false) {
						if (monitor.isCanceled() || iProcess.isTerminated()) {
							//gdbserver launch failed
							if (remoteShellProcess != null) {
								remoteShellProcess.destroy();
							}

							// Need to shutdown the DSF launch session because it is
							// partially started already.
							try {
								l.getSession().getExecutor().execute(new DsfRunnable() {
									@Override
									public void run() {
										l.shutdownSession(new ImmediateRequestMonitor());
									}
								});
							} catch (RejectedExecutionException e) {
								// Session disposed.
							}

							RemoteHelper.abort(Messages.RemoteGdbLaunchDelegate_gdbserverFailedToStartErrorMessage,
									null, ICDTLaunchConfigurationConstants.ERR_DEBUGGER_NOT_INSTALLED);
						}
						try {
							lock.wait(300);
						} catch (InterruptedException e) {
						}
					}
				}

				// 3. Let debugger know how gdbserver was started on the remote
				ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
				wc.setAttribute(IGDBLaunchConfigurationConstants.ATTR_REMOTE_TCP, true);
				wc.setAttribute(IGDBLaunchConfigurationConstants.ATTR_HOST, RemoteHelper.getRemoteHostname(config));
				wc.setAttribute(IGDBLaunchConfigurationConstants.ATTR_PORT, gdbserverPortNumber);
				wc.doSave();

			}
			try {
				super.launch(config, mode, launch, monitor);
			} catch (CoreException ex) {
				//launch failed, need to kill gdbserver
				if (remoteShellProcess != null) {
					remoteShellProcess.destroy();
				}

				//report failure further
				throw ex;
			} finally {
				monitor.done();
			}
		}
	}

	protected String getProgramArguments(ILaunchConfiguration config) throws CoreException {
		String args = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, (String) null);
		if (args != null) {
			args = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(args);
		}
		return args;
	}

	@Override
	protected String getPluginID() {
		return Activator.PLUGIN_ID;
	}
}
