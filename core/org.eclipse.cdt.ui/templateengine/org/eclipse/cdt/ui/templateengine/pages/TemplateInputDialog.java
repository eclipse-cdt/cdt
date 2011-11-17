/*******************************************************************************
 * Copyright (c) 2007, 2008 Symbian Software Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.templateengine.pages;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.eclipse.cdt.core.templateengine.TemplateEngineUtil;


/**
 * Creates a JFace Dialog for the user to get name-value pair to perform
 * SharedDefaults settings. The class takes care of user input validation.
 */

public class TemplateInputDialog extends Dialog {

	/**
	 * Controls settings in the GUI
	 */
	private static final String NAME = Messages.getString("TemplateInputDialog.0");// To be externalised //$NON-NLS-1$
	private static final String VALUE = Messages.getString("TemplateInputDialog.1");// To be externalised //$NON-NLS-1$

	/**
	 * Shell display messages for ADD and EDIT functionality
	 */

	private static final String ADD_SHELL_MESSAGE = Messages.getString("TemplateInputDialog.2"); //$NON-NLS-1$
	private static final String EDIT_SHELL_MESSAGE = Messages.getString("TemplateInputDialog.3"); //$NON-NLS-1$

	/**
	 * Label Error Message
	 */
	private Label errMessageLabel;
	private String labelMessage = Messages.getString("TemplateInputDialog.4"); //$NON-NLS-1$

	/**
	 * Text fields properties
	 */
	private static final int TEXT_LIMIT = 100;

	/**
	 * Dialog creation instances for display
	 */
	private TemplatePreferencePage templatePreferencePage;
	private TemplateInputDialog sharedDialog;
	private Shell shell;
	private Display display;

	/**
	 * Dialog control instances
	 */
	private Label valueLabel;
	private Label nameLabel;
	private Text valueText;
	private Text nameText;
	private Button oKButton;

	/**
	 * Indentifies ADD/EDIT function
	 */
	private int option;

	/**
	 * Parent composite
	 */
	private Composite parent;

	/**
	 * JFace Dialog Constructor, constructs controls of the super class
	 *
	 * @param parentShell
	 */

	protected TemplateInputDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		this.shell = shell;
		display = shell.getDisplay();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */

	@Override
	protected Control createDialogArea(Composite parent) {

		this.parent = parent;
		Composite composite = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = new GridLayout(2, false);
		composite.setLayout(gridLayout);
		createControls(composite);
		return composite;
	}

	/**
	 * Opens the Dialog to accept user input for shared values.
	 *
	 * @param myDialog
	 * @param dataOption
	 */

	public void open(TemplateInputDialog myDialog, int dataOption) {
		this.option = dataOption;
		sharedDialog = myDialog;
		sharedDialog.create();
		Button oK = getButton(IDialogConstants.OK_ID);
		oK.setEnabled(false);
		if (option == TemplatePreferencePage.OPTION_ADD) {

			shell.setText(ADD_SHELL_MESSAGE);
		} else if (option == TemplatePreferencePage.OPTION_EDIT) {

			shell.setText(EDIT_SHELL_MESSAGE);
		}

		sharedDialog.open();
	}

	/**
	 * Creates control under the parent composite
	 *
	 * @param composite
	 */

