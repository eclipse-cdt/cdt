/*******************************************************************************
 * Copyright (c) 2006, 2015 Red Hat Inc..
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class SingleInputDialog extends InputDialog {

	private String firstMessage;

	public SingleInputDialog(Shell parentShell, String firstMessage, String dialogTitle, String dialogMessage,
			String initialValue, IInputValidator validator) {
		super(parentShell, dialogTitle, dialogMessage, initialValue, validator);

		this.firstMessage = firstMessage;
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		// create composite
		Composite composite = (Composite) super.createDialogArea(parent);

		CLabel label0 = new CLabel(composite, SWT.WRAP);
		label0.setText(firstMessage);
		GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL
				| GridData.VERTICAL_ALIGN_CENTER);
		data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
		label0.setLayoutData(data);
		label0.setFont(parent.getFont());

		// remove error message dialog from focusing.
		composite.getTabList()[2].setVisible(false);
		composite.getTabList()[2].setEnabled(false);

		return composite;
	}

}
