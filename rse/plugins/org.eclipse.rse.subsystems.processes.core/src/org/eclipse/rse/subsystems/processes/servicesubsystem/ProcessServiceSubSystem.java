/********************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [182454] improve getAbsoluteName() documentation
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * David Dykstal (IBM) - [197036] refactored switch configuration
 * David Dykstal (IBM) - [217556] remove service subsystem types
 * David McKnight (IBM) - [220524] internalSwitchServiceSubSystemConfiguration -> internalSwitchSubSystemConfiguration
 * Martin Oberhuber (Wind River) - [218304] Improve deferred adapter loading
 * David McKnight   (IBM)        - [272882] [api] Handle exceptions in IService.initService()
 * David McKnight   (IBM)        - [302478] ProcessServiceSubSystem.getRemoteProcessObject() doesn't check for null
 ********************************************************************************/

package org.eclipse.rse.subsystems.processes.servicesubsystem;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
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
		checkIsConnected(new NullProgressMonitor());
		HostProcessFilterImpl rpfs = new HostProcessFilterImpl();
		rpfs.setPid("" + pid); //$NON-NLS-1$
		IRemoteProcessContext context = new RemoteProcessContext(this, null, rpfs);
		IHostProcess process = getProcessService().getProcess(pid, new NullProgressMonitor());
		if (process != null){
			return getHostProcessToRemoteProcessAdapter().convertToRemoteProcess(context, null, process);
		}
		else {
			return null;
		}
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
		checkIsConnected(new NullProgressMonitor());
		return getProcessService().kill(process.getPid(), signal, null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.processes.core.subsystem.impl.RemoteProcessSubSystemImpl#listAllProcesses(org.eclipse.rse.services.clientserver.processes.IHostProcessFilter, org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcessContext)
	 */
	public IRemoteProcess[] listAllProcesses(IHostProcessFilter processFilter, IRemoteProcessContext context, IProgressMonitor monitor) throws InterruptedException, SystemMessageException
	{
		checkIsConnected(monitor);
		IHostProcess[] processes = getProcessService().listAllProcesses(processFilter, monitor);
		return getHostProcessToRemoteProcessAdapter().convertToRemoteProcesses(context, null, processes);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.processes.core.subsystem.RemoteProcessSubSystem#listChildProcesses(org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcess, org.eclipse.rse.services.clientserver.processes.IHostProcessFilter, org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcessContext)
	 */
	public IRemoteProcess[] listChildProcesses(IRemoteProcess parent, IHostProcessFilter processFilter, IRemoteProcessContext context, IProgressMonitor monitor) throws SystemMessageException
	{
		checkIsConnected(monitor);
		IHostProcess[] processes = getProcessService().listChildProcesses(parent.getPid(), processFilter, monitor);
		return getHostProcessToRemoteProcessAdapter().convertToRemoteProcesses(context, parent, processes);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.processes.core.subsystem.RemoteProcessSubSystem#listChildProcesses(org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcess, org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcessContext)
	 */
	public IRemoteProcess[] listChildProcesses(IRemoteProcess parent, IRemoteProcessContext context, IProgressMonitor monitor) throws SystemMessageException
	{
		checkIsConnected(monitor);
		IHostProcess[] processes = getProcessService().listChildProcesses(parent.getPid(), monitor);
		return getHostProcessToRemoteProcessAdapter().convertToRemoteProcesses(context, parent, processes);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SubSystem#internalSwitchSubSystemConfiguration(org.eclipse.rse.core.subsystems.ISubSystemConfiguration)
	 */
	protected void internalSwitchSubSystemConfiguration(ISubSystemConfiguration configuration)
	{
			IProcessServiceSubSystemConfiguration config = (IProcessServiceSubSystemConfiguration) configuration;
			IHost host = getHost();
			setProcessService(config.getProcessService(host));
			setHostProcessToRemoteProcessAdapter(config.getHostProcessAdapter());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SubSystem#canSwitchTo(org.eclipse.rse.core.subsystems.ISubSystemConfiguration)
	 */
	public boolean canSwitchTo(ISubSystemConfiguration configuration) {
		return configuration instanceof IProcessServiceSubSystemConfiguration;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.servicesubsystem.ISubSystem#getServiceType()
	 */
	public Class getServiceType()
	{
		return IProcessService.class;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.processes.core.subsystem.impl.RemoteProcessSubSystemImpl#initializeSubSystem(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void initializeSubSystem(IProgressMonitor monitor) throws SystemMessageException
	{
		super.initializeSubSystem(monitor);
		getProcessService().initService(monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.processes.core.subsystem.impl.RemoteProcessSubSystemImpl#initializeSubSystem(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void uninitializeSubSystem(IProgressMonitor monitor)
	{
		getProcessService().uninitService(monitor);
		super.uninitializeSubSystem(monitor);
	}

}