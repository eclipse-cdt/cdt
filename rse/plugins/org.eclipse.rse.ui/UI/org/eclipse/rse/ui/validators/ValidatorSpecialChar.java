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
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;


/**
 * This class is used in dialogs that prompt for string, where the
 * string is not allowed to content special characters, as supplied to this class.
 *
 * The IInputValidator interface is used by jface's
 * InputDialog class and numerous other platform and system classes.
 */
public class ValidatorSpecialChar 
	   implements ISystemMessages, ISystemValidator // IInputValidator, ICellEditorValidator
{
	
	public static final boolean EMPTY_ALLOWED_NO = false;
	public static final boolean EMPTY_ALLOWED_YES= true;
	private   boolean isEmptyAllowed = true;
	protected StringBuffer specialChars;
	protected SystemMessage   msg_Invalid;
	protected SystemMessage   msg_Empty;	
	protected SystemMessage   currentMessage;
	private int nbrSpecialChars;
	
	/**
	 * Constructor
	 * @param specialChars String containing special characters to test for.
	 * @param isEmptyAllowed true if an empty string is valid
	 */
	public ValidatorSpecialChar(String specialChars, boolean isEmptyAllowed)
	{
		this(specialChars, isEmptyAllowed, RSEUIPlugin.getPluginMessage(MSG_VALIDATE_ENTRY_NOTVALID), RSEUIPlugin.getPluginMessage(MSG_VALIDATE_ENTRY_EMPTY));
	}
	/**
	 * Constructor
	 * @param specialChars String containing special characters to test for.
	 * @param isEmptyAllowed true if an empty string is valid
	 * @param error message when invalid characters entered
	 */
	public ValidatorSpecialChar(String specialChars, boolean isEmptyAllowed, SystemMessage msg_Invalid)
	{
		this(specialChars, isEmptyAllowed, msg_Invalid, RSEUIPlugin.getPluginMessage(MSG_VALIDATE_ENTRY_EMPTY));
	}	
	/**
	 * Constructor
	 * @param specialChars String containing special characters to test for.
	 * @param isEmptyAllowed true if an empty string is valid
	 * @param error message when invalid characters entered
	 * @param error message when empty string
	 */
	public ValidatorSpecialChar(String specialChars, boolean isEmptyAllowed, SystemMessage msg_Invalid, SystemMessage msg_Empty)
	{
		this.isEmptyAllowed = isEmptyAllowed;
		this.specialChars = new StringBuffer(specialChars);
	    this.nbrSpecialChars = specialChars.length();	
	    setErrorMessages(msg_Empty, msg_Invalid);
	}		
	/**
	 * Supply your own error message text. By default, messages from RSEUIPlugin resource bundle are used.
	 * @param error message when entry field is empty or null if to keep the default
	 * @param error message when value entered is not valid, or null if to keep the default
	 */
	public void setErrorMessages(SystemMessage msg_Empty, SystemMessage msg_Invalid)
	{
		if (msg_Empty != null)
  		  this.msg_Empty     = msg_Empty;
  	    if (msg_Invalid != null)
		  this.msg_Invalid = msg_Invalid;		
	}

    // --------------------------
    // Internal helper methods...
    // --------------------------

    /**
     * Helper method to substitute data into a message
     */
	protected String doMessageSubstitution(SystemMessage msg, String substitution)
	{
        currentMessage = msg;
		if (msg.getNumSubstitutionVariables() > 0)
		  return msg.makeSubstitution(substitution).getLevelOneText();
		else
		  return msg.getLevelOneText();
	}
	
	/**
	 * Helper method to set the current system message and return its level one text
	 */
	protected String getSystemMessageText(SystemMessage msg)
	{
		currentMessage = msg;
		if (msg != null)
		  return msg.getLevelOneText();
		else
		  return null;
	}

    // ---------------------------
    // ISystemValidator methods...
    // ---------------------------    

	/**
	 * Validate each character. 
	 */
	public String isValid(String newText)
	{
		currentMessage = null;
	    if ((newText==null) || (newText.length() == 0))
	    {
          if (isEmptyAllowed)
            return null;
          else
            currentMessage = msg_Empty;	   	  
	    }
	    else if (containsSpecialCharacters(newText))
	   	  currentMessage = msg_Invalid; 
	    else
	      currentMessage = isSyntaxOk(newText);
        return (currentMessage == null) ? null : doMessageSubstitution(currentMessage, newText);
	}
	
	/**
	 * As required by ICellEditor
	 */
	public String isValid(Object newValue)
	{
		if (newValue instanceof String)
		  return isValid((String)newValue);
		else
		  return null;
	}	
	
 
    protected boolean containsSpecialCharacters(String newText)
    {
    	boolean contains = false;
    	int newLen = newText.length();
    	for (int idx=0; !contains && (idx<newLen); idx++)
    	{
    		for (int jdx=0; !contains && (jdx<nbrSpecialChars); jdx++)
    		   if (newText.charAt(idx) == specialChars.charAt(jdx))
    		     contains = true;
    	}
    	return contains;
    }	
    
	/**
	 * Override in child to do your own syntax checking.
	 */
	public SystemMessage isSyntaxOk(String newText)
	{
		return null;
	}    

    /**
     * Return the max length for this name, or -1 if no max
     */
    public int getMaximumNameLength()
    {
    	return -1;
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
	
}