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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.RSEUIPlugin;


/**
 * This class is used in dialogs that prompt for a name that eventually needs to become a folder.
 * Relies on Eclipse supplied method to test for folder name validity.
 *
 * The IInputValidator interface is used by jface's
 * InputDialog class and numerous other platform and system classes.
 */
public class ValidatorFolderName 
	   extends ValidatorUniqueString implements ISystemValidator
{
	public static final int MAX_FOLDERNAME_LENGTH = 256; 
		
	protected boolean  fUnique;
	protected SystemMessage   msg_Invalid;
	protected IWorkspace workspace = ResourcesPlugin.getWorkspace();
	
	/**
	 * Use this constructor when the name must be unique. Give the
	 * ctor a vector containing a list of existing names to compare against.
	 */
	public ValidatorFolderName(Vector existingNameList)
	{
		super(existingNameList, CASE_INSENSITIVE); // case insensitive uniqueness
		super.setErrorMessages(RSEUIPlugin.getPluginMessage(MSG_VALIDATE_NAME_EMPTY),
		                       RSEUIPlugin.getPluginMessage(MSG_VALIDATE_NAME_NOTUNIQUE));  
		fUnique = true;
		msg_Invalid = RSEUIPlugin.getPluginMessage(MSG_VALIDATE_FOLDERNAME_NOTVALID);				
	}
	/**
	 * Use this constructor when the name must be unique. Give the
	 * ctor a string array of existing names to compare against.
	 */
	public ValidatorFolderName(String existingNameList[])
	{
		super(existingNameList, CASE_INSENSITIVE); // case sensitive uniqueness
		super.setErrorMessages(RSEUIPlugin.getPluginMessage(MSG_VALIDATE_NAME_EMPTY),
		                       RSEUIPlugin.getPluginMessage(MSG_VALIDATE_NAME_NOTUNIQUE));  
		fUnique = true;
		msg_Invalid = RSEUIPlugin.getPluginMessage(MSG_VALIDATE_FOLDERNAME_NOTVALID);				
	}
	
	/**
	 * Use this constructor when the name need not be unique, and you just want
	 * the syntax checking.
	 */
	public ValidatorFolderName()
	{
		super(new String[0], CASE_INSENSITIVE);
		super.setErrorMessages(RSEUIPlugin.getPluginMessage(MSG_VALIDATE_NAME_EMPTY),
		                       RSEUIPlugin.getPluginMessage(MSG_VALIDATE_NAME_NOTUNIQUE));  
		fUnique = true;
		msg_Invalid = RSEUIPlugin.getPluginMessage(MSG_VALIDATE_FOLDERNAME_NOTVALID);				
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

	/**
	 * Overridable extension point to check for invalidate characters beyond what Eclipse checks for
	 * @return true if valid, false if not
	 */
	protected boolean checkForBadCharacters(String newText)
	{
		return true;
	}
	
	public String toString()
	{
		return "FolderNameValidator class";
	}

    // ---------------------------
    // Parent Overrides...
    // ---------------------------
	/**
	 * Validate each character. 
	 * Override of parent method.
	 * Override yourself to refine the error checking.	 
	 */
	public SystemMessage isSyntaxOk(String newText)
	{
	   IStatus rc = workspace.validateName(newText, IResource.FOLDER);
	   if (rc.getCode() != IStatus.OK)
	     currentMessage = msg_Invalid;
	   else if ((getMaximumNameLength() > 0) && // defect 42507
	            (newText.length() > getMaximumNameLength()))
	     currentMessage = msg_Invalid; // TODO: better message            
	   else
	     currentMessage = checkForBadCharacters(newText) ? null: msg_Invalid;
	   return currentMessage;
	}

    
    // ---------------------------
    // ISystemValidator methods...
    // ---------------------------
    
    /**
     * Return the max length for folder names: 256
     */
    public int getMaximumNameLength()
    {
    	return MAX_FOLDERNAME_LENGTH;
    }

	
}