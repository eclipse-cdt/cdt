/*******************************************************************************
 * Copyright (c) 2007, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.core;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.eclipse.core.filesystem.IFileStore;

/**
 * Abstraction of a process builder for remote processes. This interface behaves in the same manner as
 * {@link java.lang.ProcessBuilder}.
 */
public interface IRemoteProcessBuilder {
	/**
	 * @since 5.0
	 */
	public static int NONE = 0x00;

	/**
	 * Flag to request allocation of a pseudo-terminal for the process (RFC-4254
	 * Sec. 6.2)
	 * 
	 * @since 5.0
	 */
	public static int ALLOCATE_PTY = 0x01;

	/**
	 * Flag to request X11 forwarding (RFC-4254 Sec. 6.3)
	 * 
	 * @since 5.0
	 */
	public static int FORWARD_X11 = 0x02;

	/**
	 * Returns this process builder's operating system program and arguments.
	 * 
	 * @return a list containing the program and arguments
	 */
	public List<String> command();

	/**
	 * Sets this process builder's operating system program and arguments.
	 * 
	 * @param command
	 * @return This process builder
	 */
	public IRemoteProcessBuilder command(List<String> command);

	/**
	 * Sets this process builder's operating system program and arguments.
	 * 
	 * @param command
	 * @return this process builder
	 */
	public IRemoteProcessBuilder command(String... command);

	/**
	 * Returns this process builder's working directory.
	 * 
	 * @return an IFileStore reference to the working directory
	 */
	public IFileStore directory();

	/**
	 * Sets this process builder's working directory.
	 * 
	 * @param directory
	 * @return This process builder
	 */
	public IRemoteProcessBuilder directory(IFileStore directory);

	/**
	 * Returns a string map view of this process builder's environment. The
	 * returned map behaves in the same manner as described in {@link java.lang.ProcessBuilder#environment()}.
	 * 
	 * @return the process builder's environment
	 */
	public Map<String, String> environment();

	/**
	 * Get the flags that are supported by this process builder.
	 * 
	 * @return bitwise-or of the supported flags
	 * @since 5.0
	 */
	public int getSupportedFlags();

	/**
	 * Tells whether this process builder merges standard error and standard
	 * output.
	 * 
	 * @return true if standard error and standard output will be merged
	 */
	public boolean redirectErrorStream();

	/**
	 * Sets this process builder's redirectErrorStream property.
	 * 
	 * @param redirectErrorStream
	 * @return This process builder
	 */
	public IRemoteProcessBuilder redirectErrorStream(boolean redirectErrorStream);

	/**
	 * Starts a new process using the attributes of this process builder.
	 * 
	 * @return remote process object
	 * @throws IOException
	 */
	public IRemoteProcess start() throws IOException;

	/**
	 * Starts a new process using the attributes of this process builder. The
	 * flags may be used to modify behavior of the remote process. These flags
	 * may only be supported by specific types of remote service providers.
	 * Clients can use {@link #getSupportedFlags()} to find out the flags
	 * supported by the service provider.
	 * 
	 * <pre>
	 * Current flags are:
	 *   NONE			- disable any flags
	 *   ALLOCATE_PTY	- allocate a pseudo-terminal for the process (RFC-4254 Sec. 6.2)
	 *   FORWARD_X11	- enable X11 forwarding (RFC-4254 Sec. 6.3)
	 * </pre>
	 * 
	 * @param flags
	 *            bitwise-or of flags to use when starting process
	 * @return remote process object
	 * @throws IOException
	 * @since 5.0
	 */
	public IRemoteProcess start(int flags) throws IOException;
}