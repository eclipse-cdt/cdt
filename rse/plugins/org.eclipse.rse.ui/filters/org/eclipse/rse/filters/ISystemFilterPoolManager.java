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
import java.util.Vector;

import org.eclipse.rse.core.persistance.IRSEPersistableContainer;
import org.eclipse.rse.model.ISystemProfile;





/**
 * A filter pool manager manages filter pools.
 * <p>
 * Each filter pool that is managed becomes a folder on disk.
 * <p>
 * To create a filter pool manager instance, use the factory methods
 *  in SystemFilterPoolManagerImpl in the ...impl package.
 * You must pass a folder that represents the anchor point for the 
 *  pools managed by this manager instance.
 * <p>
 * Depending on your tools' needs, you have four choices about how
 * the filter pools and filters are persisted to disk. The decision is
 * made at the time you instantiate the pool manager and is one of the
 * following constants from the {@link SystemFilterConstants} interface:
 * <ul>
 *   <li>SAVE_POLICY_ONE_FILE_PER_MANAGER - one file: mgrName.xmi
 *   <li>SAVE_POLICY_ONE_FILEANDFOLDER_PER_POOL - one file and folder per pool
 *   <li>SAVE_POLICY_ONE_FILE_PER_POOL_SAME_FOLDER - one file per pool, all files in one folder
 *   <li>SAVE_POLICY_ONE_FILE_PER_FILTER - one file per filter, one folder per pool
 * </ul> 
 * <p>
 * With the policy of one file per pool, there are two possibilities regarding
 * the folder structure:
 * <ul>
 *   <li>Each pool gets its own subfolder, and the pool's xmi file goes in 
 *         the pool's unique subfolder: SAVE_POLICY_ONE_FILEANDFOLDER_PER_POOL
 *   <li>There are no subfolders per pool, all the xmi pool files go in the 
 *         same folder as specified when creating this manager instance:
 *         SAVE_POLICY_ONE_FILE_PER_POOL_SAME_FOLDER
 * </ul>
 * <p>
 * With the policy of one file per filter, each filter pool must have its own folder.
 * <p>
 * With an instantiated filter pool manager (most tools will only need
 *  one such instance), you now simply call its methods to work with
 *  filter pools. For example, use it to:
 *  <ul>
 *    <li>Restore all filter pools from disk
 *    <li>Save all, or individual, filter pools to disk
 *    <li>Get a list of existing filter pools
 *    <li>Create filter pools
 *    <li>Delete filter pools
 *    <li>Re-order filter pools
 *    <li>Clone filter pools
 *    <li>Rename filter pools
 *    <li>Save all, or individual, filter pools
 *  </ul>
 * All the underlying file system work is handled for you.
 * <p>
 * Further, this is the front door for working with filters too. By forcing all
 * filter related activity through a single point like this, we can ensure that
 * all changes are saved to disk, and events are fired properly.
 */
/** 
 * @lastgen interface SystemFilterPoolManager  {}
 */
public interface ISystemFilterPoolManager extends IRSEPersistableContainer
{		
    // ---------------------------------
    // ATTRIBUTE METHODS
    // ---------------------------------
    /**
     * Return the caller which instantiated the filter pool manager
     */
    public ISystemFilterPoolManagerProvider getProvider();
    
    /**
     * Return the owning profile for this provider
     */
    public ISystemProfile getSystemProfile();
    
    /**
     * Set the caller instance which instantiated the filter pool manager.
     * This is only recorded to enable getProvider from any filter framework object.
     */
    public void setProvider(ISystemFilterPoolManagerProvider caller);

	/**
	 * This is to set transient data that is subsequently queryable.
	 */
	public void setSystemFilterPoolManagerData(Object data);
	/**
	 * Return transient data set via setFilterPoolDataManager.
	 */
	public Object getSystemFilterPoolManagerData();    
    /**
     * Return the name of this manager.
     * This matches the name of the folder, which is the parent of the individual filter pool folders.
     */
	public String getName();

/**
     * Set the name of this manager.
     */
	public void setName(String name);

    /**
     * Return attribute indicating if filter pools managed by this manager support nested filters.
     */
    public boolean supportsNestedFilters();
    /**
     * Return attribute indicating if filters managed by this manager support nested duplicate filter strings.
     */
    public boolean supportsDuplicateFilterStrings();    
	/**
     * Set attribute indicating if filter pools managed by this manager support nested filters, by default.
     */
	public void setSupportsNestedFilters(boolean supports);

	/**
     * Set attribute indicating if filters managed by this manager support duplicate filter strings, by default.
     */
	public void setSupportsDuplicateFilterStrings(boolean supports);

