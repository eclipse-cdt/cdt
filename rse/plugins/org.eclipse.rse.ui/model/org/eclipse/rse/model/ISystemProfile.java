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

package org.eclipse.rse.model;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.filters.ISystemFilterPool;

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
public interface ISystemProfile extends IRSEModelObject
{
	
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
	 * Shortcut for {@link ISystemRegistry#createHost(String,String,String,String)}
	 */
	public IHost createHost(String systemType, String connectionName, String hostName, String description) throws Exception;
	
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
}