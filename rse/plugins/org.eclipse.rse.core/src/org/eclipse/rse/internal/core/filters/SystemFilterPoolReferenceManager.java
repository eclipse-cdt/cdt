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
 * David Dykstal (IBM) - [197036] fixed parent references and names so that delete references would function correctly
 * David Dykstal (IBM) - [213353] fix move of filter pool references
 *******************************************************************************/

package org.eclipse.rse.internal.core.filters;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.eclipse.rse.core.filters.ISystemFilter;
import org.eclipse.rse.core.filters.ISystemFilterPool;
import org.eclipse.rse.core.filters.ISystemFilterPoolManager;
import org.eclipse.rse.core.filters.ISystemFilterPoolManagerProvider;
import org.eclipse.rse.core.filters.ISystemFilterPoolReference;
import org.eclipse.rse.core.filters.ISystemFilterPoolReferenceManager;
import org.eclipse.rse.core.filters.ISystemFilterPoolReferenceManagerProvider;
import org.eclipse.rse.core.filters.ISystemFilterReference;
import org.eclipse.rse.core.model.IRSEPersistableContainer;
import org.eclipse.rse.core.references.IRSEBasePersistableReferencingObject;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.references.SystemPersistableReferenceManager;

/**
 * This class manages a persistable list of objects each of which reference
 * a filter pool. This class builds on the parent class SystemPersistableReferenceManager,
 * offering convenience versions of the parent methods that are typed to the
 * classes in the filters framework.
 * 
 * There will be one of these instantiated for a subsystem. Filter pool references can 
 * be moved within a subsystem and this manager provides that function as well.
 */
public class SystemFilterPoolReferenceManager extends SystemPersistableReferenceManager implements ISystemFilterPoolReferenceManager {
	private ISystemFilterPoolManagerProvider poolMgrProvider = null;
	private ISystemFilterPoolManager defaultPoolMgr = null;
	private ISystemFilterPoolReferenceManagerProvider caller = null;
	private Object mgrData = null;
	private boolean initialized = false;
	private boolean noEvents;
	private boolean fireEvents = true;
	private ISystemFilterPoolReference[] fpRefsArray = null;
	private static final ISystemFilterPoolReference[] emptyFilterPoolRefArray = new ISystemFilterPoolReference[0];

	/**
	 * Default constructor. Typically called by MOF factory methods.
	 */
	public SystemFilterPoolReferenceManager() {
		super();
	}

	/**
	 * A factory method to create a SystemFilterPoolReferenceManager instance.
	 * @param caller Objects which instantiate this class should implement the
	 *   SystemFilterPoolReferenceManagerProvider interface, and pass "this" for this parameter.
	 *   Given any filter framework object, it is possible to retrieve the caller's
	 *   object via the getProvider method call.
	 * @param relatedPoolManagerProvider The managers that owns the master list of filter pools that 
	 *   this manager will contain references to.
	 * @param name the name of the filter pool reference manager.
	 * @return a filter pool reference manager
	 */
	public static ISystemFilterPoolReferenceManager createSystemFilterPoolReferenceManager(ISystemFilterPoolReferenceManagerProvider caller,	ISystemFilterPoolManagerProvider relatedPoolManagerProvider, String name) {
		SystemFilterPoolReferenceManager mgr = null;

		if (mgr == null) // not found or some serious error.
		{
			mgr = createManager();
		}
		if (mgr != null) {
			mgr.initialize(caller, name, relatedPoolManagerProvider);
		}

		return mgr;
	}

	/*
	 * Private helper method.
	 */
	protected static SystemFilterPoolReferenceManager createManager() {
		ISystemFilterPoolReferenceManager mgr = new SystemFilterPoolReferenceManager();
		return (SystemFilterPoolReferenceManager) mgr;
	}

	/*
	 * Private helper method to initialize state
	 */
	protected void initialize(ISystemFilterPoolReferenceManagerProvider caller, String name, ISystemFilterPoolManagerProvider relatedPoolManagerProvider) {
		if (!initialized) initialize(caller, name); // core data
		//setSystemFilterPoolManagers(relatedPoolManagers);
		setSystemFilterPoolManagerProvider(relatedPoolManagerProvider);
	}

