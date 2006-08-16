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

package org.eclipse.rse.subsystems.shells.local;

import org.eclipse.rse.connectorservice.local.LocalConnectorService;
import org.eclipse.rse.connectorservice.local.LocalConnectorServiceManager;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.services.local.ILocalService;
import org.eclipse.rse.services.local.shells.LocalShellService;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IShellService;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem.IServiceCommandShell;
import org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem.ShellServiceSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem.ShellServiceSubSystemConfiguration;
import org.eclipse.rse.subsystems.shells.local.model.LocalServiceCommandShell;



public class LocalShellSubSystemConfiguration extends ShellServiceSubSystemConfiguration
{
	public LocalShellSubSystemConfiguration() 
	{
		super();
	}

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.internal.subsystems.shells.subsystems.RemoteCmdSubSystemConfiguration#getCommandSeparator()
     */
    public String getCommandSeparator()
    {
    	String os = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$
    	if (os.startsWith("win")) //$NON-NLS-1$
    	{
    		return "&"; //$NON-NLS-1$
    	}
    	else
    	{
    		return ";"; //$NON-NLS-1$
    	}
    	
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
	 */
	public ISubSystem createSubSystemInternal(IHost host) 
	{
		LocalConnectorService connectorService = (LocalConnectorService)getConnectorService(host);
		ISubSystem subsys = new ShellServiceSubSystem(host, connectorService, getShellService(host));
		return subsys;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.rse.internal.subsystems.shells.subsystems.RemoteCmdSubSystemConfiguration#supportsFilters()
	 */
	public boolean supportsFilters() {
		return false;
	}

	public IConnectorService getConnectorService(IHost host)
	{
		return LocalConnectorServiceManager.getTheLocalSystemManager().getConnectorService(host, getServiceImplType());
	}
	
	public IShellService createShellService(IHost host)
	{
		return new LocalShellService();
	}

	public boolean supportsCommands()
	{	
		return true;
	}
	public boolean canRunCommand()
	{
		return true;
	}
	
	public void setConnectorService(IHost host, IConnectorService connectorService)
	{
		LocalConnectorServiceManager.getTheLocalSystemManager().setConnectorService(host, getServiceImplType(), connectorService);
	}
	
	public Class getServiceImplType()
	{
		return ILocalService.class;
	}
	
	public IServiceCommandShell createRemoteCommandShell(IRemoteCmdSubSystem cmdSS, IHostShell hostShell)
	{		
		return new LocalServiceCommandShell(cmdSS, hostShell);
	}
	
}