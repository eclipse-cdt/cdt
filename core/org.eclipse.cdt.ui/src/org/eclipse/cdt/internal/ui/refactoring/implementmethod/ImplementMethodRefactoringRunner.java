/*******************************************************************************
 * Copyright (c) 2008, 2009 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.implementmethod;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.refactoring.RefactoringRunner;

/**
 * @author Lukas Felber
 */
public class ImplementMethodRefactoringRunner extends RefactoringRunner {

	public ImplementMethodRefactoringRunner(IFile file, ISelection selection, ICElement element,
			IShellProvider shellProvider, ICProject cProject) {
		super(file, selection, element, shellProvider, cProject);
	}

	@Override
	public void run() {
		ImplementMethodRefactoring refactoring = new ImplementMethodRefactoring(file, selection, celement, project);
		ImplementMethodRefactoringWizard wizard = new ImplementMethodRefactoringWizard(refactoring);
		RefactoringWizardOpenOperation operator = new RefactoringWizardOpenOperation(wizard);

		try {
			refactoring.lockIndex();
			try {
				operator.run(shellProvider.getShell(), refactoring.getName());
			} finally {
				refactoring.unlockIndex();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
	}
}
