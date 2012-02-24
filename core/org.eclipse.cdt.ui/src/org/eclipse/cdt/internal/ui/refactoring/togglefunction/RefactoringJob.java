/*******************************************************************************
 * Copyright (c) 2011, 2012 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.togglefunction;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.IUndoManager;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;

class RefactoringJob extends Job {
	public final static Object FAMILY_TOGGLE_DEFINITION = new Object();
	private final ToggleRefactoring refactoring;
	
	RefactoringJob(ToggleRefactoring refactoring) {
		super("Toggle Function Definition code automation"); //$NON-NLS-1$
		this.refactoring = refactoring;
		setPriority(Job.SHORT);
	}
	
	@Override
	public boolean belongsTo(Object family) {
		return family == FAMILY_TOGGLE_DEFINITION;
	}
	
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		CRefactoringContext context = new CRefactoringContext(refactoring);
		IUndoManager undoManager = RefactoringCore.getUndoManager();
		Change change = new NullChange();
		Change undoChange = new NullChange();
		boolean success = false;
		try {
			RefactoringStatus status = refactoring.checkAllConditions(monitor);
			if (status.hasFatalError())
				return new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, status.getMessageMatchingSeverity(RefactoringStatus.ERROR));
			change = refactoring.createChange(monitor);
			change.initializeValidationData(monitor);
			if (!change.isValid(monitor).isOK()) {
				return Status.CANCEL_STATUS;
			}
			undoManager.aboutToPerformChange(change);
			undoChange = change.perform(monitor);
			success = true;
		} catch (IllegalStateException e) {
			CUIPlugin.log("Another refactoring is still in progress, aborting.", e); //$NON-NLS-1$
		} catch (CoreException e) {
			CUIPlugin.log("Failure during generation of changes.", e); //$NON-NLS-1$
		} finally {
			context.dispose();
			undoChange.initializeValidationData(monitor);
			undoManager.changePerformed(change, success);
			try {
				if (success && undoChange.isValid(monitor).isOK()) {
					// Note: addUndo MUST be called AFTER changePerformed or
					// the change won't be unlocked correctly. (17.11.2010)
					undoManager.addUndo(Messages.RefactoringJob_UndoName, undoChange);
				}
			} catch (OperationCanceledException e) {
			} catch (CoreException e) {
			}
		}
		return Status.OK_STATUS;
	}
}