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
 * Action to clear the contents of the associated GDB terminal
 */
public class GdbConsoleClearAction extends Action {

	private final ITerminalViewControl fTerminalCtrl;

	public GdbConsoleClearAction(ITerminalViewControl terminalControl) {
		fTerminalCtrl = terminalControl;
		if (fTerminalCtrl == null || fTerminalCtrl.isDisposed()) {
			setEnabled(false);
		}

		setText(ConsoleMessages.ConsoleClearAction_name);
		setToolTipText(ConsoleMessages.ConsoleClearAction_description);
		setImageDescriptor(GdbUIPlugin.getImageDescriptor(IConsoleImagesConst.IMG_CONSOLE_CLEAR_ACTIVE_COLOR));
		setDisabledImageDescriptor(
				GdbUIPlugin.getImageDescriptor(IConsoleImagesConst.IMG_CONSOLE_CLEAR_DISABLED_COLOR));
	}

	@Override
	public void run() {
		if (fTerminalCtrl != null) {
			fTerminalCtrl.clearTerminal();
		}
	}
}
