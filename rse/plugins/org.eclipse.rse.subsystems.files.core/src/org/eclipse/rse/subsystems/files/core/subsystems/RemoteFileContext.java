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
import java.util.Vector;

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
public class RemoteFileContext implements IRemoteFileContext
{

    protected IRemoteFileSubSystem subsystem;
    protected IRemoteFile parentFile;
    protected RemoteFileFilterString filterString;
    protected Vector                 allFilterStrings;
        
    /**
     * Constructor that takes all inputs.
     */ 
    public RemoteFileContext(IRemoteFileSubSystem subsystem, IRemoteFile parentFileObject, 
                             RemoteFileFilterString filterString)
    {
    	this.subsystem = subsystem;
    	this.parentFile = parentFileObject;
    	this.filterString = filterString;
    }
    /**
     * Constructor that takes no inputs.
     */ 
    public RemoteFileContext()
    {
    }

    // ------------------------------------------------------------------------
    // SETTER METHODS...
    // ------------------------------------------------------------------------
    /**
     * Set the parent subsystem
     */
    public void setParentRemoteFileSubSystem(IRemoteFileSubSystem subsystem)
    {
    	this.subsystem = subsystem;
    }
    /**
     * Set the parent remote file object expanded to get this object
     */
    public void setParentRemoteFile(IRemoteFile parentFileObject)
    {
    	this.parentFile = parentFileObject;
    }
    /**
     * Set the filter string object resolved to get this object
     */
    public void setFilterString(RemoteFileFilterString filterString)
    {
    	this.filterString = filterString;
    	allFilterStrings = null;
    }    
    /**
     * Add an additional filter string. This is called for folders in a multi-filter string
     *  filter. It is called for each filter string that lists the contents of the same parent
     *  path, when show subdirs is true. It is needed to support the subsequent expansion of 
     *  this subdirectory, so that it can filter on all the appropriate filter strings.
     */
    public void addFilterString(RemoteFileFilterString additionalFilterString)
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
    		RemoteFileFilterString prevfs = (RemoteFileFilterString)allFilterStrings.elementAt(idx);
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
    public IRemoteFileSubSystem getParentRemoteFileSubSystem()
    {
    	return subsystem;
    }
    /**
     * Return the parent remote file object expanded to get this object, or null if no such parent
     */
    public IRemoteFile getParentRemoteFile()
    {
    	return parentFile;
    }    
    /**
     * Return the filter string object resolved to get this object
     */
    public RemoteFileFilterString getFilterString()
    {
    	return filterString;
    }
    /**
     * Return all the filter string objects applicable to this folder object.
     */
    public RemoteFileFilterString[] getAllFilterStrings()
    {
    	if (allFilterStrings == null)
    	  return new RemoteFileFilterString[] {filterString};
    	else
    	{
    		RemoteFileFilterString[] all = new RemoteFileFilterString[allFilterStrings.size()];
    		for (int idx=0; idx<all.length; idx++)
    		  all[idx] = (RemoteFileFilterString)allFilterStrings.elementAt(idx);
    		return all;
    	}
    }
      
}