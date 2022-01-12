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
 * A connection service for setting up port forwarding between the host and the remote.
 * This is a feature provided by SSH.
 *
 * @since 2.0
 */
public interface IRemotePortForwardingService extends IRemoteConnection.Service {

	/**
	 * Forward local port localPort to remote port fwdPort on remote machine fwdAddress. If this IRemoteConnection is not to
	 * fwdAddress, the port will be routed via the connection machine to fwdAddress.
	 *
	 * @param localPort
	 *            local port to forward
	 * @param fwdAddress
	 *            address of remote machine
	 * @param fwdPort
	 *            remote port on remote machine
	 * @throws RemoteConnectionException
	 */
	public void forwardLocalPort(int localPort, String fwdAddress, int fwdPort) throws RemoteConnectionException;

	/**
	 * Forward a local port to remote port fwdPort on remote machine fwdAddress. The local port is chosen dynamically and returned
	 * by the method. If this IRemoteConnection is not to fwdAddress, the port will be routed via the connection machine to
	 * fwdAddress.
	 *
	 * @param fwdAddress
	 * @param fwdPort
	 * @param monitor
	 * @return local port number
	 * @throws RemoteConnectionException
	 */
	public int forwardLocalPort(String fwdAddress, int fwdPort, IProgressMonitor monitor)
			throws RemoteConnectionException;

	/**
	 * Forward remote port remotePort to port fwdPort on machine fwdAddress. When a connection is made to remotePort on the remote
	 * machine, it is forwarded via this IRemoteConnection to fwdPort on machine fwdAddress.
	 *
	 * @param remotePort
	 *            remote port to forward
	 * @param fwdAddress
	 *            address of recipient machine
	 * @param fwdPort
	 *            port on recipient machine
	 * @throws RemoteConnectionException
	 */
	public void forwardRemotePort(int remotePort, String fwdAddress, int fwdPort) throws RemoteConnectionException;

	/**
	 * Forward a remote port to port fwdPort on remote machine fwdAddress. The remote port is chosen dynamically and returned by the
	 * method. When a connection is made to this port on the remote machine, it is forwarded via this IRemoteConnection to fwdPort
	 * on machine fwdAddress.
	 *
	 * If fwdAddress is the empty string ("") then the fwdPort will be bound to any address on all interfaces. Note that this
	 * requires enabling the GatewayPort sshd option on some systems.
	 *
	 * @param fwdAddress
	 * @param fwdPort
	 * @param monitor
	 * @return remote port number
	 * @throws RemoteConnectionException
	 */
	public int forwardRemotePort(String fwdAddress, int fwdPort, IProgressMonitor monitor)
			throws RemoteConnectionException;

	/**
	 * Remove the local port forwarding associated with the given port.
	 *
	 * @param port
	 *            forwarded port
	 * @throws RemoteConnectionException
	 * @since 7.0
	 */
	public void removeLocalPortForwarding(int port) throws RemoteConnectionException;

	/**
	 * Remove the remote port forwarding associated with the given port.
	 *
	 * @param port
	 *            forwarded port
	 * @throws RemoteConnectionException
	 * @since 7.0
	 */
	public void removeRemotePortForwarding(int port) throws RemoteConnectionException;

}
