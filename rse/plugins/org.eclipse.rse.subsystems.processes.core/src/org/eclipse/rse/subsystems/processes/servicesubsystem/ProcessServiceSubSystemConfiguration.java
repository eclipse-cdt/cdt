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

package org.eclipse.rse.subsystems.processes.servicesubsystem;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rse.model.IHost;
import org.eclipse.rse.services.IService;
import org.eclipse.rse.services.processes.IProcessService;
import org.eclipse.rse.subsystems.processes.core.subsystem.impl.RemoteProcessSubSystemConfiguration;

/**
 * Factory for creating the ProcessServiceSubSystem and for getting the associated
 * service and adapter.
 * @author mjberger
 *
 */
public abstract class ProcessServiceSubSystemConfiguration extends RemoteProcessSubSystemConfiguration implements IProcessServiceSubSystemConfiguration 
{

	protected Map _services;
	
	protected ProcessServiceSubSystemConfiguration()
	{
		super();
		_services = new HashMap();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.processes.servicesubsystem.IProcessServiceSubSystemConfiguration#getProcessService(org.eclipse.rse.model.IHost)
	 */
	public IProcessService getProcessService(IHost host) 
	{
		IProcessService service = (IProcessService)_services.get(host);
		if (service == null)
		{
			service = createProcessService(host);
			_services.put(host, service);
		}
		return service;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.servicesubsystem.IServiceSubSystemConfiguration#getServiceType()
	 */
	public final Class getServiceType()
	{
		return IProcessService.class;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#supportsServerLaunchProperties(org.eclipse.rse.model.IHost)
	 */
	public boolean supportsServerLaunchProperties(IHost host) 
	{
		return getConnectorService(host).supportsServerLaunchProperties();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.servicesubsystem.IServiceSubSystemConfiguration#getService(org.eclipse.rse.model.IHost)
	 */
	public IService getService(IHost host)
	{
		return getProcessService(host);
	}
}