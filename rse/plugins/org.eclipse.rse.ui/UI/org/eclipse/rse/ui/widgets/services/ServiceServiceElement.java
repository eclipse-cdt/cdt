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
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.services.IService;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.swt.graphics.Image;




public class ServiceServiceElement extends ServiceElement
{
	private IService _service;
	private ImageDescriptor _imageDescriptor;

	public ServiceServiceElement(IHost host, ServiceElement parent, IService service)
	{
		super(host, parent);
		_service = service;
	}
	
	public Image getImage()
	{
		if (_imageDescriptor == null)
		{
			_imageDescriptor= RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_SERVICE_ID);
		}
		return _imageDescriptor.createImage();
	}
	
	public IService getService()
	{
		return _service;
	}

	public String getName()
	{
		return _service.getName();
	}
	
	public String getDescription()
	{
		return _service.getDescription();
	}
	
	public boolean hasChildren()
	{
		return false;
	}
	
	public ServiceElement[] getChildren()
	{
		return null;
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
		// TODO Auto-generated method stub
		return false;
	}

	public PropertyElement[] getProperties()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void commit()
	{
	}
	
	public void revert()
	{
	}
}