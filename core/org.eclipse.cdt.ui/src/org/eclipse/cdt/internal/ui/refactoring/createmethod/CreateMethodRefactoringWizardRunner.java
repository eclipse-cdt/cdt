/*******************************************************************************
 * Copyright (c) 2013 - Xdin AB
 * This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Erik Johansson
 ******************************************************************************/

package org.eclipse.cdt.internal.ui.refactoring.createmethod;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringRunner;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringSaveHelper;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.IShellProvider;

@SuppressWarnings("restriction")
public class CreateMethodRefactoringWizardRunner extends RefactoringRunner {

	private IMarker marker;
	
	public CreateMethodRefactoringWizardRunner(ICElement element, ISelection selection,
			IShellProvider shellProvider, ICProject cProject, IMarker marker) {
		super(element, selection, shellProvider, cProject);
		this.marker = marker;
	}

	@Override
	public void run() {
		CreateMethodRefactoring refactoring =
				new CreateMethodRefactoring(element, null, element.getCProject(), marker);
		CreateMethodRefactoringWizard wizard = new CreateMethodRefactoringWizard(refactoring);
		run(wizard, refactoring, RefactoringSaveHelper.SAVE_NOTHING);
		if (refactoring.shouldOpenLocation())
			Helpers.openFileLocationInEditor(refactoring.getInsertionPointFileName(), refactoring.getInsertionPointOffset());

	}
}
