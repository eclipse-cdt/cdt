/*******************************************************************************
 * Copyright (c) 2014 University of Tennessee and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * University of Tennessee (Roland Schulz) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.internal.jsch.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.text.MessageFormat;
import java.util.List;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteProcess;
import org.eclipse.remote.core.IRemoteProcessBuilder;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.remote.internal.jsch.core.messages.Messages;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.Proxy;
import com.jcraft.jsch.SocketFactory;

/**
 * Creates a JSch Proxy. Supports both command proxies, as well as the ssh build-in
 * stream forwarding.
 *
 * @author rschulz
 *
 */
public class JSchConnectionProxyFactory {
	private static class CommandProxy implements Proxy {
		private String command;
		private IRemoteProcess process;
		private IRemoteConnection connection;
		private IProgressMonitor monitor;

		private CommandProxy(IRemoteConnection connection, String command, IProgressMonitor monitor) {
			if (command == null || connection == null || monitor == null)
				throw new IllegalArgumentException();
			this.command = command;
			this.connection = connection;
			this.monitor = monitor;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see com.jcraft.jsch.Proxy#close()
		 */
		@Override
		public void close() {
			process.destroy();
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see com.jcraft.jsch.Proxy#connect(com.jcraft.jsch.SocketFactory, java.lang.String, int, int)
		 */
		@Override
		public void connect(SocketFactory socket_factory, String host,
				int port, int timeout) throws IOException {
			assert connection != null : "connect should only be called once"; //$NON-NLS-1$
			try {
				if (timeout == 0) {
					timeout = 10000; // default to 10s
				}
				final int waitTime = 50;
				final int waitSteps = timeout / waitTime;
				SubMonitor subMon;

				// Open connection if it isn't already opened
				if (!connection.isOpen()) {
					subMon = SubMonitor.convert(monitor, waitSteps * 2);
					try {
						connection.open(subMon.newChild(waitSteps));
					} catch (RemoteConnectionException e) {
						throw new IOException(e);
					}
				} else {
					subMon = SubMonitor.convert(monitor, waitSteps);
				}

				// Start command
				command = command.replace("%h", host); //$NON-NLS-1$
				command = command.replace("%p", Integer.toString(port)); //$NON-NLS-1$
				List<String> cmd = new ArgumentParser(command).getTokenList();
				IRemoteProcessBuilder pb = connection.getProcessBuilder(cmd);
				process = pb.start();

				// Wait on command to produce stdout output
				long endTime = System.currentTimeMillis() + timeout;
				boolean bOutputAvailable, bProcessComplete, bTimedOut, bCanceled;
				do {
					try {
						Thread.sleep(waitTime);
						subMon.worked(1);
					} catch (InterruptedException e) {
						/* ignore */
					}
					bOutputAvailable = (getInputStream().available() != 0);
					bProcessComplete = process.isCompleted();
					bTimedOut = System.currentTimeMillis() > endTime;
					bCanceled = subMon.isCanceled();
				} while (!bOutputAvailable && !bProcessComplete && !bTimedOut && !bCanceled);

				// If no output was produced before process died, throw an exception with the stderr output
				final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
				if (getInputStream().available() == 0 || process.isCompleted()) {
					String msg = ""; //$NON-NLS-1$
					while (bufferedReader.ready()) {
						msg += (char) bufferedReader.read();
					}
					msg = msg.trim();

					if (!process.isCompleted()) {
						process.destroy();
					}

					String cause = Messages.JSchConnectionProxyFactory_failed;
					if (bTimedOut) {
						cause = Messages.JSchConnectionProxyFactory_timedOut;
					} else if (bCanceled) {
						cause = Messages.JSchConnectionProxyFactory_wasCanceled;
					}
					throw new IOException(MessageFormat.format(Messages.JSchConnectionProxyFactory_ProxyCommandFailed,
							command, cause, msg));
				}

				// Dump the stderr to log
				new Thread() {
					@Override
					public void run() {
						final ILog log = Activator.getDefault().getLog();
						String line;
						try {
							while ((line = bufferedReader.readLine()) != null) {
								log.log(new Status(IStatus.INFO, Activator.getUniqueIdentifier(),
										IStatus.OK, line, null));
							}
						} catch (IOException e) {
							Activator.log(e);
						}
					};
				}.start();
			} finally {
				// Not valid to call connect again or for any other command to access these variables. Setting to null to ensure.
				connection = null;
			}
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see com.jcraft.jsch.Proxy#getInputStream()
		 */
		@Override
		public InputStream getInputStream() {
			return process.getInputStream();
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see com.jcraft.jsch.Proxy#getOutputStream()
		 */
		@Override
		public OutputStream getOutputStream() {
			return process.getOutputStream();
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see com.jcraft.jsch.Proxy#getSocket()
		 */
		@Override
		public Socket getSocket() {
			return null;
		}
	}

	private static class SSHForwardProxy implements Proxy {
		private Channel channel;
		private JSchConnection connection;
		private IProgressMonitor monitor;

		private SSHForwardProxy(IRemoteConnection proxyConnection, IProgressMonitor monitor) {
			if (proxyConnection == null || monitor == null)
				throw new IllegalArgumentException();
			this.connection = (JSchConnection) proxyConnection; // only JSch supported for ForwardProxy
			this.monitor = monitor;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see com.jcraft.jsch.Proxy#close()
		 */
		@Override
		public void close() {
			channel.disconnect();
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see com.jcraft.jsch.Proxy#connect(com.jcraft.jsch.SocketFactory, java.lang.String, int, int)
		 */
		@Override
		public void connect(SocketFactory socket_factory, String host, int port,
				int timeout) throws Exception {
			assert connection != null : "connect should only be called once"; //$NON-NLS-1$
			try {
				if (!connection.isOpen()) {
					try {
						connection.open(monitor);
					} catch (RemoteConnectionException e) {
						throw new IOException(e);
					}
				}
				channel = connection.getStreamForwarder(host, port);
			} finally {
				// Not valid to call connect again or for any other command to access these variables. Setting to null to ensure.
				connection = null;
			}
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see com.jcraft.jsch.Proxy#getInputStream()
		 */
		@Override
		public InputStream getInputStream() {
			try {
				return channel.getInputStream();
			} catch (IOException e) {
				Activator.log(e);
				return null;
			}
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see com.jcraft.jsch.Proxy#getOutputStream()
		 */
		@Override
		public OutputStream getOutputStream() {
			try {
				return channel.getOutputStream();
			} catch (IOException e) {
				Activator.log(e);
				return null;
			}
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see com.jcraft.jsch.Proxy#getSocket()
		 */
		@Override
		public Socket getSocket() {
			return null;
		}
	}

	/**
	 * Creates a (local or remote) command proxy.
	 *
	 * @param connection
	 *            Any (local or remote) connection. Cannot be null.
	 * @param command
	 *            A valid proxy command. Cannot be null or empty.
	 * @param monitor
	 *            A valid progress monitor. Cannot be null.
	 * @return ssh proxy
	 */
	public static Proxy createCommandProxy(IRemoteConnection connection, String command, IProgressMonitor monitor) {
		return new CommandProxy(connection, command, monitor);
	}

	/**
	 * Creates a ssh forward proxy.
	 *
	 * @param proxyConnection
	 *            The Jsch proxy connection. Cannot be null.
	 * @param monitor
	 *            A valid progress monitor. Cannot be null.
	 * @return ssh proxy
	 */
	public static Proxy createForwardProxy(IRemoteConnection proxyConnection, IProgressMonitor monitor) {
		return new SSHForwardProxy(proxyConnection, monitor);
	}
}
