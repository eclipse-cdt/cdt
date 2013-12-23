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
package org.eclipse.remote.internal.jsch.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.jface.window.Window;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.ui.IRemoteUIFileManager;
import org.eclipse.remote.ui.dialogs.RemoteResourceBrowser;
import org.eclipse.swt.widgets.Shell;

public class JSchUIFileManager implements IRemoteUIFileManager {
	private IRemoteConnection connection = null;
	private boolean showConnections = false;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.core.IRemoteFileManager#browseDirectory(org.eclipse
	 * .swt.widgets.Shell, java.lang.String, java.lang.String)
	 */
	@Override
	public String browseDirectory(Shell shell, String message, String filterPath, int flags) {
		RemoteResourceBrowser browser = new RemoteResourceBrowser(shell, RemoteResourceBrowser.SINGLE);
		browser.setType(RemoteResourceBrowser.DIRECTORY_BROWSER);
		browser.setInitialPath(filterPath);
		browser.setTitle(message);
		browser.showConnections(showConnections);
		browser.setConnection(connection);
		if (browser.open() == Window.CANCEL) {
			return null;
		}
		connection = browser.getConnection();
		IFileStore resource = browser.getResource();
		if (resource == null) {
			return null;
		}
		return resource.toURI().getPath();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.core.IRemoteFileManager#browseFile(org.eclipse
	 * .swt.widgets.Shell, java.lang.String, java.lang.String)
	 */
	@Override
	public String browseFile(Shell shell, String message, String filterPath, int flags) {
		RemoteResourceBrowser browser = new RemoteResourceBrowser(shell, RemoteResourceBrowser.SINGLE);
		browser.setType(RemoteResourceBrowser.FILE_BROWSER);
		browser.setInitialPath(filterPath);
		browser.setTitle(message);
		browser.showConnections(showConnections);
		browser.setConnection(connection);
		if (browser.open() == Window.CANCEL) {
			return null;
		}
		connection = browser.getConnection();
		IFileStore resource = browser.getResource();
		if (resource == null) {
			return null;
		}
		return resource.toURI().getPath();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.core.IRemoteFileManager#browseFile(org.eclipse
	 * .swt.widgets.Shell, java.lang.String, java.lang.String)
	 */
	@Override
	public List<String> browseFiles(Shell shell, String message, String filterPath, int flags) {
		RemoteResourceBrowser browser = new RemoteResourceBrowser(shell, RemoteResourceBrowser.MULTI);
		browser.setType(RemoteResourceBrowser.FILE_BROWSER);
		browser.setInitialPath(filterPath);
		browser.setTitle(message);
		browser.showConnections(showConnections);
		browser.setConnection(connection);
		if (browser.open() == Window.CANCEL) {
			return null;
		}
		connection = browser.getConnection();
		List<String> paths = new ArrayList<String>();
		for (IFileStore store : browser.getResources()) {
			paths.add(store.toURI().getPath());
		}
		return paths;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.ui.IRemoteUIFileManager#getConnection()
	 */
	@Override
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
	@Override
	public void setConnection(IRemoteConnection connection) {
		this.connection = connection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.ui.IRemoteUIFileManager#showConnections(boolean)
	 */
	@Override
	public void showConnections(boolean enable) {
		showConnections = enable;
	}
}
