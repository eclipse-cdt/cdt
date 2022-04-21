/*******************************************************************************
 * Copyright (c) 2010, 2022 Sage Electronic Engineering and others.
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
 *     John Dallaway - Eliminate deprecated API - bug 566462
 *     John Dallaway - Support multiple remote debug protocols - bug 535143
 *******************************************************************************/

package org.eclipse.cdt.debug.gdbjtag.core.jtagdevice;

import java.util.Collection;

import org.eclipse.cdt.debug.gdbjtag.core.IGDBJtagConnection;

/**
 * @since 7.0
 */
public class DefaultGDBJtagConnectionImpl extends DefaultGDBJtagDeviceImpl implements IGDBJtagConnection {

	private static final String PROTOCOL_REMOTE = "remote"; //$NON-NLS-1$

	private String[] protocols = new String[0];
	protected String connection = null;

	@Override
	public final void setDeviceProtocols(String[] protocols) {
		this.protocols = protocols;
	}

	@Override
	public final void setDefaultDeviceConnection(String connection) {
		this.connection = connection;
	}

	/** @deprecated call or override {@link #doTarget(String, String, Collection)} instead */
	@Override
	@Deprecated(since = "10.6")
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

	@Override
	public void doTarget(String protocol, String connection, Collection<String> commands) {
		if (PROTOCOL_REMOTE.equals(protocol)) {
			doRemote(connection, commands); // use legacy method which may have been overridden
		} else if ((connection != null) && (protocol != null)) {
			String cmd = String.format("-target-select %s %s", protocol, connection); //$NON-NLS-1$
			addCmd(commands, cmd);
		}
	}

	@Override
	public String[] getDeviceProtocols() {
		return protocols;
	}

	@Override
	public String getDefaultDeviceConnection() {
		return connection;
	}

}
