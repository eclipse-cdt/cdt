/********************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.subsystems.processes.core.subsystem;


import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.clientserver.processes.IHostProcessFilter;

/**
 * The RSE subsystem for Remote Processes
 * @author mjberger
 *
 */
public interface IRemoteProcessSubSystem extends ISubSystem
{

    // ----------------------
    // HELPER METHODS...
    // ----------------------
    
	/**
	 * Return parent subsystem factory, cast to a RemoteProcessSubSystemConfiguration
	 */
	public IRemoteProcessSubSystemConfiguration getParentRemoteProcessSubSystemConfiguration();		
	
	/**
	 * Return true if names are case-sensitive. Used when doing name or type filtering
	 */
	public boolean isCaseSensitive();
	
    // ----------------------
    // PROCESS METHODS...
    // ----------------------

	/**
	 * Return a list of all processes on the remote system.
	 * This version is called directly by users.
	 * @param processNameFilter filter the results according to this object
	 * @param context A context object that will be associated with each returned process
	 * @param monitor the progress monitor
	 * @return the list of all processes running on the host machine that correspond to the filter, 
	 * or null if there are none.
	 */
	public IRemoteProcess[] listAllProcesses(IHostProcessFilter processNameFilter, IRemoteProcessContext context, IProgressMonitor monitor) throws InterruptedException, SystemMessageException;
	
	/**
	 * Returns root processes
	 * @param context A context object that will be associated with each returned process
	 * @param monitor the progress monitor
	 */
	public IRemoteProcess[] listRoots(IRemoteProcessContext context, IProgressMonitor monitor);
	
	/**
	 * Return a list of all remote child processes of the given parent process on the remote system
	 * @param parent The parent process whose children to list
	 * @param context A context object that will be associated with each returned process
	 * @param monitor the progress monitor
	 */
	public IRemoteProcess[] listChildProcesses(IRemoteProcess parent, IRemoteProcessContext context, IProgressMonitor monitor) throws SystemMessageException;
			
	/**
	 * Return a list of remote child processes of the given process, which match the filter.
	 * @param parent The parent process whose children to list
	 * @param processFilter The process filter to subset the list by, or null to return all child processes.
	 * @param context A context object that will be associated with each returned process
	 * @param monitor the progress monitor
	 */
	public IRemoteProcess[] listChildProcesses(IRemoteProcess parent, IHostProcessFilter processFilter, IRemoteProcessContext context, IProgressMonitor monitor) throws SystemMessageException;
			
	/**
	 * Given a process, return its parent process object.
	 * @param process the process to return parent of.
	 */
	public IRemoteProcess getParentProcess(IRemoteProcess process);
	
	/**
	 * Given a pid, return an IRemoteProcess object for it.
	 * @param pid The pid of the desired process
	 */
	public IRemoteProcess getRemoteProcessObject(long pid) throws SystemMessageException;		
	
	/**
	 * Kill the given process. 
	 * 
	 * @param process represents the object to be killed.
	 * @return false if the given process doesn't exist, else true. Throws an exception if anything fails.
	 */
	public boolean kill(IRemoteProcess process, String signal) throws SystemMessageException;	

	/**
	 * Returns a list of the types of signals that can be sent to
	 * a process on the remote system.
	 * @return the signal types, or null if there are none, or they cannot be found.
	 */
	public String[] getSignalTypes() throws SystemMessageException;
}