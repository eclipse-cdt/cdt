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


import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.IServerLauncherProperties;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.swt.graphics.Image;




public class ConnectorServiceElement extends RSEModelServiceElement
{

	private IConnectorService _connectorService;
	private ImageDescriptor _imageDescriptor;
	private ServiceElement[] _children;

	public ConnectorServiceElement(IHost host, ServiceElement parent, IConnectorService connectorService)
	{
		super(host, parent, connectorService);
		_connectorService = connectorService;

	}
		
	public IConnectorService getConnectorService()
	{
		return _connectorService;
	}
	
	public Image getImage()
	{
		if (_imageDescriptor == null)
		{
			_imageDescriptor= RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_CONNECTOR_SERVICE_ID);
		}
		return _imageDescriptor.createImage();
	}
	
	public String getName()
	{
		return _connectorService.getName();
	}

	public boolean internalHasChildren()
	{
		return _connectorService.hasRemoteServerLauncherProperties();
	}
	
	public ServiceElement[] internalGetChildren()
	{
		if (_children == null)
		{
			IServerLauncherProperties properties = _connectorService.getRemoteServerLauncherProperties();
			ServiceElement child = new ServerLauncherPropertiesServiceElement(getHost(), this, properties);
			return new ServiceElement[] { child};
		}
		return _children;
	}

	public void commit()
	{
		super.commit();
		ServiceElement[] children = getChildren();
		if (children != null)
		{
			for (int i = 0; i < children.length; i++)
			{
				ServiceElement child = children[i];
				child.commit();
			}
		}
		_connectorService.commit();
	}

	public void revert()
	{
		super.revert();
		ServiceElement[] children = getChildren();
		if (children != null)
		{
			for (int i = 0; i < children.length; i++)
			{
				ServiceElement child = children[i];
				child.revert();
			}
		}
	}

}