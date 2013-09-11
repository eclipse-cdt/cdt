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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.internal.remote.jsch.core.messages.Messages;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionManager;
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
	public Set<IRemoteConnection> getConnections() {
		loadConnections();
		Set<IRemoteConnection> set = new HashSet<IRemoteConnection>();
		set.addAll(fConnections.values());
		return set;
	}

	// private void loadAuth(ISecurePreferences node) throws StorageException {
	// JSchConnection connection = fConnections.get(node.name());
	// if (connection != null) {
	// boolean isPasswordAuth = node.getBoolean(IS_PASSWORD_AUTH_KEY, true);
	// connection.setIsPasswordAuth(isPasswordAuth);
	// if (isPasswordAuth) {
	// connection.setPassword(node.get(PASSWORD_KEY, null));
	// } else {
	// connection.setPassphrase(node.get(PASSPHRASE_KEY, null));
	// connection.setKeyFile(node.get(KEYFILE_KEY, null));
	// }
	// } else {
	// node.removeNode();
	// }
	// }

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
	public IRemoteConnection newConnection(String name) throws RemoteConnectionException {
		if (getConnection(name) != null) {
			throw new RemoteConnectionException(Messages.JSchConnectionManager_connection_with_this_name_exists);
		}
		return createConnection(name);
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

	// private void saveAuth(JSchConnection conn, ISecurePreferences node) throws StorageException {
	// boolean isPasswordAuth = conn.isPasswordAuth();
	// node.putBoolean(IS_PASSWORD_AUTH_KEY, isPasswordAuth, false);
	// if (isPasswordAuth) {
	// node.put(PASSWORD_KEY, conn.getPassword(), true);
	// } else {
	// node.put(PASSPHRASE_KEY, conn.getPassphrase(), true);
	// node.put(KEYFILE_KEY, conn.getKeyFile(), false);
	// }
	// }
	//
	// private void saveConnection(JSchConnection conn, Preferences node) {
	// node.put(HOST_KEY, conn.getAddress());
	// node.put(USER_KEY, conn.getUsername());
	// node.putInt(PORT_KEY, conn.getPort());
	// node.putInt(TIMEOUT_KEY, conn.getTimeout());
	// }
	//
	// public synchronized void saveConnections() {
	// if (fConnections != null) {
	// IEclipsePreferences root = InstanceScope.INSTANCE.getNode(Activator.getUniqueIdentifier());
	// Preferences connections = root.node(CONNECTIONS_KEY);
	// try {
	// connections.clear();
	// } catch (BackingStoreException e) {
	// Activator.log(e.getMessage());
	// }
	// for (JSchConnection conn : fConnections.values()) {
	// Preferences node = connections.node(conn.getName());
	// saveConnection(conn, node);
	// }
	// ISecurePreferences secRoot = SecurePreferencesFactory.getDefault();
	// ISecurePreferences secConnections = secRoot.node("org.eclipse.remote.jsch.connections");
	// secConnections.clear();
	// try {
	// for (JSchConnection conn : fConnections.values()) {
	// ISecurePreferences secNode = secConnections.node(conn.getName());
	// saveAuth(conn, secNode);
	// }
	// } catch (StorageException e) {
	// Activator.log(e.getMessage());
	// }
	// try {
	// root.flush();
	// } catch (BackingStoreException e) {
	// Activator.log(e.getMessage());
	// }
	// try {
	// secRoot.flush();
	// } catch (IOException e) {
	// Activator.log(e.getMessage());
	// }
	// }
	// }
}
