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
 * David McKnight   (IBM)        - [252708] Saving Profile Job happens when not changing Property Values on Connections
 ********************************************************************************/

package org.eclipse.rse.ui.widgets.services;

import org.eclipse.rse.core.model.IHost;
import org.eclipse.swt.graphics.Image;




public abstract class ServiceElement
{	
	protected IHost _host;
	protected ServiceElement _parent;
	protected boolean _isSelected = false;
	
	/**
	 * Indicates whether a child of this element has changed
	 * this is used to determine whether or not a commit is required
	 * 
	 * @since 3.1
	 */
	protected boolean _childChanged = false; 
	
	public ServiceElement(IHost host, ServiceElement parent)
	{
		_host = host;
		_parent = parent;
	}
	
	public Image getImage()
	{
		return null;
	}
	
	public IHost getHost()
	{
		if (_host == null)
		{
			ServiceElement[] elements = getChildren();
			if (elements != null)
			{
			for (int i = 0; i < elements.length; i++)
			{
				ServiceElement el = elements[i];
				if (el.isSelected())
				{
					return el.getHost();
				}
			}
			}
		}
		return _host;
	}
	
	public void childChanged(ServiceElement element)
	{
		_childChanged = true;
		if (_parent != null)
		{
			_parent.childChanged(element);
		}
	}
	
	public void setParent(ServiceElement parent)
	{
		_parent = parent;
	}
	
	public ServiceElement getParent()
	{
		return _parent;
	}
	
	public void setSelected(boolean flag)
	{
		_isSelected = flag;
	}

	public boolean isSelected()
	{
		return _isSelected;
	}
	
	public String toString()
	{
		return getName();
	}

	public abstract boolean hasProperties();
	public abstract PropertyElement[] getProperties();
	public abstract String getName();
	public abstract String getDescription();
	public abstract boolean hasChildren();
	public abstract ServiceElement[] getChildren();
	public abstract void commit();
	public abstract void revert();
}