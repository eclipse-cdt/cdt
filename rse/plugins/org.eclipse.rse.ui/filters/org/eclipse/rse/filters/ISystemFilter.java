/********************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation. All rights reserved.
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
import java.util.Vector;

import org.eclipse.rse.core.references.IRSEReferencedObject;
import org.eclipse.rse.model.IRSEModelObject;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>System Filter</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.rse.filters.ISystemFilter#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.rse.filters.ISystemFilter#getType <em>Type</em>}</li>
 *   <li>{@link org.eclipse.rse.filters.ISystemFilter#isSupportsNestedFilters <em>Supports Nested Filters</em>}</li>
 *   <li>{@link org.eclipse.rse.filters.ISystemFilter#getRelativeOrder <em>Relative Order</em>}</li>
 *   <li>{@link org.eclipse.rse.filters.ISystemFilter#isDefault <em>Default</em>}</li>
 *   <li>{@link org.eclipse.rse.filters.ISystemFilter#isStringsCaseSensitive <em>Strings Case Sensitive</em>}</li>
 *   <li>{@link org.eclipse.rse.filters.ISystemFilter#isPromptable <em>Promptable</em>}</li>
 *   <li>{@link org.eclipse.rse.filters.ISystemFilter#isSupportsDuplicateFilterStrings <em>Supports Duplicate Filter Strings</em>}</li>
 *   <li>{@link org.eclipse.rse.filters.ISystemFilter#isNonDeletable <em>Non Deletable</em>}</li>
 *   <li>{@link org.eclipse.rse.filters.ISystemFilter#isNonRenamable <em>Non Renamable</em>}</li>
 *   <li>{@link org.eclipse.rse.filters.ISystemFilter#isNonChangable <em>Non Changable</em>}</li>
 *   <li>{@link org.eclipse.rse.filters.ISystemFilter#isStringsNonChangable <em>Strings Non Changable</em>}</li>
 *   <li>{@link org.eclipse.rse.filters.ISystemFilter#getRelease <em>Release</em>}</li>
 *   <li>{@link org.eclipse.rse.filters.ISystemFilter#isSingleFilterStringOnly <em>Single Filter String Only</em>}</li>
 *   <li>{@link org.eclipse.rse.filters.ISystemFilter#getNestedFilters <em>Nested Filters</em>}</li>
 *   <li>{@link org.eclipse.rse.filters.ISystemFilter#getParentFilter <em>Parent Filter</em>}</li>
 *   <li>{@link org.eclipse.rse.filters.ISystemFilter#getStrings <em>Strings</em>}</li>
 * </ul>
 * </p>
 *
 */
/**
 * @lastgen interface SystemFilterPool extends SystemReferencedObject, SystemFilterContainer {}
 */
public interface ISystemFilter extends IRSEReferencedObject, ISystemFilterContainer, IRSEModelObject
{
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Name</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.rse.filters.FiltersPackage#getSystemFilter_Name()
	 * @model 
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.filters.ISystemFilter#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Type</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Type</em>' attribute.
	 * @see #setType(String)
	 * @see org.eclipse.rse.filters.FiltersPackage#getSystemFilter_Type()
	 * @model 
	 * @generated
	 */
	String getType();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.filters.ISystemFilter#getType <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Type</em>' attribute.
	 * @see #getType()
	 * @generated
	 */
	void setType(String value);

	/**
	 * Returns the value of the '<em><b>Supports Nested Filters</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Supports Nested Filters</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Supports Nested Filters</em>' attribute.
	 * @see #setSupportsNestedFilters(boolean)
	 * @see org.eclipse.rse.filters.FiltersPackage#getSystemFilter_SupportsNestedFilters()
	 * @model 
	 * @generated
	 */
	boolean isSupportsNestedFilters();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.filters.ISystemFilter#isSupportsNestedFilters <em>Supports Nested Filters</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Supports Nested Filters</em>' attribute.
	 * @see #isSupportsNestedFilters()
	 * @generated
	 */
	void setSupportsNestedFilters(boolean value);

	/**
	 * Returns the value of the '<em><b>Relative Order</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Relative Order</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Relative Order</em>' attribute.
	 * @see #setRelativeOrder(int)
	 * @see org.eclipse.rse.filters.FiltersPackage#getSystemFilter_RelativeOrder()
	 * @model 
	 * @generated
	 */
	int getRelativeOrder();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.filters.ISystemFilter#getRelativeOrder <em>Relative Order</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Relative Order</em>' attribute.
	 * @see #getRelativeOrder()
	 * @generated
	 */
	void setRelativeOrder(int value);

