/*******************************************************************************
 * Copyright (c) 2016 Oak Ridge National Laboratory and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.remote.internal.proxy.core;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.remote.core.IRemoteCommandShellService;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionChangeListener;
import org.eclipse.remote.core.IRemoteConnectionControlService;
import org.eclipse.remote.core.IRemoteConnectionHostService;
import org.eclipse.remote.core.IRemoteConnectionPropertyService;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.IRemoteProcess;
import org.eclipse.remote.core.IRemoteProcessBuilder;
import org.eclipse.remote.core.IRemoteProcessService;
import org.eclipse.remote.core.RemoteConnectionChangeEvent;
import org.eclipse.remote.core.RemoteServicesUtils;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.remote.internal.proxy.core.commands.ExecCommand;
import org.eclipse.remote.internal.proxy.core.commands.GetCwdCommand;
import org.eclipse.remote.internal.proxy.core.commands.GetEnvCommand;
import org.eclipse.remote.internal.proxy.core.commands.GetPropertiesCommand;
import org.eclipse.remote.internal.proxy.core.messages.Messages;
import org.eclipse.remote.proxy.protocol.core.StreamChannel;
import org.eclipse.remote.proxy.protocol.core.StreamChannelManager;
import org.eclipse.remote.proxy.protocol.core.exceptions.ProxyException;

import com.jcraft.jsch.ChannelShell;

/**
 * @since 5.0
 */