	/**
	 * @return The value of the StringsCaseSensitive attribute
	 * Are filter strings in this filter case sensitive?
	 */
	public boolean isStringsCaseSensitive();

	/**
	 * @return The value of the StringsCaseSensitive attribute
	 * Are filter strings in this filter case sensitive?
	 * Same as isStringsCaseSensitive()
	 */
	public boolean areStringsCaseSensitive();

    /**
     * Return false if the instantiation of this filter pool manager resulting in a new manager versus a restoration
     */
    public boolean wasRestored();
    
    // ---------------------------------
    // FILTER POOL METHODS
    // ---------------------------------
    /**
     * Get array of filter pool names currently existing.
     */
    public String[] getSystemFilterPoolNames();
    /**
     * Get vector of filter pool names currently existing.
     */
    public Vector getSystemFilterPoolNamesVector();

    /**
     * Return array of SystemFilterPools managed by this manager.
     */
    public ISystemFilterPool[] getSystemFilterPools();
    
    /**
     * Given a filter pool name, return that filter pool object.
     * If not found, returns null.
     */
    public ISystemFilterPool getSystemFilterPool(String name);

    /**
     * Return the first pool that has the default attribute set to true.
     * If none found, returns null.
     */
    public ISystemFilterPool getFirstDefaultSystemFilterPool();   
        
    /**
     * Create a new filter pool.
     * Inherits the following attributes from this manager:
     * <ul>
     *   <li>data ... the transient data to be associated with every filter pool and filter
     *   <li>supportsNestedFilters ... whether filters in the pool can themselves contain filters
     * </ul>
     * <p>
     * If a pool of this name already exists, null will be returned.
     * <p>
     * Depending on the save policy, a new folder to hold the pool may be created. Its name will
     *   be derived from the pool name.
     * <p>
     * If the operation is successful, the pool will be saved to disk.
     * <p>
     * If this operation fails unexpectedly, an exception will be thrown.
     */
    public ISystemFilterPool createSystemFilterPool(String name, boolean isDeletable)
      throws Exception;

    /**
     * Delete a given filter pool. Dependending on the save policy, the 
     *  appropriate file or folder on disk will also be deleted.
     * <p>
     * Does the following:
     * <ul>
     *   <li>Removes all references
     *   <li>Removes pool object from in-memory model
     *   <li>Removes folder from disk for policies of one folder per pool
     *   <li>Removes file from disk for policy of one file per pool
     *   <li>Saves model to disk for policy of one file per manager
     *   <li>Invalidates in-memory caches
     *   <li>Calls back to inform caller of this event
     * </ul>
     * @param pool The filter pool object to physically delete
     */
    public void deleteSystemFilterPool(ISystemFilterPool pool)
      throws Exception;

    /**
     * Delete all existing filter pools. Call this when you are about to delete this manager, say.
     */
    public void deleteAllSystemFilterPools();

    /**
     * Pre-test if we are going to run into any trouble renaming any of the files or folders
     *  used to persist a filter pool.
     * @return true if everything seems ok, false if a file/folder is in use.
     */
    public boolean preTestRenameFilterPool(ISystemFilterPool pool) throws Exception;
    /**
     * Rename a given filter pool. Dependending on the save policy, the 
     *  appropriate file or folder on disk will also be renamed.
     * <p>
     * Does the following:
     * <ul>
     *   <li>Renames referencing objects
     *   <li>Renames pool object in the in-memory model
     *   <li>Renames folder on disk for policies of one folder per pool
     *   <li>Renames file on disk for policy of one file per pool
     *   <li>Saves model to disk for policy of one file per manager
     *   <li>Invalidates in-memory caches
     * </ul>
     * @param pool The filter pool object to physically rename
     * @param newName The new name to give the pool
     */
    public void renameSystemFilterPool(ISystemFilterPool pool, String newName)
      throws Exception;

    /**
     * Copy the specified filter pool from this manager to this manager or another manager.
     * <p>
     * Does the following:
     * <ul>
     *   <li>Clones all filters within the pool
     *   <li>Clones all filter strings within each filter
     *   <li>Asks target manager to save to disk
     *   <li>Calls back to target manager provider, unless callbacks are suspended
     * </ul>
     * @param targetMgr The target manager to copy our filter pool to. Can be this manager, but target pool name must be unique.
     * @param pool The filter pool to copy
     * @param newName The new name to give the copied pool
     * @return the new copy of the copied system filter pool
     */
    public ISystemFilterPool copySystemFilterPool(ISystemFilterPoolManager targetMgr, ISystemFilterPool pool, String newName)
      throws Exception;
      
