/*******************************************************************************
 * Copyright (c) 2011, 2012 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * 	   Martin Schwab & Thomas Kallenberg - initial API and implementation
 *     Sergey Prigogin (Google) 
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.togglefunction;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;

/**
 * Represents the interface between the user who invokes the action and the
 * actual refactoring mechanism. Starts the ToggleRefactoringRunner.
 * 
 * Order of execution is: constructor, init, selectionChanged, run
 */
public class TogglingActionDelegate implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;
	private TextSelection selection;
	
	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;
		assert (window != null);
	}
	
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		boolean isTextSelection = selection != null && selection instanceof TextSelection;
		action.setEnabled(isTextSelection);
		if (!isTextSelection)
			return;
		// Get our own selection due to (a possible) bug?
		this.selection = (TextSelection) CUIPlugin.getActivePage().getActiveEditor().getEditorSite().getSelectionProvider().getSelection();
	}

	@Override
	public void run(IAction action) {
		IWorkbenchPage activePage = window.getActivePage();
		if (activePage == null)
			return;
		IEditorPart editor = activePage.getActiveEditor();
		if (editor == null || editor.getEditorInput() == null)
			return;
		ITranslationUnit tu =
				CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(editor.getEditorInput());
		if (tu == null || tu.getResource() == null)
			return;
		new ToggleRefactoringRunner(tu, selection, window, tu.getCProject()).run();
	}

	@Override
	public void dispose() {
	}
}