	/*
	 * Private helper method to do core initialization.
	 * Might be called from either the static factory method or the static restore method.
	 */
	protected void initialize(ISystemFilterPoolReferenceManagerProvider caller, String name) {
		setProvider(caller);
		setName(name);
		initialized = true;
	}

	private void invalidateFilterPoolReferencesCache() {
		fpRefsArray = null;
		invalidateCache();
	}

	// ------------------------------------------------------------
	// Methods for setting and querying attributes
	// ------------------------------------------------------------
	/**
	 * Set the associated master pool manager provider. Note the provider
	 * typically manages multiple pool managers and we manage references
	 * across those.
	 * @param poolMgrProvider the factory (provider) for the filter pool managers that this 
	 * reference manager provides services to
	 */
	public void setSystemFilterPoolManagerProvider(ISystemFilterPoolManagerProvider poolMgrProvider) {
		this.poolMgrProvider = poolMgrProvider;
	}

	/**
	 * @return the associated master pool manager provider. Note the provider
	 * typically manages multiple pool managers and we manage references
	 * across those.
	 */
	public ISystemFilterPoolManagerProvider getSystemFilterPoolManagerProvider() {
		return poolMgrProvider;
	}

	/**
	 * @return the managers of the master list of filter pools, from which
	 * objects in this list reference.
	 */
	public ISystemFilterPoolManager[] getSystemFilterPoolManagers() {
		ISystemFilterPoolManager[] result = new ISystemFilterPoolManager[0];
		if (poolMgrProvider != null) {
			result = poolMgrProvider.getSystemFilterPoolManagers();
		}
		return result;
	}

	/**
	 * @return the managers of the master list of filter pools, from which
	 * objects in this list reference, but which are not in the list of
	 * managers our pool manager supplier gives us. That is, these are
	 * references to filter pools outside the expected list.
	 */
	public ISystemFilterPoolManager[] getAdditionalSystemFilterPoolManagers() {
		ISystemFilterPoolManager[] poolMgrs = getSystemFilterPoolManagers();
		Vector v = new Vector();
		ISystemFilterPoolReference[] fpRefs = getSystemFilterPoolReferences();
		for (int idx = 0; idx < fpRefs.length; idx++) {
			ISystemFilterPool pool = fpRefs[idx].getReferencedFilterPool();
			if (pool != null) {
				ISystemFilterPoolManager mgr = pool.getSystemFilterPoolManager();
				if (!managerExists(poolMgrs, mgr) && !v.contains(mgr)) {
					System.out.println("Found unmatched manager: " + mgr.getName()); //$NON-NLS-1$
					v.addElement(mgr);
				}
			}
		}
		ISystemFilterPoolManager[] additionalMgrs = null;
		if (v.size() > 0) {
			additionalMgrs = new ISystemFilterPoolManager[v.size()];
			for (int idx = 0; idx < v.size(); idx++)
				additionalMgrs[idx] = (ISystemFilterPoolManager) v.elementAt(idx);
		}
		return additionalMgrs;
	}

	/**
	 * Look for a pool manager in an array of pool managers.
	 * @param mgrs the array in which to look
	 * @param mgr the item to look for
	 * @return true if the manager was found
	 */
	private boolean managerExists(ISystemFilterPoolManager[] mgrs, ISystemFilterPoolManager mgr) {
		boolean match = false;
		for (int idx = 0; !match && (idx < mgrs.length); idx++)
			if (mgr == mgrs[idx]) match = true;
		return match;
	}

	/**
	 * Set the default manager of the master list of filter pools, from which
	 * objects in this list reference.
	 * @param mgr the filter pool manager that is the default pool manager.
	 */
	public void setDefaultSystemFilterPoolManager(ISystemFilterPoolManager mgr) {
		defaultPoolMgr = mgr;
	}

