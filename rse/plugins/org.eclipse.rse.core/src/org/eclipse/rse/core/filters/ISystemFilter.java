/*******************************************************************************
 * Copyright (c) 2003, 2008 IBM Corporation and others.
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
 *******************************************************************************/

package org.eclipse.rse.core.filters;

import java.util.List;
import java.util.Vector;

import org.eclipse.rse.core.model.IRSEModelObject;
import org.eclipse.rse.core.references.IRSEReferencedObject;

/**
 * A filter consists of filter strings and may be contained in a filter pool.
 * Filter pools are referenced by subsystems.
 * Subsystems are responsible for interpreting the filters. Filters, in and 
 * of themselves, provide no interpretation of themselves when applied 
 * to the resources managed by a subsystem.
 */

public interface ISystemFilter extends IRSEReferencedObject, ISystemFilterContainer, IRSEModelObject {

	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * This is the name of the filter. It may be present in the user interface and is also 
	 * used to refer to the filter when it is persisted.
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.core.filters.ISystemFilter#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * This is the name of the filter. It may be present in the user interface and is also 
	 * used to refer to the filter when it is persisted.
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * Filters may be further typed for use by subsystems. The type is also uninterpreted by the 
	 * filter. The type may be used to select a parser/interpreter for the filter strings.
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Type</em>' attribute.
	 * @see #setType(String)
	 */
	String getType();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.core.filters.ISystemFilter#getType <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * Filters may be further typed for use by subsystems. The type is also uninterpreted by the 
	 * filter. The type may be used to select a parser/interpreter for the filter strings.
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Type</em>' attribute.
	 * @see #getType()
	 */
	void setType(String value);

	/**
	 * Returns the value of the '<em><b>Supports Nested Filters</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * Specifies whether filters may be nested or not. If nested the intent is to apply this filter
	 * to the results of the parent filter - further restricting the resources selected by the 
	 * parent filter. However, it is up to the subsystem to interpret exactly what "nesting" means.
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Supports Nested Filters</em>' attribute.
	 * @see #setSupportsNestedFilters(boolean)
	 */
	boolean isSupportsNestedFilters();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.core.filters.ISystemFilter#isSupportsNestedFilters <em>Supports Nested Filters</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * Specifies whether filters may be nested or not. If nested the intent is to apply this filter
	 * to the results of the parent filter - further restricting the resources selected by the 
	 * parent filter. However, it is up to the subsystem to interpret exactly what "nesting" means.
	 * </p>
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Supports Nested Filters</em>' attribute.
	 * @see #isSupportsNestedFilters()
	 */
	void setSupportsNestedFilters(boolean value);

	/**
	 * Returns the value of the '<em><b>Relative Order</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * The relative order of a filter is intended to be used
	 * by a persistence mechanism to keep the filters in a particular order
	 * when restoring them into a filter pool. Filters with higher numbers should
	 * occur later in the pool. It may be employed in the absence of another
	 * mechanism for maintaining order.
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Relative Order</em>' attribute.
	 * @see #setRelativeOrder(int)
	 */
	int getRelativeOrder();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.core.filters.ISystemFilter#getRelativeOrder <em>Relative Order</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * The relative order of a filter is intended to be used
	 * by a persistence mechanism to keep the filters in a particular order
	 * when restoring them into a filter pool. Filters with higher numbers should
	 * occur later in the pool. It may be employed in the absence of another
	 * mechanism for maintaining order.
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Relative Order</em>' attribute.
	 * @see #getRelativeOrder()
	 */
	void setRelativeOrder(int value);

	/**
	 * Returns the value of the '<em><b>Default</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * This filter is a "default" filter in this filter pool.
	 * The meaning of "default" is determined by the subsystem in which it is deployed.
	 * It typically means that the filter is supplied by the subsystem at the time the subsystem is created.
	 * That is, the filter is "vendor supplied".
	 * There is no restriction on the number of default filters in a pool.
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Default</em>' attribute.
	 * @see #setDefault(boolean)
	 */
	boolean isDefault();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.core.filters.ISystemFilter#isDefault <em>Default</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * Make this filter is a "default" filter in this filter pool.
	 * The meaning of "default" is determined by the subsystem in which it is deployed.
	 * It typically means that the filter is supplied by the subsystem at the time the subsystem is created.
	 * That is, the filter is "vendor supplied".
	 * There is no restriction on the number of default filters in a pool.
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Default</em>' attribute.
	 * @see #isDefault()
	 */
	void setDefault(boolean value);

