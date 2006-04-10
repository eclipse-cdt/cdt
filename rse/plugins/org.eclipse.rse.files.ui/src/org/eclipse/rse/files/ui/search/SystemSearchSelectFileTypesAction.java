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


package org.eclipse.rse.files.ui.search;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.rse.files.ui.actions.SystemSelectFileTypesAction;
import org.eclipse.swt.widgets.Shell;


/**
 * Action to select file types for the search dialog.
 */
public class SystemSearchSelectFileTypesAction
	extends SystemSelectFileTypesAction {

	/**
	 * Creates the action.
	 * @param shell
	 */
	public SystemSearchSelectFileTypesAction(Shell shell) {
		super(shell);
	}
	

	/**
	 * @see org.eclipse.rse.ui.actions.SystemBaseDialogAction#createDialog(org.eclipse.swt.widgets.Shell)
	 */
	public Dialog createDialog(Shell parent) {
		SystemSearchSelectFileTypesDialog dialog = new SystemSearchSelectFileTypesDialog(getShell(), types);
		return dialog;  
	}
}