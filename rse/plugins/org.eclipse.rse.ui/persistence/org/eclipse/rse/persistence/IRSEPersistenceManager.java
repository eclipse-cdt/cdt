/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.persistence;

import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.filters.ISystemFilterPoolManager;
import org.eclipse.rse.filters.ISystemFilterPoolManagerProvider;
import org.eclipse.rse.logging.Logger;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemHostPool;
import org.eclipse.rse.model.ISystemProfile;
import org.eclipse.rse.model.ISystemProfileManager;


public interface IRSEPersistenceManager 
{
	/**
	 * Register the persistance provider to be used when saving and restoring RSE doms
	 * @param provider
	 */
	public void registerRSEPersistenceProvider(IRSEPersistenceProvider provider);
	
	/**
	 * Restore all profiles
	 * @param profileManager
	 * @return true if successful
	 */
	public boolean restore(ISystemProfileManager profileManager);
	
	/**
	 * Save all profiles
	 * @param profileManager
	 * @return true if successful
	 */
	public boolean commit(ISystemProfileManager profileManager);
	
	public boolean commit(IHost host);
	
	/**
	 * Restore all connections in the connection pool
	 * @param connectionPool
	 * @return true if successful
	 */
	public boolean restore(ISystemHostPool connectionPool);
	
	/**
	 * Save all connections in the connection pool
	 * @param connectionPool
	 * @return true if successful
	 */
	public boolean commit(ISystemHostPool connectionPool);
	
	public boolean commit(ISystemFilterPoolManager filterPoolManager);
	/**
	 * Save all the filters in the filter pool
	 * @param filterPool
	 * @return true if successful
	 */
	public boolean commit(ISystemFilterPool filterPool);
	
	/**
	 * Save this filter
	 * @param filter
	 * @return true if successful
	 */
	public boolean commit(ISystemFilter filter);
	
	/**
	 * Restore all the filters for the filter pool
	 * @param filterPool
	 * @return true if sucessful
	 */
	public boolean restore(ISystemFilterPool filterPool);
	
	/**
	 * Restore the filter pool
	 * @param name
	 * @return the filter pool if successful
	 */
	public ISystemFilterPool restoreFilterPool(String name);
	
	
	/**
	 * Save this subsystem
	 * @param subSystem
	 * @return true if successful
	 */
	public boolean commit(ISubSystem subSystem);
	
	/**
	 * Save this profile
	 * @param profile
	 * @return true if successful
	 */
	public boolean commit(ISystemProfile profile);
	
	public ISystemFilterPoolManager restoreFilterPoolManager(ISystemProfile profile, Logger logger, ISystemFilterPoolManagerProvider caller,  String name);
    
    /**
     * Save the profile externally
     * @param profile
     * @param clean indicates whether to create from scratch or merger
     * @return
     */
    public boolean save(ISystemProfile profile, boolean clean);
    
	public boolean isExporting();
	public boolean isImporting();
}