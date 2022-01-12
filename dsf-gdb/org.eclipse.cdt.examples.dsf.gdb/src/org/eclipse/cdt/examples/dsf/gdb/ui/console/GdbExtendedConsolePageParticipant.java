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

import org.eclipse.cdt.dsf.gdb.internal.ui.console.GdbBasicCliConsole;
import org.eclipse.cdt.dsf.gdb.internal.ui.console.GdbFullCliConsolePage;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.tm.internal.terminal.control.ITerminalViewControl;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.part.IPageBookViewPage;

/**
 * An example console page participant for both standard console pages of the Debugger
 * Console view.
 * It adds a button to the GdbBasicCliConsolePage and different one to the GdbFullCliConsolePage.
 */
public class GdbExtendedConsolePageParticipant implements IConsolePageParticipant {

	private IPageBookViewPage fPage;
	private IConsole fConsole;

	@Override
	public void init(IPageBookViewPage page, IConsole console) {
		fPage = page;
		fConsole = console;

		addButtons();
	}

	private void addButtons() {
		IToolBarManager toolBarManager = fPage.getSite().getActionBars().getToolBarManager();
		IAction action = null;
		if (fConsole instanceof GdbBasicCliConsole) {
			action = new GdbExtendedSpecialBackgroundToggle(fConsole);
		} else if (fPage instanceof GdbFullCliConsolePage) {
			ITerminalViewControl terminalControl = ((GdbFullCliConsolePage) fPage).getTerminalViewControl();
			action = new GdbExtendedInfoThreadsAction(terminalControl);
		}
		toolBarManager.appendToGroup(IConsoleConstants.OUTPUT_GROUP, action);
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return null;
	}

	@Override
	public void dispose() {
	}

	@Override
	public void activated() {
	}

	@Override
	public void deactivated() {
	}
}
