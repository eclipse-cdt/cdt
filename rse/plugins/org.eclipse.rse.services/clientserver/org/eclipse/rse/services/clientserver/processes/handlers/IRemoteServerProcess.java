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

package org.eclipse.rse.services.clientserver.processes.handlers;

import org.eclipse.rse.services.clientserver.processes.IHostProcess;

/**
 * 
 * @author mjberger
 * 
 * This interface represents a simple process on the remote server.
 * It is simply a data structure for the passing of information about
 * a process from the UniversalProcessMiner to the ProcessHandler
 * that actually gets all the process information. The setters all
 * take strings as input for the sake of convenience, since that is 
 * how the process information is mined. However, the getters continue
 * to return the attributes as their real data types. NOTE THAT THE SETTERS
 * HERE DO NOT ACTUALLY MODIFY ANY PROPERTIES OF THE UNDERLYING PROCESS. IN
 * ORDER TO DO THIS YOU MUST SEND THE PROCESS A KILL SIGNAL THROUGH THE
 * SUBSYSTEM.
 * <p>
 */
public interface IRemoteServerProcess extends IHostProcess
{
    /**
     * Set the process id (pid) associated with this process.
     */
    public void setPid(String pid);
    
    /**
     * Set the parent process id (ppid) associated with this process.
     */
    public void setPPid(String ppid);
    
    /**
     * Set the name of the executable owning this process
     */
    public void setName(String name);
    
    /**
     * Set the state of the process
     */
    public void setState(String state);
    
    /**
     * Set the Tgid
     */
    public void setTgid(String tgid);
    
    /**
     * Set the TracerPid
     */
    public void setTracerPid(String tracerpid);
    
    /**
     * Set the process owner's user id (uid)
     */
    public void setUid(String uid);
    
    /**
     * Set the process owner's username
     */
    public void setUsername(String username);
    
    /**
     * Set the process owner's group id (gid)
     */
    public void setGid(String gid);
    
    /**
     * Sets all properties of this process in the same format as given in the
     * dataelement
     */
    public void setAllProperties(String allProperties);
    
    /**
     * Sets the virtual memory size of this process (in kB)
     */
    public void setVmSizeInKB(String size);
    
    /**
     * Sets the virtual memory resident set size of this process (in kB).
     * This is the actual amount of RAM used by the process.
     */
    public void setVmRSSInKB(String size);
    
    /**
     * Set the display name for this process.
     */
    public void setLabel(String label);

}