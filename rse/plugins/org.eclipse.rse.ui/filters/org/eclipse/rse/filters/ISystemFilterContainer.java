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

package org.eclipse.rse.filters;
//

import java.util.Vector;

import org.eclipse.rse.persistence.IRSEPersistableContainer;

/**
 * Filter containers are any objects that contain filters.
 * This includes filter pools and filters themselves.
 */
public interface ISystemFilterContainer extends IRSEPersistableContainer 
{
    /**
     * Return the filter pool manager managing this collection of filter pools and their filters.
     */
    public ISystemFilterPoolManager getSystemFilterPoolManager();
	/**
	 * @return The value of the StringsCaseSensitive attribute
	 * Are filter strings in this filter case sensitive?
	 * If not set locally, queries the parent filter pool manager's atttribute.
	 */
	public boolean areStringsCaseSensitive();    
    /**
     * Creates a new system filter within this container (SystemFilterPool or SystemFilter)
     * @param data Optional transient data you want stored in the created filter. Can be null.
     * @param aliasName The name to give the new filter. Must be unique for this pool.
     * @param filterStrings The list of String objects that represent the filter strings.
     */    
    public ISystemFilter createSystemFilter(String aliasName, Vector filterStrings);     
    /**
     * Adds given filter to the list.
     * <p>PLEASE NOTE:
     * <ul>
     *  <li> createSystemFilter calls this method for you!
     *  <li> this is a no-op if a filter with the same aliasname already exists
     * </ul>
     * @param filter SystemFilter object to add
     * @return true if added, false if filter with this aliasname already existed.
     */
    public boolean addSystemFilter(ISystemFilter filter);
    /**
     * Return Vector of String objects: the names of existing filters in this container.
     * Needed by name validators for New and Rename actions to verify new name is unique.
     */
    public Vector getSystemFilterNames();
    /**
     * Return a Vector of the filters contained in this filter container.
     */
    public Vector getSystemFiltersVector();
    /**
     * Return an array of the filters contained in this filter container.
     */
    public ISystemFilter[] getSystemFilters();   
    /**
     * Return a system filter given its name
     */
    public ISystemFilter getSystemFilter(String filterName);   
    /**
     * Return the parent pool of this container.
     * If this is itself a pool, returns "this".
     * Else, for a nested filter, returns the pool that is the ultimate parent of this filter.
     */
    public ISystemFilterPool getSystemFilterPool(); 
    /**
     * Return how many filters are defined in this filter container
     */
    public int getSystemFilterCount();
    /**
     * Removes a given filter from the list.
     * @param filter SystemFilter object to remove
     */
    public void deleteSystemFilter(ISystemFilter filter);
    /**
     * Renames a given filter in the list.
     * @param filter SystemFilter object to rename
     * @param newName New name to assign it. Assumes unique checking already done.
     */
    public void renameSystemFilter(ISystemFilter filter, String newName);
    /**
     * Return a given filter's zero-based location
     */
    public int getSystemFilterPosition(ISystemFilter filter);
    /**
     * Move a given filter to a given zero-based location
     */
    public void moveSystemFilter(int pos, ISystemFilter filter);
    /**
     * Updates a given filter in the list.
     * @param filter SystemFilter object to update
     * @param newName New name to assign it. Assumes unique checking already done.
     * @param newStrings New strings to assign it. Replaces current strings.
     */
    public void updateSystemFilter(ISystemFilter filter, String newName, String[] newStrings);
}