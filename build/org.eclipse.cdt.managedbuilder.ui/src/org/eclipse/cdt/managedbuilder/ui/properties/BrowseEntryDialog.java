package org.eclipse.cdt.managedbuilder.ui.properties;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
 * **********************************************************************/

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class BrowseEntryDialog extends Dialog {
	// String constants
	private static final String PREFIX = "BuildPropertyCommon";	//$NON-NLS-1$
	private static final String LABEL = PREFIX + ".label";	//$NON-NLS-1$
	private static final String BROWSE = LABEL + ".browse";	//$NON-NLS-1$

	/**
	 * The title of the dialog.
	 */
	private String title = "";

	/**
	 * The message to display, or <code>null</code> if none.
	 */
	private String message = "";

	/**
	 * The input value; the empty string by default.
	 */
	private String value = "";

	/**
	 * Error message label widget.
	 */
	private Label errorMessageLabel;

	// Widgets
	private Button btnBrowse = null;
	private Button btnOK = null;
	private Text text = null;

	/**
	 * Creates an input dialog with OK, Cancel, and a Browse button.
	 * 
	 * @param shell the parent shell
	 * @param dialogTitle the title of the dialog or <code>null</code> if none
	 * @param dialogMessage the dialog message, or <code>null</code> if none
	 * @param initialValue the initial input value, or <code>null</code> if none
	 * (equivalent to the empty string)
	 */
	public BrowseEntryDialog(Shell shell, String dialogTitle, String dialogMessage, String initialValue) {
		super(shell);
		// We are editing the value argument if it is not an empty string
		if (dialogTitle != null) {
			title = dialogTitle;
		}
		// Cache the message to be shown in the label
		if (dialogMessage != null) {
			message = dialogMessage;
		}
		// Value for the text widget
		if (initialValue != null) {
			value = initialValue;
		}
	}

	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			value = text.getText().trim();
		} else {
			value = null;
		}
		super.buttonPressed(buttonId);
	}

	/* (non-Javadoc)
	 * Method declared in Window.
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (title != null)
			shell.setText(title);
	}

	protected Control createDialogArea(Composite parent) {
		Composite composite = ControlFactory.createComposite(parent, 4);

		// Create the label
		if (message != null) {
			Label label = new Label(composite, SWT.WRAP);
			label.setText(message);
			GridData gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
			gd.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
			gd.horizontalSpan = 4;
			label.setLayoutData(gd);
			label.setFont(parent.getFont());
		}
		
		text = new Text(composite, SWT.SINGLE | SWT.BORDER);
		GridData gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 3;
		text.setLayoutData(gd);
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateButtonState();
			}
		});
		
		// Instantiate the browse button
		btnBrowse = ControlFactory.createPushButton(composite, CUIPlugin.getResourceString(BROWSE));
		setButtonLayoutData(btnBrowse);
		btnBrowse.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				handleBrowsePressed();
			}
		});

		return composite;
	}

	protected void createButtonsForButtonBar(Composite parent) {
		// create OK and Cancel buttons by default
		btnOK = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);

		text.setFocus();
		if (value != null) {
			text.setText(value);
		}
		updateButtonState();
	}

	protected String getValue() {
		return value;
	}

	protected void handleBrowsePressed() {
		// Popup a file browser widget
		DirectoryDialog dialog = new DirectoryDialog(getShell());
		// Create a hint if text widget contains value
		String widgetText; 
		if ((widgetText = text.getText().trim()).length() > 0) {
			dialog.setFilterPath(widgetText);
		} 
		// Open the selection dialog and populate the widget 
		String directory;
		if ((directory = dialog.open()) != null) {
			 /* 
			  * TODO: Convert the dialog to the proper format for platform (i.e.
			  * if platform.pathStyle == Platform.POSIX then swap \\ to / )
			  */
			 text.setText(directory.trim());
			 updateButtonState();
		}
	}

	protected void updateButtonState() {
		if (btnOK != null)
			btnOK.setEnabled(text.getText().trim().length() > 0);
	}
}
