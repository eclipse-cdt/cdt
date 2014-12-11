/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Doug Schaefer (QNX) - initial
 *******************************************************************************/
package org.eclipse.remote.core.api2;

/**
 * Root interface for all services available from a connection.
 */
public interface IRemoteConnectionService {

	/**
	 * Return the connection this service applies to.
	 * 
	 * @return connection
	 */
	IRemoteConnection getConnection();

}
