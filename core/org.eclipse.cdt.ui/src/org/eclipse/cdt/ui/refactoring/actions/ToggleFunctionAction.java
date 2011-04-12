/*******************************************************************************
 * Copyright (c) 2011 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Institute for Software (IFS)- initial API and implementation 
 ******************************************************************************/
package org.eclipse.cdt.ui.refactoring.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.refactoring.togglefunction.ToggleRefactoringRunner;

/**
 * 
 * @since 5.3
 * @author Emanuel Graf IFS
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ToggleFunctionAction extends RefactoringAction {
	private ICProject project;
	private IFile file;

	public ToggleFunctionAction() {
		super(Messages.ToggleFunctionAction_label);
	}

	@Override
	public void run(IShellProvider shellProvider, ICElement elem) {
	}

	@Override
	public void run(IShellProvider shellProvider, IWorkingCopy wc,
			ITextSelection s) {
		IResource res = wc.getResource();
		if (isWorkbenchReady(wc) && res instanceof IFile) {
			new ToggleRefactoringRunner(file, s, project, shellProvider, project).run();
		}
	}

	private boolean isWorkbenchReady(IWorkingCopy wc) {
		try {
			IWorkbenchPage activePage = CUIPlugin.getActivePage();
			if (activePage == null)
				return false;
			IEditorPart editor = activePage.getActiveEditor();
			if (editor == null || editor.getEditorInput() == null)
				return false;
			if (wc == null)
				return false;
			project = wc.getCProject();
			file = (IFile) wc.getResource();
			return project != null && file != null;
		} catch (ClassCastException e) {
			return false;
		}
	}

    @Override
	public void updateSelection(ICElement elem) {
		super.updateSelection(elem);
		setEnabled(false);
	}

}
