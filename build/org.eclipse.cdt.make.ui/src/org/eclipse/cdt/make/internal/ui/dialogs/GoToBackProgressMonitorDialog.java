/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.ui.dialogs;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IProgressMonitorWithBlocking;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IconAndMessageDialog;
import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Assert;

/**
 * Progress monitor with a 'Go to background button'
 */
public class GoToBackProgressMonitorDialog extends IconAndMessageDialog implements IRunnableContext {

	private class MyJob extends Job {

		private IRunnableWithProgress runnable;
		private volatile boolean continueEventDispatching = true;
		private Display display;

		public MyJob(IRunnableWithProgress operation, Display display) {
			super(fJobText);
			this.runnable = operation;
			this.display = display;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		protected IStatus run(IProgressMonitor monitor) {
			progressMonitor.setWrapped(monitor);

			try {
				if (runnable != null)
					runnable.run(progressMonitor);
			} catch (InvocationTargetException e) {
				return new Status(IStatus.ERROR, MakeUIPlugin.getPluginId(), -1, "Operation failed.", e); //$NON-NLS-1$
			} catch (InterruptedException e) {
				return Status.CANCEL_STATUS;
			} catch (OperationCanceledException e) {
				return Status.CANCEL_STATUS;
			} catch (RuntimeException e) {
				return new Status(IStatus.ERROR, MakeUIPlugin.getPluginId(), -1, "Operation failed.", e); //$NON-NLS-1$
			} catch (Error e) {
				return new Status(IStatus.ERROR, MakeUIPlugin.getPluginId(), -1, "Operation failed.", e); //$NON-NLS-1$
			} finally {
				// Make sure that all events in the asynchronous event queue
				// are dispatched.
				display.syncExec(new Runnable() {

					public void run() {
						// do nothing
					}
				});

				// Stop event dispatching
				continueEventDispatching = false;

				// Force the event loop to return from sleep () so that
				// it stops event dispatching.
				display.asyncExec(null);
			}
			return Status.OK_STATUS;
		}

		/**
		 * Processes events or waits until this modal context thread terminates.
		 */
		public void block() {
			if (display == Display.getCurrent()) {
				while (continueEventDispatching && !progressMonitor.isInBackground()) {
					if (!display.readAndDispatch())
						display.sleep();
				}
			} else {
				try {
					join();
				} catch (InterruptedException e) {
				}
			}
		}
	}

	/**
	 * Name to use for task when normal task name is empty string.
	 */
	private static String DEFAULT_TASKNAME = JFaceResources.getString("ProgressMonitorDialog.message"); //$NON-NLS-1$

	/**
	 * Constants for label and monitor size
	 */
	private static int LABEL_DLUS = 21;
	private static int BAR_DLUS = 9;

	/**
	 * The progress indicator control.
	 */
	protected ProgressIndicator progressIndicator;

	/**
	 * The label control for the task. Kept for backwards compatibility.
	 */
	protected Label taskLabel;

	/**
	 * The label control for the subtask.
	 */
	protected Label subTaskLabel;

	/**
	 * The Cancel button control.
	 */
	protected Button cancel;

	protected Button toBackgroundButton;

	/**
	 * Indicates whether the Cancel button is to be shown.
	 */
	protected boolean operationCancelableState = false;

	/**
	 * Indicates whether the Cancel button is to be enabled.
	 */
	protected boolean enableCancelButton;

	/**
	 * The progress monitor.
	 */
	private ProgressMonitor progressMonitor = new ProgressMonitor();

	/**
	 * The name of the current task (used by ProgressMonitor).
	 */
	private String task;

	/**
	 * The nesting depth of currently running runnables.
	 */
	private int nestingDepth;

	/**
	 * The cursor used in the cancel button;
	 */
	protected Cursor arrowCursor;

	/**
	 * The cursor used in the shell;
	 */
	private Cursor waitCursor;

	/**
	 * Flag indicating whether to open or merely create the dialog before run.
	 */
	private boolean openOnRun = true;

	final private String fJobText;
	/**
	 * Internal progress monitor implementation. Wraps the real progress monitor and intercepts all steps to forwrad them to the
	 * progress monitor dialog.
	 */
	private class ProgressMonitor implements IProgressMonitorWithBlocking {

		private String fSubTask = "";//$NON-NLS-1$
		private boolean fIsCanceled;
		protected boolean locked = false;

		private boolean fInBackground = false;
		private IProgressMonitor fWrapped;

		public ProgressMonitor() {
			fWrapped = null;
		}

		public void setWrapped(IProgressMonitor wrapped) {
			fWrapped = wrapped;
		}

