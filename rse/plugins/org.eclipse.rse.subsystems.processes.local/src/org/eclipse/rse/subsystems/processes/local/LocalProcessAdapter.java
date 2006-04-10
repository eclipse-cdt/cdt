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

package org.eclipse.rse.subsystems.processes.local;

import org.eclipse.rse.services.clientserver.processes.IHostProcess;
import org.eclipse.rse.subsystems.processes.core.subsystem.IHostProcessToRemoteProcessAdapter;
import org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcess;
import org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcessContext;
import org.eclipse.rse.subsystems.processes.core.subsystem.impl.RemoteProcessImpl;

/**
 * Adapter for converting IHostProcess objects (objects where the underlying
 * process resides on the local machine) to IRemoteProcess objects
 * 
 * @author mjberger
 *
 */
public class LocalProcessAdapter implements IHostProcessToRemoteProcessAdapter {

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.processes.core.subsystem.IHostProcessToRemoteProcessAdapter#convertToRemoteProcesses(org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcessContext, org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcess, org.eclipse.rse.services.clientserver.processes.IHostProcess[])
	 */
	public IRemoteProcess[] convertToRemoteProcesses(
			IRemoteProcessContext context, IRemoteProcess parent,
			IHostProcess[] nodes) 
	{
		if (nodes == null)
			return null;
				
		IRemoteProcess[] processes = new IRemoteProcess[nodes.length];

		for (int idx = 0; idx < nodes.length; idx++)
		{
			processes[idx] = new RemoteProcessImpl(context, nodes[idx]);				
		}

		return processes;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.processes.core.subsystem.IHostProcessToRemoteProcessAdapter#convertToRemoteProcess(org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcessContext, org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcess, org.eclipse.rse.services.clientserver.processes.IHostProcess)
	 */
	public IRemoteProcess convertToRemoteProcess(IRemoteProcessContext context,
			IRemoteProcess parent, IHostProcess node) 
	{
		IHostProcess[] nodes = new IHostProcess[1];
		nodes[0] = node;
		
		IRemoteProcess[] processes = convertToRemoteProcesses(context, parent, nodes);
		if (processes != null && processes.length > 0)
			return processes[0];
		else return null;
	}

}