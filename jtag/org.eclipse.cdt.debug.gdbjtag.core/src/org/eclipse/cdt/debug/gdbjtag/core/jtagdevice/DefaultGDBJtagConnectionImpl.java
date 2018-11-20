/*******************************************************************************
 * Copyright (c) 2010, 2012 Sage Electronic Engineering and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
	@Override
	public final void setDefaultDeviceConnection(String connection) {
		this.connection = connection;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.IGDBJtagDevice#doRemote(java.lang.String, java.util.Collection)
	 */
	@Override
	public void doRemote(String connection, Collection<String> commands) {
		String cmd = ""; //$NON-NLS-1$
		if (connection != null) {
			// The CLI version (target remote) does not let us know
			// that we have properly connected.  For older GDBs (<= 6.8)
			// we need this information for a DSF session.
			// The MI version does tell us, which is why we must use it
			// Bug 348043
			cmd = "-target-select remote " + connection; //$NON-NLS-1$
			addCmd(commands, cmd);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.IGDBJtagDevice#getDefaultDeviceConnection()
	 */
	@Override
	public String getDefaultDeviceConnection() {
		return connection;
	}

	@Override
	public String getDefaultIpAddress() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getDefaultPortNumber() {
		throw new UnsupportedOperationException();
	}

}
