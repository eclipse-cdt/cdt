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

package org.eclipse.rse.internal.filters;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.core.references.IRSEPersistableReferencedObject;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.ISystemFilterContainer;
import org.eclipse.rse.filters.ISystemFilterContainerReference;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.filters.ISystemFilterPoolManager;
import org.eclipse.rse.filters.ISystemFilterPoolReference;
import org.eclipse.rse.filters.ISystemFilterPoolReferenceManager;
import org.eclipse.rse.filters.ISystemFilterPoolReferenceManagerProvider;
import org.eclipse.rse.filters.ISystemFilterReference;
import org.eclipse.rse.internal.references.SystemPersistableReferencingObject;

/**
 * A reference to a filter pool. A reference may be "resolved" or "unresolved".
 */
public class SystemFilterPoolReference extends SystemPersistableReferencingObject implements ISystemFilterPoolReference, ISystemFilterContainerReference, IAdaptable {
	
	private SystemFilterContainerReferenceCommonMethods containerHelper = null;
	private ISystemFilterPoolManager filterPoolManager = null;
	protected static final String DELIMITER = "___";
	protected static final int DELIMITER_LENGTH = 3;

	/**
	 * Default constructor.
	 */
	private SystemFilterPoolReference() {
		super();
		containerHelper = new SystemFilterContainerReferenceCommonMethods(this);
	}
	
	/**
	 * Constructs a new resolved filter pool reference.
	 * @param filterPool The filter pool that this filter will refer to.
	 */
	public SystemFilterPoolReference(ISystemFilterPool filterPool) {
		this();
		setReferenceToFilterPool(filterPool);
	}
	
	/**
	 * Constructs a new filter pool reference. This is an unresolved reference.
	 * It is resolved on first use by using the supplied filterPoolManager.
	 * @param filterPoolManager the manager used to resolve the reference.
	 * @param filterPoolName the name of the filter pool.
	 */
	public SystemFilterPoolReference(ISystemFilterPoolManager filterPoolManager, String filterPoolName) {
		this();
		this.filterPoolManager = filterPoolManager;
		setReferencedObjectName(filterPoolName);
	}

	/**
	 * Return the reference manager which is managing this filter reference
	 * framework object.
	 */
	public ISystemFilterPoolReferenceManager getFilterPoolReferenceManager() {
		return (ISystemFilterPoolReferenceManager) getParentReferenceManager();
	}

	/**
	 * Return the object which instantiated the pool reference manager object.
	 * Makes it easy to get back to the point of origin, given any filter reference
	 * framework object
	 */
	public ISystemFilterPoolReferenceManagerProvider getProvider() {
		ISystemFilterPoolReferenceManager mgr = getFilterPoolReferenceManager();
		if (mgr != null)
			return mgr.getProvider();
		else
			return null;
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
	 * Return name of the filter pool we reference
	 * The name is stored qualified by the manager name,
	 * so we first have to strip that off.
	 */
	public String getReferencedFilterPoolName() {
		String savedName = super.getReferencedObjectName();
		String poolName = null;
		int idx = savedName.indexOf(DELIMITER);
		if (idx >= 0)
			poolName = savedName.substring(idx + DELIMITER_LENGTH);
		else
			poolName = savedName;
		return poolName;
	}

	/**
	 * Return name of the filter pool manager containing the pool we reference.
	 * The pool name is stored qualified by the manager name,
	 *  so we get it from there.
	 */
	public String getReferencedFilterPoolManagerName() {
		String savedName = super.getReferencedObjectName();
		String mgrName = null;
		int idx = savedName.indexOf(DELIMITER);
		if (idx >= 0)
			mgrName = savedName.substring(0, idx);
		else
			mgrName = savedName;
		return mgrName;
	}

	/**
	 * Reset the name of the filter pool we reference. 
	 * Called on filter pool rename operations
	 */
	public void resetReferencedFilterPoolName(String newName) {
		super.setReferencedObjectName(newName);
	}

	/**
	 * Set the filter pool that we reference.
	 * This also calls addReference(this) on that pool!
	 */
	public void setReferenceToFilterPool(ISystemFilterPool pool) {
		super.setReferencedObject((IRSEPersistableReferencedObject) pool);
	}

	/**
	 * Return referenced filter pool object. If the reference is unresolved it will
	 * attempt to resolve it.
	 */
	public ISystemFilterPool getReferencedFilterPool() {
		ISystemFilterPool filterPool = (ISystemFilterPool) getReferencedObject();
		if (filterPool == null) {
			String filterPoolName = getReferencedFilterPoolName();
			filterPool = filterPoolManager.getSystemFilterPool(filterPoolName);
			setReferenceToFilterPool(filterPool);
		}
		return filterPool;
	}

	// -------------------------------------------------------------
	// Methods common with SystemFilterPoolReferenceImpl, and hence
	//  abstracted out into SystemFilterContainerReference...
	// -------------------------------------------------------------
	/**
	 * Return the object to which we hold a reference. This is either
	 * SystemFilter or SystemFilterPool. Since both implement 
	 * SystemFilterContainer, that is what we return.
	 * <p>
	 * Of course, this is a generic method, and in our case it is always
	 * true that we only hold a SystemFilter. Hence, this is the same
	 * as calling getReferenceFilter and casting the result.
	 */
	public ISystemFilterContainer getReferencedSystemFilterContainer() {
		return getReferencedFilterPool();
	}

	/**
	 * Build and return an array of SystemFilterReference objects.
	 * Each object is created new. There is one for each of the filters
	 * in the reference SystemFilter or SystemFilterPool.
	 * For performance reasons, we will cache this array and only 
	 * return a fresh one if something changes in the underlying 
	 * filter list.
	 */
	public ISystemFilterReference[] getSystemFilterReferences(ISubSystem subSystem) {
		return containerHelper.getSystemFilterReferences(subSystem);
	}

	/**
	 * Create a single filter refererence to a given filter. 
	 * If there already is a reference to this filter, it is returned.
	 * If not, a new reference is created and appended to the end of the existing filter reference array.
	 * @see #getExistingSystemFilterReference(ISystemFilter)
	 */
	public ISystemFilterReference getSystemFilterReference(ISubSystem subSystem, ISystemFilter filter) {
		//return containerHelper.generateFilterReference(filter);
		return containerHelper.generateAndRecordFilterReference(subSystem, filter);
	}

	/**
	 * Return an existing reference to a given system filter. 
	 * If no reference currently exists to this filter, returns null.
	 * @see #getSystemFilterReference(ISystemFilter)
	 */
	public ISystemFilterReference getExistingSystemFilterReference(ISubSystem subSystem, ISystemFilter filter) {
		return containerHelper.getExistingSystemFilterReference(subSystem, filter);
	}

	/**
	 * Return true if the referenced pool or filter has filters.
	 */
	public boolean hasFilters() {
		return containerHelper.hasFilters();
	}

	/**
	 * Return count of the number of filters in the referenced pool or filter
	 */
	public int getFilterCount() {
		return containerHelper.getFilterCount();
	}

	/**
	 * Return the name of the SystemFilter or SystemFilterPool that we reference.
	 * For such objects this is what we show in the GUI.
	 */
	public String getName() {
		return getReferencedFilterPoolName();
	}

	/**
	 * Return fully qualified name that includes the filter pool managers name
	 */
	public String getFullName() {
		return super.getReferencedObjectName();
	}

	public boolean commit() {
		return false;
		//	return RSEUIPlugin.getThePersistenceManager().commit(getProvider().);
	}
}