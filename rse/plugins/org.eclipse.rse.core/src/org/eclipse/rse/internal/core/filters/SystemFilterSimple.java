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
 * David Dykstal (IBM) - [224671] [api] org.eclipse.rse.core API leaks non-API types
 *******************************************************************************/

package org.eclipse.rse.internal.core.filters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.rse.core.filters.ISystemFilter;
import org.eclipse.rse.core.filters.ISystemFilterPoolManager;
import org.eclipse.rse.core.filters.ISystemFilterPoolManagerProvider;
import org.eclipse.rse.core.filters.ISystemFilterString;
import org.eclipse.rse.core.model.ISystemContentsType;
import org.eclipse.rse.core.model.ISystemModifiableContainer;
import org.eclipse.rse.core.subsystems.ISubSystem;

/**
 * A lightweight implementation of ISystemFilter.
 * <p>
 * This flavor  is for those cases where a simple in-memory
 * ISystemFilter is needed temporarily, perhaps to populate a GUI widget say, and the filter
 * does not need to be savable/restorable. As a result there is no need for a
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
public class SystemFilterSimple extends SystemFilter implements ISystemModifiableContainer {

	private String name = null;
	private String type = null;
	private boolean caseSensitive = false;
	private boolean promptable = false;
	private boolean isStale = true;
	private Object subsystem = null;
	private List filterStrings = new ArrayList(3);
	private HashMap cachedContents = new HashMap();

	/**
	 * Constructor for SystemFilterSimple
	 */
	public SystemFilterSimple(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.internal.core.filters.SystemFilter#isTransient()
	 */
	public boolean isTransient() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.internal.core.filters.SystemFilter#clone(org.eclipse.rse.core.filters.ISystemFilter)
	 */
	public void clone(ISystemFilter targetFilter) {
		super.clone(targetFilter);
		targetFilter.setFilterStrings(getFilterStrings());
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
	}

	/**
	 * Get the single (or the first) filter string.
	 * Returns null if setFilterString has not been called.
	 */
	public String getFilterString() {
		String result = filterStrings.isEmpty() ? null : (String) filterStrings.get(0);
		return result;
	}

	/**
	 * Set the parent. Since we don't have any filter manager, we need
	 * some way to store context info for the adapter. Use this.
	 */
	public void setSubSystem(ISubSystem parent) {
		this.subsystem = parent;
	}

	/**
	 * Get the parent as set in setParent(Object)
	 */
	public Object getSubSystem() {
		return subsystem;
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
	}

	/**
	 * Return how many filter strings are defined in this filter.
	 */
	public int getFilterStringCount() {
		return filterStrings.size();
	}

	/**
	 * Set all the filter strings for this filter.
	 * @param newStrings array of String objects
	 */
	public void setFilterStrings(String newStrings[]) {
		filterStrings.clear();
		filterStrings.addAll(Arrays.asList(newStrings)); // cannot just set since asList returns a fixed-size array
	}

	/**
	 * Append a new filter string to this filter's list.
	 * Returns null.
	 */
	public ISystemFilterString addFilterString(String newString) {
		filterStrings.add(newString);
		return null;
	}

	/**
	 * Insert a new filter string to this filter's list, at the given zero-based position.
	 * Returns null.
	 */
	public ISystemFilterString addFilterString(String newString, int position) {
		filterStrings.add(position, newString);
		return null;
	}

	/**
	 * Delete a filter string from this filter's list.
	 * Returns null.
	 */
	public ISystemFilterString removeFilterString(String oldString) {
		filterStrings.remove(oldString);
		return null;
	}

	/**
	 * Remove a filter string from this filter's list, given its zero-based position
	 * @return the SystemFilterString object deleted, or null if not found
	 */
	public ISystemFilterString removeFilterString(int position) {
		filterStrings.remove(position);
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
	 * Overridden to return an empty array
	 */
	public String[] getSystemFilterNames() {
		return new String[0];
	}

	/**
	 * Overridden to return an empty array
	 */
	public ISystemFilter[] getNestedFilters() {
		return new ISystemFilter[0];
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
	 * Overridden to return an empty array
	 */
	public ISystemFilterString[] getStrings() {
		return new ISystemFilterString[0];
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

	/*
	 * @see org.eclipse.rse.core.model.ISystemContainer#getContents(org.eclipse.rse.core.model.ISystemContentsType)
	 */
	public Object[] getContents(ISystemContentsType contentsType) {
		return (Object[]) cachedContents.get(contentsType);
	}

	/*
	 * @see org.eclipse.rse.core.model.ISystemContainer#hasContents(org.eclipse.rse.core.model.ISystemContentsType)
	 */
	public boolean hasContents(ISystemContentsType contentsType) {
		if (cachedContents.containsKey(contentsType)) {
			return true;
		}
		return false;
	}

	/*
	 * @see org.eclipse.rse.core.model.ISystemContainer#isStale()
	 */
	public boolean isStale() {
		return isStale;
	}

	/*
	 * @see org.eclipse.rse.core.model.ISystemContainer#markStale(boolean)
	 */
	public void markStale(boolean isStale) {
		markStale(isStale, true);
	}

	/*
	 * @see org.eclipse.rse.core.model.ISystemContainer#markStale(boolean)
	 */
	public void markStale(boolean isStale, boolean clearCache) {
		this.isStale = isStale;
		if (clearCache) {
			cachedContents.clear();
		}
	}

}
