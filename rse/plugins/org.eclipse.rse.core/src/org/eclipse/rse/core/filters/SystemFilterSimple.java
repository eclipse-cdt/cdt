/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
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
 * David Dykstal (IBM) - [206901] fixing ArrayStoreException in getPersistableChildren
 *   Fix involved removing visibility for data referenced in SystemFilter. Addressed
 *   that by modifying the implementation of SystemFilterSimple to use its own data.
 *******************************************************************************/

package org.eclipse.rse.core.filters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.rse.core.model.ISystemContainer;
import org.eclipse.rse.core.model.ISystemContentsType;
import org.eclipse.rse.internal.core.filters.SystemFilter;

/**
 * A lightweight override of the full-fledged persistable implementation of SystemFilter.
 * This class replaces the heavy-weight MOF implementations with simple non-MOF 
 * implementations.
 * <p>
 * This flavour of SystemFilter implementation is for those cases where a simple in-memory
 * SystemFilter is needed temporarily, perhaps to populate a GUI widget say, and the filter
 * does not need to be savable/restorable. As a result there is no mof, and no need for a
 * parent SystemFilterPool or SystemFilterPoolManager. The class is small, simple and 
 * directly instantiable.
 * <p>
 * This simple implementation does <i>not</i> support:
 * <ul>
 *  <li>Saving or restoring from disk
 *  <li>SystemFilterStrings ... only Strings are used for the filter strings
 *  <li>Nested filters
 *  <li>Parent filter pool 
 *  <li>The attributes relativeOrder, promptable and default
 * </ul>
 */
public class SystemFilterSimple extends SystemFilter implements ISystemContainer {

	private String name = null;
	private String type = null;
	private boolean caseSensitive = false;
	private boolean promptable = false;
	private Object parent;
	private boolean isStale;
	private List filterStrings = new ArrayList(3);
	private HashMap cachedContents;

	/**
	 * Constructor for SystemFilterSimpleImpl
	 */
	public SystemFilterSimple(String name) {
		//super();
		this.name = name;
//		filterStringVector = new Vector();
		isStale = true;
		cachedContents = new HashMap();
	}

//    protected void invalidateCache()
//    {
//    	filterStringArray = null;
//    	filterStringObjectArray = null;
//    	//filterStringVector = null;
//    }

	/**
	 * Return true if this a transient or simple filter that is only created temporary "on the fly"
	 *  and not intended to be saved or part of the filter framework. Eg it has no manager or provider.
	 * <p>
	 * We always return true
	 */
	public boolean isTransient() {
		return true;
	}

	/**
	 * Clones a given filter to the given target filter.
	 * All filter strings, and all nested filters, are copied.
	 * @param targetFilter new filter into which we copy all our data
	 */
	public void clone(ISystemFilter targetFilter) {
		super.clone(targetFilter);
		targetFilter.setFilterStrings(getFilterStringsVector());
	}

	// -------------------------------------------------------
	// New methods to simplify life. Often a simple filter 
	//  contains a single filter string so these methods 
	//  make it easier to set/get that filter string
	// -------------------------------------------------------
	/**
	 * Set the single filter string
	 */
	public void setFilterString(String filterString) {
		filterStrings.clear();
		filterStrings.add(filterString);
//    	filterStringVector.clear();
//    	filterStringVector.addElement(filterString);
//    	invalidateCache();
	}

	/**
	 * Get the single (or the first) filter string.
	 * Returns null if setFilterString has not been called.
	 */
	public String getFilterString() {
		String result = filterStrings.isEmpty() ? null : (String) filterStrings.get(0);
		return result;
//    	if (filterStringVector.size() == 0)
//    	  return null;
//    	else
//    	  return (String)filterStringVector.elementAt(0);
	}

	/**
	 * Set the parent. Since we don't have any filter manager, we need
	 * some way to store context info for the adapter. Use this.
	 */
	public void setParent(Object parent) {
		this.parent = parent;
	}

	/**
	 * Get the parent as set in setParent(Object)
	 */
	public Object getParent() {
		return parent;
	}

	// -------------------------------------------------------
	// Functional methods overridden to do something simple...
	// -------------------------------------------------------

	/**
	 * Set the filter's name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get the filter's name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the filter's type
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Get the filter's type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Specify if filter strings in this filter are case sensitive. 
	 * Default is false.
	 * @param value The new value of the StringsCaseSensitive attribute
	 */
	public void setStringsCaseSensitive(boolean value) {
		this.caseSensitive = value;
	}