	/**
	 * Returns the value of the '<em><b>Strings Case Sensitive</b></em>' attribute.
	 * <!-- begin-user-doc -->
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
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Strings Case Sensitive</em>' attribute.
	 * @see #isSetStringsCaseSensitive()
	 * @see #unsetStringsCaseSensitive()
	 * @see #setStringsCaseSensitive(boolean)
	 */
	boolean isStringsCaseSensitive();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.core.filters.ISystemFilter#isStringsCaseSensitive <em>Strings Case Sensitive</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * An attribute that may be used by the subystems when interpreting the filter. Used
	 * to indicate whether or not comparisons involving the filter should be considered
	 * case sensitive.
	 * This attribute is optional and may be known (set) or unknown (unset).
	 * </p>
	 * <p>
	 * This will cause this attribute to be set in this filter.
	 * </p>
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Strings Case Sensitive</em>' attribute.
	 * @see #isSetStringsCaseSensitive()
	 * @see #unsetStringsCaseSensitive()
	 * @see #isStringsCaseSensitive()
	 */
	void setStringsCaseSensitive(boolean value);

	/**
	 * Unsets the value of the '{@link org.eclipse.rse.core.filters.ISystemFilter#isStringsCaseSensitive <em>Strings Case Sensitive</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * An attribute that may be used by the subystems when interpreting the filter. Used
	 * to indicate whether or not comparisons involving the filter should be considered
	 * case sensitive.
	 * This attribute is optional and may be known (set) or unknown (unset).
	 * </p><p>
	 * Causes this filter to use the value specified by the parent filter pool.
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #isSetStringsCaseSensitive()
	 * @see #isStringsCaseSensitive()
	 * @see #setStringsCaseSensitive(boolean)
	 */
	void unsetStringsCaseSensitive();

	/**
	 * Returns whether the value of the '{@link org.eclipse.rse.core.filters.ISystemFilter#isStringsCaseSensitive <em>Strings Case Sensitive</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <p>
	 * An attribute that may be used by the subystems when interpreting the filter. Used
	 * to indicate whether or not comparisons involving the filter should be considered
	 * case sensitive.
	 * This attribute is optional and may be known (set) or unknown (unset).
	 * </p><p>
	 * This will be true if this attribute is explicitly specified for this filter. It will be false if this
	 * is being inherited from the parent filter pool.
	 * </p>
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Strings Case Sensitive</em>' attribute is set.
	 * @see #unsetStringsCaseSensitive()
	 * @see #isStringsCaseSensitive()
	 * @see #setStringsCaseSensitive(boolean)
	 */
	boolean isSetStringsCaseSensitive();

	/**
	 * Returns the value of the '<em><b>Promptable</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * An attribute that may be used by the subystems when interpreting the filter. 
	 * Typically used to indicate whether or not some sort of prompting is to occur when the
	 * filter is used. Typically used when applying a filter in the UI to indicate
	 * the start of a wizard or dialog. It may also have an effect on whether 
	 * the state of the filter is saved when the workbench is shut down.
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Promptable</em>' attribute.
	 * @see #setPromptable(boolean)
	 */
	boolean isPromptable();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.core.filters.ISystemFilter#isPromptable <em>Promptable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * An attribute that may be used by the subystems when interpreting the filter. 
	 * Typically used to indicate whether or not some sort of prompting is to occur when the
	 * filter is used. Typically used when applying a filter in the UI to indicate
	 * the start of a wizard or dialog. It may also have an effect on whether 
	 * the state of the filter is saved when the workbench is shut down.
	 * </p>
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Promptable</em>' attribute.
	 * @see #isPromptable()
	 */
	void setPromptable(boolean value);

	/**
	 * Returns the value of the '<em><b>Supports Duplicate Filter Strings</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * This attribute may be used by subsystems when interpreting the filter.
	 * Typically used when adding filter strings to the filter or as a hint when
	 * applying the filter to the resources understood by the subsystem.
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Supports Duplicate Filter Strings</em>' attribute.
	 * @see #setSupportsDuplicateFilterStrings(boolean)
	 */
	boolean isSupportsDuplicateFilterStrings();

