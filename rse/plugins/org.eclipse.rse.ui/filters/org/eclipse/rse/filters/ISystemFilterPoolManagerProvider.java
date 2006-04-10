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
import org.eclipse.core.runtime.IAdaptable;

/**
 * An interface for classes that instantiate SystemFilterPoolManager objects.
 * This is the "caller" and as is recorded and recoverable from any object within
 * the filter framework. This enables callers to get back instances of themselves
 * given any filter object. Important when enabling UI actions against user
 * selected filter framework objects
 * <p>
 * Further, the goal is the allow all the filter framework UI actions to work 
 * independently, able to fully handle all actions without intervention on the
 * provider's part. However, often the provider needs to be informed of all events
 * in order to fire events to update its GUI. So this interface captures those 
 * callbacks that done to the provider for every interesting event. Should you 
 * not care about these, supply empty shells for these methods.
 */
public interface ISystemFilterPoolManagerProvider extends IAdaptable
{
	
	/**
	 * Return the unique id for this provider
	 * @return
	 */
	 public String getId();
	
	/**
	 * Return the manager object for the given manager name.
	 */
    public ISystemFilterPoolManager getSystemFilterPoolManager(String managerName);
	/**
	 * Return all the manager objects this provider owns
	 */
    public ISystemFilterPoolManager[] getSystemFilterPoolManagers();
	/**
	 * Return all the manager objects this provider owns, to which it wants 
	 *  to support referencing from the given filter reference manager.
	 * <p>
	 * Called by SystemFilterPoolReferenceManager.
	 */
    public ISystemFilterPoolManager[] getReferencableSystemFilterPoolManagers(ISystemFilterPoolReferenceManager refMgr);
    /**
     * Last chance call, by a filter pool reference manager, when a reference to a filter
     * pool is found but the referenced master filter pool is not found in those the reference
     * manager by getSystemFilterPoolManagers().
     * <p>
     * If this returns null, then this broken reference will be deleted
     */
    public ISystemFilterPool getSystemFilterPoolForBrokenReference(ISystemFilterPoolReferenceManager callingRefenceMgr,
                                                                  String missingPoolMgrName, String missingPoolName);

    // ---------------------
    // FILTER POOL EVENTS...
    // ---------------------
    /**
     * A new filter pool has been created
     */
    public void filterEventFilterPoolCreated(ISystemFilterPool newPool);
    /**
     * A filter pool has been deleted
     */
    public void filterEventFilterPoolDeleted(ISystemFilterPool oldPool);
    /**
     * A filter pool has been renamed
     */
    public void filterEventFilterPoolRenamed(ISystemFilterPool pool, String oldName);
    /**
     * One or more filter pools have been re-ordered within their manager
     */
    public void filterEventFilterPoolsRePositioned(ISystemFilterPool[] pools, int delta);

    // ---------------------
    // FILTER EVENTS...
    // ---------------------
    /**
     * A new filter has been created
     */
    public void filterEventFilterCreated(ISystemFilter newFilter);
    /**
     * A filter has been deleted
     */
    public void filterEventFilterDeleted(ISystemFilter oldFilter);
    /**
     * A filter has been renamed
     */
    public void filterEventFilterRenamed(ISystemFilter filter, String oldName);
    /**
     * A filter's strings have been updated
     */
    public void filterEventFilterUpdated(ISystemFilter filter);
    /**
     * One or more filters have been re-ordered within their pool or filter (if nested)
     */
    public void filterEventFiltersRePositioned(ISystemFilter[] filters, int delta);    

    // -----------------------
    // FILTER STRING EVENTS...
    // -----------------------
    /**
     * A new filter string has been created
     */
    public void filterEventFilterStringCreated(ISystemFilterString newFilterString);
    /**
     * A filter string has been deleted
     */
    public void filterEventFilterStringDeleted(ISystemFilterString oldFilterString);
    /**
     * A filter string has been updated
     */
    public void filterEventFilterStringUpdated(ISystemFilterString filterString);
    /**
     * One or more filters have been re-ordered within their filter
     */
    public void filterEventFilterStringsRePositioned(ISystemFilterString[] filterStrings, int delta);    

}