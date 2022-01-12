/*******************************************************************************
 * Copyright (c) 2011, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Greg Watson (IBM Corporation) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils;

import java.io.File;
import java.net.URI;

import org.eclipse.cdt.internal.core.UNCPathConverterImpl;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Base class for the UNC path conversion extension point. UNC paths are used to represent remote
 * include locations, and this class is used to translate between UNC, IPath and URI
 * representations. By default, paths are translated into the equivalent local file version to
 * preserve existing behavior, but by providing an appropriate extension, these paths can be mapped
 * into locations on a remote system.
 *
 * May be subclassed by clients.
 * @since 5.3
 */
public abstract class UNCPathConverter {
	/**
	 * Get the instance of the class that combines the registered converters.
	 * @return instance of UNCPathConverter
	 */
	public static UNCPathConverter getInstance() {
		return UNCPathConverterImpl.getInstance();
	}

	/**
	 * Test if the string path is in UNC format.
	 *
	 * @param path
	 *            path to test
	 * @return true if the path is in UNC format, false otherwise
	 */
	public static boolean isUNC(String path) {
		if (path.length() >= 2) {
			char c = path.charAt(0);
			if (c == IPath.SEPARATOR || c == File.separatorChar) {
				c = path.charAt(1);
				return c == IPath.SEPARATOR || c == File.separatorChar;
			}
		}
		return false;
	}

	/**
	 * Convert a URI to an IPath.
	 * Resolves to local path if possible, including using EFS where required.
	 *
	 * @param uri
	 *            URI to convert to an IPath
	 * @return IPath representation of the URI
	 */
	public static IPath toPath(URI uri) {
		IPath localPath = URIUtil.toPath(uri);
		if (localPath != null) {
			return localPath;
		}
		// see if the uri has an host part
		String host = uri.getHost();
		if (host == null) {
			// see if the uri has a authority part
			host = uri.getAuthority();
			if (host == null) {
				return localPath;
			}
		}
		return new Path(host).makeUNC(true).append(uri.getPath());
	}

	/**
	 * Convert an IPath to a URI.
	 *
	 * @param path
	 *            path to convert
	 * @return URI representation of the IPath
	 */
	public abstract URI toURI(IPath path);

	/**
	 * Convert a string path to a URI
	 *
	 * @param path
	 *            path to convert
	 * @return URI representation of the path
	 */
	public abstract URI toURI(String path);
}
