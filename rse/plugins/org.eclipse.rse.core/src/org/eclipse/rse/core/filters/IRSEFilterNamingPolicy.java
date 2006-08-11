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

package org.eclipse.rse.core.filters;
/**
 * Allows tool writers to specify the naming standards for the
 * persistence files and folders involved with filters.
 * <p>
 * Note that not all methods will be used for all saving policies.
 * <p>
 * @see org.eclipse.rse.internal.filters.SystemFilterNamingPolicy#getNamingPolicy()
 */
public interface IRSEFilterNamingPolicy
{
    /**
     * Get the unqualified save file name for the given SystemFilterPoolManager object name.
     * Do NOT include the extension, as .xmi will be added.
     */
    public String getManagerSaveFileName(String managerName);
    /**
     * Get the unqualified save file name for the given SystemFilterPoolReferenceManager object name.
     * Do NOT include the extension, as .xmi will be added.
     */
    public String getReferenceManagerSaveFileName(String managerName);    
    /**
     * Get the unqualified save file name for the given SystemFilterPool object name.
     * Do NOT include the extension, as .xmi will be added.
     */
    public String getFilterPoolSaveFileName(String poolName);    
    /**
     * Get the file name prefix for all pool files. 
     * Used to deduce the saved pools by examining the file system
     */
    public String getFilterPoolSaveFileNamePrefix();
    /**
     * Get the folder name for the given SystemFilterPool object name.
     */
    public String getFilterPoolFolderName(String poolName);        
    /**
     * Get the folder name prefix for all pool folders. 
     * Used to deduce the saved pools by examining the file system
     */
    public String getFilterPoolFolderNamePrefix();
    /**
     * Get the unqualified save file name for the given SystemFilter object name
     * Do NOT include the extension, as .xmi will be added.
     */
    public String getFilterSaveFileName(String filterName);        
    /**
     * Get the file name prefix for all filter files. 
     * Used to deduce the saved pools by examining the file system
     */
    public String getFilterSaveFileNamePrefix();
    
} 