/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *******************************************************************************/
package org.eclipse.remote.terminal;

public interface IRemoteSettings {
	public static final String CONNECTION_NAME = "ConnectionName"; //$NON-NLS-1$
	public static final String REMOTE_SERVICES = "RemoteServices"; //$NON-NLS-1$

	/**
	 * Get the host name or IP address of remote system to connect.
	 * 
	 * @return host name or IP address of the remote system.
	 */
	String getRemoteServices();

	/**
	 * Get the login name for connecting to the remote system.
	 * 
	 * @return remote login name
	 */
	String getConnectionName();
}
