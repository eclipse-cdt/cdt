/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 * Patrick Tasse - [462418] use stored password on non-preferred password based authentication
 * Martin Oberhuber - [468889] Support Eclipse older than Mars
 *******************************************************************************/
package org.eclipse.remote.internal.jsch.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jsch.core.IJSchService;
import org.eclipse.osgi.util.NLS;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionChangeListener;
import org.eclipse.remote.core.IRemoteConnectionControlService;
import org.eclipse.remote.core.IRemoteConnectionHostService;
import org.eclipse.remote.core.IRemoteConnectionPropertyService;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.IRemotePortForwardingService;
import org.eclipse.remote.core.IRemoteProcessBuilder;
import org.eclipse.remote.core.IRemoteProcessService;
import org.eclipse.remote.core.IUserAuthenticatorService;
import org.eclipse.remote.core.RemoteConnectionChangeEvent;
import org.eclipse.remote.core.RemoteServicesUtils;
import org.eclipse.remote.core.exception.AddressInUseException;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.remote.core.exception.UnableToForwardPortException;
import org.eclipse.remote.internal.jsch.core.commands.ExecCommand;
import org.eclipse.remote.internal.jsch.core.messages.Messages;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * @since 5.0
 */
public class JSchConnection
		implements IRemoteConnectionControlService, IRemoteConnectionPropertyService, IRemotePortForwardingService,
		IRemoteProcessService, IRemoteConnectionHostService, IRemoteConnectionChangeListener {
	// Connection Type ID
	public static final String JSCH_ID = "org.eclipse.remote.JSch"; //$NON-NLS-1$

	// Attributes
	public static final String ADDRESS_ATTR = "JSCH_ADDRESS_ATTR"; //$NON-NLS-1$
	public static final String USERNAME_ATTR = "JSCH_USERNAME_ATTR"; //$NON-NLS-1$
	public static final String PASSWORD_ATTR = "JSCH_PASSWORD_ATTR"; //$NON-NLS-1$
	public static final String PORT_ATTR = "JSCH_PORT_ATTR"; //$NON-NLS-1$
	public static final String PROXYCONNECTION_ATTR = "JSCH_PROXYCONNECTION_ATTR"; //$NON-NLS-1$
	public static final String PROXYCOMMAND_ATTR = "JSCH_PROXYCOMMAND_ATTR"; //$NON-NLS-1$
	public static final String IS_PASSWORD_ATTR = "JSCH_IS_PASSWORD_ATTR"; //$NON-NLS-1$
	public static final String PASSPHRASE_ATTR = "JSCH_PASSPHRASE_ATTR"; //$NON-NLS-1$
	public static final String TIMEOUT_ATTR = "JSCH_TIMEOUT_ATTR"; //$NON-NLS-1$
	public static final String USE_LOGIN_SHELL_ATTR = "JSCH_USE_LOGIN_SHELL_ATTR"; //$NON-NLS-1$
	public static final String LOGIN_SHELL_COMMAND_ATTR = "JSCH_LOGIN_SHELL_COMMAND_ATTR"; //$NON-NLS-1$

	public static final int DEFAULT_PORT = 22;
	public static final int DEFAULT_TIMEOUT = 0;
	public static final boolean DEFAULT_IS_PASSWORD = false;
	public static final boolean DEFAULT_USE_LOGIN_SHELL = true;
	public static final String DEFAULT_LOGIN_SHELL_COMMAND = "/bin/bash -l -c '{0}'"; //$NON-NLS-1$
	public static final String DEFAULT_ENCODING = "UTF-8"; //$NON-NLS-1$
	public static final String EMPTY_STRING = ""; //$NON-NLS-1$

	private String fWorkingDir;

	private final IRemoteConnection fRemoteConnection;
	private final IJSchService fJSchService;

	private final Map<String, String> fEnv = new HashMap<>();
	private final Map<String, String> fProperties = new HashMap<>();
	private final List<Session> fSessions = new ArrayList<>();

	private ChannelSftp fSftpCommandChannel;
	private boolean isFullySetup; // including sftp channel and environment

	private static final Map<IRemoteConnection, JSchConnection> connectionMap = new HashMap<>();

	public JSchConnection(IRemoteConnection connection) {
		fRemoteConnection = connection;
		fJSchService = Activator.getDefault().getService();
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
			if (JSchConnection.class.equals(service)) {
				synchronized (connectionMap) {
					JSchConnection jschConnection = connectionMap.get(connection);
					if (jschConnection == null) {
						jschConnection = new JSchConnection(connection);
						connectionMap.put(connection, jschConnection);
					}
					return (T) jschConnection;
				}
			} else if (IRemoteConnectionControlService.class.equals(service)
					|| IRemoteConnectionPropertyService.class.equals(service)
					|| IRemotePortForwardingService.class.equals(service) || IRemoteProcessService.class.equals(service)
					|| IRemoteConnectionHostService.class.equals(service)) {
				return (T) connection.getService(JSchConnection.class);
			} else {
				return null;
			}
		}
	}

	private boolean checkConfiguration(Session session, IProgressMonitor monitor) throws RemoteConnectionException {
		SubMonitor subMon = SubMonitor.convert(monitor, 10);
		ChannelSftp sftp;
		try {
			/*
			 * First, check if sftp is supported at all. This is required for EFS, so throw exception if not supported.
			 */
			sftp = openSftpChannel(session);
		} catch (RemoteConnectionException e) {
			throw new RemoteConnectionException(Messages.JSchConnection_Remote_host_does_not_support_sftp);
		}
		/*
		 * While sftp channel is open, try opening an exec channel. If it doesn't succeed, then MaxSession is < 2 so we need at
		 * least one additional session.
		 */
		try {
			loadEnv(subMon.newChild(10));
		} catch (RemoteConnectionException e) {
			if (e.getMessage().contains("channel is not opened")) { //$NON-NLS-1$
				return false;
			}
		} finally {
			if (sftp != null) {
				sftp.disconnect();
			}
		}
		return true;
	}

	/**
	 * @throws RemoteConnectionException
	 */
	private void checkIsConfigured() throws RemoteConnectionException {
		if (fRemoteConnection.getAttribute(ADDRESS_ATTR) == null) {
			throw new RemoteConnectionException(Messages.JSchConnection_remote_address_must_be_set);
		}
		if (fRemoteConnection.getAttribute(USERNAME_ATTR) == null) {
			throw new RemoteConnectionException(Messages.JSchConnection_username_must_be_set);
		}
	}

	private synchronized void cleanup() {
		if (fSftpCommandChannel != null) {
			if (fSftpCommandChannel.isConnected()) {
				fSftpCommandChannel.disconnect();
			}
			fSftpCommandChannel = null;
		}
		for (Session session : fSessions) {
			if (session.isConnected()) {
				session.disconnect();
			}
		}
		fSessions.clear();
	}

	@Override
	public synchronized void close() {
		cleanup();
		fRemoteConnection.fireConnectionChangeEvent(RemoteConnectionChangeEvent.CONNECTION_CLOSED);
	}

	/**
	 * Execute the command and return the result as a string.
	 *
	 * @param cmd
	 *            command to execute
	 * @param monitor
	 *            progress monitor
	 * @return result of command
	 * @throws RemoteConnectionException
	 */
	private String executeCommand(String cmd, IProgressMonitor monitor) throws RemoteConnectionException {
		ExecCommand exec = new ExecCommand(this);
		monitor.subTask(NLS.bind(Messages.JSchConnection_Executing_command, cmd));
		return exec.setCommand(cmd).getResult(monitor).trim();
	}

	@Override
	public void forwardLocalPort(int localPort, String fwdAddress, int fwdPort) throws RemoteConnectionException {
		if (!isOpen()) {
			throw new RemoteConnectionException(Messages.JSchConnection_connectionNotOpen);
		}
		try {
			fSessions.get(0).setPortForwardingL(localPort, fwdAddress, fwdPort);
		} catch (JSchException e) {
			throw new RemoteConnectionException(e.getMessage());
		}
	}

	@Override
	public int forwardLocalPort(String fwdAddress, int fwdPort, IProgressMonitor monitor)
			throws RemoteConnectionException {
		if (!isOpen()) {
			throw new RemoteConnectionException(Messages.JSchConnection_connectionNotOpen);
		}
		SubMonitor progress = SubMonitor.convert(monitor, 10);
		progress.beginTask(Messages.JSchConnection_forwarding, 10);
		/*
		 * Start with a different port number, in case we're doing this all on localhost.
		 */
		int localPort = fwdPort + 1;

		/*
		 * Try to find a free port on the remote machine. This take a while, so allow it to be canceled. If we've tried all
		 * ports (which could take a very long while) then bail out.
		 */
		while (!progress.isCanceled()) {
			try {
				forwardLocalPort(localPort, fwdAddress, fwdPort);
			} catch (AddressInUseException e) {
				if (++localPort == fwdPort) {
					throw new UnableToForwardPortException(Messages.JSchConnection_remotePort);
				}
				progress.worked(1);
			}
			return localPort;
		}
		return -1;
	}

	@Override
	public void forwardRemotePort(int remotePort, String fwdAddress, int fwdPort) throws RemoteConnectionException {
		if (!isOpen()) {
			throw new RemoteConnectionException(Messages.JSchConnection_connectionNotOpen);
		}
		try {
			fSessions.get(0).setPortForwardingR(remotePort, fwdAddress, fwdPort);
		} catch (JSchException e) {
			throw new RemoteConnectionException(e.getMessage());
		}
	}

	@Override
	public int forwardRemotePort(String fwdAddress, int fwdPort, IProgressMonitor monitor)
			throws RemoteConnectionException {
		if (!isOpen()) {
			throw new RemoteConnectionException(Messages.JSchConnection_connectionNotOpen);
		}
		SubMonitor progress = SubMonitor.convert(monitor, 10);
		progress.beginTask(Messages.JSchConnection_forwarding, 10);
		/*
		 * Start with a different port number, in case we're doing this all on localhost.
		 */
		int remotePort = fwdPort + 1;
		/*
		 * Try to find a free port on the remote machine. This take a while, so allow it to be canceled. If we've tried all
		 * ports (which could take a very long while) then bail out.
		 */
		while (!progress.isCanceled()) {
			try {
				forwardRemotePort(remotePort, fwdAddress, fwdPort);
				return remotePort;
			} catch (AddressInUseException e) {
				if (++remotePort == fwdPort) {
					throw new UnableToForwardPortException(Messages.JSchConnection_remotePort);
				}
				progress.worked(1);
			}
		}
		return -1;
	}

	@Override
	public String getHostname() {
		return fRemoteConnection.getAttribute(ADDRESS_ATTR);
	}

	/**
	 * Get the result of executing a pwd command.
	 *
	 * @return current working directory
	 */
	private String getCwd(IProgressMonitor monitor) {
		SubMonitor subMon = SubMonitor.convert(monitor, 10);
		try {
			return executeCommand("pwd", subMon.newChild(10)); //$NON-NLS-1$
		} catch (RemoteConnectionException e) {
			// Ignore
		}
		return null;
	}

	@Override
	public Map<String, String> getEnv() {
		return Collections.unmodifiableMap(fEnv);
	}

	@Override
	public String getEnv(String name) {
		return getEnv().get(name);
	}

	/**
	 * Open an exec channel to the remote host.
	 *
	 * @return exec channel or null if the progress monitor was cancelled
	 *
	 * @throws RemoteConnectionException
	 *             if a channel could not be opened
	 */
	public ChannelExec getExecChannel() throws RemoteConnectionException {
		try {
			return (ChannelExec) fSessions.get(0).openChannel("exec"); //$NON-NLS-1$
		} catch (JSchException e) {
			throw new RemoteConnectionException(e.getMessage());
		}
	}

	/**
	 * Open a shell channel to the remote host.
	 *
	 * @return shell channel or null if the progress monitor was cancelled
	 *
	 * @throws RemoteConnectionException
	 *             if a channel could not be opened
	 */
	public ChannelShell getShellChannel() throws RemoteConnectionException {
		try {
			return (ChannelShell) fSessions.get(0).openChannel("shell"); //$NON-NLS-1$
		} catch (JSchException e) {
			throw new RemoteConnectionException(e.getMessage());
		}
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
	public int getPort() {
		String portStr = fRemoteConnection.getAttribute(PORT_ATTR);
		return !portStr.isEmpty() ? Integer.parseInt(portStr) : DEFAULT_PORT;
	}

	@Override
	public IRemoteProcessBuilder getProcessBuilder(List<String> command) {
		if (!isOpen()) {
			return null;
		}
		return new JSchProcessBuilder(getRemoteConnection(), command);
	}

	@Override
	public IRemoteProcessBuilder getProcessBuilder(String... command) {
		if (!isOpen()) {
			return null;
		}
		return new JSchProcessBuilder(getRemoteConnection(), command);
	}

	@Override
	public String getProperty(String key) {
		return fProperties.get(key);
	}

	/**
	 * Get the login shell command if useLoginShell is true
	 *
	 * @return login shell command
	 */
	public String getLoginShellCommand() {
		String loginShell = fRemoteConnection.getAttribute(LOGIN_SHELL_COMMAND_ATTR);
		return loginShell.isEmpty() ? DEFAULT_LOGIN_SHELL_COMMAND : loginShell;
	}

	/**
	 * Gets the proxy command. For no proxy command null is returned.
	 *
	 * @return proxy command
	 */
	public String getProxyCommand() {
		return fRemoteConnection.getAttribute(PROXYCOMMAND_ATTR);
	}

	/**
	 * Gets the proxy connection. If no proxy connection is used it returns null.
	 *
	 * @return proxy connection
	 */
	public JSchConnection getProxyConnection() {
		String proxyConnectionName = getProxyConnectionName();
		if (proxyConnectionName.isEmpty()) {
			return null;
		}
		return fRemoteConnection.getConnectionType().getConnection(proxyConnectionName)
				.getService(JSchConnection.class);
	}

	/**
	 * Gets the proxy connection name
	 *
	 * @return proxy connection name. If no proxy is used returns null.
	 */
	public String getProxyConnectionName() {
		return fRemoteConnection.getAttribute(PROXYCONNECTION_ATTR);
	}

	/**
	 * Open an sftp command channel to the remote host. This channel is for commands that do not require any
	 * state being preserved and should not be closed. Long running commands (such as get/put) should use a separate channel
	 * obtained via {#link #newSftpChannel()}.
	 *
	 * Always use the second session if available.
	 *
	 * @return sftp channel
	 * @throws RemoteConnectionException
	 *             if a channel could not be opened
	 */
	public ChannelSftp getSftpCommandChannel() throws RemoteConnectionException {
		if (fSftpCommandChannel == null || fSftpCommandChannel.isClosed()) {
			fSftpCommandChannel = newSftpChannel();
		}
		return fSftpCommandChannel;
	}

	/**
	 * Open a channel for long running commands. This channel should be closed when the command is completed.
	 *
	 * @return sftp channel
	 * @throws RemoteConnectionException
	 *             if a channel could not be opened
	 */
	public ChannelSftp newSftpChannel() throws RemoteConnectionException {
		Session session = fSessions.get(0);
		if (fSessions.size() > 1) {
			session = fSessions.get(1);
		}
		ChannelSftp channel = openSftpChannel(session);
		if (channel == null) {
			throw new RemoteConnectionException(Messages.JSchConnection_Unable_to_open_sftp_channel);
		}
		return channel;
	}

	public Channel getStreamForwarder(String host, int port) throws RemoteConnectionException {
		try {
			Channel channel = fSessions.get(0).getStreamForwarder(host, port);
			channel.connect();
			return channel;
		} catch (JSchException e) {
			throw new RemoteConnectionException(e);
		}
	}

	@Override
	public int getTimeout() {
		String str = fRemoteConnection.getAttribute(TIMEOUT_ATTR);
		return !str.isEmpty() ? Integer.parseInt(str) : DEFAULT_TIMEOUT;
	}

	@Override
	public String getUsername() {
		return fRemoteConnection.getAttribute(USERNAME_ATTR);
	}

	@Override
	public String getWorkingDirectory() {
		if (!isOpen()) {
			return "/"; //$NON-NLS-1$
		}
		if (fWorkingDir == null) {
			return "/"; //$NON-NLS-1$
		}
		return fWorkingDir;
	}

	/**
	 * Test if the connection has a valid open session. Doesn't check whether the connection is fully setup.
	 *
	 * @return true if a valid session is available.
	 */
	public boolean hasOpenSession() {
		boolean hasOpenSession = fSessions.size() > 0;
		if (hasOpenSession) {
			for (Session session : fSessions) {
				hasOpenSession &= session.isConnected();
			}
		}
		if (!hasOpenSession) {
			cleanup(); // Cleanup if session is closed
		}
		return hasOpenSession;
	}

	@Override
	public boolean isOpen() {
		return hasOpenSession() && isFullySetup;
	}

	@Override
	public boolean usePassword() {
		String str = fRemoteConnection.getAttribute(IS_PASSWORD_ATTR);
		return !str.isEmpty() ? Boolean.parseBoolean(str) : DEFAULT_IS_PASSWORD;
	}

	private void loadEnv(IProgressMonitor monitor) throws RemoteConnectionException {
		SubMonitor subMon = SubMonitor.convert(monitor, 10);
		String env = executeCommand("printenv", subMon.newChild(10)); //$NON-NLS-1$
		String[] vars = env.split("\n"); //$NON-NLS-1$
		for (String var : vars) {
			String[] kv = var.split("="); //$NON-NLS-1$
			if (kv.length == 2) {
				fEnv.put(kv[0], kv[1]);
			}
		}
	}

	/**
	 *
	 * Load the following hard-coded properties at runtime:
	 *
	 * <dl>
	 * <dt>file.separator
	 * <dd>File separator character of the (remote) connection. Hardcoded "/" (forward slash).
	 * <dt>path.separator
	 * <dd>Path separator character of the (remote) connection. Hardcoded ":" (colon).
	 * <dt>line.separator
	 * <dd>Line separator character of the (remote) connection. Hardcoded "\n" (new-line).
	 * <dt>user.home
	 * <dd>User home directory on the (remote) connection.
	 * <dt>os.name
	 * <dd>Operating system name of the (remote) connection. For example, given results from the "uname" command:
	 * <ul>
	 * <li>Linux</li>
	 * <li>AIX</li>
	 * <li>Mac OS X - if results equal "Darwin" then results from "sw_vers -productName"</li>
	 * <li>everything else - results from "uname" command</li>
	 * </ul>
	 * <dt>os.version
	 * <dd>Operating system version of the (remote) connection. For example:
	 * <ul>
	 * <li>For Linux - results from "uname -r" such as "2.6.32-279.2.1.el6.x86_64"</li>
	 * <li>For AIX - results from "oslevel" such as "7.1.0.0"</li>
	 * <li>For Mac OS X - results from "sw_vers -productVersion" such as "10.8.3"</li>
	 * <li>For everything else - "unknown"</li>
	 * </ul>
	 * <dt>os.arch
	 * <dd>Machine architecture of the (remote) connection. For example:
	 * <ul>
	 * <li>For Linux - results from "uname -m" such as "x86_64"</li>
	 * <li>For AIX - if results from "uname -p" equals "powerpc"
	 * <ul style="list-style: none;">
	 * <li>then if "prtconf -k" contains "64-bit" then "ppc64" else "ppc"</li>
	 * <li>else the result from "uname -p"</li>
	 * </ul>
	 * </li>
	 * <li>For Mac OS X - if results from "uname -m" equals "i386"
	 * <ul style="list-style: none;">
	 * <li>then if results from "sysctl -n hw.optional.x86_64" equals "1" then "x86_64" else the results from "uname -m"</li>
	 * <li>else the results from "uname -m"</li>
	 * </ul>
	 * </li>
	 * <li>For everything else - "unknown"</li>
	 * </ul>
	 * <dl>
	 *
	 */
	private void loadProperties(IProgressMonitor monitor) throws RemoteConnectionException {
		SubMonitor subMon = SubMonitor.convert(monitor, 100);
		fProperties.put(IRemoteConnection.FILE_SEPARATOR_PROPERTY, "/"); //$NON-NLS-1$
		fProperties.put(IRemoteConnection.PATH_SEPARATOR_PROPERTY, ":"); //$NON-NLS-1$
		fProperties.put(IRemoteConnection.LINE_SEPARATOR_PROPERTY, "\n"); //$NON-NLS-1$
		fProperties.put(IRemoteConnection.USER_HOME_PROPERTY, getWorkingDirectory());

		String osVersion;
		String osArch;
		String encoding = DEFAULT_ENCODING;

		String osName = executeCommand("uname", subMon.newChild(10)); //$NON-NLS-1$
		switch (osName.toLowerCase()) {
		case "linux": //$NON-NLS-1$
			osArch = executeCommand("uname -m", subMon.newChild(10)); //$NON-NLS-1$
			osVersion = executeCommand("uname -r", subMon.newChild(10)); //$NON-NLS-1$
			try {
				encoding = executeCommand("locale charmap", subMon.newChild(10)); //$NON-NLS-1$
			} catch (RemoteConnectionException e) {
				// Use default
			}
			break;

		case "darwin": //$NON-NLS-1$
			osName = executeCommand("sw_vers -productName", subMon.newChild(10)); //$NON-NLS-1$
			osVersion = executeCommand("sw_vers -productVersion", subMon.newChild(10)); //$NON-NLS-1$
			osArch = executeCommand("uname -m", subMon.newChild(10)); //$NON-NLS-1$
			if (osArch.equalsIgnoreCase("i386")) { //$NON-NLS-1$
				String opt = executeCommand("sysctl -n hw.optional.x86_64", subMon.newChild(10)); //$NON-NLS-1$
				if (opt.equals("1")) { //$NON-NLS-1$
					osArch = "x86_64"; //$NON-NLS-1$
				}
			}
			try {
				encoding = executeCommand("locale charmap", subMon.newChild(10)); //$NON-NLS-1$
			} catch (RemoteConnectionException e) {
				// Use default
			}
			break;

		case "aix": //$NON-NLS-1$
			osArch = executeCommand("uname -p", subMon.newChild(10)); //$NON-NLS-1$
			osVersion = executeCommand("oslevel", subMon.newChild(10)); //$NON-NLS-1$
			if (osArch.equalsIgnoreCase("powerpc")) { //$NON-NLS-1$
				/* Make the architecture match what Linux produces: either ppc or ppc64 */
				osArch = "ppc"; //$NON-NLS-1$
				/* Get Kernel type either 32-bit or 64-bit */
				String opt = executeCommand("prtconf -k", subMon.newChild(10)); //$NON-NLS-1$
				if (opt.indexOf("64-bit") > 0) { //$NON-NLS-1$
					osArch += "64"; //$NON-NLS-1$
				}
			}
			try {
				encoding = executeCommand("locale charmap", subMon.newChild(10)); //$NON-NLS-1$
			} catch (RemoteConnectionException e) {
				// Use default
			}
			break;

		case "qnx": //$NON-NLS-1$
			osArch = executeCommand("uname -p", subMon.newChild(10)); //$NON-NLS-1$
			osVersion = executeCommand("uname -r", subMon.newChild(10)); //$NON-NLS-1$
			break;

		default:
			osVersion = "unknown"; //$NON-NLS-1$
			osArch = "unknown"; //$NON-NLS-1$
			encoding = "unknown"; //$NON-NLS-1$
			break;
		}

		fProperties.put(IRemoteConnection.OS_NAME_PROPERTY, osName);
		fProperties.put(IRemoteConnection.OS_VERSION_PROPERTY, osVersion);
		fProperties.put(IRemoteConnection.OS_ARCH_PROPERTY, osArch);
		fProperties.put(IRemoteConnection.LOCALE_CHARMAP_PROPERTY, encoding);
	}

	private Session newSession(IProgressMonitor monitor) throws RemoteConnectionException {
		SubMonitor progress = SubMonitor.convert(monitor, 10);
		try {
			IRemoteConnectionWorkingCopy wc = getRemoteConnection().getWorkingCopy();
			IRemoteConnectionHostService hostService = wc.getService(IRemoteConnectionHostService.class);
			IUserAuthenticatorService authService = wc.getService(IUserAuthenticatorService.class);
			Session session = fJSchService.createSession(hostService.getHostname(), hostService.getPort(),
					hostService.getUsername());
			session.setUserInfo(new JSchUserInfo(hostService, authService));
			if (hostService.usePassword()) {
				session.setConfig("PreferredAuthentications", //$NON-NLS-1$
						"password,keyboard-interactive,gssapi-with-mic,publickey"); //$NON-NLS-1$
			} else {
				session.setConfig("PreferredAuthentications", //$NON-NLS-1$
						"publickey,gssapi-with-mic,password,keyboard-interactive"); //$NON-NLS-1$
			}
			String password = hostService.getPassword();
			if (!password.isEmpty()) {
				session.setPassword(password);
			}
			if (getProxyCommand().isEmpty() && getProxyConnectionName().isEmpty()) {
				fJSchService.connect(session, getTimeout() * 1000, progress.newChild(10)); // connect without proxy
			} else {
				if (getProxyCommand().isEmpty()) {
					session.setProxy(
							JSchConnectionProxyFactory.createForwardProxy(getProxyConnection(), progress.newChild(10)));
					fJSchService.connect(session, getTimeout() * 1000, progress.newChild(10));
				} else {
					session.setProxy(JSchConnectionProxyFactory.createCommandProxy(getProxyConnection(),
							getProxyCommand(), progress.newChild(10)));
					session.connect(getTimeout() * 1000); // the fJSchService doesn't pass the timeout correctly
				}
			}
			if (progress.isCanceled()) {
				return null;
			}
			wc.save();
			fSessions.add(session);
			return session;
		} catch (OperationCanceledException e) {
			throw new RemoteConnectionException(Messages.JSchConnection_0);
		} catch (JSchException e) {
			throw new RemoteConnectionException(e.getMessage());
		}
	}

	@Override
	public void open(IProgressMonitor monitor) throws RemoteConnectionException {
		open(monitor, true);
	}

	/**
	 * Open ssh connection without full setup (environment, sftp)
	 *
	 * @see org.eclipse.remote.core.IRemoteConnection#open()
	 *
	 * @param monitor
	 * @throws RemoteConnectionException
	 */
	public void openMinimal(IProgressMonitor monitor) throws RemoteConnectionException {
		open(monitor, false);
	}

	/**
	 * @see org.eclipse.remote.core.IRemoteConnection#open()
	 *
	 * @param monitor
	 * @param setupFully
	 *            open a full featured connection (environment query and sftp)
	 * @throws RemoteConnectionException
	 */
	private void open(IProgressMonitor monitor, boolean setupFully) throws RemoteConnectionException {
		SubMonitor subMon = SubMonitor.convert(monitor, 60);
		if (!hasOpenSession()) {
			checkIsConfigured();
			newSession(subMon.newChild(10));
			if (subMon.isCanceled()) {
				throw new RemoteConnectionException(Messages.JSchConnection_Connection_was_cancelled);
			}
			isFullySetup = false;
		}
		if (setupFully && !isFullySetup) { // happens on the first open with setupFully==true, which might not be the first open
			isFullySetup = true;
			// getCwd checks the exec channel before checkConfiguration checks the sftp channel
			fWorkingDir = getCwd(subMon.newChild(10));
			try {
				if (!checkConfiguration(fSessions.get(0), subMon.newChild(20))) {
					newSession(subMon.newChild(10));
					loadEnv(subMon.newChild(10));
				}
			} catch (RemoteConnectionException e) {
				// Do not throw exception now, it will be thrown if FileService is accessed.
			}
			loadProperties(subMon.newChild(10));
			fRemoteConnection.fireConnectionChangeEvent(RemoteConnectionChangeEvent.CONNECTION_OPENED);
		}
	}

	private ChannelSftp openSftpChannel(Session session) throws RemoteConnectionException {
		try {
			ChannelSftp channel = (ChannelSftp) session.openChannel("sftp"); //$NON-NLS-1$
			channel.connect();
			return channel;
		} catch (JSchException e) {
			throw new RemoteConnectionException(e.getMessage());
		}
	}

	@Override
	public void removeLocalPortForwarding(int port) throws RemoteConnectionException {
		if (!isOpen()) {
			throw new RemoteConnectionException(Messages.JSchConnection_connectionNotOpen);
		}
		try {
			fSessions.get(0).delPortForwardingL(port);
		} catch (JSchException e) {
			throw new RemoteConnectionException(e.getMessage());
		}
	}

	@Override
	public void removeRemotePortForwarding(int port) throws RemoteConnectionException {
		if (!isOpen()) {
			throw new RemoteConnectionException(Messages.JSchConnection_connectionNotOpen);
		}
		try {
			fSessions.get(0).delPortForwardingR(port);
		} catch (JSchException e) {
			throw new RemoteConnectionException(e.getMessage());
		}
	}

	@Override
	public void setWorkingDirectory(String path) {
		if (RemoteServicesUtils.posixPath(path).isAbsolute()) {
			fWorkingDir = path;
		}
	}

	@Override
	public boolean useLoginShell() {
		String str = fRemoteConnection.getAttribute(USE_LOGIN_SHELL_ATTR);
		return !str.isEmpty() ? Boolean.parseBoolean(str) : DEFAULT_USE_LOGIN_SHELL;
	}

	@Override
	public void setHostname(String hostname) {
		if (fRemoteConnection instanceof IRemoteConnectionWorkingCopy) {
			IRemoteConnectionWorkingCopy wc = (IRemoteConnectionWorkingCopy) fRemoteConnection;
			wc.setAttribute(ADDRESS_ATTR, hostname);
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
		if (fRemoteConnection instanceof IRemoteConnectionWorkingCopy) {
			IRemoteConnectionWorkingCopy wc = (IRemoteConnectionWorkingCopy) fRemoteConnection;
			wc.setAttribute(USE_LOGIN_SHELL_ATTR, Boolean.toString(useLogingShell));
		}
	}

	@Override
	public void setUsePassword(boolean usePassword) {
		if (fRemoteConnection instanceof IRemoteConnectionWorkingCopy) {
			IRemoteConnectionWorkingCopy wc = (IRemoteConnectionWorkingCopy) fRemoteConnection;
			wc.setAttribute(IS_PASSWORD_ATTR, Boolean.toString(usePassword));
		}
	}

	@Override
	public void setUsername(String username) {
		if (fRemoteConnection instanceof IRemoteConnectionWorkingCopy) {
			IRemoteConnectionWorkingCopy wc = (IRemoteConnectionWorkingCopy) fRemoteConnection;
			wc.setAttribute(USERNAME_ATTR, username);
		}
	}

}
