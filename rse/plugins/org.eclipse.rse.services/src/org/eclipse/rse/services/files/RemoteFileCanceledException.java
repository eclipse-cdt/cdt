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
 * Martin Oberhuber (Wind River) - [216351] Improve cancellation of SystemFetchOperation for files
 * David McKnight   (IBM)        - [216252] [api][nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 * David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 *******************************************************************************/

package org.eclipse.rse.services.files;

import org.eclipse.core.runtime.IStatus;

import org.eclipse.rse.internal.services.Activator;
import org.eclipse.rse.services.clientserver.messages.CommonMessages;
import org.eclipse.rse.services.clientserver.messages.ICommonMessageIds;
import org.eclipse.rse.services.clientserver.messages.SimpleSystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;

/**
 * Exception thrown when attempting a file operation and the user canceled it
 * before it could be completed.
 *
 * @since org.eclipse.rse.services 3.0 renamed from RemoteFileCancelledException
 */
public class RemoteFileCanceledException extends RemoteFileException {

	/**
	 * A serialVersionUID is recommended for all serializable classes.
	 * This trait is inherited from Throwable.
	 * This should be updated if there is a schema change for this class.
	 */
	private static final long serialVersionUID = 1L;
	private static SystemMessage myMessage = null;

	/**
	 * Constructor
	 */
	public RemoteFileCanceledException() {
		super(getMyMessage());
	}

	private static SystemMessage getMyMessage() {
		String msgTxt = CommonMessages.MSG_OPERATION_CANCELLED;

		myMessage = new SimpleSystemMessage(Activator.PLUGIN_ID,
				ICommonMessageIds.MSG_OPERATION_CANCELLED,
				IStatus.ERROR, msgTxt);

		return myMessage;
	}

}
