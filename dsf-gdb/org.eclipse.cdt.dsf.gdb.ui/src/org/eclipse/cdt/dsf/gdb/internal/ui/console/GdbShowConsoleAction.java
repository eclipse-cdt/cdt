/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.console;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.console.IConsole;

/**
 * Shows a specific console in the console view
 */
public class GdbShowConsoleAction extends Action {

	private IConsole fConsole;
	private GdbConsoleView fView;

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
	public static void showConsole(IConsole console, GdbConsoleView consoleView) {
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
	public GdbShowConsoleAction(GdbConsoleView view, IConsole console) {
		super(console.getName(), AS_RADIO_BUTTON);
		fConsole = console;
		fView = view;
		setImageDescriptor(console.getImageDescriptor());
	}
}
