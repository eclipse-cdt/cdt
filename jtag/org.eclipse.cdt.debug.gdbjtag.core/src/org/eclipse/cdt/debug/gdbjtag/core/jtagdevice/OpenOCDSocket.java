/*******************************************************************************
 * Copyright (c) 2008, 2018 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Andy Jin - Hardware debugging UI improvements, bug 229946
 *     John Dallaway - OpenOCD extensions, bug 494059
 *******************************************************************************/
package org.eclipse.cdt.debug.gdbjtag.core.jtagdevice;

import java.util.Collection;


/**
 * @since 9.2
 */
public class OpenOCDSocket extends DefaultGDBJtagDeviceImpl {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.DefaultGDBJtagDeviceImpl#getDefaultPortNumber()
	 */
	@Override
	public String getDefaultPortNumber() {
		return "3333"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.DefaultGDBJtagDeviceImpl#doDelay(int, java.util.Collection)
	 */
	@Override
	public void doDelay(int delay, Collection <String>commands) {
		addCmd(commands, "monitor sleep " + String.valueOf(delay * 1000)); //$NON-NLS-1$
	}

}
