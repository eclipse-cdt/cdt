/*******************************************************************************
 * Copyright (c) 2008, 2010 QNX Software Systems and others.
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
public class MacraigorUsb2Demon extends DefaultGDBJtagDeviceImpl {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.DefaultGDBJtagDeviceImpl#getDefaultPortNumber()
	 */
	public String getDefaultPortNumber() {
		return "8888";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.DefaultGDBJtagDeviceImpl#doDelay(int, java.util.Collection)
	 */
	public void doDelay(int delay, Collection<String> commands) {
		super.addCmd(commands, "monitor sleep " + String.valueOf(delay));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.DefaultGDBJtagDeviceImpl#doReset(java.util.Collection)
	 */
	public void doReset(Collection<String> commands) {
		super.addCmd(commands, "monitor resetrun");
	}

}
