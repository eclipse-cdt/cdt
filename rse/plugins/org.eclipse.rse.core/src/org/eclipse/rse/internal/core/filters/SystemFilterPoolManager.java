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
 * David Dykstal (IBM) - 142806: refactoring persistence framework
 * David Dykstal (IBM) - [197036] removed caching mechanism to clean up logic
 * David Dykstal (IBM) - [222270] clean up interfaces in org.eclipse.rse.core.filters
 *******************************************************************************/

package org.eclipse.rse.internal.core.filters;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.filters.ISystemFilter;
import org.eclipse.rse.core.filters.ISystemFilterContainer;
import org.eclipse.rse.core.filters.ISystemFilterPool;
import org.eclipse.rse.core.filters.ISystemFilterPoolManager;
import org.eclipse.rse.core.filters.ISystemFilterPoolManagerProvider;
import org.eclipse.rse.core.filters.ISystemFilterPoolReference;
import org.eclipse.rse.core.filters.ISystemFilterPoolReferenceManager;
import org.eclipse.rse.core.filters.ISystemFilterString;
import org.eclipse.rse.core.model.IRSEPersistableContainer;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.RSEPersistableObject;
import org.eclipse.rse.core.references.IRSEBaseReferencingObject;
import org.eclipse.rse.logging.Logger;

/**
 * A filter pool manager manages filter pools. It is used to
 * <ul>
 * <li>Get a list of existing filter pools
 * <li>Create filter pools
 * <li>Delete filter pools
 * <li>Clone filter pools
 * <li>Rename filter pools
 * </ul>
 * <p>
 * The filter pool manager ensures that changes to filters and pools are
 * committed and events are fired properly.
 * <p>
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */
public class SystemFilterPoolManager extends RSEPersistableObject implements ISystemFilterPoolManager {
	private ISystemFilterPoolManagerProvider caller = null;
	private Object poolMgrData;
	private boolean initialized = false;

	private boolean suspendCallbacks = false;
	private boolean suspendSave = false;
	private Logger logger = null;
	private ISystemProfile _profile;

	public static boolean debug = true;

	/**
	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
	 * @see #getName()
	 */
	protected static final String NAME_EDEFAULT = null;

	protected String name = NAME_EDEFAULT;
	/**
	 * The default value of the '{@link #isSupportsNestedFilters() <em>Supports Nested Filters</em>}' attribute.
	 * @see #isSupportsNestedFilters()
	 */
	protected static final boolean SUPPORTS_NESTED_FILTERS_EDEFAULT = false;

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	protected boolean supportsNestedFilters = SUPPORTS_NESTED_FILTERS_EDEFAULT;
	/**
	 * The default value of the '{@link #isStringsCaseSensitive() <em>Strings Case Sensitive</em>}' attribute.
	 * @see #isStringsCaseSensitive()
	 */
	protected static final boolean STRINGS_CASE_SENSITIVE_EDEFAULT = false;

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	protected boolean stringsCaseSensitive = STRINGS_CASE_SENSITIVE_EDEFAULT;
	/**
	 * The default value of the '{@link #isSupportsDuplicateFilterStrings() <em>Supports Duplicate Filter Strings</em>}' attribute.
	 * @see #isSupportsDuplicateFilterStrings()
	 */
	protected static final boolean SUPPORTS_DUPLICATE_FILTER_STRINGS_EDEFAULT = false;

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	protected boolean supportsDuplicateFilterStrings = SUPPORTS_DUPLICATE_FILTER_STRINGS_EDEFAULT;
	/**
	 * This is true if the Supports Duplicate Filter Strings attribute has been set.
	 */
	protected boolean supportsDuplicateFilterStringsESet = false;

