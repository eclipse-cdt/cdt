/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
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
 * David McKnight   (IBM)        - [216252] [api][nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 *******************************************************************************/

package org.eclipse.rse.services.files;

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

	/*
	 * TODO dwd update this to retrieve the new messages when those are created
	 * super(RSEUIPlugin.getPluginMessage(ISystemMessages.FILEMSG_FOLDER_NOTEMPTY), remoteException);
	 * public static final String FILEMSG_FOLDER_NOTEMPTY = "RSEF1003";
	 * <Message ID="1003" Indicator="E">
	 *   <LevelOne>Folder is not empty. Cannot delete</LevelOne>
	 *   <LevelTwo>The operation failed. One possible reason is that the folder is not empty</LevelTwo>
	 * </Message>
	 */
	private static SystemMessage getMyMessage() {
		if (myMessage == null) {
			String l1 = "Folder is not empty. Cannot delete"; //$NON-NLS-1$
			String l2 = "The operation failed. One possible reason is that the folder is not empty"; //$NON-NLS-1$
			myMessage = new SystemMessage("RSE", "F", "1003", SystemMessage.ERROR, l1, l2); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		}
		return myMessage;
	}

}
