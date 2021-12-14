/*******************************************************************************
 * Copyright (c) 2016 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.remote.internal.proxy.core;

import java.net.URI;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnection.Service;
import org.eclipse.remote.core.IRemoteFileService;
import org.eclipse.remote.core.IRemoteProcessService;
import org.eclipse.remote.core.RemoteServicesUtils;

public class ProxyFileManager implements IRemoteFileService {
	private final IRemoteConnection fConnection;

	private ProxyFileManager(IRemoteConnection connection) {
		fConnection = connection;
	}

	public static class Factory implements IRemoteFileService.Factory {
		@SuppressWarnings("unchecked")
		@Override
		public <T extends Service> T getService(IRemoteConnection remoteConnection, Class<T> service) {
			if (IRemoteFileService.class.equals(service)) {
				return (T) new ProxyFileManager(remoteConnection);
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
		return ProxyFileStore.getInstance(ProxyFileSystem.getURIFor(fConnection.getName(), path.toString()));
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
			return ProxyFileSystem.getURIFor(fConnection.getName(), path.toString());
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public URI toURI(String path) {
		return toURI(RemoteServicesUtils.posixPath(path));
	}
}
