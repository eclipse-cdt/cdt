/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.cdt.internal.ui.actions.WorkbenchRunnableAdapter;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * A helper class to execute a refactoring. The class takes care of pushing the
 * undo change onto the undo stack and folding editor edits into one editor
 * undo object.
 */
public class RefactoringExecutionHelper {
	private final Refactoring fRefactoring;
	private final Shell fParent;
	private final IRunnableContext fExecContext;
	private final int fSaveMode;

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
		fRefactoring = refactoring;
		fParent = parent;
		fExecContext = context;
		fSaveMode = saveMode;
	}

	public void performChange(Change change, boolean fork) throws InterruptedException, InvocationTargetException {
		PerformChangeOperation operation = createPerformChangeOperation(change);
		performOperation(operation, fork);
	}

	/**
	 * Executes either a complete refactoring operation or a change operation.
	 * @param changeOperation The change operation. Has to be not <code>null</code>.
	 * @param fork If set, the execution will be forked.
	 */
	private void performOperation(PerformChangeOperation changeOperation, boolean fork)
			throws InterruptedException, InvocationTargetException {
		Assert.isTrue(changeOperation != null);
		Assert.isTrue(Display.getCurrent() != null);
		final IJobManager manager = Job.getJobManager();
		final ISchedulingRule rule = getSchedulingRule();
		try {
			try {
				Runnable r = () -> manager.beginRule(rule, null);
				BusyIndicator.showWhile(fParent.getDisplay(), r);
			} catch (OperationCanceledException e) {
				throw new InterruptedException(e.getMessage());
			}

			RefactoringSaveHelper saveHelper = new RefactoringSaveHelper(fSaveMode);
			fRefactoring.setValidationContext(fParent);
			try {
				if (fork)
					fExecContext.run(false, false, new WorkbenchRunnableAdapter(changeOperation, rule, true));
				RefactoringStatus validationStatus = changeOperation.getValidationStatus();
				if (validationStatus != null && validationStatus.hasFatalError()) {
					MessageDialog.openError(fParent, fRefactoring.getName(),
							NLS.bind(Messages.RefactoringExecutionHelper_cannot_execute,
									validationStatus.getMessageMatchingSeverity(RefactoringStatus.FATAL)));
					throw new InterruptedException();
				}
			} catch (InvocationTargetException e) {
				if (changeOperation.changeExecutionFailed()) {
					ChangeExceptionHandler handler = new ChangeExceptionHandler(fParent, fRefactoring);
					Throwable inner = e.getTargetException();
					if (inner instanceof RuntimeException) {
						handler.handle(changeOperation.getChange(), (RuntimeException) inner);
					} else if (inner instanceof CoreException) {
						handler.handle(changeOperation.getChange(), (CoreException) inner);
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
