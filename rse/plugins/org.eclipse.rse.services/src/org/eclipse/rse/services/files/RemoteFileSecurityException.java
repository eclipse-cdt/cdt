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
 * Exception thrown when attempting an operation and it fails for security reasons.
 * The original remote system's security message is always embedded and retrievable
 * via getRemoteException().
 */
public class RemoteFileSecurityException extends RemoteFileException {

	/**
	 * A serialVersionUID is recommended for all serializable classes.
	 * This trait is inherited from Throwable.
	 * This should be updated if there is a schema change for this class.
	 */
	private static final long serialVersionUID = 1L;
	private static SystemMessage myMessage = null;

	/**
	 * Constructor 
	 * @param remoteException the original cause of this exception.
	 */
	public RemoteFileSecurityException(Exception remoteException) {
		super(getMyMessage(), remoteException);
		String secondLevel = remoteException.getMessage();
		if (secondLevel == null) {
			secondLevel = remoteException.getClass().getName();
		}
		getSystemMessage().makeSubstitution(secondLevel);
	}

	/*
	 * TODO dwd update this to retrieve the new messages when those are created
	 * super(RSEUIPlugin.getPluginMessage(ISystemMessages.FILEMSG_SECURITY_ERROR), remoteException);
	 * public static final String FILEMSG_SECURITY_ERROR = "RSEF1001";
	 * <Message ID="1001" Indicator="E">
	 * <LevelOne>Operation failed. Security violation</LevelOne>
	 * <LevelTwo>Message reported from file system: %1</LevelTwo>
	 * </Message>
	 */
	private static SystemMessage getMyMessage() {
		if (myMessage == null) {
			String l1 = "Operation failed. Security violation"; //$NON-NLS-1$
			String l2 = "Message reported from file system: %1"; //$NON-NLS-1$
			myMessage = new SystemMessage("RSE", "F", "1001", SystemMessage.ERROR, l1, l2); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		}
		return myMessage;
	}
}
