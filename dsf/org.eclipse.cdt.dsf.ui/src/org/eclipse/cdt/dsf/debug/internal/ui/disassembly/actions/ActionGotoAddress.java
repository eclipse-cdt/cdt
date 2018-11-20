/*******************************************************************************
 * Copyright (c) 2008, 2015 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.actions;

import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.DisassemblyMessages;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional.IDisassemblyPart;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.internal.ui.text.CWordFinder;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.widgets.Shell;

public final class ActionGotoAddress extends AbstractDisassemblyAction {
	public ActionGotoAddress(IDisassemblyPart disassemblyPart) {
		super(disassemblyPart);
		setText(DisassemblyMessages.Disassembly_action_GotoAddress_label);
	}

	@Override
	public void run() {
		ITextViewer viewer = getDisassemblyPart().getTextViewer();
		IDocument document = viewer.getDocument();
		IRegion wordRegion = CWordFinder.findWord(document, viewer.getSelectedRange().x);
		String defaultValue = null;
		if (wordRegion != null) {
			try {
				defaultValue = document.get(wordRegion.getOffset(), wordRegion.getLength());
			} catch (BadLocationException e) {
				// safely ignored
			}
		}
		if (defaultValue == null) {
			defaultValue = DsfUIPlugin.getDefault().getDialogSettings().get("gotoAddress"); //$NON-NLS-1$
			if (defaultValue == null) {
				defaultValue = ""; //$NON-NLS-1$
			}
		}
		String dlgTitle = DisassemblyMessages.Disassembly_GotoAddressDialog_title;
		String dlgLabel = DisassemblyMessages.Disassembly_GotoAddressDialog_label;
		final Shell shell = getDisassemblyPart().getSite().getShell();
		InputDialog dlg = new InputDialog(shell, dlgTitle, dlgLabel, defaultValue, null);
		if (dlg.open() == IDialogConstants.OK_ID) {
			String value = dlg.getValue();
			DsfUIPlugin.getDefault().getDialogSettings().put("gotoAddress", value); //$NON-NLS-1$
			getDisassemblyPart().gotoSymbol(value);
		}
	}
}
