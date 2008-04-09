/*******************************************************************************
 * Copyright (c) 2006, 2008 PalmSource, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Ewa Matejska (PalmSource) - Adapted from LocalRunLaunchDelegate
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Martin Oberhuber (Wind River) - [226301][api] IShellService should throw SystemMessageException on error
 *******************************************************************************/


package org.eclipse.rse.internal.remotecdt;

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
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.IService;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.shells.HostShellProcessAdapter;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IShellService;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.IFileServiceSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem.IShellServiceSubSystem;
import org.eclipse.swt.widgets.Display;

public class RemoteRunLaunchDelegate extends AbstractCLaunchDelegate {

	private final static String SHELL_SERVICE = "shell.service";  //$NON-NLS-1$
	private final static String FILE_SERVICE = "file.service";  //$NON-NLS-1$
	private final static String EXIT_CMD = "exit"; //$NON-NLS-1$
	private final static String CMD_DELIMITER = ";"; //$NON-NLS-1$

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate#launch
	 */
	public void launch(ILaunchConfiguration config, String mode, ILaunch launch,
			IProgressMonitor monitor)  throws CoreException {

		IBinaryObject exeFile = null;
		IPath exePath = verifyProgramPath(config);
		ICProject project = verifyCProject(config);
		if (exePath != null) {
			exeFile = verifyBinary(project, exePath);
		}

		String arguments = getProgramArguments(config);
		String remoteExePath = config.getAttribute(IRemoteConnectionConfigurationConstants.ATTR_REMOTE_PATH, ""); //$NON-NLS-1$

		if(mode.equals(ILaunchManager.DEBUG_MODE)){
			setDefaultSourceLocator(launch, config);
			String debugMode = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
					ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN);
			if (debugMode.equals(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN)) {
				Process remoteShellProcess = null;
				ICDISession dsession = null;
				try {
					// Download the binary to the remote before debugging.
					remoteFileDownload(config, launch, exePath.toString(), remoteExePath);

					// Automatically start up the gdbserver.  In the future this should be expanded to launch
					// an arbitrary remote damon.
					String gdbserver_port_number = config.getAttribute(IRemoteConnectionConfigurationConstants.ATTR_GDBSERVER_PORT,
																       IRemoteConnectionConfigurationConstants.ATTR_GDBSERVER_PORT_DEFAULT);
					String gdbserver_command = config.getAttribute(IRemoteConnectionConfigurationConstants.ATTR_GDBSERVER_COMMAND,
							   									   IRemoteConnectionConfigurationConstants.ATTR_GDBSERVER_COMMAND_DEFAULT);
					String command_arguments = ":" + gdbserver_port_number + " "  //$NON-NLS-1$ //$NON-NLS-2$
												+ spaceEscapify(remoteExePath);
					if(arguments != null && !arguments.equals("")) //$NON-NLS-1$
						command_arguments += " " + arguments; //$NON-NLS-1$
					remoteShellProcess = remoteShellExec(config, gdbserver_command,
														 command_arguments);
					DebugPlugin.newProcess(launch, remoteShellProcess, Messages.RemoteRunLaunchDelegate_RemoteShell);

					// Pre-set configuration constants for the GDBSERVERCDIDebugger to indicate how the gdbserver
					// was automatically started on the remote.  GDBServerCDIDebugger uses these to figure out how
					// to connect to the remote gdbserver.
					ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
					wc.setAttribute(IGDBServerMILaunchConfigurationConstants.ATTR_REMOTE_TCP, true);
					wc.setAttribute(IGDBServerMILaunchConfigurationConstants.ATTR_HOST, getRemoteHostname(config));
					wc.setAttribute(IGDBServerMILaunchConfigurationConstants.ATTR_PORT,
						gdbserver_port_number);
					wc.doSave();

					// Default to using the GDBServerCDIDebugger.
					GDBServerCDIDebugger2 debugger = new GDBServerCDIDebugger2();
					dsession = ((ICDIDebugger2)debugger).createSession(launch, exePath.toFile(),
																	   new SubProgressMonitor(monitor, 8));

					boolean stopInMain = config
					.getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, false);
					String stopSymbol = null;
					if ( stopInMain )
						stopSymbol = launch.getLaunchConfiguration().getAttribute(
								ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL,
								ICDTLaunchConfigurationConstants.DEBUGGER_STOP_AT_MAIN_SYMBOL_DEFAULT );

					ICDITarget[] targets = dsession.getTargets();
					for (int i = 0; i < targets.length; i++) {
						Process process = targets[i].getProcess();
						IProcess iprocess = null;
						if (process != null) {
							iprocess = DebugPlugin.newProcess(launch, process,
										renderProcessLabel(exePath.toOSString()), getDefaultProcessMap());
						}
						CDIDebugModel.newDebugTarget(launch, project.getProject(), targets[i],
										renderProcessLabel("gdbserver debugger"), //$NON-NLS-1$
										iprocess, exeFile, true, false, stopSymbol, true);
					}
				} catch (CoreException e) {
					try {
						if(dsession != null)
							dsession.terminate();
						if(remoteShellProcess != null)
							remoteShellProcess.destroy();
					} catch (CDIException e1) {
						// ignore
					}
					throw e;
				}
			}

		} else if(mode.equals(ILaunchManager.RUN_MODE)) {
			Process remoteProcess = null;
			try {
				// Download the binary to the remote before debugging.
				remoteFileDownload(config, launch, exePath.toString(),remoteExePath );
				// Use a remote shell to launch the binary.
				remoteProcess = remoteShellExec(config, remoteExePath, arguments);
				DebugPlugin.newProcess(launch, remoteProcess, renderProcessLabel(exePath.toOSString()));
			} catch (CoreException e) {
				if(remoteProcess != null)
					remoteProcess.destroy();
				throw e;
			}

		} else {
			IStatus status = new Status(IStatus.ERROR, getPluginID(),
								 IStatus.OK, NLS.bind(Messages.RemoteRunLaunchDelegate_1, mode), null);
			throw new CoreException(status);
		}
	}

	private String spaceEscapify(String inputString) {
		if(inputString == null)
			return null;

		return inputString.replaceAll(" ", "\\\\ "); //$NON-NLS-1$ //$NON-NLS-2$
	}

	protected IHost getCurrentConnection(ILaunchConfiguration config) throws CoreException {
		String remoteConnection = config.getAttribute(IRemoteConnectionConfigurationConstants.ATTR_REMOTE_CONNECTION, ""); //$NON-NLS-1$

		IHost[] connections = RSECorePlugin.getTheSystemRegistry().getHosts();
		int i = 0;
		for(i = 0; i < connections.length; i++)
			if(connections[i].getAliasName().equals(remoteConnection))
				break;
		if(i >= connections.length) {
			abort("Could not find the remote connection.", null, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR); //$NON-NLS-1$
		}
		return connections[i];
	}

	protected IService getConnectedRemoteService(ILaunchConfiguration config, String kindOfService)
		throws CoreException {

		// Check that the service requested is file or shell.
		if(!kindOfService.equals(SHELL_SERVICE) && !kindOfService.equals(FILE_SERVICE))
			abort(Messages.RemoteRunLaunchDelegate_3, null, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);

		IHost currentConnection = getCurrentConnection(config);

		ISubSystem[] subSystems = currentConnection.getSubSystems();
		int i = 0;
		for(i = 0; i < subSystems.length; i++) {
			if(subSystems[i] instanceof IShellServiceSubSystem && kindOfService.equals(SHELL_SERVICE))
				break;
			if(subSystems[i] instanceof IFileServiceSubSystem && kindOfService.equals(FILE_SERVICE))
				break;
		}
		if(i >= subSystems.length)
			abort(Messages.RemoteRunLaunchDelegate_4, null, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);

		// Need to run this in the UI thread

		final ISubSystem  subsystem = subSystems[i];
		Display.getDefault().syncExec(new Runnable()
		{
			public void run()
			{	try {
				subsystem.connect(false, null);
				} catch (Exception e) {
					// Ignore
				}
			}
		});

		if(!subsystem.isConnected())
			abort(Messages.RemoteRunLaunchDelegate_5, null, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);

		if(kindOfService.equals(SHELL_SERVICE))
			return  ((IShellServiceSubSystem) subsystem).getShellService();
		else
			return  ((IFileServiceSubSystem) subsystem).getFileService();
	}

	protected Process remoteFileDownload(ILaunchConfiguration config,  ILaunch launch,
			String localExePath, String remoteExePath) throws CoreException {

		boolean skipDownload = config.getAttribute(IRemoteConnectionConfigurationConstants.ATTR_SKIP_DOWNLOAD_TO_TARGET, false);

		if(skipDownload)
			// Nothing to do.  Download is skipped.
			return null;

		IFileService fileService = (IFileService) getConnectedRemoteService(config, FILE_SERVICE);
		File file = new File(localExePath);
		Path remotePath = new Path(remoteExePath);
		try {
			fileService.upload(file, remotePath.removeLastSegments(1).toString(), remotePath.lastSegment(),
					true, null, null, new NullProgressMonitor());
			// Need to change the permissions to match the original file permissions because of a bug in upload
			Process p = remoteShellExec(config, "chmod", "+x " + spaceEscapify(remotePath.toString())); //$NON-NLS-1$ //$NON-NLS-2$
			Thread.sleep(500);
			p.destroy();
		} catch (Exception e) {
			abort(Messages.RemoteRunLaunchDelegate_6, e, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR );
		}
		return null;
	}

	protected String getRemoteHostname(ILaunchConfiguration config) throws CoreException {
		IHost currentConnection = getCurrentConnection(config);
		return currentConnection.getHostName();
	}

	protected Process remoteShellExec(ILaunchConfiguration config, String remoteCommandPath,
			String arguments) throws CoreException {
		// The exit command is called to force the remote shell to close after our command
		// is executed. This is to prevent a running process at the end of the debug session.
		// See Bug 158786.
		String real_remote_command = arguments == null ? spaceEscapify(remoteCommandPath) :
												    spaceEscapify(remoteCommandPath) + " " + arguments; //$NON-NLS-1$
		String remote_command = real_remote_command + CMD_DELIMITER + EXIT_CMD;

		IShellService shellService = (IShellService) getConnectedRemoteService(config, SHELL_SERVICE);

		// This is necessary because runCommand does not actually run the command right now.
		String env[] = new String[0];
		Process p = null;
		try {
			IHostShell hostShell = shellService.launchShell("", env, new NullProgressMonitor()); //$NON-NLS-1$
			hostShell.writeToShell(remote_command);
			p = new HostShellProcessAdapter(hostShell);
		} catch (Exception e) {
			if (p != null) {
				p.destroy();
			}
			abort(Messages.RemoteRunLaunchDelegate_7, e, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		}
		return p;

	}

	protected String getPluginID() {
		return "org.eclipse.rse.internal.remotecdt"; //$NON-NLS-1$
	}
}
