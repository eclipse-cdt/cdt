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

package org.eclipse.rse.internal.filters;
import org.eclipse.rse.filters.ISystemFilterConstants;
import org.eclipse.rse.filters.ISystemFilterNamingPolicy;
/**
 * A naming policy so tool writers can override defaults used when
 *  saving filter data to disk.
 * <p>
 * Subclass this and override what you wish to change.
 */
public class SystemFilterNamingPolicy implements ISystemFilterNamingPolicy, ISystemFilterConstants 
{
	
    protected String managerFileNamePrefix, poolFolderNamePrefix, poolFolderNameSuffix, 
                     poolFileNamePrefix, filterFileNamePrefix, 
                     referenceManagerFileNamePrefix;
    /**
     * Default prefix for filter pool manager persisted file: "filterPools_"
     */
    public static final String DEFAULT_FILENAME_PREFIX_FILTERPOOLMANAGER = "filterPools_";
    /**
     * Default prefix for filter pool reference manager persisted file: "filterPoolRefs_"
     */
    public static final String DEFAULT_FILENAME_PREFIX_FILTERPOOLREFERENCEMANAGER = "filterPoolRefs_";    
    /**
     * Default prefix for filter pool persisted file: "filterPool_"
     */
    public static final String DEFAULT_FILENAME_PREFIX_FILTERPOOL = "filterPool_";
    /**
     * Default prefix for filter persisted file: "filter_"
     */
    public static final String DEFAULT_FILENAME_PREFIX_FILTER = "filter_";    
    /**
     * Default prefix for filter pool folder: "FilterPool_"
     */
    public static final String DEFAULT_FOLDERNAME_PREFIX_FILTERPOOL = "FilterPool_";


    /**
     * Factory method to return an instance populated with defaults.
     * Can then simply override whatever is desired.
     */
    public static ISystemFilterNamingPolicy getNamingPolicy()
    {
    	return new SystemFilterNamingPolicy();
    }
    
	/**
	 * Constructor for SystemFilterNamingPolicyImpl
	 */
	public SystemFilterNamingPolicy() 
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
    	return DEFAULT_FILENAME_PREFIX_FILTERPOOLMANAGER+managerName;
    }
    /**
     * Get the unqualified save file name for the given SystemFilterPoolReferenceManager object.
     * Do NOT include the extension, as .xmi will be added.
     * <p>
     * Returns "filterPoolRefs_"+managerName by default.
     */
    public String getReferenceManagerSaveFileName(String managerName)
    {
        return DEFAULT_FILENAME_PREFIX_FILTERPOOLREFERENCEMANAGER+managerName;
    }
    /**
     * Get the unqualified save file name for the given SystemFilterPool object.
     * Do NOT include the extension, as .xmi will be added.
     * <p>
     * Returns getFilterPoolSaveFileNamePrefix()+poolName by default.
     */
    public String getFilterPoolSaveFileName(String poolName)
    {
        return getFilterPoolSaveFileNamePrefix()+poolName;
    }
    /**
     * Get the file name prefix for all pool files. 
     * Used to deduce the saved pools by examining the file system
     * <p>
     * By default returns "filterPool_"
     */
    public String getFilterPoolSaveFileNamePrefix()
    {
        return DEFAULT_FILENAME_PREFIX_FILTERPOOL;    	
    }
    /**
     * Get the folder name for the given SystemFilterPool object.
     * <p>
     * Returns getFilterPoolFolderNamePrefix()+poolName by default.
     */
    public String getFilterPoolFolderName(String poolName)
    {
        return getFilterPoolFolderNamePrefix()+poolName;
    }
    /**
     * Get the folder name prefix for all pool folders. 
     * Used to deduce the saved pools by examining the file system
     * <p>
     * By default returns "FilterPool_"
     */
    public String getFilterPoolFolderNamePrefix()
    {
    	return DEFAULT_FOLDERNAME_PREFIX_FILTERPOOL;
    }    
    /**
     * Get the unqualified save file name for the given SystemFilter object.
     * Do NOT include the extension, as .xmi will be added.
     * <p>
     * Returns getFilterSaveFileNamePrefix()+filterName by default.
     */
    public String getFilterSaveFileName(String filterName)
    {
        return getFilterSaveFileNamePrefix()+filterName;
    }
    /**
     * Get the file name prefix for all filter files. 
     * Used to deduce the saved filters by examining the file system
     * <p>
     * Returns "Filter_" by default.
     */
    public String getFilterSaveFileNamePrefix()
    {
        return DEFAULT_FILENAME_PREFIX_FILTER;
    }    	
}