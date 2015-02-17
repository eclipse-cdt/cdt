/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.internal.core;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.eclipse.remote.core.RemoteConnectionChangeEvent;
import org.eclipse.remote.core.exception.ConnectionExistsException;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * The implementation for a given remote services collection.n
 */
public class RemoteConnectionType implements IRemoteConnectionType {

	private final RemoteServicesManager remoteServicesManager;
	private final String id;
	private final String name;
	private final String scheme;
	private final int capabilities;

	private final Map<String, Object> serviceMap = new HashMap<>();
	private final Map<String, IConfigurationElement> serviceDefinitionMap = new HashMap<>();
	
	private final Map<String, RemoteConnection> connections = new HashMap<>();

	public RemoteConnectionType(IConfigurationElement ce, RemoteServicesManager manager) {
		this.remoteServicesManager = manager;
		id = ce.getAttribute("id"); //$NON-NLS-1$
		name = ce.getAttribute("name"); //$NON-NLS-1$
		scheme = ce.getAttribute("scheme"); //$NON-NLS-1$

		String caps = ce.getAttribute("capabilities"); //$NON-NLS-1$
		if (caps != null) {
			capabilities = Integer.parseInt(caps);
		} else {
			capabilities = 0;
		}
		
		// load up existing connections
		try {
			for (String connectionName : getPreferenceNode().childrenNames()) {
				connections.put(connectionName, new RemoteConnection(this, connectionName));
			}
		} catch (BackingStoreException e) {
			RemoteCorePlugin.log(e);
		}
	}

	Preferences getPreferenceNode() {
		return remoteServicesManager.getPreferenceNode().node(id);
	}

	ISecurePreferences getSecurePreferencesNode() {
		return remoteServicesManager.getSecurePreferenceNode().node(id);
	}

	@Override
	public IRemoteServicesManager getRemoteServicesManager() {
		return remoteServicesManager;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getScheme() {
		return scheme;
	}

	@Override
	public int getCapabilities() {
		return capabilities;
	}

	@Override
	public <T extends Service> T getService(Class<T> service) {
		String serviceName = service.getName();
		@SuppressWarnings("unchecked")
		T obj = (T) serviceMap.get(serviceName);
		if (obj == null) {
			IConfigurationElement ce = serviceDefinitionMap.get(serviceName);
			if (ce != null) {
				try {
					Service.Factory factory = (Service.Factory) ce.createExecutableExtension("factory"); //$NON-NLS-1$
					if (factory != null) {
						obj = factory.getService(this, service);
						serviceMap.put(serviceName, obj);
						serviceDefinitionMap.remove(serviceName);
					}
				} catch (CoreException e) {
					RemoteCorePlugin.log(e.getStatus());
				}
			}
		}
		return obj;
	}

	@Override
	public <T extends Service> boolean hasService(Class<T> service) {
		String serviceName = service.getName();
		return serviceMap.get(serviceName) != null || serviceDefinitionMap.get(service) != null;
	}

	/**
	 * Called from the connection to get a service object for that connection.
	 * 
	 * @param connection the connection to which the service applies
	 * @param service the interface the service must implement
	 * @return the service object
	 * @throws CoreException
	 */
	public <T extends IRemoteConnection.Service> T getConnectionService(IRemoteConnection connection, Class<T> service) {
		// Both top level and connection services are stored in the serviceDefinitionMap.
		// In theory the two sets of interfaces can't collide.
		IConfigurationElement ce = serviceDefinitionMap.get(service.getName());
		if (ce != null) {
			try {
				IRemoteConnection.Service.Factory factory = (IRemoteConnection.Service.Factory) ce.createExecutableExtension("factory"); //$NON-NLS-1$
				if (factory != null) {
					return factory.getService(connection, service);
				}
			} catch (CoreException e) {
				RemoteCorePlugin.log(e.getStatus());
			}
		}

		return null;
	}

	public <T extends IRemoteConnection.Service> boolean hasConnectionService(IRemoteConnection connection, Class<T> service) {
		return serviceDefinitionMap.get(service.getName()) != null;
	}

	/**
	 * Called from the remote service manager to register a service extension for
	 * this remote services implementation
	 * 
	 * @param ce the extension element defining the service
	 */
	public void addService(IConfigurationElement ce) {
		String service = ce.getAttribute("service"); //$NON-NLS-1$
		serviceDefinitionMap.put(service, ce);
	}

	/**
	 * Signal connection has been added.
	 * @since 2.0
	 */
	protected void connectionAdded(final IRemoteConnection connection) {
		RemoteConnectionChangeEvent event = new RemoteConnectionChangeEvent(connection, RemoteConnectionChangeEvent.CONNECTION_ADDED);
		remoteServicesManager.fireRemoteConnectionChangeEvent(event);
	}

	/**
	 * Signal a connnection is about to be removed.
	 * @since 2.0
	 */
	protected void connectionRemoved(final IRemoteConnection connection) {
		RemoteConnectionChangeEvent event = new RemoteConnectionChangeEvent(connection, RemoteConnectionChangeEvent.CONNECTION_ADDED);
		remoteServicesManager.fireRemoteConnectionChangeEvent(event);
	}

	@Override
	public IRemoteConnection getConnection(String name) {
		return connections.get(name);
	}

	@Override
	public IRemoteConnection getConnection(URI uri) {
		IRemoteConnection connection = connections.get(uri.getAuthority());
		if (connection != null) {
			return connection;
		}
		
		// If it's a file: scheme we must be the local connection type, just return our
		// hopefully one connection, the Local connection.
		if (uri.getScheme().equals("file") && !connections.isEmpty()) { //$NON-NLS-1$
			return connections.values().iterator().next();
		}
		
		return null;
	}

	@Override
	public List<IRemoteConnection> getConnections() {
		return new ArrayList<IRemoteConnection>(connections.values());
	}

	@Override
	public IRemoteConnectionWorkingCopy newConnection(String name) throws RemoteConnectionException {
		if (connections.containsKey(name)) {
			throw new ConnectionExistsException(name);
		}
		return new RemoteConnectionWorkingCopy(this, name);
	}

	void addConnection(RemoteConnection remoteConnection) {
		connections.put(remoteConnection.getName(), remoteConnection);
	}

	void removeConnection(String name) {
		connections.remove(name);
	}

	@Override
	public void removeConnection(IRemoteConnection connection) throws RemoteConnectionException {
		if (connection instanceof RemoteConnection) {
			connection.close();
			RemoteConnection conn = (RemoteConnection) connection;
			try {
				conn.getPreferences().removeNode();
			} catch (BackingStoreException e) {
				throw new RemoteConnectionException(e);
			}
			conn.getSecurePreferences().removeNode();
			connections.remove(conn.getName());
			connection.fireConnectionChangeEvent(RemoteConnectionChangeEvent.CONNECTION_REMOVED);
		} else {
			RemoteCorePlugin.log("Wrong class for " + connection.getName() + ", was " + connection.getClass().getName()); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

}
