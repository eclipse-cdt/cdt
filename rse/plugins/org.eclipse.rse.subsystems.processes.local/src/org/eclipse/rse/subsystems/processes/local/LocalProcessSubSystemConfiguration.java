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

package org.eclipse.rse.subsystems.processes.local;


import org.eclipse.rse.connectorservice.local.LocalConnectorService;
import org.eclipse.rse.connectorservice.local.LocalConnectorServiceManager;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.services.local.ILocalService;
import org.eclipse.rse.services.local.processes.LocalProcessService;
import org.eclipse.rse.services.processes.IProcessService;
import org.eclipse.rse.subsystems.processes.core.subsystem.IHostProcessToRemoteProcessAdapter;
import org.eclipse.rse.subsystems.processes.servicesubsystem.ProcessServiceSubSystem;
import org.eclipse.rse.subsystems.processes.servicesubsystem.ProcessServiceSubSystemConfiguration;


public class LocalProcessSubSystemConfiguration extends ProcessServiceSubSystemConfiguration 
{
	public LocalProcessSubSystemConfiguration()
	{
		super();
	}
	protected IHostProcessToRemoteProcessAdapter _hostProcessAdapter;

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#isFactoryFor(java.lang.Class)
	 */
	public boolean isFactoryFor(Class subSystemType) {
		boolean isFor = ProcessServiceSubSystem.class.equals(subSystemType);
		return isFor;
	}
	
	/**
     * Instantiate and return an instance of OUR subystem. Do not populate it yet though!
     * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#createSubSystemInternal(IHost)
     */
    public ISubSystem createSubSystemInternal(IHost host)
    {
		LocalConnectorService connectorService = (LocalConnectorService)getConnectorService(host);
		ISubSystem subsys = new ProcessServiceSubSystem(host, connectorService, getProcessService(host), getHostProcessAdapter());
		return subsys;
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#supportsFileTypes()
	 */
	public boolean supportsFileTypes() {
		return false;
	}

	/**
	 * @return
	 */
	public boolean supportsSearch() {
		return false;
	}

	/**
	 * @return
	 */
	public boolean supportsEnvironmentVariablesPropertyPage() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.processes.core.subsystem.impl.RemoteProcessSubSystemConfiguration#supportsFilters()
	 */
	public boolean supportsFilters() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.ISubSystemConfiguration#getConnectorService(org.eclipse.rse.model.IHost)
	 */
	public IConnectorService getConnectorService(IHost host)
	{
		return LocalConnectorServiceManager.getTheLocalSystemManager().getConnectorService(host, getServiceImplType());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.processes.servicesubsystem.IProcessServiceSubSystemConfiguration#createProcessService(org.eclipse.rse.model.IHost)
	 */
	public IProcessService createProcessService(IHost host)
	{
		//LocalConnectorService connectorService = (LocalConnectorService)getConnectorService(host);
		return new LocalProcessService();
	}  
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.processes.servicesubsystem.IProcessServiceSubSystemConfiguration#getHostProcessAdapter()
	 */
	public IHostProcessToRemoteProcessAdapter getHostProcessAdapter()
	{
		if (_hostProcessAdapter == null)
		{
			_hostProcessAdapter =  new LocalProcessAdapter();
		}
		return _hostProcessAdapter;
	}
	
	public void setConnectorService(IHost host, IConnectorService connectorService)
	{
		LocalConnectorServiceManager.getTheLocalSystemManager().setConnectorService(host, getServiceImplType(), connectorService);
	}
	
	public Class getServiceImplType()
	{
		return ILocalService.class;
	}
}