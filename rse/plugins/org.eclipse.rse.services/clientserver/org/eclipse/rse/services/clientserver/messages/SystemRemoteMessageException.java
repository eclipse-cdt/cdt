/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - [226374] [api] Derived from RemoteFileException
 *******************************************************************************/

package org.eclipse.rse.services.clientserver.messages;


/**
 * Base class for remote system exceptions.
 * <p>
 * All remote system exceptions are guaranteed to have a translated message
 * retrievable via getMessage(), to make it easy to display to the user.
 * <p>
 * All child exceptions potentially contain an embedded exception that is the
 * original exception from the remote system.
 * <p>
 * Use {#link getRemoteException()} to retrieve that wrapped exception, if any.
 *
 * @since 3.0
 */
public class SystemRemoteMessageException extends SystemMessageException {

	/**
	 * A serialVersionUID is recommended for all serializable classes. This
	 * trait is inherited from Throwable. This should be updated if there is a
	 * schema change for this class.
	 */
	private static final long serialVersionUID = 1L;
	private Exception wrappedException = null;

	/**
	 * Constructor with an error message for getMessage() to return, and a
	 * wrapped exception to contain. It is accessed via getRemoteException().
	 *
	 * @param msg The fully resolved message
	 * @param remoteException The exception to contain within this exception
	 */
	public SystemRemoteMessageException(SystemMessage msg, Exception remoteException) {
		super(msg);
		wrappedException = remoteException;
	}

	/**
	 * Constructor with a plain text message, and a wrapped exception.
	 * 
	 * @param msg The fully resolved message
	 * @param remoteException The exception to contain within this exception
	 */
	public SystemRemoteMessageException(String msg, Exception remoteException) {
		super(msg);
		wrappedException = remoteException;
	}

	/**
	 * @return the original remote exception
	 */
	public Exception getRemoteException() {
		return wrappedException;
	}

}
