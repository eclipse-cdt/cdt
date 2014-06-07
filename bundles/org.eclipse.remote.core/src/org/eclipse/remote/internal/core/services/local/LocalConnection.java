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
package org.eclipse.remote.internal.core.services.local;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Path;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionChangeEvent;
import org.eclipse.remote.core.IRemoteConnectionChangeListener;
import org.eclipse.remote.core.IRemoteConnectionManager;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.IRemoteFileManager;
import org.eclipse.remote.core.IRemoteProcess;
import org.eclipse.remote.core.IRemoteProcessBuilder;
import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.remote.core.exception.UnableToForwardPortException;
import org.eclipse.remote.internal.core.RemoteCorePlugin;
import org.eclipse.remote.internal.core.messages.Messages;

public class LocalConnection implements IRemoteConnection {
	private final String fName = IRemoteConnectionManager.LOCAL_CONNECTION_NAME;
	private final String fAddress = Messages.LocalConnection_1;
	private final String fUsername = System.getProperty("user.name"); //$NON-NLS-1$
	private boolean fConnected = true;
	private IPath fWorkingDir = null;

	private final IRemoteFileManager fFileMgr = new LocalFileManager();
	private final IRemoteConnection fConnection = this;
	private final IRemoteServices fRemoteServices;
	private final ListenerList fListeners = new ListenerList();

