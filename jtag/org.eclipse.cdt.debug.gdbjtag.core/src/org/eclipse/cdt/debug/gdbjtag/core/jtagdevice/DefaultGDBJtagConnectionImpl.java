/*******************************************************************************
 * Copyright (c) 2010 Sage Electronic Engineering and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruce Griffith,Sage Electronic Engineering, LLC - bug 305943
 *              - API generalization to become transport-independent (e.g. to
 *                allow connections via serial ports and pipes).
 *******************************************************************************/

package org.eclipse.cdt.debug.gdbjtag.core.jtagdevice;

import java.util.Collection;

import org.eclipse.cdt.debug.gdbjtag.core.IGDBJtagConnection;

/**
 * @since 7.0
 */
public class DefaultGDBJtagConnectionImpl extends DefaultGDBJtagDeviceImpl implements IGDBJtagConnection {

	protected String connection = null;

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.IGDBJtagDevice#doDelay(int, java.util.Collection)
	 */
	public final void setDefaultDeviceConnection(String connection) {
		this.connection = connection;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.IGDBJtagDevice#doRemote(java.lang.String, java.util.Collection)
	 */
	public void doRemote(String connection, Collection<String> commands) {
		String cmd = ""; //$NON-NLS-1$
		if (connection != null) {
			cmd = "target remote " + connection; //$NON-NLS-1$
			addCmd(commands, cmd);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.IGDBJtagDevice#getDefaultDeviceConnection()
	 */
	public String getDefaultDeviceConnection() {
		return connection;
	}

	public String getDefaultIpAddress() {
		throw new UnsupportedOperationException();
	}

	public String getDefaultPortNumber() {
		throw new UnsupportedOperationException();
	}

}
