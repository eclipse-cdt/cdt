/*******************************************************************************
 * Copyright (c) 2017 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.launchbar.ui.internal.target;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.launchbar.ui.internal.Messages;
import org.eclipse.launchbar.ui.target.LaunchTargetWizard;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

public class LaunchTargetWizardDialog extends WizardDialog {

	public static final int ID_DELETE = IDialogConstants.CLIENT_ID + 0;

	public LaunchTargetWizardDialog(Shell parentShell, IWizard newWizard) {
		super(parentShell, newWizard);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, ID_DELETE, Messages.LaunchTargetWizardDialog_Delete, false);

		super.createButtonsForButtonBar(parent);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == ID_DELETE) {
			((LaunchTargetWizard) getWizard()).performDelete();
			setReturnCode(CANCEL);
			close();
		} else {
			super.buttonPressed(buttonId);
		}
	}

}
