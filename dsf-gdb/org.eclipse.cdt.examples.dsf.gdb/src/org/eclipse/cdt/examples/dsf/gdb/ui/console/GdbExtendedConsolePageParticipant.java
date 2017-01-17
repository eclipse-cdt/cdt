/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.gdb.ui.console;

import org.eclipse.cdt.dsf.gdb.internal.ui.console.GdbBasicCliConsole;
import org.eclipse.cdt.dsf.gdb.internal.ui.console.GdbFullCliConsolePage;
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
		if (fConsole instanceof GdbBasicCliConsole) {
			IToolBarManager toolBarManager = fPage.getSite().getActionBars().getToolBarManager();

			GdbExtendedSpecialBackgroundToggle backgroundAction = new GdbExtendedSpecialBackgroundToggle(fConsole);
			toolBarManager.appendToGroup(IConsoleConstants.OUTPUT_GROUP, backgroundAction);						
		} else if (fPage instanceof GdbFullCliConsolePage) {
			// Add the paste console action to full console only (just to show how)
			IToolBarManager toolBarManager = fPage.getSite().getActionBars().getToolBarManager();

			ITerminalViewControl terminalControl = ((GdbFullCliConsolePage)fPage).getTerminalViewControl();
			GdbExtendedInfoThreadsAction pasteStringAction = new GdbExtendedInfoThreadsAction(terminalControl);
			toolBarManager.appendToGroup(IConsoleConstants.OUTPUT_GROUP, pasteStringAction);
		}
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