    /**
     * Copy all filter pools from this manager to another manager.
     * <p>
     * Does the following:
     * <ul>
     *   <li>Clones all filter pools
     *   <li>Clones all filters within each pool
     *   <li>Clones all filter strings within each filter
     *   <li>Asks target manager to save to disk
     *   <li>Does not callback to caller to fire events, assumes caller doesn't want to know
     * </ul>
     * @param targetMgr The target manager to copy our filter pools to
     */
    public void copySystemFilterPools(ISystemFilterPoolManager targetMgr)
      throws Exception;

    /**
     * Move the specified filter pool from this manager to another manager.
     * <p>
     * Does the following:
     * <ul>
     *   <li>Performs a {@link #copySystemFilterPool(ISystemFilterPoolManager, ISystemFilterPool, String) copySystemFilterPool} operation.
     *   <li>If copy is successful, updates all references to reference the new copy.
     *   <li>If copy is successful, deletes original filter pool in this manager 
     *   <li>If this final delete fails, deletes the copied version and restore original references
     *   <li>Asks target manager to save to disk
     *   <li>Saves this manager to disk
     *   <li>Calls back to both targer manager provider and this manager provider, unless callbacks are suspended
     * </ul>
     * @param targetMgr The target manager to move our filter pool to. Cannot be this manager.
     * @param oldPool The filter pool to move
     * @param newName The new name to give the moved pool
     * @return the new copy of the moved system filter pool
     */
    public ISystemFilterPool moveSystemFilterPool(ISystemFilterPoolManager targetMgr, ISystemFilterPool oldPool, String newName)
      throws Exception;
      
    // ---------------------------------
    // FILTER METHODS
    // ---------------------------------
    /**
     * Creates a new system filter within the given filter container (either a filter pool, or
     *  a filter). This creates the filter, and then saves the filter pool. 
     * <p>Calls back to provider to inform of the event (filterEventFilterCreated)
     * @param parent The parent which is either a SystemFilterPool or a SystemFilter
     * @param aliasName The name to give the new filter. Must be unique for this pool.
     * @param filterStrings The list of String objects that represent the filter strings.
     */    
    public ISystemFilter createSystemFilter(ISystemFilterContainer parent,
                                           String aliasName, Vector filterStrings)
        throws Exception;
    /**
     * Creates a new system filter that is typed.
     * Same as {@link #createSystemFilter(ISystemFilterContainer, String, Vector)} but 
     *  takes a filter type as an additional parameter.
     * <p>
     * A filter's type is an arbitrary string that is not interpreted or used by the base framework. This
     * is for use entirely by tools who wish to support multiple types of filters and be able to launch unique
     * actions per type, say.
     * 
     * @param parent The parent which is either a SystemFilterPool or a SystemFilter
     * @param aliasName The name to give the new filter. Must be unique for this pool.
     * @param filterStrings The list of String objects that represent the filter strings.
     * @param type The type of this filter
     */    
    public ISystemFilter createSystemFilter(ISystemFilterContainer parent,
                                           String aliasName, Vector filterStrings, String type)
        throws Exception;
    /**
     * Creates a new system filter that is typed and promptable
     * Same as {@link #createSystemFilter(ISystemFilterContainer, String ,Vector, String)} but 
     *  takes a boolean indicating if it is promptable.
     * <p>
     * A promptable filter is one in which the user is prompted for information at expand time.
     * There is no base filter framework support for this, but tools can query this attribute and
     * do their own thing at expand time.
     * 
     * @param parent The parent which is either a SystemFilterPool or a SystemFilter
     * @param aliasName The name to give the new filter. Must be unique for this pool.
     * @param filterStrings The list of String objects that represent the filter strings.
     * @param type The type of this filter
     * @param promptable Pass true if this is a promptable filter
     */    
    public ISystemFilter createSystemFilter(ISystemFilterContainer parent,
                                           String aliasName, Vector filterStrings, String type, boolean promptable)
        throws Exception;

