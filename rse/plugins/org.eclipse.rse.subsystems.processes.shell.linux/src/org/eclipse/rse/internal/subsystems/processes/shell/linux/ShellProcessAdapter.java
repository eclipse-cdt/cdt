/*******************************************************************************
 * Copyright (c) 2006, 2007 MontaVista Software, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Yu-Fen Kuo (MontaVista) - initial API and implementation
 * Martin Oberhuber (Wind River) - [refactor] "shell" instead of "ssh" everywhere 
 *******************************************************************************/

package org.eclipse.rse.internal.subsystems.processes.shell.linux;

import java.util.ArrayList;

import org.eclipse.rse.services.clientserver.processes.IHostProcess;
import org.eclipse.rse.subsystems.processes.core.subsystem.IHostProcessToRemoteProcessAdapter;
import org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcess;
import org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcessContext;
import org.eclipse.rse.subsystems.processes.core.subsystem.impl.RemoteProcessImpl;

/**
 * Utility class to convert host process records into remote process records
 * 
 */
public class ShellProcessAdapter implements IHostProcessToRemoteProcessAdapter {

	/**
	 * Convert a set of IHostProcess objects to IRemoteProcess objects.
	 * 
	 * @see org.eclipse.rse.subsystems.processes.core.subsystem.IHostProcessToRemoteProcessAdapter#convertToRemoteProcesses(org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcessContext,
	 *      org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcess,
	 *      org.eclipse.rse.services.clientserver.processes.IHostProcess[])
	 */
	public IRemoteProcess[] convertToRemoteProcesses(
			final IRemoteProcessContext context, final IRemoteProcess parent,
			final IHostProcess[] nodes) {
		if (nodes == null)
			return null;

		final ArrayList list = new ArrayList(nodes.length);

		for (int idx = 0; idx < nodes.length; idx++) {
			final LinuxHostProcess node = (LinuxHostProcess) nodes[idx];
			final IRemoteProcess newProcess = new RemoteProcessImpl(context,
					node);
			list.add(newProcess);
		}

		final IRemoteProcess[] processes = new IRemoteProcess[list.size()];

		for (int idx = 0; idx < list.size(); idx++) {
			processes[idx] = (IRemoteProcess) list.get(idx);
		}

		return processes;
	}

	/**
	 * Convert a single IHostProcess object to an IRemoteProcess object.
	 * 
	 * @see org.eclipse.rse.subsystems.processes.core.subsystem.IHostProcessToRemoteProcessAdapter#convertToRemoteProcess(org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcessContext,
	 *      org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcess,
	 *      org.eclipse.rse.services.clientserver.processes.IHostProcess)
	 */
	public IRemoteProcess convertToRemoteProcess(
			final IRemoteProcessContext context, final IRemoteProcess parent,
			final IHostProcess node) {
		final IHostProcess[] nodes = new IHostProcess[1];
		nodes[0] = node;

		final IRemoteProcess[] processes = convertToRemoteProcesses(context,
				parent, nodes);
		if (processes != null && processes.length > 0)
			return processes[0];
		else
			return null;
	}

}