	private void createControls(Composite composite) {

		// Name Label
		nameLabel = new Label(composite, SWT.NONE);
		nameLabel.setText(NAME);

		// Name Text Field
		nameText = new Text(composite, SWT.BORDER | SWT.SINGLE);
		nameText.setTextLimit(TEXT_LIMIT);
		GridData textData = new GridData(GridData.FILL_HORIZONTAL);
		textData.horizontalSpan = GridData.BEGINNING;
		nameText.setLayoutData(textData);
		addTextListener(nameText);

		// Value Label
		valueLabel = new Label(composite, SWT.NONE);
		valueLabel.setText(VALUE);

		// Value Text Field
		valueText = new Text(composite, SWT.BORDER | SWT.SINGLE);
		valueText.setTextLimit(TEXT_LIMIT);
		GridData valueData = new GridData(GridData.FILL_HORIZONTAL);
		valueData.horizontalSpan = GridData.BEGINNING;
		valueData.verticalSpan = 5;
		valueText.setLayoutData(valueData);
		addTextListener(valueText);

		// Label for Error Message
		Color color = display.getSystemColor(SWT.COLOR_RED); // Get a red Color
		Composite labelComposite = new Composite(parent, SWT.NONE);
		labelComposite.setLayout(new GridLayout());
		labelComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		errMessageLabel = new Label(labelComposite, SWT.COLOR_DARK_RED);
		errMessageLabel.setForeground(color);
		errMessageLabel.setText(labelMessage);
		errMessageLabel.setVisible(false);

		if (option == TemplatePreferencePage.OPTION_EDIT) {
			nameLabel.setEnabled(false);
			nameText.setEnabled(false);

			String name = TemplatePreferencePage.getSelectedItemNameFromTable();
			if (name != null) {
				nameText.setText(name);
			}
		}
	}

	/**
	 * Adds Modify listeners to the Text fields
	 *
	 * @param aText
	 */
	public void addTextListener(final Text aText) {

		ModifyListener mListener = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				String nameField = aText.getText();
				textChanged(nameField);
			}
		};

		aText.addModifyListener(mListener);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed() This method is
	 *      overridden to perform custom events on OK button.
	 */

	@Override
	protected void okPressed() {
		if (option == TemplatePreferencePage.OPTION_ADD) {
			String name = nameText.getText();
			String value = valueText.getText();

			if (name != TemplatePreferencePage.Blank && value != TemplatePreferencePage.Blank) {
				templatePreferencePage = new TemplatePreferencePage(name, value);
				templatePreferencePage.addNewDataIntoTable();

				if (TemplatePreferencePage.isDup) {
					nameText.setText(TemplatePreferencePage.Blank);
					nameText.setFocus();
					TemplatePreferencePage.isDup = false;
				} else if (!TemplatePreferencePage.isDup) {
					nameText.setFocus();
					sharedDialog.close();
				}
			}
		}

		if (option == TemplatePreferencePage.OPTION_EDIT) {
			String name = nameText.getText();
			String value = valueText.getText();

			if (!value.equals(TemplatePreferencePage.Blank)) {
				templatePreferencePage = new TemplatePreferencePage(name, value);
				templatePreferencePage.updateDataInTheTable();
			}
			sharedDialog.close();
		}
	}

	/**
	 * Pops up Message dialog if duplicate entry is found and returns
	 * confirmation.
	 *
	 * @return result
	 */

	public int popDuplicate() {

		MessageBox mBox = new MessageBox(new Shell(), SWT.ICON_INFORMATION);
		mBox.setText(TemplatePreferencePage.Message);
		mBox.setMessage(TemplatePreferencePage.DuplicateEntry);
		int result = mBox.open();
		return result;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
	 */
	@Override
	protected void cancelPressed() {

		sharedDialog.close();
	}

	/**
	 *
	 * Implements the modify listener for the text field Name and Value fields.
	 *
	 * @param textField
	 */

	private void textChanged(String textField) {

		errMessageLabel.setVisible(false);
		try {

			// Diable OK button if special characters are entererd.
			if (textField.matches(TemplatePreferencePage.Blank)) {

				oKButton = getButton(IDialogConstants.OK_ID);
				errMessageLabel.setText(labelMessage);
				errMessageLabel.setVisible(true);
				oKButton.setEnabled(false);

			}

			// Enable OK button if and only if data is entered.
			else if (!nameText.getText().equals(TemplatePreferencePage.Blank)
					&& !valueText.getText().equals(TemplatePreferencePage.Blank)) {

				oKButton = getButton(IDialogConstants.OK_ID);
				oKButton.setEnabled(true);
			}

		} catch (Exception exp) {
			TemplateEngineUtil.log(exp);
		}

	}
}
