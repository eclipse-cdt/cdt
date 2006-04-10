/********************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.subsystems.processes.core.subsystem;

import org.eclipse.rse.services.clientserver.processes.IHostProcessFilter;


/**
 * This interface represents a place to hold contextual information stored within
 *  each remote process object.
 * <p>
 * We store in here information that might be the same for multiple remote process objects,
 *  in order to save memory. This way, we can merely hold a reference to a single object,
 *  which itself may have numerous pieces of information helpful when processing actions
 *  on a particular remote object.
 */ 
public interface IRemoteProcessContext
{
	
	/**
	 * Get parent subsystem
	 */
	public RemoteProcessSubSystem getParentRemoteProcessSubSystem();
	
    /**
     * Return the parent remote process object expanded to get this object, or null if no such parent
     */
    public IRemoteProcess getParentRemoteProcess();
    
    /**
     * Return the filter string used to resolve the list resulting in this remote object.
     */
    public IHostProcessFilter getFilterString();
    
    /**
     * Return all the filter string objects applicable to this folder object.
     */
    public IHostProcessFilter[] getAllFilterStrings();
        
	/**
	 * Set parent subsystem
	 */
	public void setParentRemoteProcessSubSystem(RemoteProcessSubSystem parentSubSystem);
	
    /**
     * Set the parent remote process object expanded to get this object, or null if no such parent
     */
    public void setParentRemoteProcess(IRemoteProcess parentProcess);
    
    /**
     * Set the filter string used to resolve the list resulting in this remote object.
     */
    public void setFilterString(IHostProcessFilter filterString);
    
    /**
     * Add an additional filter string. This is called for processes in a multi-filter string
     *  filter. It is called for each filter string that lists the contents of the same parent
     *  path, when show subprocesses is true. It is needed to support the subsequent expansion of 
     *  this branch of the process tree, so that it can filter on all the appropriate filter strings.
     */
    public void addFilterString(IHostProcessFilter additionalFilterString);
}