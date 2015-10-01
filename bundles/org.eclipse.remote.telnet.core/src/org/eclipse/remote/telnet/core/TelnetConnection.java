/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial contribution
 *******************************************************************************/
package org.eclipse.remote.telnet.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.remote.core.IRemoteCommandShellService;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnection.Service;
import org.eclipse.remote.core.IRemoteConnectionChangeListener;
import org.eclipse.remote.core.IRemoteConnectionControlService;
import org.eclipse.remote.core.IRemoteConnectionHostService;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.IRemoteProcess;
import org.eclipse.remote.core.RemoteConnectionChangeEvent;
import org.eclipse.remote.core.exception.RemoteConnectionException;

public class TelnetConnection implements IRemoteConnectionControlService, IRemoteCommandShellService, IRemoteConnectionHostService,
		IRemoteConnectionChangeListener {
	public static int DEFAULT_PORT = 23;
	public static int DEFAULT_TIMEOUT = 0; // Infinite

	public static final String HOSTNAME_ATTR = "telnet.hostname.attr"; //$NON-NLS-1$
	public static final String USERNAME_ATTR = "telnet.username.attr"; //$NON-NLS-1$
	public static final String PASSWORD_ATTR = "telnet.password.attr"; //$NON-NLS-1$
	public static final String PORT_ATTR = "telnet.port.attr"; //$NON-NLS-1$
	public static final String TIMEOUT_ATTR = "telnet.timeout.attr"; //$NON-NLS-1$

	private final IRemoteConnection remoteConnection;
	private final List<TelnetCommandShell> shells = Collections.synchronizedList(new ArrayList<TelnetCommandShell>());
	private boolean isOpen;

	private TelnetConnection(IRemoteConnection remoteConnection) {
		this.remoteConnection = remoteConnection;
		remoteConnection.addConnectionChangeListener(this);
	}

	public static class Factory implements IRemoteConnection.Service.Factory {
		@SuppressWarnings("unchecked")
		@Override
		public <T extends Service> T getService(IRemoteConnection remoteConnection, Class<T> service) {
			if (TelnetConnection.class.equals(service)) {
				return (T) new TelnetConnection(remoteConnection);
			} else if (IRemoteConnectionControlService.class.equals(service) || IRemoteConnectionHostService.class.equals(service)
					|| IRemoteCommandShellService.class.equals(service)) {
				return (T) remoteConnection.getService(TelnetConnection.class);
			}
			return null;
		}
	}

	@Override
	public IRemoteConnection getRemoteConnection() {
		return remoteConnection;
	}

	@Override
	public IRemoteProcess getCommandShell(int flags) throws IOException {
		if (isOpen) {
			TelnetCommandShell shell = new TelnetCommandShell(remoteConnection, this);
			try {
				shell.connect();
			} catch (RemoteConnectionException e) {
				throw new IOException(e.getMessage());
			}
			shells.add(shell);
			return shell;
		}
		return null;
	}

	@Override
	public int getPort() {
		try {
			String portStr = remoteConnection.getAttribute(PORT_ATTR);
			return !portStr.isEmpty() ? Integer.parseInt(portStr) : DEFAULT_PORT;
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	@Override
	public int getTimeout() {
		try {
			String timeoutStr = remoteConnection.getAttribute(TIMEOUT_ATTR);
			return !timeoutStr.isEmpty() ? Integer.parseInt(timeoutStr) : DEFAULT_TIMEOUT;
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	@Override
	public void close() {
		synchronized (shells) {
			for (TelnetCommandShell shell : shells) {
				shell.destroy();
			}
		}
		isOpen = false;
	}

	@Override
	public String getHostname() {
		return remoteConnection.getAttribute(HOSTNAME_ATTR);
	}

	@Override
	public boolean useLoginShell() {
		return true;
	}

	@Override
	public String getUsername() {
		return remoteConnection.getAttribute(USERNAME_ATTR);
	}

	@Override
	public void setHostname(String hostname) {
		if (remoteConnection instanceof IRemoteConnectionWorkingCopy) {
			IRemoteConnectionWorkingCopy wc = (IRemoteConnectionWorkingCopy) remoteConnection;
			wc.setAttribute(HOSTNAME_ATTR, hostname);
		}
	}

	@Override
	public void setPassphrase(String passphrase) {
		// Ignore
	}

	@Override
	public void setPassword(String password) {
		if (remoteConnection instanceof IRemoteConnectionWorkingCopy) {
			IRemoteConnectionWorkingCopy wc = (IRemoteConnectionWorkingCopy) remoteConnection;
			wc.setSecureAttribute(PASSWORD_ATTR, password);
		}
	}

	@Override
	public void setPort(int port) {
		if (remoteConnection instanceof IRemoteConnectionWorkingCopy) {
			IRemoteConnectionWorkingCopy wc = (IRemoteConnectionWorkingCopy) remoteConnection;
			wc.setAttribute(PORT_ATTR, Integer.toString(port));
		}
	}

	@Override
	public void setTimeout(int timeout) {
		if (remoteConnection instanceof IRemoteConnectionWorkingCopy) {
			IRemoteConnectionWorkingCopy wc = (IRemoteConnectionWorkingCopy) remoteConnection;
			wc.setAttribute(TIMEOUT_ATTR, Integer.toString(timeout));
		}
	}

	@Override
	public void setUseLoginShell(boolean useLogingShell) {
		// Ignore
	}

	@Override
	public void setUsePassword(boolean usePassword) {
		// Ignore
	}

	@Override
	public void setUsername(String username) {
		if (remoteConnection instanceof IRemoteConnectionWorkingCopy) {
			IRemoteConnectionWorkingCopy wc = (IRemoteConnectionWorkingCopy) remoteConnection;
			wc.setAttribute(USERNAME_ATTR, username);
		}
	}

	@Override
	public void open(IProgressMonitor monitor) throws RemoteConnectionException {
		isOpen = true;
	}

	@Override
	public boolean isOpen() {
		return isOpen;
	}

	@Override
	public void connectionChanged(RemoteConnectionChangeEvent event) {
		switch (event.getType()) {
		case RemoteConnectionChangeEvent.CONNECTION_OPENED:
			isOpen = true;
			break;

		case RemoteConnectionChangeEvent.CONNECTION_ABORTED:
		case RemoteConnectionChangeEvent.CONNECTION_CLOSED:
			isOpen = false;
			break;
		}
	}

	protected void terminated(TelnetCommandShell shell) {
		shells.remove(shell);
	}
}
