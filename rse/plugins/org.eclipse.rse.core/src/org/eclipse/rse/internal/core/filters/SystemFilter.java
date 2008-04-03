/********************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others. All rights reserved.
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
 * David Dykstal (IBM) - 142806: refactoring persistence framework
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * David Dykstal (IBM) - [206901] fixing ArrayStoreException in getPersistableChildren
 *   Removed caching that was here because of previous EMF/MOF implementation. This makes
 *   the class simpler.
 * David Dykstal (IBM) - [224671] [api] org.eclipse.rse.core API leaks non-API types
 ********************************************************************************/

package org.eclipse.rse.internal.core.filters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.core.filters.ISystemFilter;
import org.eclipse.rse.core.filters.ISystemFilterContainer;
import org.eclipse.rse.core.filters.ISystemFilterPool;
import org.eclipse.rse.core.filters.ISystemFilterPoolManager;
import org.eclipse.rse.core.filters.ISystemFilterPoolManagerProvider;
import org.eclipse.rse.core.filters.ISystemFilterString;
import org.eclipse.rse.core.model.IRSEPersistableContainer;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.references.SystemReferencedObject;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.core.RSECoreMessages;

/**
 * A filter is an encapsulation of a unique name, and a list of filter strings.
 * Filters can be referenced.
 */
public class SystemFilter extends SystemReferencedObject implements ISystemFilter, IAdaptable {

	private SystemFilterContainerCommonMethods helpers = null;
	private ISystemFilterPool parentPool = null;
	private List filterStrings = new ArrayList(3);
	private String name = null;
	private String type = null;
	private boolean supportsNestedFilters = false;
	private int relativeOrder = 0;
	private boolean default_ = false;
	private boolean stringsCaseSensitive = false;
	private boolean promptable = false;
	private boolean supportsDuplicateFilterStrings = false;
	private boolean nonDeletable = false;
	private boolean nonRenamable = false;
	private boolean nonChangable = false;
	private boolean stringsNonChangable = false;
	private int release = 0;
	private boolean singleFilterStringOnly = false;
	private List nestedFilters = new ArrayList(3);
	private ISystemFilter _parentFilter;

	/**
	 * Constructor.
	 */
	protected SystemFilter() {
		super();
		helpers = new SystemFilterContainerCommonMethods();
	}

	/*
	 * Private internal way to get filters. Makes it easy to change in future, if we don't use MOF.
	 */
	private List internalGetFilters() {
		return nestedFilters;
	}

	/**
	  * Returns the type attribute. Intercepted to return SystemFilterConstants.DEFAULT_TYPE if it is currently null
	  */
	public String getType() {
		String type = getTypeGen();
		if (type == null)
			return ISystemFilterConstants.DEFAULT_TYPE;
		else
			return type;
	}

	/**
	  * Returns the type attribute. Intercepted to return SystemFilterConstants.DEFAULT_TYPE if it is currently null
	  */
	public String getTypeGen() {
		return type;
	}

	/**
	 * Creates a new nested system filter within this filter. 
	 * This filter will inherit/store the following attributes from this filter:
	 * <ul>
	 *   <li>supportsNestedFilters
	 *   <li>supportsDuplicateFilterStrings
	 *   <li>stringsCaseSensitive
	 *   <li>data
	 * </ul>
	 * @param aliasName The name to give the new filter. Must be unique for this pool.
	 * @param filterStrings The list of String objects that represent the filter strings.
	 */
	public ISystemFilter createSystemFilter(String aliasName, String[] filterStrings) {
		ISystemFilter newFilter = helpers.createSystemFilter(internalGetFilters(), getParentFilterPool(), aliasName, filterStrings);
		newFilter.setSupportsNestedFilters(true); // presumably it does since it is nested itself.
		newFilter.setSupportsDuplicateFilterStrings(supportsDuplicateFilterStrings());
		newFilter.setStringsCaseSensitive(areStringsCaseSensitive());
		return newFilter;
	}

	/**
	 * Internal use method
	 */
	public void initializeFilterStrings() {
		Iterator i = filterStrings.iterator();
		while (i.hasNext())
			((ISystemFilterString) i.next()).setParentSystemFilter(this);
	}

