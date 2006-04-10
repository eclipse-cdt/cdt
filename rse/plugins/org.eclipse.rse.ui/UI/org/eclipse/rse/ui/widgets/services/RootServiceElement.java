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

import org.eclipse.rse.ui.SystemResources;


public class RootServiceElement extends ServiceElement
{
	private ServiceElement[] _children;
	
	public RootServiceElement()
	{
		super(null, null);		
	}
	
	public RootServiceElement(ServiceElement[] children)
	{
		super(null, null);
		_children = children;
		for (int i = 0; i < _children.length; i++)
		{
			_children[i].setParent(this);
		}
	}
	
	public void setChildren(ServiceElement[] children)	
	{
		_children = children;
	}

	public String getName()
	{
		return SystemResources.RESID_PROPERTIES_SERVICES_NAME;
	}
	
	public String getDescription()
	{
		return SystemResources.RESID_PROPERTIES_SERVICES_TOOLTIP;
	}

	public boolean hasChildren()
	{
		return true;
	}

	public ServiceElement[] getChildren()
	{
		return _children;
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