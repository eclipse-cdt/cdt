/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.hidemethod;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringRunner;

/**
 * @author Guido Zgraggen IFS
 *
 */
public class HideMethodRefactoringRunner extends RefactoringRunner {

	public HideMethodRefactoringRunner(IFile file, ISelection selection, ICElement element, IShellProvider shellProvider) {
		super(file, selection, element, shellProvider);
	}


	@Override
	public void run() {
		CRefactoring refactoring= new HideMethodRefactoring(file, selection, celement);
		HideMethodRefactoringWizard wizard = new HideMethodRefactoringWizard(refactoring);
		RefactoringWizardOpenOperation operator = new RefactoringWizardOpenOperation(wizard);

		try {
			refactoring.lockIndex();
			try {
				operator.run(shellProvider.getShell(), refactoring.getName());
			}
			finally {
				refactoring.unlockIndex();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
	}
}
