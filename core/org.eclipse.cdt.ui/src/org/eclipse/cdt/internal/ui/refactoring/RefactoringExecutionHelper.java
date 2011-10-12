/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.ui.refactoring.RefactoringUI;
import org.eclipse.osgi.util.NLS;

import org.eclipse.cdt.internal.ui.actions.WorkbenchRunnableAdapter;

/**
 * A helper class to execute a refactoring. The class takes care of pushing the
 * undo change onto the undo stack and folding editor edits into one editor
 * undo object.
 */
public class RefactoringExecutionHelper {
	private final Refactoring fRefactoring;
	private final Shell fParent;
	private final IRunnableContext fExecContext;
	private final int fStopSeverity;
	private final int fSaveMode;

	private class Operation implements IWorkspaceRunnable {
		Change fChange;
		PerformChangeOperation fPerformChangeOperation;
		final boolean fForked;
		final boolean fForkChangeExecution;
		final boolean fCancelable;

		public Operation(boolean forked, boolean forkChangeExecution, boolean cancelable) {
			fForked= forked;
			fForkChangeExecution= forkChangeExecution;
			this.fCancelable = cancelable;
        }

		public void run(IProgressMonitor pm) throws CoreException {
			try {
				pm.beginTask("", fForked && !fForkChangeExecution ? 7 : 11); //$NON-NLS-1$
				pm.subTask(""); //$NON-NLS-1$

				final RefactoringStatus status= fRefactoring.checkAllConditions(
						new SubProgressMonitor(pm, 4, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
				if (status.getSeverity() >= fStopSeverity) {
					final boolean[] canceled= { false };
					if (fForked) {
						fParent.getDisplay().syncExec(new Runnable() {
							public void run() {
								canceled[0]= showStatusDialog(status);
							}
						});
					} else {
						canceled[0]= showStatusDialog(status);
					}
					if (canceled[0]) {
						throw new OperationCanceledException();
					}
				}

				fChange= fRefactoring.createChange(new SubProgressMonitor(pm, 2, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
				fChange.initializeValidationData(new SubProgressMonitor(pm, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));

				fPerformChangeOperation = createPerformChangeOperation(fChange);

				if (!fForked || fForkChangeExecution)
					fPerformChangeOperation.run(new SubProgressMonitor(pm, 4, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
			} finally {
				pm.done();
			}
		}

		/**
		 * @param status the status to show
		 * @return <code>true</code> iff the operation should be cancelled
		 */
		private boolean showStatusDialog(RefactoringStatus status) {
			Dialog dialog= RefactoringUI.createRefactoringStatusDialog(status, fParent, fRefactoring.getName(), false);
			return dialog.open() == IDialogConstants.CANCEL_ID;
		}
	}

	/**
	 * Creates a new refactoring execution helper.
	 *
	 * @param refactoring the refactoring
	 * @param stopSeverity a refactoring status constant from {@link RefactoringStatus}
	 * @param saveMode a save mode from {@link RefactoringSaveHelper}
	 * @param parent the parent shell
	 * @param context the runnable context
	 */
	public RefactoringExecutionHelper(Refactoring refactoring, int stopSeverity, int saveMode, Shell parent,
			IRunnableContext context) {
		super();
		Assert.isNotNull(refactoring);
		Assert.isNotNull(parent);
		Assert.isNotNull(context);
		fRefactoring= refactoring;
		fStopSeverity= stopSeverity;
		fParent= parent;
		fExecContext= context;
		fSaveMode= saveMode;
	}

	/**
	 * Must be called in the UI thread.
	 * @param fork if set, the operation will be forked
	 * @param cancelable  if set, the operation will be cancelable
	 * @throws InterruptedException thrown when the operation is canceled
	 * @throws InvocationTargetException thrown when the operation failed to execute
	 */
	public void perform(boolean fork, boolean cancelable) throws InterruptedException, InvocationTargetException {
		perform(fork, false, cancelable);
	}

	/**
	 * Must be called in the UI thread.<br>
	 * <strong>Use {@link #perform(boolean, boolean)} unless you know exactly what you are doing!</strong>
	 *
	 * @param fork if set, the operation will be forked
	 * @param forkChangeExecution if the change should not be executed in the UI thread: This may not work in any case
	 * @param cancelable  if set, the operation will be cancelable
	 * @throws InterruptedException thrown when the operation is canceled
	 * @throws InvocationTargetException thrown when the operation failed to execute
	 */
	public void perform(boolean fork, boolean forkChangeExecution, boolean cancelable)
			throws InterruptedException, InvocationTargetException {
		Operation operation = new Operation(fork, forkChangeExecution, cancelable);
		performOperation(operation, null, fork);
	}

	public void performChange(Change change, boolean fork)
			throws InterruptedException, InvocationTargetException {
		PerformChangeOperation operation = createPerformChangeOperation(change);
		performOperation(null, operation, fork);
	}

	/**
	 * Executes either a complete refactoring operation or a change operation. 
	 * @param operation The refactoring operation. Can be <code>null</code>.
	 * @param changeOperation The change operation. Has to be <code>null</code> if {@code operation}
	 *     is not <code>null</code> and not <code>null</code> otherwise.
	 * @param fork If set, the execution will be forked.
	 */
	private void performOperation(Operation operation, PerformChangeOperation changeOperation, boolean fork)
			throws InterruptedException, InvocationTargetException {
		Assert.isTrue((operation == null) != (changeOperation == null));
		Assert.isTrue(Display.getCurrent() != null);
		final IJobManager manager= Job.getJobManager();
		final ISchedulingRule rule = getSchedulingRule();
		try {
			try {
				Runnable r= new Runnable() {
					public void run() {
						manager.beginRule(rule, null);
					}
				};
				BusyIndicator.showWhile(fParent.getDisplay(), r);
			} catch (OperationCanceledException e) {
				throw new InterruptedException(e.getMessage());
			}

			RefactoringSaveHelper saveHelper= new RefactoringSaveHelper(fSaveMode);
			if (operation != null) {
				if (!saveHelper.saveEditors(fParent))
					throw new InterruptedException();
			}
			fRefactoring.setValidationContext(fParent);
			try {
				if (operation != null) {
					fExecContext.run(fork, operation.fCancelable, new WorkbenchRunnableAdapter(operation, rule, true));
					changeOperation = operation.fPerformChangeOperation;
					fork = fork && !operation.fForkChangeExecution;
				}
	
				if (changeOperation != null) {
					if (fork)
						fExecContext.run(false, false, new WorkbenchRunnableAdapter(changeOperation, rule, true));
					RefactoringStatus validationStatus= changeOperation.getValidationStatus();
					if (validationStatus != null && validationStatus.hasFatalError()) {
						MessageDialog.openError(fParent, fRefactoring.getName(),
								NLS.bind(Messages.RefactoringExecutionHelper_cannot_execute,
										validationStatus.getMessageMatchingSeverity(RefactoringStatus.FATAL)));
						throw new InterruptedException();
					}
				}
			} catch (InvocationTargetException e) {
				if (changeOperation != null && changeOperation.changeExecutionFailed()) {
					ChangeExceptionHandler handler= new ChangeExceptionHandler(fParent, fRefactoring);
					Throwable inner= e.getTargetException();
					if (inner instanceof RuntimeException) {
						handler.handle(changeOperation.getChange(), (RuntimeException)inner);
					} else if (inner instanceof CoreException) {
						handler.handle(changeOperation.getChange(), (CoreException)inner);
					} else {
						throw e;
					}
				} else {
					throw e;
				}
			} catch (OperationCanceledException e) {
				throw new InterruptedException(e.getMessage());
			} finally {
				saveHelper.triggerIncrementalBuild();
			}
		} finally {
			manager.endRule(rule);
			fRefactoring.setValidationContext(null);
		}
	}

	private ISchedulingRule getSchedulingRule() {
		if (fRefactoring instanceof IScheduledRefactoring) {
			return ((IScheduledRefactoring) fRefactoring).getSchedulingRule();
		} else {
			return ResourcesPlugin.getWorkspace().getRoot();
		}
	}

	private PerformChangeOperation createPerformChangeOperation(Change change) {
		PerformChangeOperation operation = new PerformChangeOperation(change);
		operation.setUndoManager(RefactoringCore.getUndoManager(), fRefactoring.getName());
		if (fRefactoring instanceof IScheduledRefactoring)
			operation.setSchedulingRule(((IScheduledRefactoring) fRefactoring).getSchedulingRule());
		return operation;
	}
}
