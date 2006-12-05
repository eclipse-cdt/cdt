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
package org.eclipse.rse.files.ui.internal.search;

import org.eclipse.rse.files.ui.dialogs.SystemSelectRemoteFileOrFolderDialog;
import org.eclipse.rse.files.ui.widgets.SystemSelectRemoteFileOrFolderForm;
import org.eclipse.swt.widgets.Shell;

/**
 * Select dialog for search.
 */
public class SystemSearchRemoteFolderDialog extends SystemSelectRemoteFileOrFolderDialog {

	/**
	 * Constructor.
	 * @param shell the parent shell.
	 */
	public SystemSearchRemoteFolderDialog(Shell shell) {
		super(shell, false);
	}

	/**
	 * Contructor.
	 * @param shell the parent shell.
	 * @param title the the title of the dialog.
	 */
	public SystemSearchRemoteFolderDialog(Shell shell, String title) {
		super(shell, title, false);
	}

	/**
	 * Creates an instance of the select form for search {@link SystemSearchRemoteFolderForm}
	 * @see org.eclipse.rse.files.ui.dialogs.SystemSelectRemoteFileOrFolderDialog#getForm(boolean)
	 */
	protected SystemSelectRemoteFileOrFolderForm getForm(boolean fileMode) {
		form = new SystemSearchRemoteFolderForm(getMessageLine(), this);
		setOutputObject(null);
		outputConnection = null;
    	return form;
	}
}