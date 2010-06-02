/*******************************************************************************
 * Copyright (c) 2009, 2010 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.dialogs;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * An input dialog for soliciting an input string from the user.
 * The string can be validated. In case of problem error/warning/info message
 * is shown in status line and decorated with appropriate status icon.
 * <p>
 * This concrete dialog class can be instantiated as is, or further subclassed as required.
 * </p>
 * @since 5.2
 */
public class InputStatusDialog extends StatusDialog {
	/**
	 * The title of the dialog.
	 */
	private String title;

	/**
	 * The message to display, or <code>null</code> if none.
	 */
	private String message;

	/**
	 * The input value; the empty string by default.
	 */
	private String value = "";//$NON-NLS-1$

	/**
	 * The input validator, or <code>null</code> if none.
	 */
	private IInputStatusValidator validator;

	/**
	 * Input text widget.
	 */
	private Text text;

    /**
     * Creates an input dialog with OK and Cancel buttons. Note that the dialog
     * will have no visual representation (no widgets) until it is told to open.
     * <p>
     * Note that the <code>open</code> method blocks for input dialogs.
     * </p>
     * 
     * @param parentShell
     *            the parent shell, or <code>null</code> to create a top-level
     *            shell
     * @param dialogTitle
     *            the dialog title, or <code>null</code> if none
     * @param dialogMessage
     *            the dialog message, or <code>null</code> if none
     * @param initialValue
     *            the initial input value, or <code>null</code> if none
     *            (equivalent to the empty string)
     * @param validator
     *            an input validator, or <code>null</code> if none
     *            For a validator, following return statuses are recognized:
     *            <li/>{@link Status#OK_STATUS} or any {@link IStatus#OK} to indicate no error.
     *            <li/>{@link IStatus#ERROR} indicates an error.
     *            <li/>{@link IStatus#WARNING} indicates a warning.
     *            <li/>{@link IStatus#INFO} indicates an informational message
     */
	public InputStatusDialog(Shell parentShell, String dialogTitle, String dialogMessage,
			String initialValue, IInputStatusValidator validator) {
		super(parentShell);
		this.title = dialogTitle;
		if (dialogMessage == null) {
			this.message = ""; //$NON-NLS-1$
		} else {
			this.message = dialogMessage;
		}
		if (initialValue == null) {
			this.value = ""; //$NON-NLS-1$
		} else {
			this.value = initialValue;
		}
		this.validator = validator;
	}

	/*
	 * (non-Javadoc) Method declared on Dialog.
	 */
	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			value = text.getText();
		} else {
			value = null;
		}
		super.buttonPressed(buttonId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (title != null) {
			shell.setText(title);
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);

		Label label = new Label(composite, SWT.WRAP);
		label.setText(message);
		GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL
				| GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
		data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
		label.setLayoutData(data);
		label.setFont(parent.getFont());

		text = new Text(composite, getInputTextStyle());
		text.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validateInput();
			}
		});
		text.setFocus();
		if (value != null) {
			text.setText(value);
			text.selectAll();
		}

		applyDialogFont(composite);

		return composite;
	}

	/**
	 * Returns the text area.
	 * 
	 * @return the text area
	 */
	protected Text getText() {
		return text;
	}

	/**
	 * Returns the validator.
	 * 
	 * @return the validator
	 */
	protected IInputStatusValidator getValidator() {
		return validator;
	}

	/**
	 * Returns the string typed into this input dialog.
	 * 
	 * @return the input string
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Validates the input.
	 * <p>
	 * The default implementation of this framework method delegates the request to the supplied input
	 * validator object; if it finds the input invalid, the error message is displayed in the dialog's message
	 * line. This hook method is called whenever the text changes in the input field.
	 * </p>
	 */
	protected void validateInput() {
		IStatus status = Status.OK_STATUS;
		if (validator != null) {
			status = validator.isValid(text.getText());
		}
		updateStatus(status);
	}

	/**
	 * Returns the style bits that should be used for the input text field. Defaults to a single line entry.
	 * Subclasses may override.
	 * 
	 * @return the integer style bits that should be used when creating the input text
	 * 
	 */
	protected int getInputTextStyle() {
		return SWT.SINGLE | SWT.BORDER;
	}

}
