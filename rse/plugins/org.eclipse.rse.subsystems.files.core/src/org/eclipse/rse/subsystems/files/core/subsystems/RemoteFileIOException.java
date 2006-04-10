/********************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.subsystems.files.core.subsystems;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.ui.ISystemMessages;


/**
 * Exception thrown when attempting an operation and it fails for IO reasons, such as
 * the file is read-only.
 * <p>
 * The original remote system's io message is always embedded and retrievable
 * via getRemoteException().
 */
public class RemoteFileIOException extends RemoteFileException
{
	/**
	 * Constructor 
	 */
	public RemoteFileIOException(Exception remoteException)
	{
		super(SystemPlugin.getPluginMessage(ISystemMessages.FILEMSG_IO_ERROR), remoteException);
		String secondLevel = remoteException.getMessage();
		if (secondLevel == null)
		  secondLevel = remoteException.getClass().getName();
		getSystemMessage().makeSubstitution(secondLevel);
	}
}