/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.shells.ui.propertypages;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.core.servicesubsystem.IServiceSubSystemConfiguration;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.model.DummyHost;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem.IShellServiceSubSystemConfiguration;
import org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem.ShellServiceSubSystem;
import org.eclipse.rse.ui.propertypages.ServicesPropertyPage;
import org.eclipse.rse.ui.widgets.services.FactoryServiceElement;
import org.eclipse.rse.ui.widgets.services.ServiceElement;


public class ShellServicesPropertyPage extends ServicesPropertyPage
{

	private IShellServiceSubSystemConfiguration _currentFactory;

	
	protected ShellServiceSubSystem getShellServiceSubSystem()
	{
		return (ShellServiceSubSystem)getElement();
	}
	

	protected ServiceElement[] getServiceElements()
	{
		ShellServiceSubSystem subSystem = getShellServiceSubSystem();
		IShellServiceSubSystemConfiguration[] factories = null;
		IHost host = null;
		if (subSystem == null || _currentFactory != null)
		{
			// create dummy host
			factories = getShellServiceSubSystemFactories(getSystemType());
			host = new DummyHost(getHostname(), getSystemType());
		}
		else
		{
			host = subSystem.getHost();
			_currentFactory = (IShellServiceSubSystemConfiguration)subSystem.getParentRemoteCmdSubSystemFactory();
			factories = getShellServiceSubSystemFactories(host.getSystemType());
		}
		
		// create elements for each
		ServiceElement[] elements = new ServiceElement[factories.length];
		for (int i = 0; i < factories.length; i++)
		{	
			IShellServiceSubSystemConfiguration factory = factories[i];
			
			elements[i] = new FactoryServiceElement(host, factory);
			if (factory == _currentFactory)
			{
				elements[i].setSelected(true);
			}
		}
		return elements;
	}
	
	
	protected IShellServiceSubSystemConfiguration[] getShellServiceSubSystemFactories(String systemType)
	{
		List results = new ArrayList();
		ISystemRegistry sr = SystemPlugin.getTheSystemRegistry();
		ISubSystemConfiguration[] factories = sr.getSubSystemConfigurationsBySystemType(systemType);
		
		for (int i = 0; i < factories.length; i++)
		{
			ISubSystemConfiguration factory = factories[i];
			if (factory instanceof IShellServiceSubSystemConfiguration)
			{
				results.add(factory);
			}
		}
		
		return (IShellServiceSubSystemConfiguration[])results.toArray(new IShellServiceSubSystemConfiguration[results.size()]);
	}


	protected IServiceSubSystemConfiguration getCurrentServiceSubSystemFactory()
	{
		return _currentFactory;
	}
	
	public void setSubSystemFactory(ISubSystemConfiguration factory)
	{
		_currentFactory = (IShellServiceSubSystemConfiguration)factory;
	}
}