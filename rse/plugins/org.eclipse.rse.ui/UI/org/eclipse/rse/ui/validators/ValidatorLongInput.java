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

package org.eclipse.rse.ui.validators;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemMessages;



/**
 * For editable large numeric properties.
 * Ensures only digits are entered.
 */
public class ValidatorLongInput implements ISystemValidator 
{
	protected boolean allowBlank = false;		
    protected long number; 
    protected SystemMessage emptyMsg, invalidMsg, currentMessage;
    
    /**
     * Constructor to use when the default error messages are ok
     * @see #setBlankAllowed(boolean)
     */
    public ValidatorLongInput()
    {
    	this(SystemPlugin.getPluginMessage(ISystemMessages.MSG_VALIDATE_ENTRY_EMPTY));
    }
    /**
     * Constructor to use when wanting to specify the "value required" error message,
     * but use the default for the "Value not valid" error message
     * @see #setBlankAllowed(boolean)
     */
    public ValidatorLongInput(SystemMessage emptyMsg)
    {
    	this(emptyMsg, SystemPlugin.getPluginMessage(ISystemMessages.MSG_VALIDATE_NOT_NUMERIC));
    }
    /**
     * Constructor to use when wanting to specify both error messages
     * @see #setBlankAllowed(boolean)
     */
    public ValidatorLongInput(SystemMessage emptyMsg, SystemMessage invalidMsg)
    {
		setErrorMessages(emptyMsg, invalidMsg);
    }

    /**
     * Specify if an empty field is ok or not. The default is not, and will result in an error message.
     */
    public void setBlankAllowed(boolean allowBlank)
    {
    	this.allowBlank = allowBlank;
    }

	/**
	 * Set the error messages, overriding the defaults
	 */
	public void setErrorMessages(SystemMessage emptyMsg, SystemMessage invalidMsg)
	{
    	this.emptyMsg = emptyMsg;
    	this.invalidMsg = invalidMsg;
	}
	
    // ---------------------------
    // ISystemValidator methods...
    // ---------------------------    
         
	/**
	 * @see org.eclipse.jface.viewers.ICellEditorValidator#isValid(java.lang.Object)
	 */
	public String isValid(Object input) 
	{
		currentMessage = null;
		if (!(input instanceof String))
		{
		  //return "Unknown input";
		  number = 1;
		  return null;
		}
		else
		  return isValid((String)input);
	}
	/**
	 * @see org.eclipse.jface.dialogs.IInputValidator#isValid(java.lang.String)
	 * @see #getSystemMessage()
	 */
	public String isValid(String input) 
	{
		currentMessage = null;
		if ((input==null)||(input.length()==0))
		{
			if (!allowBlank)
		      currentMessage = emptyMsg;
		}
		else
		{
		  try 
		  {
		   number = Long.parseLong(input);
		  }
		  catch (NumberFormatException exc)
		  {
		  	currentMessage = invalidMsg;
		    currentMessage.makeSubstitution(input);
		  }
		}
		return (currentMessage==null) ? null : currentMessage.getLevelOneText();
	}

    /**
     * When isValid returns non-null, call this to get the SystemMessage object for the error 
     *  versus the simple string message.
     */
    public SystemMessage getSystemMessage()
    {
    	return currentMessage;
    }
    
    /**
     * Return the max length for this name. For us, we return 20.
     */
    public int getMaximumNameLength()
    {
    	return 20;
    }

    /**
     * For convenience, this is a shortcut to calling:
     * <pre><code>
     *  if (isValid(text) != null)
     *    msg = getSystemMessage();
     * </code></pre>
     */
    public SystemMessage validate(String text)
    {
    	if (isValid(text) != null)
    	  return currentMessage;
    	else
    	  return null;
    }	

    /**
     * If validation is true, you can call this to get the input as a number
     */
    public long getNumber()
    {
    	 return number;
    }        
}