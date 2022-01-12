/*******************************************************************************
 * Copyright (c) 2006, 2018 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 * Michael Scharf (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 * Anton Leherbauer (Wind River) - [458398] Add support for normal/application cursor keys mode
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.control.impl;

import java.io.OutputStream;

import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;

/**
 * need a better name!
 * @author Michael Scharf
 *
 */
public interface ITerminalControlForText {

	TerminalState getState();

	void setState(TerminalState state);

	void setTerminalTitle(String title);

	ITerminalConnector getTerminalConnector();

	OutputStream getOutputStream();

	/**
	 * Enable/disable Application Cursor Keys mode (DECCKM)
	 * @param enable
	 */
	void enableApplicationCursorKeys(boolean enable);

}
