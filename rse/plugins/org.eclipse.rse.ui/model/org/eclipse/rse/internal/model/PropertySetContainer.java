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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.rse.model.IPropertySet;
import org.eclipse.rse.model.IPropertySetContainer;



public class PropertySetContainer implements IPropertySetContainer 
{
	private Map _propertySets;
	
	public PropertySetContainer()
	{
		_propertySets = new HashMap();
	}
	
	public IPropertySet[] getPropertySets() 
	{
		List sets = new ArrayList();
		Iterator iter = _propertySets.values().iterator();
		while (iter.hasNext())
		{
			sets.add(iter.next());
		}
		return (IPropertySet[])sets.toArray(new IPropertySet[sets.size()]);
	}

	public IPropertySet getPropertySet(String name) 
	{
		return (IPropertySet)_propertySets.get(name);
	}

	public IPropertySet createPropertySet(String name, String description) 
	{
		IPropertySet newSet = new PropertySet(name);
		newSet.addProperty("description", description);
		_propertySets.put(name, newSet);
		return newSet;
	}
	
	public IPropertySet createPropertySet(String name) 
	{
		IPropertySet newSet = new PropertySet(name);
		_propertySets.put(name, newSet);
		return newSet;
	}

	public boolean addPropertySet(IPropertySet set) 
	{
		_propertySets.put(set.getName(), set);
		return true;
	}
	
	public boolean addPropertySets(IPropertySet[] sets) 
	{
		for (int i = 0; i < sets.length; i++)
		{
			addPropertySet(sets[i]);
		}
		return true;
	}

	public boolean removePropertySet(String name) 
	{
		return _propertySets.remove(name) != null;
	}

}