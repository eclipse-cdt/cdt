/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation and others.
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
 * A service to obtain host specific information.
 * 
 * @since 2.0
 */
public interface IRemoteConnectionHostService extends IRemoteConnection.Service {

	/**
	 * Obtain the hostname associated with this connection.
	 * 
	 * @return hostname
	 */
	String getHostname();

	/**
	 * Obtain the port associated with this connection
	 * 
	 * @return port
	 */
	int getPort();

	/**
	 * Obtain the timeout used when establishing the connection.
	 * 
	 * @return timeout
	 */
	int getTimeout();

	/**
	 * Obtain the flag that indicates a login shell should be started once the connection is established
	 * 
	 * @return login shell flag
	 */
	boolean useLoginShell();

	/**
	 * Obtain the username associated with this connection.
	 * 
	 * @return
	 */
	String getUsername();

	/**
	 * Set the hostname associated with this connection. Note, this method can only be used for an
	 * IRemoteConnectionWorkingCopy and will have no effect otherwise.
	 * 
	 * @param hostname
	 *            new hostname for connection
	 */
	void setHostname(String hostname);

	/**
	 * Set the pass phrase associated with this connection. Note, this method can only be used for an
	 * IRemoteConnectionWorkingCopy and will have no effect otherwise.
	 * 
	 * @param passphrase
	 */
	void setPassphrase(String passphrase);

	/**
	 * Set the password associated with this connection. Note, this method can only be used for an
	 * IRemoteConnectionWorkingCopy and will have no effect otherwise.
	 * 
	 * @param password
	 *            new password for connection
	 */
	void setPassword(String password);

	/**
	 * Set the port used for the connection. Note, this method can only be used forh an
	 * IRemoteConnectionWorkingCopy and will have no effect otherwise.
	 * 
	 * @param port
	 *            new port for connection
	 */
	void setPort(int port);

	/**
	 * Set the timeout used when establishing the connection. A timeout of 0 means infinite. Note, this method can only be used
	 * for an IRemoteConnectionWorkingCopy and will have no effect otherwise.
	 * 
	 * @param timeout
	 *            new timeout value
	 */
	void setTimeout(int timeout);

	/**
	 * Set the flag indicating a login shell should be stated for this connection. Note, this method can only be used
	 * for an IRemoteConnectionWorkingCopy and will have no effect otherwise.
	 * 
	 * @param useLogingShell
	 *            true to start a login shell
	 */
	void setUseLoginShell(boolean useLogingShell);

	/**
	 * Set the connection to try password authentication first. Note, this method can only be used for an
	 * IRemoteConnectionWorkingCopy and will have no effect otherwise.
	 * 
	 * @param usePassword
	 *            use password authentication
	 */
	void setUsePassword(boolean usePassword);

	/**
	 * Set the username associated with this connection. Note, this method can only be used for an
	 * IRemoteConnectionWorkingCopy and will have no effect otherwise.
	 * 
	 * @param username
	 *            new username for connection
	 */
	void setUsername(String username);
}
