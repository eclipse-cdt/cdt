/********************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Martin Oberhuber (Wind River) - [226262] Make IService IAdaptable and add Javadoc
 ********************************************************************************/

package org.eclipse.rse.services.processes;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.rse.services.AbstractService;
import org.eclipse.rse.services.clientserver.messages.SimpleSystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.clientserver.processes.HostProcessFilterImpl;
import org.eclipse.rse.services.clientserver.processes.IHostProcess;
import org.eclipse.rse.services.clientserver.processes.IHostProcessFilter;

/**
 * Abstract base class for RSE Process Service.
 */
public abstract class AbstractProcessService extends AbstractService implements IProcessService
{
	public IHostProcess[] listAllProcesses(String exeNameFilter, String userNameFilter, String stateFilter, IProgressMonitor monitor) throws SystemMessageException
	{
		HostProcessFilterImpl rpfs = new HostProcessFilterImpl();
		rpfs.setName(exeNameFilter);
		rpfs.setUsername(userNameFilter);
		rpfs.setSpecificState(stateFilter);
		return listAllProcesses(rpfs, monitor);
	}

	public IHostProcess[] listAllProcesses(IProgressMonitor monitor) throws SystemMessageException
	{
		HostProcessFilterImpl rpfs = new HostProcessFilterImpl();
		return listAllProcesses(rpfs, monitor);
	}

	/**
	 * Return a single IHostProcess object for the 'init' process with pid 1.
	 *
	 * @param monitor Progress monitor
	 * @return Array with 1 element, the IHostProcess object for the root
	 *         process
	 * @throws SystemMessageException in case anything goes wrong
	 * @see org.eclipse.rse.services.processes.IProcessService#listRootProcesses(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IHostProcess[] listRootProcesses(IProgressMonitor monitor) throws SystemMessageException
	{
		IHostProcess[] roots = new IHostProcess[1];
		roots[0] = getProcess(1, monitor);
		return roots;
	}

	public IHostProcess[] listChildProcesses(long parentPID, IProgressMonitor monitor) throws SystemMessageException
	{
		String pPidString = "" + parentPID; //$NON-NLS-1$
		HostProcessFilterImpl rpfs = new HostProcessFilterImpl();
		rpfs.setPpid(pPidString);

		return listAllProcesses(rpfs, monitor);
	}

	public IHostProcess[] listChildProcesses(long parentPID, IHostProcessFilter filter, IProgressMonitor monitor) throws SystemMessageException
	{
		String pPidString = "" + parentPID; //$NON-NLS-1$
		filter.setPpid(pPidString);

		return listAllProcesses(filter, monitor);
	}

	public IHostProcess getParentProcess(long pid, IProgressMonitor monitor) throws SystemMessageException
	{
		return getProcess(getProcess(pid, monitor).getPPid(), monitor);
	}

	public IHostProcess getProcess(long pid, IProgressMonitor monitor) throws SystemMessageException
	{
		String pidString = "" + pid; //$NON-NLS-1$
		HostProcessFilterImpl rpfs = new HostProcessFilterImpl();
		rpfs.setPid(pidString);

		IHostProcess[] results = listAllProcesses(rpfs, monitor);
		if ((results == null) || (results.length == 0)) return null;
		else return results[0];
	}

	/**
	 * @deprecated This method was removed from IProcessService in RSE 3.0, and
	 *             implementers are not expected to return anything useful. Use
	 *             {@link SimpleSystemMessage} to create system messages
	 *             instead.
	 */
	public SystemMessage getMessage(String messageID)
	{
		return null;
	}
}