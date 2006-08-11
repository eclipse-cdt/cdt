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

package org.eclipse.rse.core.internal.subsystems;

import org.eclipse.rse.core.filters.IRSEFilterNamingPolicy;
import org.eclipse.rse.internal.filters.SystemFilterNamingPolicy;

/**
 * A filter naming policy is responsible for returning the save file names for
 *  a given filter framework object.
 * This policy implements our naming policy for the filter files in the
 *  remote systems project.
 */
public class SubSystemFilterNamingPolicy
	extends SystemFilterNamingPolicy
	implements IRSEFilterNamingPolicy 
{
	
	/**
	 * Constructor for SubSystemFilterNamingPolicy
	 */
	public SubSystemFilterNamingPolicy() 
	{
		super();
	}

    /**
     * Get the unqualified save file name for the given SystemFilterPoolManager object.
     * Do NOT include the extension, as .xmi will be added.
     * <p>
     * Returns "filterPools_"+managerName by default.
     */
    public String getManagerSaveFileName(String managerName)
    {
    	return super.getManagerSaveFileName(managerName);
    }
    /**
     * Get the unqualified save file name for the given SystemFilterPoolReferenceManager object.
     * Do NOT include the extension, as .xmi will be added.
     * <p>
     * Returns "filterPoolRefs_"+managerName by default.
     */
    public String getReferenceManagerSaveFileName(String managerName)
    {
        return super.getReferenceManagerSaveFileName(managerName);
    }
    /**
     * Get the unqualified save file name for the given SystemFilterPool object.
     * Do NOT include the extension, as .xmi will be added.
     * <p>
     * Returns getFilterPoolSaveFileNamePrefix()+poolName by default.
     */
    public String getFilterPoolSaveFileName(String poolName)
    {
        return super.getFilterPoolSaveFileName(poolName);
    }
    /**
     * Get the file name prefix for all pool files. 
     * Used to deduce the saved pools by examining the file system
     * <p>
     * By default returns "filterPool_"
     */
    public String getFilterPoolSaveFileNamePrefix()
    {
        return super.getFilterPoolSaveFileNamePrefix();
    }
    /**
     * Get the folder name for the given SystemFilterPool object.
     * <p>
     * Returns getFilterPoolFolderNamePrefix()+poolName by default.
     */
    public String getFilterPoolFolderName(String poolName)
    {
        return super.getFilterPoolFolderName(poolName);
    }
    /**
     * Get the folder name prefix for all pool folders. 
     * Used to deduce the saved pools by examining the file system
     * <p>
     * By default returns "FilterPool_"
     */
    public String getFilterPoolFolderNamePrefix()
    {
        return super.getFilterPoolFolderNamePrefix();
    }    
    /**
     * Get the unqualified save file name for the given SystemFilter object.
     * Do NOT include the extension, as .xmi will be added.
     * <p>
     * Returns getFilterSaveFileNamePrefix()+filterName by default.
     */
    public String getFilterSaveFileName(String filterName)
    {
        return super.getFilterSaveFileName(filterName);
    }
    /**
     * Get the file name prefix for all filter files. 
     * Used to deduce the saved filters by examining the file system
     * <p>
     * Returns "Filter_" by default.
     */
    public String getFilterSaveFileNamePrefix()
    {
        return super.getFilterSaveFileNamePrefix();        
    }    	

}