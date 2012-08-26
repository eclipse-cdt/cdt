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

package org.eclipse.cdt.launch.remote.launching;

import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunchDelegate;
import org.eclipse.cdt.internal.launch.remote.Activator;
import org.eclipse.cdt.internal.launch.remote.Messages;
import org.eclipse.cdt.launch.remote.IRemoteConnectionConfigurationConstants;
import org.eclipse.cdt.launch.remote.RSEHelper;
import org.eclipse.cdt.launch.remote.TEHelper;
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

public class TEGdbLaunchDelegate extends GdbLaunchDelegate {

	@Override
	public void launch(ILaunchConfiguration config, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		IPath exePath = checkBinaryDetails(config);
		if (exePath != null) {
			// -1. Initialize TE
			TEHelper.initializeTE();
			// 0. Get the peer from the launch configuration
			IPeer peer = TEHelper.getCurrentConnection(config).getPeer();
			// 1.Download binary if needed
			String remoteExePath = config.getAttribute(
					IRemoteConnectionConfigurationConstants.ATTR_REMOTE_PATH,
					""); //$NON-NLS-1$
			monitor.setTaskName(Messages.RemoteRunLaunchDelegate_2);
			boolean skipDownload = config
					.getAttribute(
							IRemoteConnectionConfigurationConstants.ATTR_SKIP_DOWNLOAD_TO_TARGET,
							false);

			if (!skipDownload) {
				TEHelper.remoteFileTransfer(peer, exePath.toString(),
						remoteExePath, new SubProgressMonitor(monitor, 80));
			}
			// 2.Launch gdbserver on target
			String gdbserverPortNumber = config
					.getAttribute(
							IRemoteConnectionConfigurationConstants.ATTR_GDBSERVER_PORT,
							IRemoteConnectionConfigurationConstants.ATTR_GDBSERVER_PORT_DEFAULT);
			String gdbserverCommand = config
					.getAttribute(
							IRemoteConnectionConfigurationConstants.ATTR_GDBSERVER_COMMAND,
							IRemoteConnectionConfigurationConstants.ATTR_GDBSERVER_COMMAND_DEFAULT);
			String commandArguments = ":" + gdbserverPortNumber + " " //$NON-NLS-1$ //$NON-NLS-2$
					+ RSEHelper.spaceEscapify(remoteExePath);
			String arguments = getProgramArguments(config);
			String prelaunchCmd = config
					.getAttribute(
							IRemoteConnectionConfigurationConstants.ATTR_PRERUN_COMMANDS,
							""); //$NON-NLS-1$

			TEHelper.launchCmd(peer, prelaunchCmd,
					new SubProgressMonitor(monitor, 2), new Callback());
			
			if (arguments != null && !arguments.equals("")) //$NON-NLS-1$
				commandArguments += " " + arguments; //$NON-NLS-1$
			monitor.setTaskName(Messages.RemoteRunLaunchDelegate_9);

			final GdbLaunch l = (GdbLaunch) launch;
			final Callback callback = new Callback(){
				@Override
				protected void internalDone(Object caller, IStatus status) {
					if(!status.isOK()){
						// Need to shutdown the DSF launch session because it is
						// partially started already.
						try {
							l.getSession().getExecutor().execute(new DsfRunnable() {
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
			TEHelper.launchCmd(peer, gdbserverCommand, commandArguments,
					new SubProgressMonitor(monitor, 3), callback);
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
