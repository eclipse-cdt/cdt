/*******************************************************************************
 * Copyright (c) 2008, 2012 QNX Software Systems and others.
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
	@Override
	public String getDefaultPortNumber() {
		return "8888";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.DefaultGDBJtagDeviceImpl#doDelay(int, java.util.Collection)
	 */
	@Override
	public void doDelay(int delay, Collection<String> commands) {
		super.addCmd(commands, "monitor sleep " + String.valueOf(delay));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.DefaultGDBJtagDeviceImpl#doReset(java.util.Collection)
	 */
	@Override
	public void doReset(Collection<String> commands) {
		super.addCmd(commands, "monitor resetrun");
	}

}
