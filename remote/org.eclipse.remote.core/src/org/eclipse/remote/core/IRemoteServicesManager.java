/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX - initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.core;

import java.net.URI;
import java.util.List;

/**
 * The main entry point into the remote services system. The remote services manager
 * is an OSGi service. It provides a list of connection types and the global
 * list of all connections.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 2.0
 */
public interface IRemoteServicesManager {

	/**
	 * Get the connection type identified by the id
	 *
	 * @param id
	 *            id of the connection type
	 * @return connection type or null if the service can not be found
	 */
	IRemoteConnectionType getConnectionType(String id);

	/**
	 * Get the connection type that provides connections to locations identified by
	 * the URI.
	 *
	 * @param uri
	 *            uri of locations to be accessed
	 * @return the connection type that can be used to access the locations
	 *         or null if no connection type is available for the uri.
	 */
	IRemoteConnectionType getConnectionType(URI uri);

	/**
	 * Return the connection type used to access local resources.
	 *
	 * @return the local services
	 */
	IRemoteConnectionType getLocalConnectionType();

	/**
	 * Returns the list of all connection types including the local services.
	 *
	 * @return all connection types
	 */
	List<IRemoteConnectionType> getAllConnectionTypes();

	/**
	 * Returns the list of all connection types that support connections that provide specific services. The connections
	 * can provide additional services that are not included in the list, so this just guarantees the minimum set of services that
	 * will be supported.
	 *
	 * @param services
	 *            services provided by connections supported by this connection type
	 * @return compatible connection types
	 */
	@SuppressWarnings("unchecked")
	List<IRemoteConnectionType> getConnectionTypesSupporting(Class<? extends IRemoteConnection.Service>... services);

	/**
	 * Returns the list of all connection types that provide specific services. The connection types can provide additional services
	 * that are not included in the list, so this just guarantees the minimum set of services that will be supported.
	 *
	 * @param services
	 *            services provided by this connection type
	 * @return compatible connection types
	 */
	@SuppressWarnings("unchecked")
	List<IRemoteConnectionType> getConnectionTypesByService(Class<? extends IRemoteConnectionType.Service>... services);

	/**
	 * Returns the list of connection types except for the local connection type.
	 *
	 * @return all connection types that are really remote
	 */
	List<IRemoteConnectionType> getRemoteConnectionTypes();

	/**
	 * Returns the list of all known remote connections.
	 *
	 * @return all remote connections
	 */
	List<IRemoteConnection> getAllRemoteConnections();

	/**
	 * Add a global connection change listener that receives events for all connections.
	 *
	 * @param listener
	 *            global connection change listener to be added
	 */
	void addRemoteConnectionChangeListener(IRemoteConnectionChangeListener listener);

	/**
	 * Remove the global connection change listener.
	 *
	 * @param listener
	 *            global connection change listener to be removed
	 */
	void removeRemoteConnectionChangeListener(IRemoteConnectionChangeListener listener);

	/**
	 * Used by connections and other components to notify the global connection
	 * change listeners of events.
	 *
	 * @param event
	 *            connection change event
	 */
	void fireRemoteConnectionChangeEvent(RemoteConnectionChangeEvent event);

}
