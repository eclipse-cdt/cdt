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

package org.eclipse.rse.core.model;


public class Property implements IProperty
{
	protected String _name;
	protected String _label;
	protected String _value;
	protected IPropertyType _type;
	protected boolean _isEnabled;
	
	
	public Property(IProperty property)
	{
		_name = property.getKey();
		_label = property.getLabel();
		_value = property.getValue();
		_type = property.getType();
		_isEnabled = property.isEnabled();
	}
	
	public Property(String name, String value, IPropertyType type, boolean isEnabled)
	{
		_name = name;
		_value = value;
		_type = type;
		_isEnabled = isEnabled;
	}
	
	public void setLabel(String label)
	{
		_label = label;
	}
	
	public String getLabel()
	{
		if (_label == null)
		{
			return _name;
		}
		return _label;
	}
	
	public String getKey()
	{
		return _name;
	}
	
	public String getValue()
	{
		return _value;
	}

	public IPropertyType getType()
	{
		return _type;
	}

	public boolean isEnabled()
	{
		return _isEnabled;
	}
	
	public void setValue(String value)
	{
		_value = value;
	}
	
	public void setType(IPropertyType type)
	{
		_type = type;
	}
	
	public void setEnabled(boolean flag)
	{
		_isEnabled = flag;
	}

}