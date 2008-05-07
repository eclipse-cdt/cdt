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
 * David McKnight   (IBM)        - [216252] [api][nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 * David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 *******************************************************************************/

package org.eclipse.rse.services.files;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;

import org.eclipse.rse.internal.services.Activator;
import org.eclipse.rse.internal.services.RSEServicesMessages;
import org.eclipse.rse.services.clientserver.messages.SimpleSystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;

/**
 * Exception thrown when attempting an operation and it fails for IO reasons, such as
 * the file is read-only.
 * <p>
 * The original remote system's io message is always embedded and retrievable
 * via getRemoteException().
 */
public class RemoteFileIOException extends RemoteFileException {

	/**
	 * A serialVersionUID is recommended for all serializable classes.
	 * This trait is inherited from Throwable.
	 * This should be updated if there is a schema change for this class.
	 */
	private static final long serialVersionUID = 1L;
	private static SystemMessage myMessage = null;

	/**
	 * Constructor for RemoteFileIOException
	 * @param remoteException the initial cause of this exception
	 */
	public RemoteFileIOException(Exception remoteException) {
		super(getMyMessage(Activator.PLUGIN_ID, remoteException), remoteException);

	}

	/**
	 * Constructor for RemoteFileIOException that also takes a pluginId
	 *
	 * @param pluginId a plugin ID
	 * @param remoteException the initial cause of this exception
	 * @since 3.0
	 */
	public RemoteFileIOException(String pluginId, Exception remoteException) {
		super(getMyMessage(pluginId, remoteException), remoteException);

	}

	private static SystemMessage getMyMessage(String pluginId, Exception remoteException) {

		String secondLevel = remoteException.getMessage();
		if (secondLevel == null) {
			secondLevel = remoteException.getClass().getName();
		}

        String msgTxt = RSEServicesMessages.FILEMSG_OPERATION_FAILED;
        String msgDetails = NLS.bind(RSEServicesMessages.FILEMSG_OPERATION_FAILED_DETAILS, secondLevel);

        myMessage = new SimpleSystemMessage(pluginId,
        		"RSEF1002", //$NON-NLS-1$
        		IStatus.ERROR, msgTxt, msgDetails);

		return myMessage;
	}

}
