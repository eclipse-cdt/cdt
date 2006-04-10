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
import org.eclipse.rse.services.IService;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.clientserver.processes.IHostProcess;
import org.eclipse.rse.services.clientserver.processes.IHostProcessFilter;

/**
 * An IProcessService is an abstraction of a process service that runs over some sort of connection.
 * It can be shared among multiple instances of a subsystem. Each
 * subsystem is currently responsible for layering an abstraction over whatever it 
 * wants to construct as a service.
 * <p>
 * Implementers of this interface will have to either be instantiated, initialized, or
 * somehow derive a connection as part of its state.
 */
public interface IProcessService extends IService
{
	/**
	 * Return a list of all processes on the remote system.
	 * @param monitor A progress monitor to which progress will be reported
	 */
	public IHostProcess[] listAllProcesses(IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Return a list of all processes on the remote system.
	 * @param monitor A progress monitor to which progress will be reported
	 * @param filter An object to filter results by
	 */
	public IHostProcess[] listAllProcesses(IProgressMonitor monitor, IHostProcessFilter filter)  throws SystemMessageException;

	/**
	 * Return a list of all processes on the remote system.
	 * @param monitor A progress monitor to which progress will be reported
	 * @param exeNameFilter The executable name to filter results by, or null if no exeName filtering
	 * @param userNameFilter The user name to filter results by, or null if no userName filtering
	 * @param stateFilter The state code to filter results by, or null if no state filtering
	 */
	public IHostProcess[] listAllProcesses(IProgressMonitor monitor, String exeNameFilter, String userNameFilter, String stateFilter)  throws SystemMessageException;
	
	/**
	 * Returns root processes on the remote system
	 * @param monitor A progress monitor to which progress will be reported
	 */
	public IHostProcess[] listRootProcesses(IProgressMonitor monitor) throws SystemMessageException;
	
	/**
	 * Return a list of all remote child processes of the given parent process on the remote system
	 * @param monitor A progress monitor to which progress will be reported
	 * @param parentPID The ID of the parent process whose children are to be listed
	 */
	public IHostProcess[] listChildProcesses(IProgressMonitor monitor, long parentPID) throws SystemMessageException;

	/**
	 * Return a list of all remote child processes of the given parent process on the remote system
	 * @param monitor A progress monitor to which progress will be reported
	 * @param parentPID The ID of the parent process whose children are to be listed
	 * @param filter A filter to narrow results by
	 */
	public IHostProcess[] listChildProcesses(IProgressMonitor monitor, long parentPID, IHostProcessFilter filter) throws SystemMessageException;
	
	/**
	 * Given a process, return its parent process object.
	 * @param monitor A progress monitor to which progress will be reported
	 * @param PID the ID of the process to return parent of.
	 */
	public IHostProcess getParentProcess(IProgressMonitor monitor, long PID) throws SystemMessageException;
	
	/**
	 * Given a pid, return an IHostProcess object for it.
 	 * @param monitor A progress monitor to which progress will be reported
	 * @param PID The process ID of the desired process
	 */
	public IHostProcess getProcess(IProgressMonitor monitor, long PID) throws SystemMessageException;		
	
	/**
	 * Kills a process. 
	 * @param monitor A progress monitor to which progress will be reported
	 * @param PID the ID of the process to be killed.
	 * @param signal the signal to send to the process
	 * @return false if the given process doesn't exist, else true. Throws an exception if anything fails.
	 */
	public boolean kill(IProgressMonitor monitor, long PID, String signal) throws SystemMessageException;	

	/**
	 * Returns a list of the types of signals that can be sent to
	 * a process on the remote system.
	 * @return the signal types, or null if there are none, or they cannot be found.
	 */
	public String[] getSignalTypes();
}