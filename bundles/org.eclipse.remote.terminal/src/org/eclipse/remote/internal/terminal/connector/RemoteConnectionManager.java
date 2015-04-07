/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.remote.internal.terminal.connector;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.osgi.util.NLS;
import org.eclipse.remote.core.IRemoteCommandShellService;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteProcess;
import org.eclipse.remote.core.IRemoteProcessBuilder;
import org.eclipse.remote.core.IRemoteProcessService;
import org.eclipse.remote.core.IRemoteProcessTerminalService;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.remote.internal.terminal.messages.Messages;
import org.eclipse.remote.internal.terminal.view.TerminalViewPlugin;
import org.eclipse.remote.terminal.IRemoteTerminalConstants;
import org.eclipse.remote.terminal.IRemoteTerminalParser;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;

@SuppressWarnings("restriction")
public class RemoteConnectionManager extends Job {
	private final static String PARSERS_EXTENSION_POINT = "parsers"; //$NON-NLS-1$
	private final static String PARSER_ELEMENT = "parser"; //$NON-NLS-1$
	private static int fgNo;

	private final ITerminalControl control;
	private final RemoteConnector connector;

	private IRemoteProcess remoteProcess;
	private IRemoteTerminalParser parser;

	protected RemoteConnectionManager(RemoteConnector conn, ITerminalControl control) {
		super("Remote Terminal-" + fgNo++); //$NON-NLS-1$
		this.control = control;
		this.connector = conn;
		setSystem(true);
		loadParserExtension();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		IRemoteConnection remoteConnection = null;

		try {
			IRemoteServicesManager svcMgr = TerminalViewPlugin.getService(IRemoteServicesManager.class);
			String connTypeId = connector.getSshSettings().getRemoteServices();
			IRemoteConnectionType connType = svcMgr.getConnectionType(connTypeId);
			if (connType != null) {
				remoteConnection = connType.getConnection(connector.getSshSettings().getConnectionName());
			}
			if (remoteConnection == null) {
				throw new RemoteConnectionException(NLS.bind(Messages.RemoteConnectionManager_0, connector.getSshSettings()
						.getConnectionName()));
			}

			if (!remoteConnection.isOpen()) {
				remoteConnection.open(monitor);
				if (!remoteConnection.isOpen()) {
					throw new RemoteConnectionException(NLS.bind(Messages.RemoteConnectionManager_1, connector.getSshSettings()
							.getConnectionName()));
				}
			}

			if (parser != null) {
				remoteProcess = parser.initialize(remoteConnection);
			} else {
				/*
				 * Check the terminal shell command preference. If the preference is empty and we support a command shell,
				 * just use that. Otherwise use the preference value if it is set, or fall back to a default if not.
				 */
				IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(TerminalViewPlugin.getUniqueIdentifier());
				String terminalShellCommand = prefs.get(IRemoteTerminalConstants.PREF_TERMINAL_SHELL_COMMAND, ""); //$NON-NLS-1$
				if (remoteConnection.hasService(IRemoteCommandShellService.class)) {
					IRemoteCommandShellService cmdShellSvc = remoteConnection.getService(IRemoteCommandShellService.class);
					remoteProcess = cmdShellSvc.getCommandShell(IRemoteProcessBuilder.ALLOCATE_PTY);
				} else {
					if ("".equals(terminalShellCommand)) { //$NON-NLS-1$
						terminalShellCommand = "/bin/bash -l"; //$NON-NLS-1$
					}
					IRemoteProcessService procSvc = remoteConnection.getService(IRemoteProcessService.class);
					IRemoteProcessBuilder processBuilder = procSvc.getProcessBuilder(new ArgumentParser(terminalShellCommand)
							.getTokenList());
					remoteProcess = processBuilder.start(IRemoteProcessBuilder.ALLOCATE_PTY);
				}
			}

			connector.setInputStream(remoteProcess.getInputStream());
			control.setState(TerminalState.CONNECTED);
			control.setTerminalTitle(remoteConnection.getName());
			connector.setOutputStream(remoteProcess.getOutputStream());

			// read data until the connection gets terminated
			readData(connector.getInputStream());
		} catch (IOException e) {
			return new Status(IStatus.ERROR, TerminalViewPlugin.getUniqueIdentifier(), e.getMessage());
		} catch (RemoteConnectionException e) {
			return new Status(IStatus.ERROR, TerminalViewPlugin.getUniqueIdentifier(), e.getMessage());
		} finally {
			// make sure the terminal is disconnected when the thread ends
			connector.disconnect();
			synchronized (this) {
				if (remoteProcess != null && !remoteProcess.isCompleted()) {
					remoteProcess.destroy();
				}
			}
		}
		return Status.OK_STATUS;
	}

	public void setTerminalSize(int cols, int rows, int width, int height) {
		if (remoteProcess != null) {
			IRemoteProcessTerminalService termService = remoteProcess.getService(IRemoteProcessTerminalService.class);
			if (termService != null) {
				termService.setTerminalSize(cols, rows, width, height);
			}
		}
	}

	/**
	 * Read the data from the connection and display it in the terminal.
	 * 
	 * @param in
	 * @throws IOException
	 */
	private void readData(InputStream in) throws IOException {
		byte[] buf = new byte[32 * 1024];
		while (getState() == Job.RUNNING) {
			int n = in.read(buf, 0, buf.length);
			if (n < 0) {
				break;
			}
			if (n > 0 && (parser == null || parser.parse(buf))) {
				control.getRemoteToTerminalOutputStream().write(buf, 0, n);
			}
		}
	}

	private void loadParserExtension() {
		IExtensionPoint point = RegistryFactory.getRegistry().getExtensionPoint(TerminalViewPlugin.getUniqueIdentifier(),
				PARSERS_EXTENSION_POINT);
		if (point != null) {
			IExtension[] extensions = point.getExtensions();
			for (IExtension extension : extensions) {
				IConfigurationElement[] elements = extension.getConfigurationElements();
				for (IConfigurationElement element : elements) {
					if (PARSER_ELEMENT.equals(element.getName())) {
						try {
							parser = (IRemoteTerminalParser) element.createExecutableExtension("class"); //$NON-NLS-1$
						} catch (CoreException e) {
							TerminalViewPlugin.log(e);
						}
					}
				}
			}
		}
	}
}
