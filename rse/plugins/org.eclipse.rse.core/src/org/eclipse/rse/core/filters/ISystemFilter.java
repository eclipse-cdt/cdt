/*******************************************************************************
 * Copyright (c) 2003, 2009 IBM Corporation and others.
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
 * David Dykstal (IBM) - cleanup format and javadoc
 * Martin Oberhuber (Wind River) - [cleanup] Add API "since" Javadoc tags
 * David Dykstal (IBM) - [224671] [api] org.eclipse.rse.core API leaks non-API types
 * David Dykstal (IBM) - [226561] Add API markup to RSE Javadocs where extend / implement is allowed
 * David Dykstal (IBM) - [261486][api] add noextend to interfaces that require it
 *******************************************************************************/

package org.eclipse.rse.core.filters;

import org.eclipse.rse.core.model.IRSEModelObject;
import org.eclipse.rse.core.references.IRSEReferencedObject;
import org.eclipse.rse.core.subsystems.ISubSystem;

/**
 * A filter consists of filter strings and may be contained in a filter pool.
 * Filter pools are referenced by subsystems. Subsystems are responsible for
 * interpreting the filters. Filters, in and of themselves, provide no
 * interpretation of themselves when applied to the resources managed by a
 * subsystem.
 * 
 * @noimplement This interface is not intended to be implemented by clients. The
 *              allowable implementations already present in the framework.
 * @noextend This interface is not intended to be extended by clients.
 */

public interface ISystemFilter extends IRSEReferencedObject, ISystemFilterContainer, IRSEModelObject {

	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <p>
	 * This is the name of the filter. It may be present in the user interface and is also
	 * used to refer to the filter when it is persisted.
	 * </p>
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 */
	public String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.core.filters.ISystemFilter#getName <em>Name</em>}' attribute.
	 * This is the name of the filter. It may be present in the user interface and is also
	 * used to refer to the filter when it is persisted.
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 */
	public void setName(String value);

	/**
	 * Returns the value of the '<em><b>Type</b></em>' attribute.
	 * <p>
	 * Filters may be further typed for use by subsystems. The type is also uninterpreted by the
	 * filter. The type may be used to select a parser/interpreter for the filter strings.
	 * </p>
	 * @return the value of the '<em>Type</em>' attribute.
	 * @see #setType(String)
	 */
	public String getType();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.core.filters.ISystemFilter#getType <em>Type</em>}' attribute.
	 * Filters may be further typed for use by subsystems. The type is also uninterpreted by the
	 * filter. The type may be used to select a parser/interpreter for the filter strings.
	 * @param value the new value of the '<em>Type</em>' attribute.
	 * @see #getType()
	 */
	public void setType(String value);

	/**
	 * Returns the value of the '<em><b>Supports Nested Filters</b></em>' attribute.
	 * <p>
	 * Specifies whether filters may be nested or not. If nested the intent is to apply this filter
	 * to the results of the parent filter - further restricting the resources selected by the
	 * parent filter. However, it is up to the subsystem to interpret exactly what "nesting" means.
	 * </p>
	 * @return the value of the '<em>Supports Nested Filters</em>' attribute.
	 * @see #setSupportsNestedFilters(boolean)
	 */
	public boolean isSupportsNestedFilters();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.core.filters.ISystemFilter#isSupportsNestedFilters <em>Supports Nested Filters</em>}' attribute.
	 * <p>
	 * Specifies whether filters may be nested or not. If nested the intent is to apply this filter
	 * to the results of the parent filter - further restricting the resources selected by the
	 * parent filter. However, it is up to the subsystem to interpret exactly what "nesting" means.
	 * </p>
	 * @param value the new value of the '<em>Supports Nested Filters</em>' attribute.
	 * @see #isSupportsNestedFilters()
	 */
	public void setSupportsNestedFilters(boolean value);

