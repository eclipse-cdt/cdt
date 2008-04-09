/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Kevin Doyle (IBM) - [199871] LocalProcessService needs to implement getMessage()
 * Martin Oberhuber (Wind River) - [226262] Make IService IAdaptable and add Javadoc
 *******************************************************************************/

package org.eclipse.rse.internal.services.local.processes;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.SortedSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.internal.services.local.ILocalService;
import org.eclipse.rse.internal.services.local.LocalServiceResources;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.clientserver.processes.IHostProcess;
import org.eclipse.rse.services.clientserver.processes.IHostProcessFilter;
import org.eclipse.rse.services.clientserver.processes.handlers.ProcessHandler;
import org.eclipse.rse.services.clientserver.processes.handlers.ProcessHandlerManager;
import org.eclipse.rse.services.processes.AbstractProcessService;

public class LocalProcessService extends AbstractProcessService implements ILocalService
{
	protected String[] _statusTypes;
	protected ProcessHandler handler;

	public LocalProcessService()
	{
		handler = ProcessHandlerManager.getInstance().getNewProcessHandler();
	}


	public String getName()
	{
		return LocalServiceResources.Local_Process_Service_Name;
	}

	public String getDescription()
	{
		return LocalServiceResources.Local_Process_Service_Description;
	}

	public IHostProcess[] listAllProcesses(IHostProcessFilter filter, IProgressMonitor monitor) throws SystemMessageException
	{
		IHostProcess[] processes = null;

		if (handler == null) return null;
		try
		{
			SortedSet results = handler.lookupProcesses(filter);
			processes = (IHostProcess[]) results.toArray(new IHostProcess[results.size()]);
		}
		catch (Exception e)
		{
			throw new SystemMessageException(getMessage("RSEPG1301")); //$NON-NLS-1$
		}
		return processes;
	}

	public boolean kill(long PID, String signal, IProgressMonitor monitor) throws SystemMessageException
	{
		IHostProcess process = null;
		try
		{
			process = getProcess(PID, monitor);

			// if there is no process, simply return true
			if (process == null) {
				return true;
			}

			handler.kill(process, signal);
			return true;
		}
		catch (InterruptedException e)
		{
			throw new SystemMessageException(getMessage("RSEG1067")); //$NON-NLS-1$
		}
		catch (Exception e)
		{
			String name = ""; //$NON-NLS-1$
			if (process != null) name += process.getName();
			String pid = ""; //$NON-NLS-1$
			if (process != null) pid += process.getPid();

			SystemMessage msg = getMessage("RSEPG1300"); //$NON-NLS-1$
			msg.makeSubstitution(name + " (" + pid + ")", e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
			throw new SystemMessageException(msg);
		}
	}

	public String[] getSignalTypes()
	{
		if (_statusTypes != null)
		{
			return _statusTypes;
		}
		else
		{
			_statusTypes = internalGetSignalTypes();
		}
		return _statusTypes;
	}

	/**
	 * Returns a list of the signal types supported by the 'kill' command on this system
	 * @return a list of the signal types or null if there are none or there is an error in
	 * executing the kill command.
	 */
	protected String[] internalGetSignalTypes()
	{
		try
		{
			// use the kill command to find out the signal types
			Process kill = Runtime.getRuntime().exec("kill -l"); //$NON-NLS-1$
			InputStreamReader isr = new InputStreamReader(kill.getInputStream());
			BufferedReader reader = new BufferedReader(isr);

			String nextLine = reader.readLine();
			String output = ""; //$NON-NLS-1$
			while (nextLine != null)
			{
				output = output + nextLine + "\n"; //$NON-NLS-1$
				nextLine = reader.readLine();
			}
			reader.close();
			isr.close();
			if (output.equals("")) throw new Exception(); //$NON-NLS-1$
			String[] lines = output.trim().split("\\s+"); //$NON-NLS-1$
			if (lines == null) throw new Exception();
			return lines;
		}
		catch (Exception e)
		{
			//SystemPlugin.logError("LocalProcessSubSystemImpl.getSignalTypes() 'kill -l' command failed.", e);
			return null;
		}
	}

}
