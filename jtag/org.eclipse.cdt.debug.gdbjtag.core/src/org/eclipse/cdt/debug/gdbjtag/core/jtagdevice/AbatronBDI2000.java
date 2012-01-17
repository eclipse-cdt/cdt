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

public class AbatronBDI2000 extends DefaultGDBJtagDeviceImpl {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.DefaultGDBJtagDeviceImpl#getDefaultIpAddress()
	 */
	@Override
	public String getDefaultIpAddress() {
		return "bdi2000";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.DefaultGDBJtagDeviceImpl#getDefaultPortNumber()
	 */
	@Override
	public String getDefaultPortNumber() {
		return "2001";
	}

}
