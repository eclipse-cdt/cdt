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
package org.eclipse.internal.remote.core.services.local;

import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.remote.core.IRemoteFileManager;

public class LocalFileManager implements IRemoteFileManager {
	private final LocalConnection fConnection;

	public LocalFileManager(LocalConnection conn) {
		fConnection = conn;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.remote.core.IRemoteFileManager#getDirectorySeparator()
	 */
	public String getDirectorySeparator() {
		return System.getProperty("file.separator", "/"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.remote.core.IRemoteFileManager#getResource(java.lang.String)
	 */
	public IFileStore getResource(String pathStr) {
		IPath path = new Path(pathStr);
		if (!path.isAbsolute()) {
			path = new Path(fConnection.getWorkingDirectory()).append(path);
		}
		return EFS.getLocalFileSystem().getStore(path);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.remote.core.IRemoteFileManager#toPath(java.net.URI)
	 */
	public String toPath(URI uri) {
		return URIUtil.toPath(uri).toString();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.remote.core.IRemoteFileManager#toURI(org.eclipse.core.runtime.IPath)
	 */
	public URI toURI(IPath path) {
		return URIUtil.toURI(path);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.remote.core.IRemoteFileManager#toURI(java.lang.String)
	 */
	public URI toURI(String path) {
		return URIUtil.toURI(path);
	}
}
