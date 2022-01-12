/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.core.exception;

/**
 * Thrown when trying to add a connection with the same name as an existing
 * connection, or when trying to rename a connection to the same name as an
 * existing connection.
 * 
 * @since 2.0
 */
public class ConnectionExistsException extends RemoteConnectionException {

	private static final long serialVersionUID = -1591235868439783613L;

	public ConnectionExistsException(String message) {
		super(message);
	}

}
