package org.eclipse.rse.internal.useractions.ui;

/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
import org.eclipse.rse.internal.useractions.ui.uda.SystemUDAResources;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.dialogs.SystemPromptDialog;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog used when to prompt the user with a command, with the intention that 
 *  the user can change.
 * <p>
 * This default implementation merely puts the command into an entry field, which
 *  the user can edit.
 * <p>
 * Typically this is subclassed or configured to supply unique translated text.
 */
public class SystemPromptCommandDialog extends SystemPromptDialog  {
	// gui
	protected Text cmdText;
	protected Label newNamePrompt;
	// input
	protected String cmd;
	// output
	protected String newCmdString;
	// state
	protected SystemMessage errorMessage;

	/**
	 * Constructor.
	 * @param shell The parent window hosting this dialog
	 * @param command The resolved command from the user action
	 */
	public SystemPromptCommandDialog(Shell shell, String command) {
		this(shell, command, SystemUDAResources.RESID_UDA_PROMPTCMD_TITLE);
	}

	/**
	 * Constructor when specifying your own title.
	 * @param shell The parent window hosting this dialog
	 * @param command The resolved command from the user action
	 */
	public SystemPromptCommandDialog(Shell shell, String command, String title) {
		super(shell, title);
		this.cmd = command;
		super.setOkButtonLabel(getOKButtonLabel());
		super.setOkButtonToolTipText(getOKButtonToolTipText());
		super.setCancelButtonToolTipText(getCancelButtonToolTipText());
		//setHelp(RSEUIPlugin.HELPPREFIX+"drnp0000");
	}

	// --------------	
	// MRI METHODS...
	// --------------
	/**
	 * Translated text configuration method.
	 * Override to return OK button label if you don't want the default
	 */
	protected String getOKButtonLabel() {
		return SystemUDAResources.RESID_UDA_PROMPTCMD_OKBUTTON_LABEL;
	}

	/**
	 * Translated text configuration method.
	 * Override to return OK button tooltip if you don't want the default
	 */
	protected String getOKButtonToolTipText() {
		return SystemUDAResources.RESID_UDA_PROMPTCMD_OKBUTTON_TOOLTIP;
	}

	/**
	 * Translated text configuration method.
	 * Override to return Cancel button tooltip if you don't want the default
	 */
	protected String getCancelButtonToolTipText() {
		return SystemUDAResources.RESID_UDA_PROMPTCMD_CANCELBUTTON_TOOLTIP;
	}

	/**
	 * Translated text configuration method.
	 * Override to return verbage message if you don't want the default
	 */
	protected String getVerbage() {
		return SystemUDAResources.RESID_UDA_PROMPTCMD_VERBAGE_LABEL;
	}

	/**
	 * Translated text configuration method.
	 * Override to return label for the command prompt, if you don't want the default
	 */
	protected String getPromptLabel() {
		return SystemUDAResources.RESID_UDA_PROMPTCMD_PROMPT_LABEL;
	}

	/**
	 * Translated text configuration method.
	 * Override to return tooltip text for the command prompt, if you don't want the default
	 */
	protected String getPromptToolTipText() {
		return SystemUDAResources.RESID_UDA_PROMPTCMD_PROMPT_TOOLTIP;
	}

	/**
	 * Create GUI controls, populate into given composite.
	 */
	protected Control createInner(Composite parent) {
		// Inner composite
		int nbrColumns = 1;
		Composite composite_prompts = SystemWidgetHelpers.createComposite(parent, nbrColumns);
		// VERBAGE	
		SystemWidgetHelpers.createVerbiage(composite_prompts, getVerbage(), nbrColumns, false, 250);
		addFillerLine(composite_prompts, nbrColumns);
		// ENTRY FIELD		
		SystemWidgetHelpers.createLabel(composite_prompts, getPromptLabel());
		cmdText = SystemWidgetHelpers.createMultiLineTextField(composite_prompts, null, 65);
		((GridData) cmdText.getLayoutData()).widthHint = 350;
		cmdText.setToolTipText(getPromptToolTipText());
		cmdText.setTextLimit(2000);
		cmdText.setText(cmd);
		// add keystroke listeners...
		cmdText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validateInput();
			}
		});
		return composite_prompts;
	}

	/**
	 * Return widget to set initial focus to
	 */
	protected Control getInitialFocusControl() {
		return cmdText;
	}

	/**
	 * Called when user presses OK button. 
	 * Return true to close dialog.
	 * Return false to not close dialog.
	 */
	protected boolean processOK() {
		newCmdString = cmdText.getText().trim();
		boolean closeDialog = verify();
		if (closeDialog) {
			setOutputObject(newCmdString);
		}
		return closeDialog;
	}

	/**
	 * Verifies all input. Currently, we do no verification!
	 * @return true if there are no errors in the user input
	 */
	public boolean verify() {
		//clearErrorMessage();				
		//errorMessage = null;
		//if (errorMessage != null)
		//  cmdText.setFocus();
		return (errorMessage == null);
	}

	/**
	 * This hook method is called whenever the text changes in the cmd input field.
	 * Currently not used.
	 */
	protected SystemMessage validateInput() {
		//errorMessage = null;
		//if (errorMessage != null)
		//   displayErrorMessage(errorMessage);
		//else
		//   clearErrorMessage();
		setPageComplete();
		return errorMessage;
	}

	/**
	 * This method can be called by the dialog or wizard page host, to decide whether to enable
	 * or disable the next, final or ok buttons. It returns true if the minimal information is
	 * available and is correct.
	 */
	public boolean isPageComplete() {
		boolean pageComplete = false;
		if (errorMessage == null) {
			String theNewCmd = cmdText.getText().trim();
			pageComplete = (theNewCmd.length() > 0);
		}
		return pageComplete;
	}

	/**
	 * Inform caller of page-complete status of this form
	 */
	public void setPageComplete() {
		setPageComplete(isPageComplete());
	}

	/**
	 * Returns the user-edited command
	 */
	public String getCommand() {
		return newCmdString;
	}
}
