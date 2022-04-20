/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.remote.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.jface.window.Window;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteConnectionType.Service;
import org.eclipse.remote.ui.dialogs.RemoteResourceBrowser;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

/**
 * @since 2.1
 */
public class RemoteUIFileService implements IRemoteUIFileService {
	private final IRemoteConnectionType connectionType;
	private IRemoteConnection connection = null;
	private boolean showConnections = false;

	public RemoteUIFileService(IRemoteConnectionType connectionType) {
		this.connectionType = connectionType;
	}

	public static class Factory implements IRemoteConnectionType.Service.Factory {
		@SuppressWarnings("unchecked")
		@Override
		public <T extends Service> T getService(IRemoteConnectionType connectionType, Class<T> service) {
			if (IRemoteUIFileService.class.equals(service)) {
				return (T) new RemoteUIFileService(connectionType);
			}
			return null;
		}
	}

	@Override
	public IRemoteConnectionType getConnectionType() {
		return connectionType;
	}

	@Override
	public String browseDirectory(Shell shell, String message, String filterPath, int flags) {
		RemoteResourceBrowser browser = new RemoteResourceBrowser(shell, SWT.SINGLE);
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

	@Override
	public String browseFile(Shell shell, String message, String filterPath, int flags) {
		RemoteResourceBrowser browser = new RemoteResourceBrowser(shell, SWT.SINGLE);
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

	@Override
	public List<String> browseFiles(Shell shell, String message, String filterPath, int flags) {
		RemoteResourceBrowser browser = new RemoteResourceBrowser(shell, SWT.MULTI);
		browser.setType(RemoteResourceBrowser.FILE_BROWSER);
		browser.setInitialPath(filterPath);
		browser.setTitle(message);
		browser.showConnections(showConnections);
		browser.setConnection(connection);
		if (browser.open() == Window.CANCEL) {
			return null;
		}
		connection = browser.getConnection();
		List<String> paths = new ArrayList<>();
		for (IFileStore store : browser.getResources()) {
			paths.add(store.toURI().getPath());
		}
		return paths;
	}

	@Override
	public IRemoteConnection getConnection() {
		return connection;
	}

	@Override
	public void setConnection(IRemoteConnection connection) {
		this.connection = connection;
	}

	@Override
	public void showConnections(boolean enable) {
		showConnections = enable;
	}
}
