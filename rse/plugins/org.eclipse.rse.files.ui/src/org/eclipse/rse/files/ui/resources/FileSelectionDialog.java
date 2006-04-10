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

package org.eclipse.rse.files.ui.resources;
import org.eclipse.rse.files.ui.dialogs.SystemSelectRemoteFileOrFolderDialog;
import org.eclipse.rse.files.ui.widgets.SystemSelectRemoteFileOrFolderForm;
import org.eclipse.swt.widgets.Shell;


/**
 * Dialog used to select a file based on a dialog similar to the SaveAs
 */
public class FileSelectionDialog
	extends SystemSelectRemoteFileOrFolderDialog
	implements ISaveAsDialog
{

	public static final String Copyright =
		"(C) Copyright IBM Corp. 2003  All Rights Reserved.";

	/**
	 * Constructor
	 * 
	 * @param shell The shell to hang the dialog off of
	 * @param fileMode True if selecting files, false if selecting folders
	 * 
	 */

	private FileSelectionForm form;

	protected FileSelectionDialog(Shell shell)
	{
		super(shell, false);
	}
	/**
	 * Constructor when you want to supply your own title.
	 * 
	 * @param shell The shell to hang the dialog off of
	 * @param title The title to give the dialog
	 * @param fileMode True if selecting files, false if selecting folders
	 */
	protected FileSelectionDialog(Shell shell, String title)
	{
		super(shell, title, true);
	}

	public static FileSelectionDialog getFileSelectionDialog(
		Shell shell,
		String title)
	{
		return new FileSelectionDialog(shell, title);
	}

	public static FileSelectionDialog getFileSelectionDialog(Shell shell)
	{
		return new FileSelectionDialog(shell);
	}

	protected SystemSelectRemoteFileOrFolderForm getForm(boolean fileMode)
	{
		form = new FileSelectionForm(getMessageLine(), this, fileMode);
		super.getForm(fileMode);
		return form;
	}
	/**
 	 * Return file name specified
 	 * @return File name
 	 */
	public String getFileName()
	{
		return form.getFileName();
	}	
}