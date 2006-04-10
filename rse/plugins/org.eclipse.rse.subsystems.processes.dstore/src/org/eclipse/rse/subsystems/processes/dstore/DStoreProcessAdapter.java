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

package org.eclipse.rse.subsystems.processes.dstore;

import java.util.ArrayList;

import org.eclipse.rse.services.clientserver.processes.IHostProcess;
import org.eclipse.rse.services.dstore.processes.DStoreHostProcess;
import org.eclipse.rse.subsystems.processes.core.subsystem.IHostProcessToRemoteProcessAdapter;
import org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcess;
import org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcessContext;
import org.eclipse.rse.subsystems.processes.core.subsystem.impl.RemoteProcessImpl;

import org.eclipse.dstore.core.model.DataElement;

/**
 * Adapter for converting DStoreHostProcess objects (objects where the underlying
 * process is represented by a DataElement) to IRemoteProcess objects
 * 
 * @author mjberger
 *
 */
public class DStoreProcessAdapter implements IHostProcessToRemoteProcessAdapter {

	/**
	 * @see org.eclipse.rse.subsystems.processes.core.subsystem.IHostProcessToRemoteProcessAdapter#convertToRemoteProcesses(org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcessContext, org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcess, org.eclipse.rse.services.clientserver.processes.IHostProcess[])
	 * Precondition: each element of nodes must be a DStoreHostProcess
	 */
	public IRemoteProcess[] convertToRemoteProcesses(IRemoteProcessContext context, IRemoteProcess parent, IHostProcess[] nodes) 
	{
		if (nodes == null)
			return null;
				
		ArrayList list = new ArrayList(nodes.length);

		for (int idx = 0; idx < nodes.length; idx++)
		{
			DStoreHostProcess node = (DStoreHostProcess) nodes[idx];
			DataElement de = (DataElement) node.getObject();
			if (!de.isDeleted())
			{
				IRemoteProcess newProcess = new RemoteProcessImpl(context, node);				
				list.add(newProcess);	
			}
		}

		IRemoteProcess[] processes = new IRemoteProcess[list.size()];

		for (int idx = 0; idx < list.size(); idx++)
		{
			processes[idx] = (IRemoteProcess) list.get(idx);
		}

		return processes;
	}

	/**
	 * @see org.eclipse.rse.subsystems.processes.core.subsystem.IHostProcessToRemoteProcessAdapter#convertToRemoteProcess(org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcessContext, org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcess, org.eclipse.rse.services.clientserver.processes.IHostProcess)
	 * Precondition: node must be a DStoreHostProcess
	 */
	public IRemoteProcess convertToRemoteProcess(IRemoteProcessContext context, IRemoteProcess parent, IHostProcess node) 
	{
		IHostProcess[] nodes = new IHostProcess[1];
		nodes[0] = node;
		
		IRemoteProcess[] processes = convertToRemoteProcesses(context, parent, nodes);
		if (processes != null && processes.length > 0)
			return processes[0];
		else return null;
	}

}