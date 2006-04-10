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

package org.eclipse.rse.subsystems.processes.core.subsystem.impl;


import java.util.Vector;

import org.eclipse.rse.services.clientserver.processes.IHostProcessFilter;
import org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcess;
import org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcessContext;
import org.eclipse.rse.subsystems.processes.core.subsystem.RemoteProcessSubSystem;

/**
 * This class represents a place to hold contextual information stored within
 *  each remote process object.
 * <p>
 * We store in here information that might be the same for multiple remote process objects,
 *  in order to save memory. This way, we can merely hold a reference to a single object,
 *  which itself may have numerous pieces of information helpful when processing actions
 *  on a particular remote object.
 */ 
public class RemoteProcessContext implements IRemoteProcessContext
{

    protected RemoteProcessSubSystem subsystem;
    protected IRemoteProcess parentProcess;
    protected IHostProcessFilter filterString;
    protected Vector                 allFilterStrings;
    
    /**
     * Constructor that takes all inputs.
     */ 
    public RemoteProcessContext(RemoteProcessSubSystem subsystem, IRemoteProcess parentProcessObject, 
    		IHostProcessFilter filterString)
    {
    	this.subsystem = subsystem;
    	this.parentProcess = parentProcessObject;
    	this.filterString = filterString;
    }
    
    /**
     * Constructor that takes no inputs.
     */ 
    public RemoteProcessContext()
    {
    }    
    // ------------------------------------------------------------------------
    // SETTER METHODS...
    // ------------------------------------------------------------------------
    /**
     * Set the parent subsystem
     */
    public void setParentRemoteProcessSubSystem(RemoteProcessSubSystem subsystem)
    {
    	this.subsystem = subsystem;
    }
    
    /**
     * Set the parent remote process object of this process
     */
    public void setParentRemoteProcess(IRemoteProcess parentProcessObject)
    {
    	this.parentProcess = parentProcessObject;
    }
    
    /**
     * Set the filter string object resolved to get this object
     */
    public void setFilterString(IHostProcessFilter filterString)
    {
    	this.filterString = filterString;
    	allFilterStrings = null;
    }
    
    /**
     * Add an additional filter string. This is called for folders in a multi-filter string
     *  filter. It is needed to support the subsequent expansion of 
     *  this process, so that it can filter on all the appropriate filter strings.
     */
    public void addFilterString(IHostProcessFilter additionalFilterString)
    {
    	if (allFilterStrings == null)
    	{
    		allFilterStrings = new Vector();
    		if (filterString != null)
    		  allFilterStrings.addElement(filterString);    		
    	}
    	// ensure this filter string is not already in the list...
    	boolean match = false;
    	String newfs = additionalFilterString.toString();
    	for (int idx=0; !match && (idx<allFilterStrings.size()); idx++)
    	{
    		IHostProcessFilter prevfs = (IHostProcessFilter)allFilterStrings.elementAt(idx);
    		if (newfs.equals(prevfs.toString()))
    		  match = true;
    	}
    	if (!match)
    	  allFilterStrings.addElement(additionalFilterString);
    }

    // ------------------------------------------------------------------------
    // GETTER METHODS...
    // ------------------------------------------------------------------------
    
    /**
     * Return the parent subsystem
     */
    public RemoteProcessSubSystem getParentRemoteProcessSubSystem()
    {
    	return subsystem;
    }
    /**
     * Return the parent remote process object expanded to get this object, or null if no such parent
     */
    public IRemoteProcess getParentRemoteProcess()
    {
    	return parentProcess;
    }   
    
    /**
     * Return the filter string object resolved to get this object
     */
    public IHostProcessFilter getFilterString()
    {
    	return filterString;
    }
    
    /**
     * Return all the filter string objects applicable to this folder object.
     */
    public IHostProcessFilter[] getAllFilterStrings()
    {
    	if (allFilterStrings == null)
    	  return new IHostProcessFilter[] {filterString};
    	else
    	{
    		IHostProcessFilter[] all = new IHostProcessFilter[allFilterStrings.size()];
    		for (int idx=0; idx<all.length; idx++)
    		  all[idx] = (IHostProcessFilter)allFilterStrings.elementAt(idx);
    		return all;
    	}
    }
}