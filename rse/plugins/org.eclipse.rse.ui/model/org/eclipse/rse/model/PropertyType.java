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

package org.eclipse.rse.model;

import org.eclipse.rse.internal.model.IPropertyType;


public  class PropertyType implements IPropertyType
{
	private final String ENUMERATION_STR = "enumeration:";
	
	private int _type = 0;

	
	private String[] _enumValues;
	
	public PropertyType(int type)
	{
		_type = type;
	}
	
	public PropertyType(String typeStr)
	{
		if (typeStr.equals(String.class.toString()))
		{
			setType(TYPE_STRING);
		}
		else if (typeStr.equals(Integer.class.toString()))
		{
			setType(TYPE_INTEGER);
		}
		else if (typeStr.startsWith(ENUMERATION_STR))
		{
			setType(TYPE_ENUM);
			String subString = typeStr.substring(ENUMERATION_STR.length());
			String[] enumValues = subString.split(",");
			setEnumValues(enumValues);
		}
		else if (typeStr.equals(Boolean.class.toString()))
		{
			setType(TYPE_BOOLEAN);
		}
		else
		{
			setType(TYPE_STRING);
		}
	}
	
	public int getType()
	{
		return _type;
	}
	
	public void setType(int type)
	{
		_type = type;
	}
	
	public boolean isString()
	{
		return _type == TYPE_STRING;
	}

	public boolean isInteger()
	{
		return _type == TYPE_INTEGER;
	}

	public boolean isEnum()
	{
		return _type == TYPE_ENUM;
	}
	
	public boolean isBoolean()
	{
		return _type == TYPE_BOOLEAN;
	}

	public void setEnumValues(String[] enumValues)
	{
		_enumValues = enumValues;
	}

	
	public String[] getEnumValues()
	{
		return _enumValues;
	}
	
	public String toString()
	{
		if (isString())
		{
			return String.class.getName();
		}
		else if (isInteger())
		{
			return Integer.class.getName();
		}
		else if (isEnum())
		{
			StringBuffer buf = new StringBuffer();
			buf.append(ENUMERATION_STR);
			String[] enumValues = getEnumValues();
			for (int i = 0; i < enumValues.length; i++)
			{
				buf.append(enumValues[i]);
				if (i + 1 < enumValues.length)
				{
					buf.append(",");
				}				
			}
			return buf.toString();
		}
		else if (isBoolean())
		{
			return Boolean.class.getName();
		}
		return super.toString();
	}
	
}