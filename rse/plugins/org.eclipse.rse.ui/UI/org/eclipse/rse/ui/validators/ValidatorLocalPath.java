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
import java.io.File;
import java.util.Vector;

import org.eclipse.rse.services.clientserver.messages.SystemMessage;


/**
 * This class is used in dialogs that prompt for a local directory path.
 *
 * The IInputValidator interface is used by jface's
 * InputDialog class and numerous other platform and system classes.
 */
public class ValidatorLocalPath extends ValidatorPathName 
{


    public static final boolean WINDOWS = System.getProperty("os.name").toLowerCase().indexOf("windows") >= 0;
    public static final char SEPCHAR = File.separatorChar;
    
	/**
	 * Constructor for ValidatorLocalPath
	 */
	public ValidatorLocalPath(Vector existingNameList) 
	{
		super(existingNameList);
	}

	/**
	 * Constructor for ValidatorLocalPath
	 */
	public ValidatorLocalPath(String[] existingNameList) 
	{
		super(existingNameList);
	}

	/**
	 * Constructor for ValidatorLocalPath
	 */
	public ValidatorLocalPath() 
	{
		super();
	}

	/**
	 * Validate each character. 
	 * Override of parent method.
	 */
	public SystemMessage isSyntaxOk(String newText)
	{
		SystemMessage msg = super.isSyntaxOk(newText);
		if (msg == null)
		{
		  boolean ok = true;
          if (WINDOWS)	
          {
          	if (newText.length()<3)
          	  ok = false;
          	else if (newText.charAt(1) != ':')
          	  ok = false;
          	else if (newText.charAt(2) != SEPCHAR)
          	  ok = false;
          	else if (!Character.isLetter(newText.charAt(0)))
          	  ok = false;          	 
          }
          else
          {
          	if (newText.length()<1)
          	  ok = false;
          	else if (newText.charAt(0) != SEPCHAR)
          	  ok = false;
          }
          if (!ok)
            msg = msg_Invalid;	
		}
	    return msg;
	}

}