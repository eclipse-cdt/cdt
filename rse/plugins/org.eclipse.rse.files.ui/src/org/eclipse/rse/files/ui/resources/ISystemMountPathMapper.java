/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * David McKnight   (IBM) - [195285] mount path mapper changes
 *******************************************************************************/

package org.eclipse.rse.files.ui.resources;

import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;

/**
 * This interface is used to provide a common way of mapping mounted resources to the temp files project.
 * Since local (or remote) mounts can change or be removed (i.e. disconnected) this provides a way for a vendor to
 * remap a particular resource if the vendor software is able to determine the new mount location.
 * 
 * There are a number of use cases where a customized workspace mapping would be desirable:
 * <ul>
 *   <li>Two connections to the same host using different user IDs for each connection.  In that case, it may make
 *   sense to store the temp files differently for each connection (for example, qualified by user ID).
 *   <li>If port-forwarding is used then a port could be used to qualify the temp file path.
 *   <li>If the remote path contains invalid characters for the local file system, then the temp file mapping could 
 *   be made such that invalid characters ( e.g. :<>?* on Windows) translate to a sequence of valid characters on the client.   
 * </ul>
 * 
 * 
 * Implementors of this interface should register their mappers via the mountPathMappers extension point.
 */
public interface ISystemMountPathMapper
{

	
	/**
	 * Returns the qualified workspace path for a replica of this mounted file.  Since the
	 * system path is not unique for mounted files, this allows a vendor to make sure it is unique.
	 * The workspace mapping should always be the remote path on the originating host.
	 * 
	 * @param hostname the remote host
	 * @param remotePath the remote path as seen by the file subsystem
	 * @param subsystem the remote file subsystem.  User the subsystem to customize how the temp file is located.  If null
	 *        is specified, then the subsystem is not used in determining the mapping
     * @return the corresponding workspace replica mapping
	 * @since 3.0
	 */
	public String getWorkspaceMappingFor(String hostname, String remotePath, IRemoteFileSubSystem subsystem);
	
	
	
	/**
	 * Returns the corresponding hostname for the specified path on the specified host.  If a file is mounted
	 * then the actual host containing the file will not be local.  If there is no mapping, then
	 * the hostname will be the same as the argument.  If the file is remote (i.e. not locally mounted) then
	 * this will return the same as the hostname argument.
	 * 
	 * @param hostname the system host
	 * @param remotePath the path on the system host
	 * @return the actual host that contains the specified remote path
	 */
	public String getActualHostFor(String hostname, String remotePath);
	
	/**
	 * Returns the system path that can be used for copying the replica back to remote. When null
	 * is returned RSE the file is treated as no longer available and thus remote uploads do not occur.  Vendors
	 * who would like to disable uploads for some period can implement this to return null during that period.
	 * 
	 * @param hostname the remote host
	 * @param remotePath the remote path as seen by the local file subsystem
	 * @return the local system path that represents the mounted file
	 */
	public String getMountedMappingFor(String hostname, String remotePath);
	
	/**
	 * Indicates whether this mapper handles remapping of the specified resource.  If more than one mount
	 * path mapper returns true for this, then the getPriority() method will be used to determine precedence.
	 * @param hostname the remote host
	 * @param remotePath the remote path as seen by the file subsystem
	 * @param subsystem the remote file subsystem
	 * @return whether this mapper handles remapping of the specified remote resource
	 * @since 3.0
	 */
	public boolean handlesMappingFor(String hostname, String remotePath, IRemoteFileSubSystem subsystem);


	/**
	 * 
	 * Returns the priority of this mount path mapper.  This is used to determine which mount
	 * path mapper to use when more than one are applicable.  The lower the return value, the
	 * higher priority. 
	 * 
	 * @param hostname the host name for the file system
	 * @param remotePath the path on the remote file system
	 * @param subsystem the subsystem used to retrieve files
	 * 
	 * @return the priority, where the lower in value, the higher the priority.
	 * @since 3.0
	 */
	int getPriority(String hostname, String remotePath, IRemoteFileSubSystem subsystem);
}
