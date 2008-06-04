/********************************************************************************
 * Copyright (c) 2008 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 *
 * Contributors:
 * David McKnight   (IBM)        - [209593] [api] add support for "file permissions" and "owner" properties for unix files
 ********************************************************************************/
package org.eclipse.rse.services.files;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.rse.services.clientserver.messages.SystemMessageException;

/**
 * Service used to get and set the permissions of a file. The
 * {@link #getCapabilities(IHostFile)} method must be implemented to tell
 * clients what kinds of permission services are actually available on a
 * concrete implementation.
 * 
 * @since 3.0
 */
public interface IFilePermissionsService {

    public static int FS_CAN_GET_OWNER       = 1 << 0;
    public static int FS_CAN_GET_GROUP       = 1 << 1;
    public static int FS_CAN_GET_PERMISSIONS = 1 << 2;
    public static int FS_CAN_SET_OWNER       = 1 << 3;
    public static int FS_CAN_SET_GROUP       = 1 << 4;
    public static int FS_CAN_SET_PERMISSIONS = 1 << 5;

	public static final int FS_CAN_GET_ALL = FS_CAN_GET_OWNER | FS_CAN_GET_GROUP | FS_CAN_GET_PERMISSIONS;
	public static final int FS_CAN_SET_ALL = FS_CAN_SET_OWNER | FS_CAN_SET_GROUP | FS_CAN_SET_PERMISSIONS;


	/**
	 * Gets the permissions for a file including the user and group owner
	 *
	 * @param file the remote file
	 * @param monitor the monitor for this potentially long running operation
	 * @return the host file permissions
	 * @throws SystemMessageException if an error occurs.
	 * Typically this would be one of those in the RemoteFileException family.
	 */
	public IHostFilePermissions getFilePermissions(IHostFile file, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Sets the permissions for a file including the user and group owner as specified in the permissions
	 *
	 * @param file the remote file
	 * @param permissions the new permissions for this file
	 * @param monitor the monitor for this potentially long running operation
	 * @throws SystemMessageException if an error occurs.
	 * Typically this would be one of those in the RemoteFileException family.
	 */
	public void setFilePermissions(IHostFile file, IHostFilePermissions permissions, IProgressMonitor monitor) throws SystemMessageException;


	/**
	 * Returns the capabilities of this file permissions service for the corresponding file.  If
	 * null is specified, this returns the general capabilities of this service.
	 *
	 * @param file the remote file
	 * @return the capabilities of this service against this file
	 */
	public int getCapabilities(IHostFile file);

}
