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

import org.eclipse.rse.core.model.IProperty;
import org.eclipse.rse.core.model.IPropertySet;
import org.eclipse.rse.core.model.IPropertyType;
import org.eclipse.rse.internal.model.PropertySet;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;


public class PropertySetServiceElement extends ServiceElement 
implements IPropertySource
{
	protected PropertyElement[] _properties;
	protected IPropertySet _propertySet;
	protected IPropertySet _originalPropertySet;
	
	public PropertySetServiceElement(IHost host, ServiceElement parent, IPropertySet propertySet)
	{
		super(host, parent);
		_propertySet = propertySet;
		_originalPropertySet = new PropertySet(_propertySet);
	}
	
	public IPropertySet getOriginalProperySet()
	{
		return _originalPropertySet;
	}
	
	public IPropertySet getPropertySet()
	{
		return _propertySet;
	}
	
	public Image getImage()
	{
		return RSEUIPlugin.getDefault().getImage(ISystemIconConstants.ICON_SYSTEM_PROPERTIES_ID);
	}
	
	public String getName()
	{
		return _propertySet.getName();
	}
	
	public String getDescription()
	{
		return _propertySet.getDescription();
	}

	public boolean hasChildren()
	{
		return false;
	}
	
	public ServiceElement[] getChildren()
	{
		return null;
	}
	
	public void refreshProperties()
	{
		_properties = null;
		
	}
	
	public boolean hasProperties()
	{
		return _propertySet.getPropertyKeys().length > 0;
	}

	public PropertyElement[] getProperties()
	{
		if (_properties == null)
		{
			String[] keys = _propertySet.getPropertyKeys();
			List enabledProperties = new ArrayList();
			
			for (int i = 0; i < keys.length; i++)
			{
				String key = keys[i];
				IProperty property = _propertySet.getProperty(key);				
				if (property.isEnabled())
				{
					enabledProperties.add(new PropertyElement(this, property));				
				}
			}
			_properties =(PropertyElement[])enabledProperties.toArray(new PropertyElement[enabledProperties.size()]);
		}
		return _properties;
	}


	public IPropertyDescriptor[] getPropertyDescriptors()
	{
		PropertyElement[] properties = getProperties();
		IPropertyDescriptor[] descriptors = new IPropertyDescriptor[properties.length];
		for (int i = 0; i < properties.length; i++)
		{
			descriptors[i] = properties[i].getPropertyDescriptor();
		}
		return descriptors;
	}
	
	private PropertyElement getPropertyElement(String id)
	{
		PropertyElement[] elements = getProperties();
		for (int i = 0; i < elements.length; i++)
		{
			PropertyElement element = elements[i];
			if (element.getKey().equals(id))
			{
				return element;
			}
		}
		return null;
	}

	public Object getPropertyValue(Object id)
	{
		return getPropertyElement((String)id).getValue();
	}

	public boolean isPropertySet(Object id)
	{
		return getPropertyElement((String)id) != null;
	}

	public void resetPropertyValue(Object id)
	{
		// update ui object
		PropertyElement element = getPropertyElement((String)id);
		if (element.getKey().equals(id))
		{
			element.setValue(_propertySet.getPropertyValue((String)id));
		}
	}

	public void setPropertyValue(Object id, Object value)
	{	
		PropertyElement element = getPropertyElement((String)id);
		String strValue = null;
		 if (value instanceof Integer)
			{
				Integer intValue = (Integer)value;
				
				IPropertyType type = element.getType();
				if (type.isEnum())
				{
					strValue = type.getEnumValues()[intValue.intValue()];
				}
				else if (type.isInteger())
				{
					strValue = intValue.toString();
				}
			}
		 else
		 {
			 strValue = (String)value;
		 }
		 
	

		if (element.getKey().equals(id))
		{
			element.setValue(strValue);
		}
		
		refreshProperties();
		childChanged(this);
	}

	public Object getEditableValue()
	{
		return this;
	}

	public void commit()
	{
		PropertyElement[] properties = getProperties();
		for (int i = 0; i < properties.length; i++)
		{
			properties[i].commit();
		}
	}
	
	public void revert()
	{
		PropertyElement[] properties = getProperties();
		for (int i = 0; i < properties.length; i++)
		{
			properties[i].revert();
		}
	}	

}