/********************************************************************************
 * Copyright (c) 2009, 2010 MontaVista Software, Inc. and others.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Anna Dushistova (MontaVista)      - initial API and implementation
 * Anna Dushistova (Mentor Graphics) - [314659] moved common methods for DSF and CDI launches to this class
 * Anna Dushistova (Mentor Graphics) - changed spaceEscapify visibility
 ********************************************************************************/

package org.eclipse.cdt.launch.remote;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.internal.launch.remote.Activator;
import org.eclipse.cdt.internal.launch.remote.Messages;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.IService;
import org.eclipse.rse.services.clientserver.messages.SystemOperationCancelledException;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.shells.HostShellProcessAdapter;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IShellService;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.IFileServiceSubSystem;

public class RSEHelper {

	private final static String EXIT_CMD = "exit"; //$NON-NLS-1$
	private final static String CMD_DELIMITER = ";"; //$NON-NLS-1$

	
	public static IHost getRemoteConnectionByName(String remoteConnection) {
		if (remoteConnection == null)
			return null;
		IHost[] connections = RSECorePlugin.getTheSystemRegistry().getHosts();
		for (int i = 0; i < connections.length; i++)
			if (connections[i].getAliasName().equals(remoteConnection))
				return connections[i];
		return null; // TODO Connection is not found in the list--need to react
		// somehow, throw the exception?

	}

	public static IService getConnectedRemoteFileService(
			IHost currentConnection, IProgressMonitor monitor) throws Exception {
		final ISubSystem subsystem = getFileSubsystem(currentConnection);

		if (subsystem == null)
			throw new Exception(Messages.RemoteRunLaunchDelegate_4);

		try {
			subsystem.connect(monitor, false);
		} catch (CoreException e) {
			throw e;
		} catch (OperationCanceledException e) {
			throw new CoreException(Status.CANCEL_STATUS);
		}

		if (!subsystem.isConnected())
			throw new Exception(Messages.RemoteRunLaunchDelegate_5);

		return ((IFileServiceSubSystem) subsystem).getFileService();
	}

	public static IService getConnectedRemoteShellService(
			IHost currentConnection, IProgressMonitor monitor) throws Exception {
		ISubSystem subsystem = getSubSystemWithShellService(currentConnection);
		if (subsystem != null) {
			try {
				subsystem.connect(monitor, false);
			} catch (CoreException e) {
				throw e;
			} catch (OperationCanceledException e) {
				throw new CoreException(Status.CANCEL_STATUS);
			}
			if (!subsystem.isConnected())
				throw new Exception(Messages.RemoteRunLaunchDelegate_5);

			return (IShellService) subsystem.getSubSystemConfiguration()
					.getService(currentConnection).getAdapter(
							IShellService.class);
		} else {
			throw new Exception(Messages.RemoteRunLaunchDelegate_4);
		}
	}

	/**
	 * Find the first shell service associated with the host.
	 * 
	 * @param host
	 *            the connection
	 * @return shell service object, or <code>null</code> if not found.
	 */
	public static IShellService getShellService(IHost host) {
		ISubSystem ss = getSubSystemWithShellService(host);
		if (ss != null) {
			return (IShellService) ss.getSubSystemConfiguration().getService(
					host).getAdapter(IShellService.class);
		}
		return null;
	}

	/**
	 * Find the first IShellServiceSubSystem service associated with the host.
	 * 
	 * @param host
	 *            the connection
	 * @return shell service subsystem, or <code>null</code> if not found.
	 */
	public static ISubSystem getSubSystemWithShellService(IHost host) {
		if (host == null)
			return null;
		ISubSystem[] subSystems = host.getSubSystems();
		IShellService ssvc = null;
		for (int i = 0; subSystems != null && i < subSystems.length; i++) {
			IService svc = subSystems[i].getSubSystemConfiguration()
					.getService(host);
			if (svc != null) {
				ssvc = (IShellService) svc.getAdapter(IShellService.class);
				if (ssvc != null) {
					return subSystems[i];
				}
			}
		}
		return null;
	}

	public static ISubSystem getFileSubsystem(IHost host) {
		if (host == null)
			return null;
		ISubSystem[] subSystems = host.getSubSystems();
		for (int i = 0; i < subSystems.length; i++) {
			if (subSystems[i] instanceof IFileServiceSubSystem)
				return subSystems[i];
		}
		return null;
	}

