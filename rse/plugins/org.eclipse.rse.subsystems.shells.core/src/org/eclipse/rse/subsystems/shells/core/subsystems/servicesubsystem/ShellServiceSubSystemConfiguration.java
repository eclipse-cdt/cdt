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

package org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem;



import java.util.HashMap;
import java.util.Map;

import org.eclipse.rse.internal.subsystems.shells.subsystems.RemoteCmdSubSystemConfiguration;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.services.IService;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IShellService;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem;




public abstract class ShellServiceSubSystemConfiguration extends RemoteCmdSubSystemConfiguration implements IShellServiceSubSystemConfiguration 
{
	private Map _services;
	protected ShellServiceSubSystemConfiguration()
	{
		super();
		_services = new HashMap();
	}

	public final Class getServiceType()
	{
		return IShellService.class;
	}
	
	public final IShellService getShellService(IHost host)
	{
		IShellService service = (IShellService)_services.get(host);
		if (service == null)
		{
			service = createShellService(host);
			_services.put(host, service);
		}
		return service;
	}
	
	public final IService getService(IHost host)
	{
		return getShellService(host);
	}

	public boolean supportsServerLaunchProperties(IHost host)
	{
		return getConnectorService(host).supportsServerLaunchProperties();
	}
	
	public IServiceCommandShell createRemoteCommandShell(IRemoteCmdSubSystem cmdSS, IHostShell hostShell)
	{		
		return new ServiceCommandShell(cmdSS, hostShell);
	}
} 