	/**
	 * The default value of the '{@link #isSingleFilterStringOnly() <em>Single Filter String Only</em>}' attribute.
	 * @see #isSingleFilterStringOnly()
	 */
	protected static final boolean SINGLE_FILTER_STRING_ONLY_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isSingleFilterStringOnly() <em>Single Filter String Only</em>}' attribute.
	 * @see #isSingleFilterStringOnly()
	 */
	protected boolean singleFilterStringOnly = SINGLE_FILTER_STRING_ONLY_EDEFAULT;

	protected List pools = new ArrayList(3);
	//	protected List pools = null;

	/**
	 * Constructor
	 */
	private SystemFilterPoolManager(ISystemProfile profile) {
		super();
		_profile = profile;
	}

	public ISystemProfile getSystemProfile() {
		return _profile;
	}

	/**
	 * Factory to create a filter pool manager.
	 * @param profile the profile for which to create the filter pool manager.
	 * @param logger A logging object into which to log errors as they happen in the framework
	 * @param caller Objects which instantiate this class should implement the
	 *   SystemFilterPoolManagerProvider interface, and pass "this" for this parameter.
	 *   Given any filter framework object, it is possible to retrieve the caller's
	 *   object via the getProvider method call.
	 * @param name the name of the filter pool manager. Typically this is also the name
	 *   of the given folder, but this is not required. For the save policy of one file
	 *   per manager, the name of the file is derived from this. For other save policies,
	 *   the name is not used.
	 * @param allowNestedFilters true if filters inside filter pools in this manager are
	 *   to allow nested filters. This is the default, but can be overridden at the
	 *   individual filter pool level.
	 */
	public static ISystemFilterPoolManager createSystemFilterPoolManager(ISystemProfile profile, Logger logger,
			ISystemFilterPoolManagerProvider caller, String name, boolean allowNestedFilters) {
		SystemFilterPoolManager mgr = new SystemFilterPoolManager(profile);
		mgr.initialize(logger, caller, name, allowNestedFilters);
		return mgr;
	}

	/*
	 * Private helper method to initialize state
	 */
	public void initialize(Logger logger, ISystemFilterPoolManagerProvider caller, String name, boolean allowNestedFilters) {
		if (!initialized) {
			initialize(logger, caller, name); // core data
		}
		setSupportsNestedFilters(allowNestedFilters); // cascade it down
	}

	/*
	 * Private helper method to do core initialization.
	 * Might be called from either the static factory method or the static restore method.
	 */
	public void initialize(Logger logger, ISystemFilterPoolManagerProvider caller, String name) {
		this.logger = logger;
		setProvider(caller);
		setName(name);
		setFilterPoolManager(); // cascade it down
		initialized = true;
	}

	/**
	 * Return the caller which instantiated the filter pool manager
	 */
	public ISystemFilterPoolManagerProvider getProvider() {
		return caller;
	}

	/**
	 * Set the caller instance which instantiated the filter pool manager.
	 * This is only recorded to enable getProvider from any filter framework object.
	 */
	public void setProvider(ISystemFilterPoolManagerProvider caller) {
		this.caller = caller;
	}

	/**
	 * Set the name of this manager.
	 */
	public void setName(String newName) {
		if ((name == null && newName != null) || !name.equals(newName)) {
			this.name = newName;
			setDirty(true);
		}
	}

	/**
	 * Return attribute indicating if filter pools managed by this manager support nested filters.
	 * Same as isSupportsNestedFilters()
	 */
	public boolean supportsNestedFilters() {
		return isSupportsNestedFilters();
	}

	/**
	 * Return attribute indicating if filters managed by this manager support nested duplicate filter strings.
	 * Same as isSupportsDuplicateFilterStrings()
	 */
	public boolean supportsDuplicateFilterStrings() {
		//return allowDuplicateFilterStrings;
		return isSupportsDuplicateFilterStrings();
	}

	/**
	 * Set attribute indicating if filter pools managed by this manager support nested filters, by default.
	 * Cascaded down to all pools, and all filters in all pools.
	 * Alternatively, just call it on the particular pool or filter it applies to.
	 */
	public void setSupportsNestedFilters(boolean newSupportsNestedFilters) {
		// as generated by emf...
		setSupportsNestedFiltersGen(newSupportsNestedFilters);
		// our own stuff..
		ISystemFilterPool[] pools = getSystemFilterPools();
		for (int idx = 0; idx < pools.length; idx++) {
			pools[idx].setSupportsNestedFilters(newSupportsNestedFilters);
		}
	}

	/**
	 * Set attribute indicating if filters managed by this manager support duplicate filter strings, by default.
	 * Cascaded down to all pools, and all filters in all pools.
	 * Alternatively, just call it on the particular pool or filter it applies to.
	 */
	public void setSupportsDuplicateFilterStrings(boolean newSupportsDuplicateFilterStrings) {
		// as generated by emf...
		setSupportsDuplicateFilterStringsGen(newSupportsDuplicateFilterStrings);

		// our own stuff..
		ISystemFilterPool[] pools = getSystemFilterPools();
		for (int idx = 0; idx < pools.length; idx++) {
			pools[idx].setSupportsDuplicateFilterStrings(newSupportsDuplicateFilterStrings);
		}
	}

	/**
	 */
	private void setSupportsDuplicateFilterStringsGen(boolean newSupportsDuplicateFilterStrings) {
		supportsDuplicateFilterStrings = newSupportsDuplicateFilterStrings;
		supportsDuplicateFilterStringsESet = true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilterPoolManager#isStringsCaseSensitive()
	 */
	public boolean isStringsCaseSensitive() {
		return stringsCaseSensitive;
	}

	/**
	 * Same as isStringsCaseSensitive()
	 * Are filter strings in this filter case sensitive?
	 * @return The value of the StringsCaseSensitive attribute
	 */
	public boolean areStringsCaseSensitive() {
		return isStringsCaseSensitive();
	}

	/**
	 * Set attribute indicating if filters managed by this manager support case-sensitive filter strings, by default.
	 * Cascaded down to all pools, and all filters in all pools.
	 * Alternatively, just call it on the particular pool or filter it applies to.
	 */
	public void setStringsCaseSensitive(boolean newStringsCaseSensitive) {
		// as generated by emf...
		setStringsCaseSensitiveGen(newStringsCaseSensitive);
		// our special code...
		ISystemFilterPool[] pools = getSystemFilterPools();
		for (int idx = 0; idx < pools.length; idx++) {
			pools[idx].setStringsCaseSensitive(newStringsCaseSensitive);
		}
	}

	/**
	 */
	private void setStringsCaseSensitiveGen(boolean newStringsCaseSensitive) {
		stringsCaseSensitive = newStringsCaseSensitive;
	}

	/**
	 * Set transient variable pointing back to us. Called after restoring.
	 * Cascaded down to all pools, and all filters in all pools.
	 */
	protected void setFilterPoolManager() {
		ISystemFilterPool[] pools = getSystemFilterPools();
		for (int idx = 0; idx < pools.length; idx++) {
			pools[idx].setSystemFilterPoolManager(this);
		}
	}

	/**
	 * This is to set transient data that is subsequently queryable.
	 */
	public void setSystemFilterPoolManagerData(Object data) {
		this.poolMgrData = data;
	}

	/**
	 * Return transient data set via setFilterPoolData.
	 */
	public Object getSystemFilterPoolManagerData() {
		return poolMgrData;
	}

	/**
	 * Return array of SystemFilterPools managed by this manager.
	 */
	public ISystemFilterPool[] getSystemFilterPools() {
		ISystemFilterPool[] result = new ISystemFilterPool[pools.size()];
		pools.toArray(result);
		return result;
		//		//System.out.println("Inside getSFPools for mgr "+getName()+". poolArray null? "+(poolArray==null));
		//		if ((poolArray == null) || (getPools().size() != poolArray.length)) {
		//			List pools = getPools();
		//			poolArray = new ISystemFilterPool[pools.size()];
		//			Iterator i = pools.iterator();
		//			int idx = 0;
		//			while (i.hasNext())
		//				poolArray[idx++] = (ISystemFilterPool) i.next();
		//			//System.out.println("Pool array created. length = "+poolArray.length);
		//		}
		//		return poolArray;
	}

	/**
	 * Get list of filter pool names currently existing.
	 */
	public String[] getSystemFilterPoolNames() {
		ISystemFilterPool[] pools = getSystemFilterPools();
		String[] names = new String[pools.length];
		for (int i = 0; i < pools.length; i++) {
			ISystemFilterPool pool = pools[i];
			names[i] = pool.getName();
		}
		return names;
	}

	/*
	 * Call this to invalidate array cache after any activity
	 */
	//	private void invalidatePoolCache() {
	//		poolArray = null;
	//		poolNames = null;
	//	}

	/**
	 * Create a new filter pool.
	 * Inherits the following attributes from this manager:
	 * <ul>
	 *   <li>data ... the transient data to be associated with every filter pool and filter
	 *   <li>supportsNestedFilters ... whether filters in the pool can themselves contain filters
	 *   <li>save policy
	 *   <li>filter pool folder and file name prefixes
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
	 * <p>
	 * Calls back to inform provider of this event
	 */
	public ISystemFilterPool createSystemFilterPool(String poolName, boolean isDeletable) throws Exception {
		ISystemFilterPool pool = null;
		poolName = poolName.trim();
		if (getSystemFilterPool(poolName) == null) {
			pool = new SystemFilterPool(poolName, supportsNestedFilters(), isDeletable);
			pool.setSystemFilterPoolManager(this);
			pool.setStringsCaseSensitive(areStringsCaseSensitive());
			pool.setSupportsDuplicateFilterStrings(isSetSupportsDuplicateFilterStrings() && supportsDuplicateFilterStrings());
			//			List pools = getPools();
			pools.add(pool);
			//			invalidatePoolCache();
			commit(pool);
			if ((caller != null) && !suspendCallbacks) {
				caller.filterEventFilterPoolCreated(pool);
			}
		}
		return pool;
	}

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
	public void deleteSystemFilterPool(ISystemFilterPool pool) throws Exception {
		IRSEBaseReferencingObject[] refs = pool.getReferencingObjects();
		if (refs != null) {
			for (int idx = 0; idx < refs.length; idx++) {
				if (refs[idx] instanceof ISystemFilterPoolReference) {
					ISystemFilterPoolReference fpRef = (ISystemFilterPoolReference) refs[idx];
					ISystemFilterPoolReferenceManager fprMgr = fpRef.getFilterPoolReferenceManager();
					if (fprMgr != null) {
						fprMgr.removeSystemFilterPoolReference(fpRef, false); // false means don't dereference DWD why?
					}
				}
			}
		}
		pools.remove(pool);
		_profile.setDirty(true);
		_profile.commit();
		getProvider().filterEventFilterPoolDeleted(pool);
	}

	/**
	 * Delete all existing filter pools. Call this when you are about to delete this manager, say.
	 */
	public void deleteAllSystemFilterPools() {
		ISystemFilterPool[] allPools = getSystemFilterPools();
		for (int idx = 0; idx < allPools.length; idx++) {
			String name = allPools[idx].getName();
			try {
				deleteSystemFilterPool(allPools[idx]);
			} catch (Exception exc) {
				logError("Exception deleting filter pool " + name + " from mgr " + getName(), exc); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}

	/**
	 * Pre-test if we are going to run into any trouble renaming a filter pool.
	 * @return true if the pool can be renamed.
	 */
	public boolean preTestRenameFilterPool(ISystemFilterPool pool) throws Exception {
		/*
		 * The default implementation returns true. Persistence providers should be able to handle this
		 * circumstance regardless. If a pool can be renamed internally, it should be able to be
		 * renamed in its persistent form.
		 */
		boolean ok = true;
		return ok;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilterPoolManager#renameSystemFilterPool(org.eclipse.rse.core.filters.ISystemFilterPool, java.lang.String)
	 */
	public void renameSystemFilterPool(ISystemFilterPool pool, String newName) throws Exception {
		String oldName = pool.getName();
		pool.setName(newName);
		// inform all referencees
		IRSEBaseReferencingObject[] refs = pool.getReferencingObjects();
		if (refs != null) {
			for (int idx = 0; idx < refs.length; idx++) {
				IRSEBaseReferencingObject ref = refs[idx];
				if (ref instanceof ISystemFilterPoolReference) {
					ISystemFilterPoolReference fpRef = (ISystemFilterPoolReference) ref;
					ISystemFilterPoolReferenceManager fprMgr = fpRef.getFilterPoolReferenceManager();
					fprMgr.renameReferenceToSystemFilterPool(pool);
				}
			}
		}
		// if caller provider, callback to inform them of this event
		if ((caller != null) && !suspendCallbacks) {
			caller.filterEventFilterPoolRenamed(pool, oldName);
		}
	}

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
	 * @param oldPool The filter pool to copy
	 * @param newName The new name to give the copied pool
	 * @return the new copy
	 */
	public ISystemFilterPool copySystemFilterPool(ISystemFilterPoolManager targetMgr, ISystemFilterPool oldPool, String newName) throws Exception {
		ISystemFilterPool newPool = targetMgr.createSystemFilterPool(newName, oldPool.isDeletable());
		//System.out.println("In SystemFilterPoolManagerImpl#copySystemFilterPool: newPool "+newName+" null? " + (newPool == null));
		oldPool.cloneSystemFilterPool(newPool);
		commit(newPool); // save it all to disk
		return newPool;
	}

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
	public ISystemFilterPool moveSystemFilterPool(ISystemFilterPoolManager targetMgr, ISystemFilterPool oldPool, String newName) throws Exception {
		ISystemFilterPool newPool = copySystemFilterPool(targetMgr, oldPool, newName);
		// find all references to original, and reset them to reference the new...
		IRSEBaseReferencingObject[] refs = oldPool.getReferencingObjects();
		if (refs != null) {
			for (int idx = 0; idx < refs.length; idx++) {
				if (refs[idx] instanceof ISystemFilterPoolReference) {
					ISystemFilterPoolReference fpRef = (ISystemFilterPoolReference) refs[idx];
					//SystemFilterPool fp = fpRef.getReferencedFilterPool();
					ISystemFilterPoolReferenceManager fprMgr = fpRef.getFilterPoolReferenceManager();
					fprMgr.resetSystemFilterPoolReference(fpRef, newPool); // reset the referenced pool
				}
			}
		}
		try {
			deleteSystemFilterPool(oldPool);
		} catch (Exception exc) {
			if (refs != null) {
				for (int idx = 0; idx < refs.length; idx++) {
					if (refs[idx] instanceof ISystemFilterPoolReference) {
						ISystemFilterPoolReference fpRef = (ISystemFilterPoolReference) refs[idx];
						ISystemFilterPoolReferenceManager fprMgr = fpRef.getFilterPoolReferenceManager();
						fprMgr.resetSystemFilterPoolReference(fpRef, oldPool); // reset the referenced pool
					}
				}
			}
			targetMgr.deleteSystemFilterPool(newPool);
			throw exc;
		}
		return newPool;
	}

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
	public void copySystemFilterPools(ISystemFilterPoolManager targetMgr) throws Exception {
		targetMgr.setStringsCaseSensitive(areStringsCaseSensitive());
		ISystemFilterPool[] pools = getSystemFilterPools();
		if ((pools != null) && (pools.length > 0)) {
			targetMgr.suspendCallbacks(true);
			//boolean oldSuspendCallbacks = suspendCallbacks;
			for (int idx = 0; idx < pools.length; idx++) {
				ISystemFilterPool pool = pools[idx];
				copySystemFilterPool(targetMgr, pool, pool.getName());
			}
			//suspendCallbacks = oldSuspendCallbacks;
			targetMgr.suspendCallbacks(false);
		}
	}

	/**
	 * Given a filter pool name, return that filter pool object.
	 * If not found, returns null.
	 */
	public ISystemFilterPool getSystemFilterPool(String name) {
		ISystemFilterPool pool = null;
		ISystemFilterPool[] pools = getSystemFilterPools();
		if (pools != null) {
			for (int idx = 0; (idx < pools.length) && (pool == null); idx++) {
				if (pools[idx].getName().equals(name)) pool = pools[idx];
			}
		}
		return pool;
	}

	/**
	 * Return the first pool that has the default attribute set to true.
	 * If none found, returns null.
	 */
	public ISystemFilterPool getFirstDefaultSystemFilterPool() {
		ISystemFilterPool pool = null;
		ISystemFilterPool[] pools = getSystemFilterPools();
		for (int idx = 0; (pool == null) && (idx < pools.length); idx++)
			if (pools[idx].isDefault()) pool = pools[idx];
		return pool;
	}

	// ---------------------------------
	// FILTER METHODS
	// ---------------------------------

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilterPoolManager#createSystemFilter(org.eclipse.rse.core.filters.ISystemFilterContainer, java.lang.String, java.util.List, java.lang.String, boolean)
	 */
	public ISystemFilter createSystemFilter(ISystemFilterContainer parent, String aliasName, List filterStrings, String type, boolean promptable) throws Exception {
		String[] filterStringsArray = new String[filterStrings.size()];
		filterStrings.toArray(filterStringsArray);
		ISystemFilter result = doCreateSystemFilter(parent, aliasName, filterStringsArray, type, promptable);
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilterPoolManager#createSystemFilter(org.eclipse.rse.core.filters.ISystemFilterContainer, java.lang.String, java.util.List, java.lang.String)
	 */
	public ISystemFilter createSystemFilter(ISystemFilterContainer parent, String aliasName, List filterStrings, String type) throws Exception {
		ISystemFilter result = createSystemFilter(parent, aliasName, filterStrings, type, false);
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilterPoolManager#createSystemFilter(org.eclipse.rse.core.filters.ISystemFilterContainer, java.lang.String, java.util.List)
	 */
	public ISystemFilter createSystemFilter(ISystemFilterContainer parent, String aliasName, List filterStrings) throws Exception {
		ISystemFilter result = createSystemFilter(parent, aliasName, filterStrings, null, false);
		return result;
	}

	/**
	 * Creates a new system filter within the given filter container (either a filter pool, or
	 *  a filter). This creates the filter, and then saves the filter pool.
	 * <p>Calls back to inform provider of this event (filterEventFilterCreated)
	 * @param parent The parent which is either a SystemFilterPool or a SystemFilter
	 * @param aliasName The name to give the new filter. Must be unique for this pool.
	 * @param filterStrings The list of String objects that represent the filter strings.
	 */
	public ISystemFilter createSystemFilter(ISystemFilterContainer parent, String aliasName, String[] filterStrings) throws Exception {
		ISystemFilter newFilter = doCreateSystemFilter(parent, aliasName, filterStrings, null, false);
		return newFilter;
	}

	/**
	 * Creates a new system filter that is typed.
	 * Same as {@link #createSystemFilter(ISystemFilterContainer, String, String[])} but
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
	public ISystemFilter createSystemFilter(ISystemFilterContainer parent, String aliasName, String[] filterStrings, String type) throws Exception {
		ISystemFilter newFilter = doCreateSystemFilter(parent, aliasName, filterStrings, type, false);
		return newFilter;
	}

	/**
	 * Creates a new system filter that is typed and promptable
	 * Same as {@link #createSystemFilter(ISystemFilterContainer, String ,String[], String)} but
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
	public ISystemFilter createSystemFilter(ISystemFilterContainer parent, String aliasName, String[] filterStrings, String type, boolean promptable) throws Exception {
		ISystemFilter newFilter = doCreateSystemFilter(parent, aliasName, filterStrings, type, promptable);
		return newFilter;
	}

	/**
	 * Creates a system filter.
	 * @param parent the owning parent of this filter, must be a filter pool or a filter capable of nesting
	 * @param name the name of this filter
	 * @param filterStrings the strings associated with this filter
	 * @param type the type of this filter, used only if inheritType is false
	 * @param promptable the promptable nature of this filter, used only if inheritPromptable is false
	 * @return
	 */
	private ISystemFilter doCreateSystemFilter(ISystemFilterContainer parent, String name, String[] filterStrings, String type, boolean promptable) {
		ISystemFilterPool parentPool = null;
		if (parent instanceof ISystemFilterPool) {
			parentPool = (ISystemFilterPool) parent;
		} else {
			parentPool = ((ISystemFilter) parent).getParentFilterPool();
		}
		ISystemFilter newFilter = parentPool.createSystemFilter(name, filterStrings);
		newFilter.setType(type);
		newFilter.setPromptable(promptable);
		if (!suspendSave) {
			commit(parentPool);
		}
		if ((caller != null) && !suspendCallbacks) {
			caller.filterEventFilterCreated(newFilter);
		}
		return newFilter;
	}

	/**
	 * Delete an existing system filter.
	 * Does the following:
	 * <ul>
	 *   <li>Removes filter from its parent in memory.
	 *   <li>If appropriate for the save policy, deletes the filter's file from disk.
	 *   <li>Save the SystemFilterPool which direct or indirectly contains the filter.
	 *   <li>Calls back to provider to inform it of this event (filterEventFilterDeleted)
	 * </ul>
	 */
	public boolean deleteSystemFilter(ISystemFilter filter) throws Exception {

		// ok to proceed...
		boolean ok = true;
		ISystemFilterContainer parent = filter.getParentFilterContainer();
		parent.deleteSystemFilter(filter);
		commit(filter.getParentFilterPool());

		// if caller provider, callback to inform them of this event
		if ((caller != null) && !suspendCallbacks) caller.filterEventFilterDeleted(filter);
		return ok;
	}

	/**
	 * Renames a filter. This is better than filter.setName(String newName) as it
	 *  saves the parent pool to disk.
	 * <p>
	 * Does the following:
	 * <ul>
	 *   <li>Renames the object in the in-memory cache
	 *   <li>If appropriate for the save policy, rename's the filter's file on disk.
	 *   <li>Save parent filter pool's in-memory object to disk.
	 *   <li>Calls back to provider to inform it of this event (filterEventFilterRenamed)
	 * </ul>
	 * Does fire an event.
	 */
	public void renameSystemFilter(ISystemFilter filter, String newName) throws Exception {

		// ok to proceed
		ISystemFilterContainer parent = filter.getParentFilterContainer();
		String oldName = filter.getName();
		parent.renameSystemFilter(filter, newName);
		// rename on disk
		try {

			commit(filter.getParentFilterPool());
		} catch (Exception exc) {
			parent.renameSystemFilter(filter, oldName); // rollback name change
			throw exc;
		}
		// if caller provider, callback to inform them of this event
		if ((caller != null) && !suspendCallbacks) caller.filterEventFilterRenamed(filter, oldName);
	}

	/**
	 * Updates a filter. This is better than doing it directly as it saves it to disk.
	 * <p>
	 * Does the following:
	 * <ul>
	 *   <li>Updates the object in the in-memory cache
	 *   <li>Save parent filter pool's in-memory object to disk.
	 *   <li>Calls back to provider to inform it of this event (filterEventFilterUpdated).
	 * </ul>
	 */
	public void updateSystemFilter(ISystemFilter filter, String newName, String[] strings) throws Exception {

		// ok to proceed...
		ISystemFilterContainer parent = filter.getParentFilterContainer();
		String oldName = filter.getName();
		boolean rename = !oldName.equals(newName);
		if (rename) {
			renameSystemFilter(filter, newName);
		}
		parent.updateSystemFilter(filter, newName, strings);
		ISystemFilterPool parentPool = filter.getParentFilterPool();
		commit(parentPool);
		if ((caller != null) && !suspendCallbacks) caller.filterEventFilterUpdated(filter);
	}

	/**
	 * Sets a filter's type. This is better than calling filter.setType(String) directly as it saves the filter to disk after.
	 * <p>
	 * A filter's type is an arbitrary string that is not interpreted or used by the base framework. This
	 * is for use entirely by tools who wish to support multiple types of filters and be able to launch unique
	 * actions per type, say.
	 * @param filter The filter to be modified
	 * @param newType The type of this filter
	 */
	public void setSystemFilterType(ISystemFilter filter, String newType) throws Exception {
		filter.setType(newType);
		commit(filter.getParentFilterPool());
	}

	/**
	 * Copy a system filter to a pool in this or another filter manager.
	 */
	public ISystemFilter copySystemFilter(ISystemFilterPool targetPool, ISystemFilter oldFilter, String newName) throws Exception {
		ISystemFilterPoolManager targetMgr = targetPool.getSystemFilterPoolManager();
		ISystemFilterPool oldPool = oldFilter.getParentFilterPool();

		targetMgr.suspendCallbacks(true);

		ISystemFilter newFilter = oldPool.copySystemFilter(targetPool, oldFilter, newName); // creates it in memory
		commit(targetPool); // save updated pool to disk

		targetMgr.suspendCallbacks(false);

		targetMgr.getProvider().filterEventFilterCreated(newFilter);
		return newFilter;
	}

	/**
	 * Move a system filter to a pool in this or another filter manager.
	 * Does this by first copying the filter, and only if successful, deleting the old copy.
	 */
	public ISystemFilter moveSystemFilter(ISystemFilterPool targetPool, ISystemFilter oldFilter, String newName) throws Exception {
		ISystemFilter newFilter = copySystemFilter(targetPool, oldFilter, newName);
		if (newFilter != null) {
			deleteSystemFilter(oldFilter);
		}
		return newFilter;
	}

	/**
	 * Return the zero-based position of a SystemFilter object within its container
	 */
	public int getSystemFilterPosition(ISystemFilter filter) {
		ISystemFilterContainer container = filter.getParentFilterContainer();
		int position = -1;
		boolean match = false;
		ISystemFilter[] filters = container.getSystemFilters();

		for (int idx = 0; !match && (idx < filters.length); idx++) {
			if (filters[idx].getName().equals(filter.getName())) {
				match = true;
				position = idx;
			}
		}
		return position;
	}

	/**
	 * Move existing filters a given number of positions in the same container.
	 * If the delta is negative, they are all moved up by the given amount. If
	 * positive, they are all moved down by the given amount.<p>
	 * <p>
	 * Does the following:
	 * <ul>
	 * <li>After the move, the pool containing the filter is saved to disk.
	 * <li>Calls back to provider to inform of this event
	 * </ul>
	 * @param filters Array of SystemFilters to move.
	 * @param delta the amount by which to move the filters (filterEventFiltersRePositioned)
	 */
	public void moveSystemFilters(ISystemFilter filters[], int delta) throws Exception {
		int[] oldPositions = new int[filters.length];
		for (int idx = 0; idx < filters.length; idx++) {
			oldPositions[idx] = getSystemFilterPosition(filters[idx]);
		}
		if (delta > 0) { // moving down, process backwards
			for (int idx = filters.length - 1; idx >= 0; idx--) {
				moveFilter(filters[idx], oldPositions[idx] + delta);
			}
		} else {
			for (int idx = 0; idx < filters.length; idx++) {
				moveFilter(filters[idx], oldPositions[idx] + delta);
			}
		}
		commit(filters[0].getParentFilterPool());
		if ((caller != null) && !suspendCallbacks) {
			caller.filterEventFiltersRePositioned(filters, delta);
		}
	}

	/**
	 * Move one filter to new zero-based position.
	 */
	private void moveFilter(ISystemFilter filter, int newPos) {
		ISystemFilterContainer container = filter.getParentFilterContainer();
		container.moveSystemFilter(newPos, filter);
	}

	/**
	 * Order filters according to user preferences.
	 * <p>
	 * While the framework has all the code necessary to arrange filters and save/restore
	 * that arrangement, you may choose to use preferences instead of this support.
	 * In this case, call this method and pass in the saved and sorted filter name list.
	 * <p>
	 * Called by someone after restore.
	 */
	public void orderSystemFilters(ISystemFilterPool pool, String[] names) throws Exception {
		pool.orderSystemFilters(names);
		commit(pool);
	}

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
	public ISystemFilterString addSystemFilterString(ISystemFilter filter, String newString) throws Exception {
		ISystemFilterString newFilterString = filter.addFilterString(newString);
		ISystemFilterPool parentPool = filter.getParentFilterPool();
		commit(parentPool);
		if ((caller != null) && !suspendCallbacks) caller.filterEventFilterStringCreated(newFilterString);
		return newFilterString;
	}

	/**
	 * Insert a new filter string to the its filters' list, at the given zero-based position
	 * <p>
	 * Does the following:
	 * <ul>
	 *   <li>Adds the filter string to the in-memory cache
	 *   <li>Saves parent filter pool to disk.
	 *   <li>Calls back to provider to inform it of this event (filterEventFilterStringCreated)
	 * </ul>
	 */
	public ISystemFilterString addSystemFilterString(ISystemFilter filter, String newString, int position) throws Exception {
		ISystemFilterString newFilterString = filter.addFilterString(newString, position);
		ISystemFilterPool parentPool = filter.getParentFilterPool();
		commit(parentPool);
		if ((caller != null) && !suspendCallbacks) caller.filterEventFilterStringCreated(newFilterString);
		return newFilterString;
	}

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
	public boolean removeSystemFilterString(ISystemFilter filter, String oldString) throws Exception {
		ISystemFilterString oldFilterString = filter.removeFilterString(oldString);
		if (oldFilterString == null) return false;
		ISystemFilterPool parentPool = filter.getParentFilterPool();
		commit(parentPool);
		if ((caller != null) && !suspendCallbacks) caller.filterEventFilterStringDeleted(oldFilterString);
		return true;
	}

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
	public boolean removeSystemFilterString(ISystemFilter filter, ISystemFilterString filterString) throws Exception {
		boolean ok = filter.removeFilterString(filterString);
		if (!ok) return false;
		ISystemFilterPool parentPool = filter.getParentFilterPool();
		commit(parentPool);
		if ((caller != null) && !suspendCallbacks) caller.filterEventFilterStringDeleted(filterString);
		return ok;
	}

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
	public boolean removeSystemFilterString(ISystemFilter filter, int position) throws Exception {
		ISystemFilterString oldFilterString = filter.removeFilterString(position);
		if (oldFilterString == null) return false;
		ISystemFilterPool parentPool = filter.getParentFilterPool();
		commit(parentPool);
		if ((caller != null) && !suspendCallbacks) caller.filterEventFilterStringDeleted(oldFilterString);
		return true;
	}

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
	public void updateSystemFilterString(ISystemFilterString filterString, String newValue) throws Exception {
		if (newValue.equals(filterString.getString())) return;
		ISystemFilter filter = filterString.getParentSystemFilter();
		filter.updateFilterString(filterString, newValue);
		ISystemFilterPool parentPool = filter.getParentFilterPool();
		commit(parentPool);
		if ((caller != null) && !suspendCallbacks) caller.filterEventFilterStringUpdated(filterString);
	}

	/**
	 * Return the zero-based position of a SystemFilterString object within its filter
	 */
	public int getSystemFilterStringPosition(ISystemFilterString filterString) {
		ISystemFilter filter = filterString.getParentSystemFilter();
		int position = -1;
		boolean match = false;
		ISystemFilterString[] filterStrings = filter.getSystemFilterStrings();

		String matchString = filterString.getString();
		for (int idx = 0; !match && (idx < filterStrings.length); idx++) {
			if (filterStrings[idx].getString().equals(matchString)) {
				match = true;
				position = idx;
			}
		}
		return position;
	}

	/**
	 * Copy a system filter string to a filter in this or another filter pool manager.
	 */
	public ISystemFilterString copySystemFilterString(ISystemFilter targetFilter, ISystemFilterString oldFilterString) throws Exception {
		ISystemFilterPool targetPool = targetFilter.getParentFilterPool();
		ISystemFilterPoolManager targetMgr = targetPool.getSystemFilterPoolManager();
		ISystemFilter oldFilter = oldFilterString.getParentSystemFilter();
		targetMgr.suspendCallbacks(true);
		ISystemFilterString newFilterString = oldFilter.copySystemFilterString(targetFilter, oldFilterString); // creates it in memory
		commit(targetPool); // save updated pool to disk
		targetMgr.suspendCallbacks(false);
		targetMgr.getProvider().filterEventFilterStringCreated(newFilterString);
		return newFilterString;
	}

	/**
	 * Move a system filter string to a filter in this or another filter pool manager.
	 * Does this by doing a copy operation, then if successful doing a delete operation.
	 */
	public ISystemFilterString moveSystemFilterString(ISystemFilter targetFilter, ISystemFilterString oldFilterString) throws Exception {
		ISystemFilterString newString = copySystemFilterString(targetFilter, oldFilterString);
		if (newString != null) {
			removeSystemFilterString(oldFilterString.getParentSystemFilter(), oldFilterString);
		}
		return newString;
	}

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
	 * @param delta the amount by which to move the filter strings
	 */
	public void moveSystemFilterStrings(ISystemFilterString filterStrings[], int delta) throws Exception {
		ISystemFilter filter = filterStrings[0].getParentSystemFilter();
		int[] oldPositions = new int[filterStrings.length];
		for (int idx = 0; idx < filterStrings.length; idx++)
			oldPositions[idx] = getSystemFilterStringPosition(filterStrings[idx]);
		if (delta > 0) // moving down, process backwards
			for (int idx = filterStrings.length - 1; idx >= 0; idx--)
				moveFilterString(filter, filterStrings[idx], oldPositions[idx] + delta);
		else
			for (int idx = 0; idx < filterStrings.length; idx++)
				moveFilterString(filter, filterStrings[idx], oldPositions[idx] + delta);

		commit(filter.getParentFilterPool());

		// if caller provider, callback to inform them of this event
		if ((caller != null) && !suspendCallbacks) caller.filterEventFilterStringsRePositioned(filterStrings, delta);
	}

	/**
	 * Move one filter string to new zero-based position.
	 */
	private void moveFilterString(ISystemFilter filter, ISystemFilterString filterString, int newPos) {
		filter.moveSystemFilterString(newPos, filterString);
	}

	// -----------------------------------
	// SUSPEND/RESUME CALLBACKS METHODS...
	// -----------------------------------
	/**
	 * Suspend callbacks to the provider
	 */
	public void suspendCallbacks(boolean suspend) {
		suspendCallbacks = suspend;
	}

	// -----------------------
	// SAVE/RESTORE METHODS...
	// -----------------------
	/**
	 * Return the save file that will be written for the given filter pool.
	 * Will depend on this manager's save policy.
	 */
	public IFile getSaveFile(ISystemFilterPool pool) {
		/* FIXME
		 switch(savePolicy)
		 {
		 // ONE FILE PER FILTER POOL MANAGER
		 case SystemFilterConstants.SAVE_POLICY_ONE_FILE_PER_MANAGER:
		 return SystemMOFHelpers.getSaveFile(getFolder(), getRootSaveFileName(this));
		 // ONE FOLDER AND FILE PER FILTER POOL
		 case SystemFilterConstants.SAVE_POLICY_ONE_FILEANDFOLDER_PER_POOL:
		 // ONE FILE PER FILTER POOL, ONE FOLDER PER MANAGER
		 case SystemFilterConstants.SAVE_POLICY_ONE_FILE_PER_POOL_SAME_FOLDER:
		 // ONE FILE PER FILTER
		 case SystemFilterConstants.SAVE_POLICY_ONE_FILE_PER_FILTER:
		 return ((SystemFilterPoolImpl)pool).getSaveFile();
		 }
		 */
		return null;
	}

	/**
	 * Return our logger
	 */
	public Logger getLogger() {
		if (logger == null) logger = RSECorePlugin.getDefault().getLogger();
		return logger;
	}

	/**
	 * Set our logger
	 */
	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	/**
	 * Helper method for logging information
	 * 
	 * @param message Message to be written to the log file
	 */
	public void logInfo(String message) {
		if (logger != null)
			logger.logInfo(message);
		else
			System.out.println(getClass().getName() + ": INFO: " + message); //$NON-NLS-1$
	}

	/**
	 * Helper method for logging warnings
	 * 
	 * @param message Message to be written to the log file
	 */
	public void logWarning(String message) {
		if (logger != null)
			logger.logWarning(message);
		else
			System.out.println(getClass().getName() + ": WARNING: " + message); //$NON-NLS-1$
	}

	/**
	 * Helper method for logging errors (exceptions)
	 * 
	 * @param message Message to be written to the log file
	 * 
	 * @param exception Any exception that generated the error condition,
	 *                  this will be used to print a stack trace in the log file.
	 */
	public void logError(String message, Throwable exception) {
		if (logger != null)
			logger.logError(message, exception);
		else {
			String msg = exception.getMessage();
			if (msg == null) msg = exception.getClass().getName();
			System.out.println(getClass().getName() + ": " + message + ": " + msg); //$NON-NLS-1$ //$NON-NLS-2$
			exception.printStackTrace();
		}
	}

	/**
	 * Helper method for logging debug messages
	 * 
	 * @param prefix typically the name of the class issuing the debug message.  Pass in either
	 *                  retrieved using this.getClass() (for non-static methods)
	 *                  or using MyClass.class (for static methods)
	 * 
	 * @param message Message to be written to the log file
	 */
	public void logDebugMessage(String prefix, String message) {
		if ((logger != null)) {
			logger.logDebugMessage(prefix, message);
		} else
			System.out.println(getClass().getName() + ": DEBUG: " + message); //$NON-NLS-1$
	}

	/**
	 * Helper method for logging trace information
	 * 
	 * @deprecated Use either logInfo, logWarning, logError, or logDebugMessage.  This
	 * method now calls logInfo.
	 */
	public void logMessage(String msg) {
		if (logger != null)
			logger.logInfo(msg);
		else
			System.out.println(getClass().getName() + ": " + msg); //$NON-NLS-1$
	}

	// OTHER

	public String toString() {
		return getName();
	}

	/**
	 * @generated This field/method will be replaced during code generation
	 */
	public String getName() {
		return name;
	}

	/**
	 * @generated This field/method will be replaced during code generation
	 */
	public boolean isSupportsNestedFilters() {
		return supportsNestedFilters;
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public void setSupportsNestedFiltersGen(boolean newSupportsNestedFilters) {
		supportsNestedFilters = newSupportsNestedFilters;
	}

	/**
	 * @generated This field/method will be replaced during code generation
	 */
	public boolean isSupportsDuplicateFilterStrings() {
		return supportsDuplicateFilterStrings;
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public void unsetSupportsDuplicateFilterStrings() {
		supportsDuplicateFilterStrings = SUPPORTS_DUPLICATE_FILTER_STRINGS_EDEFAULT;
		supportsDuplicateFilterStringsESet = false;
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public boolean isSetSupportsDuplicateFilterStrings() {
		return supportsDuplicateFilterStringsESet;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilterPoolManager#isSingleFilterStringOnly()
	 */
	public boolean isSingleFilterStringOnly() {
		return singleFilterStringOnly;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilterPoolManager#setSingleFilterStringOnly(boolean)
	 */
	public void setSingleFilterStringOnly(boolean newSingleFilterStringOnly) {
		boolean oldSingleFilterStringOnly = singleFilterStringOnly;
		if (oldSingleFilterStringOnly != newSingleFilterStringOnly) {
			singleFilterStringOnly = newSingleFilterStringOnly;
			setDirty(true);
		}
	}

	/**
	 * Save all the filter pools to disk.
	 * Uses the save policy specified in this manager's factory method.
	 */
	public boolean commit() {
		ISystemProfile profile = getSystemProfile();
		boolean result = profile.commit();
		return result;
	}

	/**
	 * Save a specific filter pool.
	 */
	public boolean commit(ISystemFilterPool pool) {
		pool.setDirty(true);
		boolean result = pool.commit();
		return result;
	}

	public IRSEPersistableContainer getPersistableParent() {
		return null;
	}

	public IRSEPersistableContainer[] getPersistableChildren() {
		return IRSEPersistableContainer.NO_CHILDREN;
	}

}
