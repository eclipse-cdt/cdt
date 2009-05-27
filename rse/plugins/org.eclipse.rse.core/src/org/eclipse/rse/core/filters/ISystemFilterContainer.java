/*******************************************************************************
 * Copyright (c) 2002, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Martin Oberhuber (Wind River) - [cleanup] Add API "since" Javadoc tags
 * David Dykstal (IBM) - [226561] Add API markup to RSE Javadocs where extend / implement is allowed
 * David Dykstal (IBM) - [261486][api] add noextend to interfaces that require it
 *******************************************************************************/
package org.eclipse.rse.core.filters;

import org.eclipse.rse.core.model.IRSEPersistableContainer;

/**
 * Filter containers are any objects that contain filters.
 * This includes filter pools and filters themselves.
 * @noimplement This interface is not intended to be implemented by clients.
 * The allowable implementations are already present in the framework.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ISystemFilterContainer extends IRSEPersistableContainer {
	/**
	 * @return the filter pool manager managing this collection of filter pools and their filters.
	 */
	public ISystemFilterPoolManager getSystemFilterPoolManager();

	/**
	 * @return The value of the StringsCaseSensitive attribute Are filters in
	 *         this filter container case sensitive? If not set locally, queries
	 *         the parent filter pool manager's attribute.
	 */
	public boolean areStringsCaseSensitive();

	/**
	 * Adds a new system filter to this container (SystemFilterPool or
	 * SystemFilter) and populates it with the filter strings created from the
	 * strings provided. Does nothing if this filter already exists in this
	 * container.
	 * 
	 * @param aliasName The name to give the new filter. Must be unique for this
	 *            pool.
	 * @param filterStrings The list of String objects that represent the filter
	 *            strings.
	 * @since org.eclipse.rse.core 3.0
	 */
	public ISystemFilter createSystemFilter(String aliasName, String[] filterStrings);

	/**
	 * Adds given filter to the list without populating the filter strings.
	 * @param filter SystemFilter object to add
	 * @return true if added, false if a filter with this aliasname already existed.
	 */
	public boolean addSystemFilter(ISystemFilter filter);

	/**
	 * Get the names of existing filters in this container. Typically used by
	 * name validators for New and Rename actions to verify new name is unique.
	 * 
	 * @return String array of the names of existing filters in this container.
	 * @since org.eclipse.rse.core 3.0
	 */
	public String[] getSystemFilterNames();

	/**
	 * @return an array of the ISystemFilter objects contained in this filter container.
	 */
	public ISystemFilter[] getSystemFilters();

	/**
	 * @return a system filter given its name. Will return null if no filter by this name is found.
	 */
	public ISystemFilter getSystemFilter(String filterName);

	/**
	 * @return the parent pool of this container.
	 * If this is itself a pool, returns "this".
	 * For a nested filter, returns the pool that is the ultimate parent of this filter.
	 */
	public ISystemFilterPool getSystemFilterPool();

	/**
	 * @return how many filters are directly defined in this filter container.
	 */
	public int getSystemFilterCount();

	/**
	 * Removes a given filter from the list. Will do nothing if the specified filter
	 * is not in this filter container.
	 * @param filter SystemFilter object to remove
	 */
	public void deleteSystemFilter(ISystemFilter filter);

	/**
	 * Renames a given filter in the list. The new name is assumed to be valid.
	 * Will perform the rename whether or not the filter is contained in this
	 * container.
	 * @param filter SystemFilter object to rename
	 * @param newName New name to assign it.
	 */
	public void renameSystemFilter(ISystemFilter filter, String newName);

	/**
	 * @return a given filter's zero-based location. Will return -1 if the filter
	 * is not present in this container.
	 */
	public int getSystemFilterPosition(ISystemFilter filter);

	/**
	 * Move a given filter to a given zero-based location.
	 * Does nothing if the filter is not in this container.
	 * @param pos the new position of the filter.
	 * @param filter the filter to move.
	 */
	public void moveSystemFilter(int pos, ISystemFilter filter);

	/**
	 * Updates a given filter.
	 * The filter need not be present in this container but will operate on
	 * any filter.
	 * @param filter SystemFilter object to update
	 * @param newName New name to assign it. Assumes unique checking already done.
	 * @param newStrings New strings to assign it. Replaces current strings.
	 */
	public void updateSystemFilter(ISystemFilter filter, String newName, String[] newStrings);
}
