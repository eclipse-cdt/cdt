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

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.window.IShellProvider;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.refactoring.RefactoringRunner;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringSaveHelper;

/**
 * Responsible for scheduling a job which runs the ToggleRefactoring. Differs
 * from other subclasses of RefactoringRunner in the way that it does not use a
 * wizard but calls the refactoring directly.
 */
public class ToggleRefactoringRunner extends RefactoringRunner {

	public ToggleRefactoringRunner(ICElement element, ITextSelection selection,
			IShellProvider shellProvider, ICProject project) {
		super(element, selection, shellProvider, project);
	}

	@Override
	public void run() {
		Job[] jobs = Job.getJobManager().find(RefactoringJob.FAMILY_TOGGLE_DEFINITION);
		if (jobs.length > 0) {
			CUIPlugin.log("No concurrent toggling allowed", new NotSupportedException(""));  //$NON-NLS-1$//$NON-NLS-2$
			return;
		}
		RefactoringSaveHelper saveHelper= new RefactoringSaveHelper(RefactoringSaveHelper.SAVE_REFACTORING);
		if (!saveHelper.saveEditors(shellProvider.getShell()))
			return;

		ToggleRefactoring refactoring =
				new ToggleRefactoring(element, (ITextSelection) selection, project);
		new RefactoringJob(refactoring).schedule();
	}
}