/*******************************************************************************
 * Copyright (c) 2010, 2011 IBM Corporation and others.
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

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.URIUtil;

/**
 * Abstract class providing the basis for supplementary support classes that can extract meaningful
 * information from and provide useful operations on EFS file-systems. This allows for operations that can
 * operate on virtual EFS file-systems (where IFileStores are just links to other IFileStores), or that operate
 * on the physical file backed by an IFileStore, without having to know the implementation details of a given
 * EFS file-system.
 *
 * Provides a default implementation that assumes that URIs for the given file-system map directly to resources
 * in the physical file-system, and that the path component of the URI is a direct representation of the
 * absolute path to the file in the physical file-system.
 *
 * Clients wishing to support a file-system with different behavior should extend this class and override its
 * methods where appropriate.
 *
 * Clients should not typically call methods on this class or its descendants directly. Instead, they should
 * call the appropriate method in FileSystemUtilityManager so that said manager can properly route calls to
 * the proper utility, depending on the file-system.
 *
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added to CDT 7.0 as part of a work in progress.
 * There is no guarantee that this API will work or that it will remain the same. Please do not use this API without
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
		String path = locationURI.getPath();
		String schema = locationURI.getScheme();
		if (schema != null && schema.equals(EFS.SCHEME_FILE) && Platform.getOS().equals(Platform.WS_WIN32)) {
			// URI path on Windows is represented as "/C:/path"
			if (path != null && path.matches("/[A-Za-z]:.*")) { //$NON-NLS-1$
				path = path.substring(1);
			}
		}
		return path;
	}

	/**
	 * In the case of a virtual file-system, where URIs in the given file-system are just soft links in EFS to
	 * URIs in other file-systems, returns the URI that this URI links to. If the file-system is not virtual,
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
	 * It also determines whether or not to convert backslashes in the provided path based on whether or not the
	 * local operating system's file separator is a backslash, thus ensuring proper behaviour for URIs corresponding
	 * to the local file-system.
	 *
	 * @param locationOnSameFilesystem
	 * @param path An absolute path.
	 * @return URI
	 */
	public URI createNewURIFromPath(URI locationOnSameFilesystem, String path) {
		URI uri = locationOnSameFilesystem;

		Path p = new Path(path);
		String pathString = p.toString(); // to convert any backslashes to slashes if we are on Windows
		final int length = pathString.length();
		StringBuffer pathBuf = new StringBuffer(length + 1);

		// force the path to be absolute including Windows where URI path is represented as "/C:/path"
		if (length > 0 && (pathString.charAt(0) != '/')) {
			pathBuf.append('/');
		}
		//additional double-slash for UNC paths to distinguish from host separator
		if (pathString.startsWith("//")) //$NON-NLS-1$
			pathBuf.append('/').append('/');
		pathBuf.append(pathString);

		try {
			//Bug 326957 - EFSExtensionProvider does not handle URI's correctly
			return new URI(uri.getScheme(), uri.getAuthority(), pathBuf.toString(), // replaced!
					uri.getQuery(), uri.getFragment());
		} catch (URISyntaxException e) {
			CCorePlugin.log(e);
		}
		return null;
	}

	/**
	 * For file-systems that map the path to a physical file in one file-system (say on a remote machine) to
	 * another path (say, on the local machine), this method returns the path that the store maps to. I.e., it
	 * returns the path that the path returned by getPathFromURI(URI locationURI) maps to. If there is no such
	 * mapping, then an identity mapping of the paths is assumed.
	 *
	 * Typically if a file-system maps one file-system to another, it will place the mapped path in the path
	 * field of its URIs (which the default implementation assumes), but this is not guaranteed to be so for
	 * all file-system implementations.
	 *
	 * @return String representing the path, or <code>null</code> on error.
	 */
	public String getMappedPath(URI locationURI) {
		return getPathFromURI(locationURI);
	}

	/**
	 * Returns true if the given URI is part of a virtual file-system and thus points to another underlying
	 * URI. Returns false otherwise. By default, file-systems are assumed to be non-virtual.
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
