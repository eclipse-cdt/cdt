/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, Inc. and others.
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
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.view;

public interface ITerminalView {

	/**
	 * Create a new terminal connection within the view.
	 */
	void onTerminalNewTerminal();
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