	/**
	 * Does this support duplicate filter strings?
	 * A convenience method for {@link #isSupportsDuplicateFilterStrings()}.
	 */
	public boolean supportsDuplicateFilterStrings();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.core.filters.ISystemFilter#isSupportsDuplicateFilterStrings <em>Supports Duplicate Filter Strings</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * This attribute may be used by subsystems when interpreting the filter.
	 * Typically used when adding filter strings to the filter or as a hint when
	 * applying the filter to the resources understood by the subsystem.
	 * </p>
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Supports Duplicate Filter Strings</em>' attribute.
	 * @see #isSupportsDuplicateFilterStrings()
	 */
	void setSupportsDuplicateFilterStrings(boolean value);

	/**
	 * Returns the value of the '<em><b>Non Deletable</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * An attribute that can be used when managing filters in filter pools.
	 * Some filters should not be deleted.
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Non Deletable</em>' attribute.
	 * @see #setNonDeletable(boolean)
	 */
	boolean isNonDeletable();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.core.filters.ISystemFilter#isNonDeletable <em>Non Deletable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * An attribute that can be used when managing filters in filter pools.
	 * Some filters should not be deleted.
	 * </p>
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Non Deletable</em>' attribute.
	 * @see #isNonDeletable()
	 */
	void setNonDeletable(boolean value);

	/**
	 * Returns the value of the '<em><b>Non Renamable</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * An attribute that can be used when managing filters in filter pools.
	 * Some filters should not be renamed.
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Non Renamable</em>' attribute.
	 * @see #setNonRenamable(boolean)
	 */
	boolean isNonRenamable();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.core.filters.ISystemFilter#isNonRenamable <em>Non Renamable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * An attribute that can be used when managing filters in filter pools.
	 * Some filters should not be renamed.
	 * </p>
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Non Renamable</em>' attribute.
	 * @see #isNonRenamable()
	 */
	void setNonRenamable(boolean value);

	/**
	 * Returns the value of the '<em><b>Non Changable</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * An attribute that can be used when managing filters in filter pools.
	 * Some filters should not be modifiable.
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Non Changable</em>' attribute.
	 * @see #setNonChangable(boolean)
	 */
	boolean isNonChangable();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.core.filters.ISystemFilter#isNonChangable <em>Non Changable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * An attribute that can be used when managing filters in filter pools.
	 * Some filters should not be modifiable.
	 * </p>
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Non Changable</em>' attribute.
	 * @see #isNonChangable()
	 */
	void setNonChangable(boolean value);

	/**
	 * Returns the value of the '<em><b>Strings Non Changable</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * An attribute that can be used when managing filters in filter pools.
	 * Some filters contain filter strings that should not be modifiable.
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Strings Non Changable</em>' attribute.
	 * @see #setStringsNonChangable(boolean)
	 */
	boolean isStringsNonChangable();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.core.filters.ISystemFilter#isStringsNonChangable <em>Strings Non Changable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * An attribute that can be used when managing filters in filter pools.
	 * Some filters contain filter strings that should not be modifiable.
	 * </p>
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Strings Non Changable</em>' attribute.
	 * @see #isStringsNonChangable()
	 */
	void setStringsNonChangable(boolean value);

	/**
	 * Returns the value of the '<em><b>Release</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * This is an attribute specifying the release level of the  
	 * filter. This will be persisted in the filter definition and
	 * can be used to migrate the internal form of the filter.
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Release</em>' attribute.
	 * @see #setRelease(int)
	 */
	int getRelease();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.core.filters.ISystemFilter#getRelease <em>Release</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * This is an attribute specifying the release level of the  
	 * filter. This will be persisted in the filter definition and
	 * can be used to migrate the internal form of the filter.
	 * </p>
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Release</em>' attribute.
	 * @see #getRelease()
	 */
	void setRelease(int value);