	/**
	 * Returns the value of the '<em><b>Relative Order</b></em>' attribute.
	 * <p>
	 * The relative order of a filter is intended to be used
	 * by a persistence mechanism to keep the filters in a particular order
	 * when restoring them into a filter pool. Filters with higher numbers should
	 * occur later in the pool. It may be employed in the absence of another
	 * mechanism for maintaining order.
	 * </p>
	 * @return the value of the '<em>Relative Order</em>' attribute.
	 * @see #setRelativeOrder(int)
	 */
	public int getRelativeOrder();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.core.filters.ISystemFilter#getRelativeOrder <em>Relative Order</em>}' attribute.
	 * The relative order of a filter is intended to be used
	 * by a persistence mechanism to keep the filters in a particular order
	 * when restoring them into a filter pool. Filters with higher numbers should
	 * occur later in the pool. It may be employed in the absence of another
	 * mechanism for maintaining order.
	 * @param value the new value of the '<em>Relative Order</em>' attribute.
	 * @see #getRelativeOrder()
	 */
	public void setRelativeOrder(int value);

	/**
	 * Returns the value of the '<em><b>Default</b></em>' attribute.
	 * <p>
	 * This filter is a "default" filter in this filter pool.
	 * The meaning of "default" is determined by the subsystem in which it is deployed.
	 * It typically means that the filter is supplied by the subsystem at the time the subsystem is created.
	 * That is, the filter is "vendor supplied".
	 * There is no restriction on the number of default filters in a pool.
	 * </p>
	 * @return the value of the '<em>Default</em>' attribute.
	 * @see #setDefault(boolean)
	 */
	public boolean isDefault();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.core.filters.ISystemFilter#isDefault <em>Default</em>}' attribute.
	 * Make this filter is a "default" filter in this filter pool.
	 * The meaning of "default" is determined by the subsystem in which it is deployed.
	 * It typically means that the filter is supplied by the subsystem at the time the subsystem is created.
	 * That is, the filter is "vendor supplied".
	 * There is no restriction on the number of default filters in a pool.
	 * @param value the new value of the '<em>Default</em>' attribute.
	 * @see #isDefault()
	 */
	public void setDefault(boolean value);

	/**
	 * Returns the value of the '<em><b>Strings Case Sensitive</b></em>' attribute.
	 * <p>
	 * An attribute that may be used by the subystems when interpreting the filter. Used
	 * to indicate whether or not comparisons involving the filter should be considered
	 * case sensitive.
	 * This attribute is optional and may be known (set) or unknown (unset).
	 * </p>
	 * <p>
	 * If unset the value
	 * returned is from the parent filter pool.
	 * </p>
	 * @return the value of the '<em>Strings Case Sensitive</em>' attribute.
	 * @see #isSetStringsCaseSensitive()
	 * @see #unsetStringsCaseSensitive()
	 * @see #setStringsCaseSensitive(boolean)
	 */
	public boolean isStringsCaseSensitive();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.core.filters.ISystemFilter#isStringsCaseSensitive <em>Strings Case Sensitive</em>}' attribute.
	 * <p>
	 * An attribute that may be used by the subystems when interpreting the filter. Used
	 * to indicate whether or not comparisons involving the filter should be considered
	 * case sensitive.
	 * This attribute is optional and may be known (set) or unknown (unset).
	 * </p>
	 * <p>
	 * This will cause this attribute to be set in this filter.
	 * </p>
	 * @param value the new value of the '<em>Strings Case Sensitive</em>' attribute.
	 * @see #isSetStringsCaseSensitive()
	 * @see #unsetStringsCaseSensitive()
	 * @see #isStringsCaseSensitive()
	 */
	void setStringsCaseSensitive(boolean value);

	/**
	 * Unsets the value of the '{@link org.eclipse.rse.core.filters.ISystemFilter#isStringsCaseSensitive <em>Strings Case Sensitive</em>}' attribute.
	 * <p>
	 * An attribute that may be used by the subystems when interpreting the filter. Used
	 * to indicate whether or not comparisons involving the filter should be considered
	 * case sensitive.
	 * This attribute is optional and may be known (set) or unknown (unset).
	 * </p><p>
	 * Causes this filter to use the value specified by the parent filter pool.
	 * </p>
	 * @see #isSetStringsCaseSensitive()
	 * @see #isStringsCaseSensitive()
	 * @see #setStringsCaseSensitive(boolean)
	 */
	public void unsetStringsCaseSensitive();

