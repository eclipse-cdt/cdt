/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
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
 * David Dykstal (IBM) - [cleanup] adding noimplement tag
 * David Dykstal (IBM) - [225988] need API to mark persisted profiles as migrated
 * Martin Oberhuber (Wind River) - [261486][api][cleanup] Mark @noimplement interfaces as @noextend
 *******************************************************************************/

package org.eclipse.rse.persistence;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.rse.core.model.ISystemProfile;

/**
 * This interface defines the services provided by a persistence manager for
 * RSE. There is typically only one persistence manager instance defined when
 * RSE is running. The persistence manager controls the persistence of RSE
 * profiles through the use of registered persistence providers.
 *
 * @noimplement this interface is not intended to be implemented by clients
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IRSEPersistenceManager {

	/**
	 * Schedules a save of particular profile. If the profile has an existing persistence provider
	 * it is saved by that persistence provider. If the profile has no persistence provider
	 * then the default persistence provider is used. If the persistence manager is in a state where
	 * it is saving or restoring another profile on another thread this call will block for the
	 * timeout value specified. If the timeout expires this call will return false.
	 * @param profile the profile to save
	 * @param timeout the timeout value in milliseconds. If the operation cannot be started in this time
	 * it will return false.
	 * @return true if the save was scheduled and false if the timeout expired without scheduling the save.
	 */
	public boolean commitProfile(ISystemProfile profile, long timeout);

	/**
	 * Save all profiles. Will attempt to schedule a save of all profiles. Each attempt will time out after
	 * the number of milliseconds specified if the operation cannot be started.
	 * @param timeout the maximum number of milliseconds to wait until the persistence manager becomes available
	 * to schedule a save for an individual profile.
	 * @return the list of profiles that could not be scheduled for save.
	 */
	public ISystemProfile[] commitProfiles(long timeout);

	/**
	 * Restore all profiles known to autostart persistence providers.
	 * @param timeout the maximum number of milliseconds to wait for the manager to become idle for each profile.
	 * @return an array of restored profiles.
	 */
	public ISystemProfile[] restoreProfiles(long timeout);

	/**
	 * Restore the profiles for a particular provider.
	 * @param provider a persistence provider
	 * @param timeout the maximum number of milliseconds to wait for the manager to become idle before restoring this
	 * the each profile managed by this provider.
	 * @return an array of the restored profiles.
	 */
	public ISystemProfile[] restoreProfiles(IRSEPersistenceProvider provider, long timeout);

	/**
	 * Delete the persistent form of a profile.
	 * @param persistenceProvider the persistence provider to use to delete the profile.
	 * If this is null the default persistence provider is used.
	 * @param profileName The name of the profile to delete
	 */
	public void deleteProfile(IRSEPersistenceProvider persistenceProvider, String profileName);

	/**
	 * Migrates a profile to a new persistence provider.
	 * It will delete the persistent form known to its previous persistence provider.
	 * If the new provider and the previous provider are the same this does nothing.
	 * Exactly the same as <code>migrateProfile(profile, persistenceProvider, true);</code>
	 * @param profile the system profile to be migrated
	 * @param persistenceProvider the persistence provider to which this profile will be migrated.
	 */
	public void migrateProfile(ISystemProfile profile, IRSEPersistenceProvider persistenceProvider);

	/**
	 * Migrates a profile to a new persistence provider. It will mark the
	 * persistent form known to its previous persistence provider as migrated.
	 * This may, in fact, result in the persistent form of this profile being
	 * deleted. If the new provider and the previous provider are the same this
	 * does nothing.
	 *
	 * @param profile the system profile to be migrated
	 * @param persistenceProvider the persistence provider to which this profile
	 *            will be migrated.
	 * @param delete true if the persistent form of this profile is to be
	 *            deleted from the old provider, false if the persistent form of
	 *            the profile is to be marked as migrated.
	 * @return an IStatus indicating the success of the migration.
	 * @since org.eclipse.rse.core 3.0
	 */
	public IStatus migrateProfile(ISystemProfile profile, IRSEPersistenceProvider persistenceProvider, boolean delete);

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
	 * @return true if this instance of the persistence manager is currently saving or restoring a profile.
	 */
	public boolean isBusy();

	/**
	 * Indicate if all profiles for all autostart persistence provider have been restored.
	 * These profiles are restored when RSE is activated and when profiles
	 * are reloaded by the user.
	 * This can be used from a different thread
	 * than the one that requested the restore.
	 * @return true if the profiles have been fully restored
	 */
	public boolean isRestoreComplete();

}
