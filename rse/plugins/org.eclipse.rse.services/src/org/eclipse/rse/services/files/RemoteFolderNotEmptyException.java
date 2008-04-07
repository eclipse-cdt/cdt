/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * David McKnight   (IBM)        - [216252] [api][nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 * David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 *******************************************************************************/

package org.eclipse.rse.services.files;

import org.eclipse.core.runtime.IStatus;

import org.eclipse.rse.internal.services.Activator;
import org.eclipse.rse.internal.services.RSEServicesMessages;
import org.eclipse.rse.services.clientserver.messages.SimpleSystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;

/**
 * Exception thrown when attempting to delete a folder, and the folder is not empty.
 */
public class RemoteFolderNotEmptyException extends RemoteFileException {

	/**
	 * A serialVersionUID is recommended for all serializable classes.
	 * This trait is inherited from Throwable.
	 * This should be updated if there is a schema change for this class.
	 */
	private static final long serialVersionUID = 1L;
	private static SystemMessage myMessage = null;

	/**
	 * Constructor for RemoteFolderNotEmptyException when there is no remote exception to wrap
	 */
	public RemoteFolderNotEmptyException() {
		this(null);
	}

	/**
	 * Constructor for RemoteFolderNotEmptyException when there is a remote exception to wrap
	 * @param remoteException the exception that caused this one to be constructed.
	 */
	public RemoteFolderNotEmptyException(Exception remoteException) {
		super(getMyMessage(), remoteException);
	}


	private static SystemMessage getMyMessage() {
		if (myMessage == null) {
			String msgTxt = RSEServicesMessages.FILEMSG_FOLDER_NOT_EMPTY;
			String msgDetails = RSEServicesMessages.FILEMSG_FOLDER_NOT_EMPTY_DETAILS;
			myMessage = new SimpleSystemMessage(Activator.PLUGIN_ID, 
					"RSEF1003", //$NON-NLS-1$
					IStatus.ERROR, msgTxt, msgDetails);
		}
		return myMessage;
	}

}
