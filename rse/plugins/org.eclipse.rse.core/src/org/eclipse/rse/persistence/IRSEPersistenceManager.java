/********************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation. All rights reserved.
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
 * David Dykstal (IBM) - 142806: refactoring persistence framework
 ********************************************************************************/

package org.eclipse.rse.persistence;

import org.eclipse.rse.core.filters.ISystemFilterPool;
import org.eclipse.rse.core.filters.ISystemFilterPoolManager;
import org.eclipse.rse.core.filters.ISystemFilterPoolManagerProvider;
import org.eclipse.rse.core.model.ISystemHostPool;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.ISystemProfileManager;
import org.eclipse.rse.logging.Logger;

public interface IRSEPersistenceManager {

	public boolean commit(ISystemFilterPoolManager filterPoolManager);

	/**
	 * Save all connections in the connection pool
	 * @param connectionPool
	 * @return true if successful
	 */
	public boolean commit(ISystemHostPool connectionPool);

	/**
	 * Save this profile
	 * @param profile
	 * @return true if successful
	 */
	public boolean commit(ISystemProfile profile);

	/**
	 * Save all profiles
	 * @param profileManager
	 * @return true if successful
	 */
	public boolean commit(ISystemProfileManager profileManager);

	/**
	 * Delete the persistent form of a profile.
	 * @param profileName The name of the profile to delete
	 */
	public void deleteProfile(String profileName);

	public boolean isExporting();

	public boolean isImporting();

	/**
	 * Register the persistence provider to be used when saving and restoring RSE doms.
	 * The provider is registered under the provided id.
	 * @param id the provider id.
	 * @param provider the provider.
	 */
	public void registerRSEPersistenceProvider(String id, IRSEPersistenceProvider provider);

	/**
	 * Restore all the filters for the filter pool
	 * @param filterPool
	 * @return true if sucessful
	 */
	public boolean restore(ISystemFilterPool filterPool);

	/**
	 * Restore all connections in the connection pool
	 * @param connectionPool
	 * @return true if successful
	 */
	public boolean restore(ISystemHostPool connectionPool);

	/**
	 * Restore all profiles
	 * @param profileManager
	 * @return true if successful
	 */
	public boolean restore(ISystemProfileManager profileManager);

	/**
	 * Restore the filter pool
	 * @param name
	 * @return the filter pool if successful
	 */
	public ISystemFilterPool restoreFilterPool(String name);

	public ISystemFilterPoolManager restoreFilterPoolManager(ISystemProfile profile, Logger logger, ISystemFilterPoolManagerProvider caller, String name);
}