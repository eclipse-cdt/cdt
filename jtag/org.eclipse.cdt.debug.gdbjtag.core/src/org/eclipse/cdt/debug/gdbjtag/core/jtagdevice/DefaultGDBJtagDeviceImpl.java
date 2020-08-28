/*******************************************************************************
 * Copyright (c) 2008, 2020 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Andy Jin - Hardware debugging UI improvements, bug 229946
 *     John Dallaway - Use GDB/MI for temporary breakpoint, bug 525726
 *     John Dallaway - Eliminate deprecated API, bug 566462
 *******************************************************************************/

package org.eclipse.cdt.debug.gdbjtag.core.jtagdevice;

import java.util.Collection;

/**
 * Default implementation of the "jtag device"
 *
 */
public class DefaultGDBJtagDeviceImpl implements IGDBJtagDevice {

	/**
	 * @since 7.0
	 */
	protected static final String LINESEP = System.getProperty("line.separator"); //$NON-NLS-1$

	@Override
	public void doDelay(int delay, Collection<String> commands) {
		String cmd = "monitor delay " + String.valueOf(delay * 1000); //$NON-NLS-1$
		addCmd(commands, cmd);
	}

	@Override
	public void doReset(Collection<String> commands) {
		String cmd = "monitor reset run"; //$NON-NLS-1$
		addCmd(commands, cmd);
	}

	@Override
	public int getDefaultDelay() {
		return 0;
	}

	@Override
	public void doHalt(Collection<String> commands) {
		String cmd = "monitor halt"; //$NON-NLS-1$
		addCmd(commands, cmd);
	}

	@Override
	public void doContinue(Collection<String> commands) {
		// The CLI version "continue" causes GDB to block and would not be
		// able to respond other MI commands, this is a problem
		// when running in async mode as it depends on the processing
		// of MI commands e.g. to suspend the program.
		//   Therefore we need to use the MI command version "-exec-continue"
		// which does not block GDB.
		String cmd = "-exec-continue"; //$NON-NLS-1$
		addCmd(commands, cmd);
	}

	@Override
	public void doLoadImage(String imageFileName, String imageOffset, Collection<String> commands) {
		addCmd(commands, "load " + escapeScpaces(imageFileName) + ' ' + imageOffset); //$NON-NLS-1$
	}

	@Override
	public void doLoadSymbol(String symbolFileName, String symbolOffset, Collection<String> commands) {
		String file = escapeScpaces(symbolFileName);
		if (symbolOffset == null || (symbolOffset.length() == 0)) {
			addCmd(commands, "symbol-file " + file); //$NON-NLS-1$
		} else {
			addCmd(commands, "add-symbol-file " + file + " " + symbolOffset); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	protected String escapeScpaces(String file) {
		if (file.indexOf(' ') >= 0) {
			return '"' + file + '"';
		}
		return file;
	}

	@Override
	public void doSetPC(String pc, Collection<String> commands) {
		String cmd = "set $pc=0x" + pc; //$NON-NLS-1$
		addCmd(commands, cmd);
	}

	@Override
	public void doStopAt(String stopAt, Collection<String> commands) {
		String cmd = "-break-insert -t -f " + stopAt; //$NON-NLS-1$
		addCmd(commands, cmd);
	}

	/*
	 * addCmd Utility method to format commands
	 */
	protected void addCmd(Collection<String> commands, String cmd) {
		commands.add(cmd + LINESEP);
	}

}
