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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.internal.remote.jsch.core.messages.Messages;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionManager;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class JSchConnectionManager implements IRemoteConnectionManager {
	private final IRemoteServices fRemoteServices;
	private Map<String, JSchConnection> fConnections;

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
		loadConnections();
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

	public JSchConnection createConnection(String name) {
		return new JSchConnection(name, fRemoteServices);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.remote.core.IRemoteConnectionManager#getConnections()
	 */
	public List<IRemoteConnection> getConnections() {
		loadConnections();
		List<IRemoteConnection> conns = new ArrayList<IRemoteConnection>();
		conns.addAll(fConnections.values());
		return conns;
	}

	private synchronized void loadConnections() {
		if (fConnections == null) {
			fConnections = Collections.synchronizedMap(new HashMap<String, JSchConnection>());
			IEclipsePreferences root = InstanceScope.INSTANCE.getNode(Activator.getUniqueIdentifier());
			Preferences connections = root.node(JSchConnectionAttributes.CONNECTIONS_KEY);
			try {
				for (String name : connections.childrenNames()) {
					JSchConnection connection = new JSchConnection(name, fRemoteServices);
					fConnections.put(name, connection);
				}
			} catch (BackingStoreException e) {
				Activator.log(e.getMessage());
			}
		}
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
	public IRemoteConnectionWorkingCopy newConnection(String name) throws RemoteConnectionException {
		if (getConnection(name) != null) {
			throw new RemoteConnectionException(Messages.JSchConnectionManager_connection_with_this_name_exists);
		}
		return createConnection(name).getWorkingCopy();
	}

	public void add(JSchConnection conn) {
		if (!fConnections.containsKey(conn.getName())) {
			fConnections.put(conn.getName(), conn);
		}
	}

	public void remove(JSchConnection conn) {
		if (fConnections.containsKey(conn.getName())) {
			fConnections.remove(conn.getName());
		}
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
		((JSchConnection) conn).getInfo().remove();
		fConnections.remove(conn.getName());
	}
}
