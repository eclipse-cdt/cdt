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
 * Provides device specific debug commands for different hardware
 * JTAG devices. See <code>DefaultGDBJtagDeviceImpl</code> for
 * the default implementations.
 *
 */
public interface IGDBJtagDevice {

	/**
	 * Device reset command
	 * 
	 * @param commands
	 *            implementation should populate the collection with the gdb
	 *            commands that will reset the device, or leave the collection
	 *            as-is if that operation is either unsupported or not
	 *            applicable
	 */
	public void doReset(Collection<String> commands);

	/**
	 * Default device delay in millisecond
	 * 
	 * @return delay in second
	 */
	public int getDefaultDelay();

	/**
	 * Target needs some delay in order to initialize
	 * 
	 * @param delay
	 *            delay in second
	 * @param commands
	 *            implementation should populate the collection with the gdb
	 *            commands that will carry out a delay, or leave the collection
	 *            as-is if that operation is either unsupported or not
	 *            applicable
	 */
	public void doDelay(int delay, Collection<String> commands);

	/**
	 * Target needs to be in pause mode in order to do JTAG debug. This should
	 * happen before the target MMU takes control
	 * 
	 * @param commands
	 *            implementation should populate the collection with the gdb
	 *            commands that will halt the target, or leave the collection
	 *            as-is if that operation is either unsupported or not
	 *            applicable
	 */
	public void doHalt(Collection<String> commands);

	/**
	 * Commands to connect to remote JTAG device
	 * 
	 * @param ip
	 *            host name of IP address of JTAG device
	 * @param port
	 *            TCP socket port number of JTAG device
	 * @param commands
	 *            implementation should populate the collection with the gdb
	 *            commands that will connect to the device, or leave the
	 *            collection as-is if that operation is either unsupported or
	 *            not applicable
	 * @deprecated use @see IGDBJtagConnection#doRemote
	 */
	public void doRemote(String ip, int port, Collection<String> commands);

	/**
	 * Commands to download the executable binary to target
	 * 
	 * @param imageFileName
	 *            executable binary file name
	 * @param imageOffset
	 *            executable binary memory offset
	 * @param commands
	 *            implementation should populate the collection with the gdb
	 *            commands that will download the executable to the target, or
	 *            leave the collection as-is if that operation is either
	 *            unsupported or not applicable
	 */
	public void doLoadImage(String imageFileName, String imageOffset, Collection<String> commands);

	/**
	 * Commands to get gdb to consume the symbolics information in the given
	 * file
	 * 
	 * @param symbolFileName
	 *            symbols file name
	 * @param symbolOffset
	 *            symbols file memory offset
	 * @param commands
	 *            implementation should populate the collection with the gdb
	 *            commands that will process the symbolics file, or leave the
	 *            collection as-is if that operation is either unsupported or
	 *            not applicable
	 */
	public void doLoadSymbol(String symbolFileName, String symbolOffset, Collection<String> commands);

	/**
	 * Commands to set initial program counter
	 * 
	 * @param pc
	 *            program counter
	 * @param commands
	 *            implementation should populate the collection with the gdb
	 *            commands that will set the PC, or leave the collection as-is
	 *            if that operation is either unsupported or not applicable
	 */
	public void doSetPC(String pc, Collection<String> commands);

	/**
	 * Commands to set initial breakpoint
	 * 
	 * @param stopAt
	 *            initial breakpoint location
	 * @param commands
	 *            implementation should populate the collection with the gdb
	 *            commands that will set the initial breakpoint, or leave the
	 *            collection as-is if that operation is either unsupported or
	 *            not applicable
	 */
	public void doStopAt(String stopAt, Collection<String> commands);

	/**
	 * De-freeze the target in order to start debugging
	 * 
	 * @param commands
	 *            implementation should populate the collection with the gdb
	 *            commands that will resume the target, or leave the collection
	 *            as-is if that operation is either unsupported or not
	 *            applicable
	 */
	public void doContinue(Collection<String> commands);

	/**
	 * Device specific default hostname of IP address
	 * 
	 * @return default hostname of IP address
	 * @deprecated use @see IGDBJtagConnection#getDetaultDeviceConnection
	 */
	public String getDefaultIpAddress();

	/**
	 * Device specific default port number
	 * 
	 * @return default port number
	 * @deprecated use @see IGDBJtagConnection#getDetaultDeviceConnection
	 */
	public String getDefaultPortNumber();
}
