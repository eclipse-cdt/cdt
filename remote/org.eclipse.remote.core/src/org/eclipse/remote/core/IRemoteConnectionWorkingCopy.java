/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.core;

import org.eclipse.remote.core.exception.RemoteConnectionException;

/**
 * A working copy of a remote connection used to change the name and/or
 * attributes of the connection. It is also used when creating a new
 * connection
 */
public interface IRemoteConnectionWorkingCopy extends IRemoteConnection {

	/**
	 * Returns the original connection this working copy was created from.
	 * Returns null if this is a new connection.
	 *
	 * @return original connection
	 */
	public IRemoteConnection getOriginal();

	/**
	 * Returns whether this connection has been modified since it was last saved or created.
	 *
	 * @return true if the connection has been modified
	 */
	public boolean isDirty();

	/**
	 * Saves this working copy to its original connection and returns a handle to the resulting connection. Has no effect if this
	 * connection does not need saving.
	 *
	 * @return saved connection
	 * @throws RemoteConnectionException
	 */
	public IRemoteConnection save() throws RemoteConnectionException;

	/**
	 * Set the name for this connection
	 *
	 * @param name
	 */
	public void setName(String name);

	/**
	 * Set an implementation dependent attribute for the connection. Attributes keys supported by the connection can be obtained
	 * using {@link #getAttributes()}. Attributes are persisted along with connection information.
	 *
	 * @param key
	 *            attribute key
	 * @param value
	 *            attribute value
	 */
	public void setAttribute(String key, String value);

	/**
	 * Set an attribute such as a password that's stored in secure storage.
	 *
	 * @param key
	 * @param value
	 * @since 2.0
	 */
	public void setSecureAttribute(String key, String value);

}