	public static IHost[] getSuitableConnections() {
		ArrayList shellConnections = new ArrayList(Arrays.asList(RSECorePlugin.getTheSystemRegistry()
				.getHostsBySubSystemConfigurationCategory("shells"))); //$NON-NLS-1$
		ArrayList terminalConnections = new ArrayList(Arrays.asList(RSECorePlugin.getTheSystemRegistry()
		.getHostsBySubSystemConfigurationCategory("terminals")));//$NON-NLS-1$

		Iterator iter = terminalConnections.iterator();
		while(iter.hasNext()){
			Object terminalConnection = iter.next();
			if(!shellConnections.contains(terminalConnection)){
				shellConnections.add(terminalConnection);
			}
		}
		
		return (IHost[]) shellConnections.toArray(new IHost[shellConnections.size()]);
	}
	

	public static void remoteFileDownload(ILaunchConfiguration config,
			ILaunch launch, String localExePath, String remoteExePath,
			IProgressMonitor monitor) throws CoreException {

		boolean skipDownload = config
				.getAttribute(
						IRemoteConnectionConfigurationConstants.ATTR_SKIP_DOWNLOAD_TO_TARGET,
						false);

		if (skipDownload)
			// Nothing to do. Download is skipped.
			return;
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
			remoteShellExec(
					config,
					"", "chmod", "+x " + spaceEscapify(remotePath.toString()), new SubProgressMonitor(monitor, 5)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} catch (SystemOperationCancelledException e) {
			abort(e.getLocalizedMessage(), null, IStatus.CANCEL);
		} catch (Exception e) {
			abort(Messages.RemoteRunLaunchDelegate_6, e,
					ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		} finally {
			monitor.done();
		}
	}

	public static String spaceEscapify(String inputString) {
		if (inputString == null)
			return null;

		return inputString.replaceAll(" ", "\\\\ "); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static IHost getCurrentConnection(ILaunchConfiguration config)
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

	public static Process remoteShellExec(ILaunchConfiguration config,
			String prelaunchCmd, String remoteCommandPath, String arguments,
			IProgressMonitor monitor) throws CoreException {
		// The exit command is called to force the remote shell to close after
		// our command
		// is executed. This is to prevent a running process at the end of the
		// debug session.
		// See Bug 158786.
		monitor.beginTask(NLS.bind(Messages.RemoteRunLaunchDelegate_8,
				remoteCommandPath, arguments), 10);
		String realRemoteCommand = arguments == null ? spaceEscapify(remoteCommandPath)
				: spaceEscapify(remoteCommandPath) + " " + arguments; //$NON-NLS-1$

		String remoteCommand = realRemoteCommand + CMD_DELIMITER + EXIT_CMD;

		if (!prelaunchCmd.trim().equals("")) //$NON-NLS-1$
			remoteCommand = prelaunchCmd + CMD_DELIMITER + remoteCommand;

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
				hostShell.writeToShell(remoteCommand);
				p = new HostShellProcessAdapter(hostShell);
			} catch (Exception e) {
				if (p != null) {
					p.destroy();
				}
				abort(Messages.RemoteRunLaunchDelegate_7, e,
						ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
			}
		} catch (Exception e1) {
			abort(e1.getMessage(), e1,
					ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		}

		monitor.done();
		return p;
	}

	public static String getRemoteHostname(ILaunchConfiguration config)
			throws CoreException {
		IHost currentConnection = getCurrentConnection(config);
		return currentConnection.getHostName();
	}

	/**
	 * Throws a core exception with an error status object built from the given
	 * message, lower level exception, and error code.
	 * 
	 * @param message
	 *            the status message
	 * @param exception
	 *            lower level exception associated with the error, or
	 *            <code>null</code> if none
	 * @param code
	 *            error code
	 */
	public static void abort(String message, Throwable exception, int code) throws CoreException {
		IStatus status;
		if (exception != null) {
			MultiStatus multiStatus = new MultiStatus(Activator.PLUGIN_ID, code, message, exception);
			multiStatus.add(new Status(IStatus.ERROR, Activator.PLUGIN_ID, code, exception.getLocalizedMessage(), exception));
			status= multiStatus;
		} else {
			status= new Status(IStatus.ERROR, Activator.PLUGIN_ID, code, message, null);
		}
		throw new CoreException(status);
	}

	
}