	/**
	 * Clones a given filter to the given target filter.
	 * All filter strings, and all nested filters, are copied.
	 * @param targetFilter new filter into which we copy all our data
	 */
	public void clone(ISystemFilter targetFilter) {
		// clone attributes
		// targetFilter.setName(getName()); name is not cloned, we assume the target filter already has a name
		targetFilter.setDefault(isDefault());
		targetFilter.setType(getType());
		targetFilter.setPromptable(isPromptable());
		targetFilter.setRelativeOrder(getRelativeOrder());
		targetFilter.setSupportsNestedFilters(isSupportsNestedFilters());
		targetFilter.setSupportsDuplicateFilterStrings(isSupportsDuplicateFilterStrings());
		targetFilter.setStringsNonChangable(isStringsNonChangable());
		targetFilter.setNonChangable(isNonChangable());
		targetFilter.setNonDeletable(isNonDeletable());
		targetFilter.setNonRenamable(isNonRenamable());
		if (isSetSingleFilterStringOnly()) targetFilter.setSingleFilterStringOnly(isSingleFilterStringOnly());
		if (isSetStringsCaseSensitive()) targetFilter.setStringsCaseSensitive(isStringsCaseSensitive());
		// clone filter strings
		ISystemFilterString[] strings = getSystemFilterStrings();
		if (strings != null) {
			for (int idx = 0; idx < strings.length; idx++) {
				copySystemFilterString(targetFilter, strings[idx]);
			}
		}
		// clone nested filters...
		ISystemFilter[] filters = getSystemFilters();
		if (filters != null) for (int idx = 0; idx < filters.length; idx++) {
			ISystemFilter newFilter = targetFilter.createSystemFilter(filters[idx].getName(), null);
			filters[idx].clone(newFilter); // recursive call
		}
	}

	/**
	 * Copies a given filter string from this filter to another filter in this pool or another pool
	 *  in this manager or another manager.
	 */
	public ISystemFilterString copySystemFilterString(ISystemFilter targetFilter, ISystemFilterString oldFilterString) {
		ISystemFilterString newString = targetFilter.addFilterString(null);
		oldFilterString.clone(newString);
		return newString;
	}

	/**
	 * From SystemFilterContainer.
	 * Same as calling getParentFilterPool(). It walks the parent chain until the pool is found.
	 */
	public ISystemFilterPool getSystemFilterPool() {
		return getParentFilterPool();
	}

	/**
	 * Return true if this a transient or simple filter that is only created temporary "on the fly"
	 *  and not intended to be saved or part of the filter framework. Eg it has no manager or provider.
	 * <p>
	 * We always return false.
	 */
	public boolean isTransient() {
		return false;
	}

	/**
	 * Does this support nested filters? Calls mof-generated isSupportsNestedFilters.
	 */
	public boolean supportsNestedFilters() {
		return isSupportsNestedFilters();
	}

	/**
	 * Does this support duplicate filter strings? Calls mof-generated isSupportsDuplicateFilterStrings.
	 */
	public boolean supportsDuplicateFilterStrings() {
		return isSupportsDuplicateFilterStrings();
	}

	/**
	 * Return true if this filter is a nested filter or not. If not, its parent is the filter pool.
	 */
	public boolean isNested() {
		return (getParentFilter() != null);
	}

	/**
	 * Are filter strings in this filter case sensitive?
	 * If not set locally, queries the parent filter pool's atttribute.
	 * @return The value of the StringsCaseSensitive attribute
	 */
	public boolean isStringsCaseSensitive() {
		if (!isSetStringsCaseSensitive())
			return getParentFilterPool().isStringsCaseSensitive();
		else
			return stringsCaseSensitive;
	}

	/**
	 * Same as isStringsCaseSensitive()
	 * @return The value of the StringsCaseSensitive attribute
	 */
	public boolean areStringsCaseSensitive() {
		return isStringsCaseSensitive();
	}

	/**
	 * Return Vector of String objects: the names of existing filters in this container.
	 * Needed by name validators for New and Rename actions to verify new name is unique.
	 */
	public String[] getSystemFilterNames() {
		List filters = internalGetFilters();
		List names = helpers.getSystemFilterNames(filters);
		String[] result = new String[names.size()];
		names.toArray(result);
		return result;
	}

	/**
	 * Return the nested filters as an array
	 */
	public ISystemFilter[] getSystemFilters() {
		return helpers.getSystemFilters(internalGetFilters());
	}

	/**
	 * Return how many filters are defined in this filter container
	 */
	public int getSystemFilterCount() {
		return internalGetFilters().size();
	}

