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


/**
 * A dialog to select or enter archive files.
 */
public class CombineDialog extends SystemSelectRemoteFileOrFolderDialog {
	
	protected CombineForm form;
	protected boolean prePop = false;
	
	/**
	 * Constructor.
	 * @param shell the parent shell.
	 */	
	public CombineDialog(Shell shell)
	{
		super(shell, false);
		setHelp(RSEUIPlugin.HELPPREFIX + "cmbd0000");
	}	
	
	/**
	 * Constructor when you want to supply your own title.
	 * @param shell The shell to hang the dialog off of
	 * @param title The title to give the dialog
	 */
	public CombineDialog(Shell shell, String title)
	{
		super(shell, title, false);
		setHelp(RSEUIPlugin.HELPPREFIX + "cmbd0000");
	}
	
	/**
	 * Constructor when you want to supply your own title.
	 * 
	 * @param shell The shell to hang the dialog off of
	 * @param title The title to give the dialog
	 * @param prePopSelection Whether or not the destination name
	 * and type will be prepopulated with the selection, or given
	 * a generic name and type.
	 */
	public CombineDialog(Shell shell, String title, boolean prePopSelection)
	{
		super(shell, title, false);
		prePop = prePopSelection;
		if (form != null) form.setPrePopSelection(prePop);
		setHelp(RSEUIPlugin.HELPPREFIX + "cmbd0000");
	}

	/**
	 * Constructor when you want to supply your own title.
	 * 
	 * @param shell The shell to hang the dialog off of
	 * @param title The title to give the dialog
	 * @param prePopSelection Whether or not the destination name
	 * and type will be prepopulated with the selection, or given
	 * a generic name and type.
	 * @param relativePaths The relative paths to be inserted as choices
	 * in the relative path combo box. Null if you wish to disable this feature.
	 */
	public CombineDialog(Shell shell, String title, boolean prePopSelection, String[] relativePaths)
	{
		super(shell, title, false);
		prePop = prePopSelection;
		if (form != null) form.setPrePopSelection(prePop);
		setHelp(RSEUIPlugin.HELPPREFIX + "cmbd0000");
	}

		
	protected SystemSelectRemoteFileOrFolderForm getForm(boolean fileMode)
	{
		form = new CombineForm(getMessageLine(), this, fileMode, prePop);
		super.getForm(fileMode);
		return form;
	}
    
	public Object getOutputObject()
	{
		String fileName = form.getFileName();
		
		IRemoteFile file = (IRemoteFile) super.getOutputObject();
		
		// if a file was selected, get reference to its parent
		if (file.isFile()) {
			file = file.getParentRemoteFile();
		}
		
		try
		{
			// return a remote file that is the child of the parent folder
			return file.getParentRemoteFileSubSystem().getRemoteFileObject(file, fileName);
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	public void setShowLocationPrompt(boolean show)
	{
		form.setShowLocationPrompt(show);
	}

	public void setLocationPrompt(String prompt)
	{
		form.setLocationPrompt(prompt);
	}
	
	public void setNameAndTypePrompt(String prompt)
	{
		form.setNameAndTypePrompt(prompt);
	}
	
	/**
	 * Sets the extensions to disallow.
	 * @param extensions the archive extensions that will not be allowed.
	 */
	public void setDisallowedArchiveExtensions(String[] extensions) {
		form.setDisallowedArchiveExtensions(extensions);
	}
}