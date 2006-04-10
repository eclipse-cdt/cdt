/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.dstore.extra.internal.extra;



public class DataElementActionFilter 
{


    private static String                  _type = "type";
    private static DataElementActionFilter _instance;
    
    public static DataElementActionFilter getInstance() 
    {
	if (_instance == null)
	    _instance = new DataElementActionFilter();
	return _instance;
    }
    
    /**
     * @see IActionFilter#testAttribute(Object, String, String)
     */
    public boolean testAttribute(Object target, String name, String value) 
    {
	if (name.equals(_type)) 
	    {
		IDataElement le = (IDataElement)target;
		if (le.getType().equals(value) || le.isOfType(value))
		    {
			return true;
		    }
	    }       
	
	return false;
    }

    public static boolean matches(Class aClass)
    {
	return false;
    }

}