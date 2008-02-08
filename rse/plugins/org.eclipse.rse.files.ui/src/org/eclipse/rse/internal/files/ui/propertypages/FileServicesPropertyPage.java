/********************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [175262] IHost.getSystemType() should return IRSESystemType 
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * David Dykstal (IBM) - [217556] remove service subsystem types
 ********************************************************************************/

package org.eclipse.rse.internal.files.ui.propertypages;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.IFileServiceSubSystemConfiguration;
import org.eclipse.rse.ui.propertypages.ServicesPropertyPage;
import org.eclipse.rse.ui.widgets.services.FactoryServiceElement;
import org.eclipse.rse.ui.widgets.services.ServiceElement;


public class FileServicesPropertyPage extends ServicesPropertyPage
{

	private IFileServiceSubSystemConfiguration _currentFactory;
	
	protected FileServiceSubSystem getFileServiceSubSystem()
	{
		return (FileServiceSubSystem)getElement();
	}
	
	protected ServiceElement[] getServiceElements()
	{
		FileServiceSubSystem subSystem = getFileServiceSubSystem();
	
		IHost host = subSystem.getHost();
		_currentFactory = (IFileServiceSubSystemConfiguration)subSystem.getParentRemoteFileSubSystemConfiguration();
		IFileServiceSubSystemConfiguration[] factories = getFileServiceSubSystemConfigurations(host.getSystemType());
		
		
		// create elements for each 
		ServiceElement[] elements = new ServiceElement[factories.length];
		for (int i = 0; i < factories.length; i++)
		{	
			IFileServiceSubSystemConfiguration factory = factories[i];
			elements[i] = new FactoryServiceElement(host, factory);
			if (factory == _currentFactory)
			{
				elements[i].setSelected(true);
			}
		}
		
		return elements;
	}
	
	protected IFileServiceSubSystemConfiguration[] getFileServiceSubSystemConfigurations(IRSESystemType systemType)
	{
		List results = new ArrayList();
		ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
		ISubSystemConfiguration[] factories = sr.getSubSystemConfigurationsBySystemType(systemType, false);
		
		for (int i = 0; i < factories.length; i++)
		{
			ISubSystemConfiguration factory = factories[i];
			if (factory instanceof IFileServiceSubSystemConfiguration)
			{
				results.add(factory);
			}
		}
		
		return (IFileServiceSubSystemConfiguration[])results.toArray(new IFileServiceSubSystemConfiguration[results.size()]);
	}
	
	
	protected ISubSystemConfiguration getCurrentSubSystemConfiguration()
	{
		return _currentFactory;
	}

	public void setSubSystemConfiguration(ISubSystemConfiguration factory)
	{
		_currentFactory = (IFileServiceSubSystemConfiguration)factory;
	}


}