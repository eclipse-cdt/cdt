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

package org.eclipse.rse.subsystems.files.core.servicesubsystem;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rse.model.IHost;
import org.eclipse.rse.services.IService;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.search.ISearchService;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileSubSystemConfiguration;



public abstract class FileServiceSubSystemConfiguration extends RemoteFileSubSystemConfiguration implements IFileServiceSubSystemConfiguration 
{
	private Map _services;
	private Map _searchServices;
	
	protected FileServiceSubSystemConfiguration()
	{
		super();
		_services = new HashMap();
		_searchServices = new HashMap();
	}
	
	public final Class getServiceType()
	{
		return IFileService.class;
	}
	
	public final IService getService(IHost host)
	{
		return getFileService(host);
	}
	
	public final IFileService getFileService(IHost host)
	{
		IFileService service = (IFileService)_services.get(host);
		if (service == null)
		{
			service = createFileService(host);
			_services.put(host, service);
		}
		return service;
	}
	
	public final ISearchService getSearchService(IHost host)
	{
		ISearchService service = (ISearchService)_searchServices.get(host);
		if (service == null)
		{
			service = createSearchService(host);
			_searchServices.put(host, service);
		}
		return service;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileSubSystemConfiguration#supportsServerLaunchProperties(org.eclipse.rse.model.IHost)
	 */
	public final boolean supportsServerLaunchProperties(IHost host) 
	{
		return getConnectorService(host).supportsServerLaunchProperties();
	}

} 