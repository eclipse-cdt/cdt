/*******************************************************************************
 * Copyright (c) 2008-2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Andy Jin - Hardware debugging UI improvements, bug 229946
 *     Sage Electronic Engineering, LLC - bug 305943
 *              - API generalization to become transport-independent (e.g. to
 *                allow connections via serial ports and pipes).
 *******************************************************************************/

package org.eclipse.cdt.debug.gdbjtag.core;

import java.util.Collection;

/**
 * Provides device specific debug commands for different hardware
 * JTAG devices. See <code>DefaultGDBJtagDeviceImpl</code> for
 * the default implementations.
 * @since 7.0
 */
public interface IGDBJtagConnection {
	
	/**
	 * Used during instantiation to set the device default connection string from XML
	 * @param connection A device specific default connection string that GDB understands
	 */
	public void setDefaultDeviceConnection(String connection);

	/**
	 * Commands to connect to remote JTAG device
	 * @param connection defines the gdb string required to establish a connection to the target
	 * @param commands gdb commands to execute on the remote device (usually the target probe)
	 */
	public void doRemote(String connection, Collection<String> commands);

	/**
	 * Host specific default device name used by GDB to connect to a device
	 * @return identifier for the remote device.  It is up to GDB to figure out how to interpret
	 * the connection string (e.g /dev/COM1, 127.0.0.1:8888, etc.) 
	 */
	public String getDefaultDeviceConnection();
	
}
