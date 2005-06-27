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

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.IRewriteTarget;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.core.resources.IWorkspaceRunnable;

import org.eclipse.ui.IEditorPart;

import org.eclipse.cdt.core.model.CoreModel;

import org.eclipse.cdt.internal.corext.refactoring.base.ChangeAbortException;
import org.eclipse.cdt.internal.corext.refactoring.base.ChangeContext;
import org.eclipse.cdt.internal.corext.refactoring.base.IChange;
import org.eclipse.cdt.internal.corext.refactoring.base.IRefactoring;
import org.eclipse.cdt.internal.corext.refactoring.base.IUndoManager;
import org.eclipse.cdt.internal.corext.refactoring.base.Refactoring;
import org.eclipse.cdt.internal.corext.refactoring.base.RefactoringStatus;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.internal.ui.refactoring.AbortChangeExceptionHandler;
import org.eclipse.cdt.internal.ui.refactoring.ChangeExceptionHandler;
import org.eclipse.cdt.internal.ui.util.ExceptionHandler;

/**
 * A helper class to execute a refactoring. The class takes care of pushing the
 * undo change onto the undo stack and folding editor edits into one editor
 * undo object.
 */
public class RefactoringExecutionHelper {

	private final IRefactoring fRefactoring;
	private final Shell fParent;
	private final IRunnableContext fExecContext;
	private final int fStopSeverity;
	private final boolean fNeedsSavedEditors;
	private ChangeContext fContext;

	private class Operation implements IRunnableWithProgress {
		public IChange fChange;
		public void run(IProgressMonitor pm) throws InvocationTargetException, InterruptedException {
			try {
				pm.beginTask("", 10); //$NON-NLS-1$
				pm.subTask(""); //$NON-NLS-1$
				RefactoringStatus status= fRefactoring.checkPreconditions(new SubProgressMonitor(pm, 4, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
				if (status.getSeverity() >= fStopSeverity) {
					RefactoringStatusDialog dialog= new RefactoringStatusDialog(fParent, status, fRefactoring.getName(), false);
					if(dialog.open() == IDialogConstants.CANCEL_ID) {
						throw new InterruptedException();
					}
				}
				fChange= fRefactoring.createChange(new SubProgressMonitor(pm, 2, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
				try {
					fChange.aboutToPerform(fContext, new NullProgressMonitor());
					CoreModel.run(new IWorkspaceRunnable() {
						public void run(IProgressMonitor monitor) throws CoreException {
							fChange.perform(fContext, monitor);
						}
					}, new SubProgressMonitor(pm, 4, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
				} finally {
					fChange.performed();
				}
			} catch (ChangeAbortException e) {
				throw new InvocationTargetException(e);
		    } catch (CoreException e) {
				throw new InvocationTargetException(e);
			} finally {
				pm.done();
			}
		}
		public boolean isExecuted() {
			return fChange != null;
		}
		public boolean isUndoable() {
			return fChange.isUndoable();
		}
		public IChange getUndoChange() {
			return fChange.getUndoChange();
		}
	}
	
	public RefactoringExecutionHelper(IRefactoring refactoring, int stopSevertity, boolean needsSavedEditors, Shell parent, IRunnableContext context) {
		super();
		Assert.isNotNull(refactoring);
		Assert.isNotNull(parent);
		Assert.isNotNull(context);
		fRefactoring= refactoring;
		fStopSeverity= stopSevertity;
		fParent= parent;
		fExecContext= context;
		fNeedsSavedEditors= needsSavedEditors;
	}
	
	public void perform() throws InterruptedException, InvocationTargetException {
		RefactoringSaveHelper saveHelper= new RefactoringSaveHelper();
		if (fNeedsSavedEditors && !saveHelper.saveEditors(fParent))
			throw new InterruptedException();
		fContext= new ChangeContext(new ChangeExceptionHandler(fParent));
		boolean success= false;
		IUndoManager undoManager= Refactoring.getUndoManager();
		Operation op= new Operation();
		IRewriteTarget[] targets= null;
		try{
			targets= getRewriteTargets();
			beginCompoundChange(targets);
			undoManager.aboutToPerformRefactoring();
			fExecContext.run(false, false, op);
			if (op.isExecuted()) {
				if (!op.isUndoable()) {
					success= false;
				} else { 
					undoManager.addUndo(fRefactoring.getName(), op.getUndoChange());
					success= true;
				}
			} 
		} catch (InvocationTargetException e) {
			Throwable t= e.getTargetException();
			if (t instanceof ChangeAbortException) {
				handleChangeAbortException((ChangeAbortException)t);
			} else {
				throw e;
			}
		} finally {
			fContext.clearPerformedChanges();
			undoManager.refactoringPerformed(success);
			saveHelper.triggerBuild();
			if (targets != null)
				endCompoundChange(targets);
		}
	}
	
	private void handleChangeAbortException(ChangeAbortException exception) {
		if (!fContext.getTryToUndo())
			return;
			
		IRunnableWithProgress op= new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
				try {
					CoreModel.run(new IWorkspaceRunnable() {
						public void run(IProgressMonitor pm) throws CoreException {
							ChangeContext undoContext= new ChangeContext(new AbortChangeExceptionHandler());
							IChange[] changes= fContext.getPerformedChanges();
							pm.beginTask(RefactoringMessages.getString("RefactoringWizard.undoing"), changes.length); //$NON-NLS-1$
							IProgressMonitor sub= new NullProgressMonitor();
							for (int i= changes.length - 1; i >= 0; i--) {
								IChange change= changes[i];
								pm.subTask(change.getName());
								change.getUndoChange().perform(undoContext, sub);
								pm.worked(1);
							}
						}
					}, monitor);
				} catch (ChangeAbortException e) {
					throw new InvocationTargetException(e.getThrowable());
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				} 
			}
		};
		
		try {
			fExecContext.run(false, false, op);
		} catch (InvocationTargetException e) {
			handleUnexpectedException(e);
		} catch (InterruptedException e) {
			// not possible. Operation not cancelable.
		}
	}
	
	private void handleUnexpectedException(InvocationTargetException e) {
		ExceptionHandler.handle(e, RefactoringMessages.getString("RefactoringWizard.refactoring"), RefactoringMessages.getString("RefactoringWizard.unexpected_exception_1")); //$NON-NLS-2$ //$NON-NLS-1$
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
