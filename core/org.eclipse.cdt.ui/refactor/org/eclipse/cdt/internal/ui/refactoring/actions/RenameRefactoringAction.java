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
package org.eclipse.cdt.internal.ui.refactoring.actions;


import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.model.CElement;
import org.eclipse.cdt.internal.corext.refactoring.RenameRefactoring;
import org.eclipse.cdt.internal.ui.actions.SelectionConverter;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringMessages;
import org.eclipse.cdt.internal.ui.refactoring.UserInterfaceStarter;
import org.eclipse.cdt.internal.ui.util.ExceptionHandler;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IWorkingCopyManager;
import org.eclipse.cdt.ui.actions.SelectionDispatchAction;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchSite;


public class RenameRefactoringAction extends SelectionDispatchAction {

	private CEditor fEditor;

	public RenameRefactoringAction(CEditor editor) {
		this(editor.getEditorSite());
		fEditor= editor;
	}

	public RenameRefactoringAction(IWorkbenchSite site) {
		super(site);
		setText(RefactoringMessages.getString("RenameRefactoringAction.text"));//$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.ui.actions.SelectionDispatchAction#selectionChanged(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void selectionChanged(IStructuredSelection selection) {
		setEnabled(selection.size() == 1);
	}

	public void selectionChanged(ITextSelection selection) {
		boolean enable = true;
		IWorkingCopyManager manager = CUIPlugin.getDefault().getWorkingCopyManager();
		ICElement element = manager.getWorkingCopy(fEditor.getEditorInput());
		if((element == null) || (element instanceof ITranslationUnit)){
			setEnabled(false);
			return;
		}
		ITextSelection textSelection= (ITextSelection)fEditor.getSelectionProvider().getSelection();
		
		if (textSelection == null) {
			setEnabled(false);
			return;
		}
		
		if( (((CElement)element).getIdStartPos() != textSelection.getOffset()) 
		|| (((CElement)element).getIdLength() != textSelection.getLength())) {
			enable = false;
		}
		setEnabled(enable);
	}

	public void run(ITextSelection selection) {
		try {
			Object element= SelectionConverter.getElementAtOffset(fEditor);
			RenameRefactoring refactoring= new RenameRefactoring(element);
			run(refactoring, getShell());
		} catch (CoreException e) {
			ExceptionHandler.handle(e, getShell(), RefactoringMessages.getString("RenameRefactoringAction.label"), //$NON-NLS-1$
					RefactoringMessages.getString("RenameRefactoringAction.unexpected_exception"));//$NON-NLS-1$
		}
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
			ExceptionHandler.handle(e, getShell(), RefactoringMessages.getString("RenameRefactoringAction.label"), //$NON-NLS-1$
					RefactoringMessages.getString("RenameRefactoringAction.unexpected_exception"));//$NON-NLS-1$
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
			MessageDialog.openInformation(parent, RefactoringMessages.getString("RenameRefactoringAction.label"), //$NON-NLS-1$
					RefactoringMessages.getString("RenameRefactoringAction.no_refactoring_available")//$NON-NLS-1$
					);
		}		
	}
}
