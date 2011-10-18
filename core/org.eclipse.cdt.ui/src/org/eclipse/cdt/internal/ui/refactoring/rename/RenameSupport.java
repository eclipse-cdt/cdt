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

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.refactoring.RefactoringExecutionHelper;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringStarter;

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

    /** @see #openDialog(Shell, CRenameRefactoring, DialogMode) */
    private enum DialogMode { ALL_PAGES, PREVIEW_ONLY, CONDITIONAL_PREVIEW }
    /** @see #openDialog(Shell, CRenameRefactoring, DialogMode) */
    private enum DialogResult { OK, CANCELED, SKIPPED }
    // Same as org.eclipse.ltk.internal.ui.refactoring.IErrorWizardPage#PAGE_NAME
    private static final String ERROR_PAGE_NAME = "ErrorPage"; //$NON-NLS-1$

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
	 * @see #perform(Shell, IWorkbenchWindow)
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
	 * @param shell a shell used as a parent for the refactoring dialog.
	 * @throws CoreException if an unexpected exception occurs while opening the dialog.
	 *
	 * @see #openDialog(Shell, boolean)
	 */
	public boolean openDialog(Shell shell) throws CoreException {
        return openDialog(shell, false);
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

		DialogMode mode = showPreviewOnly ?	DialogMode.PREVIEW_ONLY : DialogMode.ALL_PAGES; 
        return openDialog(shell, fRefactoring, mode) == DialogResult.OK;
	}

	/**
	 * Opens the refactoring dialog for a given rename refactoring.
	 *
	 * @param shell a shell used as a parent for the refactoring dialog.
	 * @param refactoring the refactoring object.
	 *
	 * @see #openDialog(Shell, boolean)
	 */
	public static void openDialog(Shell shell, CRenameRefactoring refactoring) {
        openDialog(shell, refactoring, DialogMode.ALL_PAGES);
	}

	/**
	 * Opens the refactoring dialog.
	 *
	 * <p>
	 * This method has to be called from within the UI thread.
	 * </p>
	 *
	 * @param shell A shell used as a parent for the refactoring, preview, or error dialog
	 * @param refactoring The refactoring.
	 * @param mode One of DialogMode values. ALL_PAGES opens wizard with all pages shown;
	 *     PREVIEW_ONLY opens the preview page only; CONDITIONAL_PREVIEW opens the wizard with
	 *     preview page only and only if a warning was generated during the final conditions check.
	 * @return One of DialogResult values. OK is returned if the dialog was shown and
	 *     the refactoring change was applied; CANCELED is returned if the refactoring was
	 *     cancelled. SKIPPED is returned if the dialog was skipped in CONDITIONAL_PREVIEW mode and
	 *     the refactoring change has not been applied yet.
	 */
	static DialogResult openDialog(Shell shell, CRenameRefactoring refactoring, final DialogMode mode) {
		try {
			final boolean[] dialogSkipped = new boolean[1];
    		CRenameRefactoringWizard wizard;
    		if (mode == DialogMode.ALL_PAGES) {
    			wizard = new CRenameRefactoringWizard(refactoring);
    		} else {
    			wizard = new CRenameRefactoringWizard(refactoring) {
					@Override
					protected void addUserInputPages() {
    					// Nothing to add
    				}

					@Override
					public IWizardPage getStartingPage() {
						IWizardPage startingPage = super.getStartingPage();
						if (mode == DialogMode.CONDITIONAL_PREVIEW &&
								!startingPage.getName().equals(ERROR_PAGE_NAME)) {
							dialogSkipped[0] = true;
							return null;
						}
						return startingPage;
					}
    			};
    			wizard.setForcePreviewReview(mode != DialogMode.ALL_PAGES);
    		}
    		RefactoringStarter starter = new RefactoringStarter();
			CRenameProcessor processor = (CRenameProcessor) refactoring.getProcessor();
        	processor.lockIndex();
        	try {
        		RefactoringStatus status = processor.checkInitialConditions(new NullProgressMonitor());
        		if (status.hasFatalError()) {
					showInformation(shell, status);
        			return DialogResult.CANCELED;
        		}
        		if (starter.activate(wizard, shell, RenameMessages.CRefactory_title_rename,
        				processor.getSaveMode())) {
        			return DialogResult.OK;
        		}
        		return dialogSkipped[0] ? DialogResult.SKIPPED : DialogResult.CANCELED;
        	} finally {
        		processor.unlockIndex();
        	}
        } catch (InterruptedException e) {
			Thread.currentThread().interrupt();
        } catch (CoreException e) {
        	CUIPlugin.log(e);
		}
        return DialogResult.CANCELED;
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
	public boolean perform(Shell parent, IWorkbenchWindow context) throws InterruptedException, InvocationTargetException {
		try {
			ensureChecked();
			if (fPreCheckStatus.hasFatalError()) {
				showInformation(parent, fPreCheckStatus);
				return false;
			}

			CRenameProcessor renameProcessor = getRenameProcessor();
			renameProcessor.lockIndex();
			try {
				fPreCheckStatus = renameProcessor.checkInitialConditions(new NullProgressMonitor());
				if (fPreCheckStatus.hasFatalError()) {
					showInformation(parent, fPreCheckStatus);
					return false;
				}
				DialogResult result = openDialog(context.getShell(), fRefactoring,
						DialogMode.CONDITIONAL_PREVIEW);
				switch (result) {
				case OK:
					return true;
				case SKIPPED:
					RefactoringExecutionHelper helper= new RefactoringExecutionHelper(fRefactoring,
							RefactoringCore.getConditionCheckingFailedSeverity(),
							renameProcessor.getSaveMode(), parent, context);
					Change change = renameProcessor.getChange();
					Assert.isNotNull(change);
					helper.performChange(change, true);
					return true;
				default:
					return false;
				}
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

	private static void showInformation(Shell parent, RefactoringStatus status) {
		String message= status.getMessageMatchingSeverity(RefactoringStatus.FATAL);
		MessageDialog.openInformation(parent, RenameMessages.RenameSupport_dialog_title, message);
	}
}
