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
package org.eclipse.internal.remote.jsch.core;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.internal.remote.jsch.core.messages.Messages;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionManager;
import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.remote.core.exception.RemoteConnectionException;

public class JSchConnectionManager implements IRemoteConnectionManager {
	private final IRemoteServices fRemoteServices;
	private final Map<String, IRemoteConnection> fConnections = new HashMap<String, IRemoteConnection>();

	/**
	 * @since 4.0
	 */
	public JSchConnectionManager(IRemoteServices services) {
		fRemoteServices = services;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.remote.core.IRemoteConnectionManager#getConnection(java
	 * .lang.String)
	 */
	public IRemoteConnection getConnection(String name) {
		return fConnections.get(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.remote.core.IRemoteConnectionManager#getConnection(java
	 * .net.URI)
	 */
	/**
	 * @since 4.0
	 */
	public IRemoteConnection getConnection(URI uri) {
		String connName = JSchFileSystem.getConnectionNameFor(uri);
		if (connName != null) {
			return getConnection(connName);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.remote.core.IRemoteConnectionManager#getConnections()
	 */
	public IRemoteConnection[] getConnections() {
		return fConnections.values().toArray(new IRemoteConnection[fConnections.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.remote.core.IRemoteConnectionManager#newConnection(java
	 * .lang.String, java.util.Map)
	 */
	/**
	 * @since 5.0
	 */
	public IRemoteConnection newConnection(String name) throws RemoteConnectionException {
		if (getConnection(name) != null) {
			throw new RemoteConnectionException(Messages.JSchConnectionManager_connection_with_this_name_exists);
		}
		IRemoteConnection connection = new JSchConnection(name, fRemoteServices);
		fConnections.put(name, connection);
		return connection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.remote.core.IRemoteConnectionManager#removeConnection
	 * (org.eclipse.remote.core.IRemoteConnection)
	 */
	public void removeConnection(IRemoteConnection conn) throws RemoteConnectionException {
		if (!(conn instanceof JSchConnection)) {
			throw new RemoteConnectionException(Messages.JSchConnectionManager_invalidConnectionType);
		}
		if (conn.isOpen()) {
			throw new RemoteConnectionException(Messages.JSchConnectionManager_cannotRemoveOpenConnection);
		}
		fConnections.remove(conn);
	}
}
