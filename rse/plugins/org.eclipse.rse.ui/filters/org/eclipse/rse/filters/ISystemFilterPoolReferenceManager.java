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

import org.eclipse.core.resources.IFolder;
import org.eclipse.rse.core.references.IRSEBasePersistableReferenceManager;
import org.eclipse.rse.core.subsystems.ISubSystem;

/**
 * This class manages a persistable list of objects each of which reference
 * a filter pool. This class builds on the parent class SystemPersistableReferenceManager,
 * offering convenience versions of the parent methods that are typed to the
 * classes in the filters framework.
 */
/**
 * @lastgen interface SystemFilterPoolReferenceManager extends SystemPersistableReferenceManager {}
 */
public interface ISystemFilterPoolReferenceManager extends IRSEBasePersistableReferenceManager {
	/**
	 * Get the object which instantiated this instance of the filter pool reference manager.
	 * This is also available from any filter reference framework object.
	 */
	public ISystemFilterPoolReferenceManagerProvider getProvider();

	/**
	 * Set the object which instantiated this instance of the filter pool reference manager.
	 * This makes it available to retrieve from any filter reference framework object,
	 * via the ubiquitous getProvider interface method.
	 */
	public void setProvider(ISystemFilterPoolReferenceManagerProvider caller);

	/**
	 * Turn off callbacks to the provider until turned on again.
	 */
	public void setProviderEventNotification(boolean fireEvents);

	// ------------------------------------------------------------
	// Methods for setting and querying related filterpool manager
	// ------------------------------------------------------------
	/*
	 * Set the managers of the master list of filter pools, from which
	 *  objects in this list reference.
	 * NOW DELETED SO THAT WE DYNAMICALLY QUERY THIS LIST FROM THE 
	 * ASSOCIATED SYSTEMFILTERPOOLMANAGER PROVIDER, SO IT IS ALWAYS UP
	 * TO DATE. psc.
	 */
	//public void setSystemFilterPoolManagers(SystemFilterPoolManager[] mgrs);
	/**
	 * Set the associated master pool manager provider. Note the provider
	 *  typically manages multiple pool managers and we manage references
	 *  across those.
	 */
	public void setSystemFilterPoolManagerProvider(ISystemFilterPoolManagerProvider poolMgrProvider);

	/**
	 * Get the associated master pool manager provider. Note the provider
	 *  typically manages multiple pool managers and we manage references
	 *  across those.
	 */
	public ISystemFilterPoolManagerProvider getSystemFilterPoolManagerProvider();

	/**
	 * Get the managers of the master list of filter pools, from which
	 *  objects in this list reference.
	 */
	public ISystemFilterPoolManager[] getSystemFilterPoolManagers();

	/**
	 * Get the managers of the master list of filter pools, from which
	 *  objects in this list reference, but which are not in the list of
	 *  managers our pool manager supplier gives us. That is, these are
	 *  references to filter pools outside the expected list.
	 * @return null if no unmatched managers found, else an array of such managers.
	 */
	public ISystemFilterPoolManager[] getAdditionalSystemFilterPoolManagers();

	/**
	 * Set the default manager of the master list of filter pools, from which
	 *  objects in this list reference.
	 */
	public void setDefaultSystemFilterPoolManager(ISystemFilterPoolManager mgr);

	/**
	 * Get the default manager of the master list of filter pools, from which
	 *  objects in this list reference.
	 */
	public ISystemFilterPoolManager getDefaultSystemFilterPoolManager();

	// ---------------------------------------------------
	// Methods that work on FilterPool referencing objects
	// ---------------------------------------------------
	/**
	 * Ask each referenced pool for its name, and update it.
	 * Called after the name of the pool or its manager changes.
	 */
	public void regenerateReferencedSystemFilterPoolNames();

	/**
	 * Return array of SystemFilterPoolReference objects.
	 * Result will never be null, although it may be an array of length zero.
	 */
	public ISystemFilterPoolReference[] getSystemFilterPoolReferences();

	/**
	 * In one shot, set the filter pool references
	 * <p> Calls back to inform provider
	 * @param array of filter pool reference objects to set the list to.
	 * @param deReference true to first de-reference all objects in the existing list.
	 */
	public void setSystemFilterPoolReferences(ISystemFilterPoolReference[] filterPoolReferences, boolean deReference);

	/**
	 * Add a filter pool referencing object to the list. 
	 * @return the new count of referencing objects
	 */
	public int addSystemFilterPoolReference(ISystemFilterPoolReference filterPoolReference);

	/**
	 * Reset the filter pool a reference points to. Called on a move-filter-pool operation
	 */
	public void resetSystemFilterPoolReference(ISystemFilterPoolReference filterPoolReference, ISystemFilterPool newPool);

	/**
	 * Remove a filter pool referencing object from the list.
	 * @param filterPool Reference the reference to remove
	 * @param deReference true if we want to dereference the referenced object (call removeReference on it)
	 * @return the new count of referencing objects
	 */
	public int removeSystemFilterPoolReference(ISystemFilterPoolReference filterPoolReference, boolean deReference);

	/**
	 * Return count of referenced filter pools
	 */
	public int getSystemFilterPoolReferenceCount();

	/**
	 * Return the zero-based position of a SystemFilterPoolReference object within this list
	 */
	public int getSystemFilterPoolReferencePosition(ISystemFilterPoolReference filterPoolRef);

