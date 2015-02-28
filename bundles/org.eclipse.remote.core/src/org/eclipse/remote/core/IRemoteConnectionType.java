/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.core;

import java.net.URI;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.remote.core.exception.RemoteConnectionException;

/**
 * A remote connection type manages a list of connections that implement the same services.
 * Services may be registered on the individual connections, or at the connection type level
 * for service that apply to all connections of this type.
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 2.0
 */
public interface IRemoteConnectionType {
	/**
	 * The interface that is extend by services provided for this remote services implementation.
	 * 
	 * @since 2.0
	 */
	interface Service {
		IRemoteConnectionType getConnectionType();

		interface Factory {
			<T extends Service> T getService(IRemoteConnectionType connectionType, Class<T> service);
		}
	}

	// Capabilities
	static final int CAPABILITY_ADD_CONNECTIONS = 0x01;
	static final int CAPABILITY_EDIT_CONNECTIONS = 0x02;
	static final int CAPABILITY_REMOVE_CONNECTIONS = 0x04;
	static final int CAPABILITY_SUPPORTS_TCP_PORT_FORWARDING = 0x08;
	static final int CAPABILITY_SUPPORTS_X11_FORWARDING = 0x10;

	/**
	 * Get the remote services manager. This is a convenient way to get back
	 * to the root.
	 * 
	 * @return remote services manager
	 */
	IRemoteServicesManager getRemoteServicesManager();

	/**
	 * Get unique ID of this service. Can be used as a lookup key.
	 * 
	 * @return unique ID
	 */
	String getId();

	/**
	 * Get display name of this service.
	 * 
	 * @return display name
	 */
	String getName();

	/**
	 * Get the EFS scheme provided by this service.
	 * 
	 * @return display name
	 */
	String getScheme();

	/**
	 * Gets the capabilities of the remote service.
	 * 
	 * @return bit-wise or of capability flag constants
	 */
	int getCapabilities();

	/**
	 * Get the service for this remote services implementation that implements the given interface.
	 * 
	 * @param service
	 *            the interface the required service must implements
	 * @return the desired service or null if there is no such service available
	 * @throws CoreException
	 * @since 2.0
	 */
	<T extends Service> T getService(Class<T> service);

	/**
	 * Does this connection type support the given service.
	 * 
	 * @param service
	 *            the service to be tested
	 * @return true if this connection type supports this service
	 */
	<T extends Service> boolean hasService(Class<T> service);

	/**
	 * Do connections created by this connection type support the given service.
	 * 
	 * @param service
	 *            the service to be tested
	 * @return true if connections created by this connection type support this service
	 */
	<T extends IRemoteConnection.Service> boolean hasConnectionService(Class<T> service);

	/**
	 * Gets the remote connection corresponding to the supplied name.
	 * 
	 * @param name
	 *            name of the connection (as returned by {@link IRemoteConnection#getName()})
	 * @return remote connection or null if no connection exists
	 */
	IRemoteConnection getConnection(String name);

	/**
	 * Gets the remote connection corresponding to the supplied URI.
	 * 
	 * @param uri
	 *            URI containing a schema for this remote connection
	 * @return remote connection or null if no connection exists or the schema
	 *         is incorrect
	 * @since 4.0
	 */
	IRemoteConnection getConnection(URI uri);

	/**
	 * Get all the connections for this service provider.
	 * 
	 * @return connections that we know about
	 */
	List<IRemoteConnection> getConnections();

	/**
	 * Creates a new remote connection named with supplied name. The connection attributes will be the default for the
	 * implementation.
	 * 
	 * Returns a working copy of the remote connection. Callers must call {@link IRemoteConnectionWorkingCopy#save()} before the
	 * connection can be used.
	 * 
	 * @param name
	 *            name of the connection
	 * @return a new connection working copy with the supplied name
	 * @throws RemoteConnectionException
	 *             if connection creation failed
	 * @since 5.0
	 */
	IRemoteConnectionWorkingCopy newConnection(String name) throws RemoteConnectionException;

	/**
	 * Remove a connection and all resources associated with it.
	 * 
	 * @param connection
	 *            connection to remove
	 * @throws RemoteConnectionException
	 *             if the connection could not be removed
	 */
	void removeConnection(IRemoteConnection connection) throws RemoteConnectionException;

}
