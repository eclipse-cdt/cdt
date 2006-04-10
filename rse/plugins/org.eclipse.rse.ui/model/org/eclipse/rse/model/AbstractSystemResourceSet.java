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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rse.services.clientserver.messages.SystemMessage;




public class AbstractSystemResourceSet implements ISystemResourceSet
{
	private List _resourceSet;
	private SystemMessage _message;
    private long _byteSize;   
	
	public AbstractSystemResourceSet()
	{
		_resourceSet = new ArrayList();
	}

	public AbstractSystemResourceSet(Object[] set)
	{
		_resourceSet = new ArrayList();
		if (set != null)
		{
			for (int i = 0; i < set.length; i++)
			{
				addResource(set[i]);
			}
		}
	}
	
	public AbstractSystemResourceSet(List set)
	{
		_resourceSet = set;
	}
	
	public int size()
	{
		return _resourceSet.size();
	}
	
	public Object get(String absoluteName)
	{
		for (int i = 0; i < _resourceSet.size(); i++)
		{
			String path = pathFor(_resourceSet.get(i));
			if (path.equals(absoluteName))
			{
				return _resourceSet.get(i);
			}
		}
		return null;
	}
	
	public Object get(int index)
	{
		return _resourceSet.get(index);
	}
	
	public List getResourceSet()
	{
		return _resourceSet;
	}
	
	public void addResource(Object src)
	{
		_resourceSet.add(src);
	}
	
	public void removeResource(Object src)
	{
		_resourceSet.remove(src);
	}
	
	public String pathFor(Object resource)
	{
		return resource.toString();
	}
	
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < _resourceSet.size(); i++)
		{
			Object resource = (Object)_resourceSet.get(i);
			buf.append(pathFor(resource));				
			if (i < _resourceSet.size())
			{
				buf.append(", ");
			}
			buf.append('\n');
		}
		return buf.toString();
	}
	

	public void setMessage(SystemMessage message)
	{
		_message = message;
	}

	public SystemMessage getMessage()
	{
		return _message;
	}
	
	public boolean hasMessage()
	{
		return _message != null;
	}
	
	public boolean hasByteSize()
	{
		return (_byteSize > 0);
	}
	
	public long byteSize()
	{
		return _byteSize;
	}

	public void setByteSize(long byteSize)
	{
		_byteSize = byteSize;
	}
}