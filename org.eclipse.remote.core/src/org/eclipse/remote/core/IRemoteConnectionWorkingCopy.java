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

public interface IRemoteConnectionWorkingCopy extends IRemoteConnection {
	/**
	 * Set the address for this connection
	 * 
	 * @param address
	 */
	public void setAddress(String address);

	/**
	 * Set an implementation dependent attribute for the connection. Attributes keys supported by the connection can be obtained
	 * using {@link #getAttributes()}. Attributes are persisted along with connection information.
	 * 
	 * @param key
	 *            attribute key
	 * @param value
	 *            attribute value
	 * @since 5.0
	 */
	public void setAttribute(String key, String value);

	/**
	 * Set the name for this connection
	 * 
	 * @param name
	 */
	public void setName(String name);

	/**
	 * Set the password for this connection
	 * 
	 * @param password
	 * @since 5.0
	 */
	public void setPassword(String password);

	/**
	 * Set the port used for this connection. Only valid if supported by the underlying service provider.
	 * 
	 * @param port
	 *            port number for the connection
	 * @since 5.0
	 */
	public void setPort(int port);

	/**
	 * Set the username for this connection
	 * 
	 * @param username
	 */
	public void setUsername(String username);

	public void save();
}
