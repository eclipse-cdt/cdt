/*******************************************************************************
 * Copyright (c) 2006 PalmSource, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Ewa Matejska (PalmSource) - Adapted from LocalRunLaunchDelegate
 *******************************************************************************/



package org.eclipse.rse.remotecdt;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.mi.core.GDBServerCDIDebugger;
import org.eclipse.cdt.debug.mi.core.IGDBServerMILaunchConfigurationConstants;
import org.eclipse.cdt.launch.AbstractCLaunchDelegate;
import org.eclipse.cdt.utils.spawner.Spawner;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.ui.RSEUIPlugin;


public class RemoteRunLaunchDelegate extends AbstractCLaunchDelegate {
	
	private final static String REMOTE_GDBSERVER_COMMAND = "gdbserver";
	private final static String SFTP_COMMAND = "sftp";
	private final static String SFTP_COMMAND_ARGS = "-b -";
	private final static String SSH_COMMAND = "ssh";
	private final static String SYSTEM_TYPE = "Ssh/Gdbserver";
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
		String remoteExePath = config.getAttribute(IRemoteConnectionConfigurationConstants.ATTR_REMOTE_PATH,
				 "");
		
		if(mode.equals(ILaunchManager.DEBUG_MODE)){
			setDefaultSourceLocator(launch, config);			
			ICDISession dsession = null;
			String debugMode = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
					ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN);
			if (debugMode.equals(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN)) {
				/* Download the binary to the remote before debugging using the scp program*/
				remoteSftpDownload(config, launch, exePath.toString(), remoteExePath);
				
				/* Default to using the GDBServerCDIDebugger. */
				GDBServerCDIDebugger debugger = new GDBServerCDIDebugger();
				
				/* Automatically start up the gdbserver to be used by the GDBServerCDIDebugger on the remote
				 * using ssh.
				 */
				String command_arguments = ":" + IRemoteConnectionConfigurationConstants.ATTR_TCP_PORT + " " + remoteExePath;
				if(arguments != null && !arguments.equals(""))
					command_arguments += " " + arguments;
				Process sshProcess = remoteSshExec(config, REMOTE_GDBSERVER_COMMAND, command_arguments);
				DebugPlugin.newProcess(launch, sshProcess, renderProcessLabel(REMOTE_GDBSERVER_COMMAND));
				/* Pre-set configuration constants for the GDBSERVERCDIDebugger to indicate how the gdbserver
				 * was automatically started on the remote.  GDBServerCDIDebugger uses these to figure out how
				 * to connect to the remote gdbserver.
				 */
				ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
				wc.setAttribute(IGDBServerMILaunchConfigurationConstants.ATTR_REMOTE_TCP, true);
				wc.setAttribute(IGDBServerMILaunchConfigurationConstants.ATTR_HOST, getRemoteHostname(config)); 
				wc.setAttribute(IGDBServerMILaunchConfigurationConstants.ATTR_PORT, 
						IRemoteConnectionConfigurationConstants.ATTR_TCP_PORT);
				
				dsession = debugger.createLaunchSession(wc.doSave(), exeFile, new SubProgressMonitor(monitor, 8));
						
				try {
					/* Assume that stopInMain is true until the Debugger tab is added */
					boolean stopInMain = true;
					String stopSymbol = null;
					if ( stopInMain )
						stopSymbol = launch.getLaunchConfiguration().
									    getAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL, 
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
										renderProcessLabel("gdbserver debugger"),
										iprocess, exeFile, true, false, stopSymbol, true);
					}
				} catch (CoreException e) {
					try {
						dsession.terminate();
					} catch (CDIException e1) {
						// ignore
					}
					throw e;
				}
			}

		} else if(mode.equals(ILaunchManager.RUN_MODE)) {
			/* Download the binary to the remote before debugging */
			remoteSftpDownload(config, launch, exePath.toString(),remoteExePath );
			
			/* Use ssh to launch the binary on the remote */
			Process process = remoteSshExec(config, remoteExePath, arguments);
			DebugPlugin.newProcess(launch, process, renderProcessLabel(exePath.toOSString()));
			
		} else {
			IStatus status = new Status(IStatus.ERROR, getPluginID(),
								 IStatus.OK, "Unidentified mode " + mode + " passed to RemoteRunLaunchDelegate", null);
			throw new CoreException(status);
		}		
	}
	
	private String quotify(String inputString) {
		if(inputString == null)
			return null;
		return '"' + inputString + '"';
	}
	
	private String spaceEscapify(String inputString) {
		if(inputString == null)
			return null;
		
		return inputString.replaceAll(" ", "\\\\ ");
	}
	
	protected Process remoteSftpDownload(ILaunchConfiguration config,  ILaunch launch, String localExePath, String remoteExePath)
		throws CoreException {
		boolean skipDownload = config.getAttribute(IRemoteConnectionConfigurationConstants.ATTR_SKIP_DOWNLOAD_TO_TARGET,
		 false);
		
		if(skipDownload)
			//Nothing to do.  Download is skipped.
			return null;

		String arguments = SFTP_COMMAND_ARGS + " " + getRemoteHostname(config);
		Process p = null;
		try {
			p = execLocal(SFTP_COMMAND, arguments);
			DebugPlugin.newProcess(launch, p, renderProcessLabel(SFTP_COMMAND));
			OutputStream outStream = p.getOutputStream();
			String putCommand = "put " + quotify(localExePath) + " "  + quotify(remoteExePath) + "\n";
			String exitCommand = "exit\n";
			// Execute the put and then the exit command.
			outStream.write(putCommand.getBytes());
			outStream.write(exitCommand.getBytes());
			if(p.waitFor() != 0) {
				IStatus status = new Status(IStatus.ERROR, getPluginID(),
						 IStatus.OK, "Couldn't download program to remote. See console for reason.", null);
				throw new CoreException(status);
			}
		} catch (InterruptedException e) {
		} catch (IOException e) {
		}
		
		return null;
	}
	
	protected String getRemoteHostname(ILaunchConfiguration config) throws CoreException{
		String remoteConnection = config.getAttribute(IRemoteConnectionConfigurationConstants.ATTR_REMOTE_CONNECTION,
		 "");

		IHost[] connections = RSEUIPlugin.getTheSystemRegistry().getHostsBySystemType(SYSTEM_TYPE);
		int i = 0;
		for(i = 0; i < connections.length; i++)
			if(connections[i].getAliasName().equals(remoteConnection))
				break;
		if(i >= connections.length) {
			IStatus status = new Status(IStatus.ERROR, getPluginID(),
					 IStatus.OK, "Internal Error: Could not find the remote connection.\n", null);
			throw new CoreException(status);
		}
		return connections[i].getHostName();
	}
	
	protected Process remoteSshExec(ILaunchConfiguration config, String remoteCommandPath,
			String arguments) throws CoreException {
		String remote_command = arguments == null ? spaceEscapify(remoteCommandPath) :
												    spaceEscapify(remoteCommandPath) + " " + arguments;
		String ssh_arguments = getRemoteHostname(config) + " " + remote_command;	 
		
		Process p = execLocal(SSH_COMMAND, ssh_arguments);
		return p;
		
		/* In the future the Shell subsystem from RSE should be used instead of invoking ssh and scp 
		 * directly.  The code to accomplish this might look like below.
		 */
		
		/*ISubSystem[] subSystems = connections[i].getSubSystems();
		for(i = 0; i < subSystems.length; i++)
			if(subSystems[i] instanceof IShellServiceSubSystem)
				break;
		if(i >= subSystems.length)
			abort("Internal Error: No shell subsystem found.\n", null, 0);
		IShellServiceSubSystem shellSubSystem = (IShellServiceSubSystem) subSystems[i];
		IShellService shellService = shellSubSystem.getShellService();
		String command = arguments == null ? file.toOSString() : file.toOSString() + " " + arguments;
		
		IHostShell hostShell = shellService.runCommand(null, file.removeLastSegments(1).toOSString(),command, null);
	
		Process p = null;
		try {
			p = new HostShellProcess(hostShell);
		} catch (Exception e) {
			if (p != null) {
				p.destroy();
			}
			abort("Internal Error: Could not create the hostShellProcess.\n", null, 0);
		}
	    */
	}
	
	protected Process execLocal(String file, String arguments) throws CoreException {
		Process p = null;
		String command = file + " " + arguments;
		try {
			p = new Spawner(command, false);
		} catch (Exception e) {
			if (p != null) {
				p.destroy();
			}
			IStatus status = new Status(IStatus.ERROR, getPluginID(),
					 IStatus.OK, "Internal Error: Could not execute command.\n", null);
			throw new CoreException(status);
		}

		return p;
	}

	protected String getPluginID() {
		return "org.eclipse.rse.remotecdt";
	}
}
