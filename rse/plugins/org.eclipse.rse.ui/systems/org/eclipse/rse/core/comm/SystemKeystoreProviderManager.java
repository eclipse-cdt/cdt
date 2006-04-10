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

package org.eclipse.rse.core.comm;

import java.util.ArrayList;
import java.util.List;

public class SystemKeystoreProviderManager
{
	private static SystemKeystoreProviderManager _instance = new SystemKeystoreProviderManager();
	
	private List _extensions;
	
	private SystemKeystoreProviderManager()
	{		
		_extensions= new ArrayList();
	}
	
	public static SystemKeystoreProviderManager getInstance()
	{
		return _instance;
	}
	
	public void registerKeystoreProvider(ISystemKeystoreProvider ext)
	{
		_extensions.add(ext);
	}
	
	public boolean hasProvider()
	{
		return !_extensions.isEmpty();
	}
	
	public ISystemKeystoreProvider getDefaultProvider()
	{
		if (_extensions.size() > 0)
		{
			return (ISystemKeystoreProvider)_extensions.get(_extensions.size() - 1);
		}		
		return null;
	}
	
	public ISystemKeystoreProvider[] getProviders()
	{
		ISystemKeystoreProvider[] providers = new ISystemKeystoreProvider[_extensions.size()];
		for (int i = 0; i < _extensions.size(); i++)
		{
			providers[i] = (ISystemKeystoreProvider)_extensions.get(i);
		}
		return providers;
	}
}