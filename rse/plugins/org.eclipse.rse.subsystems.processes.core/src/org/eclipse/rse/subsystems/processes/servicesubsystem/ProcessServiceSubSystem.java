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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.servicesubsystem.IServiceSubSystemConfiguration;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.clientserver.processes.HostProcessFilterImpl;
import org.eclipse.rse.services.clientserver.processes.IHostProcess;
import org.eclipse.rse.services.clientserver.processes.IHostProcessFilter;
import org.eclipse.rse.services.processes.IProcessService;
import org.eclipse.rse.subsystems.processes.core.subsystem.IHostProcessToRemoteProcessAdapter;
import org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcess;
import org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcessContext;
import org.eclipse.rse.subsystems.processes.core.subsystem.impl.RemoteProcessContext;
import org.eclipse.rse.subsystems.processes.core.subsystem.impl.RemoteProcessSubSystemImpl;

/**
 * The subsystem that, coupled with a ProcessService implementation,
 * can query and kill remote processes on a remote system.
 * @author mjberger
 *
 */
public class ProcessServiceSubSystem extends RemoteProcessSubSystemImpl implements IProcessServiceSubSystem 
{
	protected IProcessService _hostProcessService;
	protected IHostProcessToRemoteProcessAdapter _hostProcessToRemoteProcessAdapter;
	
	public ProcessServiceSubSystem(IHost host, IConnectorService connectorService, IProcessService hostProcessService, IHostProcessToRemoteProcessAdapter adapter)
	{
		super(host, connectorService);
		_hostProcessService = hostProcessService;
		_hostProcessToRemoteProcessAdapter = adapter;
	}
	
	/**
	 * @return the process service associated with this subsystem.
	 */
	public IProcessService getProcessService()
	{
		return _hostProcessService;
	}

	/**
	 * Sets which process service is associated with and used by this subsystem.
	 * @param service The IProcessService with which to associate this subsystem.
	 */
	public void setProcessService(IProcessService service)
	{
		_hostProcessService = service;
	}
	
	/**
	 * @return the associated adapter for converting IHostProcess objects to IRemoteProcess objects
	 */
	public IHostProcessToRemoteProcessAdapter getHostProcessToRemoteProcessAdapter()
	{
		return _hostProcessToRemoteProcessAdapter;
	}
	
	/**
	 * Sets the associated adapter for converting IHostProcess objects to IRemoteProcess objects
	 */
	public void setHostProcessToRemoteProcessAdapter(IHostProcessToRemoteProcessAdapter hostProcessAdapter)
	{
		_hostProcessToRemoteProcessAdapter = hostProcessAdapter;
	}

	/**
	 * Returns the IRemoteProcess representing the process on the remote machine that has a
	 * certain pid.
	 * @param pid The pid of the process to return
	 */
	public IRemoteProcess getRemoteProcessObject(long pid) throws SystemMessageException 
	{
		checkIsConnected();
		HostProcessFilterImpl rpfs = new HostProcessFilterImpl();
		rpfs.setPid("" + pid);
		IRemoteProcessContext context = new RemoteProcessContext(this, null, rpfs);
		IHostProcess process = getProcessService().getProcess(null, pid);
		return getHostProcessToRemoteProcessAdapter().convertToRemoteProcess(context, null, process);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.processes.core.subsystem.impl.RemoteProcessSubSystemImpl#getSignalTypes()
	 */
	public String[] getSignalTypes() throws SystemMessageException 
	{
		return getProcessService().getSignalTypes();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.processes.core.subsystem.impl.RemoteProcessSubSystemImpl#kill(org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcess, java.lang.String)
	 */
	public boolean kill(IRemoteProcess process, String signal) throws SystemMessageException 
	{
		checkIsConnected();
		return getProcessService().kill(null, process.getPid(), signal);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.processes.core.subsystem.impl.RemoteProcessSubSystemImpl#listAllProcesses(org.eclipse.rse.services.clientserver.processes.IHostProcessFilter, org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcessContext)
	 */
	public IRemoteProcess[] listAllProcesses(IHostProcessFilter processFilter, IRemoteProcessContext context) throws InterruptedException, SystemMessageException 
	{
		checkIsConnected();
		IHostProcess[] processes = getProcessService().listAllProcesses(null, processFilter);
		return getHostProcessToRemoteProcessAdapter().convertToRemoteProcesses(context, null, processes);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.processes.core.subsystem.RemoteProcessSubSystem#listChildProcesses(org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcess, org.eclipse.rse.services.clientserver.processes.IHostProcessFilter, org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcessContext)
	 */
	public IRemoteProcess[] listChildProcesses(IRemoteProcess parent, IHostProcessFilter processFilter, IRemoteProcessContext context) throws SystemMessageException
	{
		checkIsConnected();
		IHostProcess[] processes = getProcessService().listChildProcesses(null, parent.getPid(), processFilter);
		return getHostProcessToRemoteProcessAdapter().convertToRemoteProcesses(context, parent, processes);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.processes.core.subsystem.RemoteProcessSubSystem#listChildProcesses(org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcess, org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcessContext)
	 */
	public IRemoteProcess[] listChildProcesses(IRemoteProcess parent, IRemoteProcessContext context) throws SystemMessageException
	{
		checkIsConnected();
		IHostProcess[] processes = getProcessService().listChildProcesses(null, parent.getPid());
		return getHostProcessToRemoteProcessAdapter().convertToRemoteProcesses(context, parent, processes);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.servicesubsystem.IServiceSubSystem#switchServiceFactory(org.eclipse.rse.core.servicesubsystem.IServiceSubSystemConfiguration)
	 */
	public void switchServiceFactory(IServiceSubSystemConfiguration fact) 
	{
		if (fact != getSubSystemConfiguration() && fact instanceof IProcessServiceSubSystemConfiguration)
		{
			IProcessServiceSubSystemConfiguration factory = (IProcessServiceSubSystemConfiguration)fact;
			try
			{
				disconnect(SystemBasePlugin.getActiveWorkbenchShell());
			}
			catch (Exception e)
			{	
			}
			
			IHost host = getHost();
			setSubSystemConfiguration(factory);
			setName(factory.getName());

			IConnectorService oldConnectorService = getConnectorService();			
			oldConnectorService.deregisterSubSystem(this);
			
			IConnectorService newConnectorService = factory.getConnectorService(host);
			setConnectorService(newConnectorService);
			
			oldConnectorService.commit();
			newConnectorService.commit();

			setProcessService(factory.getProcessService(host));
			setHostProcessToRemoteProcessAdapter(factory.getHostProcessAdapter());
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.servicesubsystem.IServiceSubSystem#getServiceType()
	 */
	public Class getServiceType()
	{
		return IProcessService.class;
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.processes.core.subsystem.impl.RemoteProcessSubSystemImpl#initializeSubSystem(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void initializeSubSystem(IProgressMonitor monitor)
	{
		super.initializeSubSystem(monitor);
		getProcessService().initService(monitor);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.processes.core.subsystem.impl.RemoteProcessSubSystemImpl#initializeSubSystem(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void uninitializeSubSystem(IProgressMonitor monitor)
	{
		super.uninitializeSubSystem(monitor);
		getProcessService().uninitService(monitor);
	}
	
}