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
 * @author ajin
 *
 */
public class DefaultGDBJtagDeviceImpl implements IGDBJtagDevice {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.IGDBJtagDevice#doDelay(int, java.util.Collection)
	 */
	public void doDelay(int delay, Collection commands) {
		String cmd = "monitor delay " + String.valueOf(delay * 1000);
		addCmd(commands, cmd);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.IGDBJtagDevice#doReset(java.util.Collection)
	 */
	public void doReset(Collection commands) {
		String cmd = "monitor reset run";
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
	public void doRemote(String ip, int port, Collection commands) {
		String cmd = "target remote " + ip + ":" + String.valueOf(port);
		addCmd(commands, cmd);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.IGDBJtagDevice#doHalt(java.util.Collection)
	 */
	public void doHalt(Collection commands) {
		String cmd = "monitor halt";
		addCmd(commands, cmd);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.IGDBJtagDevice#doContinue(java.util.Collection)
	 */
	public void doContinue(Collection commands) {
		String cmd = "continue";
		addCmd(commands, cmd);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.IGDBJtagDevice#doLoadImage(java.lang.String, java.lang.String, java.util.Collection)
	 */
	public void doLoadImage(String imageFileName, String imageOffset, Collection commands) {
		String file = escapeScpaces(imageFileName);
		String cmd = "restore " + file + " " + imageOffset;
		addCmd(commands, cmd);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.IGDBJtagDevice#doLoadSymbol(java.lang.String, java.lang.String, java.util.Collection)
	 */
	public void doLoadSymbol(String symbolFileName, String symbolOffset, Collection commands) {
		String file = escapeScpaces(symbolFileName);
		String cmd = "add-sym " + file + " " + symbolOffset;
		addCmd(commands, cmd);
	}
	
	protected String escapeScpaces(String file) {
		if (file.indexOf(' ') >= 0) { return '"' + file + '"'; }
		return file;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.IGDBJtagDevice#doSetPC(java.lang.String, java.util.Collection)
	 */
	public void doSetPC(String pc, Collection commands) {
		String cmd = "set $pc=0x" + pc;
		addCmd(commands, cmd);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.IGDBJtagDevice#doStopAt(java.lang.String, java.util.Collection)
	 */
	public void doStopAt(String stopAt, Collection commands) {
		String cmd = "tbreak " + stopAt;
		addCmd(commands, cmd);
	}

	/*
	 * addCmd Utility method to format commands
	 */
	protected void addCmd(Collection commands, String cmd) {
		commands.add(cmd + System.getProperty("line.separator"));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.IGDBJtagDevice#getDefaultIpAddress()
	 */
	public String getDefaultIpAddress() {
		return "localhost";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.IGDBJtagDevice#getDefaultPortNumber()
	 */
	public String getDefaultPortNumber() {
		return "10000";
	}

}
