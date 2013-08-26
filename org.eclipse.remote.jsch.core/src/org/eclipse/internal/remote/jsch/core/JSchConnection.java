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
package org.eclipse.internal.remote.jsch.core;

import java.io.IOException;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.internal.remote.jsch.core.commands.ExecCommand;
import org.eclipse.internal.remote.jsch.core.messages.Messages;
import org.eclipse.jsch.core.IJSchLocation;
import org.eclipse.jsch.core.IJSchService;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionChangeEvent;
import org.eclipse.remote.core.IRemoteConnectionChangeListener;
import org.eclipse.remote.core.IRemoteFileManager;
import org.eclipse.remote.core.IRemoteProcess;
import org.eclipse.remote.core.IRemoteProcessBuilder;
import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.remote.core.IUserAuthenticator;
import org.eclipse.remote.core.exception.AddressInUseException;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.remote.core.exception.UnableToForwardPortException;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

/**
 * @since 5.0
 */
public class JSchConnection implements IRemoteConnection {
	private static int DEFAULT_PORT = 22;

	private String fWorkingDir;
	private final Map<String, String> fEnv = new HashMap<String, String>();
	private final Map<String, String> fProperties = new HashMap<String, String>();
	private String fHost;
	private String fUsername;
	private String fPassword;
	private int fPort = DEFAULT_PORT;
	private String fConnName;
	private boolean fIsOpen;
	private final IJSchService fJSchService;

	private final IRemoteServices fRemoteServices;
	private final ListenerList fListeners = new ListenerList();
	private final List<Session> fSessions = new ArrayList<Session>();

	private ChannelSftp fSftpChannel;

