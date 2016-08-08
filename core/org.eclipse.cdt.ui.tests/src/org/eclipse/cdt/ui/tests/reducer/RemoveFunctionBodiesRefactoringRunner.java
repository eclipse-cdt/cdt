/*******************************************************************************
 * Copyright (c) 2016 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.reducer;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.IShellProvider;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;

import org.eclipse.cdt.internal.ui.refactoring.RefactoringRunner;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringSaveHelper;

public class RemoveFunctionBodiesRefactoringRunner extends RefactoringRunner  {

	public RemoveFunctionBodiesRefactoringRunner(ICElement element, ISelection selection,
			IShellProvider shellProvider, ICProject cProject) {
		super(element, selection, shellProvider, cProject);
	}

	@Override
	public void run() {
		RemoveFunctionBodiesRefactoring refactoring =
				new RemoveFunctionBodiesRefactoring(element, selection, project);
		RemoveFunctionBodiesWizard wizard = new RemoveFunctionBodiesWizard(refactoring);
		run(wizard, refactoring, RefactoringSaveHelper.SAVE_NOTHING);
	}
}
