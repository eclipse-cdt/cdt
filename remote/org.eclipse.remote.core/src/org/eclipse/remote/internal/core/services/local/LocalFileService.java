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

import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnection.Service;
import org.eclipse.remote.core.IRemoteFileService;
import org.eclipse.remote.core.IRemoteProcessService;

public class LocalFileService implements IRemoteFileService {

	private final IRemoteConnection connection;

	public LocalFileService(IRemoteConnection connection) {
		this.connection = connection;
	}

	public static class Factory implements IRemoteFileService.Factory {
		@SuppressWarnings("unchecked")
		@Override
		public <T extends Service> T getService(IRemoteConnection remoteConnection, Class<T> service) {
			if (IRemoteFileService.class.equals(service)) {
				return (T) new LocalFileService(remoteConnection);
			}
			return null;
		}
	}

	@Override
	public IRemoteConnection getRemoteConnection() {
		return connection;
	}

	@Override
	public String getDirectorySeparator() {
		return System.getProperty("file.separator", "/"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public IFileStore getResource(String path) {
		return EFS.getLocalFileSystem().getStore(Path.fromOSString(path));
	}

	@Override
	public String getBaseDirectory() {
		return connection.getService(IRemoteProcessService.class).getWorkingDirectory();
	}

	@Override
	public void setBaseDirectory(String path) {
		connection.getService(IRemoteProcessService.class).setWorkingDirectory(path);
	}

	@Override
	public String toPath(URI uri) {
		return URIUtil.toPath(uri).toString();
	}

	@Override
	public URI toURI(IPath path) {
		return URIUtil.toURI(path);
	}

	@Override
	public URI toURI(String path) {
		return URIUtil.toURI(path);
	}

}