	/**
	 * Returns whether the value of the '{@link org.eclipse.rse.core.filters.ISystemFilter#isStringsCaseSensitive <em>Strings Case Sensitive</em>}' attribute is set.
	 * <p>
	 * An attribute that may be used by the subystems when interpreting the filter. Used
	 * to indicate whether or not comparisons involving the filter should be considered
	 * case sensitive.
	 * This attribute is optional and may be known (set) or unknown (unset).
	 * </p><p>
	 * This will be true if this attribute is explicitly specified for this filter. It will be false if this
	 * is being inherited from the parent filter pool.
	 * </p>
	 * @return whether the value of the '<em>Strings Case Sensitive</em>' attribute is set.
	 * @see #unsetStringsCaseSensitive()
	 * @see #isStringsCaseSensitive()
	 * @see #setStringsCaseSensitive(boolean)
	 */
	public boolean isSetStringsCaseSensitive();

	/**
	 * Returns the value of the '<em><b>Promptable</b></em>' attribute.
	 * <p>
	 * An attribute that may be used by the subystems when interpreting the filter.
	 * Typically used to indicate whether or not some sort of prompting is to occur when the
	 * filter is used. Typically used when applying a filter in the UI to indicate
	 * the start of a wizard or dialog. It may also have an effect on whether
	 * the state of the filter is saved when the workbench is shut down.
	 * </p>
	 * @return the value of the '<em>Promptable</em>' attribute.
	 * @see #setPromptable(boolean)
	 */
	public boolean isPromptable();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.core.filters.ISystemFilter#isPromptable <em>Promptable</em>}' attribute.
	 * <p>
	 * An attribute that may be used by the subystems when interpreting the filter.
	 * Typically used to indicate whether or not some sort of prompting is to occur when the
	 * filter is used. Typically used when applying a filter in the UI to indicate
	 * the start of a wizard or dialog. It may also have an effect on whether
	 * the state of the filter is saved when the workbench is shut down.
	 * </p>
	 * @param value the new value of the '<em>Promptable</em>' attribute.
	 * @see #isPromptable()
	 */
	public void setPromptable(boolean value);

	/**
	 * Returns the value of the '<em><b>Supports Duplicate Filter Strings</b></em>' attribute.
	 * <p>
	 * This attribute may be used by subsystems when interpreting the filter.
	 * Typically used when adding filter strings to the filter or as a hint when
	 * applying the filter to the resources understood by the subsystem.
	 * </p>
	 * @return the value of the '<em>Supports Duplicate Filter Strings</em>' attribute.
	 * @see #setSupportsDuplicateFilterStrings(boolean)
	 */
	public boolean isSupportsDuplicateFilterStrings();

	/**
	 * Does this support duplicate filter strings?
	 * A convenience method for {@link #isSupportsDuplicateFilterStrings()}.
	 */
	public boolean supportsDuplicateFilterStrings();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.core.filters.ISystemFilter#isSupportsDuplicateFilterStrings <em>Supports Duplicate Filter Strings</em>}' attribute.
	 * <p>
	 * This attribute may be used by subsystems when interpreting the filter.
	 * Typically used when adding filter strings to the filter or as a hint when
	 * applying the filter to the resources understood by the subsystem.
	 * </p>
	 * @param value the new value of the '<em>Supports Duplicate Filter Strings</em>' attribute.
	 * @see #isSupportsDuplicateFilterStrings()
	 */
	public void setSupportsDuplicateFilterStrings(boolean value);

	/**
	 * Returns the value of the '<em><b>Non Deletable</b></em>' attribute.
	 * <p>
	 * An attribute that can be used when managing filters in filter pools.
	 * Some filters should not be deleted.
	 * </p>
	 * @return the value of the '<em>Non Deletable</em>' attribute.
	 * @see #setNonDeletable(boolean)
	 */
	public boolean isNonDeletable();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.core.filters.ISystemFilter#isNonDeletable <em>Non Deletable</em>}' attribute.
	 * <p>
	 * An attribute that can be used when managing filters in filter pools.
	 * Some filters should not be deleted.
	 * </p>
	 * @param value the new value of the '<em>Non Deletable</em>' attribute.
	 * @see #isNonDeletable()
	 */
	public void setNonDeletable(boolean value);

