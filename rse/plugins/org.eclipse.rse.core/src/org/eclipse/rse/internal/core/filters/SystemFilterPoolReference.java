/*******************************************************************************
 * Copyright (c) 2002, 2011 IBM Corporation and others.
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
 * Martin Oberhuber (Wind River) - [177523] Unify singleton getter methods
 * David Dykstal (IBM) - [189858] made sure that a reference remains broken if the profile
 *                                contained in the reference was not found.
 * David Dykstal (IBM) - [192122] extended search to look for filter pools in
 *   profile during getReferencedFilterPool() rather than returning broken reference
 * David McKnight (IBM) - [358999] Deleting multiple connections takes long time
 *******************************************************************************/

package org.eclipse.rse.internal.core.filters;

import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.filters.ISystemFilter;
import org.eclipse.rse.core.filters.ISystemFilterContainer;
import org.eclipse.rse.core.filters.ISystemFilterContainerReference;
import org.eclipse.rse.core.filters.ISystemFilterPool;
import org.eclipse.rse.core.filters.ISystemFilterPoolManager;
import org.eclipse.rse.core.filters.ISystemFilterPoolReference;
import org.eclipse.rse.core.filters.ISystemFilterPoolReferenceManager;
import org.eclipse.rse.core.filters.ISystemFilterPoolReferenceManagerProvider;
import org.eclipse.rse.core.filters.ISystemFilterReference;
import org.eclipse.rse.core.model.IRSEPersistableContainer;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.internal.references.SystemPersistableReferencingObject;

/**
 * A reference to a filter pool. A reference may be "resolved" or "unresolved".
 */
public class SystemFilterPoolReference extends SystemPersistableReferencingObject implements ISystemFilterPoolReference, ISystemFilterContainerReference, IAdaptable {
	
	private SystemFilterContainerReferenceCommonMethods containerHelper = null;
	private ISystemFilterPoolManager filterPoolManager = null;
	public static final String DELIMITER = "___"; //$NON-NLS-1$
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
	 * @param filterPoolName the name of the filter pool.
	 */
	public SystemFilterPoolReference(String filterPoolName) {
		this();
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
		/*
		 * A filter pool reference stores the name of the filter pool it references in the form managerName___filterPoolName.
		 * or in the unqualified form of filterPoolName which references a locally defined filter pool.
		 * ___ is the delimiter. Absence of the delimiter indicates an unqualified name.
		 * The filter pool manager name is the same as its owning profile.
		 */
		String savedName = getReferencedObjectName();
		String[] parts = savedName.split(DELIMITER, 2);
		String result = parts[0];
		if (parts.length == 2) {
			result = parts[1];
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilterPoolReference#getReferencedFilterPoolManagerName()
	 */
	public String getReferencedFilterPoolManagerName() {
		/*
		 * A filter pool reference stores the name of the filter pool it references in the form managerName___filterPoolName.
		 * or in the unqualified form of filterPoolName which references a locally defined filter pool.
		 * ___ is the delimiter. Absence of the delimiter indicates an unqualified name.
		 * The filter pool manager name is the same as its owning profile.
		 */
		String result = null;
		String savedName = getReferencedObjectName();
		String[] parts = savedName.split(DELIMITER, 2);
		if (parts.length == 2) {
			result = parts[0];
		} else {
			ISystemFilterPoolReferenceManagerProvider provider = getProvider();
			if (provider instanceof ISubSystem) {
				ISubSystem subsystem = (ISubSystem) provider;
				ISystemProfile profile = subsystem.getSystemProfile();
				result = profile.getName();
			}
		}
		if (result == null) {
			RSECorePlugin.getDefault().getLogger().logWarning("Unexpected condition: filter pool manager name not found.", null); //$NON-NLS-1$
		}
		return result;
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
			String profileName = getReferencedFilterPoolManagerName();
			ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
			ISystemProfile profile = registry.getSystemProfile(profileName);
			if (profile != null) {
				ISubSystem subsystem = (ISubSystem) getProvider();
				ISubSystemConfiguration config = subsystem.getSubSystemConfiguration();
				filterPoolManager = config.getFilterPoolManager(profile);
				filterPool = filterPoolManager.getSystemFilterPool(filterPoolName);
				// TODO (dwd) may not need filter pools managers on a per subsystem configuration basis, investigate
				// added for 192122 - search all pools in the profile, these are unique anyway
				if (filterPool == null) {
					ISystemFilterPool[] candidatePools = profile.getFilterPools();
					for (int i = 0; i < candidatePools.length; i++) {
						ISystemFilterPool candidatePool = candidatePools[i];
						String candidatePoolName = candidatePool.getName();
						if (candidatePoolName.equals(filterPoolName)) {
							filterPool = candidatePool;
							break;
						}
					}
				}
			}
	
			
			// bug 358999 - this was originally outside of the first if but then it created tons of unnecessary references
			if (filterPool != null) {
				setReferenceToFilterPool(filterPool);
				setReferenceBroken(false);
			} else {
				setReferenceBroken(true);
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
		List references = containerHelper.getSystemFilterReferences(subSystem);
		ISystemFilterReference[] result = new ISystemFilterReference[references.size()];
		references.toArray(result);
		return result;
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
		return getReferencedObjectName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.persistance.IRSEPersistableContainer#commit()
	 */
	public boolean commit() {
		return false;
		//	return RSEUIPlugin.getThePersistenceManager().commit(getProvider().);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IRSEPersistableContainer#getPersistableParent()
	 */
	public IRSEPersistableContainer getPersistableParent() {
		IRSEPersistableContainer parent = null;
		ISystemFilterPoolReferenceManagerProvider provider = getProvider();
		if (provider instanceof IRSEPersistableContainer) {
			parent = (IRSEPersistableContainer) provider;
		}
		return parent;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IRSEPersistableContainer#getPersistableChildren()
	 */
	public IRSEPersistableContainer[] getPersistableChildren() {
		return IRSEPersistableContainer.NO_CHILDREN;
	}
}
