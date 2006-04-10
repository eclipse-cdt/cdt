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

import org.eclipse.rse.core.servicesubsystem.IServiceSubSystemConfiguration;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.services.processes.IProcessService;
import org.eclipse.rse.subsystems.processes.core.subsystem.IHostProcessToRemoteProcessAdapter;
import org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcessSubSystemConfiguration;

/**
 * Factory for creating the ProcessServiceSubSystem and for getting the associated
 * service and adapter.
 * @author mjberger
 *
 */
public interface IProcessServiceSubSystemConfiguration extends IServiceSubSystemConfiguration, IRemoteProcessSubSystemConfiguration 
{
	/**
	 * @param host The object representing the remote machine to which the process service is associated
	 * @return The process service associated with the host.
	 */
	public IProcessService getProcessService(IHost host);	

	/**
	 * Creates a new process service object associated with a specific remote host
	 * @param host The object representing the remote machine to which the process service is associated
	 * @return The process service associated with the host.
	 */
	public IProcessService createProcessService(IHost host);

	/**
	 * @return An adapter for converting IHostProcess objects to IRemoteProcess
	 */
	public IHostProcessToRemoteProcessAdapter getHostProcessAdapter();

}