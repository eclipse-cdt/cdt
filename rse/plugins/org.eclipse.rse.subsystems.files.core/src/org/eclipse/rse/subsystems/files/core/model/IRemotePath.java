/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.subsystems.files.core.model;

import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;

/**
 * This interface represents a remote path.
 */
public interface IRemotePath {
	
	/**
	 * Returns the profile name.
	 * @return the profile name.
	 */
	public String getProfileName();
	
	/**
	 * Returns the connection name.
	 * @return the connection name.
	 */
	public String getConnectionName();
	
	/**
	 * Returns the absolute path.
	 * @return the absolute path.
	 */
	public String getAbsolutePath();
	
	/**
	 * Returns the fully qualified path. The fully qualified path is the absolute path on the remote machine,
	 * prefixed by profile name and connection name. It is of the form "profileName.connectionName:absolutePath".
	 * If the profile name, connection name or absolute path is <code>null</code>, then the fully qualified path 
	 * will also be <code>null</code>.
	 * @return the fully qualified path.
	 */
	public String getFullyQualifiedPath();
	
	/**
	 * Returns the file extension for the path. The file extension portion is
	 * defined as the string following the last period (".") character in the path.
	 * @return the extension, or <code>null</code> if none.
	 */
	public String getFileExtension();
	
	/**
	 * Returns a new path with the file extension added to this path. The file extension portion is
	 * defined as the string following the last period (".") character in the path. If this path ends
	 * with a separator, i.e. '/' or '\\', then this path is returned.
	 * The given extension should not include a leading ".".
	 * @param extension the file extension to append to the path.
	 * @return the new path.
	 */
	public IRemotePath addFileExtension(String extension);
	
	/**
	 * Returns a new path with the file extension removed from this path. The file extension portion is
	 * defined as the string following the last period (".") character in the path. If this path ends
	 * with a separator, i.e. '/' or '\\', or if it does not have an extension, then this path is returned.
	 * The given extension should not include a leading ".".
	 * @param the new path.
	 */
	public IRemotePath removeFileExtension();
	
	/**
	 * Returns whether this path represents a virtual file.
	 * @return <code>true</code> if the path represents a virtual file, <code>false</code> otherwise.
	 */
	public boolean isVirtual();
	
	/**
	 * Returns the remote file represented by the remote path.
	 * @return the remote file.
	 */
	public IRemoteFile toRemoteFile();
}