		public void setInBackground(boolean inBackground) {
			fInBackground = inBackground;
		}

		public boolean isInBackground() {
			return fInBackground;
		}

		public void beginTask(final String name, final int totalWork) {
			if (fWrapped != null) {
				fWrapped.beginTask(name, totalWork);
			}
			if (fInBackground) {
				return;
			}
			if (progressIndicator.isDisposed())
				return;

			if (name == null)
				task = "";//$NON-NLS-1$
			else
				task = name;

			getShell().getDisplay().asyncExec(new Runnable() {

				public void run() {
					internalBeginTask(name, totalWork);
				}
			});
		}

		private void internalBeginTask(String name, int totalWork) {
			if (progressIndicator.isDisposed())
				return;

			String s = task;
			if (s.length() <= 0)
				s = DEFAULT_TASKNAME;
			setDialogMessage(s);

			if (totalWork == UNKNOWN) {
				progressIndicator.beginAnimatedTask();
			} else {
				progressIndicator.beginTask(totalWork);
			}
		}

		public void done() {
			if (fWrapped != null) {
				fWrapped.done();
			}
			if (fInBackground) {
				return;
			}
			getShell().getDisplay().asyncExec(new Runnable() {

				public void run() {
					internalDone();
				}
			});
		}

		private void internalDone() {
			if (!progressIndicator.isDisposed()) {
				progressIndicator.sendRemainingWork();
				progressIndicator.done();
			}
		}

		public void setTaskName(final String name) {
			if (fWrapped != null) {
				fWrapped.setTaskName(name);
			}

			if (fInBackground) {
				return;
			}

			if (name == null)
				task = "";//$NON-NLS-1$
			else
				task = name;

			getShell().getDisplay().asyncExec(new Runnable() {

				public void run() {
					internalSetTaskName(name);
				}
			});
		}

		private void internalSetTaskName(String name) {
			String s = task;
			if (s.length() <= 0)
				s = DEFAULT_TASKNAME;
			setDialogMessage(s);
		}

		public boolean isCanceled() {
			return fIsCanceled || (fWrapped != null && fWrapped.isCanceled());
		}

		public void setCanceled(boolean b) {
			if (fWrapped != null) {
				fWrapped.setCanceled(b);
			}
			if (fInBackground) {
				return;
			}

			fIsCanceled = b;
			if (locked)
				clearBlocked();
		}

		public void subTask(String name) {
			if (fWrapped != null) {
				fWrapped.subTask(name);
			}
			if (fInBackground) {
				return;
			}

			if (name == null)
				fSubTask = "";//$NON-NLS-1$
			else
				fSubTask = name;

			if (!subTaskLabel.isDisposed()) {
				getShell().getDisplay().asyncExec(new Runnable() {

					public void run() {
						if (!subTaskLabel.isDisposed()) {
							subTaskLabel.setText(fSubTask);
						}
					}
				});
			}
		}

		public void worked(int work) {
			if (fWrapped != null) {
				fWrapped.worked(work);
			}
			if (fInBackground) {
				return;
			}

			internalWorked(work);
		}

