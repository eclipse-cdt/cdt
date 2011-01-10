/*******************************************************************************
 * Copyright (c) 2008, 2011 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * 	   Institute for Software - initial API and implementation
 * 	   Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.gettersandsetters;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;

import org.eclipse.cdt.internal.ui.refactoring.RefactoringRunner2;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringASTCache;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringSaveHelper;

/**
 * @author Thomas Corbat
 */
public class GenerateGettersAndSettersRefactoringRunner extends RefactoringRunner2 {

	public GenerateGettersAndSettersRefactoringRunner(ICElement element, ISelection selection,
			IShellProvider shellProvider, ICProject cProject) {
		super(element, selection, shellProvider, cProject);
	}

	@Override
	public void run(RefactoringASTCache astCache) {
		if (getActiveEditor() instanceof ITextEditor) {
			GenerateGettersAndSettersRefactoring refactoring =
					new GenerateGettersAndSettersRefactoring(element, selection, project, astCache);
			GenerateGettersAndSettersRefactoringWizard wizard =
					new GenerateGettersAndSettersRefactoringWizard(refactoring);
			starter.activate(wizard, shellProvider.getShell(), refactoring.getName(),
					RefactoringSaveHelper.SAVE_REFACTORING);
		}
	}

	private IEditorPart getActiveEditor() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
	}
}
