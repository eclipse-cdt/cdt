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
package org.eclipse.remote.core;

import java.net.URI;
import java.util.List;

import org.eclipse.remote.core.exception.RemoteConnectionException;

/**
 * Interface for managing connections to remote systems.
 */
public interface IRemoteConnectionManager {
	/**
	 * The name of the connection for local services. There is only one connection for local services.
	 * 
	 * @since 7.0
	 */
	public static String LOCAL_CONNECTION_NAME = "Local"; //$NON-NLS-1$

	/**
	 * Gets the remote connection corresponding to the supplied name.
	 * 
	 * @param name
	 *            name of the connection (as returned by {@link IRemoteConnection#getName()})
	 * @return remote connection or null if no connection exists
	 */
	public IRemoteConnection getConnection(String name);

	/**
	 * Gets the remote connection corresponding to the supplied URI.
	 * 
	 * @param uri
	 *            URI containing a schema for this remote connection
	 * @return remote connection or null if no connection exists or the schema
	 *         is incorrect
	 * @since 4.0
	 */
	public IRemoteConnection getConnection(URI uri);

	/**
	 * Get all the connections for this service provider.
	 * 
	 * @return connections that we know about
	 */
	public List<IRemoteConnection> getConnections();

	/**
	 * Get the user authenticator that will be used when opening connections. The user authenticator is specified using the
	 * org.eclipse.remote.core.authenticator extension point.
	 * 
	 * @param connection
	 *            connection that will use this authenticator
	 * @return user authenticator
	 */
	public IUserAuthenticator getUserAuthenticator(IRemoteConnection connection);

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
	public IRemoteConnectionWorkingCopy newConnection(String name) throws RemoteConnectionException;

	/**
	 * Remove a connection and all resources associated with it.
	 * 
	 * @param connection
	 *            connection to remove
	 * @throws RemoteConnectionException
	 *             if the connection could not be removed
	 */
	public void removeConnection(IRemoteConnection connection) throws RemoteConnectionException;
}
