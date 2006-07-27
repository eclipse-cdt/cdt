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

package org.eclipse.rse.subsystems.shells.dstore;

import org.eclipse.dstore.core.model.IDataStoreProvider;
import org.eclipse.rse.connectorservice.dstore.DStoreConnectorService;
import org.eclipse.rse.connectorservice.dstore.DStoreConnectorServiceManager;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.model.Host;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.services.dstore.IDStoreService;
import org.eclipse.rse.services.dstore.shells.DStoreShellService;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IShellService;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem.IServiceCommandShell;
import org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem.ShellServiceSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem.ShellServiceSubSystemConfiguration;
import org.eclipse.rse.subsystems.shells.dstore.model.DStoreServiceCommandShell;

/**
 * Provides a factory for generating instances of the class
 * ShellServiceSubSystem.
 */
public class DStoreShellSubSystemConfiguration extends ShellServiceSubSystemConfiguration
{
	protected boolean _isWindows;
	public DStoreShellSubSystemConfiguration() 
	{
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#isFactoryFor(java.lang.Class)
	 */
	public boolean isFactoryFor(Class subSystemType) {
		boolean isFor = ShellServiceSubSystem.class.equals(subSystemType);
		return isFor;
	}


   
	/**
	 * Instantiate and return an instance of OUR subystem. 
	 * Do not populate it yet though!
	 * @see org.eclipse.rse.core.subsystems.impl.SubSystemFactoryImpl#createSubSystemInternal(Host)
	 */
	public ISubSystem createSubSystemInternal(IHost host) 
	{
		DStoreConnectorService connectorService = (DStoreConnectorService)getConnectorService(host);
		ISubSystem subsys = new ShellServiceSubSystem(host, connectorService, new DStoreShellService(connectorService));
		return subsys;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#supportsFilters()
	 */
	public boolean supportsFilters() {
		return false;
	}

	public IConnectorService getConnectorService(IHost host)
	{
		return DStoreConnectorServiceManager.getTheUniversalSystemManager().getConnectorService(host, getServiceImplType());
	}
	
	public void setConnectorService(IHost host, IConnectorService connectorService)
	{
		DStoreConnectorServiceManager.getTheUniversalSystemManager().setConnectorService(host, getServiceImplType(), connectorService);
	}
	
	public IShellService createShellService(IHost host)
	{
		return new DStoreShellService((IDataStoreProvider)getConnectorService(host));
	}

	public boolean supportsCommands()
	{	
		return true;
	}
	public boolean canRunCommand()
	{
		return true;
	}
	
	public IServiceCommandShell createRemoteCommandShell(IRemoteCmdSubSystem cmdSS, IHostShell hostShell)
	{		
		return new DStoreServiceCommandShell(cmdSS, hostShell);
	}
	
	public Class getServiceImplType()
	{
		return IDStoreService.class;
	}
}