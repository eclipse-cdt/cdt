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
import org.eclipse.remote.core.IRemoteProcess;
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
	private final boolean canAdd;
	private final boolean canEdit;
	private final boolean canRemove;

	private final Map<Class<? extends Service>, Object> serviceMap = new HashMap<>();
	private final Map<String, IConfigurationElement> serviceDefinitionMap = new HashMap<>();
	private final Map<String, IConfigurationElement> connectionServiceDefinitionMap = new HashMap<>();
	private final Map<String, IConfigurationElement> processServiceDefinitionMap = new HashMap<>();

	private final Map<String, RemoteConnection> connections = new HashMap<>();

	public RemoteConnectionType(IConfigurationElement ce, RemoteServicesManager manager) {
		this.remoteServicesManager = manager;
		id = ce.getAttribute("id"); //$NON-NLS-1$
		name = ce.getAttribute("name"); //$NON-NLS-1$
		scheme = ce.getAttribute("scheme"); //$NON-NLS-1$

		// capabilities, default is true for all of these
		String canAddStr = ce.getAttribute("canAdd"); //$NON-NLS-1$
		canAdd = canAddStr != null ? Boolean.parseBoolean(canAddStr) : true;
		
		String canEditStr = ce.getAttribute("canEdit"); //$NON-NLS-1$
		canEdit = canEditStr != null ? Boolean.parseBoolean(canEditStr) : true;
		
		String canRemoveStr = ce.getAttribute("canRemove"); //$NON-NLS-1$
		canRemove = canRemoveStr != null ? Boolean.parseBoolean(canRemoveStr) : true;
		
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnectionType#getRemoteServicesManager()
	 */
	@Override
	public IRemoteServicesManager getRemoteServicesManager() {
		return remoteServicesManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnectionType#getId()
	 */
	@Override
	public String getId() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnectionType#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnectionType#getScheme()
	 */
	@Override
	public String getScheme() {
		return scheme;
	}

	@Override
	public boolean canAdd() {
		return canAdd;
	}

	@Override
	public boolean canEdit() {
		return canEdit;
	}

	@Override
	public boolean canRemove() {
		return canRemove;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnectionType#getService(java.lang.Class)
	 */
	@Override
	public <T extends Service> T getService(Class<T> service) {
		@SuppressWarnings("unchecked")
		T obj = (T) serviceMap.get(service);
		if (obj == null) {
			IConfigurationElement ce = serviceDefinitionMap.get(service.getName());
			if (ce != null) {
				try {
					Service.Factory factory = (Service.Factory) ce.createExecutableExtension("factory"); //$NON-NLS-1$
					if (factory != null) {
						obj = factory.getService(this, service);
						serviceMap.put(service, obj);
					}
				} catch (CoreException e) {
					RemoteCorePlugin.log(e.getStatus());
				}
			}
		}
		return obj;
	}

	@Override
	public List<String> getServices() {
		return new ArrayList<>(serviceDefinitionMap.keySet());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnectionType#hasService(java.lang.Class)
	 */
	@Override
	public <T extends Service> boolean hasService(Class<T> service) {
		return serviceDefinitionMap.get(service.getName()) != null;
	}

	/**
	 * Called from the connection to get a service object for that connection.
	 * 
	 * @param connection
	 *            the connection to which the service applies
	 * @param service
	 *            the interface the service must implement
	 * @return the service object
	 * @throws CoreException
	 */
	public <T extends IRemoteConnection.Service> T getConnectionService(IRemoteConnection connection, Class<T> service) {
		IConfigurationElement ce = connectionServiceDefinitionMap.get(service.getName());
		if (ce != null) {
			try {
				IRemoteConnection.Service.Factory factory = (IRemoteConnection.Service.Factory) ce
						.createExecutableExtension("factory"); //$NON-NLS-1$
				if (factory != null) {
					return factory.getService(connection, service);
				}
			} catch (CoreException e) {
				RemoteCorePlugin.log(e.getStatus());
			}
		}

		return null;
	}

	@Override
	public List<String> getConnectionServices() {
		return new ArrayList<>(connectionServiceDefinitionMap.keySet());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnectionType#hasConnectionService(java.lang.Class)
	 */
	@Override
	public <T extends IRemoteConnection.Service> boolean hasConnectionService(Class<T> service) {
		return connectionServiceDefinitionMap.get(service.getName()) != null;
	}

	/**
	 * Called from the remote process to get a service object for that process.
	 * 
	 * @param process
	 *            the process to which the service applies
	 * @param service
	 *            the interface the service must implement
	 * @return the service object
	 * @throws CoreException
	 */
	public <T extends IRemoteProcess.Service> T getProcessService(IRemoteProcess process, Class<T> service) {
		IConfigurationElement ce = processServiceDefinitionMap.get(service.getName());
		if (ce != null) {
			try {
				IRemoteProcess.Service.Factory factory = (IRemoteProcess.Service.Factory) ce.createExecutableExtension("factory"); //$NON-NLS-1$
				if (factory != null) {
					return factory.getService(process, service);
				}
			} catch (CoreException e) {
				RemoteCorePlugin.log(e.getStatus());
			}
		}

		return null;
	}

	@Override
	public List<String> getProcessServices() {
		return new ArrayList<>(processServiceDefinitionMap.keySet());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnectionType#hasProcessService(java.lang.Class)
	 */
	@Override
	public <T extends IRemoteProcess.Service> boolean hasProcessService(Class<T> service) {
		return processServiceDefinitionMap.get(service.getName()) != null;
	}

	/**
	 * Called from the remote service manager to register a service extension for
	 * this remote services implementation
	 * 
	 * @param ce
	 *            the extension element defining the service
	 */
	public void addService(IConfigurationElement ce) {
		String serviceName = ce.getAttribute("service"); //$NON-NLS-1$
		String name = ce.getName();
		switch (name) {
		case "connectionTypeService": //$NON-NLS-1$
			serviceDefinitionMap.put(serviceName, ce);
			break;
		case "connectionService": //$NON-NLS-1$
			connectionServiceDefinitionMap.put(serviceName, ce);
			break;
		case "processService": //$NON-NLS-1$
			processServiceDefinitionMap.put(serviceName, ce);
			break;
		}
	}

	/**
	 * Signal connection has been added.
	 * 
	 * @since 2.0
	 */
	protected void connectionAdded(final IRemoteConnection connection) {
		RemoteConnectionChangeEvent event = new RemoteConnectionChangeEvent(connection,
				RemoteConnectionChangeEvent.CONNECTION_ADDED);
		remoteServicesManager.fireRemoteConnectionChangeEvent(event);
	}

	/**
	 * Signal a connnection is about to be removed.
	 * 
	 * @since 2.0
	 */
	protected void connectionRemoved(final IRemoteConnection connection) {
		RemoteConnectionChangeEvent event = new RemoteConnectionChangeEvent(connection,
				RemoteConnectionChangeEvent.CONNECTION_ADDED);
		remoteServicesManager.fireRemoteConnectionChangeEvent(event);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnectionType#getConnection(java.lang.String)
	 */
	@Override
	public IRemoteConnection getConnection(String name) {
		synchronized (connections) {
			return connections.get(name);
		}
	}

	@Override
	public IRemoteConnection getConnection(URI uri) {
		synchronized (connections) { 
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnectionType#getConnections()
	 */
	@Override
	public List<IRemoteConnection> getConnections() {
		synchronized (connections) {
			return new ArrayList<IRemoteConnection>(connections.values());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnectionType#newConnection(java.lang.String)
	 */
	@Override
	public IRemoteConnectionWorkingCopy newConnection(String name) throws RemoteConnectionException {
		synchronized (connections) {
			if (connections.containsKey(name)) {
				throw new ConnectionExistsException(name);
			}
			return new RemoteConnectionWorkingCopy(this, name);
		}
	}

	void addConnection(RemoteConnection remoteConnection) {
		synchronized (connections) {
			connections.put(remoteConnection.getName(), remoteConnection);
		}
	}

	void removeConnection(String name) {
		synchronized (connections) {
			connections.remove(name);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnectionType#removeConnection(org.eclipse.remote.core.IRemoteConnection)
	 */
	@Override
	public void removeConnection(IRemoteConnection connection) throws RemoteConnectionException {
		synchronized (connections) {
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

}
