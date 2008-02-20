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
 *******************************************************************************/

package org.eclipse.rse.services.files;

import org.eclipse.rse.services.clientserver.messages.SystemMessage;

/**
 * Exception thrown when attempting a file operation and the user
 * canceled it before it could be completed.
 */
public class RemoteFileCancelledException extends RemoteFileException {

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
	public RemoteFileCancelledException() {
		super(getMyMessage());
	}

	/*
	 * TODO dwd update this to retrieve the new messages when those are created
	 * myMessage = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_OPERATION_CANCELLED);
	 * public static final String MSG_OPERATION_CANCELLED = "RSEG1067";		
	 * <Message ID="1067" Indicator="E">
	 *   <LevelOne>Operation cancelled.</LevelOne>		
	 *   <LevelTwo></LevelTwo>
	 * </Message>
	 */
	private static SystemMessage getMyMessage() {
		String l1 = "Operation cancelled."; //$NON-NLS-1$
		String l2 = ""; //$NON-NLS-1$
		myMessage = new SystemMessage("RSE", "G", "1067", SystemMessage.ERROR, l1, l2); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		return myMessage;
	}

}
