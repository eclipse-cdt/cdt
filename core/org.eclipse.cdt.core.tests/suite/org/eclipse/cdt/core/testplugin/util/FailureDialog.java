/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.testplugin.util;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/*
 * A dialog for collecting notes from the tester regarding
 * the failure of a test.
 */
public class FailureDialog extends Dialog {
	private Text _text;
	private String _log;
	private int SIZING_TEXT_WIDTH = 400;
	private int SIZING_TEXT_HEIGHT = 200;

	/**
	 * Constructor for FailureDialog
	 */
	public FailureDialog(Shell parentShell) {
		super(parentShell);
	}

	/* (non-Javadoc)
	 * Method declared on Window.
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Dialog Test Failed");
	}

	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "&OK", true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		// page group
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		Label label = new Label(composite, SWT.WRAP);
		label.setText("&Enter a note regarding the failure:");

		_text = new Text(composite, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		_text.setFont(JFaceResources.getFontRegistry().get(JFaceResources.TEXT_FONT));
		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = SIZING_TEXT_WIDTH;
		data.heightHint = SIZING_TEXT_HEIGHT;
		_text.setLayoutData(data);

		return composite;
	}

	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	@Override
	protected void okPressed() {
		_log = _text.getText();
		super.okPressed();
	}

	/*
	 * @return String the text contained in the input area of
	 * the dialog.
	 */
	String getText() {
		if (_log == null) {
			return "Empty entry.";
		} else {
			return _log;
		}
	}

	/*
	 * Sets the text of the input area.  This should only be
	 * called to set the initial text so only call before invoking
	 * open().
	 */
	void setText(String text) {
		_text.setText(text);
	}

	/*
	 * Returns a string representation of this class which
	 * the text contained in the input area of the dialog.
	 */
	@Override
	public String toString() {
		return getText();
	}
}
