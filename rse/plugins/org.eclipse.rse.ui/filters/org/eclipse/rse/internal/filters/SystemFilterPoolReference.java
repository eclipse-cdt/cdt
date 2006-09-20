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
import org.eclipse.rse.core.filters.ISystemFilter;
import org.eclipse.rse.core.filters.ISystemFilterContainer;
import org.eclipse.rse.core.filters.ISystemFilterContainerReference;
import org.eclipse.rse.core.filters.ISystemFilterPool;
import org.eclipse.rse.core.filters.ISystemFilterPoolManager;
import org.eclipse.rse.core.filters.ISystemFilterPoolReference;
import org.eclipse.rse.core.filters.ISystemFilterPoolReferenceManager;
import org.eclipse.rse.core.filters.ISystemFilterPoolReferenceManagerProvider;
import org.eclipse.rse.core.filters.ISystemFilterReference;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.references.SystemPersistableReferencingObject;

/**
 * A reference to a filter pool. A reference may be "resolved" or "unresolved".
 */
public class SystemFilterPoolReference extends SystemPersistableReferencingObject implements ISystemFilterPoolReference, ISystemFilterContainerReference, IAdaptable {
	
	private SystemFilterContainerReferenceCommonMethods containerHelper = null;
	private ISystemFilterPoolManager filterPoolManager = null;
	public static final String DELIMITER = "___";
	public static final int DELIMITER_LENGTH = 3;

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

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilterPoolReference#getFilterPoolReferenceManager()
	 */
	public ISystemFilterPoolReferenceManager getFilterPoolReferenceManager() {
		return (ISystemFilterPoolReferenceManager) getParentReferenceManager();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilterPoolReference#getProvider()
	 */
	public ISystemFilterPoolReferenceManagerProvider getProvider() {
		ISystemFilterPoolReferenceManager mgr = getFilterPoolReferenceManager();
		if (mgr != null)
			return mgr.getProvider();
		else
			return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapterType) {
		return Platform.getAdapterManager().getAdapter(this, adapterType);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilterPoolReference#getReferencedFilterPoolName()
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

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilterPoolReference#getReferencedFilterPoolManagerName()
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

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilterPoolReference#resetReferencedFilterPoolName(java.lang.String)
	 */
	public void resetReferencedFilterPoolName(String newName) {
		super.setReferencedObjectName(newName);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilterPoolReference#setReferenceToFilterPool(org.eclipse.rse.core.filters.ISystemFilterPool)
	 */
	public void setReferenceToFilterPool(ISystemFilterPool pool) {
		super.setReferencedObject(pool);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilterPoolReference#getReferencedFilterPool()
	 */
	public ISystemFilterPool getReferencedFilterPool() {
		ISystemFilterPool filterPool = (ISystemFilterPool) getReferencedObject();
		if (filterPool == null) {
			String filterPoolName = getReferencedFilterPoolName();
			filterPool = filterPoolManager.getSystemFilterPool(filterPoolName);
			if (filterPool != null) {
				setReferenceToFilterPool(filterPool);
			} else {
				this.setReferenceBroken(true);
			}
		}
		return filterPool;
	}

	// -------------------------------------------------------------
	// Methods common with SystemFilterPoolReferenceImpl, and hence
	//  abstracted out into SystemFilterContainerReference...
	// -------------------------------------------------------------

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilterContainerReference#getReferencedSystemFilterContainer()
	 */
	public ISystemFilterContainer getReferencedSystemFilterContainer() {
		return getReferencedFilterPool();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilterContainerReference#getSystemFilterReferences(org.eclipse.rse.core.subsystems.ISubSystem)
	 */
	public ISystemFilterReference[] getSystemFilterReferences(ISubSystem subSystem) {
		return containerHelper.getSystemFilterReferences(subSystem);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilterContainerReference#getSystemFilterReference(org.eclipse.rse.core.subsystems.ISubSystem, org.eclipse.rse.core.filters.ISystemFilter)
	 */
	public ISystemFilterReference getSystemFilterReference(ISubSystem subSystem, ISystemFilter filter) {
		//return containerHelper.generateFilterReference(filter);
		return containerHelper.generateAndRecordFilterReference(subSystem, filter);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilterContainerReference#getExistingSystemFilterReference(org.eclipse.rse.core.subsystems.ISubSystem, org.eclipse.rse.core.filters.ISystemFilter)
	 */
	public ISystemFilterReference getExistingSystemFilterReference(ISubSystem subSystem, ISystemFilter filter) {
		return containerHelper.getExistingSystemFilterReference(subSystem, filter);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilterContainerReference#hasFilters()
	 */
	public boolean hasFilters() {
		return containerHelper.hasFilters();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilterContainerReference#getFilterCount()
	 */
	public int getFilterCount() {
		return containerHelper.getFilterCount();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IRSEModelObject#getName()
	 */
	public String getName() {
		return getReferencedFilterPoolName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilterPoolReference#getFullName()
	 */
	public String getFullName() {
		return super.getReferencedObjectName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.persistance.IRSEPersistableContainer#commit()
	 */
	public boolean commit() {
		return false;
		//	return RSEUIPlugin.getThePersistenceManager().commit(getProvider().);
	}
}