	public LocalConnection(IRemoteServices services) {
		fRemoteServices = services;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.remote.core.IRemoteConnection#addConnectionChangeListener
	 * (org.eclipse.remote.core.IRemoteConnectionChangeListener)
	 */
	@Override
	public void addConnectionChangeListener(IRemoteConnectionChangeListener listener) {
		fListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#close()
	 */
	@Override
	public void close() {
		if (fConnected) {
			fConnected = false;
			fireConnectionChangeEvent(IRemoteConnectionChangeEvent.CONNECTION_CLOSED);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(IRemoteConnection connection) {
		return getName().compareTo(connection.getName());
	}

	/**
	 * Notify all listeners when this connection's status changes.
	 * 
	 * @param event
	 */
	@Override
	public void fireConnectionChangeEvent(final int type) {
		IRemoteConnectionChangeEvent event = new IRemoteConnectionChangeEvent() {
			@Override
			public IRemoteConnection getConnection() {
				return fConnection;
			}

			@Override
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
	 * @see org.eclipse.remote.core.IRemoteConnection#forwardLocalPort(int,
	 * java.lang.String, int)
	 */
	@Override
	public void forwardLocalPort(int localPort, String fwdAddress, int fwdPort) throws RemoteConnectionException {
		throw new UnableToForwardPortException(Messages.LocalConnection_2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.remote.core.IRemoteConnection#forwardLocalPort(java.lang
	 * .String, int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public int forwardLocalPort(String fwdAddress, int fwdPort, IProgressMonitor monitor) throws RemoteConnectionException {
		throw new UnableToForwardPortException(Messages.LocalConnection_2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#forwardRemotePort(int,
	 * java.lang.String, int)
	 */
	@Override
	public void forwardRemotePort(int remotePort, String fwdAddress, int fwdPort) throws RemoteConnectionException {
		throw new UnableToForwardPortException(Messages.LocalConnection_2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.remote.core.IRemoteConnection#forwardRemotePort(java.
	 * lang.String, int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public int forwardRemotePort(String fwdAddress, int fwdPort, IProgressMonitor monitor) throws RemoteConnectionException {
		throw new UnableToForwardPortException(Messages.LocalConnection_2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#getAddress()
	 */
	@Override
	public String getAddress() {
		return fAddress;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#getAttributes()
	 */
	@Override
	public Map<String, String> getAttributes() {
		return new HashMap<String, String>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteServices#getCommandShell(int)
	 */
	@Override
	public IRemoteProcess getCommandShell(int flags) throws IOException {
		throw new IOException("Not currently implemented"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#getEnv()
	 */
	@Override
	public Map<String, String> getEnv() {
		return System.getenv();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.remote.core.IRemoteConnection#getEnv(java.lang.String)
	 */
	@Override
	public String getEnv(String name) {
		return System.getenv(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteServices#getFileManager(j)
	 */
	@Override
	public IRemoteFileManager getFileManager() {
		return fFileMgr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#getName()
	 */
	@Override
	public String getName() {
		return fName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#getPort()
	 */
	@Override
	public int getPort() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteServices#getProcessBuilder(java.util.List)
	 */
	@Override
	public IRemoteProcessBuilder getProcessBuilder(List<String> command) {
		return new LocalProcessBuilder(command);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteServices#getProcessBuilder(java.lang.String[])
	 */
	@Override
	public IRemoteProcessBuilder getProcessBuilder(String... command) {
		return new LocalProcessBuilder(command);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.remote.core.IRemoteConnection#getProperty(java.lang.String
	 * )
	 */
	@Override
	public String getProperty(String key) {
		/*
		 * Convert os.name and os.arch to framework properties so they make more sense
		 */
		switch (key) {
		case IRemoteConnection.OS_NAME_PROPERTY:
			return RemoteCorePlugin.getDefault().getBundle().getBundleContext().getProperty("osgi.os"); //$NON-NLS-1$
		case IRemoteConnection.OS_ARCH_PROPERTY:
			return RemoteCorePlugin.getDefault().getBundle().getBundleContext().getProperty("osgi.arch"); //$NON-NLS-1$
		}
		return System.getProperty(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#getRemoteServices()
	 */
	@Override
	public IRemoteServices getRemoteServices() {
		return fRemoteServices;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#getUsername()
	 */
	@Override
	public String getUsername() {
		return fUsername;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#getWorkingCopy()
	 */
	@Override
	public IRemoteConnectionWorkingCopy getWorkingCopy() {
		return new LocalConnectionWorkingCopy(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.remote.core.IRemoteFileManager#getWorkingDirectory(org
	 * .eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public String getWorkingDirectory() {
		if (fWorkingDir == null) {
			String cwd = System.getProperty("user.home"); //$NON-NLS-1$
			if (cwd == null) {
				cwd = System.getProperty("user.dir"); //$NON-NLS-1$;
			}
			if (cwd == null) {
				fWorkingDir = Path.ROOT;
			} else {
				fWorkingDir = new Path(cwd);
			}
		}
		return fWorkingDir.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#isOpen()
	 */
	@Override
	public boolean isOpen() {
		return fConnected;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#open()
	 */
	@Override
	public void open(IProgressMonitor monitor) throws RemoteConnectionException {
		if (!fConnected) {
			fConnected = true;
			fireConnectionChangeEvent(IRemoteConnectionChangeEvent.CONNECTION_OPENED);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.remote.core.IRemoteConnection#removeConnectionChangeListener
	 * (org.eclipse.remote.core.IRemoteConnectionChangeListener)
	 */
	@Override
	public void removeConnectionChangeListener(IRemoteConnectionChangeListener listener) {
		fListeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnection#removeLocalPortForwarding(int)
	 */
	@Override
	public void removeLocalPortForwarding(int port) throws RemoteConnectionException {
		// Do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.remote.core.IRemoteConnection#removeRemotePortForwarding(int)
	 */
	@Override
	public void removeRemotePortForwarding(int port) throws RemoteConnectionException {
		// Do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.remote.core.IRemoteFileManager#setWorkingDirectory(java
	 * .lang.String)
	 */
	@Override
	public void setWorkingDirectory(String pathStr) {
		IPath path = new Path(pathStr);
		if (path.isAbsolute()) {
			fWorkingDir = path;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.remote.core.IRemoteConnection#supportsTCPPortForwarding()
	 */
	@Override
	public boolean supportsTCPPortForwarding() {
		return false;
	}

}
