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
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.view;

public interface ITerminalView {

	/**
	 * Display a new Terminal view.  This method is called when the user clicks the New
	 * Terminal button in any Terminal view's toolbar.
	 */
	public void onTerminalNewTerminal();
	public void onTerminalConnect();
	public void onTerminalDisconnect();
	public void onTerminalSettings();
	public void onTerminalFontChanged();
	public boolean hasCommandInputField();
	public void setCommandInputField(boolean on);
	public boolean isScrollLock();
	public void setScrollLock(boolean b);
	public void setPinned(boolean pin);
	public boolean isPinned();
}
