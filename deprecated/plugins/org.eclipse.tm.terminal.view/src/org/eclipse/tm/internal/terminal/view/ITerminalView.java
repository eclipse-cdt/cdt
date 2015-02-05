/*******************************************************************************
 * Copyright (c) 2006, 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Michael Scharf (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - [227537] moved actions from terminal.view to terminal plugin
 * Michael Scharf (Wind River) - [172483] switch between connections
 * Michael Scharf (Wind River) - [240023] Get rid of the terminal's "Pin" button
 * Kris De Volder (VMWare) - [392092] Extend ITerminalView API to allow programmatically opening a UI-less connector
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.view;

import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;

public interface ITerminalView {

	/**
	 * Create a new terminal connection within the view.
	 */
	void onTerminalNewTerminal();

	/**
	 * Programmatically create a new terminal connection within the view. This method
	 * does the same thing as onTerminalNewTerminal, but instead of popping up a settings
	 * dialog to allow the user fill in connection details, a connector is provided as
	 * a parameter. The connector should have all of its details pre-configured so it can
	 * be opened without requiring user input.
	 */
	void newTerminal(ITerminalConnector c);

	/**
	 * Create a new Terminal view.
	 */
	void onTerminalNewView();

	void onTerminalConnect();
	void onTerminalDisconnect();
	void onTerminalSettings();
	void onTerminalFontChanged();
	boolean hasCommandInputField();
	void setCommandInputField(boolean on);
	boolean isScrollLock();
	void setScrollLock(boolean b);
}
