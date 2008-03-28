/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Michael Scharf (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.provisional.api;

import java.io.OutputStream;

import org.eclipse.swt.widgets.Shell;

/**
 * Represents the terminal view as seen by a terminal connection.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the <a href="http://www.eclipse.org/dsdp/tm/">Target Management</a>
 * team.
 * </p>
 * 
 * @author Michael Scharf
 * @noimplement This interface is not intended to be implemented by clients.
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
	 */
	Shell getShell();

	/**
	 * Show a text in the terminal. If puts newlines at the beginning and the end.
	 * @param text
	 * TODO: Michael Scharf: Is this really needed?
	 */
	void displayTextInTerminal(String text);

	/**
	 * @return a stream used to write to the terminal. Any bytes written to this
	 * stream appear in the terminal or are interpreted by the emulator as
	 * control sequences.
	 */
	OutputStream getRemoteToTerminalOutputStream();

	/**
	 * Set the title of the terminal view.
	 * @param title
	 */
	void setTerminalTitle(String title);

	/**
	 * Show an error message during connect.
	 * @param msg
	 * TODO: Michael Scharf: Should be replaced by a better error notification mechanism!
	 */
	void setMsg(String msg);

}
