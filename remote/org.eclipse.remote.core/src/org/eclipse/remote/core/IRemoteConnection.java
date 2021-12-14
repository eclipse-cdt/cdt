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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.remote.core.exception.RemoteConnectionException;

/**
 * Represents a connection to a remote system. Use the getService method to get at connection
 * specific services. Connections have state, open or closed. Some connection types are always
 * open in which case the close does nothing. Connections have properties which are values that
 * describe the connection and are discovered. Connections also have attributes which are
 * client specified values that control the connection.
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IRemoteConnection {
	/**
	 * The interface that is extend by services provided for this remote connection.
	 * 
	 * @since 2.0
	 */
	interface Service {
		IRemoteConnection getRemoteConnection();

		interface Factory {
			<T extends Service> T getService(IRemoteConnection remoteConnection, Class<T> service);
		}
	}

	// Common properties
	final static String OS_NAME_PROPERTY = "os.name"; //$NON-NLS-1$
	final static String OS_VERSION_PROPERTY = "os.version"; //$NON-NLS-1$
	final static String OS_ARCH_PROPERTY = "os.arch"; //$NON-NLS-1$
	final static String FILE_SEPARATOR_PROPERTY = "file.separator"; //$NON-NLS-1$
	final static String PATH_SEPARATOR_PROPERTY = "path.separator"; //$NON-NLS-1$
	final static String LINE_SEPARATOR_PROPERTY = "line.separator"; //$NON-NLS-1$
	final static String USER_HOME_PROPERTY = "user.home"; //$NON-NLS-1$
	/**
	 * @since 2.0
	 */
	final static String LOCALE_CHARMAP_PROPERTY = "locale.charmap"; //$NON-NLS-1$

	/**
	 * Get the connection type of this connection
	 * 
	 * @return connection type
	 * @since 2.0
	 */
	public IRemoteConnectionType getConnectionType();

	/**
	 * Get unique name for this connection.
	 * 
	 * @return connection name
	 */
	public String getName();

	/**
	 * Get the service for this remote connection that implements the given interface.
	 * 
	 * @param service
	 *            the interface the required service must implements
	 * @return the desired service or null if there is no such service available
	 * @throws CoreException
	 * @since 2.0
	 */
	<T extends Service> T getService(Class<T> service);

	/**
	 * Does this connection support the given service.
	 * 
	 * @param service
	 *            The service to be tested
	 * @return true if this connection supports the service
	 * @since 2.0
	 */
	<T extends Service> boolean hasService(Class<T> service);

	/**
	 * Open the connection. Must be called before the connection can be used.
	 * 
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the user. It is the caller's responsibility to call done()
	 *            on the given monitor. Accepts null, indicating that no progress should be reported and that the operation cannot
	 *            be cancelled.
	 * @throws RemoteConnectionException
	 */
	public void open(IProgressMonitor monitor) throws RemoteConnectionException;

	/**
	 * Close the connection. Must be called to terminate the connection.
	 */
	public void close();

	/**
	 * Test if the connection is open.
	 * 
	 * @return true if connection is open.
	 */
	public boolean isOpen();

	/**
	 * Gets the remote system property indicated by the specified key. The connection must be open prior to calling this method.
	 * 
	 * @param key
	 *            the name of the property
	 * @return the string value of the property, or null if no property has that key
	 */
	public String getProperty(String key);

	/**
	 * Get an attribute for a connection.
	 * 
	 * NOTE: the attributes do not include any security related information (e.g. passwords, keys, etc.)
	 * 
	 * @param key
	 * @return the attribute value, or empty string if not defined.
	 * @since 2.0
	 */
	public String getAttribute(String key);

	/**
	 * Get an attribute that is stored in secure storage, such as passwords.
	 * 
	 * @param key
	 * @return the attribute value, or empty string if not defined.
	 * @since 2.0
	 */
	public String getSecureAttribute(String key);

	/**
	 * Return a working copy to allow setting and changing of attributes.
	 * 
	 * @return working copy of remote
	 */
	public IRemoteConnectionWorkingCopy getWorkingCopy();

	/**
	 * Register a listener that will be notified when this connection's status changes.
	 * 
	 * @param listener
	 */
	public void addConnectionChangeListener(IRemoteConnectionChangeListener listener);

	/**
	 * Remove a listener that will be notified when this connection's status changes.
	 * 
	 * @param listener
	 */
	public void removeConnectionChangeListener(IRemoteConnectionChangeListener listener);

	/**
	 * Notify all listeners when this connection's status changes. See {{@link RemoteConnectionChangeEvent} for a list of event
	 * types.
	 * 
	 * @param event
	 *            event type indicating the nature of the event
	 */
	public void fireConnectionChangeEvent(int type);

}
