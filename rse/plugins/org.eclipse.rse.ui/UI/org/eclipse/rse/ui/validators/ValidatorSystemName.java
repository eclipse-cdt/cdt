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

import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.RSEUIPlugin;


/**
 * This class is used in dialogs that prompt for an alias name.
 * The rules used are the same as for Java names, for simplicity.
 * Depending on the constructor used, this will also check for duplicates.
 *
 * The IInputValidator interface is used by jface's
 * InputDialog class and numerous other platform and system classes.
 */
public class ValidatorSystemName 
	   extends ValidatorUniqueString
{
	
	//protected String[] existingNames;
	protected boolean  fUnique;
	//protected String   msg_Empty;
	//protected String   msg_NonUnique;
	protected SystemMessage   msg_Invalid;
	
	/**
	 * Use this constructor when the name must be unique. Give the
	 * ctor a vector containing a list of existing names to compare against.
	 */
	public ValidatorSystemName(Vector existingNameList)
	{
		super(existingNameList, true); // case sensitive uniqueness
		super.setErrorMessages(RSEUIPlugin.getPluginMessage(MSG_VALIDATE_NAME_EMPTY),
		                       RSEUIPlugin.getPluginMessage(MSG_VALIDATE_NAME_NOTUNIQUE));  
		fUnique = true;
		msg_Invalid = RSEUIPlugin.getPluginMessage(MSG_VALIDATE_NAME_NOTVALID);				
	}
	/**
	 * Use this constructor when the name must be unique. Give the
	 * ctor a string array of existing names to compare against.
	 */
	public ValidatorSystemName(String existingNameList[])
	{
		super(existingNameList, true); // case sensitive uniqueness
		super.setErrorMessages(RSEUIPlugin.getPluginMessage(MSG_VALIDATE_NAME_EMPTY),
		                       RSEUIPlugin.getPluginMessage(MSG_VALIDATE_NAME_NOTUNIQUE));  
		fUnique = true;
		msg_Invalid = RSEUIPlugin.getPluginMessage(MSG_VALIDATE_NAME_NOTVALID);				
	}
	
	/**
	 * Use this constructor when the name need not be unique, and you just want
	 * the syntax checking.
	 */
	public ValidatorSystemName()
	{
		super(new String[0], true);
		super.setErrorMessages(RSEUIPlugin.getPluginMessage(MSG_VALIDATE_NAME_EMPTY),
		                       RSEUIPlugin.getPluginMessage(MSG_VALIDATE_NAME_NOTUNIQUE));  
		fUnique = false;		
		msg_Invalid = RSEUIPlugin.getPluginMessage(MSG_VALIDATE_NAME_NOTVALID);				
	}	
	/**
	 * Supply your own error message text. By default, messages from RSEUIPlugin resource bundle are used.
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
		return "SystemNameValidator class";
	}

    // -------------------
    // Parent overrides...
    // -------------------
    
	/**
	 * Validate each character. 
	 * Override of parent method.
	 * Override yourself to refine the error checking.	 
	 */
	public SystemMessage isSyntaxOk(String newText)
	{
	   char currChar = newText.charAt(0);
	   if (!Character.isJavaIdentifierStart(currChar))
	     return msg_Invalid;
	   for (int idx=1; idx<newText.length(); idx++)
	   {
	   	  currChar = newText.charAt(idx);
		  if (!Character.isJavaIdentifierPart(currChar))
		    return msg_Invalid;		
	   }
	   return null;
	}
	
}