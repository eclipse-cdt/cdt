/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
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
 * Martin Oberhuber (Wind River) - [cleanup] fix javadoc.
 * David McKnight   (IBM)        - [216252] [api][nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 * David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 *******************************************************************************/

package org.eclipse.rse.subsystems.files.core.util;
import java.util.Vector;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.rse.internal.subsystems.files.core.Activator;
import org.eclipse.rse.internal.subsystems.files.core.ISystemFileMessageIds;
import org.eclipse.rse.internal.subsystems.files.core.SystemFileResources;
import org.eclipse.rse.services.clientserver.NamePatternMatcher;
import org.eclipse.rse.services.clientserver.messages.SimpleSystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystemConfiguration;
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
       extends ValidatorUniqueString
{
	//public static final boolean CASE_SENSITIVE = true;
	//public static final boolean CASE_INSENSITIVE = false;
	protected SystemMessage   msg_Invalid;
	protected IWorkspace workspace = ResourcesPlugin.getWorkspace();
	protected boolean  isFileName, isFolderName;
	private ValidatorFileName fileNameValidator;
	private ValidatorFolderName folderNameValidator;	
	private IRemoteFileSubSystemConfiguration ssConfiguration;
	
	/**
	 * Constructor accepting a Vector for the list of existing names.
	 * @param ssConfig - The remote subsystem configuration we are validating filter strings in
	 * @param existingList - A vector containing list of existing filter names to compare against.
	 *        Note that toString() is used to get the string from each item.
	 */
	public ValidatorFileFilterString(IRemoteFileSubSystemConfiguration ssConfig, Vector existingList)
	{
		super(existingList, ssConfig.isCaseSensitive()); // case sensitive uniqueness		
		this.ssConfiguration = ssConfig;
		init();
	}
	/**
	 * Constructor accepting an Array for the list of existing names.
	 * @param ssConfig - The remote subsystem configuration we are validating filter strings in
	 * @param existingList - An array containing list of existing strings to compare against.
	 */
	public ValidatorFileFilterString(IRemoteFileSubSystemConfiguration ssConfig, String[] existingList)
	{
		super(existingList, ssConfig.isCaseSensitive()); // case sensitive uniqueness		
		this.ssConfiguration = ssConfig;
		init();
	}

	/**
	 * Use this constructor when the name need not be unique, and you just want
	 * the syntax checking.
	 * @param ssConfig - The remote subsystem configuration we are validating filter strings in
	 */
	public ValidatorFileFilterString(IRemoteFileSubSystemConfiguration ssConfig)
	{
		super(new String[0], ssConfig.isCaseSensitive());
		this.ssConfiguration = ssConfig;
		init();
	}	


    private void init()
    {
		//setErrorMessages(RSEUIPlugin.getPluginMessage(FILEMSG_VALIDATE_FILEFILTERSTRING_EMPTY),
		setErrorMessages(new SimpleSystemMessage(Activator.PLUGIN_ID, 
				ISystemFileMessageIds.MSG_VALIDATE_NAME_EMPTY,
				IStatus.ERROR, SystemFileResources.MSG_VALIDATE_NAME_EMPTY, SystemFileResources.MSG_VALIDATE_NAME_EMPTY_DETAILS),
				new SimpleSystemMessage(Activator.PLUGIN_ID, 
						ISystemFileMessageIds.FILEMSG_VALIDATE_FILEFILTERSTRING_NOTUNIQUE,
						IStatus.ERROR, SystemFileResources.FILEMSG_VALIDATE_FILEFILTERSTRING_NOTUNIQUE),
				new SimpleSystemMessage(Activator.PLUGIN_ID, 
						ISystemFileMessageIds.FILEMSG_VALIDATE_FILEFILTERSTRING_NOTVALID,
						IStatus.ERROR, SystemFileResources.FILEMSG_VALIDATE_FILEFILTERSTRING_NOTVALID));  
		isFileName = isFolderName = true;
    }
    
	/**
	 * Supply your own error message text. By default, messages from RSEUIPlugin resource bundle are used.
	 * @param msg_Empty error message when entry field is empty
	 * @param msg_NonUnique error message when value entered is not unique
	 * @param msg_Invalid error message when syntax is not valid
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
	 * By default, queries it from the file subsystem configuration
	 */
	protected ValidatorFileName getFileNameValidator()
	{
		if (fileNameValidator == null)
		  fileNameValidator = ssConfiguration.getFileNameValidator();
		return fileNameValidator;
	}
	/**
	 * Overridable extension point to get basic folder name validator
	 * By default, queries it from the file subsystem configuration
	 */
	protected ValidatorFolderName getFolderNameValidator()
	{
		if (folderNameValidator == null)
		  folderNameValidator = ssConfiguration.getFolderNameValidator();
		return folderNameValidator;
	}
	
    /**
     * Return true if case sensitive, false it not. 
     * By default, return ssConfiguration.isUnixStyle()
     * @param ssConfig subsystem configuration
     */
    protected boolean isCaseSensitive(IRemoteFileSubSystemConfiguration ssConfig)
    {
    	return ssConfig.isUnixStyle();
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
		return "ValidatorFileFilterString class"; //$NON-NLS-1$
	}
	
	
}
