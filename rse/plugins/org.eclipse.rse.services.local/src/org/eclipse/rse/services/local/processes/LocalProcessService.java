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

package org.eclipse.rse.services.local.processes;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.SortedSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.clientserver.processes.IHostProcess;
import org.eclipse.rse.services.clientserver.processes.IHostProcessFilter;
import org.eclipse.rse.services.clientserver.processes.handlers.ProcessHandler;
import org.eclipse.rse.services.clientserver.processes.handlers.ProcessHandlerManager;
import org.eclipse.rse.services.local.ILocalService;
import org.eclipse.rse.services.local.files.LocalServiceResources;
import org.eclipse.rse.services.processes.AbstractProcessService;
import org.eclipse.rse.services.processes.IProcessService;

public class LocalProcessService extends AbstractProcessService implements ILocalService, IProcessService 
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
	
	public IHostProcess[] listAllProcesses(IProgressMonitor monitor, IHostProcessFilter filter) throws SystemMessageException 
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
			throw new SystemMessageException(getMessage("RSEPG1301"));
		}
		return processes;
	}

	public boolean kill(IProgressMonitor monitor, long PID, String signal) throws SystemMessageException 
	{
		IHostProcess process = null;
		try
		{
			process = getProcess(monitor, PID);
			handler.kill(process, signal);
			return true;
		}
		catch (InterruptedException e)
		{
			throw new SystemMessageException(getMessage("RSEG1067"));
		}
		catch (Exception e)
		{
			String name = "";
			if (process != null) name += process.getName();
			String pid = "";
			if (process != null) pid += process.getPid();
			
			SystemMessage msg = getMessage("RSEPG1300");
			msg.makeSubstitution(name + " (" + pid + ")", e.getMessage());
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
			Process kill = Runtime.getRuntime().exec("kill -l");
			InputStreamReader isr = new InputStreamReader(kill.getInputStream());
			if (isr == null) return null;
			BufferedReader reader = new BufferedReader(isr);
			if (reader == null) return null;
			String nextLine = reader.readLine();
			String output = "";
			while (nextLine != null)
			{
				output = output + nextLine + "\n";
				nextLine = reader.readLine();
			}
			reader.close();
			isr.close();
			if (output.equals("")) throw new Exception();
			String[] lines = output.trim().split("\\s+");
			if (lines == null) throw new Exception();
			return lines;
		}
		catch (Exception e)
		{
			//SystemPlugin.logError("LocalProcessSubSystemImpl.getSignalTypes() 'kill -l' command failed.", e);
			return null;
		}
	}

	public void initService(IProgressMonitor monitor)
	{
		
	}
	
	public void uninitService(IProgressMonitor monitor)
	{
	}
}