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

import org.eclipse.rse.services.IService;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.clientserver.processes.IHostProcess;
import org.eclipse.rse.services.clientserver.processes.IHostProcessFilter;

/**
 * An IProcessService is an abstraction of a process service that runs over some
 * sort of connection. It can be shared among multiple instances of a subsystem.
 * Each subsystem is currently responsible for layering an abstraction over
 * whatever it wants to construct as a service.
 * <p>
 * Implementers of this interface will have to either be instantiated,
 * initialized, or somehow derive a connection as part of its state.
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 *              Process service implementations must subclass
 *              {@link AbstractProcessService} rather than implementing this
 *              interface directly.
 */
public interface IProcessService extends IService
{
	/**
	 * Return a list of all processes on the remote system.
	 *
	 * @param monitor A progress monitor to which progress will be reported
	 * @return List of all processes
	 * @throws SystemMessageException in case anything goes wrong
	 */
	public IHostProcess[] listAllProcesses(IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Return a filtered list of all processes on the remote system.
	 *
	 * @param filter An object to filter results by
	 * @param monitor A progress monitor to which progress will be reported
	 * @return Filtered list of processes
	 * @throws SystemMessageException in case anything goes wrong
	 */
	public IHostProcess[] listAllProcesses(IHostProcessFilter filter, IProgressMonitor monitor)  throws SystemMessageException;

	/**
	 * Return a filtered list of all processes on the remote system.
	 *
	 * @param exeNameFilter The executable name to filter results by, or null if
	 *            no exeName filtering
	 * @param userNameFilter The user name to filter results by, or null if no
	 *            userName filtering
	 * @param stateFilter The state code to filter results by, or null if no
	 *            state filtering
	 * @param monitor A progress monitor to which progress will be reported
	 * @return Filtered list of processes
	 * @throws SystemMessageException in case anything goes wrong
	 */
	public IHostProcess[] listAllProcesses(String exeNameFilter, String userNameFilter, String stateFilter, IProgressMonitor monitor)  throws SystemMessageException;

	/**
	 * Returns root processes on the remote system
	 *
	 * @param monitor A progress monitor to which progress will be reported
	 * @return List of root processes
	 * @throws SystemMessageException in case anything goes wrong
	 */
	public IHostProcess[] listRootProcesses(IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Return a list of all remote child processes of the given parent process
	 * on the remote system
	 *
	 * @param parentPID The ID of the parent process whose children are to be
	 *            listed
	 * @param monitor A progress monitor to which progress will be reported
	 * @return List of child processes
	 * @throws SystemMessageException in case anything goes wrong
	 */
	public IHostProcess[] listChildProcesses(long parentPID, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Return a filtered list of remote child processes of the given parent
	 * process on the remote system
	 *
	 * @param parentPID The ID of the parent process whose children are to be
	 *            listed
	 * @param filter A filter to narrow results by
	 * @param monitor A progress monitor to which progress will be reported
	 * @return Filtered list of child processes
	 * @throws SystemMessageException in case anything goes wrong
	 */
	public IHostProcess[] listChildProcesses(long parentPID, IHostProcessFilter filter, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Given a process, return its parent process object.
	 *
	 * @param pid the ID of the process to return parent of.
	 * @param monitor A progress monitor to which progress will be reported
	 * @return The parent process
	 * @throws SystemMessageException in case anything goes wrong
	 */
	public IHostProcess getParentProcess(long pid, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Given a pid, return an IHostProcess object for it.
	 *
	 * @param pid The process ID of the desired process
	 * @param monitor A progress monitor to which progress will be reported
	 * @return IHostProcess object for the given pid
	 * @throws SystemMessageException in case anything goes wrong
	 */
	public IHostProcess getProcess(long pid, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Kills a process.
	 *
	 * @param pid the ID of the process to be killed.
	 * @param signal the signal to send to the process
	 * @param monitor A progress monitor to which progress will be reported
	 * @return <code>false</code> if the given process doesn't exist, else
	 *         <code>true</code>. Throws an exception if anything fails.
	 * @throws SystemMessageException in case anything goes wrong
	 */
	public boolean kill(long pid, String signal, IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Returns a list of the types of signals that can be sent to
	 * a process on the remote system.
	 * @return the signal types, or null if there are none, or they cannot be found.
	 */
	public String[] getSignalTypes();
}