	/**
	 * Return a filter object, given its aliasname.
	 * Can be used to test if an aliasname is already used (non-null return).
	 * @param aliasName unique aliasName (case insensitive) to search on.
	 * @return SystemFilter object with unique aliasName, or null if
	 *  no filter object with this name exists.
	 */
	public ISystemFilter getSystemFilter(String aliasName) {
		return helpers.getSystemFilter(internalGetFilters(), aliasName);

	}

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
	public boolean addSystemFilter(ISystemFilter filter) {
		return helpers.addSystemFilter(internalGetFilters(), filter);
	}

	/**
	 * Removes a given filter from the list.
	 * @param filter SystemFilter object to remove
	 */
	public void deleteSystemFilter(ISystemFilter filter) {
		helpers.deleteSystemFilter(internalGetFilters(), filter);
	}

	/**
	 * Rename a given filter in the list.
	 * @param filter SystemFilter object to remove
	 */
	public void renameSystemFilter(ISystemFilter filter, String newName) {
		helpers.renameSystemFilter(internalGetFilters(), filter, newName);
	}

	/**
	 * Updates a given filter in the list.
	 * @param filter SystemFilter object to update
	 * @param newName New name to assign it. Assumes unique checking already done.
	 * @param newStrings New strings to assign it. Replaces current strings.
	 */
	public void updateSystemFilter(ISystemFilter filter, String newName, String[] newStrings) {
		helpers.updateSystemFilter(internalGetFilters(), filter, newName, newStrings);
	}

	/**
	 * Duplicates a given filter in the list.
	 * @param filter SystemFilter object to clone
	 * @param aliasName New, unique, alias name to give this filter. Clone will fail if this is not unique.
	 */
	public ISystemFilter cloneSystemFilter(ISystemFilter filter, String aliasName) {
		return helpers.cloneSystemFilter(internalGetFilters(), filter, aliasName);
	}

	/**
	 * Return a given filter's zero-based location
	 */
	public int getSystemFilterPosition(ISystemFilter filter) {
		return helpers.getSystemFilterPosition(internalGetFilters(), filter);
	}

	/**
	 * Move a given filter to a given zero-based location
	 */
	public void moveSystemFilter(int pos, ISystemFilter filter) {
		helpers.moveSystemFilter(internalGetFilters(), pos, filter);
	}

	/**
	 * Return the parent pool of this filter. For nested filters, we walk up the parent chain
	 * until we find the pool.
	 */
	public ISystemFilterPool getParentFilterPool() {
		return parentPool;
	}

	/**
	 * Internal use method to set the parent filter pool.
	 */
	public void setParentFilterPool(ISystemFilterPool parentPool) {
		this.parentPool = parentPool;
		ISystemFilter[] filters = getSystemFilters();
		if (filters != null) {
			for (int idx = 0; idx < filters.length; idx++) {
				filters[idx].setParentFilterPool(parentPool);
			}
		}
	}

	/**
	 * Return the ISystemFilterContainer parent of this filter. Will be either
	 * a SystemFilterPool or a SystemFilter if this is a nested filter.
	 */
	public ISystemFilterContainer getParentFilterContainer() {
		ISystemFilter parentFilter = getParentFilter();
		return (parentFilter != null) ? (ISystemFilterContainer) parentFilter : (ISystemFilterContainer) getParentFilterPool();
	}

	/**
	 * Returns the filter strings of this filter as an array of String objects.
	 * The array may be empty but will not be null.
	 */
	public String[] getFilterStrings() {
		ISystemFilterString[] filterStrings = getSystemFilterStrings();
		String[] result = new String[filterStrings.length];
		for (int i = 0; i < filterStrings.length; i++) {
			ISystemFilterString filterString = filterStrings[i];
			result[i] = filterString.getString();
		}
		return result;
	}

	/**
	 * Return how many filter strings are defined in this filter.
	 */
	public int getFilterStringCount() {
		return filterStrings.size();
	}

	/**
	 * Get a filter string given its string value
	 */
	public ISystemFilterString getSystemFilterString(String string) {
		ISystemFilterString[] strings = getSystemFilterStrings();
		ISystemFilterString match = null;
		boolean cs = areStringsCaseSensitive();
		if (strings != null) {
			for (int idx = 0; (match == null) && (idx < strings.length); idx++) {
				if (cs) {
					if (string.equals(strings[idx].getString())) // todo: allow case-sensitivity to be definable
						match = strings[idx];
				} else {
					if (string.equalsIgnoreCase(strings[idx].getString())) // todo: allow case-sensitivity to be definable
						match = strings[idx];
				}
			}
		}
		return match;
	}

