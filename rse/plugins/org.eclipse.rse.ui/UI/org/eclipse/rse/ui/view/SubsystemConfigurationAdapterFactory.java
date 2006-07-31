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

package org.eclipse.rse.ui.view;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.core.subsystems.util.ISubsystemConfigurationAdapter;


public class SubsystemConfigurationAdapterFactory implements IAdapterFactory
{

	private ISubsystemConfigurationAdapter ssFactoryAdapter = new SubsystemConfigurationAdapter();
	
	/**
	 * @see IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() 
	{
	    return new Class[] {ISubsystemConfigurationAdapter.class};		
	}
	/**
	 * Called by our plugin's startup method to register our adaptable object types 
	 * with the platform. We prefer to do it here to isolate/encapsulate all factory
	 * logic in this one place.
	 */
	public void registerWithManager(IAdapterManager manager)
	{
		manager.registerAdapters(this, ISubSystemConfiguration.class);
	}
	/**
	 * @see IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	public Object getAdapter(Object adaptableObject, Class adapterType) 
	{
	    Object adapter = null;
	    if (adaptableObject instanceof ISubSystemConfiguration)
	    	adapter = ssFactoryAdapter;
	      	    
		return adapter;
	}


}