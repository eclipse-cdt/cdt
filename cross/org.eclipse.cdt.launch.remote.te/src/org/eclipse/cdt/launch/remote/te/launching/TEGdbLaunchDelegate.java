/*******************************************************************************
 * Copyright (c) 2010, 2012 Mentor Graphics Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Anna Dushistova (Mentor Graphics) - initial API and implementation
 * Anna Dushistova (Mentor Graphics) - moved to org.eclipse.cdt.launch.remote.launching
 * Anna Dushistova (MontaVista)      - [318051][remote debug] Terminating when "Remote shell" process is selected doesn't work
 * Anna Dushistova (MontaVista)      - [368597][remote debug] if gdbserver fails to launch on target, launch doesn't get terminated
 * Anna Dushistova (MontaVista)      - adapted from RemoteGdbLaunchDelegate
 *******************************************************************************/

package org.eclipse.cdt.launch.remote.te.launching;

import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunchDelegate;
import org.eclipse.cdt.launch.remote.te.Activator;
import org.eclipse.cdt.launch.internal.remote.te.Messages;
import org.eclipse.cdt.launch.remote.te.IRemoteTEConfigurationConstants;
import org.eclipse.cdt.launch.remote.te.utils.TEHelper;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.te.runtime.callback.Callback;
import org.eclipse.tcf.te.tcf.core.streams.StreamsDataReceiver;

public class TEGdbLaunchDelegate extends GdbLaunchDelegate {

	@Override
	public void launch(ILaunchConfiguration config, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		IPath exePath = checkBinaryDetails(config);
		if (exePath != null) {
			// -1. Initialize TE
			Activator.getDefault().initializeTE();
			// 0. Get the peer from the launch configuration
			IPeer peer = TEHelper.getCurrentConnection(config).getPeer();
			// 1.Download binary if needed
			String remoteExePath = config.getAttribute(
					IRemoteTEConfigurationConstants.ATTR_REMOTE_PATH, ""); //$NON-NLS-1$
			monitor.setTaskName(Messages.RemoteRunLaunchDelegate_2);
			boolean skipDownload = config
					.getAttribute(
							IRemoteTEConfigurationConstants.ATTR_SKIP_DOWNLOAD_TO_TARGET,
							false);

			if (!skipDownload) {
				TEHelper.remoteFileTransfer(peer, exePath.toString(),
						remoteExePath, new SubProgressMonitor(monitor, 80));
			}
			// 2.Launch gdbserver on target
			String gdbserverPortNumber = config
					.getAttribute(
							IRemoteTEConfigurationConstants.ATTR_GDBSERVER_PORT,
							IRemoteTEConfigurationConstants.ATTR_GDBSERVER_PORT_DEFAULT);
			String gdbserverCommand = config
					.getAttribute(
							IRemoteTEConfigurationConstants.ATTR_GDBSERVER_COMMAND,
							IRemoteTEConfigurationConstants.ATTR_GDBSERVER_COMMAND_DEFAULT);
			String commandArguments = ":" + gdbserverPortNumber + " " //$NON-NLS-1$ //$NON-NLS-2$
					+ TEHelper.spaceEscapify(remoteExePath);
			String arguments = getProgramArguments(config);
			String prelaunchCmd = config.getAttribute(
					IRemoteTEConfigurationConstants.ATTR_PRERUN_COMMANDS, ""); //$NON-NLS-1$

			TEHelper.launchCmd(peer, prelaunchCmd, null,
					new SubProgressMonitor(monitor, 2), new Callback());
			if (arguments != null && !arguments.equals("")) //$NON-NLS-1$
				commandArguments += " " + arguments; //$NON-NLS-1$
			monitor.setTaskName(Messages.RemoteRunLaunchDelegate_9);

			final GdbLaunch l = (GdbLaunch) launch;
			final Callback callback = new Callback() {
				@Override
				protected void internalDone(Object caller, IStatus status) {
					if (!status.isOK()) {
						// Need to shutdown the DSF launch session because it is
						// partially started already.
						try {
							l.getSession().getExecutor()
									.execute(new DsfRunnable() {
										public void run() {
											l.shutdownSession(new ImmediateRequestMonitor());
										}
									});
						} catch (RejectedExecutionException e) {
							// Session disposed.
						}

					}
					super.internalDone(caller, status);
				}
			};

			// We cannot use a global variable because multiple launches
			// could access them at the same time. We need a different
			// variable for each launch, but we also need it be final.
			// Use a final array to do that.
			final boolean gdbServerReady[] = new boolean[1];
			gdbServerReady[0] = false;

			final Object lock = new Object();

			StreamsDataReceiver.Listener listener = new StreamsDataReceiver.Listener() {

				public void dataReceived(String data) {
					if (data.contains("Listening on port")) { //$NON-NLS-1$
						synchronized (lock) {
							gdbServerReady[0] = true;
							lock.notifyAll();
						}
					}

				}
			};

			TEHelper.launchCmd(peer, gdbserverCommand, commandArguments,
					listener, new SubProgressMonitor(monitor, 3), callback);

			// Now wait until gdbserver is up and running on the remote host
			synchronized (lock) {
				while (gdbServerReady[0] == false) {
					if (monitor.isCanceled()) {
						// gdbserver launch failed
						// Need to shutdown the DSF launch session because it is
						// partially started already.
						try {
							l.getSession().getExecutor()
									.execute(new DsfRunnable() {
										public void run() {
											l.shutdownSession(new ImmediateRequestMonitor());
										}
									});
						} catch (RejectedExecutionException e) {
							// Session disposed.
						}

						abort(Messages.RemoteGdbLaunchDelegate_gdbserverFailedToStartErrorMessage,
								null,
								ICDTLaunchConfigurationConstants.ERR_DEBUGGER_NOT_INSTALLED);
					}
					try {
						lock.wait(300);
					} catch (InterruptedException e) {
					}
				}
			}

			// 3. Let debugger know how gdbserver was started on the remote
			ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
			wc.setAttribute(IGDBLaunchConfigurationConstants.ATTR_REMOTE_TCP,
					true);
			wc.setAttribute(IGDBLaunchConfigurationConstants.ATTR_HOST,
					TEHelper.getCurrentConnection(config).getPeer()
							.getAttributes().get(IPeer.ATTR_IP_HOST));
			wc.setAttribute(IGDBLaunchConfigurationConstants.ATTR_PORT,
					gdbserverPortNumber);
			wc.doSave();
			try {
				super.launch(config, mode, launch, monitor);
			} catch (CoreException ex) {
				// TODO launch failed, need to kill gdbserver

				// report failure further
				throw ex;
			} finally {
				monitor.done();
			}
		}
	}

	protected String getProgramArguments(ILaunchConfiguration config)
			throws CoreException {
		String args = config.getAttribute(
				ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
				(String) null);
		if (args != null) {
			args = VariablesPlugin.getDefault().getStringVariableManager()
					.performStringSubstitution(args);
		}
		return args;
	}

	@Override
	protected String getPluginID() {
		return Activator.PLUGIN_ID;
	}

}
