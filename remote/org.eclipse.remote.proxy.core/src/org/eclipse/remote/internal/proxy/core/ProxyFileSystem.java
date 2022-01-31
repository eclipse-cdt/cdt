/********************************************************************************
 * Copyright (c) 2016 Oak Ridge National Laboratory and others.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/legal/epl-2.0/
 ********************************************************************************/

package org.eclipse.remote.internal.proxy.core;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.provider.FileSystem;
import org.eclipse.core.runtime.IPath;

public class ProxyFileSystem extends FileSystem {
	/**
	 * Return the connection name encoded in the URI.
	 *
	 * @param uri
	 *            URI specifying a remote tools connection
	 * @return name of the connection or null if the URI is invalid
	 * @since 4.0
	 */
	public static String getConnectionNameFor(URI uri) {
		return uri.getAuthority();
	}

	/**
	 * Return an URI uniquely naming a remote tools remote resource.
	 *
	 * @param connectionName
	 *            remote tools connection name
	 * @param path
	 *            absolute path to resource as valid on the remote system
	 * @return an URI uniquely naming the remote resource.
	 */
	public static URI getURIFor(String connectionName, String path) {
		try {
			return new URI("proxy", connectionName, path, null, null); //$NON-NLS-1$
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Default constructor.
	 */
	public ProxyFileSystem() {
		super();
	}

	@Override
	public int attributes() {
		// Attributes supported by JSch IFileService
		return EFS.ATTRIBUTE_READ_ONLY | EFS.ATTRIBUTE_EXECUTABLE | EFS.ATTRIBUTE_SYMLINK | EFS.ATTRIBUTE_LINK_TARGET;
	}

	@Override
	public boolean canDelete() {
		return true;
	}

	@Override
	public boolean canWrite() {
		return true;
	}

	@Override
	public IFileStore getStore(IPath path) {
		return EFS.getNullFileSystem().getStore(path);
	}

	@Override
	public IFileStore getStore(URI uri) {
		try {
			return ProxyFileStore.getInstance(uri);
		} catch (Exception e) {
			// Could be an URI format exception
			Activator.log(e);
			return EFS.getNullFileSystem().getStore(uri);
		}
	}
}