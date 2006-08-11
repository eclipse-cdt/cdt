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

import org.eclipse.rse.core.model.IProperty;
import org.eclipse.rse.core.model.IPropertyType;
import org.eclipse.rse.internal.model.Property;
import org.eclipse.rse.ui.view.SystemComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;



public class PropertyElement 
{
	private IPropertyDescriptor _propertyDescriptor;
	private IProperty _property;
	private IProperty _originalProperty;
	private String _value;
	
	public PropertyElement(ServiceElement parent, IProperty property)
	{
		_property = property;
		_originalProperty = new Property(_property);
		_value = _property.getValue();
	}
	
	public IPropertyType getType()
	{
		return _property.getType();
	}
	
	public String getKey()
	{
		return _property.getKey();
	}
	
	public String getName()
	{
		return getLabel();
	}
	
	public String getLabel()
	{
		return _property.getLabel();
	}
	
	public String getValue()
	{
		return _value;
	}
	
	
	public void setValue(String value)
	{
		_value = value;
		_property.setValue(_value);
	}
	
	public IPropertyDescriptor getPropertyDescriptor()
	{
		if (_propertyDescriptor == null)
		{
			switch (_property.getType().getType())
			{
			case IPropertyType.TYPE_INTEGER:
				_propertyDescriptor = new TextPropertyDescriptor(getKey(), getLabel());
				break;
			case IPropertyType.TYPE_ENUM:		
				SystemComboBoxPropertyDescriptor comboDescriptor = new SystemComboBoxPropertyDescriptor(getKey(), getLabel(), _property.getType().getEnumValues());
				_propertyDescriptor = comboDescriptor;
				break;
			case IPropertyType.TYPE_STRING:
			default:
				_propertyDescriptor = new TextPropertyDescriptor(getKey(), getLabel());
			break;
			}						
		}
		return _propertyDescriptor;
	}
	
	public void commit()
	{
		/* already committed
		 if (_value != _property.getValue())
		 {
			 _property.setValue(_value);
		 }
		 */
		
	}

	public void revert()
	{
		if (_originalProperty.getValue() != _property.getValue())
		{
			_property.setValue(_originalProperty.getValue());
		}
	}
}