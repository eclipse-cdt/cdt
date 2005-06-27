/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.cdt.internal.ui.refactoring.RefactoringMessages;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringStatusContentProvider;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringStatusEntryLabelProvider;

import org.eclipse.cdt.internal.corext.refactoring.base.RefactoringStatus;
import org.eclipse.cdt.internal.corext.refactoring.base.RefactoringStatusCodes;
import org.eclipse.cdt.internal.corext.refactoring.base.RefactoringStatusEntry;

public class RefactoringErrorDialogUtil {
	
	private RefactoringErrorDialogUtil() {
		// no instance.
	}
	
	public static Object open(String dialogTitle, RefactoringStatus status, Shell parentShell) {
		if (status.getEntries().size() == 1) {
			RefactoringStatusEntry entry= (RefactoringStatusEntry)status.getEntries().get(0);
			String message= status.getFirstMessage(RefactoringStatus.FATAL);
			
			if (   entry.getCode() != RefactoringStatusCodes.OVERRIDES_ANOTHER_METHOD
				&& entry.getCode() != RefactoringStatusCodes.METHOD_DECLARED_IN_INTERFACE){
				MessageDialog.openInformation(parentShell, dialogTitle, message);
				return null;
			}			
			message= message + RefactoringMessages.getString("RefactoringErrorDialogUtil.okToPerformQuestion"); //$NON-NLS-1$
			if (MessageDialog.openQuestion(parentShell, dialogTitle, message))
				return entry.getData();
			return null;
		} else {
			openListDialog(dialogTitle, status, parentShell);	
			return null;
		}
	}
	
	private static void openListDialog(String dialogTitle, RefactoringStatus status, Shell parentShell) {
		ListDialog dialog= new ListDialog(parentShell);
		dialog.setInput(status);
		dialog.setTitle(dialogTitle);
		dialog.setMessage(RefactoringMessages.getString("RefactoringErrorDialogUtil.cannot_perform")); //$NON-NLS-1$
		dialog.setContentProvider(new RefactoringStatusContentProvider());
		dialog.setLabelProvider(new RefactoringStatusEntryLabelProvider());
		dialog.open();	
	}
}