public class ProxyConnection
		implements IRemoteConnectionControlService, IRemoteConnectionChangeListener, IRemoteProcessService,
		IRemoteCommandShellService, IRemoteConnectionHostService, IRemoteConnectionPropertyService {
	// Connection Type ID
	public static final String JSCH_ID = "org.eclipse.remote.Proxy"; //$NON-NLS-1$

	public static final int DEFAULT_PORT = 22;
	public static final int DEFAULT_TIMEOUT = 0;
	public static final boolean DEFAULT_USE_PASSWORD = false;
	public static final boolean DEFAULT_USE_DEFAULT_SERVER = true;
	public static final String DEFAULT_SERVER_COMMAND = "sh .eclipsesettings/proxy.sh"; //$NON-NLS-1$

	public static final String HOSTNAME_ATTR = "PROXY_HOSTNAME__ATTR"; //$NON-NLS-1$
	public static final String USERNAME_ATTR = "PROXY_USERNAME_ATTR"; //$NON-NLS-1$
	public static final String PASSWORD_ATTR = "PROXY_PASSWORD_ATTR"; //$NON-NLS-1$
	public static final String PORT_ATTR = "PROXY_PORT_ATTR"; //$NON-NLS-1$
	public static final String USE_PASSWORD_ATTR = "PROXY_USE_PASSWORD_ATTR"; //$NON-NLS-1$
	public static final String PASSPHRASE_ATTR = "PROXY_PASSPHRASE_ATTR"; //$NON-NLS-1$
	public static final String TIMEOUT_ATTR = "PROXY_TIMEOUT_ATTR"; //$NON-NLS-1$
	public static final String SERVER_COMMAND_ATTR = "PROXY_SERVER_COMMAND_ATTR"; //$NON-NLS-1$
	public static final String USE_DEFAULT_SERVER_ATTR = "PROXY_USE_DEFAULT_SERVER_ATTR"; //$NON-NLS-1$

	private String fWorkingDir;
	private StreamChannelManager channelMux;
	private StreamChannel commandChannel;
	private boolean isOpen;

	private final IRemoteConnection fRemoteConnection;

	private final Map<String, String> fEnv = new HashMap<>();
	private final Map<String, String> fProperties = new HashMap<>();

	private static final Map<IRemoteConnection, ProxyConnection> connectionMap = new HashMap<>();

	public ProxyConnection(IRemoteConnection connection) {
		fRemoteConnection = connection;
		connection.addConnectionChangeListener(this);
	}

	@Override
	public void connectionChanged(RemoteConnectionChangeEvent event) {
		if (event.getType() == RemoteConnectionChangeEvent.CONNECTION_REMOVED) {
			synchronized (connectionMap) {
				connectionMap.remove(event.getConnection());
			}
		}
	}

	@Override
	public IRemoteConnection getRemoteConnection() {
		return fRemoteConnection;
	}

	public static class Factory implements IRemoteConnection.Service.Factory {
		@Override
		@SuppressWarnings("unchecked")
		public <T extends IRemoteConnection.Service> T getService(IRemoteConnection connection, Class<T> service) {
			// This little trick creates an instance of this class for a connection
			// then for each interface it implements, it returns the same object.
			// This works because the connection caches the service so only one gets created.
			// As a side effect, it makes this class a service too which can be used
			// by the this plug-in
			if (ProxyConnection.class.equals(service)) {
				synchronized (connectionMap) {
					ProxyConnection conn = connectionMap.get(connection);
					if (conn == null) {
						conn = new ProxyConnection(connection);
						connectionMap.put(connection, conn);
					}
					return (T) conn;
				}
			} else if (IRemoteConnectionControlService.class.equals(service)
					|| IRemoteConnectionPropertyService.class.equals(service)
					|| IRemoteConnectionHostService.class.equals(service) || IRemoteProcessService.class.equals(service)
					|| IRemoteCommandShellService.class.equals(service)
					|| IRemoteConnectionPropertyService.class.equals(service)) {
				return (T) connection.getService(ProxyConnection.class);
			} else {
				return null;
			}
		}
	}

	@Override
	public synchronized void close() {
		if (isOpen) {
			channelMux.shutdown();
			isOpen = false;
			fRemoteConnection.fireConnectionChangeEvent(RemoteConnectionChangeEvent.CONNECTION_CLOSED);
		}
	}

	@Override
	public boolean isOpen() {
		return isOpen;
	}

	@Override
	public void open(IProgressMonitor monitor) throws RemoteConnectionException {
		SubMonitor subMon = SubMonitor.convert(monitor, Messages.ProxyConnection_0, 20);
		if (!isOpen) {
			ProxyConnectionBootstrap bootstrap = new ProxyConnectionBootstrap();
			channelMux = bootstrap.run(getRemoteConnection(), subMon.newChild(10));
			new Thread(channelMux, "multiplexer").start(); //$NON-NLS-1$
			try {
				commandChannel = channelMux.openChannel();
				initialize(subMon.newChild(10));
			} catch (RemoteConnectionException | IOException e) {
				try {
					commandChannel.close();
				} catch (IOException e1) {
					// Ignore
				}
				channelMux.shutdown();
				throw new RemoteConnectionException(e.getMessage());
			}

			isOpen = true;
			fRemoteConnection.fireConnectionChangeEvent(RemoteConnectionChangeEvent.CONNECTION_OPENED);
		}
	}

	private void initialize(IProgressMonitor monitor) throws RemoteConnectionException {
		SubMonitor subMon = SubMonitor.convert(monitor, 30);
		fWorkingDir = getCwd(subMon.newChild(10));
		if (subMon.isCanceled()) {
			throw new RemoteConnectionException(Messages.ProxyConnection_2);
		}
		fEnv.putAll(loadEnv(subMon.newChild(10)));
		if (subMon.isCanceled()) {
			throw new RemoteConnectionException(Messages.ProxyConnection_2);
		}
		fProperties.putAll(loadProperties(subMon.newChild(10)));
		if (subMon.isCanceled()) {
			throw new RemoteConnectionException(Messages.ProxyConnection_2);
		}
	}

	private String getCwd(IProgressMonitor monitor) throws RemoteConnectionException {
		try {
			GetCwdCommand cmd = new GetCwdCommand(this);
			return cmd.getResult(monitor);
		} catch (ProxyException e) {
			throw new RemoteConnectionException(e.getMessage());
		}
	}

	private Map<String, String> loadEnv(IProgressMonitor monitor) throws RemoteConnectionException {
		try {
			GetEnvCommand cmd = new GetEnvCommand(this);
			return cmd.getResult(monitor);
		} catch (ProxyException e) {
			throw new RemoteConnectionException(e.getMessage());
		}
	}

	private Map<String, String> loadProperties(IProgressMonitor monitor) throws RemoteConnectionException {
		try {
			GetPropertiesCommand cmd = new GetPropertiesCommand(this);
			return cmd.getResult(monitor);
		} catch (ProxyException e) {
			throw new RemoteConnectionException(e.getMessage());
		}
	}

	public Map<String, String> getEnv() {
		return Collections.unmodifiableMap(fEnv);
	}

	public StreamChannel getCommandChannel() {
		return commandChannel;
	}

	public StreamChannel openChannel() throws IOException {
		return channelMux.openChannel();
	}

	private StringBuffer stdout = new StringBuffer();
	private StringBuffer stderr = new StringBuffer();

	@SuppressWarnings("unused")
	private String executeSshCommand(ChannelShell shell, String command) throws RemoteConnectionException {
		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			ByteArrayOutputStream err = new ByteArrayOutputStream();
			shell.setOutputStream(stream);
			shell.setExtOutputStream(err);
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(shell.getOutputStream()));
			writer.write(command);
			writer.flush();
			if (err.size() > 0) {
				throw new RemoteConnectionException(err.toString());
			}
			return stream.toString();
		} catch (IOException e) {
			throw new RemoteConnectionException(e.getMessage());
		}
	}

	@SuppressWarnings("unused")
	private String executeCommand(List<String> command, IProgressMonitor monitor) throws ProxyException {
		try {
			final StreamChannel chanA = channelMux.openChannel();
			final StreamChannel chanB = channelMux.openChannel();
			final StreamChannel chanC = channelMux.openChannel();
			new Thread("cmd stdin reader") { //$NON-NLS-1$
				@Override
				public void run() {
					byte[] buf = new byte[1024];
					int n;
					try {
						while ((n = chanA.getInputStream().read(buf)) >= 0) {
							stdout.append(new String(buf, 0, n));
						}
					} catch (IOException e) {
						// Finish
					}
				}
			}.start();
			new Thread("cmd stderr reader") { //$NON-NLS-1$
				@Override
				public void run() {
					byte[] buf = new byte[1024];
					int n;
					try {
						while ((n = chanB.getInputStream().read(buf)) >= 0) {
							stderr.append(new String(buf, 0, n));
						}
					} catch (IOException e) {
						// Finish
					}
				}
			}.start();
			ExecCommand cmd = new ExecCommand(this, command, getEnv(), getWorkingDirectory(), false, false,
					chanA.getId(), chanB.getId(), chanC.getId());
			cmd.getResult(monitor);
			DataInputStream status = new DataInputStream(chanC.getInputStream());
			int stat = status.readInt();
			if (stat == 0) {
				return stdout.toString();
			}
			return stderr.toString();
		} catch (IOException e) {
			throw new ProxyException(e.getMessage());
		}
	}

	@Override
	public String getEnv(String name) {
		return getEnv().get(name);
	}

	@Override
	public IRemoteProcessBuilder getProcessBuilder(List<String> command) {
		return new ProxyProcessBuilder(this, command);
	}

	@Override
	public IRemoteProcessBuilder getProcessBuilder(String... command) {
		return new ProxyProcessBuilder(this, command);
	}

	@Override
	public String getWorkingDirectory() {
		return fWorkingDir;
	}

	@Override
	public void setWorkingDirectory(String path) {
		if (RemoteServicesUtils.posixPath(path).isAbsolute()) {
			fWorkingDir = path;
		}
	}

	@Override
	public IRemoteProcess getCommandShell(int flags) throws IOException {
		return new ProxyProcessBuilder(this).start(flags);
	}

	@Override
	public String getHostname() {
		return fRemoteConnection.getAttribute(HOSTNAME_ATTR);
	}

	@Override
	public int getPort() {
		String portStr = fRemoteConnection.getAttribute(PORT_ATTR);
		return !portStr.isEmpty() ? Integer.parseInt(portStr) : DEFAULT_PORT;
	}

	@Override
	public int getTimeout() {
		String portStr = fRemoteConnection.getAttribute(TIMEOUT_ATTR);
		return !portStr.isEmpty() ? Integer.parseInt(portStr) : DEFAULT_TIMEOUT;
	}

	@Override
	public String getPassphrase() {
		return fRemoteConnection.getSecureAttribute(PASSPHRASE_ATTR);
	}

	@Override
	public String getPassword() {
		return fRemoteConnection.getSecureAttribute(PASSWORD_ATTR);
	}

	@Override
	public boolean usePassword() {
		String str = fRemoteConnection.getAttribute(USE_PASSWORD_ATTR);
		return !str.isEmpty() ? Boolean.parseBoolean(str) : DEFAULT_USE_PASSWORD;
	}

	@Override
	public String getUsername() {
		return fRemoteConnection.getAttribute(USERNAME_ATTR);
	}

	@Override
	public boolean useLoginShell() {
		return false;
	}

	@Override
	public void setHostname(String hostname) {
		if (fRemoteConnection instanceof IRemoteConnectionWorkingCopy) {
			IRemoteConnectionWorkingCopy wc = (IRemoteConnectionWorkingCopy) fRemoteConnection;
			wc.setAttribute(HOSTNAME_ATTR, hostname);
		}
	}

	@Override
	public void setPassphrase(String passphrase) {
		if (fRemoteConnection instanceof IRemoteConnectionWorkingCopy) {
			IRemoteConnectionWorkingCopy wc = (IRemoteConnectionWorkingCopy) fRemoteConnection;
			wc.setSecureAttribute(PASSPHRASE_ATTR, passphrase);
		}
	}

	@Override
	public void setPassword(String password) {
		if (fRemoteConnection instanceof IRemoteConnectionWorkingCopy) {
			IRemoteConnectionWorkingCopy wc = (IRemoteConnectionWorkingCopy) fRemoteConnection;
			wc.setSecureAttribute(PASSWORD_ATTR, password);
		}
	}

	@Override
	public void setPort(int port) {
		if (fRemoteConnection instanceof IRemoteConnectionWorkingCopy) {
			IRemoteConnectionWorkingCopy wc = (IRemoteConnectionWorkingCopy) fRemoteConnection;
			wc.setAttribute(PORT_ATTR, Integer.toString(port));
		}
	}

	@Override
	public void setTimeout(int timeout) {
		if (fRemoteConnection instanceof IRemoteConnectionWorkingCopy) {
			IRemoteConnectionWorkingCopy wc = (IRemoteConnectionWorkingCopy) fRemoteConnection;
			wc.setAttribute(TIMEOUT_ATTR, Integer.toString(timeout));
		}
	}

	@Override
	public void setUseLoginShell(boolean useLogingShell) {
		// Not used
	}

	@Override
	public void setUsePassword(boolean usePassword) {
		if (fRemoteConnection instanceof IRemoteConnectionWorkingCopy) {
			IRemoteConnectionWorkingCopy wc = (IRemoteConnectionWorkingCopy) fRemoteConnection;
			wc.setAttribute(USE_PASSWORD_ATTR, Boolean.toString(usePassword));
		}
	}

	@Override
	public void setUsername(String username) {
		if (fRemoteConnection instanceof IRemoteConnectionWorkingCopy) {
			IRemoteConnectionWorkingCopy wc = (IRemoteConnectionWorkingCopy) fRemoteConnection;
			wc.setAttribute(USERNAME_ATTR, username);
		}
	}

	@Override
	public String getProperty(String key) {
		return fProperties.get(key);
	}
}
