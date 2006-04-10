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

package org.eclipse.rse.ui.widgets.services;

import org.eclipse.rse.core.servicesubsystem.IServiceSubSystemConfiguration;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.IServerLauncherProperties;
import org.eclipse.rse.model.DummyHost;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.services.IService;




public class FactoryServiceElement extends ServiceElement
{

	private IServiceSubSystemConfiguration _factory;	
	private ServiceElement[] _children;


	public FactoryServiceElement(IHost host, IServiceSubSystemConfiguration factory)
	{
		super(host, null);
		_factory = factory;
	}
	
	public String getName()
	{
		return _factory.getId();
	}
	
	public String getDescription()
	{
		return _factory.getDescription();
	}
	
	public IServiceSubSystemConfiguration getFactory()
	{
		return _factory;
	}
	
	public IConnectorService getConnectorService()
	{
		IHost host = getHost();
		IConnectorService connectorService = _factory.getConnectorService(host);
		return connectorService;
	}
	
	public IService getService()
	{
		IHost host = getHost();
		IService service = _factory.getService(host);
		return service;
	}
	
	public ServiceElement[] getChildren()
	{
		if (_children == null)
		{
			IHost host = getHost();
			_children = new ServiceElement[2];
			_children[0] = new ServiceServiceElement(host, this, getService());
		
			IConnectorService connectorService = getConnectorService();		
			_children[1] = new ConnectorServiceElement(host, this, connectorService);
			if (host instanceof DummyHost)
			{
				IServerLauncherProperties sl = connectorService.getRemoteServerLauncherProperties();
				if (sl == null)
				{
					sl = _factory.createServerLauncher(connectorService);
					connectorService.setRemoteServerLauncherProperties(sl);
				}
			}
		}
		return _children;
		
	}
	
	public boolean hasChildren()
	{
		return true;
	}

	public boolean hasPropertySets()
	{
		return false;
	}

	public PropertySetServiceElement[] getPropertySets()
	{
		return null;
	}

	public boolean hasProperties()
	{
		return false;
	}

	public PropertyElement[] getProperties()
	{
		return null;
	}

	public void commit()
	{
		ServiceElement[] children = getChildren();
		for (int i = 0; i < children.length; i++)
		{
			children[i].commit();
		}
	}
	
	public void revert()
	{
		ServiceElement[] children = getChildren();
		for (int i = 0; i < children.length; i++)
		{
			children[i].revert();
		}
	}

}