		public void internalWorked(final double work) {
			if (fWrapped != null) {
				fWrapped.internalWorked(work);
			}
			if (fInBackground) {
				return;
			}

			if (!progressIndicator.isDisposed()) {
				getShell().getDisplay().asyncExec(new Runnable() {

					public void run() {
						if (!progressIndicator.isDisposed()) {
							progressIndicator.worked(work);
						}
					}
				});
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.IProgressMonitorWithBlocking#clearBlocked()
		 */
		public void clearBlocked() {
			if (fWrapped instanceof IProgressMonitorWithBlocking) {
				((IProgressMonitorWithBlocking) fWrapped).clearBlocked();
			}
			if (fInBackground) {
				return;
			}

			getShell().getDisplay().asyncExec(new Runnable() {

				public void run() {
					internalClearBlocked();
				}
			});
		}

		private void internalClearBlocked() {
			setMessage(task);
			locked = false;
			setImageLabel(getImage());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.IProgressMonitorWithBlocking#setBlocked(org.eclipse.core.runtime.IStatus)
		 */
		public void setBlocked(final IStatus reason) {
			if (fWrapped instanceof IProgressMonitorWithBlocking) {
				((IProgressMonitorWithBlocking) fWrapped).setBlocked(reason);
			}
			if (fInBackground) {
				return;
			}
			getShell().getDisplay().asyncExec(new Runnable() {

				public void run() {
					internalSetBlocked(reason);
				}
			});
		}

		private void internalSetBlocked(IStatus reason) {
			setDialogMessage(reason.getMessage());
			locked = true;
			setImageLabel(getImage());
		}

		private void setDialogMessage(String message) {
			setMessage(message);
		}

		private void setImageLabel(Image image) {
			imageLabel.setImage(image);
		}

	}
	/**
	 * Creates a progress monitor dialog under the given shell. The dialog has a standard title and no image. <code>open</code> is
	 * non-blocking.
	 * 
	 * @param parent
	 *            the parent shell, or <code>null</code> to create a top-level shell
	 */
	public GoToBackProgressMonitorDialog(Shell parent, String jobText) {
		super(parent);
		setShellStyle(SWT.BORDER | SWT.TITLE | SWT.APPLICATION_MODAL); // no close button
		setBlockOnOpen(false);
		fJobText = jobText;
	}
	/**
	 * Enables the cancel button (asynchronously).
	 */
	private void asyncSetOperationCancelButtonEnabled(final boolean b) {
		if (getShell() != null) {
			getShell().getDisplay().asyncExec(new Runnable() {

				public void run() {
					setOperationCancelButtonEnabled(b);
				}
			});
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.BACK_ID) {
			progressMonitor.setInBackground(true);
			toBackgroundButton.setEnabled(false);
			cancel.setEnabled(true);
		}
		super.buttonPressed(buttonId);
	}

	/**
	 * The cancel button has been pressed.
	 * 
	 * @since 3.0
	 */
	protected void cancelPressed() {
		//NOTE: this was previously done from a listener installed on the cancel
		//button. On GTK, the listener installed by Dialog.createButton is called
		//first and this was throwing an exception because the cancel button
		//was already disposed
		cancel.setEnabled(false);
		toBackgroundButton.setEnabled(false);
		progressMonitor.setCanceled(true);
		super.cancelPressed();
	}
	/*
	 * (non-Javadoc) Method declared on Window.
	 */
	/**
	 * The <code>ProgressMonitorDialog</code> implementation of this method only closes the dialog if there are no currently
	 * running runnables.
	 */
	public boolean close() {
		if (getNestingDepth() <= 0) {
			clearCursors();
			return super.close();
		}
		return false;
	}

	/**
	 * Clear the cursors in the dialog.
	 * 
	 * @since 3.0
	 */
	protected void clearCursors() {
		if (cancel != null && !cancel.isDisposed()) {
			cancel.setCursor(null);
		}
		Shell shell = getShell();
		if (shell != null && !shell.isDisposed()) {
			shell.setCursor(null);
		}
		if (arrowCursor != null)
			arrowCursor.dispose();
		if (waitCursor != null)
			waitCursor.dispose();
		arrowCursor = null;
		waitCursor = null;
	}
	/*
	 * (non-Javadoc) Method declared in Window.
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(JFaceResources.getString("ProgressMonitorDialog.title")); //$NON-NLS-1$
		if (waitCursor == null)
			waitCursor = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
		shell.setCursor(waitCursor);
	}

	/*
	 * (non-Javadoc) Method declared on Dialog.
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		// cancel button
		toBackgroundButton = createButton(parent, IDialogConstants.BACK_ID, "Run in Background", true); //$NON-NLS-1$
		cancel = createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, true);

		if (arrowCursor == null)
			arrowCursor = new Cursor(cancel.getDisplay(), SWT.CURSOR_ARROW);
		cancel.setCursor(arrowCursor);
		toBackgroundButton.setCursor(arrowCursor);
		setOperationCancelButtonEnabled(enableCancelButton);
	}

	/*
	 * (non-Javadoc) Method declared on Dialog.
	 */
	protected Control createDialogArea(Composite parent) {

		setMessage(DEFAULT_TASKNAME);
		createMessageArea(parent);

		//Only set for backwards compatibility
		taskLabel = messageLabel;

		// progress indicator
		progressIndicator = new ProgressIndicator(parent);
		GridData gd = new GridData();
		gd.heightHint = convertVerticalDLUsToPixels(BAR_DLUS);
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = 2;
		progressIndicator.setLayoutData(gd);

		// label showing current task
		subTaskLabel = new Label(parent, SWT.LEFT | SWT.WRAP);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = convertVerticalDLUsToPixels(LABEL_DLUS);
		gd.horizontalSpan = 2;
		subTaskLabel.setLayoutData(gd);
		subTaskLabel.setFont(parent.getFont());

		return parent;
	}

	/*
	 * (non-Javadoc) Method declared in Window.
	 */
	protected Point getInitialSize() {

		Point calculatedSize = super.getInitialSize();
		if (calculatedSize.x < 450)
			calculatedSize.x = 450;
		return calculatedSize;
	}
	/**
	 * Returns the progress monitor to use for operations run in this progress dialog.
	 * 
	 * @return the progress monitor
	 */
	public IProgressMonitor getProgressMonitor() {
		return progressMonitor;
	}
	/*
	 * (non-Javadoc) Method declared on IRunnableContext. Runs the given <code> IRunnableWithProgress </code> with the progress
	 * monitor for this progress dialog. The dialog is opened before it is run, and closed after it completes.
	 */
	public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable) throws InvocationTargetException,
			InterruptedException {
		Assert.isTrue(fork);
		setCancelable(cancelable);
		try {
			aboutToRun();
			progressMonitor.setInBackground(false);

			MyJob job = new MyJob(runnable, getShell().getDisplay());
			job.schedule();
			job.block();
			if (progressMonitor.isCanceled()) {
				throw new InterruptedException();
			}
		} finally {
			finishedRun();
		}
	}

	/**
	 * Returns whether the dialog should be opened before the operation is run. Defaults to <code>true</code>
	 * 
	 * @return <code>true</code> to open the dialog before run, <code>false</code> to only create the dialog, but not open it
	 * @since 3.0
	 */
	public boolean getOpenOnRun() {
		return openOnRun;
	}

	/**
	 * Sets whether the dialog should be opened before the operation is run. NOTE: Setting this to false and not forking a process
	 * may starve any asyncExec that tries to open the dialog later.
	 * 
	 * @param openOnRun
	 *            <code>true</code> to open the dialog before run, <code>false</code> to only create the dialog, but not open it
	 * @since 3.0
	 */
	public void setOpenOnRun(boolean openOnRun) {
		this.openOnRun = openOnRun;
	}

	/**
	 * Returns the nesting depth of running operations.
	 * 
	 * @return the nesting depth of running operations
	 * @since 3.0
	 */
	protected int getNestingDepth() {
		return nestingDepth;
	}

	/**
	 * Increments the nesting depth of running operations.
	 * 
	 * @since 3.0
	 */
	protected void incrementNestingDepth() {
		nestingDepth++;
	}

	/**
	 * Decrements the nesting depth of running operations.
	 * 
	 * @since 3.0
	 *  
	 */
	protected void decrementNestingDepth() {
		nestingDepth--;
	}

	/**
	 * Called just before the operation is run. Default behaviour is to open or create the dialog, based on the setting of
	 * <code>getOpenOnRun</code>, and increment the nesting depth.
	 * 
	 * @since 3.0
	 */
	protected void aboutToRun() {
		if (getOpenOnRun()) {
			open();
		} else {
			create();
		}
		incrementNestingDepth();
	}

	/**
	 * Called just after the operation is run. Default behaviour is to decrement the nesting depth, and close the dialog.
	 * 
	 * @since 3.0
	 */
	protected void finishedRun() {
		decrementNestingDepth();
		close();
	}

	/**
	 * Sets whether the progress dialog is cancelable or not.
	 * 
	 * @param cancelable
	 *            <code>true</code> if the end user can cancel this progress dialog, and <code>false</code> if it cannot be
	 *            canceled
	 */
	public void setCancelable(boolean cancelable) {
		if (cancel == null)
			enableCancelButton = cancelable;
		else
			asyncSetOperationCancelButtonEnabled(cancelable);
	}
	/**
	 * Helper to enable/disable Cancel button for this dialog.
	 * 
	 * @param b
	 *            <code>true</code> to enable the cancel button, and <code>false</code> to disable it
	 * @since 3.0
	 */
	protected void setOperationCancelButtonEnabled(boolean b) {
		operationCancelableState = b;
		cancel.setEnabled(b);
	}

	/*
	 * @see org.eclipse.jface.dialogs.IconAndMessageDialog#getImage()
	 */
	protected Image getImage() {
		if (progressMonitor.locked)
			return JFaceResources.getImageRegistry().get(org.eclipse.jface.dialogs.Dialog.DLG_IMG_LOCKED);
		else
			return JFaceResources.getImageRegistry().get(org.eclipse.jface.dialogs.Dialog.DLG_IMG_INFO);
	}

	/**
	 * Set the message in the message label.
	 */
	private void setMessage(String messageString) {
		//must not set null text in a label
		message = messageString == null ? "" : messageString; //$NON-NLS-1$
		if (messageLabel == null || messageLabel.isDisposed())
			return;
		messageLabel.setText(message);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#open()
	 */
	public int open() {
		//Check to be sure it is not already done. If it is just return OK.
		if (!getOpenOnRun()) {
			if (getNestingDepth() == 0)
				return OK;
		}
		return super.open();
	}

}