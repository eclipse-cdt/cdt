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
import org.eclipse.remote.core.IRemoteFileManager;

public class LocalFileManager implements IRemoteFileManager {
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteFileManager#getDirectorySeparator()
	 */
	@Override
	public String getDirectorySeparator() {
		return System.getProperty("file.separator", "/"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteFileManager#getResource(java.lang.String)
	 */
	@Override
	public IFileStore getResource(String path) {
		return EFS.getLocalFileSystem().getStore(new Path(path));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteFileManager#toPath(java.net.URI)
	 */
	@Override
	public String toPath(URI uri) {
		return URIUtil.toPath(uri).toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteFileManager#toURI(org.eclipse.core.runtime.IPath)
	 */
	@Override
	public URI toURI(IPath path) {
		return URIUtil.toURI(path);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteFileManager#toURI(java.lang.String)
	 */
	@Override
	public URI toURI(String path) {
		return URIUtil.toURI(path);
	}
}