	/**
	 * Set all the filter strings for this filter.
	 * @param newStrings Vector of String objects
	 */
	public void setFilterStrings(Vector newStrings) {
		filterStrings.clear();
		for (Iterator z = newStrings.iterator(); z.hasNext();) {
			String newString = (String) z.next();
			ISystemFilterString filterString = createFilterString(newString);
			filterStrings.add(filterString);
		}
		setDirty(true);
	}

	/**
	 * Get this filter's filter string objects as an array.
	 */
	public ISystemFilterString[] getSystemFilterStrings() {
		ISystemFilterString[] result = new ISystemFilterString[filterStrings.size()];
		filterStrings.toArray(result);
		return result;
	}

	/**
	 * Set all the filter strings for this filter.
	 * @param newStrings array of String objects
	 */
	public void setFilterStrings(String newStrings[]) {
		filterStrings.clear();
		for (int idx = 0; idx < newStrings.length; idx++) {
			ISystemFilterString filterString = createFilterString(newStrings[idx]);
			filterStrings.add(filterString);
		}
		setDirty(true);
	}

	/**
	 * Returns a system filter string created from a string.
	 * @param string
	 * @return
	 */
	private ISystemFilterString createFilterString(String string) {
		ISystemFilterString filterstring = new SystemFilterString();
		filterstring.setString(string);
		filterstring.setParentSystemFilter(this);
		return filterstring;
	}

	/**
	 * Append a new filter string to this filter's list
	 */
	public ISystemFilterString addFilterString(String newString) {
		ISystemFilterString newFilterString = createFilterString(newString);
		filterStrings.add(newFilterString);
		setDirty(true);
		return newFilterString;
	}

	/**
	 * Insert a new filter string to this filter's list, at the given zero-based position
	 */
	public ISystemFilterString addFilterString(String newString, int position) {
		ISystemFilterString newFilterString = createFilterString(newString);
		filterStrings.add(position, newFilterString);
		setDirty(true);
		return newFilterString;
	}

	/**
	 * Update a new filter string's string value
	 */
	public void updateFilterString(ISystemFilterString filterString, String newValue) {
		filterString.setString(newValue);
	}

	/**
	 * Delete a filter string from this filter's list.
	 * @return the SystemFilterString object deleted, or null if not found
	 */
	public ISystemFilterString removeFilterString(String oldString) {
		ISystemFilterString match = null;
		Iterator i = filterStrings.iterator();
		while ((match == null) && (i.hasNext())) {
			ISystemFilterString currstring = (ISystemFilterString) i.next();
			if (currstring.getString().equals(oldString)) match = currstring;
		}
		if (match != null) {
			filterStrings.remove(match);
			setDirty(true);
		}
		return match;
	}

	/**
	 * Remove a filter string from this filter's list, given its zero-based position
	 * @return the SystemFilterString object deleted, or null if not found
	 */
	public ISystemFilterString removeFilterString(int position) {
		ISystemFilterString filterString = null;
		if (position < filterStrings.size()) {
			filterString = (ISystemFilterString) filterStrings.remove(position);
			setDirty(true);
		}
		return filterString;
	}

	/**
	 * Remove a filter string from this filter's list, given its SystemFilterString object.
	 * @return true if the given string existed and hence was deleted.
	 */
	public boolean removeFilterString(ISystemFilterString filterString) {
		boolean removed = filterStrings.remove(filterString);
		if (removed) {
			setDirty(true);
		}
		return removed;
	}

	/**
	 * Move a given filter string to a given zero-based location
	 */
	public void moveSystemFilterString(int pos, ISystemFilterString filterString) {
		boolean removed = filterStrings.remove(filterString);
		if (removed) {
			filterStrings.add(pos, filterString);
			setDirty(true);
		}
	}

	/**
	 * This is the method required by the IAdaptable interface.
	 * Given an adapter class type, return an object castable to the type, or
	 *  null if this is not possible.
	 */
	public Object getAdapter(Class adapterType) {
		return Platform.getAdapterManager().getAdapter(this, adapterType);
	}

	/**
	 * Return the children of this filter.
	 * This is all nested filters and all filter strings.
	 */
	public Object[] getChildren() {
		String[] strings = getFilterStrings();
		ISystemFilter[] filters = getSystemFilters();
		Vector vChildren = new Vector();

		// start with nested filters...
		for (int idx = 0; idx < filters.length; idx++) {
			vChildren.addElement(filters[idx]);
		}
		// continue with resolved filter string objects...
		for (int idx = 0; idx < strings.length; idx++) {
			String filterString = strings[idx];
			vChildren.addElement(filterString);
		}

		// convert whole thing to an array...
		Object[] children = new Object[vChildren.size()];
		for (int idx = 0; idx < vChildren.size(); idx++)
			children[idx] = vChildren.elementAt(idx);

		return children;
	}

