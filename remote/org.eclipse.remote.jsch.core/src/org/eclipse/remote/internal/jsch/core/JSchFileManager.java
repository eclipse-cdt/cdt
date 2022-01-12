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
 * Martin Oberhuber - [468889] Support Eclipse older than Mars
 *******************************************************************************/
package org.eclipse.remote.internal.jsch.core;

import java.net.URI;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnection.Service;
import org.eclipse.remote.core.IRemoteFileService;
import org.eclipse.remote.core.IRemoteProcessService;
import org.eclipse.remote.core.RemoteServicesUtils;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.remote.internal.jsch.core.messages.Messages;

public class JSchFileManager implements IRemoteFileService {
	private final IRemoteConnection fConnection;

	private JSchFileManager(IRemoteConnection connection) {
		fConnection = connection;
	}

	public static class Factory implements IRemoteFileService.Factory {
		@SuppressWarnings("unchecked")
		@Override
		public <T extends Service> T getService(IRemoteConnection remoteConnection, Class<T> service) {
			if (IRemoteFileService.class.equals(service)) {
				if (remoteConnection instanceof JSchConnection)
					try {
						((JSchConnection) remoteConnection).getSftpCommandChannel();
					} catch (RemoteConnectionException e) {
						throw new UnsupportedOperationException(
								Messages.JSchConnection_Remote_host_does_not_support_sftp);
					}
				return (T) new JSchFileManager(remoteConnection);
			}
			return null;
		}
	}

	@Override
	public IRemoteConnection getRemoteConnection() {
		return fConnection;
	}

	@Override
	public String getDirectorySeparator() {
		return "/"; //$NON-NLS-1$
	}

	@Override
	public IFileStore getResource(String pathStr) {
		IPath path = RemoteServicesUtils.posixPath(pathStr);
		if (!path.isAbsolute()) {
			path = RemoteServicesUtils.posixPath(getBaseDirectory()).append(path);
		}
		return JschFileStore.getInstance(JSchFileSystem.getURIFor(fConnection.getName(), path.toString()));
	}

	@Override
	public String getBaseDirectory() {
		return fConnection.getService(IRemoteProcessService.class).getWorkingDirectory();
	}

	@Override
	public void setBaseDirectory(String path) {
		fConnection.getService(IRemoteProcessService.class).setWorkingDirectory(path);
	}

	@Override
	public String toPath(URI uri) {
		return uri.getPath();
	}

	@Override
	public URI toURI(IPath path) {
		try {
			return JSchFileSystem.getURIFor(fConnection.getName(), path.toString());
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public URI toURI(String path) {
		return toURI(RemoteServicesUtils.posixPath(path));
	}
}
