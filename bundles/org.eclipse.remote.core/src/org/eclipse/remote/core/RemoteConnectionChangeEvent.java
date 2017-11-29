/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.core;

/**
 * Event representing a change in connection status. The {@link #getType()} method can be used to obtain information about the type
 * of event that occurred.
 *
 * @since 2.0
 */
public class RemoteConnectionChangeEvent {

	/**
	 * Event indicating that the connection was closed.
	 */
	public static final int CONNECTION_CLOSED = 1 << 0;

	/**
	 * Event indicating that the connection was opened.
	 */
	public static final int CONNECTION_OPENED = 1 << 1;

	/**
	 * Event indicating that the connection was closed abnormally.
	 */
	public static final int CONNECTION_ABORTED = 1 << 2;

	/**
	 * Event indicating that the connection name will be changed. It is sent prior to the name change taking place.
	 * The event passes an IRemoteConnectionWorkingCopy so that the original and new name can be used by the listener.
	 */
	public static final int CONNECTION_RENAMED = 1 << 3;

	/**
	 * Event indicating the connection was added.
	 * @since 2.0
	 */
	public static final int CONNECTION_ADDED = 1 << 4;

	/**
	 * Event indicating the connection is about to be removed.
	 * @since 2.0
	 */
	public static final int CONNECTION_REMOVED = 1 << 5;

	/**
	 * Event indicating the connection attributes had changed.
	 * @since 3.0
	 */
	public static final int ATTRIBUTES_CHANGED = 1 << 6;

	private final IRemoteConnection connection;
	private final int type;

	public RemoteConnectionChangeEvent(IRemoteConnection connection, int type) {
		this.connection = connection;
		this.type = type;
	}

	/**
	 * Get the connection that has changed.
	 * 
	 * @return IRemoteConnection
	 */
	public IRemoteConnection getConnection() {
		return connection;
	}

	/**
	 * Returns the type of event being reported. This type
	 * is obtained by bitwise OR'ing the event types together.
	 * 
	 * {@link #CONNECTION_CLOSED} {@link #CONNECTION_OPENED} {@link #CONNECTION_ABORTED} {@link #CONNECTION_RENAMED}
	 * 
	 * @return a bitwise OR of event type constants
	 */
	public int getType() {
		return type;
	}

}