	/**
	 * @return the default manager of the master list of filter pools, from which
	 * objects in this list reference.
	 */
	public ISystemFilterPoolManager getDefaultSystemFilterPoolManager() {
		return defaultPoolMgr;
	}

	/**
	 * @return the object (the "provider" or factory) which instantiated 
	 * this instance of the filter pool reference manager.
	 * This is also available from any filter reference framework object.
	 */
	public ISystemFilterPoolReferenceManagerProvider getProvider() {
		return caller;
	}

	/**
	 * Set the object which instantiated this instance of the filter pool reference manager.
	 * This makes it available to retrieve from any filter reference framework object,
	 * via the ubiquitous getProvider interface method.
	 * @param caller the factory that created this instance.
	 */
	public void setProvider(ISystemFilterPoolReferenceManagerProvider caller) {
		this.caller = caller;
	}

	/**
	 * Turn callbacks to the provider either off or on.
	 * @param fireEvents true if events are to be fired to the provider object, false if not.
	 */
	public void setProviderEventNotification(boolean fireEvents) {
		this.fireEvents = fireEvents;
	}

	/**
	 * This is to set transient data that is subsequently queryable.
	 * @param data the data associated with this pool reference manager.
	 */
	public void setSystemFilterPoolReferenceManagerData(Object data) {
		this.mgrData = data;
	}

	/**
	 * @return transient data set via setFilterPoolData.
	 */
	public Object getSystemFilterPoolReferenceManagerData() {
		return mgrData;
	}

	// ---------------------------------------------------
	// Methods that work on FilterPool referencing objects
	// ---------------------------------------------------
	/**
	 * Ask each referenced pool for its name, and update it.
	 * Called after the name of the pool or its manager changes.
	 */
	public void regenerateReferencedSystemFilterPoolNames() {
		ISystemFilterPoolReference[] fpRefs = getSystemFilterPoolReferences();
		for (int idx = 0; idx < fpRefs.length; idx++) {
			ISystemFilterPool pool = fpRefs[idx].getReferencedFilterPool();
			if (pool != null) fpRefs[idx].resetReferencedFilterPoolName(pool.getReferenceName());
		}
		invalidateFilterPoolReferencesCache(); // just in case!
	}

	/**
	 * @return array of SystemFilterPoolReference objects.
	 * Result will never be null, although it may be an array of length zero.
	 */
	public ISystemFilterPoolReference[] getSystemFilterPoolReferences() {
		IRSEBasePersistableReferencingObject[] refObjs = super.getReferencingObjects();
		if (refObjs.length == 0)
			return emptyFilterPoolRefArray;
		else if ((fpRefsArray == null) || (fpRefsArray.length != refObjs.length)) {
			fpRefsArray = new ISystemFilterPoolReference[refObjs.length];
			for (int idx = 0; idx < fpRefsArray.length; idx++)
				fpRefsArray[idx] = (ISystemFilterPoolReference) refObjs[idx];
		}
		return fpRefsArray;
	}

	/**
	 * In one shot, set the filter pool references. Calls back to inform provider.
	 * @param filterPoolReferences an array of filter pool reference objects to set the list to.
	 * @param deReference true to first de-reference all objects in the existing list.
	 */
	public void setSystemFilterPoolReferences(ISystemFilterPoolReference[] filterPoolReferences, boolean deReference) {
		super.setReferencingObjects(filterPoolReferences, deReference);
		invalidateFilterPoolReferencesCache();
		// callback to provider so they can fire events in their GUI
		if (fireEvents && (caller != null)) caller.filterEventFilterPoolReferencesReset();
	}

	/**
	 * Create a filter pool reference. This creates a raw reference that must be added to the managed
	 * lists by the caller.
	 */
	private ISystemFilterPoolReference createSystemFilterPoolReference(ISystemFilterPool filterPool) {
		ISystemFilterPoolReference filterPoolReference = new SystemFilterPoolReference(filterPool);
		filterPoolReference.setParentReferenceManager(this);
		invalidateFilterPoolReferencesCache();
		return filterPoolReference;
	}