	/**
	 * Returns the value of the '<em><b>Non Renamable</b></em>' attribute.
	 * <p>
	 * An attribute that can be used when managing filters in filter pools.
	 * Some filters should not be renamed.
	 * </p>
	 * @return the value of the '<em>Non Renamable</em>' attribute.
	 * @see #setNonRenamable(boolean)
	 */
	public boolean isNonRenamable();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.core.filters.ISystemFilter#isNonRenamable <em>Non Renamable</em>}' attribute.
	 * <p>
	 * An attribute that can be used when managing filters in filter pools.
	 * Some filters should not be renamed.
	 * </p>
	 * @param value the new value of the '<em>Non Renamable</em>' attribute.
	 * @see #isNonRenamable()
	 */
	public void setNonRenamable(boolean value);

	/**
	 * Returns the value of the '<em><b>Non Changable</b></em>' attribute.
	 * <p>
	 * An attribute that can be used when managing filters in filter pools.
	 * Some filters should not be modifiable.
	 * </p>
	 * @return the value of the '<em>Non Changable</em>' attribute.
	 * @see #setNonChangable(boolean)
	 */
	public boolean isNonChangable();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.core.filters.ISystemFilter#isNonChangable <em>Non Changable</em>}' attribute.
	 * <p>
	 * An attribute that can be used when managing filters in filter pools.
	 * Some filters should not be modifiable.
	 * </p>
	 * @param value the new value of the '<em>Non Changable</em>' attribute.
	 * @see #isNonChangable()
	 */
	public void setNonChangable(boolean value);

	/**
	 * Returns the value of the '<em><b>Strings Non Changable</b></em>' attribute.
	 * <p>
	 * An attribute that can be used when managing filters in filter pools.
	 * Some filters contain filter strings that should not be modifiable.
	 * </p>
	 * @return the value of the '<em>Strings Non Changable</em>' attribute.
	 * @see #setStringsNonChangable(boolean)
	 */
	public boolean isStringsNonChangable();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.core.filters.ISystemFilter#isStringsNonChangable <em>Strings Non Changable</em>}' attribute.
	 * <p>
	 * An attribute that can be used when managing filters in filter pools.
	 * Some filters contain filter strings that should not be modifiable.
	 * </p>
	 * @param value the new value of the '<em>Strings Non Changable</em>' attribute.
	 * @see #isStringsNonChangable()
	 */
	public void setStringsNonChangable(boolean value);

	/**
	 * Returns the value of the '<em><b>Release</b></em>' attribute.
	 * <p>
	 * This is an attribute specifying the release level of the
	 * filter. This will be persisted in the filter definition and
	 * can be used to migrate the internal form of the filter.
	 * </p>
	 * @return the value of the '<em>Release</em>' attribute.
	 * @see #setRelease(int)
	 */
	public int getRelease();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.core.filters.ISystemFilter#getRelease <em>Release</em>}' attribute.
	 * <p>
	 * This is an attribute specifying the release level of the
	 * filter. This will be persisted in the filter definition and
	 * can be used to migrate the internal form of the filter.
	 * </p>
	 * @param value the new value of the '<em>Release</em>' attribute.
	 * @see #getRelease()
	 */
	public void setRelease(int value);

	/**
	 * Returns the value of the '<em><b>Single Filter String Only</b></em>' attribute.
	 * <p>
	 * This attribute specifies that the filter may contain only a single
	 * filter string. Used by a filter manager to ensure that the
	 * filter contains only one string. May, alternatively, be enforced
	 * by the filter implementation.
	 * </p><p>
	 * This attribute may be set or unset. If unset this value will be inherited
	 * from the parent filter pool.
	 * </p>
	 * @return the value of the '<em>Single Filter String Only</em>' attribute.
	 * @see #isSetSingleFilterStringOnly()
	 * @see #unsetSingleFilterStringOnly()
	 * @see #setSingleFilterStringOnly(boolean)
	 */
	public boolean isSingleFilterStringOnly();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.core.filters.ISystemFilter#isSingleFilterStringOnly <em>Single Filter String Only</em>}' attribute.
	 * <p>
	 * This attribute specifies that the filter may contain only a single
	 * filter string. Used by a filter manager to ensure that the
	 * filter contains only one string. May, alternatively, be enforced
	 * by the filter implementation.
	 * </p><p>
	 * This causes the attribute to be set for this filter, ignoring the value
	 * specified for the parent filter pool.
	 * </p>
	 * @param value the new value of the '<em>Single Filter String Only</em>' attribute.
	 * @see #isSetSingleFilterStringOnly()
	 * @see #unsetSingleFilterStringOnly()
	 * @see #isSingleFilterStringOnly()
	 */
	public void setSingleFilterStringOnly(boolean value);

