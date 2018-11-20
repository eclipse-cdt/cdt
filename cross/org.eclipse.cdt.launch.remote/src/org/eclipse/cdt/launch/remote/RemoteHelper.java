/********************************************************************************
 * Copyright (c) 2009, 2015 MontaVista Software, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Anna Dushistova (MontaVista)      - initial API and implementation
 * Anna Dushistova (Mentor Graphics) - [314659] moved common methods for DSF and CDI launches to this class
 * Anna Dushistova (Mentor Graphics) - changed spaceEscapify visibility
 * Anna Dushistova (MontaVista)      - [318051][remote debug] Terminating when "Remote shell" process is selected doesn't work
 * Wainer Moschetta(IBM)             - [452356] replace RSE with org.eclipse.remote
 ********************************************************************************/

package org.eclipse.cdt.launch.remote;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.internal.launch.remote.Activator;
import org.eclipse.cdt.internal.launch.remote.Messages;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.osgi.util.NLS;
import org.eclipse.remote.core.IRemoteCommandShellService;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionHostService;
import org.eclipse.remote.core.IRemoteFileService;
import org.eclipse.remote.core.IRemoteProcess;
import org.eclipse.remote.core.IRemoteProcessBuilder;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.eclipse.remote.core.RemoteProcessAdapter;

public class RemoteHelper {

	private final static String EXIT_CMD = "exit"; //$NON-NLS-1$
	private final static String CMD_DELIMITER = ";"; //$NON-NLS-1$

	public static IRemoteConnection getRemoteConnectionByName(String remoteConnection) {
		if (remoteConnection == null)
			return null;
		IRemoteServicesManager manager = Activator.getService(IRemoteServicesManager.class);
		if (manager == null) {
			return null;
		}
		List<IRemoteConnection> conns = manager.getAllRemoteConnections();
		for (IRemoteConnection conn : conns) {
			if (conn.getName().contentEquals(remoteConnection)) {
				return conn;
			}
		}
		return null;
	}

	public static IRemoteFileService getFileSubsystem(IRemoteConnection conn) {
		if (conn == null) {
			return null;
		}
		return conn.getService(IRemoteFileService.class);
	}

	public static IRemoteConnection[] getSuitableConnections() {
		IRemoteServicesManager manager = Activator.getService(IRemoteServicesManager.class);
		if (manager == null)
			return null;
		ArrayList<IRemoteConnection> suitableConnections = new ArrayList<>();
		List<IRemoteConnection> allConnections = manager.getAllRemoteConnections();
		for (IRemoteConnection conn : allConnections) {
			if (conn.hasService(IRemoteCommandShellService.class)) {
				suitableConnections.add(conn);
			}
		}
		return suitableConnections.toArray(new IRemoteConnection[] {});
	}

