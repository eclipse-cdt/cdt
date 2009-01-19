/*******************************************************************************
 * Copyright (c) 2002, 2009 IBM Corporation and others.
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
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * David Dykstal (IBM) - [197036] removed createHost() shortcut (should use ISystemRegistry),
 *    cleaned javadoc for getFilterPools()
 * David Dykstal (IBM) - [202630] getDefaultPrivateProfile() and ensureDefaultPrivateProfile() are inconsistent
 * David Dykstal (IBM) - [200735][Persistence] Delete a profile that contains a connection and restart, profile is back without connections
 * David Dykstal (IBM) - [226561] Add API markup to RSE javadocs for extend / implement
 * David Dykstal (IBM) - [235800] Document naming restriction for profiles and filter pools
 * Martin Oberhuber (Wind River) - [261486][api][cleanup] Mark @noimplement interfaces as @noextend
 *******************************************************************************/

package org.eclipse.rse.core.model;

import org.eclipse.rse.core.filters.ISystemFilterPool;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.persistence.IRSEPersistenceProvider;

/**
 * A system profile holds definitions for hosts (connections), filter pools,
 * filters, and filter strings. It is the unit of persistence for those
 * definitions. Individual hosts and filter pool definitions always reside in a
 * profile and the profile itself is the entity that is saved and restored.
 * <p>
 * Profiles may be active or inactive. An active profile contributes its
 * definitions to RSE. When made inactive, it those definition are no longer
 * available for use.
 * <p>
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients. The
 *           standard implementations are included in the framework.
 */
public interface ISystemProfile extends IRSEModelObject {

	/**
	 * Set the in-memory pointer back to the parent system profile manager
	 */
	public void setProfileManager(ISystemProfileManager mgr);

	/**
	 * Get the in-memory pointer back to the parent system profile manager
	 */
	public ISystemProfileManager getProfileManager();

	/**
	 * @return The value of the Name attribute
	 */
	String getName();

	/**
	 * Sets the name of the profile.
	 * Profile names must not contain three consecutive underscores "___", since these are used
	 * to separate a profile name from a filter pool name in a filter pool reference.
	 * @param value The new value of the Name attribute
	 * @throws IllegalArgumentException if the name contains three consecutive underscore characters.
	 */
	void setName(String value);

	/**
	 * @return The value of the DefaultPrivate attribute
	 * Is this profile created automatically, and is it the profile
	 * that is unique for this developer?
	 */
	boolean isDefaultPrivate();

	/**
	 * @generated This field/method will be replaced during code generation
	 * @param value The new value of the DefaultPrivate attribute
	 */
	void setDefaultPrivate(boolean value);

	/**
	 * Return all connections for this profile
	 */
	public IHost[] getHosts();

	/**
	 * @return all existing filter pools for this profile.
	 */
	public ISystemFilterPool[] getFilterPools();

	/**
	 * Return all filter pools for this profile, scoped by a given subsystem factory
	 */
	public ISystemFilterPool[] getFilterPools(ISubSystemConfiguration ssf);

	/**
	 * Return true if this profile is currently active.
	 * An active profile is one that whose connections and filter pools are available
	 * for use by RSE. A profile may be loaded but be inactive.
	 * The active state of a profile is remembered from session to session.
	 */
	public boolean isActive();

	/**
	 * Activates or deactivates a profile. If the profile is already in the
	 * requested state, this will do nothing. The default private system profile
	 * cannot be deactivated and such a request will be ignored.
	 * @param flag true to activate the profile, false to deactivate it.
	 * @see ISystemProfile#isActive()
	 */
	public void setActive(boolean flag);

	/**
	 * Suspend this profile.
	 * Suspended profiles ignore commit requests.
	 * Profiles are created in a non-suspended state.
	 * Profiles should be suspended while deleting their contents prior to their own deletion.
	 * Note that being non-suspended is a different condition than being active.
	 * A suspended profile may be resumed.
	 * @since 3.0
	 * @see #resume()
	 */
	public void suspend();

	/**
	 * Resume this profile from a suspended state.
	 * The profile will now honor commit requests.
	 * @since 3.0
	 * @see #suspend()
	 */
	public void resume();

	/**
	 * @return true if the profile is in a suspended state
	 * @since 3.0
	 * @see #suspend()
	 * @see #resume()
	 */
	public boolean isSuspended();

	/**
	 * Each profile is persisted by a persistence provider. This returns the instance of the
	 * persistence provider used for this profile. New profiles will use the default persistence
	 * provider unless one is set by some other means.
	 * @return The persistence provider used for saving and restoring this profile.
	 */
	public IRSEPersistenceProvider getPersistenceProvider();

	/**
	 * Sets the persistence provider for the use of this profile. If this is not called then
	 * this profile will be persisted by the default persistence provider. This will typically
	 * be set by either a persistence persistence provider when restoring a profile or by a migration
	 * utility when converting profiles from one form to another.
	 * @param provider the persistence provider to use when saving this profile.
	 */
	public void setPersistenceProvider(IRSEPersistenceProvider provider);
}
