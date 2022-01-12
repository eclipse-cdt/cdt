/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.remote.core.exception;

/**
 * Exception thrown when a port is unable to be forwarded.
 */
public class UnableToForwardPortException extends RemoteConnectionException {
	/**
	 *
	 */
	private static final long serialVersionUID = -5814772755700213717L;

	/**
	 * @param message
	 * @param cause
	 */
	public UnableToForwardPortException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public UnableToForwardPortException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public UnableToForwardPortException(Throwable cause) {
		super(cause);
	}

}
