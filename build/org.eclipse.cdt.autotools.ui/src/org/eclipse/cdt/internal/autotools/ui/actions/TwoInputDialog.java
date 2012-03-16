/*******************************************************************************
 * Copyright (c) 2006, 2007 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.actions;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class TwoInputDialog extends InputDialog {

	private Text secondText;

	private String secondValue;

	private String secondMessage;
	
	private String firstMessage;

	public TwoInputDialog(Shell parentShell, String firstMessage, String dialogTitle,
			String dialogMessage, String secondMessage, String initialValue,
			IInputValidator validator) {
		super(parentShell, dialogTitle, dialogMessage, initialValue, validator);

		this.firstMessage = firstMessage;
		this.secondMessage = secondMessage;
	}

	/*
	 * (non-Javadoc) Method declared on Dialog.
	 */
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			secondValue = secondText.getText();
		} else {
			secondValue = null;
		}
		super.buttonPressed(buttonId);
	}

	protected Control createDialogArea(Composite parent) {

		// create composite
		Composite composite = (Composite) super.createDialogArea(parent);

		CLabel label0 = new CLabel(composite, SWT.WRAP);
		label0.setText(firstMessage);
		Label label = new Label(composite, SWT.WRAP);
		label.setText(secondMessage);
		GridData data = new GridData(GridData.GRAB_HORIZONTAL
				| GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL
				| GridData.VERTICAL_ALIGN_CENTER);
		data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
		label0.setLayoutData(data);
		label0.setFont(parent.getFont());
		label.setLayoutData(data);
		label.setFont(parent.getFont());
		

		secondText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		secondText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL));
		secondText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validateInput();
			}
		});

		// remove error message dialog from focusing.
		composite.getTabList()[2].setVisible(false);
		composite.getTabList()[2].setEnabled(false);

		return composite;
	}

	/**
	 * Returns the text area.
	 * 
	 * @return the text area
	 */
	protected Text getSecondText() {
		return secondText;
	}

	/**
	 * Returns the string typed into this input dialog.
	 * 
	 * @return the input string
	 */
	public String getSecondValue() {
		return secondValue;
	}

}
