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
 * David McKnight   (IBM)        - [207178] changing list APIs for file service and subsystems
 * David McKnight   (IBM)        - [216252] [api][nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 * David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 *******************************************************************************/

package org.eclipse.rse.subsystems.files.core.util;
import java.util.Vector;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.rse.internal.subsystems.files.core.Activator;
import org.eclipse.rse.internal.subsystems.files.core.ISystemFileMessageIds;
import org.eclipse.rse.internal.subsystems.files.core.SystemFileResources;
import org.eclipse.rse.services.clientserver.messages.SimpleSystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
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
       extends ValidatorUniqueString
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
	 * @param isFolder true if validating a folder name versus a file name
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

		setErrorMessages(new SimpleSystemMessage(Activator.PLUGIN_ID, 
				ISystemFileMessageIds.MSG_VALIDATE_NAME_EMPTY,
				IStatus.ERROR, 
				SystemFileResources.MSG_VALIDATE_NAME_EMPTY, SystemFileResources.MSG_VALIDATE_NAME_EMPTY_DETAILS),
				new SimpleSystemMessage(Activator.PLUGIN_ID, 
						ISystemFileMessageIds.MSG_VALIDATE_NAME_NOTUNIQUE,
						IStatus.ERROR, 
						SystemFileResources.MSG_VALIDATE_NAME_NOTUNIQUE, SystemFileResources.MSG_VALIDATE_NAME_NOTUNIQUE_DETAILS),
				isFolder ? new SimpleSystemMessage(Activator.PLUGIN_ID, 
						ISystemFileMessageIds.FILEMSG_VALIDATE_FILEFILTERSTRING_NOTUNIQUE,
						IStatus.ERROR, SystemFileResources.FILEMSG_VALIDATE_FILEFILTERSTRING_NOTUNIQUE) :
						new SimpleSystemMessage(Activator.PLUGIN_ID, 
								ISystemFileMessageIds.FILEMSG_VALIDATE_FILEFILTERSTRING_NOTVALID,
								IStatus.ERROR, SystemFileResources.FILEMSG_VALIDATE_FILEFILTERSTRING_NOTVALID));  

		try
		{
		IRemoteFile[] contents = parentFolder.getParentRemoteFileSubSystem().list(parentFolder, null);
		if (contents!=null)
		{
		  String[] names = new String[contents.length];
		  for (int idx=0; idx<names.length; idx++)
		     names[idx] = contents[idx].getName();
		  setExistingNamesList(names);
		}
		}
		catch (SystemMessageException e)
		{
		}

	    //shell.setCursor(null);
        org.eclipse.rse.ui.dialogs.SystemPromptDialog.setDisplayCursor(shell, null);	    
		busyCursor.dispose();
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
		  fileNameValidator = parentFolder.getParentRemoteFileSubSystem().getParentRemoteFileSubSystemConfiguration().getFileNameValidator();
		return fileNameValidator;
	}
	/**
	 * Overridable extension point to get basic folder name validator.
	 * By default, queries it from the file subsystem factory of the parent folder
	 */
	protected ValidatorFolderName getFolderNameValidator()
	{
		if (folderNameValidator == null)
		  folderNameValidator = parentFolder.getParentRemoteFileSubSystem().getParentRemoteFileSubSystemConfiguration().getFolderNameValidator();
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
		return "ValidatorFileFilterString class"; //$NON-NLS-1$
	}
}
