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
import org.eclipse.cdt.internal.ui.util.ExceptionHandler;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ProgressMonitorWrapper;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Copy of org.eclipse.ltk.internal.ui.refactoring.ChangeExceptionHandler
 */
public class ChangeExceptionHandler {

	public static class NotCancelableProgressMonitor extends ProgressMonitorWrapper {
		public NotCancelableProgressMonitor(IProgressMonitor monitor) {
			super(monitor);
		}

		@Override
		public void setCanceled(boolean b) {
			// ignore set cancel
		}

		@Override
		public boolean isCanceled() {
			return false;
		}
	}

	private Shell fParent;
	private String fName;

	private static class RefactorErrorDialog extends ErrorDialog {
		public RefactorErrorDialog(Shell parentShell, String dialogTitle, String dialogMessage, IStatus status,
				int displayMask) {
			super(parentShell, dialogTitle, dialogMessage, status, displayMask);
		}

		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			super.createButtonsForButtonBar(parent);
			Button ok = getButton(IDialogConstants.OK_ID);
			ok.setText(Messages.ChangeExceptionHandler_undo_button);
			Button abort = createButton(parent, IDialogConstants.CANCEL_ID,
					Messages.ChangeExceptionHandler_abort_button, true);
			abort.moveBelow(ok);
			abort.setFocus();
		}

		@Override
		protected Control createMessageArea(Composite parent) {
			Control result = super.createMessageArea(parent);
			new Label(parent, SWT.NONE); // filler
			Label label = new Label(parent, SWT.NONE);
			label.setText(Messages.ChangeExceptionHandler_message);
			label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			applyDialogFont(result);
			return result;
		}
	}

	public ChangeExceptionHandler(Shell parent, Refactoring refactoring) {
		fParent = parent;
		fName = refactoring.getName();
	}

	public void handle(Change change, RuntimeException exception) {
		CUIPlugin.log(exception);
		IStatus status = null;
		if (exception.getMessage() == null) {
			status = new Status(IStatus.ERROR, CUIPlugin.getPluginId(), IStatus.ERROR,
					Messages.ChangeExceptionHandler_status_without_detail, exception);
		} else {
			status = new Status(IStatus.ERROR, CUIPlugin.getPluginId(), IStatus.ERROR, exception.getMessage(),
					exception);
		}
		handle(change, status);
	}

	public void handle(Change change, CoreException exception) {
		CUIPlugin.log(exception);
		handle(change, exception.getStatus());
	}

	private void handle(Change change, IStatus status) {
		if (change instanceof CompositeChange) {
			Change undo = ((CompositeChange) change).getUndoUntilException();
			if (undo != null) {
				CUIPlugin.log(status);
				final ErrorDialog dialog = new RefactorErrorDialog(fParent,
						Messages.ChangeExceptionHandler_dialog_title,
						NLS.bind(Messages.ChangeExceptionHandler_dialog_message, fName), status,
						IStatus.OK | IStatus.INFO | IStatus.WARNING | IStatus.ERROR);
				int result = dialog.open();
				if (result == IDialogConstants.OK_ID) {
					performUndo(undo);
				}
				return;
			}
		}
		ErrorDialog dialog = new ErrorDialog(fParent, Messages.ChangeExceptionHandler_dialog_title,
				NLS.bind(Messages.ChangeExceptionHandler_dialog_message, fName), status,
				IStatus.OK | IStatus.INFO | IStatus.WARNING | IStatus.ERROR);
		dialog.open();
	}

	private void performUndo(final Change undo) {
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				SubMonitor subMonitor = SubMonitor.convert(monitor, 11);
				try {
					undo.initializeValidationData(new NotCancelableProgressMonitor(subMonitor.split(1)));
					if (undo.isValid(subMonitor.split(1)).hasFatalError()) {
						return;
					}
					undo.perform(subMonitor.split(9));
				} finally {
					undo.dispose();
				}
			}
		};
		WorkbenchRunnableAdapter adapter = new WorkbenchRunnableAdapter(runnable,
				ResourcesPlugin.getWorkspace().getRoot());
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(fParent);
		try {
			dialog.run(false, false, adapter);
		} catch (InvocationTargetException e) {
			ExceptionHandler.handle(e, fParent, Messages.ChangeExceptionHandler_undo_dialog_title,
					NLS.bind(Messages.ChangeExceptionHandler_undo_dialog_message, fName));
		} catch (InterruptedException e) {
			// can't happen
		}
	}
}