	public static void remoteFileDownload(ILaunchConfiguration config, ILaunch launch, String localExePath,
			String remoteExePath, IProgressMonitor monitor) throws CoreException {

		boolean skipDownload = config.getAttribute(IRemoteConnectionConfigurationConstants.ATTR_SKIP_DOWNLOAD_TO_TARGET,
				false);

		if (skipDownload) {
			// Nothing to do. Download is skipped.
			return;
		}

		SubMonitor subMonitor = SubMonitor.convert(monitor, Messages.RemoteRunLaunchDelegate_2, 100);

		try {
			IRemoteConnection conn = getCurrentConnection(config);
			IRemoteFileService fs = conn.getService(IRemoteFileService.class);
			IFileStore remoteFile = fs.getResource(remoteExePath);

			IFileSystem localfs = EFS.getLocalFileSystem();
			IFileStore localFile = localfs.getStore(new Path(localExePath));

			if (!localFile.fetchInfo().exists()) {
				return;
			}

			/* Copy the file to the remote file system. */
			localFile.copy(remoteFile, EFS.OVERWRITE, subMonitor.split(95));

			/* Force the file to executable. */
			IFileInfo remoteFileInfo = remoteFile.fetchInfo();

			remoteFileInfo.setAttribute(EFS.ATTRIBUTE_OWNER_EXECUTE, true);
			remoteFileInfo.setAttribute(EFS.ATTRIBUTE_GROUP_EXECUTE, true);
			remoteFileInfo.setAttribute(EFS.ATTRIBUTE_OTHER_EXECUTE, true);

			remoteFile.putInfo(remoteFileInfo, EFS.SET_ATTRIBUTES, subMonitor.split(5));
		} catch (CoreException e) {
			abort(Messages.RemoteRunLaunchDelegate_6, e, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		} finally {
			monitor.done();
		}
	}

	public static String spaceEscapify(String inputString) {
		if (inputString == null)
			return null;

		return inputString.replaceAll(" ", "\\\\ "); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static IRemoteConnection getCurrentConnection(ILaunchConfiguration config) throws CoreException {
		String remoteConnection = config.getAttribute(IRemoteConnectionConfigurationConstants.ATTR_REMOTE_CONNECTION,
				""); //$NON-NLS-1$
		IRemoteConnection connection = getRemoteConnectionByName(remoteConnection);
		if (connection == null) {
			abort(Messages.RemoteRunLaunchDelegate_13, null, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		}
		return connection;
	}

	public static Process remoteShellExec(ILaunchConfiguration config, String prelaunchCmd, String remoteCommandPath,
			String arguments, IProgressMonitor monitor) throws CoreException {
		// The exit command is called to force the remote shell to close after
		// our command
		// is executed. This is to prevent a running process at the end of the
		// debug session.
		// See Bug 158786.
		monitor.beginTask(NLS.bind(Messages.RemoteRunLaunchDelegate_8, remoteCommandPath, arguments), 10);
		String realRemoteCommand = arguments == null ? spaceEscapify(remoteCommandPath)
				: spaceEscapify(remoteCommandPath) + " " + arguments; //$NON-NLS-1$

		String remoteCommand = realRemoteCommand + CMD_DELIMITER + EXIT_CMD + "\r\n"; //$NON-NLS-1$

		if (!prelaunchCmd.trim().equals("")) //$NON-NLS-1$
			remoteCommand = prelaunchCmd + CMD_DELIMITER + remoteCommand;

		IRemoteConnection conn = getCurrentConnection(config);

		IRemoteCommandShellService shellService = conn.getService(IRemoteCommandShellService.class);
		IRemoteProcess remoteProcess = null;
		Process p = null;

		try {
			remoteProcess = shellService.getCommandShell(IRemoteProcessBuilder.ALLOCATE_PTY);
			p = new RemoteProcessAdapter(remoteProcess);
			OutputStream os = remoteProcess.getOutputStream();
			os.write(remoteCommand.getBytes());
			os.flush();
		} catch (IOException e) {
			abort(Messages.RemoteRunLaunchDelegate_7, e, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		}
		monitor.done();
		return p;
	}

	public static IRemoteProcess execCmdInRemoteShell(ILaunchConfiguration config, String prelaunchCmd,
			String remoteCommandPath, String arguments, IProgressMonitor monitor) throws Exception {
		// The exit command is called to force the remote shell to close after
		// our command
		// is executed. This is to prevent a running process at the end of the
		// debug session.
		// See Bug 158786.
		monitor.beginTask(NLS.bind(Messages.RemoteRunLaunchDelegate_8, remoteCommandPath, arguments), 10);
		String realRemoteCommand = arguments == null ? spaceEscapify(remoteCommandPath)
				: spaceEscapify(remoteCommandPath) + " " + arguments; //$NON-NLS-1$

		String remoteCommand = realRemoteCommand + CMD_DELIMITER + EXIT_CMD + "\r\n"; //$NON-NLS-1$

		if (!prelaunchCmd.trim().equals("")) //$NON-NLS-1$
			remoteCommand = prelaunchCmd + CMD_DELIMITER + remoteCommand;

		IRemoteConnection conn = getCurrentConnection(config);
		if (!conn.isOpen()) {
			conn.open(monitor);
		}

		IRemoteCommandShellService shellService = conn.getService(IRemoteCommandShellService.class);
		IRemoteProcess p = null;
		p = shellService.getCommandShell(IRemoteProcessBuilder.ALLOCATE_PTY);
		OutputStream os = p.getOutputStream();
		os.write(remoteCommand.getBytes());
		os.flush();
		monitor.done();
		return p;
	}

	public static String getRemoteHostname(ILaunchConfiguration config) throws CoreException {
		IRemoteConnection currentConnection = getCurrentConnection(config);
		IRemoteConnectionHostService hostService = currentConnection.getService(IRemoteConnectionHostService.class);
		return hostService.getHostname();
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
			multiStatus.add(
					new Status(IStatus.ERROR, Activator.PLUGIN_ID, code, exception.getLocalizedMessage(), exception));
			status = multiStatus;
		} else {
			status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, code, message, null);
		}
		throw new CoreException(status);
	}

}
