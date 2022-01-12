/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX - initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.core;

import java.io.IOException;

/**
 * A service that provides a command shell on a remote. This is mainly used by
 * Terminal views.
 *
 * @since 2.0
 */
public interface IRemoteCommandShellService extends IRemoteConnection.Service {

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
	 * @param flags
	 *            bitwise-or of flags
	 * @return remote process object
	 * @throws IOException
	 * @since 7.0
	 */
	public IRemoteProcess getCommandShell(int flags) throws IOException;

}
