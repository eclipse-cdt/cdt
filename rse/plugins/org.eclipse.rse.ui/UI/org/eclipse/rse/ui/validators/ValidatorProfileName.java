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
import org.eclipse.rse.ui.ISystemMessages;


/**
 * This class is used in dialogs that prompt for a profile name.
 * Relies on Eclipse supplied method to test for folder name validity.
 * <p>
 * The IInputValidator interface is used by jface's
 * InputDialog class and numerous other platform and system classes.
 */
public class ValidatorProfileName 
       extends ValidatorFolderName implements ISystemMessages, ISystemValidator
{
	public static final int MAX_PROFILENAME_LENGTH = 100; // arbitrary restriction! Defect 41816
	private SystemMessage reservedNameMsg;
	
	/**
	 * Constructor. The list of existing names can be null if this is not a rename operation.
	 */
	public ValidatorProfileName(Vector existingNameList)
	{
		super(existingNameList);
		super.setErrorMessages(SystemPlugin.getPluginMessage(MSG_VALIDATE_PROFILENAME_EMPTY),
		                       SystemPlugin.getPluginMessage(MSG_VALIDATE_PROFILENAME_NOTUNIQUE),  
		                       SystemPlugin.getPluginMessage(MSG_VALIDATE_PROFILENAME_NOTVALID));  
	}

    /**
     * Return the max length for profiles: 100
     */
    public int getMaximumNameLength()
    {
    	return MAX_PROFILENAME_LENGTH;
    }
    
    /**
     * Return the msg for reserved names
     */
    private SystemMessage getReservedNameMessage()
    {
    	if (reservedNameMsg == null)
    	   reservedNameMsg = SystemPlugin.getPluginMessage(MSG_VALIDATE_PROFILENAME_RESERVED);
    	return reservedNameMsg;
    }

    // ---------------------------
    // Parent Overrides...
    // ---------------------------
	/**
	 * Parent intercept to ensure no reserved names are used.
	 */
	public SystemMessage isSyntaxOk(String newText)
	{
	   super.isSyntaxOk(newText);
	   if (currentMessage == null)
	   {
	   	  if (newText.equalsIgnoreCase("private")) {
	   	    currentMessage = getReservedNameMessage();
	   	  }
	   	  else if (newText.indexOf('.') != -1) {
	   	    currentMessage = msg_Invalid;
	   	  }

	   }
	   return currentMessage;
	}

}