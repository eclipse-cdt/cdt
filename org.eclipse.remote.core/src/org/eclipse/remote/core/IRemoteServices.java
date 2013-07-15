/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
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

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Abstraction of a remote services provider. Clients obtain this interface using one of the static methods in
 * {@link RemoteServices}. The methods on this interface can then be used to access the full range of remote services provided.
 */
public interface IRemoteServices extends IRemoteServicesDescriptor {
	/**
	 * Get a connection manager for managing remote connections.
	 * 
	 * @return connection manager or null if services are not initialized
	 */
	public IRemoteConnectionManager getConnectionManager();

	/**
	 * Get a file manager for managing remote files
	 * 
	 * @param conn
	 *            connection to use for managing files
	 * @return file manager or null if services are not initialized
	 */
	public IRemoteFileManager getFileManager(IRemoteConnection conn);

	/**
	 * Get a process builder for creating remote processes
	 * 
	 * @param conn
	 *            connection to use for creating remote processes
	 * @return process builder or null if services are not initialized
	 */
	public IRemoteProcessBuilder getProcessBuilder(IRemoteConnection conn, List<String> command);

	/**
	 * Get a process builder for creating remote processes
	 * 
	 * @param conn
	 *            connection to use for creating remote processes
	 * @return process builder or null if services are not initialized
	 */
	public IRemoteProcessBuilder getProcessBuilder(IRemoteConnection conn, String... command);

	/**
	 * Get a remote process that runs a command shell on the remote system. The shell will be the user's default shell on the remote
	 * system. The flags may be used to modify behavior of the remote process. These flags may only be supported by specific types
	 * of remote service providers. Clients can use {@link IRemoteProcessBuilder#getSupportedFlags()} to find out the flags
	 * supported by the service provider.
	 * 
	 * <pre>
	 * Current flags are:
	 *   {@link IRemoteProcessBuilder#NONE}			- disable any flags
	 *   {@link IRemoteProcessBuilder#ALLOCATE_PTY}	- allocate a pseudo-terminal for the process (RFC-4254 Sec. 6.2)
	 *   {@link IRemoteProcessBuilder#FORWARD_X11}	- enable X11 forwarding (RFC-4254 Sec. 6.3)
	 * </pre>
	 * 
	 * @param conn
	 *            connection used for creating the remote process
	 * @param flags
	 *            bitwise-or of flags
	 * @return remote process object
	 * @throws IOException
	 * @since 7.0
	 */
	public IRemoteProcess getCommandShell(IRemoteConnection conn, int flags) throws IOException;

	/**
	 * Initialize the remote service. Clients should not call this method (it is called internally.)
	 * 
	 * @return true if the initialization was successful, false otherwise
	 * @since 7.0
	 */
	public boolean initialize(IProgressMonitor monitor);
}
