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
package org.eclipse.cdt.dsf.gdb.internal.ui.console;

import java.io.OutputStream;

import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;

/**
 * Interface to connect multiple page terminal controls with their single associated GDB process I/O.
 */
public interface IGdbTerminalControlConnector {

	/**
	 * Adds a terminal control which is ready to receive the process output
	 *
	 * @param terminalControl
	 */
	void addPageTerminalControl(ITerminalControl terminalControl);

	/**
	 * Removes a registered terminal control
	 *
	 * @param terminalControl
	 */
	void removePageTerminalControl(ITerminalControl terminalControl);

	/**
	 * @return The OutputStream shared among the managed terminal control instances
	 */
	public OutputStream getTerminalToRemoteStream();
}