	/**
	 * Unsets the value of the '{@link org.eclipse.rse.core.filters.ISystemFilter#isSingleFilterStringOnly <em>Single Filter String Only</em>}' attribute.
	 * This causes the value of this attribute to be inherited from the parent filter pool.
	 * @see #isSetSingleFilterStringOnly()
	 * @see #isSingleFilterStringOnly()
	 * @see #setSingleFilterStringOnly(boolean)
	 */
	public void unsetSingleFilterStringOnly();

	/**
	 * Returns whether the value of the '{@link org.eclipse.rse.core.filters.ISystemFilter#isSingleFilterStringOnly <em>Single Filter String Only</em>}' attribute is set.
	 * If true then the value of this attribute is set in this filter. If false it is inherited from the parent filter pool.
	 * @return whether the value of the '<em>Single Filter String Only</em>' attribute is set.
	 * @see #unsetSingleFilterStringOnly()
	 * @see #isSingleFilterStringOnly()
	 * @see #setSingleFilterStringOnly(boolean)
	 */
	public boolean isSetSingleFilterStringOnly();

	/**
	 * Returns the value of the '<em><b>Nested Filters</b></em>'
	 * containment reference list. It is bidirectional and its opposite is '{@link org.eclipse.rse.core.filters.ISystemFilter#getParentFilter <em>Parent Filter</em>}'.
	 * <p>
	 * If this filter can contain child filters this will return the list of
	 * children.
	 * </p>
	 *
	 * @return the value of the '<em>Nested Filters</em>' containment
	 *         reference list.
	 * @see org.eclipse.rse.core.filters.ISystemFilter#getParentFilter
	 * @since org.eclipse.rse.core 3.0
	 */
	public ISystemFilter[] getNestedFilters();

	/**
	 * Returns the value of the '<em><b>Parent Filter</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link org.eclipse.rse.core.filters.ISystemFilter#getNestedFilters <em>Nested Filters</em>}'.
	 * <p>
	 * If this filter can be nested inside another this will return the parent filter. Will be
	 * null if there is no parent.
	 * </p>
	 * @return the value of the '<em>Parent Filter</em>' container reference.
	 * @see #setParentFilter(ISystemFilter)
	 * @see org.eclipse.rse.core.filters.ISystemFilter#getNestedFilters
	 */
	public ISystemFilter getParentFilter();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.core.filters.ISystemFilter#getParentFilter <em>Parent Filter</em>}' container reference.
	 * This is used by a filter manager to set the parent filter when one filter is nested inside another.
	 * @param value the new value of the '<em>Parent Filter</em>' container reference.
	 * @see #getParentFilter()
	 */
	public void setParentFilter(ISystemFilter value);

	/**
	 * Returns the value of the '<em><b>Strings</b></em>' containment
	 * reference list.
	 *
	 * @return the value of the '<em>Strings</em>' containment reference
	 *         list.
	 * @since org.eclipse.rse.core 3.0
	 */
	public ISystemFilterString[] getStrings();

	/**
	 * @return the parent pool of this filter. For nested filters, this will walk up the parent chain
	 * until we find the pool. May return null if this is a simple filter that has no parent pool.
	 */
	public ISystemFilterPool getParentFilterPool();

	/**
	 * Internal use method to set the parent filter pool. This should be invoked only by a filter manager
	 * or a filter pool manager when adding a filter to a filter pool.
	 * @param parentPool the filter pool that contains or will contain this filter.
	 */
	public void setParentFilterPool(ISystemFilterPool parentPool);

	/**
	 * Set this filter's filter strings by giving an array of String objects.
	 * This will construct the filter strings objects.
	 * @param strings the array of String objects.
	 */
	public void setFilterStrings(String[] strings);

