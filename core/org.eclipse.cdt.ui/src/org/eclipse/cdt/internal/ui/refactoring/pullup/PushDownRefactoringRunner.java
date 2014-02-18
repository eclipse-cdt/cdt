/*******************************************************************************
 * Copyright (c) 2013 Simon Taddiken
 * University of Bremen.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *     Simon Taddiken (University of Bremen)
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.pullup;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.IShellProvider;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;

import org.eclipse.cdt.internal.ui.refactoring.RefactoringRunner;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringSaveHelper;
import org.eclipse.cdt.internal.ui.refactoring.pushdown.ui.PushDownWizard;



public class PushDownRefactoringRunner extends RefactoringRunner {

	public PushDownRefactoringRunner(ICElement element, ISelection selection, 
			IShellProvider shellProvider, ICProject cProject) {
		super(element, selection, shellProvider, cProject);
	}

	

	@Override
	public void run() {
		final PushDownRefactoring refactoring = new PushDownRefactoring(
				this.element, this.selection, this.project);
		final PushDownWizard wizard = new PushDownWizard(refactoring);
		run(wizard, refactoring, RefactoringSaveHelper.SAVE_NOTHING);
	}
}
