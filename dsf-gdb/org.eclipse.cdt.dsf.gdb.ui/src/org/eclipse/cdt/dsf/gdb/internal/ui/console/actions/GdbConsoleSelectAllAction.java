/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.console.actions;

import org.eclipse.cdt.dsf.gdb.internal.ui.console.ConsoleMessages;
import org.eclipse.jface.action.Action;
import org.eclipse.tm.internal.terminal.control.ITerminalViewControl;

/**
 * Action to Select-All the available text from the associated terminal
 */
public class GdbConsoleSelectAllAction extends Action {

	private final ITerminalViewControl fTerminalCtrl;

	public GdbConsoleSelectAllAction(ITerminalViewControl terminalControl) {
		fTerminalCtrl = terminalControl;
		if (fTerminalCtrl == null || fTerminalCtrl.isDisposed()) {
			setEnabled(false);
		}
		setText(ConsoleMessages.ConsoleSelectAllAction_name);
		setToolTipText(ConsoleMessages.ConsoleSelectAllAction_description);
	}

	@Override
	public void run() {
		if (fTerminalCtrl != null) {
			fTerminalCtrl.selectAll();
		}
	}
}