	/**
	 * Returns the value of the '<em><b>Default</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Default</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Default</em>' attribute.
	 * @see #setDefault(boolean)
	 * @see org.eclipse.rse.filters.FiltersPackage#getSystemFilter_Default()
	 * @model 
	 * @generated
	 */
	boolean isDefault();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.filters.ISystemFilter#isDefault <em>Default</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Default</em>' attribute.
	 * @see #isDefault()
	 * @generated
	 */
	void setDefault(boolean value);

	/**
	 * Returns the value of the '<em><b>Strings Case Sensitive</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Strings Case Sensitive</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Strings Case Sensitive</em>' attribute.
	 * @see #isSetStringsCaseSensitive()
	 * @see #unsetStringsCaseSensitive()
	 * @see #setStringsCaseSensitive(boolean)
	 * @see org.eclipse.rse.filters.FiltersPackage#getSystemFilter_StringsCaseSensitive()
	 * @model unsettable="true"
	 * @generated
	 */
	boolean isStringsCaseSensitive();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.filters.ISystemFilter#isStringsCaseSensitive <em>Strings Case Sensitive</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Strings Case Sensitive</em>' attribute.
	 * @see #isSetStringsCaseSensitive()
	 * @see #unsetStringsCaseSensitive()
	 * @see #isStringsCaseSensitive()
	 * @generated
	 */
	void setStringsCaseSensitive(boolean value);

	/**
	 * Unsets the value of the '{@link org.eclipse.rse.filters.ISystemFilter#isStringsCaseSensitive <em>Strings Case Sensitive</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetStringsCaseSensitive()
	 * @see #isStringsCaseSensitive()
	 * @see #setStringsCaseSensitive(boolean)
	 * @generated
	 */
	void unsetStringsCaseSensitive();

	/**
	 * Returns whether the value of the '{@link org.eclipse.rse.filters.ISystemFilter#isStringsCaseSensitive <em>Strings Case Sensitive</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Strings Case Sensitive</em>' attribute is set.
	 * @see #unsetStringsCaseSensitive()
	 * @see #isStringsCaseSensitive()
	 * @see #setStringsCaseSensitive(boolean)
	 * @generated
	 */
	boolean isSetStringsCaseSensitive();

	/**
	 * Returns the value of the '<em><b>Promptable</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Promptable</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Promptable</em>' attribute.
	 * @see #setPromptable(boolean)
	 * @see org.eclipse.rse.filters.FiltersPackage#getSystemFilter_Promptable()
	 * @model 
	 * @generated
	 */
	boolean isPromptable();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.filters.ISystemFilter#isPromptable <em>Promptable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Promptable</em>' attribute.
	 * @see #isPromptable()
	 * @generated
	 */
	void setPromptable(boolean value);

	/**
	 * Returns the value of the '<em><b>Supports Duplicate Filter Strings</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Supports Duplicate Filter Strings</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Supports Duplicate Filter Strings</em>' attribute.
	 * @see #setSupportsDuplicateFilterStrings(boolean)
	 * @see org.eclipse.rse.filters.FiltersPackage#getSystemFilter_SupportsDuplicateFilterStrings()
	 * @model 
	 * @generated
	 */
	boolean isSupportsDuplicateFilterStrings();

	/**
	 * Does this support duplicate filter strings? Calls mof-generated isSupportsDuplicateFilterStrings.
	 */
	public boolean supportsDuplicateFilterStrings();
	
	/**
	 * Sets the value of the '{@link org.eclipse.rse.filters.ISystemFilter#isSupportsDuplicateFilterStrings <em>Supports Duplicate Filter Strings</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Supports Duplicate Filter Strings</em>' attribute.
	 * @see #isSupportsDuplicateFilterStrings()
	 * @generated
	 */
	void setSupportsDuplicateFilterStrings(boolean value);

	/**
	 * Returns the value of the '<em><b>Non Deletable</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Non Deletable</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Non Deletable</em>' attribute.
	 * @see #setNonDeletable(boolean)
	 * @see org.eclipse.rse.filters.FiltersPackage#getSystemFilter_NonDeletable()
	 * @model 
	 * @generated
	 */
	boolean isNonDeletable();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.filters.ISystemFilter#isNonDeletable <em>Non Deletable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Non Deletable</em>' attribute.
	 * @see #isNonDeletable()
	 * @generated
	 */
	void setNonDeletable(boolean value);