	/**
	 * Returns the value of the '<em><b>Single Filter String Only</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * This attribute specifies that the filter may contain only a single 
	 * filter string. Used by a filter manager to ensure that the 
	 * filter contains only one string. May, alternatively, be enforced
	 * by the filter implementation.
	 * </p><p>
	 * This attribute may be set or unset. If unset this value will be inherited
	 * from the parent filter pool.
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Single Filter String Only</em>' attribute.
	 * @see #isSetSingleFilterStringOnly()
	 * @see #unsetSingleFilterStringOnly()
	 * @see #setSingleFilterStringOnly(boolean)
	 */
	boolean isSingleFilterStringOnly();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.core.filters.ISystemFilter#isSingleFilterStringOnly <em>Single Filter String Only</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * This attribute specifies that the filter may contain only a single 
	 * filter string. Used by a filter manager to ensure that the 
	 * filter contains only one string. May, alternatively, be enforced
	 * by the filter implementation.
	 * </p><p>
	 * This causes the attribute to be set for this filter, ignoring the value 
	 * specified for the parent filter pool.
	 * </p>
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Single Filter String Only</em>' attribute.
	 * @see #isSetSingleFilterStringOnly()
	 * @see #unsetSingleFilterStringOnly()
	 * @see #isSingleFilterStringOnly()
	 */
	void setSingleFilterStringOnly(boolean value);

	/**
	 * Unsets the value of the '{@link org.eclipse.rse.core.filters.ISystemFilter#isSingleFilterStringOnly <em>Single Filter String Only</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * This causes the value of this attribute to be inherited from the parent filter pool.
	 * <!-- end-user-doc -->
	 * @see #isSetSingleFilterStringOnly()
	 * @see #isSingleFilterStringOnly()
	 * @see #setSingleFilterStringOnly(boolean)
	 */
	void unsetSingleFilterStringOnly();

	/**
	 * Returns whether the value of the '{@link org.eclipse.rse.core.filters.ISystemFilter#isSingleFilterStringOnly <em>Single Filter String Only</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * If true then the value of this attribute is set in this filter. If false it is inherited from the parent filter pool.
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Single Filter String Only</em>' attribute is set.
	 * @see #unsetSingleFilterStringOnly()
	 * @see #isSingleFilterStringOnly()
	 * @see #setSingleFilterStringOnly(boolean)
	 */
	boolean isSetSingleFilterStringOnly();

	/**
	 * Returns the value of the '<em><b>Nested Filters</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.rse.core.filters.ISystemFilter}.
	 * It is bidirectional and its opposite is '{@link org.eclipse.rse.core.filters.ISystemFilter#getParentFilter <em>Parent Filter</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If this filter can contain child filters this will return the list of children.
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Nested Filters</em>' containment reference list.
	 * @see org.eclipse.rse.core.filters.ISystemFilter#getParentFilter
	 */
	List getNestedFilters();

	/**
	 * Returns the value of the '<em><b>Parent Filter</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link org.eclipse.rse.core.filters.ISystemFilter#getNestedFilters <em>Nested Filters</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If this filter can be nested inside another this will return the parent filter. Will be 
	 * null if there is no parent.
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Parent Filter</em>' container reference.
	 * @see #setParentFilter(ISystemFilter)
	 * @see org.eclipse.rse.core.filters.ISystemFilter#getNestedFilters
	 */
	ISystemFilter getParentFilter();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.core.filters.ISystemFilter#getParentFilter <em>Parent Filter</em>}' container reference.
	 * <!-- begin-user-doc -->
	 * This is used by a filter manager to set the parent filter when one filter is nested inside another.
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Parent Filter</em>' container reference.
	 * @see #getParentFilter()
	 */
	void setParentFilter(ISystemFilter value);

	/**
	 * Returns the value of the '<em><b>Strings</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.rse.core.filters.ISystemFilterString}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Strings</em>' containment reference list.
	 */
	List getStrings();

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
	 * Set this filter's filter strings by giving a Vector of String objects.
	 * This will construct the filter strings objects.
	 * @param strings the vector of String objects.
	 */
	public void setFilterStrings(Vector strings);

	/**
	 * Set this filter's filter strings by giving an array of String objects.
	 * This will construct the filter strings objects.
	 * @param strings the array of String objects.
	 */
	public void setFilterStrings(String[] strings);

	/**
	 * @return this filter's filter strings as a Vector of String objects
	 */
	public Vector getFilterStringsVector();

	/**
	 * @return this filter's filter strings as a Vector of IFilterString objects
	 */
	public Vector getFilterStringObjectsVector();

	/**
	 * @return this filter's filter strings as an array of String objects
	 */
	public String[] getFilterStrings();

	/**
	 * @return this filter's filter string objects as an array of IFilterString objects
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
