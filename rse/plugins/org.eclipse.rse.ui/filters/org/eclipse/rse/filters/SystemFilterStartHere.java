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
import org.eclipse.rse.internal.filters.SystemFilterNamingPolicy;
import org.eclipse.rse.internal.filters.SystemFilterPoolManager;
import org.eclipse.rse.internal.filters.SystemFilterPoolReferenceManager;
import org.eclipse.rse.logging.Logger;
import org.eclipse.rse.model.ISystemProfile;



/**
 * Static methods for creating and restoring the "front doors" to the filter framework
 * <ul>
 *   <li>SystemFilterNamingPolicy. This tells the framework what to use for file names when
 *         saving and restoring to/from disk.
 *   <li>SystemFilterPoolManager. This manages master lists of filter pools. Use only these
 *         APIs for creating:
 *       <ul>
 *           <li>SystemFilterPools.
 *           <li>SystemFilters
 *       </ul>
 *   <li>SystemFilterPoolReferenceManager. This manages a persistable list of filter pool 
 *         references. Use its APIs for creating:
 *       <ul>
 *           <li>SystemFilterPoolReferences.
 *       </ul>
 * </ul>
 */
public class SystemFilterStartHere 
       implements ISystemFilterConstants
{
    /**
     * Factory method to return an instance populated with defaults.
     * You can then simply override whatever is desired via setXXX methods.
     */
    public static ISystemFilterNamingPolicy createSystemFilterNamingPolicy()
    {
    	return SystemFilterNamingPolicy.getNamingPolicy();
    }
	
	
    /**
     * Factory to create a filter pool manager, when you do NOT want it to worry about 
     *  saving and restoring the filter data to disk. Rather, you will save and restore
     *  yourself.
     * @param logger A logging object into which to log errors as they happen in the framework
     * @param caller Objects which instantiate this class should implement the
     *   SystemFilterPoolManagerProvider interface, and pass "this" for this parameter.
     *   Given any filter framework object, it is possible to retrieve the caller's
     *   object via the getProvider method call.
     * @param name the name of the filter pool manager. Not currently used but you may
     *   find a use for it.
     * @param allowNestedFilters true if filters inside filter pools in this manager are
     *   to allow nested filters. This is the default, but can be overridden at the 
     *   individual filter pool level.
     */
    public static ISystemFilterPoolManager 
                    createSystemFilterPoolManager(ISystemProfile profile, 
                    								Logger logger,
                                                  ISystemFilterPoolManagerProvider caller,
                                                  String name,
                                                  boolean allowNestedFilters)
    {
    	return SystemFilterPoolManager.createSystemFilterPoolManager(profile, logger, caller, 
    	             name, allowNestedFilters, SAVE_POLICY_NONE, null);
    }



    /**
     * Create a SystemFilterPoolReferenceManager instance, when you  do NOT want it 
     *  to be saved and restored to its own file. Rather, you will save and restore it
     *  yourself.
     * @param caller Objects which instantiate this class should implement the
     *   SystemFilterPoolReferenceManagerProvider interface, and pass "this" for this parameter.
     *   Given any filter framework object, it is possible to retrieve the caller's
     *   object via the getProvider method call.
     * @param relatedPoolManagers The managers that own the master list of filter pools that 
     *   this manager will contain references to.
     * @param name the name of the filter pool reference manager. This is not currently 
     *   used, but you may find a use for it. 
     * @param namingPolicy the naming policy object which will return the name of that one file.
     */
    public static ISystemFilterPoolReferenceManager createSystemFilterPoolReferenceManager(
                                                    ISystemFilterPoolReferenceManagerProvider caller,
                                                    ISystemFilterPoolManagerProvider relatedPoolMgrProvider,
                                                    String name, ISystemFilterNamingPolicy namingPolicy)
    {
    	return SystemFilterPoolReferenceManager.createSystemFilterPoolReferenceManager(
    	  caller, relatedPoolMgrProvider, null, name, SAVE_POLICY_NONE, namingPolicy);
    }


}