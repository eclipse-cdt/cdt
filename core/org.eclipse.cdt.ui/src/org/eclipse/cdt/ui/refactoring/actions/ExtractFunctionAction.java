/*******************************************************************************
 * Copyright (c) 2008, 2009 Institute for Software, HSR Hochschule fuer Technik  
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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.IShellProvider;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IWorkingCopy;

import org.eclipse.cdt.internal.ui.refactoring.extractfunction.ExtractFunctionRefactoringRunner;

/**
 *
 * @since 5.0
 * @author Emanuel Graf IFS
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ExtractFunctionAction extends RefactoringAction {

	public ExtractFunctionAction() {
		super(Messages.ExtractFunctionAction_label);
	}

	@Override
	public void run(IShellProvider shellProvider, ICElement elem) {
	}

	@Override
	public void run(IShellProvider shellProvider, IWorkingCopy wc,
			ITextSelection s) {
		IResource res = wc.getResource();
		if (res instanceof IFile) {
			final ISelection selection = fEditor.getSelectionProvider().getSelection();
			new ExtractFunctionRefactoringRunner((IFile) res, selection, fEditor.getSite()).run();
		}
	}

    @Override
	public void updateSelection(ICElement elem) {
		super.updateSelection(elem);
		setEnabled(false);
	}

}
