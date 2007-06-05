/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
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
 * {Name} (company) - description of contribution.
 *******************************************************************************/

package org.eclipse.rse.ui.validators;
import java.util.Collection;

import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;


/**
 * This class is used in dialogs that prompt for filter name. Filter names
 * have to be unique.
 * <p>
 * The IInputValidator interface is implemented by our parent and it
 * is used by jface's InputDialog class and property sheet window.
 */
public class ValidatorFilterName 
       extends ValidatorUniqueString implements ISystemValidator
{
	public static final int MAX_FILTERNAME_LENGTH = 100;
		
	//public static final boolean CASE_SENSITIVE = true;
	//public static final boolean CASE_INSENSITIVE = false;
	
	/**
	 * Constructor accepting a Collection. 
	 * @param existingList a collection of existing filter names to compare against.
	 * The collection will not be modified by the validator.
	 * Note that toString() is used to get the string from each item.
	 */
	public ValidatorFilterName(Collection existingList)
	{
		super(existingList, CASE_SENSITIVE);
		init();
	}
	/**
	 * Constructor accepting an Array. 
	 * @param existingList array containing list of existing strings to compare against.
	 */
	public ValidatorFilterName(String[] existingList)
	{
		super(existingList, CASE_SENSITIVE);
		init();
	}

    private void init()
    {		
		super.setErrorMessages(
				RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_VALIDATE_FILTERNAME_EMPTY),
				RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_VALIDATE_FILTERNAME_NOTUNIQUE)
				);  
    }

	public String toString()
	{
		return "FilterNameValidator class"; //$NON-NLS-1$
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
