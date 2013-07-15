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

import java.net.PasswordAuthentication;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.internal.remote.jsch.core.messages.Messages;
import org.eclipse.jsch.core.IJSchLocation;
import org.eclipse.jsch.core.IJSchService;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionChangeEvent;
import org.eclipse.remote.core.IRemoteConnectionChangeListener;
import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.remote.core.IUserAuthenticator;
import org.eclipse.remote.core.exception.AddressInUseException;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.remote.core.exception.UnableToForwardPortException;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

/**
 * @since 5.0
 */
public class JSchConnection implements IRemoteConnection {
	private static int DEFAULT_PORT = 22;

	private String fWorkingDir;
	private Map<String, String> fEnv;
	private Map<String, String> fProperties;
	private String fHost;
	private String fUsername;
	private String fPassword;
	private int fPort = DEFAULT_PORT;
	private Session fSession;
	private String fConnName;
	private boolean fIsOpen;
	private final IJSchService fJSchService;

	private final IRemoteServices fRemoteServices;
	private final ListenerList fListeners = new ListenerList();

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
			fSession.disconnect();
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
			fSession.setPortForwardingL(localPort, fwdAddress, fwdPort);
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
			fSession.setPortForwardingR(remotePort, fwdAddress, fwdPort);
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
		if (fProperties == null) {
			fProperties = new HashMap<String, String>();
		}
		return Collections.unmodifiableMap(fProperties);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#getEnv()
	 */
	public Map<String, String> getEnv() {
		if (fEnv == null) {
			fEnv = new HashMap<String, String>();
		}
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
	private Map<String, String> getProperties() {
		if (fProperties == null) {
			fProperties = new HashMap<String, String>();
			fProperties.put(FILE_SEPARATOR_PROPERTY, "/"); //$NON-NLS-1$
			fProperties.put(PATH_SEPARATOR_PROPERTY, ":"); //$NON-NLS-1$
			fProperties.put(LINE_SEPARATOR_PROPERTY, "\n"); //$NON-NLS-1$
		}
		return fProperties;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#getProperty(java.lang.String )
	 */
	public String getProperty(String key) {
		return getProperties().get(key);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#open()
	 */
	public void open(IProgressMonitor monitor) throws RemoteConnectionException {
		if (!isOpen()) {
			checkIsConfigured();
			SubMonitor progress = SubMonitor.convert(monitor, 10);
			try {
				fSession = fJSchService.createSession(fHost, fPort, fUsername);
				fSession.setPassword(fPassword);
				fJSchService.connect(fSession, 0, progress.newChild(10));
			} catch (JSchException e) {
				throw new RemoteConnectionException(e.getMessage());
			}
			fIsOpen = true;
			fireConnectionChangeEvent(this, IRemoteConnectionChangeEvent.CONNECTION_OPENED);
		}
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
			SubMonitor progress = SubMonitor.convert(monitor, 10);
			try {
				final IJSchLocation location = fJSchService.getLocation(fUsername, fHost, fPort);
				location.setPassword(fPassword);
				fSession = fJSchService.createSession(location, new UserInfo() {

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
				});
				fJSchService.connect(fSession, 0, progress.newChild(10));
			} catch (JSchException e) {
				throw new RemoteConnectionException(e.getMessage());
			}
			fIsOpen = true;
			fireConnectionChangeEvent(this, IRemoteConnectionChangeEvent.CONNECTION_OPENED);
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
			fSession.delPortForwardingL(port);
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
			fSession.delPortForwardingR(port);
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

	public Session getSession() {
		return fSession;
	}
}
