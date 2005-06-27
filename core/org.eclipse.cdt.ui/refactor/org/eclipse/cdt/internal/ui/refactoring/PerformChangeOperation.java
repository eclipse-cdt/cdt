/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.IRewriteTarget;
import org.eclipse.jface.util.Assert;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.core.resources.IWorkspaceRunnable;

import org.eclipse.ui.IEditorPart;

import org.eclipse.cdt.core.model.CoreModel;

import org.eclipse.cdt.internal.corext.refactoring.base.ChangeAbortException;
import org.eclipse.cdt.internal.corext.refactoring.base.ChangeContext;
import org.eclipse.cdt.internal.corext.refactoring.base.IChange;
import org.eclipse.cdt.internal.corext.refactoring.base.RefactoringStatus;

import org.eclipse.cdt.internal.ui.ICStatusConstants;
import org.eclipse.cdt.ui.CUIPlugin;

/**
 * Operation that, when performed, performes a change to the workbench.
 */
public class PerformChangeOperation implements IRunnableWithProgress {

	private IChange fChange;
	private ChangeContext fChangeContext;
	private CreateChangeOperation fCreateChangeOperation;
	private int fCheckPassedSeverity;
	private boolean fChangeExecuted;
	
	/**
	 * Creates a new perform change operation instance for the given change.
	 * 
	 * @param change the change to be applied to the workbench
	 */
	public PerformChangeOperation(IChange change) {
		fChange= change;
		Assert.isNotNull(fChange);
	}

	/**
	 * Creates a new perform change operation for the given create change operation. 
	 * When executed, a new change is created using the passed instance.
	 * 
	 * @param op the <code>CreateChangeOperation</code> used to create a new
	 *  change object
	 */
	public PerformChangeOperation(CreateChangeOperation op) {
		fCreateChangeOperation= op;
		Assert.isNotNull(fCreateChangeOperation);
		fCheckPassedSeverity= RefactoringStatus.INFO;
	}

	/**
	 * Returns <code>true</code> if the change has been executed. Otherwise <code>false</code>
	 * is returned.
	 * 
	 * @return <code>true</code> if the change has been executed, otherwise
	 *  <code>false</code>
	 */
	public boolean changeExecuted() {
		return fChangeExecuted;
	}
	
	/**
	 * Returns the change used by this operation. This is either the change passed to
	 * the constructor or the one create by the <code>CreateChangeOperation</code>.
	 * Method returns <code>null</code> if the create operation did not create
	 * the change.
	 * 
	 * @return the change used by this operation or <code>null</code> if no change
	 *  has been created
	 */
	public IChange getChange() {
		return fChange;
	}
	
	public RefactoringStatus getStatus() {
		return fCreateChangeOperation != null ? fCreateChangeOperation.getStatus() : null;
	}
	 
	/**
	 * Sets the check passed severity value. This value is used to deceide whether the 
	 * condition check, executed if a change is to be created, is interpreted as passed 
	 * or not. The condition check is considered to be passed if the refactoring status's 
	 * severity is less or equal the given severity. The given value must be smaller
	 * than <code>RefactoringStatus.FATAL</code>.
	 * 
	 * @param severity the severity value considered to be "ok".
	 */
	public void setCheckPassedSeverity(int severity) {
		fCheckPassedSeverity= severity;
		Assert.isTrue (fCheckPassedSeverity < RefactoringStatus.FATAL);
	}
	
	/**
	 * Sets the change context used to execute the change. The given context is passed
	 * to the method <code>IChange.perform</code>.
	 * 
	 * @param context the change context to use
	 * @see IChange#perform(ChangeContext, IProgressMonitor)
	 */
	public void setChangeContext(ChangeContext context) {
		fChangeContext= context;
		Assert.isNotNull(fChangeContext);
	}
	
	/* (non-Javadoc)
	 * Method declard in IRunnableWithProgress
	 */
	public void run(IProgressMonitor pm) throws InvocationTargetException, InterruptedException {
		try{
			fChangeExecuted= false;
			if (createChange()) {
				pm.beginTask("", 2); //$NON-NLS-1$
				fCreateChangeOperation.run(new SubProgressMonitor(pm, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
				fChange= fCreateChangeOperation.getChange();
				RefactoringStatus status= fCreateChangeOperation.getStatus();
				int conditionCheckingStyle= fCreateChangeOperation.getConditionCheckingStyle();
				if (fChange != null && 
						(conditionCheckingStyle == CreateChangeOperation.CHECK_NONE ||
				     	 status != null && status.getSeverity() <= fCheckPassedSeverity)) {
					executeChange(new SubProgressMonitor(pm, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
					fChangeExecuted= true;
				} else {
					pm.worked(1);
				}
			} else {
				executeChange(pm);
				fChangeExecuted= true;
			}
		} catch (CoreException e) {
			throw new InvocationTargetException(e);
		} finally{
			pm.done();
		}	
	}
	
	private void executeChange(IProgressMonitor pm) throws CoreException {
		Assert.isNotNull(fChangeContext);
		IRewriteTarget[] targets= null;
		try {
			targets= getRewriteTargets();
			beginCompoundChange(targets);
			// Since we have done precondition checking this check should be fast. No PM.
			// PR: 1GEWDUH: ITPJCORE:WINNT - Refactoring - Unable to undo refactor change
			fChange.aboutToPerform(fChangeContext, new NullProgressMonitor());
			IWorkspaceRunnable runnable= new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					try {
						fChange.perform(fChangeContext, monitor);
					} catch (ChangeAbortException e) {
						throw new CoreException(
							new Status(
								IStatus.ERROR, 
								CUIPlugin.getPluginId(), ICStatusConstants.CHANGE_ABORTED, 
								RefactoringMessages.getString("PerformChangeOperation.unrecoverable_error"), e)); //$NON-NLS-1$
					}
				}
			};
			CoreModel.run(runnable, pm);
		} finally {
			fChange.performed();
			if (targets != null)
				endCompoundChange(targets);
		}
	}
	
	private boolean createChange() {
		return fCreateChangeOperation != null;
	}
	
	private static void beginCompoundChange(IRewriteTarget[] targets) {
		for (int i= 0; i < targets.length; i++) {
			targets[i].beginCompoundChange();
		}
	}
	
	private static void endCompoundChange(IRewriteTarget[] targets) {
		for (int i= 0; i < targets.length; i++) {
			targets[i].endCompoundChange();
		}
	}
	
	private static IRewriteTarget[] getRewriteTargets() {
		IEditorPart[] editors= CUIPlugin.getInstanciatedEditors();
		List result= new ArrayList(editors.length);
		for (int i= 0; i < editors.length; i++) {
			IRewriteTarget target= (IRewriteTarget)editors[i].getAdapter(IRewriteTarget.class);
			if (target != null) {
				result.add(target);
			}
		}
		return (IRewriteTarget[]) result.toArray(new IRewriteTarget[result.size()]);
	}
}

