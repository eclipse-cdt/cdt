/*******************************************************************************
 * Copyright (c) 2011 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * 		Martin Schwab & Thomas Kallenberg - initial API and implementation 
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.togglefunction;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.window.IShellProvider;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.refactoring.RefactoringRunner;

/**
 * Responsible for scheduling a job which runs the ToggleRefactoring. Differs
 * from other subclasses of RefactoringRunner in the way that it does not use a
 * wizard but calls the refactoring directly.
 */
public class ToggleRefactoringRunner extends RefactoringRunner {

	private ToggleRefactoring refactoring;

	public ToggleRefactoringRunner(IFile file, ITextSelection selection,
			ICElement element, IShellProvider shellProvider, ICProject project) {
		super(file, selection, element, shellProvider, project);
		refactoring = new ToggleRefactoring(file, selection, project);
	}

	@Override
	public void run() {
		Job[] jobs = Job.getJobManager().find(RefactoringJob.FAMILY_TOGGLE_DEFINITION);
		if (jobs.length > 0) {
			CUIPlugin.log("no concurrent toggling allowed", new NotSupportedException(""));  //$NON-NLS-1$//$NON-NLS-2$
			return;
		}
		new RefactoringJob(refactoring).schedule();
	}
}