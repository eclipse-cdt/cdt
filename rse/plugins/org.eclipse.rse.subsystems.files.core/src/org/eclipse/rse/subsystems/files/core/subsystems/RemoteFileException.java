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
import java.util.ResourceBundle;

import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;


/**
 * Base class for remote file system exceptions.
 * <p>
 * All remote file exceptions are guaranteed to have a translated message retrievable
 * via getMessage(), to make it easy to display to the user.
 * <p>
 * All child exceptions potentially contain an imbedded exception that
 *  is the original exception from the remote system. 
 * <p>
 * Use {#link getRemoteException()} to retrieve that wrapped exception, if any.
 */
public class RemoteFileException extends SystemMessageException
{
	private Exception wrappedException = null;
	
	/**
	 * Constructor for RemoteFileException with an error message for getMessage() to return.
	 * @param bundle The ResourceBundle containing the error message
	 * @param key The key to retrieve the message 
	 */
	public RemoteFileException(ResourceBundle bundle, String key)
	{
		this(getString(bundle,key), null);
	}
	/**
	 * Constructor for RemoteFileException with an error message for getMessage() to return.
	 * @param msg The fully resolved message 
	 */
	public RemoteFileException(String msg)
	{
		this(msg, null);
	}	
	/**
	 * Constructor for RemoteFileException with an error message for getMessage() to return,
	 *  and a wrapped exception to contain. It is accessed via getRemoteException().
	 * @param bundle The ResourceBundle containing the error message
	 * @param key The key to retrieve the message 
	 * @param remoteException The exception to contain within this exception
	 */
	public RemoteFileException(ResourceBundle bundle, String key, Exception remoteException)
	{
		this(getString(bundle,key), remoteException);
	}
	/**
	 * Constructor for RemoteFileException with an error message for getMessage() to return.
	 *  and a wrapped exception to contain. It is accessed via getRemoteException().
	 * @param msg The fully resolved message 
	 * @param remoteException The exception to contain within this exception
	 */
	public RemoteFileException(String msg, Exception remoteException)
	{
		super(msg);
		wrappedException = remoteException;
	}	

	/**
	 * Constructor for RemoteFileException with an error message for getMessage() to return.
	 * @param msg The fully resolved message 
	 */
	public RemoteFileException(SystemMessage msg)
	{
		this(msg, null);
	}	
	/**
	 * Constructor for RemoteFileException with an error message for getMessage() to return.
	 *  and a wrapped exception to contain. It is accessed via getRemoteException().
	 * @param msg The fully resolved message 
	 * @param remoteException The exception to contain within this exception
	 */
	public RemoteFileException(SystemMessage msg, Exception remoteException)
	{
		super(msg);
		wrappedException = remoteException;
	}	
    /**
     * Return the wrapped exception
     */
    public Exception getRemoteException()
    {
    	return wrappedException;
    }
    
	
	private static String getString(ResourceBundle bundle, String key)
	{
		String msg = null;
		try { msg = bundle.getString(key); } catch (Exception exc) {}
		if (msg == null)
		  msg = "Message with key " + key + " not found";		
		return msg;		
	}

}