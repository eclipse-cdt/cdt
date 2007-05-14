/********************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [186748] Move ISubSystemConfigurationAdapter from UI/rse.core.subsystems.util
 ********************************************************************************/
package org.eclipse.rse.examples.daytime.ui;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IAdapterManager;

import org.eclipse.rse.examples.daytime.subsystems.DaytimeSubSystemConfiguration;
import org.eclipse.rse.ui.subsystems.ISubSystemConfigurationAdapter;

public class DaytimeSubSystemConfigurationAdapterFactory implements IAdapterFactory {

	private ISubSystemConfigurationAdapter ssFactoryAdapter = new DaytimeSubSystemConfigurationAdapter();
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() 
	{
	    return new Class[] {ISubSystemConfigurationAdapter.class};		
	}
	
	/**
	 * Called by our plugin's startup method to register our adaptable object types 
	 * with the platform. We prefer to do it here to isolate/encapsulate all factory
	 * logic in this one place.
	 * 
	 * @param manager Platform adapter manager to register with
	 */
	public void registerWithManager(IAdapterManager manager)
	{
		manager.registerAdapters(this, DaytimeSubSystemConfiguration.class);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	public Object getAdapter(Object adaptableObject, Class adapterType) 
	{
	    Object adapter = null;
	    if (adaptableObject instanceof DaytimeSubSystemConfiguration)
	    	adapter = ssFactoryAdapter;
	      	    
		return adapter;
	}

}
