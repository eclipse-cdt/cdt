/*******************************************************************************
 * Copyright (c) 2008, 2022 QNX Software Systems and others.
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
 *     Sage Electronic Engineering, LLC - bug 305943
 *              - API generalization to become transport-independent (e.g. to
 *                allow connections via serial ports and pipes).
 *     John Dallaway - Support multiple remote debug protocols - bug 535143
 *******************************************************************************/

package org.eclipse.cdt.debug.gdbjtag.core;

import java.util.Collection;

/**
 * Provides device specific debug commands for different hardware
 * JTAG devices. See <code>DefaultGDBJtagConnectionImpl</code> for
 * the default implementation.
 * @since 7.0
 */
public interface IGDBJtagConnection {

	/**
	 * Used during instantiation to set the supported protocol strings from XML
	 * @param protocols
	 *            the array of supported protocols (default protocol first)
	 * @since 10.6
	 */
	default void setDeviceProtocols(String[] protocols) {
		// not implemented
	}

	/**
	 * Used during instantiation to set the device default connection string from XML
	 * @param connection
	 *            the GDB string describing the default connection to the target
	 */
	public void setDefaultDeviceConnection(String connection);

	/**
	 * Commands to connect to remote JTAG device
	 * @param connection
	 *            the GDB string describing the connection to the target
	 * @param commands
	 *            implementation should populate the collection with the gdb
	 *            commands that will connect to the JTAG device using the remote
	 *            protocol, or leave the collection as-is if that operation is
	 *            either unsupported or not applicable
	 * @deprecated call or override {@link #doTarget(String, String, Collection)} instead
	 */
	@Deprecated(since = "10.6")
	public void doRemote(String connection, Collection<String> commands);

	/**
	 * Supported protocols used by GDB to connect to a device
	 * @return the array of supported protocols (default protocol first)
	 * @since 10.6
	 */
	default String[] getDeviceProtocols() {
		return new String[] { "remote" }; //$NON-NLS-1$
	}

	/**
	 * Host specific default device name used by GDB to connect to a device
	 * @return identifier for the remote device.  It is up to GDB to figure out how to interpret
	 * the connection string (e.g /dev/COM1, 127.0.0.1:8888, etc.)
	 */
	public String getDefaultDeviceConnection();

	/**
	 * Commands to connect to remote JTAG device
	 * @param protocol
	 *            the GDB string describing the communication protocol between host and target
	 * @param connection
	 *            the GDB string required to establish a connection to the target
	 * @param commands
	 *            implementation should populate the collection with the gdb
	 *            commands that will connect to the JTAG device, or leave
	 *            the collection as-is if that operation is either unsupported
	 *            or not applicable
	 * @since 10.6
	 */
	default void doTarget(String protocol, String connection, Collection<String> commands) {
		if ("remote".equals(protocol)) { //$NON-NLS-1$
			doRemote(connection, commands);
		} // else protocol not supported
	}

}
