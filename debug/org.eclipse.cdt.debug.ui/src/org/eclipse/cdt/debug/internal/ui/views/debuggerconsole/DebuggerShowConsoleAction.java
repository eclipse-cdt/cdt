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
package org.eclipse.cdt.debug.internal.ui.views.debuggerconsole;

import org.eclipse.cdt.debug.ui.debuggerconsole.IDebuggerConsole;
import org.eclipse.jface.action.Action;

/**
 * Shows a specific console in the DebuggerConsoleView
 */
public class DebuggerShowConsoleAction extends Action {

	private IDebuggerConsole fConsole;
	private DebuggerConsoleView fView;

	@Override
	public void run() {
		showConsole(fConsole, fView);
	}

	/**
	 * Shows the given console in the given console view.
	 *
	 * @param console the console to show
	 * @param consoleView the console view
	 */
	public static void showConsole(IDebuggerConsole console, DebuggerConsoleView consoleView) {
		if (!console.equals(consoleView.getCurrentConsole())) {
			consoleView.display(console);
		}
	}

	/**
	 * Constructs an action to display the given console.
	 *
	 * @param view the console view in which the given console is contained
	 * @param console the console
	 */
	public DebuggerShowConsoleAction(DebuggerConsoleView view, IDebuggerConsole console) {
		super(console.getName(), AS_RADIO_BUTTON);
		fConsole = console;
		fView = view;
		setImageDescriptor(console.getImageDescriptor());
	}
}