	/**
	 * @return this filter's filter strings as an array of String objects. This array will
	 * not be null, but may be empty.
	 */
	public String[] getFilterStrings();

	/**
	 * @return this filter's filter string objects as an array of IFilterString objects.
	 * This array will not be null, but may be empty.
	 */
	public ISystemFilterString[] getSystemFilterStrings();

	/**
	 * @return the number of filter strings this filter currently contains.
	 */
	public int getFilterStringCount();

	/**
	 * @return a filter string given its string value. This will be null if there
	 * is no string matching the argument.
	 */
	public ISystemFilterString getSystemFilterString(String string);

	/**
	 * Append a new filter string to this filter's list of filter strings.
	 * This will construct a filter string object.
	 * @param newString the string to append
	 */
	public ISystemFilterString addFilterString(String newString);

	/**
	 * Insert a new filter string to this filter's list, at the given zero-based position.
	 * Thsi will construct a filter string object.
	 * @param newString the string from which to construct the filter string to be added.
	 * @param position the zero-based position at which to add the string.
	 */
	public ISystemFilterString addFilterString(String newString, int position);

	/**
	 * Update a new filter string's string value.
	 * The filter string need not belong to this filter.
	 * @param filterString the string update.
	 * @param newValue the new value of that string.
	 */
	public void updateFilterString(ISystemFilterString filterString, String newValue);

	/**
	 * Delete a filter string from this filter's list.
	 * @param oldString the string to remove
	 * @return the SystemFilterString object deleted, or null if not found
	 */
	public ISystemFilterString removeFilterString(String oldString);

	/**
	 * Remove a filter string from this filter's list, given its zero-based position
	 * @param position the position of the filter string.
	 * @return the SystemFilterString object deleted, or null if not found
	 */
	public ISystemFilterString removeFilterString(int position);

	/**
	 * Remove a filter string from this filter's list, given its SystemFilterString object.
	 * The specific filter string will be removed, based on object identity.
	 * @param filterString the filterString to be removed.
	 * @return true if the given string existed and hence was deleted.
	 */
	public boolean removeFilterString(ISystemFilterString filterString);

	/**
	 * Move a given filter string to a given zero-based location. This will
	 * do nothing if the argument is not a string already contained by this filter.
	 * @param pos the new position of this filter string
	 * @param filterString the filter string to move
	 */
	public void moveSystemFilterString(int pos, ISystemFilterString filterString);

	/**
	 * Copies a given filter string from this filter to another filter in this pool or another pool
	 * in this manager or another manager. Will do nothing if the filter string is not
	 * originally contained in this filter.
	 * @param targetFilter the filter to which to copy the filter string
	 * @param oldFilterString the filter string to copy
	 */
	public ISystemFilterString copySystemFilterString(ISystemFilter targetFilter, ISystemFilterString oldFilterString);

	/**
	 * @return true if this filter is a nested filter or not. If not, its parent is the filter pool.
	 */
	public boolean isNested();

	/**
	 * @return true if this a transient or simple filter that is not intended to be
	 * saved or part of the filter framework. It will have no manager or provider.
	 */
	public boolean isTransient();

	/**
	 * Set the subsytem of this filter.
	 * This is ignored if the filter is not transient.
	 * @param subsystem a subsystem associated with this transient filter
	 * @since 3.0
	 */
	public void setSubSystem(ISubSystem subsystem);

	/**
	 * Get the subsystem for this filter.
	 * This will return null if the filter is not transient.
	 * @return the subsystem
	 * @since 3.0
	 */
	public Object getSubSystem();

	/**
	 * Clones a given filter to the given target filter.
	 * All filter strings, and all nested filters, are copied.
	 * Typically used when copying a whole filter.
	 * @param targetFilter new filter into which all the data of this filter will be copied.
	 */
	public void clone(ISystemFilter targetFilter);

	/**
	 * @return the ISystemFilterContainer parent of this filter. This will be either
	 * an ISystemFilterPool or an ISystemFilter if this is a nested filter.
	 */
	public ISystemFilterContainer getParentFilterContainer();

	/**
	 * @return the caller which instantiated the filter pool manager overseeing this filter framework instance.
	 * This will typically be a subsystem configuration.
	 */
	public ISystemFilterPoolManagerProvider getProvider();

}
