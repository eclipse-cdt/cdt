/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.files.ui.dialogs;

import org.eclipse.rse.files.ui.widgets.SystemEnterOrSelectRemoteFileForm;
import org.eclipse.rse.files.ui.widgets.SystemSelectRemoteFileOrFolderForm;
import org.eclipse.rse.subsystems.files.core.SystemFileResources;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.swt.widgets.Shell;


/**
 * A dialog for either selecting or entering a file name.
 * Use {@link #getRemotePath()} to get the absolute remote path of what the user selected or entered.
 * Note that if the user enters a file name, then the output object will be its parent folder, since
 * the file with that name may not exist.
 */
public class SystemEnterOrSelectRemoteFileDialog extends SystemSelectRemoteFileOrFolderDialog {
	
	private SystemEnterOrSelectRemoteFileForm form;

	/**
	 * Creates the dialog with the parent shell.
	 * @param shell the parent shell.
	 */
	public SystemEnterOrSelectRemoteFileDialog(Shell shell) {
		super(shell, true);
		setMultipleSelectionMode(false);
	}

	/**
	 * Creates a dialog under the parent shell with the given title.
	 * @param shell the parent shell.
	 * @param title the title for the dialog.
	 */
	public SystemEnterOrSelectRemoteFileDialog(Shell shell, String title) {
		super(shell, title, true);
		setMultipleSelectionMode(false);
	}

	/**
	 * Always returns false.
	 * @see org.eclipse.rse.files.ui.dialogs.SystemSelectRemoteFileOrFolderDialog#getMultipleSelectionMode()
	 */
	public boolean getMultipleSelectionMode() {
		return false;
	}

	/**
	 * Has no effect. Multiple selection mode is not allowed.
	 * @see org.eclipse.rse.files.ui.dialogs.SystemSelectRemoteFileOrFolderDialog#setMultipleSelectionMode(boolean)
	 */
	public void setMultipleSelectionMode(boolean multiple) {
		super.setMultipleSelectionMode(false);
	}
	
	/**
	 * Creates the select or enter file form. Sets the verbage of the form.
	 * @see org.eclipse.rse.files.ui.dialogs.SystemSelectRemoteFileOrFolderDialog#getForm(boolean)
	 */
	protected SystemSelectRemoteFileOrFolderForm getForm(boolean fileMode) {
		form = new SystemEnterOrSelectRemoteFileForm(getMessageLine(), this, fileMode);
		form.setMessage(SystemFileResources.RESID_ENTER_OR_SELECT_FILE_VERBAGE_LABEL);
		setOutputObject(null);
		return form;
	}
	
	/**
	 * Returns the remote path.
	 * @return the remote path.
	 */
	public String getRemotePath() {
		IRemoteFile file = (IRemoteFile)getOutputObject();
		
		String absPath = file.getAbsolutePath();
		
		// if the output is a file, then return the absolute path.
		if (file.isFile()) {
			return absPath;
		}
		// if the output is a folder, then file name is available in the text field
		// of the form, so append the file name to the folder path
		else {
			String fileName = form.getFileName();
			String sep = file.getSeparator();
			
			// add separator if necessary
			if (!absPath.endsWith(sep)) {
				absPath += sep;
			}
			
			// add file name if necessary
			absPath += fileName;
			
			return absPath;
		}
	}
}