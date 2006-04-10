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

package org.eclipse.rse.subsystems.processes.core.subsystem;

import org.eclipse.rse.services.clientserver.processes.IHostProcess;

/**
 * An adapter for converting from IHostProcesses to IRemoteProcesses. An IHostProcess is an abstract representation of a 
 * process on a host machine, whereas an IRemoteProcess also contains information that is RSE specific.
 * @author mjberger
 *
 */
public interface IHostProcessToRemoteProcessAdapter 
{
	/**
	 * Convert a set of IHostProcess objects to IRemoteProcess objects.
	 * @param context An object representing some contextual information for the processes
	 * @param parent The parent process object
	 * @param nodes The set of IHostProcess objects to convert
	 * @return the converted objects
	 */
	public IRemoteProcess[] convertToRemoteProcesses(IRemoteProcessContext context, IRemoteProcess parent, IHostProcess[] nodes);

	/**
	 * Convert a single IHostProcess object to an IRemoteProcess object.
	 * @param context An object representing some contextual information for the process
	 * @param parent The parent process object
	 * @param node The IHostProcess object to convert
	 * @return the converted object
	 */
	public IRemoteProcess convertToRemoteProcess(IRemoteProcessContext context, IRemoteProcess parent, IHostProcess node);

}