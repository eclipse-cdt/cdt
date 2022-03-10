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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.remote.core.exception.RemoteConnectionException;

/**
 * A service to control and report on the state of a connection, open or closed.
 * Connections that do not provide this service are always assumed to be opened.
 *
 * @since 2.0
 */
public interface IRemoteConnectionControlService extends IRemoteConnection.Service {

	/**
	 * Open the connection. Must be called before the connection can be used.
	 *
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the user. It is the caller's responsibility to call done()
	 *            on the given monitor. Accepts null, indicating that no progress should be reported and that the operation cannot
	 *            be cancelled.
	 * @throws RemoteConnectionException
	 */
	public void open(IProgressMonitor monitor) throws RemoteConnectionException;

	/**
	 * Close the connection. Must be called to terminate the connection.
	 */
	public void close();

	/**
	 * Test if the connection is open.
	 *
	 * @return true if connection is open.
	 */
	public boolean isOpen();

}