	public JSchConnection(String name, IRemoteServices services) {
		fConnName = name;
		fRemoteServices = services;
		fJSchService = Activator.getDefault().getService();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#addConnectionChangeListener
	 * (org.eclipse.remote.core.IRemoteConnectionChangeListener)
	 */
	public void addConnectionChangeListener(IRemoteConnectionChangeListener listener) {
		fListeners.add(listener);
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
		if (fHost == null) {
			throw new RemoteConnectionException(Messages.JSchConnection_remote_address_must_be_set);
		}
		if (fUsername == null) {
			throw new RemoteConnectionException(Messages.JSchConnection_username_must_be_set);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#close()
	 */
	public synchronized void close() {
		if (isOpen()) {
			if (fSftpChannel != null && fSftpChannel.isConnected()) {
				fSftpChannel.disconnect();
			}
			for (Session session : fSessions) {
				if (session.isConnected()) {
					session.disconnect();
				}
			}
			fIsOpen = false;
			fireConnectionChangeEvent(this, IRemoteConnectionChangeEvent.CONNECTION_CLOSED);
		}
	}

	/**
	 * Notify all fListeners when this connection's status changes.
	 * 
	 * @param event
	 */
	public void fireConnectionChangeEvent(final IRemoteConnection connection, final int type) {
		IRemoteConnectionChangeEvent event = new IRemoteConnectionChangeEvent() {
			public IRemoteConnection getConnection() {
				return connection;
			}

			public int getType() {
				return type;
			}
		};
		for (Object listener : fListeners.getListeners()) {
			((IRemoteConnectionChangeListener) listener).connectionChanged(event);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#forwardLocalPort(int, java.lang.String, int)
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#forwardLocalPort(java.lang .String, int,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public int forwardLocalPort(String fwdAddress, int fwdPort, IProgressMonitor monitor) throws RemoteConnectionException {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#forwardRemotePort(int, java.lang.String, int)
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#forwardRemotePort(java. lang.String, int,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public int forwardRemotePort(String fwdAddress, int fwdPort, IProgressMonitor monitor) throws RemoteConnectionException {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#getAddress()
	 */
	public String getAddress() {
		return fHost;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#getAttributes()
	 */
	public Map<String, String> getAttributes() {
		return Collections.unmodifiableMap(fProperties);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteServices#getCommandShell(int)
	 */
	public IRemoteProcess getCommandShell(int flags) throws IOException {
		throw new IOException("Not currently implemented"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#getEnv()
	 */
	public Map<String, String> getEnv() {
		return Collections.unmodifiableMap(fEnv);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#getEnv(java.lang.String)
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteServices#getFileManager()
	 */
	public IRemoteFileManager getFileManager() {
		return new JSchFileManager(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#getName()
	 */
	public String getName() {
		return fConnName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#getPort()
	 */
	public int getPort() {
		return fPort;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteServices#getProcessBuilder(java.util.List)
	 */
	public IRemoteProcessBuilder getProcessBuilder(List<String> command) {
		return new JSchProcessBuilder(this, command);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteServices#getProcessBuilder(java.lang.String[])
	 */
	public IRemoteProcessBuilder getProcessBuilder(String... command) {
		return new JSchProcessBuilder(this, command);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#getProperty(java.lang.String )
	 */
	public String getProperty(String key) {
		return fProperties.get(key);
	}

	/**
	 * Get the result of executing a pwd command.
	 * 
	 * @return current working directory
	 */
	private String getPwd() {
		return null; // TODO: implement
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#getRemoteServices()
	 */
	public IRemoteServices getRemoteServices() {
		return fRemoteServices;
	}

	/**
	 * Open an sftp channel to the remote host. Always use the second session if available.
	 * 
	 * @return sftp channel or null if the progress monitor was cancelled
	 * @throws RemoteConnectionException
	 *             if a channel could not be opened
	 */
	public ChannelSftp getSftpChannel() throws RemoteConnectionException {
		if (fSftpChannel == null || fSftpChannel.isClosed()) {
			Session session = fSessions.get(0);
			if (fSessions.size() > 1) {
				session = fSessions.get(1);
			}
			fSftpChannel = openSftpChannel(session);
			if (fSftpChannel == null) {
				throw new RemoteConnectionException(Messages.JSchConnection_Unable_to_open_sftp_channel);
			}
		}
		return fSftpChannel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#getUsername()
	 */
	public String getUsername() {
		return fUsername;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#getWorkingDirectory()
	 */
	public String getWorkingDirectory() {
		if (!isOpen()) {
			return "/"; //$NON-NLS-1$
		}
		if (fWorkingDir == null) {
			fWorkingDir = getPwd();
			if (fWorkingDir == null) {
				return "/"; //$NON-NLS-1$
			}
		}
		return fWorkingDir;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#isOpen()
	 */
	public boolean isOpen() {
		return fIsOpen;
	}

	private void loadEnv(IProgressMonitor monitor) throws RemoteConnectionException {
		SubMonitor subMon = SubMonitor.convert(monitor, 10);
		ExecCommand exec = new ExecCommand(this);
		String env = exec.setCommand("printenv").getResult(subMon.newChild(10)).trim(); //$NON-NLS-1$
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
		fProperties.put(FILE_SEPARATOR_PROPERTY, "/"); //$NON-NLS-1$
		fProperties.put(PATH_SEPARATOR_PROPERTY, ":"); //$NON-NLS-1$
		fProperties.put(LINE_SEPARATOR_PROPERTY, "\n"); //$NON-NLS-1$
		fProperties.put(USER_HOME_PROPERTY, getPwd());

		ExecCommand exec = new ExecCommand(this);
		String osVersion;
		String osArch;
		String osName = exec.setCommand("uname").getResult(subMon.newChild(10)).trim(); //$NON-NLS-1$
		if (osName.equalsIgnoreCase("Linux")) { //$NON-NLS-1$
			osArch = exec.setCommand("uname -m").getResult(subMon.newChild(10)).trim(); //$NON-NLS-1$
			osVersion = exec.setCommand("uname -r").getResult(subMon.newChild(10)).trim(); //$NON-NLS-1$
		} else if (osName.equalsIgnoreCase("Darwin")) { //$NON-NLS-1$
			osName = exec.setCommand("sw_vers -productName").getResult(subMon.newChild(10)).trim(); //$NON-NLS-1$
			osVersion = exec.setCommand("sw_vers -productVersion").getResult(subMon.newChild(10)).trim(); //$NON-NLS-1$
			osArch = exec.setCommand("uname -m").getResult(subMon.newChild(10)).trim(); //$NON-NLS-1$
			if (osArch.equalsIgnoreCase("i386")) { //$NON-NLS-1$
				String opt = exec.setCommand("sysctl -n hw.optional.x86_64").getResult(subMon.newChild(10)).trim(); //$NON-NLS-1$
				if (opt.equals("1")) { //$NON-NLS-1$
					osArch = "x86_64"; //$NON-NLS-1$
				}
			}
		} else if (osName.equalsIgnoreCase("AIX")) { //$NON-NLS-1$
			osArch = exec.setCommand("uname -p").getResult(subMon.newChild(10)).trim(); //$NON-NLS-1$
			osVersion = exec.setCommand("oslevel").getResult(subMon.newChild(10)).trim(); //$NON-NLS-1$
			if (osArch.equalsIgnoreCase("powerpc")) { //$NON-NLS-1$
				/* Make the architecture match what Linux produces: either ppc or ppc64 */
				osArch = "ppc"; //$NON-NLS-1$
				/* Get Kernel type either 32-bit or 64-bit */
				String opt = exec.setCommand("prtconf -k").getResult(subMon.newChild(10)).trim(); //$NON-NLS-1$
				if (opt.indexOf("64-bit") > 0) { //$NON-NLS-1$
					osArch += "64"; //$NON-NLS-1$
				}
			}
		} else {
			osVersion = "unknown"; //$NON-NLS-1$
			osArch = "unknown"; //$NON-NLS-1$
		}
		fProperties.put(OS_NAME_PROPERTY, osName);
		fProperties.put(OS_VERSION_PROPERTY, osVersion);
		fProperties.put(OS_ARCH_PROPERTY, osArch);
	}

	private Session newSession(final IUserAuthenticator authenticator, IProgressMonitor monitor) throws RemoteConnectionException {
		SubMonitor progress = SubMonitor.convert(monitor, 10);
		try {
			final IJSchLocation location = fJSchService.getLocation(fUsername, fHost, fPort);
			location.setPassword(fPassword);
			UserInfo userInfo = null;
			if (authenticator != null) {
				userInfo = new UserInfo() {

					public String getPassphrase() {
						return null;
					}

					public String getPassword() {
						return location.getPassword();
					}

					public boolean promptPassphrase(String message) {
						return false;
					}

					public boolean promptPassword(String message) {
						PasswordAuthentication auth = authenticator.prompt(null, message);
						if (auth == null) {
							return false;
						}
						location.setUsername(auth.getUserName());
						location.setPassword(new String(auth.getPassword()));
						return true;
					}

					public boolean promptYesNo(String message) {
						int prompt = authenticator.prompt(IUserAuthenticator.QUESTION, Messages.AuthInfo_Authentication_message,
								message, new int[] { IUserAuthenticator.YES, IUserAuthenticator.NO }, IUserAuthenticator.YES);
						return prompt == IUserAuthenticator.YES;
					}

					public void showMessage(String message) {
						authenticator.prompt(IUserAuthenticator.INFORMATION, Messages.AuthInfo_Authentication_message, message,
								new int[] { IUserAuthenticator.OK }, IUserAuthenticator.OK);
					}
				};
			}
			Session session = fJSchService.createSession(location, userInfo);
			session.setPassword(fPassword);
			fJSchService.connect(session, 0, progress.newChild(10));
			if (!progress.isCanceled()) {
				fSessions.add(session);
				return session;
			}
			return null;
		} catch (JSchException e) {
			throw new RemoteConnectionException(e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#open()
	 */
	public void open(IProgressMonitor monitor) throws RemoteConnectionException {
		open(null, monitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#open(org.eclipse.remote.core.IUserAuthenticator,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void open(final IUserAuthenticator authenticator, IProgressMonitor monitor) throws RemoteConnectionException {
		if (!isOpen()) {
			checkIsConfigured();
			SubMonitor subMon = SubMonitor.convert(monitor, 30);
			Session session = newSession(authenticator, subMon.newChild(10));
			if (!subMon.isCanceled()) {
				if (!checkConfiguration(session, subMon.newChild(20))) {
					newSession(authenticator, subMon.newChild(10));
					loadEnv(subMon.newChild(10));
				}
				loadProperties(subMon.newChild(10));
				fIsOpen = true;
				fireConnectionChangeEvent(this, IRemoteConnectionChangeEvent.CONNECTION_OPENED);
			}
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#removeConnectionChangeListener
	 * (org.eclipse.remote.core.IRemoteConnectionChangeListener)
	 */
	public void removeConnectionChangeListener(IRemoteConnectionChangeListener listener) {
		fListeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#removeLocalPortForwarding(int)
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#removeRemotePortForwarding(int)
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#setAddress(java.lang.String )
	 */
	public void setAddress(String address) {
		fHost = address;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#setAttribute(java.lang. String, java.lang.String)
	 */
	public void setAttribute(String key, String value) {
		fProperties.put(key, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#setName(java.lang.String)
	 */
	public void setName(String name) {
		fConnName = name;
		fireConnectionChangeEvent(this, IRemoteConnectionChangeEvent.CONNECTION_RENAMED);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#setPassword(java.lang.String )
	 */
	public void setPassword(String password) {
		fPassword = password;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#setPort(int)
	 */
	public void setPort(int port) {
		fPort = port;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#setUsername(java.lang.String )
	 */
	public void setUsername(String userName) {
		fUsername = userName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#setWorkingDirectory(java.lang.String)
	 */
	public void setWorkingDirectory(String path) {
		if (new Path(path).isAbsolute()) {
			fWorkingDir = path;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#supportsTCPPortForwarding()
	 */
	public boolean supportsTCPPortForwarding() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String str = getName() + " [" + getUsername() + "@" + getAddress(); //$NON-NLS-1$ //$NON-NLS-2$
		if (getPort() >= 0) {
			str += ":" + getPort(); //$NON-NLS-1$
		}
		return str + "]"; //$NON-NLS-1$
	}
}
