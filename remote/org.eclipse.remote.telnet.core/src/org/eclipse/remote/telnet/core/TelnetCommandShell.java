/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation, and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Corporation - initial contribution
 *******************************************************************************/
package org.eclipse.remote.telnet.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionHostService;
import org.eclipse.remote.core.IRemoteProcess;
import org.eclipse.remote.core.IRemoteProcessBuilder;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.remote.telnet.internal.core.Logger;
import org.eclipse.remote.telnet.internal.core.messages.Messages;

public class TelnetCommandShell implements IRemoteProcess {
	private final TelnetConnection telnetConnection;
	private TelnetProtocol protocol;

	public TelnetCommandShell(IRemoteConnection remoteConnection, TelnetConnection telnetConnection) {
		this.telnetConnection = telnetConnection;
		assert (remoteConnection.getService(IRemoteConnectionHostService.class) != null);
	}

	@Override
	public void destroy() {
		if (protocol != null) {
			protocol.interrupt();
		}
	}

	@Override
	public int exitValue() {
		return 0;
	}

	@Override
	public InputStream getErrorStream() {
		return null;
	}

	@Override
	public InputStream getInputStream() {
		if (protocol != null) {
			PipedOutputStream pipedOutput = new PipedOutputStream();
			protocol.setClientOutputStream(pipedOutput);
			try {
				return new PipedInputStream(pipedOutput);
			} catch (IOException e) {
				return null;
			}
		}
		return null;
	}

	@Override
	public OutputStream getOutputStream() {
		if (protocol != null) {
			return protocol.getOutputStream();
		}
		return null;
	}

	@Override
	public int waitFor() throws InterruptedException {
		if (protocol != null && protocol.isConnected()) {
			wait();
		}
		return 0;
	}

	@Override
	public boolean isCompleted() {
		return protocol == null || !protocol.isAlive();
	}

	@Override
	public IRemoteConnection getRemoteConnection() {
		return telnetConnection.getRemoteConnection();
	}

	@Override
	public <T extends Service> T getService(Class<T> service) {
		return null;
	}

	@Override
	public <T extends Service> boolean hasService(Class<T> service) {
		return false;
	}

	@Override
	public IRemoteProcessBuilder getProcessBuilder() {
		return null;
	}

	public void connect() throws RemoteConnectionException {
		IRemoteConnectionHostService hostSvc = telnetConnection.getRemoteConnection()
				.getService(IRemoteConnectionHostService.class);

		// Retry the connect after a little pause in case the
		// remote telnet server isn't ready. ConnectExceptions might
		// happen if the telnet server process did not initialized itself.
		// This is seen especially if the telnet server is a process
		// providing it's input and output via a built in telnet server.
		int remaining = 10;

		while (remaining >= 0) {
			// Pause before we re-try if the remaining tries are less than the initial value
			if (remaining < 10) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					/* ignored on purpose */ }
			}

			try {
				int nTimeout = hostSvc.getTimeout() * 1000;
				String strHost = hostSvc.getHostname();
				int nPort = hostSvc.getPort();
				InetSocketAddress address = new InetSocketAddress(strHost, nPort);
				Socket socket = new Socket();

				socket.connect(address, nTimeout);

				// If we get to this point, the connect succeeded and we will
				// force the remaining counter to be 0.
				remaining = 0;

				// This next call causes reads on the socket to see TCP urgent data
				// inline with the rest of the non-urgent data. Without this call, TCP
				// urgent data is silently dropped by Java. This is required for
				// TELNET support, because when the TELNET server sends "IAC DM", the
				// IAC byte is TCP urgent data. If urgent data is silently dropped, we
				// only see the DM, which looks like an ISO Latin-1 '�' character.

				socket.setOOBInline(true);
				socket.setKeepAlive(true);

				protocol = new TelnetProtocol(socket, this);
				protocol.start();
			} catch (UnknownHostException ex) {
				// No re-try in case of UnknownHostException, there is no indication that
				// the DNS will fix itself
				throw new RemoteConnectionException(Messages.TelnetCommandShell_0 + ex.getMessage());
			} catch (SocketTimeoutException socketTimeoutException) {
				// Time out occurred. No re-try in this case either. Time out can
				// be increased by the user. Multiplying the timeout with the remaining
				// counter is not desired.
				throw new RemoteConnectionException(socketTimeoutException.getMessage());
			} catch (ConnectException connectException) {
				// In case of a ConnectException, do a re-try. The server could have been
				// simply not ready yet and the worker would give up to early. If the terminal
				// control is already closed (disconnected), don't print "Connection refused" errors
				if (remaining == 0) {
					throw new RemoteConnectionException(connectException.getMessage());
				}
			} catch (Exception exception) {
				// Any other exception on connect. No re-try in this case either
				// Log the exception
				Logger.logException(exception);
				// And signal failed
				throw new RemoteConnectionException(exception.getMessage());
			} finally {
				remaining--;
			}
		}
	}

	protected void terminated() {
		telnetConnection.terminated(this);
	}
}
