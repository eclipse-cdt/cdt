/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems, Inc. - initial implementation
 *     
 *******************************************************************************/
package org.eclipse.tm.terminal.internal.control;

import java.io.OutputStream;

import org.eclipse.tm.terminal.ITerminalConnector;
import org.eclipse.tm.terminal.TerminalState;

/**
 * need a better name!
 * @author Michael Scharf
 *
 */
public interface ITerminalControlForText {
	
	TerminalState getState();
	void setState(TerminalState state);
	void setTerminalTitle(String title);
	
	ITerminalConnector getTerminalConnection();

	void disconnectTerminal();

	OutputStream getOutputStream();
	
}