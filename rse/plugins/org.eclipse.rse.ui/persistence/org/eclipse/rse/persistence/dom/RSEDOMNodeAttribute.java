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


package org.eclipse.rse.persistence.dom;

import java.io.Serializable;

public class RSEDOMNodeAttribute implements Serializable
{
	private String _key;
	private String _value;
	private String _type;
	
	public RSEDOMNodeAttribute(String key, String value, String type)
	{
		_key = key;
		_value = value;
		_type = type;
	}
	
	public RSEDOMNodeAttribute(String key, String value)
	{
		_key = key;
		_value = value;
		_type = null;
	}
	

	public String getKey()
	{
		return _key;
	}
	
	public String getValue()
	{
		return _value;
	}
	
	public String getType()
	{
		return _type;
	}
}