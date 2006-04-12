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

import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;


/**
 * This class is used in dialogs that prompt for filter name. Filter names
 * have to be unique, and to enable saving per file, must be a valid file name.
 * <p>
 * The IInputValidator interface is implemented by our parent and it
 * is used by jface's InputDialog class and property sheet window.
 */
public class ValidatorFilterName 
       extends ValidatorFileName implements ISystemMessages, ISystemValidator
{
	public static final int MAX_FILTERNAME_LENGTH = 100;
		
	//public static final boolean CASE_SENSITIVE = true;
	//public static final boolean CASE_INSENSITIVE = false;
	
	/**
	 * Constructor accepting a Vector. 
	 * @param A vector containing list of existing filter names to compare against.
	 *        Note that toString() is used to get the string from each item.
	 * @param true if comparisons are to be case sensitive, false if case insensitive.	 
	 */
	public ValidatorFilterName(Vector existingList)
	{
		super(existingList);
		init();
	}
	/**
	 * Constructor accepting an Array. 
	 * @param An array containing list of existing strings to compare against.
	 * @param true if comparisons are to be case sensitive, false if case insensitive.
	 */
	public ValidatorFilterName(String[] existingList)
	{
		super(existingList);
		init();
	}

    private void init()
    {		
		super.setErrorMessages(RSEUIPlugin.getPluginMessage(MSG_VALIDATE_FILTERNAME_EMPTY),
		                       RSEUIPlugin.getPluginMessage(MSG_VALIDATE_FILTERNAME_NOTUNIQUE),  
		                       RSEUIPlugin.getPluginMessage(MSG_VALIDATE_FILTERNAME_NOTVALID));  
    }

	public String toString()
	{
		return "FilterNameValidator class";
	}
 
    // ---------------------------
    // Parent Overrides...
    // ---------------------------
 
	/**
	 * Overridable extension point to check for invalidate characters beyond what Eclipse checks for
	 * @return true if valid, false if not
	 */
	protected boolean checkForBadCharacters(String newText)
	{
		if (newText.indexOf('#') >= 0)
		  return false;
		else
		  return true;
	}

    // ---------------------------
    // ISystemValidator methods...
    // ---------------------------    

    /**
     * Return the max length for filters: 100
     */
    public int getMaximumNameLength()
    {
    	return MAX_FILTERNAME_LENGTH;
    }
    
}