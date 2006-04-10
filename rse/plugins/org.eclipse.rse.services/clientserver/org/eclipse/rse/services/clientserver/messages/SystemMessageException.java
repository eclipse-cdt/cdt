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

package org.eclipse.rse.services.clientserver.messages;
/**
 * Encapsulates a system message
 */
public class SystemMessageException extends Exception 
{
    /**
	 * 
	 */
	private static final long	serialVersionUID	= 6695260563678942200L;
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
     * Return the SystemMessage we wrap
     */
    public SystemMessage getSystemMessage()
    {
    	return msg;
    }
    
    /**
     * Tests if the SystemMessage encapsulated by this exception has a long id equal to the
     * argument.
     * @param longMessageID the String containing the message id to test.
     * @return true if the long message id of the message is equal to the supplied message id.
     */
    public boolean hasLongID(String longMessageID) {
    	return getSystemMessage().hasLongID(longMessageID);
    }
    
    /**
     * Set the SystemMessage being wrapped
     */
    public void setSystemMessage(SystemMessage msg)
    {
    	this.msg = msg;
    }
    
}