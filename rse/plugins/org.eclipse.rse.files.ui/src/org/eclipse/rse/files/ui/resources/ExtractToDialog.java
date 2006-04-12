/********************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.files.ui.resources;
import org.eclipse.rse.files.ui.dialogs.SystemSelectRemoteFileOrFolderDialog;
import org.eclipse.rse.files.ui.widgets.SystemSelectRemoteFileOrFolderForm;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.swt.widgets.Shell;


public class ExtractToDialog extends SystemSelectRemoteFileOrFolderDialog {

	/**
	 * Constructor
	 * 
	 * @param shell The shell to hang the dialog off of
	 * @param fileMode True if selecting files, false if selecting folders
	 * 
	 */
	
	private ExtractToForm form;
	
	
	public ExtractToDialog(Shell shell)
	{
		super(shell, false);
		setHelp(RSEUIPlugin.HELPPREFIX + "exdi0000");
	}	
	/**
	 * Constructor when you want to supply your own title.
	 * 
	 * @param shell The shell to hang the dialog off of
	 * @param title The title to give the dialog
	 * @param fileMode True if selecting files, false if selecting folders
	 */
	public ExtractToDialog(Shell shell, String title)
	{
		super(shell, title, false);
		setHelp(RSEUIPlugin.HELPPREFIX + "exdi0000");
	}
		
	protected SystemSelectRemoteFileOrFolderForm getForm(boolean fileMode)
	{
		form = new ExtractToForm(getMessageLine(), this, fileMode);
		super.getForm(fileMode);
		return form;
	}
    
	public Object getOutputObject()
	{
		IRemoteFile file = (IRemoteFile) super.getOutputObject();
		if (file.exists())
		{
			return file;
		}
    	return null;
	}
    
}