/********************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation. All rights reserved. 
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Kushal Munir (IBM) - initial API and implementation.
 ********************************************************************************/
package org.eclipse.rse.internal.files.ui.search;

import org.eclipse.rse.files.ui.dialogs.SystemRemoteFolderDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * Select dialog for search.
 */
public class SystemSearchRemoteFolderDialog extends SystemRemoteFolderDialog {

	/**
	 * Constructor.
	 * @param shell the parent shell.
	 */
	public SystemSearchRemoteFolderDialog(Shell shell) {
		super(shell);
	}

	/**
	 * Contructor.
	 * @param shell the parent shell.
	 * @param title the the title of the dialog.
	 */
	public SystemSearchRemoteFolderDialog(Shell shell, String title) {
		super(shell, title);
	}

	
	/**
	 * Creates an instance of the select form for search {@link SystemSearchRemoteFolderForm}
	 * @see org.eclipse.rse.files.ui.dialogs.SystemSelectRemoteFileOrFolderDialog#getForm(boolean)
	 */
	/*
	protected SystemSelectRemoteFileOrFolderForm getForm(boolean fileMode) {
		form = new SystemSearchRemoteFolderForm(getMessageLine(), this);
		setOutputObject(null);
		outputConnection = null;
    	return form;
	}
	*/
}