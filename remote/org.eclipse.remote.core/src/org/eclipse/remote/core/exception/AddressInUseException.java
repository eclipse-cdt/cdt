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
 * Exception thrown when a connection address is already in use.
 *
 */
public class AddressInUseException extends RemoteConnectionException {
	private static final long serialVersionUID = 1771839754428411610L;

	/**
	 * @param message
	 * @param cause
	 */
	public AddressInUseException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public AddressInUseException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public AddressInUseException(Throwable cause) {
		super(cause);
	}

}
