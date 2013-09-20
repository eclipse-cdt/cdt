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
package org.eclipse.internal.remote.jsch.ui;

import org.eclipse.jface.window.Window;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.remote.ui.IRemoteUIFileManager;
import org.eclipse.remote.ui.dialogs.RemoteResourceBrowser;
import org.eclipse.swt.widgets.Shell;

public class JSchUIFileManager implements IRemoteUIFileManager {
	private IRemoteServices services = null;
	private IRemoteConnection connection = null;
	private boolean showConnections = false;

	public JSchUIFileManager(IRemoteServices services) {
		this.services = services;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.core.IRemoteFileManager#browseDirectory(org.eclipse
	 * .swt.widgets.Shell, java.lang.String, java.lang.String)
	 */
	public String browseDirectory(Shell shell, String message, String filterPath, int flags) {
		RemoteResourceBrowser browser = new RemoteResourceBrowser(services, connection, shell, RemoteResourceBrowser.SINGLE);
		browser.setType(RemoteResourceBrowser.DIRECTORY_BROWSER);
		browser.setInitialPath(filterPath);
		browser.setTitle(message);
		browser.showConnections(showConnections);
		if (browser.open() == Window.CANCEL) {
			return null;
		}
		connection = browser.getConnection();
		String path = browser.getPath();
		if (path == null) {
			return null;
		}
		return path;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.core.IRemoteFileManager#browseFile(org.eclipse
	 * .swt.widgets.Shell, java.lang.String, java.lang.String)
	 */
	public String browseFile(Shell shell, String message, String filterPath, int flags) {
		RemoteResourceBrowser browser = new RemoteResourceBrowser(services, connection, shell, RemoteResourceBrowser.SINGLE);
		browser.setType(RemoteResourceBrowser.FILE_BROWSER);
		browser.setInitialPath(filterPath);
		browser.setTitle(message);
		browser.showConnections(showConnections);
		if (browser.open() == Window.CANCEL) {
			return null;
		}
		connection = browser.getConnection();
		String path = browser.getPath();
		if (path == null) {
			return null;
		}
		return path;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.core.IRemoteFileManager#browseFile(org.eclipse
	 * .swt.widgets.Shell, java.lang.String, java.lang.String)
	 */
	public String[] browseFiles(Shell shell, String message, String filterPath, int flags) {
		RemoteResourceBrowser browser = new RemoteResourceBrowser(services, connection, shell, RemoteResourceBrowser.MULTI);
		browser.setType(RemoteResourceBrowser.FILE_BROWSER);
		browser.setInitialPath(filterPath);
		browser.setTitle(message);
		browser.showConnections(showConnections);
		if (browser.open() == Window.CANCEL) {
			return null;
		}
		connection = browser.getConnection();
		String path[] = browser.getPaths();
		if (path == null) {
			return null;
		}
		return path;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.ui.IRemoteUIFileManager#getConnection()
	 */
	public IRemoteConnection getConnection() {
		return connection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.ui.IRemoteUIFileManager#setConnection(org.eclipse
	 * .ptp.remote.core.IRemoteConnection)
	 */
	public void setConnection(IRemoteConnection connection) {
		this.connection = connection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.ui.IRemoteUIFileManager#showConnections(boolean)
	 */
	public void showConnections(boolean enable) {
		showConnections = enable;
	}
}
