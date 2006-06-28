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

package org.eclipse.rse.internal.model;
import java.util.Vector;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.filters.ISystemFilterPoolManager;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemProfile;
import org.eclipse.rse.model.ISystemProfileManager;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;

/**
 * A profile contains hosts and filter pools. It is the unit of save/restore for RSE model 
 * objects. All model objects are contained within a profile.
 */
public class SystemProfile extends RSEModelObject implements ISystemProfile, IAdaptable
{

	private ISystemProfileManager mgr;
	private boolean active;
	private String name = null;
	private boolean defaultPrivate = false;

	/**
	 * Default constructor
	 */
	protected SystemProfile() 
	{
		super();
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
    public IHost createHost(String systemType, String connectionName, String hostName, String description) throws Exception
    {
		return RSEUIPlugin.getTheSystemRegistry().createHost(getName(), systemType, connectionName,  hostName, description);
    }
    
	/**
	 * Return all connections for this profile
	 */
	public IHost[] getHosts()
	{
		return RSEUIPlugin.getTheSystemRegistry().getHostsByProfile(this);
	}

	/**
	 * Return all filter pools for this profile
	 */
	public ISystemFilterPool[] getFilterPools()
	{
		ISubSystemConfiguration[] ssFactories = RSEUIPlugin.getTheSystemRegistry().getSubSystemConfigurations();
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
		for (int idx=0; idx<allPools.length; idx++)
			allPools[idx] = (ISystemFilterPool)poolsVector.elementAt(idx);
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
		return active;
	}
	/**
	 * Reset whether this profile is currently active.
	 */
	public void setActive(boolean active)
	{
		this.active = active;
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


	public String toString() {
		String result = getName();
		if (result == null) {
			StringBuffer buf = new StringBuffer(super.toString());
			buf.append("Profile(name: ");
			buf.append(name);
			buf.append(", defaultPrivate: ");
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
		return SystemResources.RESID_MODELOBJECTS_PROFILE_DESCRIPTION;
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public void setName(String newName)
	{
		name = newName;
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
	}
	
	public boolean commit() 
	{
		return RSEUIPlugin.getThePersistenceManager().commit(this);
	}

}