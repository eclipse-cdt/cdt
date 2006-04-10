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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.IPropertySet;
import org.eclipse.rse.model.IRSEModelObject;


public abstract class RSEModelServiceElement extends ServiceElement
{
	protected IRSEModelObject _modelObject;
	protected IPropertySet[]  _propertySets;
	protected PropertySetServiceElement[] _propertySetElements;
	protected ServiceElement[] _allChildren;
	
	public RSEModelServiceElement(IHost host, ServiceElement parent, IRSEModelObject modelObject)
	{
		super(host, parent);
		_modelObject = modelObject;
		_propertySets = _modelObject.getPropertySets();
	}
	
	
	
	public boolean hasPropertySets()
	{
		return _propertySets != null && _propertySets.length > 0;
	}
	
	public PropertySetServiceElement[] getPropertySets()
	{
		if (_propertySetElements == null)
		{
			_propertySetElements = new PropertySetServiceElement[_propertySets.length];
			for (int i = 0; i < _propertySets.length; i++)
			{
				_propertySetElements[i] = new PropertySetServiceElement(getHost(), this, _propertySets[i]);
			}
		}
		return _propertySetElements;
	}
	
	public String getName()
	{
		return _modelObject.getName();
	}
	
	public String getDescription()
	{
		return _modelObject.getDescription();
	}
	
	public final boolean hasChildren()
	{
		return internalHasChildren() || hasPropertySets();
	}
	
	public final ServiceElement[] getChildren()
	{
		if (_allChildren == null)
		{
			List all = new ArrayList();
				
			if (internalHasChildren())
			{
				ServiceElement[] children = internalGetChildren();
				for (int i = 0; i < children.length; i++)
				{
					all.add(children[i]);
				}
			}
		
			if (hasPropertySets())
			{
				PropertySetServiceElement[] properties = getPropertySets();	
				for (int p = 0; p < properties.length; p++)
				{
					all.add(properties[p]);
				}
			}
			_allChildren = (ServiceElement[])all.toArray(new ServiceElement[all.size()]);
		}
		return _allChildren;
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
		PropertySetServiceElement[] sets = getPropertySets();
		for (int i = 0; i < sets.length; i++)
		{
			sets[i].commit();
		}
	}
	
	public void revert()
	{
		PropertySetServiceElement[] sets = getPropertySets();
		for (int i = 0; i < sets.length; i++)
		{
			IPropertySet newSet = sets[i].getPropertySet();
			IPropertySet originalSet = sets[i].getOriginalProperySet();			
			_modelObject.removePropertySet(newSet.getName());
			_modelObject.addPropertySet(originalSet);
		}
	}
	
	public void refreshProperties()
	{
		PropertySetServiceElement[] propertySets = getPropertySets();
		for (int i = 0; i < propertySets.length; i++)
		{
			propertySets[i].refreshProperties();
		}
	}
	
	protected abstract ServiceElement[] internalGetChildren();
	protected abstract boolean internalHasChildren();


}