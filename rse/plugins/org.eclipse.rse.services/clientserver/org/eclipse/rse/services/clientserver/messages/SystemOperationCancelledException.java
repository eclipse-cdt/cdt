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
 * David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 * Martin Oberhuber (Wind River) - [226374] [api] Derived from RemoteFileCancelledException
 *******************************************************************************/

package org.eclipse.rse.services.clientserver.messages;

import org.eclipse.core.runtime.IStatus;

import org.eclipse.rse.services.clientserver.IClientServerConstants;

/**
 * Exception thrown when attempting an operation and the user cancelled it
 * before it could be completed.
 *
 * @since 3.0
 */
public class SystemOperationCancelledException extends SystemMessageException {

	/**
	 * A serialVersionUID is recommended for all serializable classes. This
	 * trait is inherited from Throwable. This should be updated if there is a
	 * schema change for this class.
	 */
	private static final long serialVersionUID = 1L;
	private static SystemMessage myMessage = null;

	/**
	 * Constructor.
	 */
	public SystemOperationCancelledException() {
		super(getMyMessage());
	}

	private static SystemMessage getMyMessage() {
		if (myMessage == null) {
			myMessage = new SimpleSystemMessage(IClientServerConstants.PLUGIN_ID, ICommonMessageIds.MSG_OPERATION_CANCELLED, IStatus.ERROR,
					CommonMessages.MSG_OPERATION_CANCELLED);
		}
		return myMessage;
	}

}