	/**
	 * Are filter strings in this filter case sensitive?
	 */
	public boolean isStringsCaseSensitive() {
		return caseSensitive;
	}

	/**
	 * Are filter strings in this filter case sensitive?
	 */
	public boolean areStringsCaseSensitive() {
		return caseSensitive;
	}

	/**
	 * Is this a special filter that prompts the user when it is expanded?
	 */
	public void setPromptable(boolean promptable) {
		this.promptable = promptable;
	}

	/**
	 * Is this a special filter that prompts the user when it is expanded?
	 */
	public boolean isPromptable() {
		return promptable;
	}

	/**
	 * Return filter strings as an array of String objects.
	 */
	public String[] getFilterStrings() {
		String[] result = new String[filterStrings.size()];
		filterStrings.toArray(result);
		return result;
//    	if (filterStringArray == null)
//    	{
//    	  filterStringArray = new String[filterStringVector.size()];
//          for (int idx=0; idx<filterStringArray.length; idx++)
//    	    filterStringArray[idx] = (String)filterStringVector.elementAt(idx);
//    	}
//    	return filterStringArray;
	}

	/**
	 * Return filter strings as a Vector of String objects
	 */
	public Vector getFilterStringsVector() {
		Vector result = new Vector(filterStrings.size());
		result.addAll(filterStrings);
		return result;
//    	return filterStringVector;
	}

	/**
	 * Return how many filter strings are defined in this filter.
	 */
	public int getFilterStringCount() {
		return filterStrings.size();
//   	return filterStringVector.size();
	}

	/**
	 * Set all the filter strings for this filter.
	 * @param newStrings Vector of String objects
	 */
	public void setFilterStrings(Vector newStrings) {
		filterStrings.clear();
		filterStrings.addAll(newStrings);
//    	filterStringVector.clear();
//    	for (int idx=0; idx<newStrings.size(); idx++)
//    	{
//    	   filterStringVector.addElement(newStrings.elementAt(idx));
//    	}    	
//    	invalidateCache();
	}

	/**
	 * Set all the filter strings for this filter.
	 * @param newStrings array of String objects
	 */
	public void setFilterStrings(String newStrings[]) {
		filterStrings.clear();
		filterStrings.addAll(Arrays.asList(newStrings)); // cannot just set since asList returns a fixed-size array
//    	filterStringVector.clear();
//    	for (int idx=0; idx<newStrings.length; idx++)
//    	{
//           filterStringVector.addElement(newStrings[idx]);
//    	}
//    	invalidateCache(); 
	}

	/**
	 * Append a new filter string to this filter's list.
	 * Returns null.
	 */
	public ISystemFilterString addFilterString(String newString) {
		filterStrings.add(newString);
//    	filterStringVector.addElement(newString);
//    	invalidateCache();    	
		return null;
	}

	/**
	 * Insert a new filter string to this filter's list, at the given zero-based position.
	 * Returns null.
	 */
	public ISystemFilterString addFilterString(String newString, int position) {
		filterStrings.add(position, newString);
//    	filterStringVector.insertElementAt(newString,position);
//    	invalidateCache();    	
		return null;
	}

	/**
	 * Delete a filter string from this filter's list.
	 * Returns null.
	 */
	public ISystemFilterString removeFilterString(String oldString) {
		filterStrings.remove(oldString);
//    	filterStringVector.removeElement(oldString);    	
//    	invalidateCache();
		return null;
	}

	/**
	 * Remove a filter string from this filter's list, given its zero-based position
	 * @return the SystemFilterString object deleted, or null if not found
	 */
	public ISystemFilterString removeFilterString(int position) {
		filterStrings.remove(position);
//    	filterStringVector.removeElementAt(position);
//    	invalidateCache();
		return null;
	}

	/**
	 * Return the children of this filter.
	 * This is the same as getFilterStrings()
	 */
	public Object[] getChildren() {
		return getFilterStrings();
	}

	/**
	 * Returns true if this filter has any filter strings
	 */
	public boolean hasChildren() {
		return filterStrings.size() > 0;
//    	return (filterStringVector.size() > 0);
	}

	// ---------------------
	// methods needed by ISystemFilter
	// ---------------------

	// -------------------------------------------------------
	// Non-applicable methods overridden to do nothing...
	// -------------------------------------------------------

	/**
	 * Get this filter's filter string objects as an array.
	 * We return null, as we don't support SystemFilterString objects,
	 * just String objects.
	 */
	public ISystemFilterString[] getSystemFilterStrings() {
		return null;
	}

