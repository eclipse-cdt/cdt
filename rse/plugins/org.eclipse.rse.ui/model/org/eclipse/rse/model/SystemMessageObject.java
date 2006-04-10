/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.model;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;

/**
 * This class captures a message we wish to display as child node in the tree view.
 */
public class SystemMessageObject implements ISystemMessageObject, IAdaptable, Comparable
{
	
	SystemMessage systemMessage;
    protected String message;
    protected int    type;
    protected Object parent;
    
    /**
     * Constructor when using SystemMessage
     * @param msgObj The system message from which to retrieve text to show in the tree viewer
     * @param type The message severity, dictating the icon. 
     * @param parent The parent node of this within the tree view
     * @see org.eclipse.rse.model.ISystemMessageObject
     */
    public SystemMessageObject(SystemMessage msgObj, int type, Object parent)
    {
    	this.systemMessage = msgObj;
    	this.message = msgObj.getLevelOneText();
    	this.type = type;
    	this.parent = parent;
    }
    
    /**
     * Get the message to display in the tree viewer
     */
    public String getMessage()
    {
    	return message;
    }
    
    /**
     * Message type. 
     * @see org.eclipse.rse.model.ISystemMessageObject
     */
    public int getType()
    {
    	return type;
    }

    /**
     * Get the parent object (within tree view)
     */
    public Object getParent()
    {
    	return parent;
    }
    
    /**
	 * This is the method required by the IAdaptable interface.
	 * Given an adapter class type, return an object castable to the type, or
	 *  null if this is not possible.
	 */
    public Object getAdapter(Class adapterType)
    {
   	    return Platform.getAdapterManager().getAdapter(this, adapterType);	
    }               
	/**
	 * @see ISystemMessageObject#isTransient()
	 */
	public boolean isTransient() {
		return true;
	}

	/**
	 * Return the SystemMessage for this SystemMessageObject.
	 */
	public SystemMessage getSystemMessage()
	{
		return systemMessage;
	}
	
	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		ISystemMessageObject other = (ISystemMessageObject)o;
		return getMessage().compareTo(other.getMessage());
	}
}