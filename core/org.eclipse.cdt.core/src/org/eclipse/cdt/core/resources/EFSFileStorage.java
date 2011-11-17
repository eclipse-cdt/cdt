/*******************************************************************************
 *  Copyright (c) 2008, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.resources;

import java.io.InputStream;
import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.PlatformObject;

/**
 * A storage object which is backed by an EFS resource.
 *
 * @author crecoskie
 * @since 5.0
 *
 */
public class EFSFileStorage extends PlatformObject implements IStorage {

	private URI locationURI;
	private InputStream inputStream;

	public EFSFileStorage(URI locationURI) {
		this.locationURI = locationURI;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IStorage#getContents()
	 */
	@Override
	public InputStream getContents() throws CoreException {
		if (inputStream == null) {

			IFileStore fileStore = EFS.getStore(locationURI);

			if (fileStore != null) {
				inputStream = fileStore.openInputStream(EFS.NONE,
						new NullProgressMonitor());
			}
		}

		return inputStream;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IStorage#getFullPath()
	 */
	@Override
	public IPath getFullPath() {
		return URIUtil.toPath(locationURI);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IStorage#getName()
	 */
	@Override
	public String getName() {
		IFileStore fileStore = null;
		try {
			fileStore = EFS.getStore(locationURI);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		if (fileStore != null) {
			return fileStore.getName();
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IStorage#isReadOnly()
	 */
	@Override
	public boolean isReadOnly() {
		IFileStore fileStore = null;
		try {
			fileStore = EFS.getStore(locationURI);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (fileStore != null) {
			IFileInfo info = fileStore.fetchInfo();

			if(info != null)
				return info.getAttribute(EFS.ATTRIBUTE_READ_ONLY);
		}

		return false;
	}

	/**
	 * Returns the location URI corresponding to the EFS resource that
	 * backs this storage.
	 *
	 * @return URI
	 */
	public URI getLocationURI() {
		return locationURI;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof EFSFileStorage && locationURI != null) {
			return locationURI.equals(((EFSFileStorage) obj).getLocationURI());
		}
		return false;
	}
}
