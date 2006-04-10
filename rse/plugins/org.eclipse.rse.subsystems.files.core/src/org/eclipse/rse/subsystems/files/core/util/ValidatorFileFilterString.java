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

package org.eclipse.rse.subsystems.files.core.util;
import java.util.Vector;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.services.clientserver.NamePatternMatcher;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystemConfiguration;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.validators.ValidatorFileName;
import org.eclipse.rse.ui.validators.ValidatorFolderName;
import org.eclipse.rse.ui.validators.ValidatorUniqueString;



/**
 * This class is used in dialogs that prompt for file filter strings.
 * File filter strings are a bit complex to validate. They have to be:
 * <ul>
 *   <li>Unique within their filter
 *   <li>Valid generic names (there are rules regarding number and location of asterisks)
 *   <li>Either a valid file or folder name, or both, when the asterisks are substituted with a letter like 'A'
 * </ul>
 * <p>
 * The IInputValidator interface is implemented by our parent and it
 * is used by jface's InputDialog class and property sheet window.
 * <p>
 * If you subclass this, consider overriding the getFileNameValidator and
 * getFolderNameValidator methods.
 */
public class ValidatorFileFilterString 
       extends ValidatorUniqueString implements ISystemMessages
{
	//public static final boolean CASE_SENSITIVE = true;
	//public static final boolean CASE_INSENSITIVE = false;
	protected SystemMessage   msg_Invalid;
	protected IWorkspace workspace = ResourcesPlugin.getWorkspace();
	protected boolean  isFileName, isFolderName;
	private ValidatorFileName fileNameValidator;
	private ValidatorFolderName folderNameValidator;	
	private IRemoteFileSubSystemConfiguration ssFactory;
	
	/**
	 * Constructor accepting a Vector for the list of existing names.
	 * @param ssFactory - The remote subsystem factory we are validating filter strings in
	 * @param existingList - A vector containing list of existing filter names to compare against.
	 *        Note that toString() is used to get the string from each item.
	 */
	public ValidatorFileFilterString(IRemoteFileSubSystemConfiguration ssFactory, Vector existingList)
	{
		super(existingList, ssFactory.isCaseSensitive()); // case sensitive uniqueness		
		this.ssFactory = ssFactory;
		init();
	}
	/**
	 * Constructor accepting an Array for the list of existing names.
	 * @param ssFactory - The remote subsystem factory we are validating filter strings in
	 * @param existingList - An array containing list of existing strings to compare against.
	 */
	public ValidatorFileFilterString(IRemoteFileSubSystemConfiguration ssFactory, String[] existingList)
	{
		super(existingList, ssFactory.isCaseSensitive()); // case sensitive uniqueness		
		this.ssFactory = ssFactory;
		init();
	}

	/**
	 * Use this constructor when the name need not be unique, and you just want
	 * the syntax checking.
	 * @param ssFactory - The remote subsystem factory we are validating filter strings in
	 */
	public ValidatorFileFilterString(IRemoteFileSubSystemConfiguration ssFactory)
	{
		super(new String[0], ssFactory.isCaseSensitive());
		this.ssFactory = ssFactory;
		init();
	}	


    private void init()
    {
		//setErrorMessages(SystemPlugin.getPluginMessage(FILEMSG_VALIDATE_FILEFILTERSTRING_EMPTY),
		setErrorMessages(SystemPlugin.getPluginMessage(MSG_VALIDATE_NAME_EMPTY),
		                 SystemPlugin.getPluginMessage(FILEMSG_VALIDATE_FILEFILTERSTRING_NOTUNIQUE),
		                 SystemPlugin.getPluginMessage(FILEMSG_VALIDATE_FILEFILTERSTRING_NOTVALID));  
		isFileName = isFolderName = true;
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
     * Call this before calling isValid!
     * Specify true if this is a file name filter
     * @see #setIsFolderName(boolean)
     */
    public void setIsFileName(boolean is)
    {
    	this.isFileName = is;
    }
    /**
     * Call this before calling isValid!
     * Specify true if this is a folder name filter
     * @see #setIsFileName(boolean)
     */
    public void setIsFolderName(boolean is)
    {
    	this.isFolderName = is;
    }


	/**
	 * Validate each character. 
	 * Override of parent method.
	 * Override yourself to refine the error checking.	 
	 */
	public SystemMessage isSyntaxOk(String newText)
	{
	   boolean ok = NamePatternMatcher.verifyPattern(newText, true); // true=>advanced-style-patterns
	   if (!ok)
	     return msg_Invalid;
       String tempText = newText;     
       if (isFileName || isFolderName)
         tempText = newText.replace('*','A');
	   //if (isFileName && (errMsg==null))
	   if (isFileName)
	   	 return getFileNameValidator().isSyntaxOk(tempText);
	   else if (isFolderName)
	   	 return getFolderNameValidator().isSyntaxOk(tempText);
	   return null;
	}
    
	/**
	 * Overridable extension point to get basic file name validator
	 * By default, queries it from the file subsystem factory
	 */
	protected ValidatorFileName getFileNameValidator()
	{
		if (fileNameValidator == null)
		  fileNameValidator = ssFactory.getFileNameValidator();;
		return fileNameValidator;
	}
	/**
	 * Overridable extension point to get basic folder name validator
	 * By default, queries it from the file subsystem factory
	 */
	protected ValidatorFolderName getFolderNameValidator()
	{
		if (folderNameValidator == null)
		  folderNameValidator = ssFactory.getFolderNameValidator();;
		return folderNameValidator;
	}
	
    /**
     * Return true if case sensitive, false it not. 
     * By default, return ssFactory.isUnixStyle()
     */
    protected boolean isCaseSensitive(IRemoteFileSubSystemConfiguration ssFactory)
    {
    	return ssFactory.isUnixStyle();
    }

    /**
     * Return the max length for this file or folder name
     */
    public int getMaximumNameLength()
    {
    	/* should probably do the following but too high risk right now
	    if (isFileName)
	   	  return getFileNameValidator().getMaximumNameLength();
	    else if (isFolderName)
	   	  return getFolderNameValidator().getMaximumNameLength();
	   	*/
    	return -1;
    }

    
	public String toString()
	{
		return "ValidatorFileFilterString class";
	}
	
	
}