/*******************************************************************************
 * Copyright (c) 2010 Mentor Graphics Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Anna Dushistova (Mentor Graphics) - initial API and implementation
 * Anna Dushistova (Mentor Graphics) - moved to org.eclipse.cdt.launch.remote.launching
 *******************************************************************************/
package org.eclipse.cdt.launch.remote.launching;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunchDelegate;
import org.eclipse.cdt.internal.launch.remote.Activator;
import org.eclipse.cdt.internal.launch.remote.Messages;
import org.eclipse.cdt.launch.remote.IRemoteConnectionConfigurationConstants;
import org.eclipse.cdt.launch.remote.RSEHelper;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.rse.core.RSECorePlugin;

public class RemoteGdbLaunchDelegate extends GdbLaunchDelegate {

	@Override
	public void launch(ILaunchConfiguration config, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		// Need to initialize RSE
		if (!RSECorePlugin.isInitComplete(RSECorePlugin.INIT_MODEL)) {
			monitor.subTask(Messages.RemoteRunLaunchDelegate_10);
			try {
				RSECorePlugin.waitForInitCompletion(RSECorePlugin.INIT_MODEL);
			} catch (InterruptedException e) {
				throw new CoreException(new Status(IStatus.ERROR,
						getPluginID(), IStatus.OK, e.getLocalizedMessage(), e));
			}
		}

		IPath exePath = CDebugUtils.verifyProgramPath(config);
		if (exePath != null) {
			// 1.Download binary if needed
			String remoteExePath = config.getAttribute(
					IRemoteConnectionConfigurationConstants.ATTR_REMOTE_PATH,
					""); //$NON-NLS-1$
			monitor.setTaskName(Messages.RemoteRunLaunchDelegate_2);
			RSEHelper.remoteFileDownload(config, launch, exePath.toString(),
					remoteExePath, new SubProgressMonitor(monitor, 80));
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

			if (arguments != null && !arguments.equals("")) //$NON-NLS-1$
				commandArguments += " " + arguments; //$NON-NLS-1$
			monitor.setTaskName(Messages.RemoteRunLaunchDelegate_9);
			Process remoteShellProcess = RSEHelper.remoteShellExec(config,
					prelaunchCmd, gdbserverCommand, commandArguments,
					new SubProgressMonitor(monitor, 5));

			DebugPlugin.newProcess(launch, remoteShellProcess,
					Messages.RemoteRunLaunchDelegate_RemoteShell);

			
			// 3. Let debugger know how gdbserver was started on the remote
			ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
			wc.setAttribute(IGDBLaunchConfigurationConstants.ATTR_REMOTE_TCP,
					true);
			wc.setAttribute(IGDBLaunchConfigurationConstants.ATTR_HOST,
					RSEHelper.getRemoteHostname(config));
			wc.setAttribute(IGDBLaunchConfigurationConstants.ATTR_PORT,
					gdbserverPortNumber);
			wc.doSave();

		}

		super.launch(config, mode, launch, monitor);
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
