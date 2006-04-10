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
/**
 * An interface for classes that instantiate SystemFilterPoolReferenceManager objects.
 * This is the "caller" and as is recorded and recoverable from any object within
 * the filter reference framework. This enables callers to get back instances of themselves
 * given any filter reference object. Important when enabling UI actions against user
 * selected filter reference framework objects
 * <p>
 * Further, the goal is the allow all the filter framework UI actions to work 
 * independently, able to fully handle all actions without intervention on the
 * provider's part. However, often the provider needs to be informed of all events
 * in order to fire events to update its GUI. So this interface captures those 
 * callbacks that done to the provider for every interesting event. Should you 
 * not care about these, supply empty shells for these methods.
 */
public interface ISystemFilterPoolReferenceManagerProvider 
{
    /**
     * Return the SystemFilterPoolReferenceManager object this provider holds/provides.
     */
    public ISystemFilterPoolReferenceManager getSystemFilterPoolReferenceManager();
    /**
     * Return the owning filter pool that is unique to this provider
     */
    public ISystemFilterPool getUniqueOwningSystemFilterPool(boolean createIfNotFound);
    
    // -------------------------------
    // FILTER POOL REFERENCE EVENTS...
    // -------------------------------
    /**
     * A new filter pool reference has been created
     */
    public void filterEventFilterPoolReferenceCreated(ISystemFilterPoolReference newPoolRef);
    /**
     * A filter pool reference has been deleted
     */
    public void filterEventFilterPoolReferenceDeleted(ISystemFilterPoolReference filterPoolRef);
    /**
     * A single filter pool reference has been reset to reference a new pool
     */
    public void filterEventFilterPoolReferenceReset(ISystemFilterPoolReference filterPoolRef);
    /**
     * All filter pool references has been reset
     */
    public void filterEventFilterPoolReferencesReset();
    /**
     * A filter pool reference has been renamed (ie, its reference filter pool renamed)
     */
    public void filterEventFilterPoolReferenceRenamed(ISystemFilterPoolReference poolRef, String oldName);
    /**
     * One or more filter pool references have been re-ordered within their manager
     */
    public void filterEventFilterPoolReferencesRePositioned(ISystemFilterPoolReference[] poolRefs, int delta);
    // -------------------------------
    // FILTER REFERENCE EVENTS...
    // -------------------------------
    /**
     * A new filter has been created. This is called when a filter pool reference is selected and a new filter
     *  is created, so that the provider can expand the selected filter pool reference and reveal the new filter 
     *  within the selected pool reference. 
     * <p>
     * Only the selected node should be expanded if not already. All other references to this pool will already
     * have been informed of the new addition, and will have refreshed their children but not expanded them.
     */
    public void filterEventFilterCreated(Object selectedObject, ISystemFilter newFilter);
    // ---------------------------------
    // FILTER STRING REFERENCE EVENTS...
    // ---------------------------------
    /**
     * A new filter string has been created. This is called when a filter reference is selected and a new filter
     *  string is created, so that the provider can expand the selected filter reference and reveal the new filter 
     *  string within the selected filter reference. 
     * <p>
     * Only the selected node should be expanded if not already. All other references to this filter will already
     * have been informed of the new addition, and will have refreshed their children but not expanded them.
     */
    public void filterEventFilterStringCreated(Object selectedObject, ISystemFilterString newFilterString);
}