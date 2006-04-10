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

/**
 * This interface is implemented by any validator that 
 *  does uniqueness checking. Allows common code that will set the 
 *  list of string to check against.
 */
public interface ISystemValidatorUniqueString 
{

	/**
	 * Reset whether this is a case-sensitive list or not
	 */
	public void setCaseSensitive(boolean caseSensitive);
	/**
	 * Reset the existing names list. 
	 */
	public void setExistingNamesList(String[] existingList);
	/**
	 * Reset the existing names list. 
	 */
	public void setExistingNamesList(Vector existingList);	
	/**
	 * Return the existing names list. This will be a case-normalized and sorted list.
	 */
	public String[] getExistingNamesList();	
}