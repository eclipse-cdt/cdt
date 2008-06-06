/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
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
 * Martin Oberhuber (Wind River) - [177523] Unify singleton getter methods
 * David Dykstal (IBM) - [197036] changed getFilterPools to not force the loading of subsystem configurations
 *   removed createHost, migrated commit logic to SystemProfileManager
 * David Dykstal (IBM) - [202630] getDefaultPrivateProfile() and ensureDefaultPrivateProfile() are inconsistent
 * David Dykstal (IBM) - [200735][Persistence] Delete a profile that contains a connection and restart, profile is back without connections
 *******************************************************************************/

package org.eclipse.rse.internal.core.model;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.RSEPreferencesManager;
import org.eclipse.rse.core.filters.ISystemFilterPool;
import org.eclipse.rse.core.filters.ISystemFilterPoolManager;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.IRSEPersistableContainer;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.ISystemProfileManager;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.model.RSEModelObject;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.core.subsystems.ISubSystemConfigurationProxy;
import org.eclipse.rse.internal.core.RSECoreMessages;
import org.eclipse.rse.internal.core.filters.SystemFilterPoolReference;
import org.eclipse.rse.persistence.IRSEPersistenceProvider;

/**
 * A profile contains hosts and filter pools. It is the unit of save/restore for RSE model 
 * objects. All model objects are contained within a profile.
 */
public class SystemProfile extends RSEModelObject implements ISystemProfile, IAdaptable
{

	private ISystemProfileManager mgr = null;
	private IRSEPersistenceProvider provider = null;
	private boolean isActive = true;
	private String name = null;
	private boolean defaultPrivate = false;
	
	/**
	 * A suspended profile ignored commit requests.
	 * Profiles must be suspended prior to being deleted.
	 */
	private boolean suspended = false;

	/**
	 * Default constructor
	 */
	protected SystemProfile() 
	{
		super();
	}
	
	public SystemProfile(String name, boolean isActive) {
		this.name = name;
		this.isActive = isActive;
	}

	/**
     * Set the in-memory pointer back to the parent system profile manager
     */
    public void setProfileManager(ISystemProfileManager mgr)
    {
    	this.mgr = mgr;
    }

    /**
     * Get the in-memory pointer back to the parent system profile manager
     */
    public ISystemProfileManager getProfileManager()
    {
    	return mgr;
    }
    