	/**
	 * Move a given filter pool reference to a given zero-based location
	 * <p> Calls back to inform provider
	 */
	public void moveSystemFilterPoolReference(ISystemFilterPoolReference filterPoolRef, int pos);

	/**
	 * Move existing filter pool references a given number of positions.
	 * If the delta is negative, they are all moved up by the given amount. If 
	 * positive, they are all moved down by the given amount.<p>
	 * <p> Calls back to inform provider
	 * @param filterPoolRefs Array of SystemFilterPoolReferences to move.
	 * @param newPosition new zero-based position for the filter pool references.
	 */
	public void moveSystemFilterPoolReferences(ISystemFilterPoolReference[] filterPoolRefs, int delta);

	// ----------------------------------------------
	// Methods that work on FilterPool master objects
	// ----------------------------------------------
	/**
	 * Return array of filter pools currently referenced by this manager
	 * Result will never be null, although it may be an array of length zero.
	 */
	public ISystemFilterPool[] getReferencedSystemFilterPools();

	/**
	 * Return true if the given filter pool has a referencing object in this list.
	 */
	public boolean isSystemFilterPoolReferenced(ISystemFilterPool filterPool);

	/**
	 * Given a filter pool, locate the referencing object for it and return it.
	 * @return the referencing object if found, else null
	 */
	public ISystemFilterPoolReference getReferenceToSystemFilterPool(ISystemFilterPool filterPool);

	/**
	 * Given a filter pool, create a referencing object and add it to the list.
	 * <p> Calls back to inform provider
	 * @return new filter pool reference
	 */
	public ISystemFilterPoolReference addReferenceToSystemFilterPool(ISystemFilterPool filterPool);

	/**
	 * Given a filter pool name, create a referencing object and add it to the list.
	 * This creates an unresolved reference to that filter pool. It will be resolved on first use.
	 * <p> Calls back to inform provider
	 * @param filterPoolManager the manager that can be used to resolve the reference.
	 * @param filterPoolName the name of the filter pool being referenced.
	 * @return new filter pool reference
	 */
	public ISystemFilterPoolReference addReferenceToSystemFilterPool(ISystemFilterPoolManager filterPoolManager, String filterPoolName);

	/**
	 * Given a filter pool, locate the referencing object for it and remove it from the list.
	 * <p> Calls back to inform provider
	 * @return the new count of referencing objects
	 */
	public int removeReferenceToSystemFilterPool(ISystemFilterPool filterPool);

	/**
	 * A reference filter pool has been renamed. Update our stored name...
	 * <p> Calls back to inform provider
	 */
	public void renameReferenceToSystemFilterPool(ISystemFilterPool pool);

	/**
	 * In one shot, set the filter pool references to new references to supplied filter pools.
	 * <p> Calls back to inform provider
	 * @param array of filter pool objects to create references for
	 * @param deReference true to first de-reference all objects in the existing list.
	 */
	public void setSystemFilterPoolReferences(ISystemFilterPool[] filterPools, boolean deReference);

	// -------------------------
	// SPECIAL CASE METHODS
	// -------------------------
	/**
	 * Create a single filter refererence to a given filter. Needed when a filter
	 *  is added to a pool, and the GUI is not showing pools but rather all filters
	 *  in all pool references.
	 */
	public ISystemFilterReference getSystemFilterReference(ISubSystem subSystem, ISystemFilter filter);

	/**
	 * Concatenate all filter references from all filter pools we reference, into one
	 * big list.
	 */
	public ISystemFilterReference[] getSystemFilterReferences(ISubSystem subSystem);

	/**
	 * Given a filter reference, return its position within this reference manager
	 *  when you think of all filter references from all filter pool references as 
	 *  being concatenated
	 */
	public int getSystemFilterReferencePosition(ISystemFilterReference filterRef);

	/**
	 * Given a filter, return its position within this reference manager
	 *  when you think of all filter references from all filter pool references as 
	 *  being concatenated
	 */
	public int getSystemFilterReferencePosition(ISubSystem subSystem, ISystemFilter filter);

	// -------------------------
	// SAVE / RESTORE METHODS...
	// -------------------------
	/**
	 * After restoring this from disk, there is only the referenced object name,
	 * not the referenced object pointer, for each referencing object.
	 * <p>
	 * This method is called after restore and for each restored object in the list must:
	 * <ol>
	 *   <li>Do what is necessary to find the referenced object, and set the internal reference pointer.
	 *   <li>Call addReference(this) on that object so it can maintain it's in-memory list
	 *          of all referencing objects.
	 *   <li>Set the important transient variables 
	 * </ol>
	 * @param relatedManagers the filter pool managers that hold filter pools we reference
	 * @param provider the host of this reference manager, so you can later call getProvider
	 * @return A Vector of SystemFilterPoolReferences that were not successfully resolved, or null if all
	 *   were resolved.
	 */
	public Vector resolveReferencesAfterRestore(ISystemFilterPoolManagerProvider relatedPoolMgrProvider, ISystemFilterPoolReferenceManagerProvider provider);

	/**
	 * Save all the filter pool references to disk.     
	 * Use only if not doing your own saving, else override or set save policy to none.
	 */
	public void save() throws Exception;

	/**
	 * Return the folder that this manager is contained in.
	 */
	public IFolder getFolder();

	/**
	 * Reset the folder that this manager is contained in.
	 */
	public void resetManagerFolder(IFolder newFolder);
}