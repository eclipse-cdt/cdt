/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Abstract class providing the basis for utility classes that can extract meaningful information from EFS filesystems.
 * Provides a default implementation that assumes that URIs for the given filesystem map directly to resources
 * in the physical filesystem, and that the path component of the URI is a direct representation of the absolute path to
 * the file in the physical filesystem.
 * 
 * Clients wishing to support a filesystem with different behaviour should extend this class and override its methods where
 * appropriate.
 * 
 * Clients should not typically call methods on this class or its descendants directly.  Instead, they should call the approrpriate method
 * in FileSystemUtilityManager so that said manager can properly route calls to the proper utility, depending on the filesystem.
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the CDT team.
 * 
 * @noinstantiate This class is not intended to be instantiated by clients.
 * 
 * @author crecoskie
 * @since 5.2
 *
 */
public class FileSystemUtility implements IFilesystemUtility {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IFilesystemUtility#getPathFromURI(java.net.URI)
	 */
	public String getPathFromURI(URI locationURI) {
		return locationURI.getPath();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IFilesystemUtility#getBaseURI(java.net.URI)
	 */
	public URI getBaseURI(URI locationURI) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IFilesystemUtility#replacePathInURI(java.net.URI, java.lang.String)
	 */
	public URI replacePathInURI(URI locationOnSameFilesystem, String path) {
		URI uri = locationOnSameFilesystem;
		try {
			return  new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(),
			           path, // replaced! 
			           uri.getQuery(),uri.getFragment());
		} catch (URISyntaxException e) {
			CCorePlugin.log(e);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IFilesystemUtility#getMappedPath(java.net.URI)
	 */
	public String getMappedPath(URI locationURI) {
			return getPathFromURI(locationURI);
	}

}
