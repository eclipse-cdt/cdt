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

package org.eclipse.rse.subsystems.files.core.subsystems;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileFilterString;

/**
 * This interface represents a place to hold contextual information stored within
 *  each remote file object.
 * <p>
 * We store in here information that might be the same for multiple remote file objects,
 *  in order to save memory. This way, we can merely hold a reference to a single object,
 *  which itself may have numerous pieces of information helpful when processing actions
 *  on a particular remote object.
 */ 
public interface IRemoteFileContext 
{
	
	/**
	 * Get parent subsystem
	 */
	public IRemoteFileSubSystem getParentRemoteFileSubSystem();
    /**
     * Return the parent remote file object expanded to get this object, or null if no such parent
     */
    public IRemoteFile getParentRemoteFile();
    /**
     * Return the filter string used to resolve the list resulting in this remote object.
     */
    public RemoteFileFilterString getFilterString();
    /**
     * Return all the filter string objects applicable to this folder object.
     */
    public RemoteFileFilterString[] getAllFilterStrings();
        
	/**
	 * Set parent subsystem
	 */
	public void setParentRemoteFileSubSystem(IRemoteFileSubSystem parentSubSystem);
    /**
     * Set the parent remote file object expanded to get this object, or null if no such parent
     */
    public void setParentRemoteFile(IRemoteFile parentFile);
    /**
     * Set the filter string used to resolve the list resulting in this remote object.
     */
    public void setFilterString(RemoteFileFilterString filterString);
    /**
     * Add an additional filter string. This is called for folders in a multi-filter string
     *  filter. It is called for each filter string that lists the contents of the same parent
     *  path, when show subdirs is true. It is needed to support the subsequent expansion of 
     *  this subdirectory, so that it can filter on all the appropriate filter strings.
     */
    public void addFilterString(RemoteFileFilterString additionalFilterString);
}