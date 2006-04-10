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

package org.eclipse.rse.services.clientserver.processes;

/**
 * 
 * @author mjberger
 * 
 * This interface represents a simple abstract process on a host
 * system.
 */
public interface IHostProcess 
{
	

	/**
     * Get the process id (pid) associated with this process.
     */
    public long getPid();
    
    /**
     * Get the parent process id (ppid) associated with this process.
     */
    public long getPPid();
    
    /**
     * Get the name of the executable owning this process
     */
    public String getName();
    
    /**
     * Get the state of the process
     */
    public String getState();
    
    /**
     * Get the Tgid
     */
    public long getTgid();
    
    /**
     * Get the TracerPid
     */
    public long getTracerPid();
    
    /**
     * Get the process owner's user id (uid)
     */
    public long getUid();
    
    /**
     * Get the process owner's username
     */
    public String getUsername();
    
    /**
     * Get the process owner's group id (gid)
     */
    public long getGid();
    
    /**
     * Returns whether this is the root process or not
     */
    public boolean isRoot();
    
    /**
     * Returns the virtual memory size of this process (in kB)
     */
    public long getVmSizeInKB();
    
    /**
     * Returns the virtual memory resident set size of this process (in kB).
     * This is the actual amount of RAM used by the process.
     */
    public long getVmRSSInKB();
    
    /**
     * Get the display name for this process.
     */
    public String getLabel();
    
    /**
     * Returns all properties of this process in the same format as given in the
     * dataelement
     */
    public String getAllProperties();
}