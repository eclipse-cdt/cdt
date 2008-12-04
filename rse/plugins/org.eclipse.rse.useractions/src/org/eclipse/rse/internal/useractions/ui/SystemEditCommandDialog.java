package org.eclipse.rse.internal.useractions.ui;

/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Kevin Doyle	(IBM) - [239704] No Validation for Command textbox in Work with Compile and User Action dialogs
 *******************************************************************************/
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.rse.internal.useractions.ui.uda.SystemUDAResources;
import org.eclipse.rse.internal.useractions.ui.validators.ValidatorUserActionCommand;
import org.eclipse.rse.shells.ui.view.SystemCommandEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog used for editing command text in a resizable widget
 */
public class SystemEditCommandDialog extends Dialog {
	// gui
	protected Label newNamePrompt;
	// input
	protected String cmd;
	// output
	protected String newCmdString;
	protected boolean ignoreChanges;
	// state
	protected SystemCommandViewerConfiguration sourceViewerConfiguration;
	protected SystemCommandEditor textCommand;
	protected Button insertVariableButton;
	protected int style;
	protected int INSERT_ID = 10;

	/**
	 * Constructor.
	 * @param shell The parent window hosting this dialog
	 * @param command The resolved command from the user action
	 * @param sourceViewerConfiguration configration for editor
	 * @param style for editor
	 */
	public SystemEditCommandDialog(Shell shell, String command, SystemCommandViewerConfiguration sourceViewerConfiguration, int style) {
		this(shell, command, SystemUDAResources.RESID_UDA_PROMPTCMD_TITLE, sourceViewerConfiguration, style);
	}

	/**
	 * Constructor when specifying your own title.
	 * @param shell The parent window hosting this dialog
	 * @param command The resolved command from the user action
	 * @param title title for the dialog
	 * @param sourceViewerConfiguration configration for editor
	 * @param style for editor
	 */
	public SystemEditCommandDialog(Shell shell, String command, String title, SystemCommandViewerConfiguration sourceViewerConfiguration, int style) {
		super(shell);
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
		this.style = style;
		this.cmd = command;
		this.sourceViewerConfiguration = sourceViewerConfiguration;
	}

	/**
	 * Create GUI controls, populate into given composite.
	 */
	protected Control createDialogArea(Composite gparent) {
		Composite parent = new Composite(gparent, SWT.NONE);
		GridData data = new GridData();
		data.heightHint = 100;
		data.widthHint = 400;
		parent.setLayout(new GridLayout());
		parent.setLayoutData(data);
		createEditor(parent, 5, sourceViewerConfiguration, cmd);
		String title = SystemUDAResources.RESID_UDA_COMMAND_LABEL;
		getShell().setText(title);
		return parent;
	}

	/**
	 * Create the editor widget
	 */
	private SourceViewer createEditor(Composite parent, int columnSpan, SystemCommandViewerConfiguration sourceViewerConfiguration, String cmd) {
		textCommand = new SystemCommandEditor(null, parent, style, columnSpan, sourceViewerConfiguration, cmd, SystemUDAResources.RESID_UDA_INSERTVAR_BUTTON_LABEL);
		textCommand.setCommandValidator(new ValidatorUserActionCommand());
		return textCommand;
	}

	protected void createButtonsForButtonBar(Composite parent) {
		String label = SystemUDAResources.RESID_UDA_INSERTVAR_BUTTON_LABEL;
		createButton(parent, INSERT_ID, label, false);
		super.createButtonsForButtonBar(parent);
	}

	/**
	 * Return widget to set initial focus to
	 */
	protected Control getInitialFocusControl() {
		return textCommand.getControl();
	}

	protected void buttonPressed(int buttonId) {
		if (IDialogConstants.OK_ID == buttonId) {
			processOK();
			textCommand.getTextWidget().dispose();
		}
		if (buttonId == INSERT_ID) {
			textCommand.getTextWidget().setFocus();
			textCommand.doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
		}
		super.buttonPressed(buttonId);
	}

	/**
	 * Called when user presses OK button. 
	 * Return true to close dialog.
	 * Return false to not close dialog.
	 */
	protected boolean processOK() {
		newCmdString = textCommand.getDocument().get().trim();
		return true;
	}

	/**
	 * Returns the user-edited command
	 */
	public String getCommand() {
		return newCmdString;
	}
}
