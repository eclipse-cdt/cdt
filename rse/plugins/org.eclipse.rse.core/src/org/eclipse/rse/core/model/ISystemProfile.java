/********************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 ********************************************************************************/

package org.eclipse.rse.core.model;

import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.filters.ISystemFilterPool;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.persistence.IRSEPersistenceProvider;

//

/**
 * The interface that RSE system profiles implement.
 * <p>
 * A profile represents a user or name which is used to key important user-data
 *  by:
 * <ul>
 *  <li>Hosts
 *  <li>Filter pools
 *  <li>User actions
 *  <li>Compile commands
 * </ul>
 * 
 * <p>
 * @lastgen interface SystemProfile  {}
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
	 * Convenience method for create a new connection within this profile.
	 * Shortcut for {@link ISystemRegistry#createHost(IRSESystemType,String,String,String)}
	 */
	public IHost createHost(IRSESystemType systemType, String connectionName, String hostName, String description) throws Exception;

	/**
	 * @return The value of the Name attribute
	 */
	String getName();

	/**
	 * @param value The new value of the Name attribute
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
	 * Return all filter pools for this profile
	 */
	public ISystemFilterPool[] getFilterPools();

	/**
	 * Return all filter pools for this profile, scoped by a given subsystem factory
	 */
	public ISystemFilterPool[] getFilterPools(ISubSystemConfiguration ssf);

	/**
	 * Return true if this profile is currently active for this user
	 */
	public boolean isActive();
	
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