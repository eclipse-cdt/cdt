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
 * Martin Oberhuber (Wind River) - [180519][api] declaratively register adapter factories
 * Martin Oberhuber (Wind River) - [186748] Move ISubSystemConfigurationAdapter from UI/rse.core.subsystems.util
 ********************************************************************************/

package org.eclipse.rse.internal.processes.ui.view;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcessSubSystemConfiguration;
import org.eclipse.rse.ui.subsystems.ISubSystemConfigurationAdapter;


public class RemoteProcessSubSystemConfigurationAdapterFactory implements IAdapterFactory
{

	private ISubSystemConfigurationAdapter ssFactoryAdapter = new RemoteProcessSubSystemConfigurationAdapter();
	
	/**
	 * @see IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() 
	{
	    return new Class[] {ISubSystemConfigurationAdapter.class};		
	}
	
//	/**
//	 * Register this factory with the Platform's Adapter Manager.
//	 * Can be used for explicit registration, but we prefer doing it 
//	 * declaratively in plugin.xml so this is currently not used. 
//	 */
//	public void registerWithManager(IAdapterManager manager)
//	{
//		manager.registerAdapters(this, IRemoteProcessSubSystemConfiguration.class);
//	}
	
	/**
	 * @see IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	public Object getAdapter(Object adaptableObject, Class adapterType) 
	{
	    Object adapter = null;
	    if (adaptableObject instanceof IRemoteProcessSubSystemConfiguration)
	    	adapter = ssFactoryAdapter;
	      	    
		return adapter;
	}


}