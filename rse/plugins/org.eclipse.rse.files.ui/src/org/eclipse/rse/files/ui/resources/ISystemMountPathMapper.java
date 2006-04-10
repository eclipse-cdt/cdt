/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.files.ui.resources;

/**
 * This interface is used to provide a common way of mapping mounted resources to the temp files project.
 * Since local mounts can change or be removed (i.e. disconnected) this provides a way for a vender to
 * remap a particular resource if the vender software is able to determine the new mount location.
 * Implementors of this interface should register their mappers via the mountPathMapper extension point.
 */
public interface ISystemMountPathMapper
{

	/**
	 * Indicates whether this mapper handles remapping of the specified resource
	 * @param hostname the remote host
	 * @param remotePath the remote path as seen by the local file subsystem
	 * @return whether this mapper handles remapping of the specified remote resource
	 */
	public boolean handlesMappingFor(String hostname, String remotePath);
	
	/**
	 * Returns the qualified workspace path for a replica of this mounted file.  Since the local
	 * system path is not unique for mounted files, this allows a vender to make sure it is unique.
	 * The workspace mapping should always be the remote path on the originating host.
	 * 
	 * @param hostname the remote host
	 * @param remotePath the remote path as seen by the local file subsystem
     * @return the corresponding workspace replica mapping
	 */
	public String getWorkspaceMappingFor(String hostname, String remotePath);
	
	
	/**
	 * Returns the corresponding hostname for the specified path on the specified host.  If a file is mounted
	 * via local, then the actual host containing the file will not be local.  If there is no mapping, then
	 * the hostname will be the same as the argument.
	 * @param hostname the system host
	 * @param remotePath the path on the system host
	 * @return the actual host that contains the specified remote path
	 */
	public String getActualHostFor(String hostname, String remotePath);
	
	/**
	 * Returns the local system path that can be used for copying the replica back to remote. When null
	 * is returned RSE the file is treated as no longer available and thus remote uploads do not occur.  Venders
	 * who would like to disable uploads for some period can implement this to return null during that period.
	 * 
	 * @param hostname the remote host
	 * @param remotePath the remote path as seen by the local file subsystem
	 * @return the local system path that represents the mounted file
	 */
	public String getMountedMappingFor(String hostname, String remotePath);
}