	/**
	 * Returns true if this filter has any nested filters or any filter strings
	 */
	public boolean hasChildren() {
		if (filterStrings.size() > 0)
			return true;
		else
			return helpers.hasSystemFilters(internalGetFilters());
	}

	/**
	 * Return the caller which instantiated the filter pool manager overseeing this filter framework instance
	 */
	public ISystemFilterPoolManagerProvider getProvider() {
		ISystemFilterPoolManager mgr = getSystemFilterPoolManager();
		if (mgr != null)
			return mgr.getProvider();
		else
			return null;
	}

	/**
	 * Return the filter pool manager managing this collection of filter pools and their filters.
	 * To save space, we delegate this query to our parent filter pool.
	 */
	public ISystemFilterPoolManager getSystemFilterPoolManager() {
		ISystemFilterPool pool = getParentFilterPool();
		if (pool != null)
			return pool.getSystemFilterPoolManager();
		else
			return null;
	}

	/*-------------------
	 * Attribute getters and setters
	 *-------------------*/
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IRSEModelObject#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.RSEModelObject#getDescription()
	 */
	public String getDescription() {
		return RSECoreMessages.RESID_MODELOBJECTS_FILTER_DESCRIPTION;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilter#setName(java.lang.String)
	 */
	public void setName(String newName) {
		name = newName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilter#setType(java.lang.String)
	 */
	public void setType(String newType) {
		type = newType;
		setDirty(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilter#isSupportsNestedFilters()
	 */
	public boolean isSupportsNestedFilters() {
		return supportsNestedFilters;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilter#setSupportsNestedFilters(boolean)
	 */
	public void setSupportsNestedFilters(boolean newSupportsNestedFilters) {
		boolean oldSupportsNestedFilters = supportsNestedFilters;
		if (oldSupportsNestedFilters != newSupportsNestedFilters) {
			supportsNestedFilters = newSupportsNestedFilters;
			setDirty(true);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilter#getRelativeOrder()
	 */
	public int getRelativeOrder() {
		return relativeOrder;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilter#setRelativeOrder(int)
	 */
	public void setRelativeOrder(int newRelativeOrder) {
		relativeOrder = newRelativeOrder;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilter#isDefault()
	 */
	public boolean isDefault() {
		return default_;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilter#setDefault(boolean)
	 */
	public void setDefault(boolean newDefault) {
		default_ = newDefault;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilter#setStringsCaseSensitive(boolean)
	 */
	public void setStringsCaseSensitive(boolean newStringsCaseSensitive) {
		boolean oldStringsCaseSensitive = stringsCaseSensitive;
		if (oldStringsCaseSensitive != newStringsCaseSensitive) {
			stringsCaseSensitive = newStringsCaseSensitive;
			setDirty(true);
		}

	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilter#unsetStringsCaseSensitive()
	 */
	public void unsetStringsCaseSensitive() {
		if (stringsCaseSensitive) {
			stringsCaseSensitive = false;
			setDirty(true);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilter#isSetStringsCaseSensitive()
	 */
	public boolean isSetStringsCaseSensitive() {
		return stringsCaseSensitive;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilter#isPromptable()
	 */
	public boolean isPromptable() {
		return promptable;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilter#setPromptable(boolean)
	 */
	public void setPromptable(boolean newPromptable) {
		promptable = newPromptable;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilter#getParentFilter()
	 */
	public ISystemFilter getParentFilter() {
		return _parentFilter;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilter#setParentFilter(org.eclipse.rse.core.filters.ISystemFilter)
	 */
	public void setParentFilter(ISystemFilter newParentFilter) {
		_parentFilter = newParentFilter;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilter#getNestedFilters()
	 */
	public ISystemFilter[] getNestedFilters() {
		if (nestedFilters == null) {
			nestedFilters = new ArrayList();
		}
		ISystemFilter[] result = new ISystemFilter[nestedFilters.size()];
		nestedFilters.toArray(result);
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilter#getStrings()
	 */
	public ISystemFilterString[] getStrings() {
		ISystemFilterString[] result = new ISystemFilterString[filterStrings.size()];
		filterStrings.toArray(result);
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilter#isSupportsDuplicateFilterStrings()
	 */
	public boolean isSupportsDuplicateFilterStrings() {
		return supportsDuplicateFilterStrings;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilter#setSupportsDuplicateFilterStrings(boolean)
	 */
	public void setSupportsDuplicateFilterStrings(boolean newSupportsDuplicateFilterStrings) {
		boolean oldSupportsDuplicateFilterStrings = supportsDuplicateFilterStrings;
		if (oldSupportsDuplicateFilterStrings != newSupportsDuplicateFilterStrings) {
			supportsDuplicateFilterStrings = newSupportsDuplicateFilterStrings;
			setDirty(true);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilter#isNonDeletable()
	 */
	public boolean isNonDeletable() {
		return nonDeletable;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilter#setNonDeletable(boolean)
	 */
	public void setNonDeletable(boolean newNonDeletable) {
		nonDeletable = newNonDeletable;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilter#isNonRenamable()
	 */
	public boolean isNonRenamable() {
		return nonRenamable;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilter#setNonRenamable(boolean)
	 */
	public void setNonRenamable(boolean newNonRenamable) {
		nonRenamable = newNonRenamable;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilter#isNonChangable()
	 */
	public boolean isNonChangable() {
		return nonChangable;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilter#setNonChangable(boolean)
	 */
	public void setNonChangable(boolean newNonChangable) {
		nonChangable = newNonChangable;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilter#isStringsNonChangable()
	 */
	public boolean isStringsNonChangable() {
		return stringsNonChangable;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilter#setStringsNonChangable(boolean)
	 */
	public void setStringsNonChangable(boolean newStringsNonChangable) {
		boolean oldStringsNonChangable = stringsNonChangable;
		if (oldStringsNonChangable != newStringsNonChangable) {
			stringsNonChangable = newStringsNonChangable;
			setDirty(true);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilter#getRelease()
	 */
	public int getRelease() {
		return release;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilter#setRelease(int)
	 */
	public void setRelease(int newRelease) {
		release = newRelease;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilter#isSingleFilterStringOnly()
	 */
	public boolean isSingleFilterStringOnly() {
		if (isSetSingleFilterStringOnly())
			return singleFilterStringOnly;
		else
			return getSystemFilterPool().isSingleFilterStringOnly();
	}

	/**
	 * @deprecated - no longer used
	 */
	public boolean isSingleFilterStringOnlyGen() {
		return singleFilterStringOnly;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilter#setSingleFilterStringOnly(boolean)
	 */
	public void setSingleFilterStringOnly(boolean newSingleFilterStringOnly) {
		boolean oldSingleFilterStringOnly = singleFilterStringOnly;
		if (oldSingleFilterStringOnly != newSingleFilterStringOnly) {
			singleFilterStringOnly = newSingleFilterStringOnly;
			setDirty(true);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilter#unsetSingleFilterStringOnly()
	 */
	public void unsetSingleFilterStringOnly() {
		if (singleFilterStringOnly) {
			singleFilterStringOnly = false;
			setDirty(true);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilter#isSetSingleFilterStringOnly()
	 */
	public boolean isSetSingleFilterStringOnly() {
		return singleFilterStringOnly;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilter#setSubSystem(org.eclipse.rse.core.subsystems.ISubSystem)
	 */
	public void setSubSystem(ISubSystem subsystem) {
		// does nothing this is not a transient filter
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilter#getSubSystem()
	 */
	public Object getSubSystem() {
		return null; // since this is not a transient filter
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IRSEPersistableContainer#commit()
	 */
	public boolean commit() {
		ISystemProfile profile = getSystemFilterPoolManager().getSystemProfile();
		boolean result = profile.commit();
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IRSEPersistableContainer#getPersistableParent()
	 */
	public IRSEPersistableContainer getPersistableParent() {
		IRSEPersistableContainer result = getParentFilterContainer();
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IRSEPersistableContainer#getPersistableChildren()
	 */
	public IRSEPersistableContainer[] getPersistableChildren() {
		List children = new ArrayList(20);
		children.addAll(nestedFilters);
		children.addAll(filterStrings);
		children.addAll(Arrays.asList(getPropertySets()));
		IRSEPersistableContainer[] result = new IRSEPersistableContainer[children.size()];
		children.toArray(result);
		return result;
	}

}