    /**
     * Delete an existing system filter.
     * Does the following:
     * <ul>
     *   <li>Removes filter from its parent in memory.
     *   <li>If appropriate for the save policy, deletes the filter's file from disk.
     *   <li>Save the SystemFilterPool which direct or indirectly contains the filter.
     *   <li>Calls back to provider to inform of the event (filterEventFilterDelete)
     * </ul>
     */  
    public boolean deleteSystemFilter(ISystemFilter filter)
                   throws Exception;
    /**
     * Renames a filter. This is better than filter.setName(String newName) as it 
     *  saves the parent pool to disk.
     * <p>
     * Does the following:
     * <ul>
     *   <li>Renames the object in the in-memory cache
     *   <li>If appropriate for the save policy, rename's the filter's file on disk.
     *   <li>Save parent filter pool's in-memory object to disk.
     *   <li>Calls back to provider to inform of the event (filterEventFilterRenamed)
     * </ul>
     */
    public void renameSystemFilter(ISystemFilter filter, String newName)
           throws Exception;

    /**
     * Updates a filter. This is better than doing it directly as it saves it to disk.
     * <p>
     * Does the following:
     * <ul>
     *   <li>Updates the object in the in-memory cache
     *   <li>Save parent filter pool's in-memory object to disk.
     *   <li>Calls back to provider to inform of the event (filterEventFilterUpdated). Will be two callbacks if the name is changed ((filterEventFilterRenamed)
     * </ul>
     */
    public void updateSystemFilter(ISystemFilter filter, String newName, String[] strings)
           throws Exception;

    /**
     * Sets a filter's type. This is better than calling filter.setType(String) directly as it saves the filter to disk after.
     * <p>
     * A filter's type is an arbitrary string that is not interpreted or used by the base framework. This
     * is for use entirely by tools who wish to support multiple types of filters and be able to launch unique
     * actions per type, say.
     * @param parent The parent which is either a SystemFilterPool or a SystemFilter
     * @param type The type of this filter
     */
    public void setSystemFilterType(ISystemFilter filter, String newType)
           throws Exception;

    /**
     * Copy a system filter to a pool in this or another filter manager.
     */
    public ISystemFilter copySystemFilter(ISystemFilterPool targetPool, ISystemFilter oldFilter, String newName)
           throws Exception;
           
    /**
     * Return the zero-based position of a SystemFilter object within its container
     */
    public int getSystemFilterPosition(ISystemFilter filter);

    /**
     * Move a system filter to a pool in this or another filter manager.
     * Does this by first copying the filter, and only if successful, deleting the old copy.
     */
    public ISystemFilter moveSystemFilter(ISystemFilterPool targetPool, ISystemFilter oldFilter, String newName)
           throws Exception;
           
    /**
     * Move existing filters a given number of positions in the same container.
     * If the delta is negative, they are all moved up by the given amount. If 
     * positive, they are all moved down by the given amount.<p>
     * <p>
     * Does the following:
     * <ul>
     *   <li>After the move, the pool containing the filter is saved to disk.
     *   <li>Calls back to provider to inform of the event (filterEventFiltersRePositioned)
     * </ul>
     * @param filters Array of SystemFilters to move.
     * @param newPosition new zero-based position for the filters
     */
    public void moveSystemFilters(ISystemFilter filters[], int delta)
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
    public void orderSystemFilters(ISystemFilterPool pool, String[] names) throws Exception;

