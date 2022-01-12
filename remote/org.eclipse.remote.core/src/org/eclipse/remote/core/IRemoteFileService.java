/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.core;

import java.net.URI;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;

/**
 * Interface for managing files on a remote system.
 *
 * @since 2.0
 */
public interface IRemoteFileService extends IRemoteConnection.Service {

	/**
	 * Get the resource associated with path. IFileStore can then be used to
	 * perform operations on the file.
	 *
	 * The remote connection does not need to be open to use this method, but
	 * subsequent operations on the IFileStore that access the underlying remote
	 * filesystem may require the connection to be open.
	 *
	 * @param path
	 *            path to resource
	 * @return the file store representing the remote path
	 */
	IFileStore getResource(String path);

	/**
	 * Get the base directory to be used for relative paths.
	 *
	 * @return base directory
	 */
	String getBaseDirectory();

	/**
	 * Set the base directory to be used for relative paths..
	 *
	 * @param path new base directory
	 */
	void setBaseDirectory(String path);

	/**
	 * Gets the directory separator on the target system.
	 *
	 * @return String
	 */
	String getDirectorySeparator();

	/**
	 * Convert URI to a remote path. This path is suitable for direct file
	 * operations <i>on the remote system</i>.
	 *
	 * The remote connection does not need to be open to use this method.
	 *
	 * @return IPath representing the remote path
	 */
	String toPath(URI uri);

	/**
	 * Convert remote path to equivalent URI. This URI is suitable for EFS
	 * operations <i>on the local system</i>.
	 *
	 * The remote connection does not need to be open to use this method.
	 *
	 * @param path
	 *            path on remote system
	 * @return URI representing path on remote system, or null if the path is
	 *         invalid
	 */
	URI toURI(IPath path);

	/**
	 * Convert string representation of a remote path to equivalent URI. This
	 * URI is suitable for EFS operations <i>on the local system</i>.
	 *
	 * The remote connection does not need to be open to use this method.
	 *
	 * @param path
	 *            path on remote system
	 * @return URI representing path on remote system, or null if the path is
	 *         invalid
	 */
	URI toURI(String path);

}
