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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;

import org.eclipse.cdt.internal.corext.refactoring.base.RefactoringStatus;

class RefactoringStatusDialog extends Dialog {
	
	private RefactoringStatus fStatus;
	private String fWindowTitle;
	private boolean fBackButton;
	
	public RefactoringStatusDialog(Shell parent, RefactoringStatus status, String windowTitle, boolean backButton) {
		super(parent);
		fStatus= status;
		fWindowTitle= windowTitle;
		fBackButton= backButton;
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}
	
	public RefactoringStatusDialog(Shell parent, ErrorWizardPage page, boolean backButton) {
		this(parent, page.getStatus(), parent.getText(), backButton);
	}
	
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(fWindowTitle);
	}
	protected Control createDialogArea(Composite parent) {
		Composite result= new Composite(parent, SWT.NONE);
		initializeDialogUnits(result);
		GridLayout layout= new GridLayout();
		result.setLayout(layout);
		GridData gd= new GridData(GridData.FILL_BOTH);
		gd.widthHint= 600;
		gd.heightHint= 400;
		result.setLayoutData(gd);
		Color background= parent.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND);
		ViewForm messagePane= new ViewForm(result, SWT.BORDER | SWT.FLAT);
		messagePane.marginWidth= layout.marginWidth;
		messagePane.marginHeight= layout.marginHeight;
		gd= new GridData(GridData.FILL_HORIZONTAL);
		// XXX http://bugs.eclipse.org/bugs/show_bug.cgi?id=27572
		Rectangle rect= messagePane.computeTrim(0, 0, 0, convertHeightInCharsToPixels(2) + messagePane.marginHeight * 2);
		gd.heightHint= rect.height;
		messagePane.setLayoutData(gd);
		messagePane.setBackground(background);
		Label label= new Label(messagePane, SWT.LEFT | SWT.WRAP);
		if (fStatus.hasFatalError())
			label.setText(RefactoringMessages.getString("RefactoringStatusDialog.Cannot_proceed")); //$NON-NLS-1$
		else 
			label.setText(RefactoringMessages.getString("RefactoringStatusDialog.Please_look")); //$NON-NLS-1$
		label.setBackground(background);
		messagePane.setContent(label);
		RefactoringStatusViewer viewer= new RefactoringStatusViewer(result, SWT.NONE);
		viewer.setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.setStatus(fStatus);
		applyDialogFont(result);
		return result;
	}
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.BACK_ID) {
			setReturnCode(IDialogConstants.BACK_ID);
			close();
		} else {
			super.buttonPressed(buttonId);
		}
	}

	protected void createButtonsForButtonBar(Composite parent) {
		if (!fStatus.hasFatalError()) {
			if (fBackButton)
				createButton(parent, IDialogConstants.BACK_ID, IDialogConstants.BACK_LABEL, false); //$NON-NLS-1$
			createButton(parent, IDialogConstants.OK_ID, RefactoringMessages.getString("RefactoringStatusDialog.Continue"), true); //$NON-NLS-1$
			createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		} else {
			if (fBackButton)
				createButton(parent, IDialogConstants.BACK_ID, IDialogConstants.BACK_LABEL, true); //$NON-NLS-1$
			createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		}
	}
}