	/**
	 * Returns the value of the '<em><b>Non Renamable</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Non Renamable</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Non Renamable</em>' attribute.
	 * @see #setNonRenamable(boolean)
	 * @see org.eclipse.rse.filters.FiltersPackage#getSystemFilter_NonRenamable()
	 * @model 
	 * @generated
	 */
	boolean isNonRenamable();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.filters.ISystemFilter#isNonRenamable <em>Non Renamable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Non Renamable</em>' attribute.
	 * @see #isNonRenamable()
	 * @generated
	 */
	void setNonRenamable(boolean value);

	/**
	 * Returns the value of the '<em><b>Non Changable</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Non Changable</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Non Changable</em>' attribute.
	 * @see #setNonChangable(boolean)
	 * @see org.eclipse.rse.filters.FiltersPackage#getSystemFilter_NonChangable()
	 * @model 
	 * @generated
	 */
	boolean isNonChangable();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.filters.ISystemFilter#isNonChangable <em>Non Changable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Non Changable</em>' attribute.
	 * @see #isNonChangable()
	 * @generated
	 */
	void setNonChangable(boolean value);

	/**
	 * Returns the value of the '<em><b>Strings Non Changable</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Strings Non Changable</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Strings Non Changable</em>' attribute.
	 * @see #setStringsNonChangable(boolean)
	 * @see org.eclipse.rse.filters.FiltersPackage#getSystemFilter_StringsNonChangable()
	 * @model 
	 * @generated
	 */
	boolean isStringsNonChangable();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.filters.ISystemFilter#isStringsNonChangable <em>Strings Non Changable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Strings Non Changable</em>' attribute.
	 * @see #isStringsNonChangable()
	 * @generated
	 */
	void setStringsNonChangable(boolean value);

	/**
	 * Returns the value of the '<em><b>Release</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Release</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Release</em>' attribute.
	 * @see #setRelease(int)
	 * @see org.eclipse.rse.filters.FiltersPackage#getSystemFilter_Release()
	 * @model 
	 * @generated
	 */
	int getRelease();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.filters.ISystemFilter#getRelease <em>Release</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Release</em>' attribute.
	 * @see #getRelease()
	 * @generated
	 */
	void setRelease(int value);

	/**
	 * Returns the value of the '<em><b>Single Filter String Only</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Single Filter String Only</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Single Filter String Only</em>' attribute.
	 * @see #isSetSingleFilterStringOnly()
	 * @see #unsetSingleFilterStringOnly()
	 * @see #setSingleFilterStringOnly(boolean)
	 * @see org.eclipse.rse.filters.FiltersPackage#getSystemFilter_SingleFilterStringOnly()
	 * @model unsettable="true"
	 * @generated
	 */
	boolean isSingleFilterStringOnly();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.filters.ISystemFilter#isSingleFilterStringOnly <em>Single Filter String Only</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Single Filter String Only</em>' attribute.
	 * @see #isSetSingleFilterStringOnly()
	 * @see #unsetSingleFilterStringOnly()
	 * @see #isSingleFilterStringOnly()
	 * @generated
	 */
	void setSingleFilterStringOnly(boolean value);

	/**
	 * Unsets the value of the '{@link org.eclipse.rse.filters.ISystemFilter#isSingleFilterStringOnly <em>Single Filter String Only</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetSingleFilterStringOnly()
	 * @see #isSingleFilterStringOnly()
	 * @see #setSingleFilterStringOnly(boolean)
	 * @generated
	 */
	void unsetSingleFilterStringOnly();

	/**
	 * Returns whether the value of the '{@link org.eclipse.rse.filters.ISystemFilter#isSingleFilterStringOnly <em>Single Filter String Only</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Single Filter String Only</em>' attribute is set.
	 * @see #unsetSingleFilterStringOnly()
	 * @see #isSingleFilterStringOnly()
	 * @see #setSingleFilterStringOnly(boolean)
	 * @generated
	 */
	boolean isSetSingleFilterStringOnly();

	/**
	 * Returns the value of the '<em><b>Nested Filters</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.rse.filters.ISystemFilter}.
	 * It is bidirectional and its opposite is '{@link org.eclipse.rse.filters.ISystemFilter#getParentFilter <em>Parent Filter</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Nested Filters</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Nested Filters</em>' containment reference list.
	 * @see org.eclipse.rse.filters.FiltersPackage#getSystemFilter_NestedFilters()
	 * @see org.eclipse.rse.filters.ISystemFilter#getParentFilter
	 * @generated
	 */
	java.util.List getNestedFilters();

