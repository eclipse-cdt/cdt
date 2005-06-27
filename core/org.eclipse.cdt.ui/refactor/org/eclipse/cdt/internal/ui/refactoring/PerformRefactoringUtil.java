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

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.core.resources.IWorkspaceRunnable;

import org.eclipse.cdt.core.model.CoreModel;

import org.eclipse.cdt.internal.corext.refactoring.base.ChangeAbortException;
import org.eclipse.cdt.internal.corext.refactoring.base.ChangeContext;
import org.eclipse.cdt.internal.corext.refactoring.base.IChange;
import org.eclipse.cdt.internal.corext.refactoring.base.IUndoManager;
import org.eclipse.cdt.internal.corext.refactoring.base.Refactoring;

import org.eclipse.cdt.internal.ui.ICStatusConstants;
import org.eclipse.cdt.internal.ui.util.ExceptionHandler;

public class PerformRefactoringUtil {

	//no instances
	private PerformRefactoringUtil(){
	}

	public static boolean performRefactoring(PerformChangeOperation op, Refactoring refactoring, IRunnableContext execContext, Shell parent) {
		ChangeContext context= new ChangeContext(new ChangeExceptionHandler(parent));
		boolean success= false;
		IUndoManager undoManager= Refactoring.getUndoManager();
		try{
			op.setChangeContext(context);
			undoManager.aboutToPerformRefactoring();
			execContext.run(false, false, op);
			if (op.changeExecuted()) {
				if (! op.getChange().isUndoable()){
					success= false;
				} else { 
					undoManager.addUndo(refactoring.getName(), op.getChange().getUndoChange());
					success= true;
				}	
			}
		} catch (InvocationTargetException e) {
			Throwable t= e.getTargetException();
			if (t instanceof CoreException) {
				IStatus status= ((CoreException)t).getStatus();
				if (status != null && status.getCode() == ICStatusConstants.CHANGE_ABORTED && status.getPlugin().equals(status.getPlugin())) {
					success= handleChangeAbortException(execContext, context);
					return true;
				}
			}
			handleUnexpectedException(e);
			return false;
		} catch (InterruptedException e) {
			return false;
		} finally {
			context.clearPerformedChanges();
			undoManager.refactoringPerformed(success);
		}
		
		return true;
	}
	
	private static boolean handleChangeAbortException(IRunnableContext execContext, final ChangeContext context) {
		if (!context.getTryToUndo())
			return false; // Return false since we handle an unexpected exception and we don't have any
						  // idea in which state the workbench is.
			
		IRunnableWithProgress op= new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
				try {
					CoreModel.run(new IWorkspaceRunnable() {
						public void run(IProgressMonitor pm) throws CoreException {
							ChangeContext undoContext= new ChangeContext(new AbortChangeExceptionHandler());
							IChange[] changes= context.getPerformedChanges();
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
			execContext.run(false, false, op);
		} catch (InvocationTargetException e) {
			handleUnexpectedException(e);
			return false;
		} catch (InterruptedException e) {
			// not possible. Operation not cancelable.
		}
		
		return true;
	}
	
	private static void handleUnexpectedException(InvocationTargetException e) {
		ExceptionHandler.handle(e, RefactoringMessages.getString("RefactoringWizard.refactoring"), RefactoringMessages.getString("RefactoringWizard.unexpected_exception_1")); //$NON-NLS-2$ //$NON-NLS-1$
	}
}