	/**
	 * Create a filter pool reference. This creates an unresolved raw reference that
	 * must be added to the managed lists by the caller.
	 * That will be attempted to be resolved on first use.
	 * @param filterPoolName the fully qualified filter pool name
	 */
	private ISystemFilterPoolReference createSystemFilterPoolReference(String filterPoolName) {
		ISystemFilterPoolReference filterPoolReference = new SystemFilterPoolReference(filterPoolName);
		filterPoolReference.setParentReferenceManager(this);
		invalidateFilterPoolReferencesCache();
		return filterPoolReference;
	}

	/**
	 * Add a filter pool referencing object to the list. 
	 * @param filterPoolReference a reference to add to this manager
	 * @return the new count of referencing objects
	 */
	public int addSystemFilterPoolReference(ISystemFilterPoolReference filterPoolReference) {
		int count = addReferencingObject(filterPoolReference);
		filterPoolReference.setParentReferenceManager(this); // DWD - should be done in addReferencingObject?
		invalidateFilterPoolReferencesCache();
		return count;
	}

	/**
	 * Reset the filter pool a reference points to. Called on a move-filter-pool operation
	 * @param filterPoolReference the reference to fix up
	 * @param newPool the new pool to reference
	 */
	public void resetSystemFilterPoolReference(ISystemFilterPoolReference filterPoolReference, ISystemFilterPool newPool) {
		filterPoolReference.removeReference();
		filterPoolReference.setReferencedObject(newPool);
		if (fireEvents && (caller != null)) caller.filterEventFilterPoolReferenceReset(filterPoolReference);
	}

	/**
	 * Remove a filter pool referencing object from the list.
	 * @param filterPoolReference the reference to remove
	 * @param deReference true if we want to dereference the referenced object (call removeReference on it)
	 * @return the new count of referencing objects
	 */
	public int removeSystemFilterPoolReference(ISystemFilterPoolReference filterPoolReference, boolean deReference) {
		int count = 0;
		if (!deReference)
			count = super.removeReferencingObject(filterPoolReference);
		else
			count = super.removeAndDeReferenceReferencingObject(filterPoolReference);
		invalidateFilterPoolReferencesCache();
		if (fireEvents && (caller != null)) caller.filterEventFilterPoolReferenceDeleted(filterPoolReference);
		return count;
	}

	/**
	 * @return count of referenced filter pools
	 */
	public int getSystemFilterPoolReferenceCount() {
		return super.getReferencingObjectCount();
	}

	/**
	 * @param filterPoolRef the filter pool reference to search for
	 * @return the zero-based position of the reference within this manager
	 */
	public int getSystemFilterPoolReferencePosition(ISystemFilterPoolReference filterPoolRef) {
		return super.getReferencingObjectPosition(filterPoolRef);
	}

	/**
	 * Move a given filter pool reference to a given zero-based location.
	 * Calls back to inform provider of the event.
	 * @param filterPoolRef the reference to move
	 * @param pos the new position at which to move it. References at that position and beyond are
	 * moved up in the list.
	 */
	public void moveSystemFilterPoolReference(ISystemFilterPoolReference filterPoolRef, int pos) {
		int oldPos = getReferencingObjectPosition(filterPoolRef);
		moveReferencingObjectPosition(pos, filterPoolRef);
		invalidateFilterPoolReferencesCache();
		if (fireEvents && (caller != null) && !noEvents) {
			ISystemFilterPoolReference[] refs = new ISystemFilterPoolReference[1];
			refs[0] = filterPoolRef;
			caller.filterEventFilterPoolReferencesRePositioned(refs, pos - oldPos);
		}
		if (caller instanceof IRSEPersistableContainer) {
			((IRSEPersistableContainer) caller).setDirty(true);
		}
	}

