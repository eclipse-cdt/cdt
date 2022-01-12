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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.remote.internal.core.RemoteCorePlugin;

/**
 * Exception thrown when a remote connection error occurs.
 */
public class RemoteConnectionException extends CoreException {
	private static final long serialVersionUID = -7794871221470179956L;

	/**
	 * @param message
	 * @param cause
	 */
	public RemoteConnectionException(String message, Throwable cause) {
		super(new Status(IStatus.ERROR, RemoteCorePlugin.getUniqueIdentifier(), message, cause));
	}

	/**
	 * @param message
	 */
	public RemoteConnectionException(String message) {
		this(message, null);
	}

	/**
	 * @param cause
	 */
	public RemoteConnectionException(Throwable cause) {
		this(cause.getMessage(), cause);
	}
}
