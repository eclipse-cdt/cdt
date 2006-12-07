/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Michael Scharf (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 *******************************************************************************/
package org.eclipse.tm.terminal;

import org.eclipse.swt.widgets.Shell;

/**
 * Represents the terminal view as seen by a terminal connection.
 * 
 * <p> Not to be implemented by clients.
 * @author Michael Scharf
 */
public interface ITerminalControl {
	
	/**
	 * @return the current state of the connection
	 */
	TerminalState getState();

	/**
	 * @param state
	 */
	void setState(TerminalState state);
	
	/**
	 * A shell to show dialogs.
	 * @return the shell in which the terminal is shown.
	 * TODO: Michael Scharf: it's not clear to me what the meaning of the open state is
	 */
	Shell getShell();

	/**
	 * Show a text in the terminal. If pots newlines at the beginning and the end.
	 * @param text
	 * TODO: Michael Scharf: Is this really needed? (use {@link #displayTextInTerminal(String)}
	 */
	void displayTextInTerminal(String text);

	/**
	 * Write a string directly to the terminal.
	 * @param txt
	 */
	void writeToTerminal(String txt);

	/**
	 * Set the title of the terminal view.
	 * @param title
	 */
	void setTerminalTitle(String title);

	/**
	 * Show an error message during connect.
	 * @param msg
	 * TODO: Michael Scharf: Should be replaced by a better error notification mechansim!
	 */
	void setMsg(String msg);
	
}