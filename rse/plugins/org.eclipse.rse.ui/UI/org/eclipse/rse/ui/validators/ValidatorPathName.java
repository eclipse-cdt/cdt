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
import org.eclipse.rse.services.clientserver.messages.SystemMessage;


/**
 * This class is used in dialogs that prompt for a name that eventually needs to become a folder path.
 * Simply checks for a few obviously bad characters.
 *
 * The IInputValidator interface is used by jface's
 * InputDialog class and numerous other platform and system classes.
 */
public class ValidatorPathName 
	   extends ValidatorUniqueString
{

	protected boolean  fUnique;
	protected SystemMessage   msg_Invalid;
	protected StringBuffer specialChars;
	private int nbrSpecialChars;
		
	/**
	 * Use this constructor when the name must be unique. Give the
	 * ctor a vector containing a list of existing names to compare against.
	 */
	public ValidatorPathName(Vector existingNameList)
	{
		super(existingNameList, CASE_INSENSITIVE); // case insensitive uniqueness
        init();
	}
	/**
	 * Use this constructor when the name must be unique. Give the
	 * ctor a string array of existing names to compare against.
	 */
	public ValidatorPathName(String existingNameList[])
	{
		super(existingNameList, CASE_INSENSITIVE); // case sensitive uniqueness
        init();
	}
	
	/**
	 * Use this constructor when the name need not be unique, and you just want
	 * the syntax checking.
	 */
	public ValidatorPathName()
	{
		super(new String[0], CASE_INSENSITIVE);
		init();
		fUnique = false;
	}	
	
	protected void init()
	{
		super.setErrorMessages(SystemPlugin.getPluginMessage(MSG_VALIDATE_PATH_EMPTY),
		                       SystemPlugin.getPluginMessage(MSG_VALIDATE_PATH_NOTUNIQUE));  		
		fUnique = true;		
		msg_Invalid = SystemPlugin.getPluginMessage(MSG_VALIDATE_PATH_NOTVALID);				
		specialChars = new StringBuffer("*?;'<>|");
	    nbrSpecialChars = specialChars.length();
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

	/**
	 * Validate each character. 
	 * Override of parent method.
	 * Override yourself to refine the error checking.	 
	 */
	public SystemMessage isSyntaxOk(String newText)
	{
	   //IStatus rc = workspace.validatePath(newText, IResource.FOLDER);
	   //if (rc.getCode() != IStatus.OK)
	     //return msg_Invalid;
	   boolean ok = !containsSpecialCharacters(newText);
	   if (!ok) 
	     return msg_Invalid;
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
	
	public String toString()
	{
		return "PathValidator class";
	}
}