	/**
	 * Return all connections for this profile
	 */
	public IHost[] getHosts()
	{
		return RSECorePlugin.getTheSystemRegistry().getHostsByProfile(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemProfile#getFilterPools()
	 */
	public ISystemFilterPool[] getFilterPools()
	{
		List filterPools = new ArrayList(10); // 10 is arbitrary but reasonable
		ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
		ISubSystemConfigurationProxy proxies[] = registry.getSubSystemConfigurationProxies();
		for (int i = 0; i < proxies.length; i++) {
			ISubSystemConfigurationProxy proxy = proxies[i];
			if (proxy.isSubSystemConfigurationActive()) {
				ISubSystemConfiguration config = proxy.getSubSystemConfiguration();
				ISystemFilterPoolManager fpm = config.getFilterPoolManager(this);
				ISystemFilterPool[] poolArray = fpm.getSystemFilterPools();
				filterPools.addAll(Arrays.asList(poolArray));
			}
		}
		ISystemFilterPool[] result = new ISystemFilterPool[filterPools.size()];
		filterPools.toArray(result);
		return result;
	}

	/**
	 * Return all filter pools for this profile, scoped by a given subsystem factory
	 */
	public ISystemFilterPool[] getFilterPools(ISubSystemConfiguration ssf)
	{
		ISystemFilterPoolManager poolMgr = ssf.getFilterPoolManager(this);
		return poolMgr.getSystemFilterPools();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemProfile#suspend()
	 */
	public void suspend() {
		suspended = true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemProfile#resume()
	 */
	public void resume() {
		suspended = false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemProfile#isSuspended()
	 */
	public boolean isSuspended() {
		return suspended;
	}
	
	/**
	 * Return true if this profile is currently active.
	 */
	public boolean isActive()
	{
		return isActive;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemProfile#setActive(boolean)
	 */
	public void setActive(boolean activate) {
		if (activate) {
			activate();
		} else {
			deactivate();
		}
	}
	
	private void activate() {
		if (!isActive) {
			isActive = true;
			setDirty(true);
			RSEPreferencesManager.addActiveProfile(getName());
		}
	}
	
	private void deactivate() {
		ISystemProfile defaultProfile = mgr.getDefaultPrivateSystemProfile();
		if (isActive && this != defaultProfile) {
			isActive = false;
			setDirty(true);
			RSEPreferencesManager.deleteActiveProfile(getName());
		}
	}

    /**
 	 * This is the method required by the IAdaptable interface.
	 * Given an adapter class type, return an object castable to the type, or
	 *  null if this is not possible.
	 */
    public Object getAdapter(Class adapterType)
    {
   	    return Platform.getAdapterManager().getAdapter(this, adapterType);	
    }   

    //For debugging
	public String toString() {
		String result = getName();
		if (result == null) {
			StringBuffer buf = new StringBuffer(super.toString());
			buf.append("Profile(name: "); //$NON-NLS-1$
			buf.append(name);
			buf.append(", defaultPrivate: "); //$NON-NLS-1$
			buf.append(defaultPrivate);
			buf.append(')');
			result = buf.toString();
		}
		return result;
	}

	/**
	 * @generated This field/method will be replaced during code generation 
	 */
	public String getName()
	{
		return name;
	}
	
	public String getDescription()
	{
		return RSECoreMessages.RESID_MODELOBJECTS_PROFILE_DESCRIPTION;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemProfile#setName(java.lang.String)
	 */
	public void setName(String newName) {
		if (newName.indexOf(SystemFilterPoolReference.DELIMITER) >= 0) {
			throw new IllegalArgumentException("Cannot have ___ in profile name.");
		}
		String oldName = name;
		if (!newName.equals(oldName)) {
			name = newName;
			setDirty(true);
			if (isActive) {
				RSEPreferencesManager.renameActiveProfile(oldName, newName);
			}
		}
	}

	/**
	 * @generated This field/method will be replaced during code generation 
	 * Is this profile created automatically, and is it the profile
	 * that is unique for this developer?
	 */
	public boolean isDefaultPrivate()
	{
		return defaultPrivate;
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public void setDefaultPrivate(boolean newDefaultPrivate)
	{
		defaultPrivate = newDefaultPrivate;
		setDirty(true);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IRSEPersistableContainer#commit()
	 */
	public boolean commit() {
		boolean scheduled =  false;
		if (!suspended) {
			IStatus status = SystemProfileManager.getDefault().commitSystemProfile(this);
			scheduled =  status.isOK();
		}
		return scheduled;
	}
	
	/**
	 * The SystemProfile is the top of the persistence hierarchy.
	 * @return null
	 */
	public IRSEPersistableContainer getPersistableParent() {
		return null;
	}
	
	public IRSEPersistableContainer[] getPersistableChildren() {
		List children = new ArrayList(10);
		children.addAll(Arrays.asList(getFilterPools()));
		children.addAll(Arrays.asList(getHosts()));
		children.addAll(Arrays.asList(getPropertySets()));
		IRSEPersistableContainer[] result = new IRSEPersistableContainer[children.size()];
		children.toArray(result);
		return result;	
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemProfile#getPersistenceProvider()
	 */
	public IRSEPersistenceProvider getPersistenceProvider() {
		return provider;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemProfile#setPersistenceProvider(org.eclipse.rse.persistence.IRSEPersistenceProvider)
	 */
	public void setPersistenceProvider(IRSEPersistenceProvider provider) {
		this.provider = provider;
	}
	
}
