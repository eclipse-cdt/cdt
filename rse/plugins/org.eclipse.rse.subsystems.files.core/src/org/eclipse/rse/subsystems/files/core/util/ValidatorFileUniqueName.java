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

import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.validators.ValidatorFileName;
import org.eclipse.rse.ui.validators.ValidatorFolderName;
import org.eclipse.rse.ui.validators.ValidatorUniqueString;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Shell;



/**
 * This class is used in dialogs that prompt for file or folder names, and those names need to be unique
 *  within a given folder. To make sure error checking while typing is fast, we go get the list of names
 *  of the files and folders in the constructor and re-use it from then on. 
 * <p>
 * The IInputValidator interface is implemented by our parent and it
 * is used by jface's InputDialog class and property sheet window.
 * <p>
 * If you subclass this, consider overriding the getFileNameValidator and
 * getFolderNameValidator methods.
 */
public class ValidatorFileUniqueName 
       extends ValidatorUniqueString implements ISystemMessages
{
	//public static final boolean CASE_SENSITIVE = true;
	//public static final boolean CASE_INSENSITIVE = false;
	protected SystemMessage   msg_Invalid;
	private ValidatorFileName fileNameValidator = new ValidatorFileName();
	private ValidatorFolderName folderNameValidator = new ValidatorFolderName();	
	protected IRemoteFile parentFolder;
	protected Vector names;
	protected boolean isFolder;
	
	/**
	 * Constructor
	 * @param parentFolder the folder within which the name must be unique.
	 * @param true if validating a folder name versus a file name
	 */
	public ValidatorFileUniqueName(Shell shell, IRemoteFile parentFolder, boolean isFolder)
	{
		super((Vector)null, parentFolder.getParentRemoteFileSubSystem().isCaseSensitive() );
		this.parentFolder = parentFolder;
		this.isFolder = isFolder;
		init(shell);
	}


    private void init(Shell shell)
    {
	    Cursor busyCursor = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
	    //shell.setCursor(busyCursor);
        org.eclipse.rse.ui.dialogs.SystemPromptDialog.setDisplayCursor(shell, busyCursor);	    

		setErrorMessages(SystemPlugin.getPluginMessage(MSG_VALIDATE_NAME_EMPTY),
		                 SystemPlugin.getPluginMessage(MSG_VALIDATE_NAME_NOTUNIQUE),
		                 isFolder ? SystemPlugin.getPluginMessage(MSG_VALIDATE_FOLDERNAME_NOTVALID) :
		                            SystemPlugin.getPluginMessage(MSG_VALIDATE_FILENAME_NOTVALID)
		                );  
		IRemoteFile[] contents = parentFolder.getParentRemoteFileSubSystem().listFoldersAndFiles(parentFolder);
		if (contents!=null)
		{
		  String[] names = new String[contents.length];
		  for (int idx=0; idx<names.length; idx++)
		     names[idx] = contents[idx].getName();
		  setExistingNamesList(names);
		}

	    //shell.setCursor(null);
        org.eclipse.rse.ui.dialogs.SystemPromptDialog.setDisplayCursor(shell, null);	    
		busyCursor.dispose();
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
	   if (!isFolder)
	   	 return getFileNameValidator().isSyntaxOk(newText);
	   else
	   	 return getFolderNameValidator().isSyntaxOk(newText);
	}

	/**
	 * Overridable extension point to get basic file name validator
	 * By default, queries it from the file subsystem factory of the parent folder
	 */
	protected ValidatorFileName getFileNameValidator()
	{
		if (fileNameValidator == null)
		  fileNameValidator = parentFolder.getParentRemoteFileSubSystem().getParentRemoteFileSubSystemFactory().getFileNameValidator();;
		return fileNameValidator;
	}
	/**
	 * Overridable extension point to get basic folder name validator.
	 * By default, queries it from the file subsystem factory of the parent folder
	 */
	protected ValidatorFolderName getFolderNameValidator()
	{
		if (folderNameValidator == null)
		  folderNameValidator = parentFolder.getParentRemoteFileSubSystem().getParentRemoteFileSubSystemFactory().getFolderNameValidator();
		return folderNameValidator;
	}

    /**
     * Return the max length for this file or folder name
     */
    public int getMaximumNameLength()
    {
	    if (isFolder)
	   	  return getFolderNameValidator().getMaximumNameLength();
	    else
	   	  return getFileNameValidator().getMaximumNameLength();
    }
    
    
	public String toString()
	{
		return "ValidatorFileFilterString class";
	}
}