/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *******************************************************************************/
package org.eclipse.tm.terminal.connector.remote;

public interface IRemoteSettings {
	public static final String CONNECTION_NAME = "ConnectionName"; //$NON-NLS-1$
	public static final String CONNECTION_TYPE_ID = "ConnectionTypeId"; //$NON-NLS-1$

	/**
	 * Get the connection type ID for the connection (e.g. local, ssh, etc.)
	 * 
	 * @return connection type ID.
	 */
	String getConnectionTypeId();

	/**
	 * Get the connection name for the target system.
	 * 
	 * @return connection name
	 */
	String getConnectionName();
}
