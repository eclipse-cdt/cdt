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

package org.eclipse.rse.subsystems.processes.dstore;

import org.eclipse.rse.connectorservice.dstore.DStoreConnectorService;
import org.eclipse.rse.connectorservice.dstore.DStoreConnectorServiceManager;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.services.dstore.IDStoreService;
import org.eclipse.rse.services.dstore.processes.DStoreProcessService;
import org.eclipse.rse.services.processes.IProcessService;
import org.eclipse.rse.subsystems.processes.core.subsystem.IHostProcessToRemoteProcessAdapter;
import org.eclipse.rse.subsystems.processes.servicesubsystem.ProcessServiceSubSystem;
import org.eclipse.rse.subsystems.processes.servicesubsystem.ProcessServiceSubSystemConfiguration;


public class DStoreProcessSubSystemConfiguration extends ProcessServiceSubSystemConfiguration 
{
	protected IHostProcessToRemoteProcessAdapter _hostProcessAdapter;
	
	public DStoreProcessSubSystemConfiguration() 
	{
		super();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#isFactoryFor(java.lang.Class)
	 */
	public boolean isFactoryFor(Class subSystemType) {
		boolean isFor = ProcessServiceSubSystem.class.equals(subSystemType);
		return isFor;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SubSystemFactory#createSubSystemInternal(org.eclipse.rse.model.IHost)
	 */
	public ISubSystem createSubSystemInternal(IHost host) 
	{
		DStoreConnectorService connectorService = (DStoreConnectorService)getConnectorService(host);
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
	 * @return true if the subsystem supports remote search
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
	 * @see org.eclipse.rse.subsystems.processes.servicesubsystem.IProcessServiceSubSystemFactory#createProcessService(org.eclipse.rse.model.IHost)
	 */
	public IProcessService createProcessService(IHost host) 
	{
		DStoreConnectorService connectorService = (DStoreConnectorService)getConnectorService(host);
		return new DStoreProcessService(connectorService);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.processes.servicesubsystem.IProcessServiceSubSystemFactory#getHostProcessAdapter()
	 */
	public IHostProcessToRemoteProcessAdapter getHostProcessAdapter() 
	{
		if (_hostProcessAdapter == null)
		{
			_hostProcessAdapter =  new DStoreProcessAdapter();
		}
		return _hostProcessAdapter;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.ISubSystemFactory#getConnectorService(org.eclipse.rse.model.IHost)
	 */
	public IConnectorService getConnectorService(IHost host)
	{
		return DStoreConnectorServiceManager.getTheUniversalSystemManager().getConnectorService(host, getServiceImplType());
	}

	public void setConnectorService(IHost host, IConnectorService connectorService)
	{
		DStoreConnectorServiceManager.getTheUniversalSystemManager().setConnectorService(host, getServiceImplType(), connectorService);
	}
	
	public Class getServiceImplType()
	{
		return IDStoreService.class;
	}

}