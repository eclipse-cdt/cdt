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

package org.eclipse.remote.core.exception;

/**
 * Exception thrown when a remote connection error occurs.
 */
public class RemoteConnectionException extends Exception {
	private static final long serialVersionUID = -7794871221470179956L;

	/**
	 * @param message
	 * @param cause
	 */
	public RemoteConnectionException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public RemoteConnectionException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public RemoteConnectionException(Throwable cause) {
		super(cause);
	}

}
