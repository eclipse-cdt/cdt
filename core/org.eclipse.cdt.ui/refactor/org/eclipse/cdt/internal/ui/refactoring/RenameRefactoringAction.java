/**********************************************************************
 * Copyright (c) 2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.ui.refactoring;


import org.eclipse.core.runtime.CoreException;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.IWorkbenchSite;

import org.eclipse.cdt.internal.corext.refactoring.RenameRefactoring;

import org.eclipse.cdt.internal.ui.refactoring.UserInterfaceStarter;
import org.eclipse.cdt.internal.ui.util.ExceptionHandler;

import org.eclipse.cdt.ui.actions.SelectionDispatchAction;


public class RenameRefactoringAction extends SelectionDispatchAction {

	public RenameRefactoringAction(IWorkbenchSite site) {
		super(site);
		setText("Rename ...");
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.ui.actions.SelectionDispatchAction#selectionChanged(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void selectionChanged(IStructuredSelection selection) {
		setEnabled(selection.size() == 1);
	}


	/* (non-Javadoc)
	 * @see org.eclipse.jdt.ui.actions.SelectionDispatchAction#run(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void run(IStructuredSelection selection) {
		Object element= selection.getFirstElement();
		try {
			RenameRefactoring refactoring= new RenameRefactoring(element);
			run(refactoring, getShell());
		} catch (CoreException e) {
			ExceptionHandler.handle(e, getShell(), "Rename Refactoring", "Unexpected Exception occured");
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		run();
	}
	
	public void selectionChanged(IAction action, ISelection selection) {
		action.setEnabled(true);
	}
	
	public static void run(RenameRefactoring refactoring, Shell parent) throws CoreException {
		if (refactoring.isAvailable()) {
			UserInterfaceStarter.run(refactoring, parent);
		} else {
			MessageDialog.openInformation(parent, 
				"Rename Refactoring", 
				"No refactoring available to process the selected element.");
		}		
	}
}
