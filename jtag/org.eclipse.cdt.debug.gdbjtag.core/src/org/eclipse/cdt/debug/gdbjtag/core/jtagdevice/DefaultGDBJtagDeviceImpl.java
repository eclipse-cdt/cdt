/*******************************************************************************
 * Copyright (c) 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Andy Jin - Hardware debugging UI improvements, bug 229946
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.IGDBJtagDevice#doDelay(int, java.util.Collection)
	 */
	public void doDelay(int delay, Collection<String> commands) {
		String cmd = "monitor delay " + String.valueOf(delay * 1000); //$NON-NLS-1$
		addCmd(commands, cmd);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.IGDBJtagDevice#doReset(java.util.Collection)
	 */
	public void doReset(Collection<String> commands) {
		String cmd = "monitor reset run"; //$NON-NLS-1$
		addCmd(commands, cmd);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.IGDBJtagDevice#getDefaultDelay()
	 */
	public int getDefaultDelay() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.IGDBJtagDevice#doRemote(java.lang.String, int, java.util.Collection)
	 */
	public void doRemote(String ip, int port, Collection<String> commands) {
		String cmd = "target remote " + ip + ":" + String.valueOf(port); //$NON-NLS-1$ //$NON-NLS-2$
		addCmd(commands, cmd);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.IGDBJtagDevice#doHalt(java.util.Collection)
	 */
	public void doHalt(Collection<String> commands) {
		String cmd = "monitor halt"; //$NON-NLS-1$
		addCmd(commands, cmd);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.IGDBJtagDevice#doContinue(java.util.Collection)
	 */
	public void doContinue(Collection<String> commands) {
		String cmd = "continue"; //$NON-NLS-1$
		addCmd(commands, cmd);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.IGDBJtagDevice#doLoadImage(java.lang.String, java.lang.String, java.util.Collection)
	 */
	public void doLoadImage(String imageFileName, String imageOffset, Collection<String> commands) {
		addCmd(commands, "load " + escapeScpaces(imageFileName) + ' ' + imageOffset);			
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.IGDBJtagDevice#doLoadSymbol(java.lang.String, java.lang.String, java.util.Collection)
	 */
	public void doLoadSymbol(String symbolFileName, String symbolOffset, Collection<String> commands) {
		String file = escapeScpaces(symbolFileName);
		if (symbolOffset == null || (symbolOffset.length() == 0)) {
			addCmd(commands, "symbol-file " + file);
		}
		else {
			addCmd(commands, "add-sym " + file + " " + symbolOffset);			
		}
	}
	
	protected String escapeScpaces(String file) {
		if (file.indexOf(' ') >= 0) { return '"' + file + '"'; }
		return file;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.IGDBJtagDevice#doSetPC(java.lang.String, java.util.Collection)
	 */
	public void doSetPC(String pc, Collection<String> commands) {
		String cmd = "set $pc=0x" + pc; //$NON-NLS-1$
		addCmd(commands, cmd);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.IGDBJtagDevice#doStopAt(java.lang.String, java.util.Collection)
	 */
	public void doStopAt(String stopAt, Collection<String> commands) {
		String cmd = "tbreak " + stopAt; //$NON-NLS-1$
		addCmd(commands, cmd);
	}

	/*
	 * addCmd Utility method to format commands
	 */
	protected void addCmd(Collection<String> commands, String cmd) {
		commands.add(cmd + LINESEP);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.IGDBJtagDevice#getDefaultIpAddress()
	 */
	public String getDefaultIpAddress() {
		return "localhost"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.IGDBJtagDevice#getDefaultPortNumber()
	 */
	public String getDefaultPortNumber() {
		return "10000"; //$NON-NLS-1$
	}

}
