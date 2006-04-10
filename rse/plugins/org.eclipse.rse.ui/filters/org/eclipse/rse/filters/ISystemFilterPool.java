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
import org.eclipse.rse.model.IRSEModelObject;
import org.eclipse.rse.references.ISystemPersistableReferencedObject;


/**
 * This interface represents a system filter pool, which is a means of
 * grouping filters.<br>
 * By default this is represented as a folder on disk, with each filter
 * stored as a file in that folder. 
 */
/**
 * @lastgen interface SystemFilterPool extends SystemPersistableReferencedObject, SystemFilterContainer {}
 */
public interface ISystemFilterPool extends ISystemPersistableReferencedObject, ISystemFilterContainer, IRSEModelObject 
{
	// external methods
    /**
     * Return the caller which instantiated the filter pool manager overseeing this filter framework instance
     */
    public ISystemFilterPoolManagerProvider getProvider();	
    /**
     * Set the naming policy used when saving data to disk.
     * @see org.eclipse.rse.filters.ISystemFilterNamingPolicy
     */
    public void setNamingPolicy(ISystemFilterNamingPolicy namingPolicy);
    /**
     * Get the naming policy currently used when saving data to disk.
     * @see org.eclipse.rse.filters.ISystemFilterNamingPolicy
     */
    public ISystemFilterNamingPolicy getNamingPolicy();
    /**
     * Does this filter support nested filters?
     */
    public boolean supportsNestedFilters();
    /**
     * Does this support duplicate filter strings? Calls mof-generated isSupportsDuplicateFilterStrings.
     */
    public boolean supportsDuplicateFilterStrings();
    /**
	 * @return The value of the StringsCaseSensitive attribute
	 * Are filter strings in this filter case sensitive?
	 * If not set locally, queries the parent filter pool manager's atttribute.
	 */
	public boolean isStringsCaseSensitive();

    /**
     * Set the filter pool manager.
     */
    public void setSystemFilterPoolManager(ISystemFilterPoolManager mgr);
    /**
     * Return the filter pool manager managing this collection of filter pools and their filters.
     */
    public ISystemFilterPoolManager getSystemFilterPoolManager();    
	/**
	 * This is to set transient data that is queryable via getFilterPoolData
	 */
	public void setSystemFilterPoolData(Object data);
	/**
	 * Return transient data set via setFilterPoolData.
	 */
	public Object getSystemFilterPoolData();
    /**
     * Clone this filter pools' attributes and filters into another filter pool.
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
     */
    public void cloneSystemFilterPool(ISystemFilterPool targetPool)
           throws Exception;
    /**
     * Copy a system filter to this or another filter pool.
     */
    public ISystemFilter copySystemFilter(ISystemFilterPool targetPool, ISystemFilter oldFilter, String newName)
           throws Exception;
    /**
     * Order filters according to user preferences.
     * <p>
     * While the framework has all the code necessary to arrange filters and save/restore
     * that arrangement, you may choose to use preferences instead of this support.
     * In this case, call this method and pass in the saved and sorted filter name list.
     * <p>
     * Called by someone after restore.
     */
    public void orderSystemFilters(String[] names);
    /**
	 * Set the save file policy. See constants in SystemFilterConstants. One of:
	 * <ul>
	 *   <li>SAVE_POLICY_ONE_FILE_PER_POOL
	 *   <li>SAVE_POLICY_ONE_FILE_PER_FILTER
	 * </ul>
	 * This method is called by the SystemFilterPoolManager.
	 */
	public void setSavePolicy(int policy);    

	
	public String getId();
	
	/**
	 * @generated This field/method will be replaced during code generation 
	 * @return The value of the Name attribute
	 */
	String getName();


	/**
	 * @generated This field/method will be replaced during code generation 
	 * @param value The new value of the Name attribute
	 */
	void setName(String value);

	/**
	 * @generated This field/method will be replaced during code generation 
	 * @return The value of the Type attribute
	 * Allows tools to have typed filter pools
	 */
	String getType();

	/**
	 * @generated This field/method will be replaced during code generation 
	 * @param value The new value of the Type attribute
	 */
	void setType(String value);

	/**
	 * @generated This field/method will be replaced during code generation 
	 * @return The value of the SupportsNestedFilters attribute
	 */
	boolean isSupportsNestedFilters();