    // -------------------------------
    // SYSTEM FILTER STRING METHODS...
    // -------------------------------
    /**
     * Append a new filter string to the given filter's list
     * <p>
     * Does the following:
     * <ul>
     *   <li>Adds the filter string to the in-memory cache
     *   <li>Saves parent filter pool to disk.
     *   <li>Calls back to provider to inform it of this event (filterEventFilterStringCreated)
     * </ul>
     */
    public ISystemFilterString addSystemFilterString(ISystemFilter filter, String newString) throws Exception;
    /**
     * Insert a new filter string to the given filter's list, at the given zero-based position
     * <p>
     * Does the following:
     * <ul>
     *   <li>Adds the filter string to the in-memory cache
     *   <li>Saves parent filter pool to disk.
     *   <li>Calls back to provider to inform it of this event (filterEventFilterStringCreated)
     * </ul>
     */
    public ISystemFilterString addSystemFilterString(ISystemFilter filter, String newString, int position) throws Exception;
    /**
     * Remove a filter string from this filter's list, given its SystemFilterString object.
     * <p>
     * Does the following:
     * <ul>
     *   <li>Removes the filter string from the in-memory cache
     *   <li>Saves parent filter pool to disk.
     *   <li>Calls back to provider to inform it of this event (filterEventFilterStringDeleted)
     * </ul>
     * @return true if the given string existed and hence was deleted.
     */
    public boolean removeSystemFilterString(ISystemFilter filter, ISystemFilterString filterString) throws Exception;
    /**
     * Delete a filter string from the given filter's list
     * <p>
     * Does the following:
     * <ul>
     *   <li>Removes the filter string from the in-memory cache
     *   <li>Saves parent filter pool to disk.
     *   <li>Calls back to provider to inform it of this event (filterEventFilterStringDeleted)
     * </ul>
     * @return true if given string was found and hence was deleted.
     */
    public boolean removeSystemFilterString(ISystemFilter filter, String oldString) throws Exception;
    /**
     * Remove a filter string from the given filter's list, given its zero-based position
     * <p>
     * Does the following:
     * <ul>
     *   <li>Removes the filter string from the in-memory cache
     *   <li>Saves parent filter pool to disk.
     *   <li>Calls back to provider to inform it of this event (filterEventFilterStringDeleted)
     * </ul>
     * @return true if a string existed at the given position and hence was deleted.
     */
    public boolean removeSystemFilterString(ISystemFilter filter, int position) throws Exception;
    /**
     * Update a filter string's string vale
     * <p>
     * Does the following:
     * <ul>
     *   <li>Update the filter string in the in-memory cache
     *   <li>Saves parent filter pool to disk.
     *   <li>Calls back to provider to inform it of this event (filterEventFilterStringUpdated)
     * </ul>
     */
    public void updateSystemFilterString(ISystemFilterString filterString, String newValue) throws Exception;
    /**
     * Return the zero-based position of a SystemFilterString object within its filter
     */
    public int getSystemFilterStringPosition(ISystemFilterString filterString);
    /**
     * Copy a system filter string to a filter in this or another filter pool manager.
     */
    public ISystemFilterString copySystemFilterString(ISystemFilter targetFilter, ISystemFilterString oldFilterString)
           throws Exception;
    /**
     * Move a system filter string to a filter in this or another filter pool manager.
     * Does this by doing a copy operation, then if successful doing a delete operation.
     */
    public ISystemFilterString moveSystemFilterString(ISystemFilter targetFilter, ISystemFilterString oldFilterString)
           throws Exception;
    /**
     * Move existing filter strings a given number of positions in the same filter
     * If the delta is negative, they are all moved up by the given amount. If 
     * positive, they are all moved down by the given amount.<p>
     * <p>
     * Does the following:
     * <ul>
     *   <li>After the move, the filter pool containing the filter containing the filter strings is saved to disk.
     *   <li>Calls back to provider to inform of the event (filterEventFilterStringsRePositioned)
     * </ul>
     * @param filterStrings Array of SystemFilterStrings to move.
     * @param newPosition new zero-based position for the filter strings
     */
    public void moveSystemFilterStrings(ISystemFilterString filterStrings[], int delta)
              throws Exception;

    // -----------------------------------
    // SUSPEND/RESUME CALLBACKS METHODS...
    // -----------------------------------
    /**
     * Suspend callbacks to the provider
     */
    public void suspendCallbacks(boolean suspend);

 

    /**
	 * @generated This field/method will be replaced during code generation 
	 * @return The value of the SupportsNestedFilters attribute
	 */
	boolean isSupportsNestedFilters();

	/**
	 * @generated This field/method will be replaced during code generation 
	 * @param value The new value of the StringsCaseSensitive attribute
	 */
	void setStringsCaseSensitive(boolean value);

	/**
	 * @generated This field/method will be replaced during code generation 
	 * @return The list of Pools references
	 */
	java.util.List getPools();

	/**
	 * @generated This field/method will be replaced during code generation 
	 * @return The value of the SupportsDuplicateFilterStrings attribute
	 */
	boolean isSupportsDuplicateFilterStrings();

	/**
	 * @generated This field/method will be replaced during code generation 
	 * Unsets the SupportsDuplicateFilterStrings attribute
	 */
	void unsetSupportsDuplicateFilterStrings();

	/**
	 * @generated This field/method will be replaced during code generation 
	 * @return <b>true</b> if the SupportsDuplicateFilterStrings attribute has been set
	 */
	boolean isSetSupportsDuplicateFilterStrings();

	/**
	 * Returns the value of the '<em><b>Single Filter String Only</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Single Filter String Only</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Single Filter String Only</em>' attribute.
	 * @see #setSingleFilterStringOnly(boolean)
	 * @see org.eclipse.rse.filters.FiltersPackage#getSystemFilterPoolManager_SingleFilterStringOnly()
	 * @model 
	 * @generated
	 */
	boolean isSingleFilterStringOnly();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.filters.ISystemFilterPoolManager#isSingleFilterStringOnly <em>Single Filter String Only</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Single Filter String Only</em>' attribute.
	 * @see #isSingleFilterStringOnly()
	 * @generated
	 */
	void setSingleFilterStringOnly(boolean value);

}