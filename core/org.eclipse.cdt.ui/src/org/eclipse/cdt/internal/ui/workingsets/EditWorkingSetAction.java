/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.workingsets;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;

import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetEditWizard;
import org.eclipse.ui.help.WorkbenchHelp;


/**
 * Displays an IWorkingSetEditWizard for editing a working set.
 * 
 * @since 2.1
 */
public class EditWorkingSetAction extends Action {
	private Shell fShell;
	private WorkingSetFilterActionGroup fActionGroup;

	public EditWorkingSetAction(WorkingSetFilterActionGroup actionGroup, Shell shell) {
		super(WorkingSetMessages.getString("EditWorkingSetAction.text")); //$NON-NLS-1$
		Assert.isNotNull(actionGroup);
		setToolTipText(WorkingSetMessages.getString("EditWorkingSetAction.toolTip")); //$NON-NLS-1$
		setEnabled(actionGroup.getWorkingSet() != null);
		fShell= shell;
		fActionGroup= actionGroup;
		WorkbenchHelp.setHelp(this, ICHelpContextIds.EDIT_WORKING_SET_ACTION);
	}
	
	/*
	 * Overrides method from Action
	 */
	public void run() {
		if (fShell == null)
			fShell= CUIPlugin.getActiveWorkbenchShell();
		IWorkingSetManager manager= PlatformUI.getWorkbench().getWorkingSetManager();
		IWorkingSet workingSet= fActionGroup.getWorkingSet();
		if (workingSet == null) {
			setEnabled(false);
			return;
		}
		IWorkingSetEditWizard wizard= manager.createWorkingSetEditWizard(workingSet);
		if (wizard == null) {
			String title= WorkingSetMessages.getString("EditWorkingSetAction.nowizard.title"); //$NON-NLS-1$
			String message= WorkingSetMessages.getString("EditWorkingSetAction.nowizard.message"); //$NON-NLS-1$
			MessageDialog.openError(fShell, title, message);
			return;
		}
		WizardDialog dialog= new WizardDialog(fShell, wizard);
	 	dialog.create();		
		if (dialog.open() == Window.OK)
			fActionGroup.setWorkingSet(wizard.getSelection(), true);
	}
}
