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
package org.eclipse.remote.core;

import java.net.URI;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;

/**
 * Interface for managing resources on a remote system.
 */
public interface IRemoteFileManager {
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
	public IFileStore getResource(String path);

	/**
	 * Gets the directory separator on the target system.
	 * 
	 * @return String
	 * @since 4.0
	 */
	public String getDirectorySeparator();

	/**
	 * Convert URI to a remote path. This path is suitable for direct file
	 * operations <i>on the remote system</i>.
	 * 
	 * The remote connection does not need to be open to use this method.
	 * 
	 * @return IPath representing the remote path
	 */
	public String toPath(URI uri);

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
	public URI toURI(IPath path);

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
	public URI toURI(String path);
}
