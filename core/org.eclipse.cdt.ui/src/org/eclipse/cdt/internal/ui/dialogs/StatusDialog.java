/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;

import org.eclipse.core.runtime.IStatus;

/**
 * An abstract base class for dialogs with a status bar and ok/cancel buttons.
 * The status message must be passed over as StatusInfo object and can be
 * an error, warning or ok. The OK button is enabled or disabled depending
 * on the status.
 */ 
public abstract class StatusDialog extends Dialog {
	
	private Button fOkButton;
	private MessageLine fStatusLine;
	private IStatus fLastStatus;
	private String fTitle;
	private Image fImage;
	
	/**
	 * Creates an instane of a status dialog.
	 */
	public StatusDialog(Shell parent) {
		super(parent);
	}
		
	/**
	 * Update the dialog's status line to reflect the given status.
	 * It is save to call this method before the dialog has been opened.
	 */
	protected void updateStatus(IStatus status) {
		fLastStatus= status;
		if (fStatusLine != null && !fStatusLine.isDisposed()) {
			updateButtonsEnableState(status);
			fStatusLine.setErrorMessage(status.getMessage());
		}
	}
	
	/**
	 * Returns the last status.
	 */
	public IStatus getStatus() {
		return fLastStatus;
	}

	/**
	 * Updates the status of the ok button to reflect the given status.
	 * Subclasses may override this method to update additional buttons.
	 * @param status the status.
	 */
	protected void updateButtonsEnableState(IStatus status) {
		if (fOkButton != null && !fOkButton.isDisposed())
			fOkButton.setEnabled(!status.matches(IStatus.ERROR));
	}
	
	/* 
	 * @see Window#create(Shell)
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (fTitle != null)
			shell.setText(fTitle);
	}

	/*
	 * @see Window#create()
	 */	
	public void create() {
		super.create();
		if (fLastStatus != null) {
			// policy: dialogs are not allowed to come up with an error message
			if (fLastStatus.matches(IStatus.ERROR)) {
				StatusInfo status= new StatusInfo();
				status.setError(""); //$NON-NLS-1$
				fLastStatus= status;
			}
			updateStatus(fLastStatus);
		}
	}

	/*
	 * @see Dialog#createButtonsForButtonBar(Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		fOkButton= createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}
	
	/*
	 * @see Dialog#createButtonBar(Composite)
	 */				
	protected Control createButtonBar(Composite parent) {
		Composite composite= new Composite(parent, SWT.NULL);
		GridLayout layout= new GridLayout();
		layout.numColumns= 1;
		layout.marginHeight= 0;
		layout.marginWidth= convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		fStatusLine= new MessageLine(composite);
		fStatusLine.setAlignment(SWT.LEFT);
		fStatusLine.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fStatusLine.setErrorMessage(null); 

		super.createButtonBar(composite);
		return composite;
	}
	
	/**
	 * Sets the title for this dialog.
	 * @param title the title.
	 */
	public void setTitle(String title) {
		fTitle= title != null ? title : ""; //$NON-NLS-1$
		Shell shell= getShell();
		if ((shell != null) && !shell.isDisposed())
			shell.setText(fTitle);
	}

	/**
	 * Sets the image for this dialog.
	 * @param image the image.
	 */
	public void setImage(Image image) {
		fImage= image;
		Shell shell= getShell();
		if ((shell != null) && !shell.isDisposed())
			shell.setImage(fImage);
	}	
	
}

