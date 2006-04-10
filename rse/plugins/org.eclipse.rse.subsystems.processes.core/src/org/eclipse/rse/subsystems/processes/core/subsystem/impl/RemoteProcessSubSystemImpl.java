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

package org.eclipse.rse.subsystems.processes.core.subsystem.impl;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.subsystems.CommunicationsEvent;
import org.eclipse.rse.core.subsystems.ICommunicationsListener;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.SubSystem;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.clientserver.processes.HostProcessFilterImpl;
import org.eclipse.rse.services.clientserver.processes.IHostProcessFilter;
import org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcess;
import org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcessContext;
import org.eclipse.rse.subsystems.processes.core.subsystem.RemoteProcessSubSystem;
import org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcessSubSystemConfiguration;

/**
 * The implementation of the RemoteProcessSubSystem interface. 
 * Some of the methods are simply convenience methods - these are
 * implemented here, whereas the real work takes place in the
 * ProcessServiceSubSystem. 
 * @author mjberger
 *
 */
/**
 * @author mjberger
 *
 */
public abstract class RemoteProcessSubSystemImpl extends SubSystem implements
		RemoteProcessSubSystem, ICommunicationsListener
{
	
	public RemoteProcessSubSystemImpl(IHost host, IConnectorService connectorService)
	{
		super(host, connectorService);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.processes.core.subsystem.RemoteProcessSubSystem#getParentRemoteProcessSubSystemFactory()
	 */
	public IRemoteProcessSubSystemConfiguration getParentRemoteProcessSubSystemFactory()
	{
		return (IRemoteProcessSubSystemConfiguration) super.getSubSystemConfiguration();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.processes.core.subsystem.RemoteProcessSubSystem#isCaseSensitive()
	 */
	public boolean isCaseSensitive()
	{
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.ICommunicationsListener#communicationsStateChange(org.eclipse.rse.core.subsystems.CommunicationsEvent)
	 */
	public void communicationsStateChange(CommunicationsEvent e)
	{
		switch (e.getState())
		{
			case CommunicationsEvent.BEFORE_CONNECT :
				break;
			case CommunicationsEvent.AFTER_DISCONNECT :	
				getConnectorService().removeCommunicationsListener(this);
		
				break;

			case CommunicationsEvent.BEFORE_DISCONNECT :
			case CommunicationsEvent.CONNECTION_ERROR :
				break;
			default :
				break;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SubSystem#initializeSubSystem(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void initializeSubSystem(IProgressMonitor monitor)
	{
		getConnectorService().addCommunicationsListener(this);			
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.processes.core.subsystem.RemoteProcessSubSystem#getParentProcess(org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcess)
	 */
	public IRemoteProcess getParentProcess(IRemoteProcess process)
	{
		return process.getParentRemoteProcess();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SubSystem#internalResolveFilterString(org.eclipse.core.runtime.IProgressMonitor, java.lang.String)
	 */
	protected Object[] internalResolveFilterString(IProgressMonitor monitor, String filterString)
	throws InvocationTargetException,
       InterruptedException
      {
		HostProcessFilterImpl rpf = new HostProcessFilterImpl(filterString);
		IRemoteProcessContext context = new RemoteProcessContext(this, null, rpf);
		IRemoteProcess[] ps = null;
		try
		{
			ps = listAllProcesses(rpf, context);
		}
		catch (SystemMessageException e)
		{
			displayAsyncMsg(e);
		}
		return  ps;		
      }
	
	/**
	 * At this point there is only one root process, the 'init' process with pid 1
	 */
	public IRemoteProcess[] listRoots(IRemoteProcessContext context)
	{
		IRemoteProcess[] roots = new IRemoteProcess[1];
		try
		{
			roots[0] = getRemoteProcessObject(1);
		}
		catch (SystemMessageException e)
		{
			SystemBasePlugin.logError("Exception resolving roots", e);
		}
		return roots;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.processes.core.subsystem.RemoteProcessSubSystem#listAllProcesses(org.eclipse.rse.services.clientserver.processes.IHostProcessFilter, org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcessContext)
	 */
	public abstract IRemoteProcess[] listAllProcesses(
			IHostProcessFilter processNameFilter,
			IRemoteProcessContext context) throws InterruptedException,
			SystemMessageException;

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.processes.core.subsystem.RemoteProcessSubSystem#getRemoteProcessObject(long)
	 */
	public abstract IRemoteProcess getRemoteProcessObject(long pid)
			throws SystemMessageException;
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.processes.core.subsystem.RemoteProcessSubSystem#kill(org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcess, java.lang.String)
	 */
	public abstract boolean kill(IRemoteProcess process, String signal)
			throws SystemMessageException;

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.processes.core.subsystem.RemoteProcessSubSystem#getSignalTypes()
	 */
	public abstract String[] getSignalTypes() throws SystemMessageException;



	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.ICommunicationsListener#isPassiveCommunicationsListener()
	 */
	public boolean isPassiveCommunicationsListener()
	{
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SubSystem#getObjectWithAbsoluteName(java.lang.String)
	 */
	public Object getObjectWithAbsoluteName(String key) throws Exception
	{
		try
		{
			long pid = Long.parseLong(key);
			return getRemoteProcessObject(pid);
		}
		catch (NumberFormatException e)
		{
			return super.getObjectWithAbsoluteName(key);
		}
	}
}