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

import java.util.Map;

import org.eclipse.rse.internal.model.IPropertyType;


public interface IPropertySet 
{
	public String getName();
	public String getDescription();
	public IProperty getProperty(String key);
	public String getPropertyValue(String key);
	public String[] getPropertyKeys();
	public IPropertyType getPropertyType(String key);
	
	public void setName(String name);
	public void setProperties(Map map);
	
	public IProperty addProperty(String key, String value);
	public IProperty addProperty(String key, String value, IPropertyType type);
	public boolean removeProperty(String key);

}