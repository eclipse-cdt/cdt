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
 * Exception thrown when an attempt is made to use a privileged port.
 */
public class PrivilegedPortException extends RemoteConnectionException {
	private static final long serialVersionUID = -7794871221470179956L;

	/**
	 * @param message
	 * @param cause
	 */
	public PrivilegedPortException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public PrivilegedPortException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public PrivilegedPortException(Throwable cause) {
		super(cause);
	}

}
