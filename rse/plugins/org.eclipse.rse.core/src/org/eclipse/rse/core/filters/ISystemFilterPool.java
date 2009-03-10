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
 * David Dykstal (IBM) - cleanup - format and javadoc
 * Martin Oberhuber (Wind River) - [cleanup] Add API "since" Javadoc tags
 * David Dykstal (IBM) - [226561] Add API markup to RSE Javadocs where extend / implement is allowed
 * David Dykstal (IBM) - [235800] Document naming restriction for profiles and filter pools
 * David Dykstal (IBM) - [261486][api] add noextend to interfaces that require it
 *******************************************************************************/

package org.eclipse.rse.core.filters;

import org.eclipse.rse.core.model.IRSEModelObject;
import org.eclipse.rse.core.references.IRSEPersistableReferencedObject;

/**
 * This interface represents a system filter pool, which is a means of
 * grouping filters to be referenced together.
 * @noimplement This interface is not intended to be implemented by clients.
 * The allowable implementations are already present in the framework.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ISystemFilterPool extends IRSEPersistableReferencedObject, ISystemFilterContainer, IRSEModelObject {

	/**
	 * @return the object that instantiated the filter pool manager owning this filter pool
	 */
	public ISystemFilterPoolManagerProvider getProvider();

	/**
	 * @return true if filters in this pool support nested filters.
	 */
	public boolean supportsNestedFilters();

	/**
	 * @return true if filters in this pool supports duplicate filter strings.
	 */
	public boolean supportsDuplicateFilterStrings();

	/**
	 * @return true if the filters in this pool are case sensitive
	 */
	public boolean isStringsCaseSensitive();

	/**
	 * Set the filter pool manager for this filter pool. Should only be done when the filter
	 * pool is created by the manager.
	 * @param mgr the manager of this filter pool
	 */
	public void setSystemFilterPoolManager(ISystemFilterPoolManager mgr);

	/**
	 * @return the filter pool manager managing this collection of filter pools and their filters.
	 */
	public ISystemFilterPoolManager getSystemFilterPoolManager();

	/**
	 * Set a data object to be associated with this filter pool. This data is uninterpreted by this
	 * filter pool.
	 * @param data the data object
	 */
	public void setSystemFilterPoolData(Object data);

	/**
	 * @return the data object set using setFilterPoolData.
	 */
	public Object getSystemFilterPoolData();

	/**
	 * Clone the attributes from this filter pool into another filter pool.
	 * Assumes the core attributes were already set when filter pool was created:
	 * <ul>
	 *   <li>Name
	 *   <li>Deletable
	 *   <li>Supports nested filters
	 *   <li>Manager
	 * </ul>
	 * Attributes we clone:
	 * <ul>
	 *   <li>Data
	 *   <li>Type
	 *   <li>Default
	 * </ul>
	 * @param targetPool the filter pool that will receive the new attributes
	 */
	public void cloneSystemFilterPool(ISystemFilterPool targetPool) throws Exception;

	/**
	 * Copy a system filter to this or another filter pool.
	 * @param targetPool the receiving pool
	 * @param oldFilter the filter to copy
	 * @param newName the name which to give the new copy of the filter
	 * @return the newly created filter
	 */
	public ISystemFilter copySystemFilter(ISystemFilterPool targetPool, ISystemFilter oldFilter, String newName) throws Exception;

	/**
	 * Order filters according to the names in the list. Typically used to enforce
	 * a particular ordering in the filter list. If a name appears in this list and
	 * does not name a filter it is ignored. If there are filters in this pool that
	 * are not named in this list their order in the pool is undefined.
	 * @param names the names of the filters in this pool in the order they should be returned.
	 */
	public void orderSystemFilters(String[] names);

	/**
	 * @return the id of the filter pool. Used for referencing this filter pool from filter pool references.
	 */
	public String getId();

	/**
	 * @return The value of the Name attribute
	 */
	public String getName();

	/**
	 * Sets the filter pool name for this filter pool.
	 * Filter pool names must not contain 3 consecutive underscores "___" since these are used to separate
	 * profile names from filter pool names in a filter pool reference.
	 * @param value The new value of the Name attribute.
	 * @throws IllegalArgumentException if the name contains three consecutive underscore characters.
	 */
	public void setName(String value);

	/**
	 * @return The value of the Type attribute
	 * Allows tools to have typed filter pools.
	 * Type is not interpreted by the filter pool, but may be used by a subsystem.
	 */
	public String getType();

	/**
	 * @param value The new value of the Type attribute
	 * Allows tools to have typed filter pools.
	 * Type is not interpreted by the filter pool, but may be used by a subsystem.
	 */
	public void setType(String value);

	/**
	 * @return The value of the SupportsNestedFilters attribute
	 */
	public boolean isSupportsNestedFilters();

	/**
	 * @param value The new value of the SupportsNestedFilters attribute
	 */
	public void setSupportsNestedFilters(boolean value);

	/**
	 * @return The value of the Deletable attribute
	 */
	public boolean isDeletable();

	/**
	 * @param value The new value of the Deletable attribute
	 */
	public void setDeletable(boolean value);

	/**
	 * @return The value of the Default attribute
	 * true if this is a vendor-supplied pool versus user-created pool
	 */
	public boolean isDefault();

	/**
	 * @param value The new value of the Default attribute
	 */
	public void setDefault(boolean value);

	/**
	 * Sets the attribute for this filter pool that determines whether strings of
	 * filters contained in this pool are case sensitive or not. If not set
	 * this attribute is inherited from the containing filter pool manager.
	 * @param value The new value of the StringsCaseSensitive attribute
	 */
	public void setStringsCaseSensitive(boolean value);

	/**
	 * Unsets the StringsCaseSensitive attribute. Causes the case sensitivity of the
	 * filter pool to be determined by its filter pool manager.
	 */
	public void unsetStringsCaseSensitive();

	/**
	 * @return <b>true</b> if the StringsCaseSensitive attribute has been set
	 */
	public boolean isSetStringsCaseSensitive();

	/**
	 * Return the filters in this pool.
	 * 
	 * @return An array of filters in this pool
	 * @since org.eclipse.rse.core 3.0
	 */
	public ISystemFilter[] getFilters();

	/**
	 * @return The value of the SupportsDuplicateFilterStrings attribute
	 */
	public boolean isSupportsDuplicateFilterStrings();

	/**
	 * @param value The new value of the SupportsDuplicateFilterStrings attribute
	 */
	public void setSupportsDuplicateFilterStrings(boolean value);

	/**
	 * @return The value of the Release attribute
	 * The release in which this filter pool was initially created.
	 * Typically, will be the version and release times 10, as in 40 or 51.
	 */
	public int getRelease();

	/**
	 * @param value The new value of the Release attribute
	 */
	public void setRelease(int value);

	/**
	 * Returns the value of the '<em><b>Single Filter String Only</b></em>' attribute.
	 * <p>
	 * If true then filters in this filter pool can have only a single filter string unless the
	 * filter has overriden this attribute.
	 * </p>
	 * @return the value of the '<em>Single Filter String Only</em>' attribute.
	 * @see #isSetSingleFilterStringOnly()
	 * @see #unsetSingleFilterStringOnly()
	 * @see #setSingleFilterStringOnly(boolean)
	 */
	public boolean isSingleFilterStringOnly();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.core.filters.ISystemFilterPool#isSingleFilterStringOnly <em>Single Filter String Only</em>}' attribute.
	 * If set to true filters in this filter pool can hold only a single filter string unless overridden by the filter itself.
	 * If false then the filter may hold more than one filter string.
	 * @param value the new value of the '<em>Single Filter String Only</em>' attribute.
	 * @see #isSetSingleFilterStringOnly()
	 * @see #unsetSingleFilterStringOnly()
	 * @see #isSingleFilterStringOnly()
	 */
	public void setSingleFilterStringOnly(boolean value);

	/**
	 * Unsets the value of the '{@link org.eclipse.rse.core.filters.ISystemFilterPool#isSingleFilterStringOnly <em>Single Filter String Only</em>}' attribute.
	 * Causes the value of the single filter string attribute to be inherited from the containing filter pool manager.
	 * @see #isSetSingleFilterStringOnly()
	 * @see #isSingleFilterStringOnly()
	 * @see #setSingleFilterStringOnly(boolean)
	 */
	public void unsetSingleFilterStringOnly();

	/**
	 * Returns whether the value of the '{@link org.eclipse.rse.core.filters.ISystemFilterPool#isSingleFilterStringOnly <em>Single Filter String Only</em>}' attribute is set.
	 * This will be true if this attribute has been set for this filter pool. It will be false if this
	 * attribute is inherited from the filter pool manager.
	 * @return whether the value of the '<em>Single Filter String Only</em>' attribute is set.
	 * @see #unsetSingleFilterStringOnly()
	 * @see #isSingleFilterStringOnly()
	 * @see #setSingleFilterStringOnly(boolean)
	 */
	public boolean isSetSingleFilterStringOnly();

	/**
	 * Returns the value of the '<em><b>Owning Parent Name</b></em>' attribute.
	 * <p>
	 * If the meaning of the '<em>Owning Parent Name</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * @return the value of the '<em>Owning Parent Name</em>' attribute.
	 * @see #setOwningParentName(String)
	 */
	public String getOwningParentName();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.core.filters.ISystemFilterPool#getOwningParentName <em>Owning Parent Name</em>}' attribute.
	 * @param value the new value of the '<em>Owning Parent Name</em>' attribute.
	 * @see #getOwningParentName()
	 */
	public void setOwningParentName(String value);

	/**
	 * Returns the value of the '<em><b>Non Renamable</b></em>' attribute.
	 * <p>
	 * If the meaning of the '<em>Non Renamable</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * @return the value of the '<em>Non Renamable</em>' attribute.
	 * @see #setNonRenamable(boolean)
	 */
	public boolean isNonRenamable();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.core.filters.ISystemFilterPool#isNonRenamable <em>Non Renamable</em>}' attribute.
	 * @param value the new value of the '<em>Non Renamable</em>' attribute.
	 * @see #isNonRenamable()
	 */
	public void setNonRenamable(boolean value);

}
