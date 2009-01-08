/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.actions;

import java.math.BigInteger;

import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.DisassemblyMessages;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.DisassemblyPart;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.IDisassemblyPart;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.widgets.Shell;

public final class ActionGotoAddress extends AbstractDisassemblyAction {
	public ActionGotoAddress(IDisassemblyPart disassemblyPart) {
		super(disassemblyPart);
		setText(DisassemblyMessages.Disassembly_action_GotoAddress_label);
	}
	@Override
	public void run() {
		IInputValidator validator = new IInputValidator() {
			public String isValid(String input) {
				if (input == null || input.length() == 0)
					return " "; //$NON-NLS-1$
				try {
					BigInteger address= DisassemblyPart.decodeAddress(input);
					if (address.compareTo(BigInteger.ZERO) < 0) {
						return DisassemblyMessages.Disassembly_GotoAddressDialog_error_invalid_address;
					}
				} catch (NumberFormatException x) {
					return DisassemblyMessages.Disassembly_GotoAddressDialog_error_not_a_number; //;
				}
				return null;
			}
		};
		String defaultValue = ((ITextSelection)getDisassemblyPart().getSite().getSelectionProvider().getSelection()).getText();
		if (validator.isValid(defaultValue) != null) {
			defaultValue = DsfUIPlugin.getDefault().getDialogSettings().get("gotoAddress"); //$NON-NLS-1$
			if (validator.isValid(defaultValue) != null) {
				defaultValue = ""; //$NON-NLS-1$
			}
		}
		String dlgTitle = DisassemblyMessages.Disassembly_GotoAddressDialog_title;
		String dlgLabel = DisassemblyMessages.Disassembly_GotoAddressDialog_label;
		final Shell shell= getDisassemblyPart().getSite().getShell();
		InputDialog dlg = new InputDialog(shell, dlgTitle, dlgLabel, defaultValue, validator);
		if (dlg.open() == IDialogConstants.OK_ID) {
			String value = dlg.getValue();
			BigInteger address= DisassemblyPart.decodeAddress(value);
			DsfUIPlugin.getDefault().getDialogSettings().put("gotoAddress", value); //$NON-NLS-1$
			getDisassemblyPart().gotoAddress(address);
		}
	}
}
