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

import org.eclipse.rse.core.model.ISystemProfile;

public interface IRSEPersistenceManager {

	/**
	 * Save a particular profile. If the profile has an existing persistence provider
	 * it is saved by that persistence provider. If the profile has no persistence provider
	 * then the default persistence provider is used.
	 * @param profile the profile to save
	 * @return true if successful
	 */
	public boolean commitProfile(ISystemProfile profile);

	/**
	 * Save all profiles.
	 * @return true if successful
	 */
	public boolean commitProfiles();

	/**
	 * Restore all profiles
	 * @return an array of restored profiles.
	 */
	public ISystemProfile[] restoreProfiles();

	/**
	 * Restore the profiles for a particular provider.
	 * @param provider a persistence provider
	 * @return an array of the restored profiles
	 */
	public ISystemProfile[] restoreProfiles(IRSEPersistenceProvider provider);

	/**
	 * Delete the persistent form of a profile.
	 * @param persistenceProvider the persistence provider to use to delete the profile.
	 * If this is null the default persistence provider is used.
	 * @param profileName The name of the profile to delete
	 */
	public void deleteProfile(IRSEPersistenceProvider persistenceProvider, String profileName);
	
	/**
	 * Migrates a profile to a new persistence provider. It will delete the persistent form known to its previous
	 * persistence provider. If the new provider and the previous provider are the same this does nothing.
	 * @param profile the system profile to be migrated
	 * @param persistenceProvider the persistence provider to which this profile will be migrated.
	 */
	public void migrateProfile(ISystemProfile profile, IRSEPersistenceProvider persistenceProvider);

	/**
	 * Register the persistence provider to be used when saving and restoring RSE doms.
	 * The provider is registered under the provided id.
	 * If the id has already been registered, this provider replaces the previous provider
	 * with that id.
	 * @param id the provider id.
	 * @param provider the provider.
	 */
	public void registerPersistenceProvider(String id, IRSEPersistenceProvider provider);
	
	/**
	 * @return an array of persistence provider ids known to this workbench. These may have been
	 * provided by extension point or by registering them using 
	 * {@link #registerPersistenceProvider(String, IRSEPersistenceProvider)}
	 */
	public String[] getPersistenceProviderIds();
	
	/**
	 * Retrieves the persistence provider named by a particular id. It can return null if there
	 * is no provider known by that id. This may have the effect of activating the plugin that 
	 * contains this provider.
	 * @param id the id of the persistence provider to locate
	 * @return the persistence provider or null
	 */
	public IRSEPersistenceProvider getPersistenceProvider(String id);

	/**
	 * @return true if this instance of the persistence manager is currently exporting a profile.
	 */
	public boolean isExporting();

	/**
	 * @return true if this instance of the persistence manager is currently importing a profile.
	 */
	public boolean isImporting();
	
	/**
	 * Indicate if all profiles for a particular persistence provider have been restored.
	 * Profiles are typically restored when RSE is activated and when profiles
	 * are reloaded by the user. This will not load the persistence provider. If the persistence
	 * provider is not loaded it will return false.
	 * @param providerId the persistence providerId
	 * @return true if the profiles have been fully restored
	 */
	public boolean isRestoreComplete(String providerId);
	
}