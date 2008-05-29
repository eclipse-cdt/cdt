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
 * {Name} (company) - description of contribution.
 * Xuan Chen        (IBM)        - [220999] [api] Need to change class SystemSelectRemoteFileAction to use SystemRemoteFileDialog
 *                                          Move SystemSelectRemoteFileOrFolderDialog to internal package first.
 *******************************************************************************/

package org.eclipse.rse.internal.files.ui.dialogs;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.rse.files.ui.dialogs.ISaveAsDialog;
import org.eclipse.rse.files.ui.widgets.SaveAsForm;
import org.eclipse.rse.files.ui.widgets.SystemSelectRemoteFileOrFolderForm;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.swt.widgets.Shell;


public class SaveAsDialog extends SystemSelectRemoteFileOrFolderDialog implements ISaveAsDialog {

	
	private SaveAsForm form;

	/**
	 * Constructor
	 * 
	 * @param shell The shell to hang the dialog off of
	 * 
	 */
	public SaveAsDialog(Shell shell)
	{
		super(shell, false);
	}	
	/**
	 * Constructor when you want to supply your own title.
	 * 
	 * @param shell The shell to hang the dialog off of
	 * @param title The title to give the dialog
	 */
	public SaveAsDialog(Shell shell, String title)
	{
		super(shell, title, true);
	}
	
	
    protected SystemSelectRemoteFileOrFolderForm getForm(boolean fileMode)
    {
		//System.out.println("INSIDE GETFORM");    	
    	//if (form == null)
    	//{
    	  form = new SaveAsForm(getMessageLine(), this, fileMode);
		  super.getForm(fileMode);

    	//}
    	return form;
    }
    
    public Object getOutputObject()
    {
    	IRemoteFile file = (IRemoteFile) super.getOutputObject();
    	if (file.isFile())
    	{
    		return file;
    	}
    	
    	try
    	{
	   		return file.getParentRemoteFileSubSystem().getRemoteFileObject(file, form.getFileName(), new NullProgressMonitor());
    	}
    	catch (Exception e)
    	{
    		return null;
    	}
    }
    
}