	/**
	 * Overridden to do nothing
	 */
	public void setSupportsNestedFilters(boolean value) {
	}

	/**
	 * Does this support nested filters? No. Not for simple filtes.
	 */
	public boolean supportsNestedFilters() {
		return false;
	}

	/**
	 * Return true if this filter is a nested filter or not. 
	 * Overridden to return false;
	 */
	public boolean isNested() {
		return false;
	}

	/**
	 * Update a new filter string's string value.
	 * Overridden to do nothing.
	 */
	public void updateFilterString(ISystemFilterString filterString, String newValue) {
	}

	/**
	 * Remove a filter string from this filter's list, given its SystemFilterString object.
	 * Overridden to do nothing
	 */
	public boolean removeFilterString(ISystemFilterString filterString) {
		return false;
	}

	/**
	 * Move a given filter string to a given zero-based location.
	 * Overridden to do nothing
	 */
	public void moveSystemFilterString(int pos, ISystemFilterString filterString) {
	}

	/**
	 * Overridden to do nothing
	 */
	public void setRelativeOrder(int value) {
	}

	/**
	 * Overridden to return -1
	 */
	public int getRelativeOrder() {
		return -1;
	}

	/**
	 * Overridden to do nothing
	 */
	public void setDefault(boolean value) {
	}

	/**
	 * Overridden to return false
	 */
	public boolean isDefault() {
		return false;
	}

	/**
	 * Overridden to do nothing
	 */
	public void setParentFilter(ISystemFilter l) {
	}

	/**
	 * Overridden to return null
	 */
	public ISystemFilter getParentFilter() {
		return null;
	}

	/**
	 * Overridden to return null
	 */
	public Vector getSystemFilterNames() {
		return null;
	}

	/**
	 * Overridden to return null
	 */
	public java.util.List getNestedFilters() {
		return null;
	}

	/**
	 * Overridden to return null
	 */
	public Vector getSystemFiltersVector() {
		return null;
	}

	/**
	 * Overridden to return null
	 */
	public ISystemFilter[] getSystemFilters() {
		return null;
	}

	/**
	 * Overridden to return 0
	 */
	public int getSystemFilterCount() {
		return 0;
	}

	/**
	 * Overridden to return null
	 */
	public java.util.List getStrings() {
		return null;
	}

	/**
	 * Overridden to return null
	 */
	public ISystemFilter getSystemFilter(String aliasName) {
		return null;
	}

	/**
	 * Overridden to return null
	 */
	public ISystemFilterPoolManagerProvider getProvider() {
		return null;
	}

	/**
	 * Overridden to return null
	 */
	public ISystemFilterPoolManager getSystemFilterPoolManager() {
		return null;
	}

	/**
	 * Overridden to return null
	 */
	public IFile getSaveFile() {
		return null;
	}

	/**
	 * Overridden to return null
	 */
	public String getSaveFileName() {
		return null;
	}

	/**
	 * Overridden to do nothing
	 */
	public void save() throws Exception {
	}

	/**
	 * Cache contents of a certain type.
	 * @param type the contents type.
	 * @param cachedContents the contents to cache.
	 */
	public void setContents(ISystemContentsType type, Object[] cachedContents) {
		this.cachedContents.put(type, cachedContents);
		isStale = false;
	}

	/**
	 * @see org.eclipse.rse.core.model.ISystemContainer#getContents(org.eclipse.rse.core.model.ISystemContentsType)
	 */
	public Object[] getContents(ISystemContentsType contentsType) {
		return (Object[]) cachedContents.get(contentsType);
	}

	/**
	 * @see org.eclipse.rse.core.model.ISystemContainer#hasContents(org.eclipse.rse.core.model.ISystemContentsType)
	 */
	public boolean hasContents(ISystemContentsType contentsType) {
		if (cachedContents.containsKey(contentsType)) {
			return true;
		}
		return false;
	}

	/**
	 * @see org.eclipse.rse.core.model.ISystemContainer#isStale()
	 */
	public boolean isStale() {
		return isStale;
	}

	/**
	 * @see org.eclipse.rse.core.model.ISystemContainer#markStale(boolean)
	 */
	public void markStale(boolean isStale) {
		markStale(isStale, true);
	}

	/**
	 * @see org.eclipse.rse.core.model.ISystemContainer#markStale(boolean)
	 */
	public void markStale(boolean isStale, boolean clearCache) {
		this.isStale = isStale;
		if (clearCache) {
			cachedContents.clear();
		}
	}

}
