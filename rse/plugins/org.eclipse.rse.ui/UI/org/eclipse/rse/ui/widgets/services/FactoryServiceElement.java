/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
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
 * David Dykstal (IBM) - [217556] remove service subsystem types
 *******************************************************************************/

package org.eclipse.rse.ui.widgets.services;

import org.eclipse.rse.core.model.DummyHost;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.IDelegatingConnectorService;
import org.eclipse.rse.core.subsystems.IServerLauncherProperties;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.services.IService;




public class FactoryServiceElement extends ServiceElement
{

	private ISubSystemConfiguration _factory;
	private ServiceElement[] _children;


	/**
	 * Constructor. Used to accept an IServiceSubSystemConfiguration before RSE
	 * 3.0
	 * 
	 * @since 3.0
	 */
	public FactoryServiceElement(IHost host, ISubSystemConfiguration factory)
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

	/**
	 * Return the subsystem configuration related to this service element. Used
	 * to return an IServiceSubSystemConfiguration before RSE 3.0.
	 *
	 * @since 3.0
	 */
	public ISubSystemConfiguration getFactory()
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

			ServiceServiceElement serviceElement = new ServiceServiceElement(host, this, getService());

			IConnectorService connectorService = getConnectorService();
			if (connectorService != null && !(connectorService instanceof IDelegatingConnectorService))
			{
				_children = new ServiceElement[2];
				_children[0] = serviceElement;
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
			else
			{
				_children = new ServiceElement[1];
				_children[0] = serviceElement;
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
		if (_isSelected)
		{
		ServiceElement[] children = getChildren();
		for (int i = 0; i < children.length; i++)
		{
			//if (children[i].isSelected())
				children[i].commit();
		}
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
