/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.internal.core.services.local;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnection.Service;
import org.eclipse.remote.core.IRemoteConnectionHostService;

public class LocalConnectionHostService implements IRemoteConnectionHostService {

	private final IRemoteConnection connection;

	public LocalConnectionHostService(IRemoteConnection connection) {
		this.connection = connection;
	}

	public static class Factory implements IRemoteConnectionHostService.Factory {
		@SuppressWarnings("unchecked")
		@Override
		public <T extends Service> T getService(IRemoteConnection remoteConnection, Class<T> service) {
			if (service.equals(IRemoteConnectionHostService.class)) {
				return (T) new LocalConnectionHostService(remoteConnection);
			}
			return null;
		}
	}

	@Override
	public IRemoteConnection getRemoteConnection() {
		return this.connection;
	}

	@Override
	public String getHostname() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			return "unknown"; //$NON-NLS-1$
		}
	}

	@Override
	public int getPort() {
		return 0;
	}

	@Override
	public int getTimeout() {
		return 0;
	}

	@Override
	public boolean useLoginShell() {
		return true;
	}

	@Override
	public String getUsername() {
		return System.getProperty("user.name"); //$NON-NLS-1$
	}

	@Override
	public void setHostname(String hostname) {
		// Ignored
	}

	@Override
	public void setPassphrase(String passphrase) {
		// Ignored
	}

	@Override
	public void setPassword(String password) {
		// Ignored
	}

	@Override
	public void setPort(int port) {
		// Ignored
	}

	@Override
	public void setTimeout(int timeout) {
		// Ignored
	}

	@Override
	public void setUseLoginShell(boolean useLogingShell) {
		// Ignored
	}

	@Override
	public void setUsePassword(boolean usePassword) {
		// Ignored
	}

	@Override
	public void setUsername(String username) {
		// Ignored
	}
}
