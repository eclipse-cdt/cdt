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
 * Martin Oberhuber (Wind River) - [177523] Unify singleton getter methods
 ********************************************************************************/

package org.eclipse.rse.internal.core.model;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.filters.ISystemFilterPool;
import org.eclipse.rse.core.filters.ISystemFilterPoolManager;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.IRSEPersistableContainer;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.ISystemProfileManager;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.model.RSEModelObject;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.internal.core.RSECoreMessages;
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
     * Convenience method for create a new connection within this profile.
     * Shortcut for {@link ISystemRegistry#createHost(String,String,String,String)}
     */
    public IHost createHost(IRSESystemType systemType, String connectionName, String hostName, String description) throws Exception
    {
		return RSECorePlugin.getTheSystemRegistry().createHost(getName(), systemType, connectionName,  hostName, description);
    }
    
	/**
	 * Return all connections for this profile
	 */
	public IHost[] getHosts()
	{
		return RSECorePlugin.getTheSystemRegistry().getHostsByProfile(this);
	}

	/**
	 * Return all filter pools for this profile
	 */
	public ISystemFilterPool[] getFilterPools()
	{
		ISubSystemConfiguration[] ssFactories = RSECorePlugin.getTheSystemRegistry().getSubSystemConfigurations();
		Vector poolsVector = new Vector();
		for (int idx = 0; idx < ssFactories.length; idx++)
		{
			ISystemFilterPoolManager poolMgr = ssFactories[idx].getFilterPoolManager(this);
			ISystemFilterPool[] pools = poolMgr.getSystemFilterPools();
			for (int ydx=0; ydx<pools.length; ydx++)
			{
				poolsVector.add(pools[ydx]);
			}
		}
		ISystemFilterPool[] allPools = new ISystemFilterPool[poolsVector.size()];
		poolsVector.toArray(allPools);
		return allPools;
	}

	/**
	 * Return all filter pools for this profile, scoped by a given subsystem factory
	 */
	public ISystemFilterPool[] getFilterPools(ISubSystemConfiguration ssf)
	{
		ISystemFilterPoolManager poolMgr = ssf.getFilterPoolManager(this);
		return poolMgr.getSystemFilterPools();
	}

	/**
	 * Return true if this profile is currently active.
	 */
	public boolean isActive()
	{
		return isActive;
	}
	/**
	 * Reset whether this profile is currently active.
	 */
	public void setActive(boolean active)
	{
		this.isActive = active;
		setDirty(true);
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

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public void setName(String newName)
	{
		name = newName;
		setDirty(true);
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
	
	public boolean commit() 
	{
		boolean result = false;
		if (!RSECorePlugin.getThePersistenceManager().isBusy()) {
			result = RSECorePlugin.getThePersistenceManager().commitProfile(this, 5000);
		}
		return result;
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