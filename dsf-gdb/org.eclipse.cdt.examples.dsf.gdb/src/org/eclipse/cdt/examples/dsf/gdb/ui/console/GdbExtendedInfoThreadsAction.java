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
package org.eclipse.cdt.examples.dsf.gdb.ui.console;

import org.eclipse.jface.action.Action;
import org.eclipse.tm.internal.terminal.control.ITerminalViewControl;

/**
 * Action to requests threads info from the full GDB console
 */
public class GdbExtendedInfoThreadsAction extends Action {

	private final ITerminalViewControl fTerminalCtrl;

	public GdbExtendedInfoThreadsAction(ITerminalViewControl terminalControl) {
		fTerminalCtrl = terminalControl;
		if (fTerminalCtrl == null || fTerminalCtrl.isDisposed()) {
			setEnabled(false);
		}

		setText(GdbExtendedConsoleMessages.Request_Thread_Info);
		setToolTipText(GdbExtendedConsoleMessages.Request_Thread_Info_Tip);
	}

	@Override
	public void run() {
		fTerminalCtrl.pasteString("info threads\n"); //$NON-NLS-1$
	}
}
