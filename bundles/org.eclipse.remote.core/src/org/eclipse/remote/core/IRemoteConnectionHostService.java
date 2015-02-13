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
	 * @return
	 */
	String getHostname();

	/**
	 * Obtain the username associated with this connection.
	 * 
	 * @return
	 */
	String getUsername();

	/**
	 * Obtain the port associated with this connection
	 * 
	 * @return
	 */
	int getPort();
}
