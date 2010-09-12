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
package org.eclipse.cdt.internal.ui.refactoring.rename;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.cdt.core.model.ICProject;

import org.eclipse.cdt.internal.ui.refactoring.RefactoringExecutionHelper;

/**
 * Central access point to execute rename refactorings.
 */
public class RenameSupport {
	/** Flag indication that no additional update is to be performed. */
	public static final int NONE= 0;

	/** Flag indicating that references are to be updated as well. */
	public static final int UPDATE_REFERENCES= 1 << 0;

	/**
	 * Flag indicating that textual matches in comments and in string literals
	 * are to be updated as well.
	 */
	public static final int UPDATE_TEXTUAL_MATCHES= 1 << 6;

	/** Flag indicating that the getter method is to be updated as well. */
	public static final int UPDATE_GETTER_METHOD= 1 << 4;

	/** Flag indicating that the setter method is to be updated as well. */
	public static final int UPDATE_SETTER_METHOD= 1 << 5;

	private CRenameRefactoring fRefactoring;
	private RefactoringStatus fPreCheckStatus;

	/**
	 * Executes some light weight precondition checking. If the returned status
	 * is an error then the refactoring can't be executed at all. However,
	 * returning an OK status doesn't guarantee that the refactoring can be
	 * executed. It may still fail while performing the exhaustive precondition
	 * checking done inside the methods <code>openDialog</code> or
	 * <code>perform</code>.
	 *
	 * The method is mainly used to determine enablement/disablement of actions.
	 *
	 * @return the result of the light weight precondition checking.
	 *
	 * @throws CoreException if an unexpected exception occurs while performing the checking.
	 *
	 * @see #openDialog(Shell)
	 * @see #perform(Shell, IRunnableContext)
	 */
	public IStatus preCheck() throws CoreException {
		ensureChecked();
		if (fPreCheckStatus.hasFatalError())
			return fPreCheckStatus.getEntryMatchingSeverity(RefactoringStatus.FATAL).toStatus();
		else
			return Status.OK_STATUS;
	}

	/**
	 * Opens the refactoring dialog for this rename support.
	 *
	 * @param parent a shell used as a parent for the refactoring dialog.
	 * @throws CoreException if an unexpected exception occurs while opening the
	 * dialog.
	 *
	 * @see #openDialog(Shell, boolean)
	 */
	public void openDialog(Shell parent) throws CoreException {
		openDialog(parent, false);
	}

	/**
	 * Opens the refactoring dialog for this rename support.
	 *
	 * <p>
	 * This method has to be called from within the UI thread.
	 * </p>
	 *
	 * @param shell a shell used as a parent for the refactoring, preview, or error dialog
	 * @param showPreviewOnly if <code>true</code>, the dialog skips all user input pages and
	 * directly shows the preview or error page. Otherwise, shows all pages.
	 * @return <code>true</code> if the refactoring has been executed successfully,
	 * <code>false</code> if it has been canceled or if an error has happened during
	 * initial conditions checking.
	 *
	 * @throws CoreException if an error occurred while executing the
	 * operation.
	 *
	 * @see #openDialog(Shell)
	 */
	public boolean openDialog(Shell shell, boolean showPreviewOnly) throws CoreException {
		ensureChecked();
		if (fPreCheckStatus.hasFatalError()) {
			showInformation(shell, fPreCheckStatus);
			return false;
		}

        return CRefactory.openDialog(shell, fRefactoring, showPreviewOnly);
	}

	/**
	 * Executes the rename refactoring without showing a dialog to gather
	 * additional user input (for example the new name of the <tt>ICElement</tt>).
	 * Only an error dialog is shown (if necessary) to present the result
	 * of the refactoring's full precondition checking.
	 * <p>
	 * The method has to be called from within the UI thread.
	 * </p>
	 *
	 * @param parent a shell used as a parent for the error dialog.
	 * @param context a {@link IRunnableContext} to execute the operation.
	 *
	 * @throws InterruptedException if the operation has been canceled by the
	 * user.
	 * @throws InvocationTargetException if an error occurred while executing the
	 * operation.
	 *
	 * @see #openDialog(Shell)
	 * @see IRunnableContext#run(boolean, boolean, org.eclipse.jface.operation.IRunnableWithProgress)
	 */
	public void perform(Shell parent, IRunnableContext context) throws InterruptedException, InvocationTargetException {
		try {
			ensureChecked();
			if (fPreCheckStatus.hasFatalError()) {
				showInformation(parent, fPreCheckStatus);
				return;
			}

			CRenameProcessor renameProcessor = getRenameProcessor();
			try {
				renameProcessor.lockIndex();
				fPreCheckStatus = renameProcessor.checkInitialConditions(new NullProgressMonitor());
				if (fPreCheckStatus.hasFatalError()) {
					showInformation(parent, fPreCheckStatus);
					return;
				}
				RefactoringExecutionHelper helper= new RefactoringExecutionHelper(fRefactoring,
						RefactoringCore.getConditionCheckingFailedSeverity(), renameProcessor.getSaveMode(),
						parent, context);
				helper.perform(true, true);
			} finally {
				renameProcessor.unlockIndex();
			}
		} catch (CoreException e) {
			throw new InvocationTargetException(e);
		}
	}

	private RenameSupport(CRenameProcessor processor) {
		fRefactoring= new CRenameRefactoring(processor);
	}

	private CRenameProcessor getRenameProcessor() {
		return (CRenameProcessor) fRefactoring.getProcessor();
	}

	/**
	 * Creates a new rename support for the given {@link ICProject}.
	 *
	 * @param processor the {@link CRenameProcessor}
	 * @return the {@link RenameSupport}.
	 * @throws CoreException if an unexpected error occurred while creating
	 * the {@link RenameSupport}.
	 */
	public static RenameSupport create(CRenameProcessor processor) throws CoreException {
		return new RenameSupport(processor);
	}

	private void ensureChecked() throws CoreException {
		if (fPreCheckStatus == null) {
			if (!fRefactoring.isApplicable()) {
				fPreCheckStatus= RefactoringStatus.createFatalErrorStatus(RenameMessages.RenameSupport_not_available);
			} else {
				fPreCheckStatus= new RefactoringStatus();
			}
		}
	}

	private void showInformation(Shell parent, RefactoringStatus status) {
		String message= status.getMessageMatchingSeverity(RefactoringStatus.FATAL);
		MessageDialog.openInformation(parent, RenameMessages.RenameSupport_dialog_title, message);
	}
}