	/**
	 * Move existing filter pool references a given number of positions.
	 * If the delta is negative, they are all moved up by the given amount. If 
	 * positive, they are all moved down by the given amount.
	 * Calls back to inform provider.
	 * @param filterPoolRefs Array of SystemFilterPoolReferences to move.
	 * @param delta the amount by which to move these references.
	 */
	public void moveSystemFilterPoolReferences(ISystemFilterPoolReference[] filterPoolRefs, int delta) {
		int[] oldPositions = new int[filterPoolRefs.length];
		noEvents = true;
		for (int idx = 0; idx < filterPoolRefs.length; idx++)
			oldPositions[idx] = getSystemFilterPoolReferencePosition(filterPoolRefs[idx]);
		if (delta > 0) // moving down, process backwards
			for (int idx = filterPoolRefs.length - 1; idx >= 0; idx--)
				moveSystemFilterPoolReference(filterPoolRefs[idx], oldPositions[idx] + delta);
		else
			for (int idx = 0; idx < filterPoolRefs.length; idx++)
				moveSystemFilterPoolReference(filterPoolRefs[idx], oldPositions[idx] + delta);
		invalidateFilterPoolReferencesCache();
		noEvents = false;
		if (fireEvents && (caller != null)) caller.filterEventFilterPoolReferencesRePositioned(filterPoolRefs, delta);
	}

	// ----------------------------------------------
	// Methods that work on FilterPool master objects
	// ----------------------------------------------
	/**
	 * @return array of filter pools currently referenced by this manager.
	 * Result will never be null, although it may be an array of length zero.
	 */
	public ISystemFilterPool[] getReferencedSystemFilterPools() {
		ISystemFilterPoolReference[] refs = getSystemFilterPoolReferences();
		List pools = new ArrayList(refs.length);
		for (int idx = 0; idx < refs.length; idx++) {
			ISystemFilterPool pool = refs[idx].getReferencedFilterPool();
			if (pool != null) {
				pools.add(pool);
			}
		}
		ISystemFilterPool[] result = new ISystemFilterPool[pools.size()];
		pools.toArray(result);
		return result;
	}

	/**
	 * @param filterPool the filter pool to test to see if we have a reference to it
	 * @return true if the given filter pool has a referencing object in this list.
	 */
	public boolean isSystemFilterPoolReferenced(ISystemFilterPool filterPool) {
		return super.isReferenced(filterPool);
	}

