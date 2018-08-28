/*******************************************************************************
 * Copyright (c) 2006, 2015 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Michael Scharf (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 * Johnson Ma (Wind River) - [218880] Add UI setting for ssh keepalives
 *******************************************************************************/
package org.eclipse.tm.terminal.connector.ssh.connector;

import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;

public interface ISshSettings {

	public static int DEFAULT_SSH_PORT = 22;

	/** 
	 * Get the host name or IP address of remote system to connect.
	 * @return host name or IP address of the remote system.
	 */ 
	String getHost();

	/**
	 * Get the login name for connecting to the remote system.
	 * @return remote login name
	 */ 
	String getUser();

	/**
	 *  Get the password for connecting to the remote system.
	 *  May be empty if connecting via SSH public key authentication
	 *  (with or without passphrase).
	 *  @return password to use 
	 */ 
	String getPassword();
	
	/**
	 * Get the timeout (in seconds) after which the SSH connection is assumed dead.
	 * @return timeout (in seconds) for the SSH connection.
	 */
	int getTimeout();

	/**
	 * Get the keepalive interval (in seconds).
	 * After this time of inactivity, the SSH connector will send a message to the
	 * remote system in order to avoid timeouts on the remote. A maximum of 6 
	 * keepalive messages will be sent if enabled. When set to 0, the keepalive 
	 * feature is disabled. 
	 * @return interval (in seconds) for keepalive messages.
	 */
	int getKeepalive();

	/**
	 * Get the TCP/IP port on the remote system to use.
	 * @return TCP/IP port on the remote system to use.
	 */
	int getPort();

	/**
	 * Return a human-readable String summarizing all relevant connection data.
	 * This String can be displayed in the Terminal caption, for instance.
	 * @return a human-readable String summarizing relevant connection data.
	 */
	String getSummary();

	/**
	 * Load connection data from a settings store.
	 * @param store the settings store to access.
	 */
	void load(ISettingsStore store);

	/**
	 * Store connection data into a settings store.
	 * @param store the settings store to access.
	 */
	void save(ISettingsStore store);
}
