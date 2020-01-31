/*******************************************************************************
 * Copyright (c) 2015, 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
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