	/**
	 * Returns the value of the '<em><b>Parent Filter</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link org.eclipse.rse.filters.ISystemFilter#getNestedFilters <em>Nested Filters</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Parent Filter</em>' container reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Parent Filter</em>' container reference.
	 * @see #setParentFilter(ISystemFilter)
	 * @see org.eclipse.rse.filters.FiltersPackage#getSystemFilter_ParentFilter()
	 * @see org.eclipse.rse.filters.ISystemFilter#getNestedFilters
	 * @model opposite="nestedFilters"
	 * @generated
	 */
	ISystemFilter getParentFilter();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.filters.ISystemFilter#getParentFilter <em>Parent Filter</em>}' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Parent Filter</em>' container reference.
	 * @see #getParentFilter()
	 * @generated
	 */
	void setParentFilter(ISystemFilter value);

	/**
	 * Returns the value of the '<em><b>Strings</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.rse.filters.ISystemFilterString}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Strings</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Strings</em>' containment reference list.
	 * @see org.eclipse.rse.filters.FiltersPackage#getSystemFilter_Strings()
	 * @generated
	 */
	java.util.List getStrings();

	/**
	 * Return the parent pool of this filter. For nested filters, we walk up the parent chain
	 * until we find the pool.
	 */
	public ISystemFilterPool getParentFilterPool();
	/**
	 * Internal use method to set the parent filter pool
	 */
	public void setParentFilterPool(ISystemFilterPool parentPool);	
	/**
	 * Set this filter's filter strings by giving a Vector of String objects
	 */
	public void setFilterStrings(Vector strings);
	/**
	 * Set this filter's filter strings by giving an array of String objects
	 */
	public void setFilterStrings(String[] strings);	
	/**
	 * Get this filter's filter strings as a Vector of String objects
	 */	
	public Vector getFilterStringsVector();
	/**
	 * Get this filter's filter strings as a Vector of FilterString objects
	 */	
	public Vector getFilterStringObjectsVector();
	/**
	 * Get this filter's filter strings as an array
	 */
	public String[] getFilterStrings();    
	/**
	 * Get this filter's filter string objects as an array
	 */
	public ISystemFilterString[] getSystemFilterStrings();
	/**
	 * Get the number of filter strings this filter currently has
	 */
	public int getFilterStringCount();
	/**
	 * Get a filter string given its string value
	 */
	public ISystemFilterString getSystemFilterString(String string);
	/**
	 * Append a new filter string to this filter's list
	 */
	public ISystemFilterString addFilterString(String newString);
	/**
	 * Insert a new filter string to this filter's list, at the given zero-based position
	 */
	public ISystemFilterString addFilterString(String newString, int position);
	/**
	 * Update a new filter string's string value
	 */
	public void updateFilterString(ISystemFilterString filterString, String newValue);
	/**
	 * Delete a filter string from this filter's list.
	 * @return the SystemFilterString object deleted, or null if not found
	 */
	public ISystemFilterString removeFilterString(String oldString);
	/**
	 * Remove a filter string from this filter's list, given its zero-based position
	 * @return the SystemFilterString object deleted, or null if not found
	 */
	public ISystemFilterString removeFilterString(int position);
	/**
	 * Remove a filter string from this filter's list, given its SystemFilterString object.
	 * @return true if the given string existed and hence was deleted.
	 */
	public boolean removeFilterString(ISystemFilterString filterString);
	/**
	 * Move a given filter string to a given zero-based location
	 */
	public void moveSystemFilterString(int pos, ISystemFilterString filterString);
	/**
	 * Copies a given filter string from this filter to another filter in this pool or another pool
	 *  in this manager or another manager.
	 */
	public ISystemFilterString copySystemFilterString(ISystemFilter targetFilter, ISystemFilterString oldFilterString);	
	/**
	 * Return true if this filter is a nested filter or not. If not, its parent is the filter pool.
	 */
	public boolean isNested();
	/**
	 * Return true if this a transient or simple filter that is only created temporary "on the fly"
	 *  and not intended to be saved or part of the filter framework. Eg it has no manager or provider.
	 * <p>
	 * We always return false.
	 * @see SystemFilterSimple
	 */
	public boolean isTransient();
	/**
	 * Clones a given filter to the given target filter.
	 * All filter strings, and all nested filters, are copied.
	 * @param targetFilter new filter into which we copy all our data
	 */
	public void clone(ISystemFilter targetFilter);
	/**
	 * Return the ISystemFilterContainer parent of this filter. Will be either
	 * a SystemFilterPool or a SystemFilter if this is a nested filter.
	 */    
	public ISystemFilterContainer getParentFilterContainer();	
	/**
	 * Return the caller which instantiated the filter pool manager overseeing this filter framework instance
	 */
	public ISystemFilterPoolManagerProvider getProvider();        

} // SystemFilter