	/**
	 * @generated This field/method will be replaced during code generation 
	 * @param value The new value of the SupportsNestedFilters attribute
	 */
	void setSupportsNestedFilters(boolean value);

	/**
	 * @generated This field/method will be replaced during code generation 
	 * @return The value of the Deletable attribute
	 */
	boolean isDeletable();

	/**
	 * @generated This field/method will be replaced during code generation 
	 * @param value The new value of the Deletable attribute
	 */
	void setDeletable(boolean value);

	/**
	 * @generated This field/method will be replaced during code generation 
	 * @return The value of the Default attribute
	 * Is this a default vendor-supplied pool versus user-created pool
	 */
	boolean isDefault();

	/**
	 * @generated This field/method will be replaced during code generation 
	 * @param value The new value of the Default attribute
	 */
	void setDefault(boolean value);

	/**
	 * @generated This field/method will be replaced during code generation 
	 * @param value The new value of the StringsCaseSensitive attribute
	 */
	void setStringsCaseSensitive(boolean value);

	/**
	 * @generated This field/method will be replaced during code generation 
	 * Unsets the StringsCaseSensitive attribute
	 */
	void unsetStringsCaseSensitive();

	/**
	 * @generated This field/method will be replaced during code generation 
	 * @return <b>true</b> if the StringsCaseSensitive attribute has been set
	 */
	boolean isSetStringsCaseSensitive();

	/**
	 * @generated This field/method will be replaced during code generation 
	 * @return The list of Filters references
	 */
	java.util.List getFilters();

	/**
	 * @generated This field/method will be replaced during code generation 
	 * @return The value of the SupportsDuplicateFilterStrings attribute
	 */
	boolean isSupportsDuplicateFilterStrings();

	/**
	 * @generated This field/method will be replaced during code generation 
	 * @param value The new value of the SupportsDuplicateFilterStrings attribute
	 */
	void setSupportsDuplicateFilterStrings(boolean value);

	/**
	 * @generated This field/method will be replaced during code generation 
	 * @return The value of the Release attribute
	 * In what release was this created? Typically, will be the version and release
	 * times 10, as in 40 or 51.
	 */
	int getRelease();

	/**
	 * @generated This field/method will be replaced during code generation 
	 * @param value The new value of the Release attribute
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
	 * @see org.eclipse.rse.filters.FiltersPackage#getSystemFilterPool_SingleFilterStringOnly()
	 * @model unsettable="true"
	 * @generated
	 */
	boolean isSingleFilterStringOnly();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.filters.ISystemFilterPool#isSingleFilterStringOnly <em>Single Filter String Only</em>}' attribute.
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
	 * Unsets the value of the '{@link org.eclipse.rse.filters.ISystemFilterPool#isSingleFilterStringOnly <em>Single Filter String Only</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetSingleFilterStringOnly()
	 * @see #isSingleFilterStringOnly()
	 * @see #setSingleFilterStringOnly(boolean)
	 * @generated
	 */
	void unsetSingleFilterStringOnly();

	/**
	 * Returns whether the value of the '{@link org.eclipse.rse.filters.ISystemFilterPool#isSingleFilterStringOnly <em>Single Filter String Only</em>}' attribute is set.
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
	 * Returns the value of the '<em><b>Owning Parent Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Owning Parent Name</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Owning Parent Name</em>' attribute.
	 * @see #setOwningParentName(String)
	 * @see org.eclipse.rse.filters.FiltersPackage#getSystemFilterPool_OwningParentName()
	 * @model 
	 * @generated
	 */
	String getOwningParentName();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.filters.ISystemFilterPool#getOwningParentName <em>Owning Parent Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Owning Parent Name</em>' attribute.
	 * @see #getOwningParentName()
	 * @generated
	 */
	void setOwningParentName(String value);

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
	 * @see org.eclipse.rse.filters.FiltersPackage#getSystemFilterPool_NonRenamable()
	 * @model 
	 * @generated
	 */
	boolean isNonRenamable();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.filters.ISystemFilterPool#isNonRenamable <em>Non Renamable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Non Renamable</em>' attribute.
	 * @see #isNonRenamable()
	 * @generated
	 */
	void setNonRenamable(boolean value);

}