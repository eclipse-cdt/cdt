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

package org.eclipse.cdt.ui.refactoring;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.refactoring.actions.UndoRefactoringAction;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * TODO: Provide description for "RefactoringRenameAction".
 */
public class UndoAction extends Action implements IViewActionDelegate {
//	private IViewPart fView;
//	private IAction fAction;
	UndoRefactoringAction refactoringAction = new UndoRefactoringAction();
		
//	protected IViewPart getView() {
//		return fView;
//	}

//	protected void setView(IViewPart view) {
//		fView = view;
//	}
//	protected IAction getAction() {
//		return fAction;
//	}

//	protected void setAction(IAction action) {
//		fAction = action;
//	}
	/**
	 * TODO: Implement the "RefactoringRenameAction" constructor.
	 */
	public UndoAction() {
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(IViewPart view) {
//		setView(view);
		refactoringAction.init(view.getSite().getWorkbenchWindow());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		refactoringAction.run(action);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		refactoringAction.selectionChanged(action, selection);
//		setAction(action);
//		if (!(selection instanceof IStructuredSelection)) {
//			return;
//		}
//		IStructuredSelection sel= (IStructuredSelection)selection;
//		Object o= sel.getFirstElement();
//		if (!(o instanceof ICElement)) {
//			return;
//		}
	}

//	private IStructuredSelection getSelection() {
//		return (IStructuredSelection)getView().getViewSite().getSelectionProvider().getSelection();
//	}
	
}

