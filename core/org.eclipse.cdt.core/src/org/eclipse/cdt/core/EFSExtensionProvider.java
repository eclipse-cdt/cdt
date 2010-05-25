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

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.URIUtil;

/**
 * Abstract class providing the basis for supplementary support classes that can extract meaningful
 * information from and provide useful operations on EFS filesystems. This allows for operations that can
 * operate on virtual EFS filesystems (where IFileStores are just links to other IFileStores), or that operate
 * on the physical file backed by an IFileStore, without having to know the implementation details of a given
 * EFS filesystem.
 * 
 * Provides a default implementation that assumes that URIs for the given filesystem map directly to resources
 * in the physical filesystem, and that the path component of the URI is a direct representation of the
 * absolute path to the file in the physical filesystem.
 * 
 * Clients wishing to support a filesystem with different behaviour should extend this class and override its
 * methods where appropriate.
 * 
 * Clients should not typically call methods on this class or its descendants directly. Instead, they should
 * call the appropriate method in FileSystemUtilityManager so that said manager can properly route calls to
 * the proper utility, depending on the filesystem.
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in progress. There
 * is no guarantee that this API will work or that it will remain the same. Please do not use this API without
 * consulting with the CDT team.
 * 
 * @author crecoskie
 * @since 5.2
 * 
 */
public abstract class EFSExtensionProvider {

	/**
	 * If the EFS store represented by locationURI is backed by a physical file, gets the path corresponding
	 * to the underlying file as the operating system on hosting machine would see it. In the future, it would
	 * be better if EFS had an API for this.
	 * 
	 * @param locationURI
	 * @return String representing the path, or <code>null</code> if there is an error or if there is no such
	 *         physical file.
	 */
	public String getPathFromURI(URI locationURI) {
		return locationURI.getPath();
	}

	/**
	 * In the case of a virtual filesystem, where URIs in the given filesystem are just soft links in EFS to
	 * URIs in other filesystems, returns the URI that this URI links to. If the filesystem is not virtual,
	 * then this method acts as an identity mapping.
	 * 
	 * @param locationURI
	 * @return A URI corresponding to the linked store, or <code>null</code> on error.
	 */
	public URI getLinkedURI(URI locationURI) {
		return locationURI;
	}

	/**
	 * Creates a new URI which clones the contents of the original URI, but with the path replaced by the
	 * given absolute path, such that calling getPathFromURI() on the returned URI will return the given path. Returns
	 * null on error.
	 * 
	 * The default implementation places the path in the path field of the URI, ensuring that there is a leading slash.
	 * 
	 * @param locationOnSameFilesystem
	 * @param path An absolute path.
	 * @return URI
	 */
	public URI createNewURIFromPath(URI locationOnSameFilesystem, String path) {
		URI uri = locationOnSameFilesystem;
		
		Path p = new Path(path);
		String pathString = p.toString(); // to convert any backslashes to slashes
		final int length = pathString.length();
		StringBuffer pathBuf = new StringBuffer(length + 1);

		// force the path to be absolute
		if (length > 0 && (pathString.charAt(0) != '/')) {
			pathBuf.append('/');
		}
		//additional double-slash for UNC paths to distinguish from host separator
		if (pathString.startsWith("//")) //$NON-NLS-1$
			pathBuf.append('/').append('/');
		pathBuf.append(pathString);
		
		try {
			return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), pathBuf.toString(), // replaced!
					uri.getQuery(), uri.getFragment());
		} catch (URISyntaxException e) {
			CCorePlugin.log(e);
		}
		return null;
	}

	/**
	 * For filesystems that map the path to a physical file in one filesystem (say on a remote machine) to
	 * another path (say, on the local machine), this method returns the path that the store maps to. I.e., it
	 * returns the path that the path returned by getPathFromURI(URI locationURI) maps to. If there is no such
	 * mapping, then an identity mapping of the paths is assumed.
	 * 
	 * Typically if a filesystem maps one filesytem to another, it will place the mapped path in the path
	 * field of its URIs (which the default implementation assumes), but this is not guaranteed to be so for
	 * all filesystem implementations.
	 * 
	 * @return String representing the path, or <code>null</code> on error.
	 */
	public String getMappedPath(URI locationURI) {
		return getPathFromURI(locationURI);
	}

	/**
	 * Returns true if the given URI is part of a virtual filesystem and thus points to another underlying
	 * URI. Returns false otherwise. By default, filesystems are assumed to be non-virtual.
	 * 
	 * @param locationURI
	 * @return boolean
	 */
	public boolean isVirtual(URI locationURI) {
		return false;
	}

	/**
	 * Creates a new URI with the same components as the baseURI, except that calling
	 * getPathFromURI() on the new URI will return a path that has the extension appended to 
	 * the path returned by baseURI.getPathFromURI()
	 * 
	 * The default implementation assumes that the path component of the URI is used
	 * to store the path.
	 * 
	 * @param baseURI
	 * @param extension
	 * @return the new URI, or <code>null</code> on error.
	 */
	public URI append(URI baseURI, String extension) {
		return URIUtil.append(baseURI, extension);
	}

}
