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
import java.util.Vector;

import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.filters.ISystemFilterString;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemMessages;



/**
 * This class is used in dialogs that prompt for filter strings.
 * This class typically needs to be overridden for a particular subsystem factory provider.
 * By default, it simply checks for uniqueness.
 */
public class ValidatorFilterString 
       extends ValidatorUniqueString implements ISystemMessages, ISystemValidator
{
	public static final int MAX_FILTERSTRINGNAME_LENGTH = 1000;
		
	//public static final boolean CASE_SENSITIVE = true;
	//public static final boolean CASE_INSENSITIVE = false;
	protected SystemMessage   msg_Invalid;
	
	/**
	 * Constructor accepting a Vector for the list of existing strings, as simple strings.
	 * @param existingList A vector of strings to compare against.
	 * @param caseSensitive true if comparisons are to be case sensitive, false if case insensitive.
	 */
	public ValidatorFilterString(Vector existingList, boolean caseSensitive)
	{
		super(existingList, caseSensitive); // case sensitive uniqueness		
		init();
	}
	/**
	 * Constructor accepting an Array for the list of existing strings, as simple strings.
	 * @param existingList An array containing list of existing strings to compare against.
	 * @param caseSensitive true if comparisons are to be case sensitive, false if case insensitive.
	 */
	public ValidatorFilterString(String[] existingList, boolean caseSensitive)
	{
		super(existingList, caseSensitive); // case sensitive uniqueness		
		init();
	}
	/**
	 * Constructor accepting an Array for the list of existing strings, as actual filter strings.
	 * @param existingList An array containing list of existing filter strings to compare against.
	 * @param caseSensitive true if comparisons are to be case sensitive, false if case insensitive.
	 */
	public ValidatorFilterString(ISystemFilterString[] existingList, boolean caseSensitive)
	{
		super(convertFilterStringsToStrings(existingList), caseSensitive); // case sensitive uniqueness		
		init();
	}

	/**
	 * Use this constructor when the name need not be unique, and you just want
	 * the syntax checking.
	 */
	public ValidatorFilterString(boolean caseSensitive)
	{
		super(new String[0], caseSensitive);
		init();
	}	
	
	/**
	 * Set the error message to issue when a duplicate filter string is found. 
	 */
	public void setDuplicateFilterStringErrorMessage(SystemMessage msg)
	{
		super.setErrorMessages(null, msg_NonUnique);		
	}
	
	/**
	 * Converts an array of filter strings into an array of strings
	 */
	protected static String[] convertFilterStringsToStrings(ISystemFilterString[] filterStrings)
	{
		if (filterStrings == null)
		  return new String[0];
		String[] strings = new String[filterStrings.length];
		for (int idx=0; idx<filterStrings.length; idx++)
		   strings[idx] = filterStrings[idx].getString();
		return strings;
	}


    private void init()
    {
		setErrorMessages(SystemPlugin.getPluginMessage(MSG_VALIDATE_FILTERSTRING_EMPTY),
		                 SystemPlugin.getPluginMessage(MSG_VALIDATE_FILTERSTRING_NOTUNIQUE),  
		                 SystemPlugin.getPluginMessage(MSG_VALIDATE_FILTERSTRING_NOTVALID));  
    }
    
	/**
	 * Supply your own error message text. By default, messages from SystemPlugin resource bundle are used.
	 * @param error message when entry field is empty
	 * @param error message when value entered is not unique
	 * @param error message when syntax is not valid
	 */
	public void setErrorMessages(SystemMessage msg_Empty, SystemMessage msg_NonUnique, SystemMessage msg_Invalid)
	{
		super.setErrorMessages(msg_Empty, msg_NonUnique);
		this.msg_Invalid = msg_Invalid;		
	}

	public String toString()
	{
		return "ValidatorFilterString class";
	}

    // ---------------------------
    // Parent Overrides...
    // ---------------------------
 
	/**
	 * Validate each character. 
	 * Override of parent method.
	 * Override yourself to refine the error checking. We don't do anything by default.	 
	 */
	public SystemMessage isSyntaxOk(String newText)
	{
		// nothing more to check so far. But if there ever is, return msg_Invalid
	    return null;
	}
    
    // ---------------------------
    // ISystemValidator methods...
    // ---------------------------    
     
    /**
     * Return the max length for filter strings: 1000
     */
    public int getMaximumNameLength()
    {
    	return MAX_FILTERSTRINGNAME_LENGTH;
    }
    
}