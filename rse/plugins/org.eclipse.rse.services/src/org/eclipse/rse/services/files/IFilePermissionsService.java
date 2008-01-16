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
 * Service used to get and set the permissions of a file.
 */
public interface IFilePermissionsService {

	/**
	 * @param remoteParent
	 * @param name
	 * @param monitor the monitor for this potentially long running operation
	 * @return the host file permissions
	 * @throws SystemMessageException if an error occurs. 
	 * Typically this would be one of those in the RemoteFileException family.
	 */
	public IHostFilePermissions getFilePermissions(String remoteParent, String name, IProgressMonitor monitor) throws SystemMessageException;
	
	/**
	 * @param remoteParent
	 * @param name
	 * @param permissions the new permissions for this file
	 * @param monitor the monitor for this potentially long running operation
	 * @throws SystemMessageException if an error occurs. 
	 * Typically this would be one of those in the RemoteFileException family.
	 */
	public void setFilePermissions(String remoteParent, String name, IHostFilePermissions permissions, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Indicates whether the file permissions can be retrieved for the specified file.  
	 * In some cases the service will need to determine whether it supports permissions 
	 * depending on the current server.
	 * 
	 * @param remoteParent
	 * @param name  
	 * @return whether the file permissions can be retrieved
	 */
	public boolean canGetFilePermissions(String remoteParent, String name);
	
	/**
	 * Indicates whether the file permissions can be set for the specified file
	 * 
	 * @param remoteParent
	 * @param name  
	 * @return whether the file permissions can be set
	 */
	public boolean canSetFilePermissions(String remoteParent, String name);
}
