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
 *******************************************************************************/

package org.eclipse.rse.services.clientserver.messages;
/**
 * Encapsulates a system message
 */
public class SystemMessageException extends Exception 
{

	/**
	 * A serialVersionUID is recommended for all serializable classes.
	 * This trait is inherited from Throwable.
	 * This should be updated if there is a schema change for this class.
	 */
	private static final long serialVersionUID = 6695260563678942200L;
	private SystemMessage msg;

    /**
     * Constructor
     * @param msg - a message to wrap. 
     */
	protected SystemMessageException(String msg) 
	{
		super(msg);
	}
	
    /**
     * Constructor
     * @param msg - a system message to wrap. 
     */	
    public SystemMessageException(SystemMessage msg)
    {
    	super(msg.getLevelOneText());
        this.msg = msg;    	
    }
    
    /**
     * @return the SystemMessage wrapped by this SystemMessageException
     */
    public SystemMessage getSystemMessage()
    {
    	return msg;
    }
    
    
    /**
     * Set the SystemMessage being wrapped
     * @param msg the SystemMessage wrapped by this SystemMessageException
     */
    public void setSystemMessage(SystemMessage msg)
    {
    	this.msg = msg;
    }
    
}
