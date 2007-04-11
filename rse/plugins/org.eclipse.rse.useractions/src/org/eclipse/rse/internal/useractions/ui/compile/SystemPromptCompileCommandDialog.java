package org.eclipse.rse.internal.useractions.ui.compile;

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
import org.eclipse.rse.internal.useractions.ui.SystemPromptCommandDialog;
import org.eclipse.rse.internal.useractions.ui.uda.SystemUDAResources;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog used when running a user action, which has the prompt option specified.
 * This allows the user to edit the resolved command, and the result is placed
 * in the output object.
 * <p>
 * This default implementation merely puts the command into an entry field, which
 *  the user can edit.
 */
public class SystemPromptCompileCommandDialog extends SystemPromptCommandDialog {
	/**
	 * Constructor.
	 * @param shell The parent window hosting this dialog
	 * @param command The resolved command from the user action
	 */
	public SystemPromptCompileCommandDialog(Shell shell, String command) {
		super(shell, command, SystemUDAResources.RESID_COMPILE_PROMPTCMD_TITLE);
		this.cmd = command;
		//setHelp(RSEUIPlugin.HELPPREFIX+"drnp0000");
	}

	/**
	 * Translated text configuration method.
	 * Override to return OK button label if you don't want the default
	 */
	protected String getOKButtonLabel() {
		return SystemUDAResources.RESID_COMPILE_PROMPTCMD_OKBUTTON_LABEL;
	}

	/**
	 * Translated text configuration method.
	 * Override to return OK button tooltip if you don't want the default
	 */
	protected String getOKButtonToolTipText() {
		return SystemUDAResources.RESID_COMPILE_PROMPTCMD_OKBUTTON_TOOLTIP;
	}

	/**
	 * Translated text configuration method.
	 * Override to return Cancel button tooltip if you don't want the default
	 */
	protected String getCancelButtonToolTipText() {
		return SystemUDAResources.RESID_COMPILE_PROMPTCMD_CANCELBUTTON_TOOLTIP;
	}

	/**
	 * Translated text configuration method.
	 * Override to return verbage message if you don't want the default
	 */
	protected String getVerbage() {
		return SystemUDAResources.RESID_COMPILE_PROMPTCMD_VERBAGE_LABEL;
	}

	/**
	 * Translated text configuration method.
	 * Override to return label for the command prompt, if you don't want the default
	 */
	protected String getPromptLabel() {
		return SystemUDAResources.RESID_COMPILE_PROMPTCMD_PROMPT_LABEL;
	}

	/**
	 * Translated text configuration method.
	 * Override to return tooltip text for the command prompt, if you don't want the default
	 */
	protected String getPromptToolTipText() {
		return SystemUDAResources.RESID_COMPILE_PROMPTCMD_PROMPT_TOOLTIP;
	}
}
