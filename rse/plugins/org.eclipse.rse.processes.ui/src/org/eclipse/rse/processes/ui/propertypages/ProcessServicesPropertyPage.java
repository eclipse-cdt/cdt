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

package org.eclipse.rse.processes.ui.propertypages;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rse.core.servicesubsystem.IServiceSubSystemConfiguration;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.subsystems.processes.servicesubsystem.IProcessServiceSubSystemConfiguration;
import org.eclipse.rse.subsystems.processes.servicesubsystem.ProcessServiceSubSystem;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.propertypages.ServicesPropertyPage;
import org.eclipse.rse.ui.widgets.services.FactoryServiceElement;
import org.eclipse.rse.ui.widgets.services.ServiceElement;


public class ProcessServicesPropertyPage extends ServicesPropertyPage
{
	private IProcessServiceSubSystemConfiguration _currentFactory;
	protected ProcessServiceSubSystem getProcessServiceSubSystem()
	{
		return (ProcessServiceSubSystem)getElement();
	}
	
	protected ServiceElement[] getServiceElements()
	{
		ProcessServiceSubSystem subSystem = getProcessServiceSubSystem();
	
		IHost host = subSystem.getHost();
		_currentFactory = (IProcessServiceSubSystemConfiguration)subSystem.getParentRemoteProcessSubSystemConfiguration();
		IProcessServiceSubSystemConfiguration[] factories = getProcessServiceSubSystemFactories(host.getSystemType());
		
		
		// create elements for each 
		ServiceElement[] elements = new ServiceElement[factories.length];
		for (int i = 0; i < factories.length; i++)
		{	
			IProcessServiceSubSystemConfiguration factory = factories[i];
			elements[i] = new FactoryServiceElement(host, factory);
			if (factory == _currentFactory)
			{
				elements[i].setSelected(true);
			}
		}
		
		return elements;
	}
	
	protected IProcessServiceSubSystemConfiguration[] getProcessServiceSubSystemFactories(String systemType)
	{
		List results = new ArrayList();
		ISystemRegistry sr = RSEUIPlugin.getTheSystemRegistry();
		ISubSystemConfiguration[] factories = sr.getSubSystemConfigurationsBySystemType(systemType);
		
		for (int i = 0; i < factories.length; i++)
		{
			ISubSystemConfiguration factory = factories[i];
			if (factory instanceof IProcessServiceSubSystemConfiguration)
			{
				results.add(factory);
			}
		}
		
		return (IProcessServiceSubSystemConfiguration[])results.toArray(new IProcessServiceSubSystemConfiguration[results.size()]);
	}

	protected IServiceSubSystemConfiguration getCurrentServiceSubSystemConfiguration()
	{
		return _currentFactory;
	}

	public void setSubSystemConfiguration(ISubSystemConfiguration factory)
	{
		_currentFactory = (IProcessServiceSubSystemConfiguration)factory;
	}
}