	/**
	 * Given a filter pool, locate the referencing object for it and return it.
	 * @param filterPool the filter pool we are testing for a reference
	 * @return the referencing object if found, else null
	 */
	public ISystemFilterPoolReference getReferenceToSystemFilterPool(ISystemFilterPool filterPool) {
		return (ISystemFilterPoolReference) super.getReferencedObject(filterPool);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.filters.ISystemFilterPoolReferenceManager#addReferenceToSystemFilterPool(org.eclipse.rse.filters.ISystemFilterPool)
	 */
	public ISystemFilterPoolReference addReferenceToSystemFilterPool(ISystemFilterPool filterPool) {
		ISystemFilterPoolReference filterPoolReference = createSystemFilterPoolReference(filterPool);
		addReferencingObject(filterPoolReference);
		filterPoolReference.setParentReferenceManager(this); // DWD - should be done in addReferencingObject?
		invalidateFilterPoolReferencesCache();
		if (fireEvents && (caller != null)) caller.filterEventFilterPoolReferenceCreated(filterPoolReference);
		return filterPoolReference;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.filters.ISystemFilterPoolReferenceManager#addReferenceToSystemFilterPool(org.eclipse.rse.filters.ISystemFilterPoolManager, java.lang.String)
	 */
	public ISystemFilterPoolReference addReferenceToSystemFilterPool(String filterPoolName) {
		ISystemFilterPoolReference filterPoolReference = createSystemFilterPoolReference(filterPoolName);
		addReferencingObject(filterPoolReference);
		filterPoolReference.setParentReferenceManager(this); // DWD - should be done in addReferencingObject?
		invalidateFilterPoolReferencesCache();
		if (fireEvents && (caller != null)) caller.filterEventFilterPoolReferenceCreated(filterPoolReference);
		return filterPoolReference;
	}

	/**
	 * Given a filter pool, locate the referencing object for it and remove it from the list.
	 * Also removes that reference from the filterPool itself, and calls back to provider when done.
	 * @param filterPool the filter pool whose references we are to remove
	 * @return the new count of referencing objects
	 */
	public int removeReferenceToSystemFilterPool(ISystemFilterPool filterPool) {
		ISystemFilterPoolReference filterPoolReference = getReferenceToSystemFilterPool(filterPool);
		int newCount = 0;
		if (filterPoolReference != null) {
			filterPoolReference.removeReference();
			newCount = removeReferencingObject(filterPoolReference);
			invalidateFilterPoolReferencesCache();
			// callback to provider so they can fire events in their GUI
			if (fireEvents && (caller != null)) {
				caller.filterEventFilterPoolReferenceDeleted(filterPoolReference);
			}
		} else
			newCount = getSystemFilterPoolReferenceCount();
		return newCount;
	}

	/**
	 * A referenced filter pool has been renamed. Update our stored name.
	 * Calls back to inform provider.
	 * @param pool the pool that has just been renamed
	 */
	public void renameReferenceToSystemFilterPool(ISystemFilterPool pool) {
		ISystemFilterPoolReference poolRef = null;
		IRSEBasePersistableReferencingObject[] refs = getReferencingObjects();
		for (int idx = 0; (poolRef == null) && (idx < refs.length); idx++)
			if (refs[idx].getReferencedObject() == pool) poolRef = (ISystemFilterPoolReference) refs[idx];

		if (poolRef != null) {
			String oldName = poolRef.getReferencedObjectName();
			poolRef.resetReferencedFilterPoolName(pool.getReferenceName());
			invalidateFilterPoolReferencesCache();
			if (fireEvents && (caller != null)) caller.filterEventFilterPoolReferenceRenamed(poolRef, oldName);
		}
	}

	/**
	 * In one shot, set the filter pool references to new references to supplied filter pools.
	 * Calls back to provider.
	 * @param filterPools of filter pool objects to create references for
	 * @param deReference true to first de-reference all objects in the existing list.
	 */
	public void setSystemFilterPoolReferences(ISystemFilterPool[] filterPools, boolean deReference) {
		if (deReference)
			super.removeAndDeReferenceAllReferencingObjects();
		else
			removeAllReferencingObjects();
		// add current
		if (filterPools != null) {
			for (int idx = 0; idx < filterPools.length; idx++) {
				//addReferenceToSystemFilterPool(filterPools[idx]);
				ISystemFilterPoolReference filterPoolReference = createSystemFilterPoolReference(filterPools[idx]);
				addReferencingObject(filterPoolReference);
				filterPoolReference.setParentReferenceManager(this); // DWD - should be done in addReferencingObject?
			}
			invalidateFilterPoolReferencesCache();
			if (fireEvents && (caller != null)) caller.filterEventFilterPoolReferencesReset();
		}
	}

	// -------------------------
	// SPECIAL CASE METHODS
	// -------------------------
	/**
	 * Create a single filter refererence to a given filter. Needed when a filter
	 * is added to a pool, and the UI is not showing pools but rather all filters
	 * in all pool references.
	 * @param subSystem the subsystem that uses this reference manager
	 * @param filter the new filter that is being added
	 * @return the new reference
	 */
	public ISystemFilterReference getSystemFilterReference(ISubSystem subSystem, ISystemFilter filter) {
		// step 1: find the reference to the filter pool that contains this filter
		ISystemFilterPool pool = filter.getParentFilterPool();
		ISystemFilterPoolReference poolRef = getReferenceToSystemFilterPool(pool);
		// step 2: generate a reference for it
		if (poolRef != null)
			return poolRef.getSystemFilterReference(subSystem, filter);
		else
			return null;
	}

	/**
	 * Concatenate all filter references from all filter pools we reference, into one
	 * big list. Used when the UI is not showing pools.
	 * @param subSystem the subsystem for which this manager is providing filter pool reference management
	 * @return an array of references for this subsystem
	 */
	public ISystemFilterReference[] getSystemFilterReferences(ISubSystem subSystem) {
		ISystemFilterPoolReference[] poolRefs = getSystemFilterPoolReferences();
		Vector v = new Vector();
		for (int idx = 0; idx < poolRefs.length; idx++) {
			ISystemFilterReference[] filterRefs = poolRefs[idx].getSystemFilterReferences(subSystem);
			for (int jdx = 0; jdx < filterRefs.length; jdx++)
				v.addElement(filterRefs[jdx]);
		}
		ISystemFilterReference[] allRefs = new ISystemFilterReference[v.size()];
		for (int idx = 0; idx < v.size(); idx++)
			allRefs[idx] = (ISystemFilterReference) v.elementAt(idx);
		return allRefs;
	}

	/**
	 * Given a filter reference, return its position within this reference manager
	 * when you think of all filter references from all filter pool references as 
	 * being concatenated.
	 * Used when the UI is not showing pools.
	 * @param filterRef the reference to locate
	 * @return the position fo this reference or -1 if not found.
	 */
	public int getSystemFilterReferencePosition(ISystemFilterReference filterRef) {
		ISystemFilterPoolReference[] poolRefs = getSystemFilterPoolReferences();
		int match = -1;
		int totalCount = 0;
		for (int idx = 0; (match == -1) && (idx < poolRefs.length); idx++) {
			ISystemFilterReference[] filterRefs = poolRefs[idx].getSystemFilterReferences(filterRef.getSubSystem());
			for (int jdx = 0; (match == -1) && (jdx < filterRefs.length); jdx++) {
				if (filterRefs[jdx] == filterRef)
					match = totalCount;
				else
					totalCount++;
			}
		}
		return match;
	}

	/**
	 * Given a filter, return its position within this reference manager
	 * when you think of all filter references from all filter pool references as 
	 * being concatenated.
	 * Used when the UI is not showing pools.
	 * @param subSystem the subsystem in which to located the filter
	 * @param filter the filter to locate
	 * @return the position of the filter within this manager.
	 */
	public int getSystemFilterReferencePosition(ISubSystem subSystem, ISystemFilter filter) {
		ISystemFilterPoolReference[] poolRefs = getSystemFilterPoolReferences();
		int match = -1;
		int totalCount = 0;
		for (int idx = 0; (match == -1) && (idx < poolRefs.length); idx++) {
			ISystemFilterReference[] filterRefs = poolRefs[idx].getSystemFilterReferences(subSystem);
			for (int jdx = 0; (match == -1) && (jdx < filterRefs.length); jdx++) {
				if (filterRefs[jdx].getReferencedFilter() == filter)
					match = totalCount;
				else
					totalCount++;
			}
		}
		return match;
	}

	/**
	 * Utility method to scan across all filter pools in a given named filter pool manager, for a match
	 * on a given filter pool name.
	 * @param mgrs The list of filter pool managers to scan for the given filter pool.
	 * @param mgrName The name of the manager to restrict the search to
	 * @param poolName The name of the filter pool as stored on disk. It may be qualified somehow
	 * to incorporate the manager name too.
	 * @return the filter pool that was found.
	 */
	public static ISystemFilterPool getFilterPool(ISystemFilterPoolManager[] mgrs, String mgrName, String poolName) {
		ISystemFilterPoolManager mgr = getFilterPoolManager(mgrs, mgrName);
		if (mgr == null) return null;
		return mgr.getSystemFilterPool(poolName);
	}

	/**
	 * Utility method to scan across all filter pool managers for a match on a give name.
	 * @param mgrs The list of filter pool managers to scan for the given name
	 * @param mgrName The name of the manager to restrict the search to
	 * @return the filter pool manager that was found or null if not found.
	 */
	public static ISystemFilterPoolManager getFilterPoolManager(ISystemFilterPoolManager[] mgrs, String mgrName) {
		ISystemFilterPoolManager mgr = null;
		for (int idx = 0; (mgr == null) && (idx < mgrs.length); idx++)
			if (mgrs[idx].getName().equals(mgrName)) mgr = mgrs[idx];
		return mgr;
	}

	// ------------------
	// HELPER METHODS...
	// ------------------

	public String toString() {
		return getName();
	}

}
