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
package org.eclipse.cdt.internal.ui.refactoring.gettersandsetters;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;

import org.eclipse.cdt.internal.ui.refactoring.RefactoringRunner;

/**
 * @author Thomas Corbat
 *
 */
public class GenerateGettersAndSettersRefactoringRunner extends RefactoringRunner {

	public GenerateGettersAndSettersRefactoringRunner(IFile file, ISelection selection,
			ICElement elem, IShellProvider shellProvider, ICProject cProject) {
		super(file, selection, elem, shellProvider, cProject);
	}

	@Override
	public void run() {
		if (PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor() instanceof ITextEditor) {
			GenerateGettersAndSettersRefactoring refactoring = new GenerateGettersAndSettersRefactoring(file, selection, celement, project);
			GenerateGettersAndSettersRefactoringWizard wizard = new GenerateGettersAndSettersRefactoringWizard(refactoring);
			RefactoringWizardOpenOperation operator = new RefactoringWizardOpenOperation(wizard);

			try {
				operator.run(shellProvider.getShell(), refactoring.getName());
			} catch (InterruptedException e) {
				//initial condition checking got canceled by the user.
			}
		}
	}
}
