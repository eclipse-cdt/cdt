/********************************************************************************
 * Copyright (c) 2016 Oak Ridge National Laboratory and others.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/legal/epl-2.0/
 ********************************************************************************/

package org.eclipse.remote.internal.proxy.ui;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.eclipse.remote.internal.proxy.core.ProxyConnection;
import org.eclipse.remote.internal.proxy.core.ProxyFileSystem;
import org.eclipse.remote.internal.proxy.ui.messages.Messages;
import org.eclipse.remote.ui.IRemoteUIFileService;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ide.fileSystem.FileSystemContributor;

public class ProxyFileSystemContributor extends FileSystemContributor {
	@Override
	public URI browseFileSystem(String initialPath, Shell shell) {
		IRemoteServicesManager manager = Activator.getService(IRemoteServicesManager.class);
		IRemoteConnectionType connectionType = manager.getConnectionType(ProxyConnection.JSCH_ID);
		IRemoteUIFileService uiFileMgr = connectionType.getService(IRemoteUIFileService.class);
		uiFileMgr.showConnections(true);
		String path = uiFileMgr.browseDirectory(shell, Messages.ProxyFileSystemContributor_0, initialPath, 0);
		if (path != null) {
			IRemoteConnection conn = uiFileMgr.getConnection();
			if (conn != null) {
				return ProxyFileSystem.getURIFor(conn.getName(), path);
			}
		}

		return null;
	}

	@Override
	public URI getURI(String string) {
		try {
			return new URI(string);
		} catch (URISyntaxException e) {
			// Ignore
		}
		return null;
	}
}