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

package org.eclipse.rse.internal.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.rse.model.IProperty;
import org.eclipse.rse.model.IPropertySet;
import org.eclipse.rse.model.PropertyType;


public class PropertySet implements IPropertySet 
{
	private String _name;
	private Map _properties;
	
	protected static PropertyType _defaultType =  new PropertyType(IPropertyType.TYPE_STRING);
	
	public PropertySet(IPropertySet propertySet)
	{
		_name = propertySet.getName();
		_properties = new HashMap();
		
		String[] keys = propertySet.getPropertyKeys();
		for (int i =0; i < keys.length; i++)
		{
			String key = keys[i];
			IProperty property = propertySet.getProperty(key);			
			addProperty(key, new Property(property));
		}
	}
	
	public PropertySet(String name)
	{
		_name= name;
		_properties = new  HashMap();
	}
	
	public String getName() 
	{
		return _name;
	}
	
	public String getDescription() 
	{
		return getPropertyValue("description");
	}	
	
	public String[] getPropertyKeys() 
	{
		Set set = _properties.keySet();
		
		return (String[])set.toArray(new String[set.size()]);
	}

	public void setName(String name) 
	{
		_name = name;
	}

	public void setProperties(Map map) 
	{
		_properties = map;
	}
	
	public IProperty addProperty(String key, IProperty property)
	{
		_properties.put(key, property);
		return property;
	}

	public IProperty addProperty(String key,  String value) 
	{
		IProperty property = getProperty(key);
		if (property != null)
		{
			property.setValue(value);
			return property;
		}
		else
		{			
			return addProperty(key, value, _defaultType);
		}
	}
	
	public IProperty addProperty(String key, String value, IPropertyType type) 
	{
		IProperty property = new Property(key, value, type, true);
		return addProperty(key, property);
	}

	public boolean removeProperty(String key) 
	{
		return _properties.remove(key) != null;
	}

	public IProperty getProperty(String key)
	{
		return (IProperty)_properties.get(key);
	}

	public String getPropertyValue(String key)
	{
		IProperty property = getProperty(key);
		if (property != null)
		{
			return property.getValue();
		}
		return null;		
	}
	public IPropertyType getPropertyType(String key)
	{
		IProperty property = getProperty(key);
		if (property != null)
		{
			return property.getType();
		}
		return null;
	}

}