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

package org.eclipse.rse.services.processes;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.clientserver.processes.HostProcessFilterImpl;
import org.eclipse.rse.services.clientserver.processes.IHostProcess;
import org.eclipse.rse.services.clientserver.processes.IHostProcessFilter;

public abstract class AbstractProcessService implements IProcessService 
{
	public IHostProcess[] listAllProcesses(IProgressMonitor monitor, String exeNameFilter, String userNameFilter, String stateFilter) throws SystemMessageException 
	{
		HostProcessFilterImpl rpfs = new HostProcessFilterImpl();
		rpfs.setName(exeNameFilter);
		rpfs.setUsername(userNameFilter);
		rpfs.setSpecificState(stateFilter);
		return listAllProcesses(monitor, rpfs);
	}

	public IHostProcess[] listAllProcesses(IProgressMonitor monitor) throws SystemMessageException 
	{
		HostProcessFilterImpl rpfs = new HostProcessFilterImpl();
		return listAllProcesses(monitor, rpfs);
	}

	/**
	 * At this point there is only one root process, the 'init' process with pid 1
	 */
	public IHostProcess[] listRootProcesses(IProgressMonitor monitor) throws SystemMessageException 
	{
		IHostProcess[] roots = new IHostProcess[1];
		roots[0] = getProcess(monitor, 1);
		return roots;
	}

	public IHostProcess[] listChildProcesses(IProgressMonitor monitor, long parentPID) throws SystemMessageException 
	{
		String pPidString = "" + parentPID;
		HostProcessFilterImpl rpfs = new HostProcessFilterImpl();
		rpfs.setPpid(pPidString);
		
		return listAllProcesses(monitor, rpfs);
	}
	
	public IHostProcess[] listChildProcesses(IProgressMonitor monitor, long parentPID, IHostProcessFilter filter) throws SystemMessageException 
	{
		String pPidString = "" + parentPID;
		filter.setPpid(pPidString);
		
		return listAllProcesses(monitor, filter);
	}

	public IHostProcess getParentProcess(IProgressMonitor monitor, long PID) throws SystemMessageException 
	{
		return getProcess(monitor, getProcess(monitor, PID).getPPid());
	}

	public IHostProcess getProcess(IProgressMonitor monitor, long PID) throws SystemMessageException 
	{
		String pidString = "" + PID;
		HostProcessFilterImpl rpfs = new HostProcessFilterImpl();
		rpfs.setPid(pidString);
		
		IHostProcess[] results = listAllProcesses(monitor, rpfs);
		if (results == null) return null;
		else return results[0];
	}
	
	public SystemMessage getMessage(String messageID)
	{
		return null;
	}
}