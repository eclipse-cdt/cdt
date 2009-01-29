/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - [182454] improve getAbsoluteName() documentation
 * Martin Oberhuber (Wind River) - [186128][refactoring] Move IProgressMonitor last in public base classes
 * Martin Oberhuber (Wind River) - [218304] Improve deferred adapter loading
 * David McKnight   (IBM)        - [262930] Remote System Details view not restoring filter memento input
 *******************************************************************************/

package org.eclipse.rse.subsystems.processes.core.subsystem.impl;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.CommunicationsEvent;
import org.eclipse.rse.core.subsystems.ICommunicationsListener;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.SubSystem;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.clientserver.processes.HostProcessFilterImpl;
import org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcess;
import org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcessContext;
import org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcessSubSystem;
import org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcessSubSystemConfiguration;
import org.eclipse.rse.ui.SystemBasePlugin;

/**
 * Default implementation of the IRemoteProcessSubSystem interface.
 * <p>
 * Some of the methods are simply convenience methods - these are
 * implemented here, whereas the real work takes place in the
 * ProcessServiceSubSystem.
 * </p>
 */
public abstract class RemoteProcessSubSystemImpl extends SubSystem implements
		IRemoteProcessSubSystem, ICommunicationsListener
{

	public RemoteProcessSubSystemImpl(IHost host, IConnectorService connectorService)
	{
		super(host, connectorService);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcessSubSystem#getParentRemoteProcessSubSystemConfiguration()
	 */
	public IRemoteProcessSubSystemConfiguration getParentRemoteProcessSubSystemConfiguration()
	{
		return (IRemoteProcessSubSystemConfiguration) super.getSubSystemConfiguration();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcessSubSystem#isCaseSensitive()
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
		super.initializeSubSystem(monitor);
		// load UI plugin for adapters right after successful connect
		Platform.getAdapterManager().loadAdapter(new RemoteProcessImpl(null, null), "org.eclipse.rse.ui.view.ISystemViewElementAdapter"); //$NON-NLS-1$
		getConnectorService().addCommunicationsListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SubSystem#uninitializeSubSystem(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void uninitializeSubSystem(IProgressMonitor monitor)
	{
		getConnectorService().removeCommunicationsListener(this);
		super.uninitializeSubSystem(monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcessSubSystem#getParentProcess(org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcess)
	 */
	public IRemoteProcess getParentProcess(IRemoteProcess process)
	{
		return process.getParentRemoteProcess();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SubSystem#internalResolveFilterString(org.eclipse.core.runtime.IProgressMonitor, java.lang.String)
	 */
	protected Object[] internalResolveFilterString(String filterString, IProgressMonitor monitor)
	throws InvocationTargetException,
       InterruptedException
      {
		if (!isConnected()) {
			return null;
		}

		HostProcessFilterImpl rpf = new HostProcessFilterImpl(filterString);
		IRemoteProcessContext context = new RemoteProcessContext(this, null, rpf);
		IRemoteProcess[] ps = null;
		try
		{
			ps = listAllProcesses(rpf, context, monitor);
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
	public IRemoteProcess[] listRoots(IRemoteProcessContext context, IProgressMonitor monitor)
	{
		IRemoteProcess[] roots = new IRemoteProcess[1];
		try
		{
			roots[0] = getRemoteProcessObject(1);
		}
		catch (SystemMessageException e)
		{
			SystemBasePlugin.logError("Exception resolving roots", e); //$NON-NLS-1$
		}
		return roots;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.ICommunicationsListener#isPassiveCommunicationsListener()
	 */
	public boolean isPassiveCommunicationsListener()
	{
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see SubSystem#getObjectWithAbsoluteName(String, IProgressMonitor)
	 */
	public Object getObjectWithAbsoluteName(String key, IProgressMonitor monitor) throws Exception
	{
		// first attempt getting filter
		Object filterRef = super.getObjectWithAbsoluteName(key, monitor);
		if (filterRef != null) {
			return filterRef;
		}
		
		try
		{
			long pid = Long.parseLong(key);
			return getRemoteProcessObject(pid);
		}
		catch (NumberFormatException e)
		{
			return super.getObjectWithAbsoluteName(key, monitor);
		}
	}
}