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

package org.eclipse.rse.services.dstore.shells;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.dstore.core.model.IDataStoreProvider;
import org.eclipse.rse.dstore.universal.miners.command.CommandMiner;
import org.eclipse.rse.dstore.universal.miners.environment.EnvironmentMiner;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.dstore.AbstractDStoreService;
import org.eclipse.rse.services.dstore.ServiceResources;
import org.eclipse.rse.services.dstore.util.DStoreStatusMonitor;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IShellService;


public class DStoreShellService extends AbstractDStoreService implements IShellService
{
	protected String[] _envVars;
	protected DataElement _envMinerElement;
	protected DataElement _envMinerStatus;
	
	public DStoreShellService(IDataStoreProvider dataStoreProvider)
	{
		super(dataStoreProvider);
	}
	
	
	public String getName()
	{
		return ServiceResources.DStore_Shell_Service_Label;
	}
	
	public String getDescription()
	{
		return ServiceResources.DStore_Shell_Service_Description;
	}
	
	
	public IHostShell launchShell(IProgressMonitor monitor, String initialWorkingDirectory, String[] environment)
	{
		if (!isInitialized())
		{
			waitForInitialize(null);
		}
		return launchShell(monitor, initialWorkingDirectory, null, environment);
	}

	public IHostShell launchShell(IProgressMonitor monitor, String initialWorkingDirectory, String encoding, String[] environment)
	{
		if (!isInitialized())
		{
			waitForInitialize(null);
		}
		return new DStoreHostShell(getStatusMonitor(getDataStore()), getDataStore(), initialWorkingDirectory, ">", encoding, environment);
	}

	public IHostShell runCommand(IProgressMonitor monitor, String initialWorkingDirectory, String command,
			String[] environment)
	{
		if (!isInitialized())
		{
			waitForInitialize(null);
		}
		return runCommand(monitor, initialWorkingDirectory, command, null, environment);
	}

	public IHostShell runCommand(IProgressMonitor monitor, String initialWorkingDirectory, String command,
			String encoding, String[] environment)
	{
		if (!isInitialized())
		{
			waitForInitialize(null);
		}
		return new DStoreHostShell(getStatusMonitor(getDataStore()), getDataStore(), initialWorkingDirectory, command, encoding, environment);
	}

	public String[] getHostEnvironment()
	{
		if (_envVars == null || _envVars.length == 0)
		{
			List envVars = new ArrayList();
			DataStore ds = getDataStore();
			DataElement envMinerData = ds.findMinerInformation(getEnvSystemMinerId());
			if (envMinerData != null)
			{
				DataElement systemEnvironment = ds.find(envMinerData, DE.A_NAME, "System Environment", 1);
				if (systemEnvironment != null && systemEnvironment.getNestedSize() > 0)
				{
					for (int i = 0; i < systemEnvironment.getNestedSize(); i++)
					{
						DataElement var = systemEnvironment.get(i);
						envVars.add(var.getValue());
					}
				}
			}
			_envVars = (String[])envVars.toArray(new String[envVars.size()]);
		}
		return _envVars;
	}
	
	protected String getMinerId()
	{
		return CommandMiner.MINER_ID;
	}

	protected String getEnvSystemMinerId()
	{
		return EnvironmentMiner.MINER_ID;
	}
	
	
	public boolean isInitialized()
	{
		if (_initializeStatus != null)
		{
			DStoreStatusMonitor smon = getStatusMonitor(getDataStore());
			return smon.determineStatusDone(_initializeStatus);
		}
		return false;
	}
	
	protected void waitForInitialize(IProgressMonitor monitor)
	{
		if (_envMinerStatus!= null)
		{
			DStoreStatusMonitor smon = getStatusMonitor(getDataStore());
			try
			{
				smon.waitForUpdate(_envMinerStatus, monitor);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			getMinerElement(getEnvSystemMinerId());
		}
		super.waitForInitialize(monitor);
	}
	
	public void uninitService(IProgressMonitor monitor)
	{
		super.uninitService(monitor);
		
	}
	
	protected void initMiner(IProgressMonitor monitor)
	{
		// init env miner first

			if (getServerVersion() >= 8)
			{
				String minerId = getEnvSystemMinerId();		
				String message = SystemMessage.sub(ServiceResources.DStore_Service_ProgMon_Initializing_Message, "&1", minerId);
				monitor.beginTask(message, IProgressMonitor.UNKNOWN);
				DataStore ds = getDataStore();
				if (_envMinerElement == null || _envMinerElement.getDataStore() != ds)
				{	
					if (ds != null)
					{				
						_envMinerStatus = ds.activateMiner(minerId);
						/*
						DStoreStatusMonitor smon = getStatusMonitor(ds);
						try
						{
							smon.waitForUpdate(status, monitor, 50);
						}
						catch (InterruptedException e)
						{
							e.printStackTrace();
						}
						
						getMinerElement(getEnvSystemMinerId());
						*/
						
						
					}			
				}
				super.initMiner(monitor);
			}	
	}
	
}