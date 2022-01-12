/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.console.actions;

import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.internal.ui.console.ConsoleMessages;
import org.eclipse.cdt.dsf.gdb.internal.ui.console.IConsoleImagesConst;
import org.eclipse.jface.action.Action;
import org.eclipse.tm.internal.terminal.control.ITerminalViewControl;

/**
 * Action to paste from the clip-board to the associated terminal
 */
public class GdbConsolePasteAction extends Action {

	private final ITerminalViewControl fTerminalCtrl;

	public GdbConsolePasteAction(ITerminalViewControl terminalControl) {
		fTerminalCtrl = terminalControl;
		if (fTerminalCtrl == null || fTerminalCtrl.isDisposed()) {
			setEnabled(false);
		}
		setText(ConsoleMessages.ConsolePasteAction_name);
		setToolTipText(ConsoleMessages.ConsolePasteAction_description);
		setImageDescriptor(GdbUIPlugin.getImageDescriptor(IConsoleImagesConst.IMG_CONSOLE_PASTE_ACTIVE_COLOR));
		setDisabledImageDescriptor(GdbUIPlugin.getImageDescriptor(IConsoleImagesConst.IMG_CONSOLE_PASTE_ACTIVE_COLOR));
	}

	@Override
	public void run() {
		if (fTerminalCtrl != null) {
			fTerminalCtrl.paste();
		}
	}
}