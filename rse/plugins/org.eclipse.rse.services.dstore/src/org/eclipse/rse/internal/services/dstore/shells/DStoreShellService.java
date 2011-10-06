/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
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
 * David McKnight   (IBM)        - [190803] Canceling a long-running dstore job prints "InterruptedException" to stdout
 * David McKnight   (IBM)        - [196624] dstore miner IDs should be String constants rather than dynamic lookup
 * David McKnight   (IBM)        - [216252] use SimpleSystemMessage instead of getMessage()
 * Martin Oberhuber (Wind River) - [226262] Make IService IAdaptable and add Javadoc
 * Martin Oberhuber (Wind River) - [226301][api] IShellService should throw SystemMessageException on error
 * David McKnight   (IBM)        - [244898] [dstore] IRemoteCmdSubSystem.getHostEnvironmentVariables() call does not always work
 * David McKnight   (IBM)        - [356658] [dstore] getHostEnvironment returns an empty array immediately after connecting
 *******************************************************************************/

package org.eclipse.rse.internal.services.dstore.shells;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.dstore.core.model.IDataStoreProvider;
import org.eclipse.rse.dstore.universal.miners.IUniversalDataStoreConstants;
import org.eclipse.rse.internal.services.dstore.ServiceResources;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.dstore.AbstractDStoreService;
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


	public IHostShell launchShell(String initialWorkingDirectory, String[] environment, IProgressMonitor monitor) throws SystemMessageException
	{
		if (!isInitialized())
		{
			waitForInitialize(null);
		}
		return launchShell(initialWorkingDirectory, null, environment, monitor);
	}

	public IHostShell launchShell(String initialWorkingDirectory, String encoding, String[] environment, IProgressMonitor monitor) throws SystemMessageException
	{
		if (!isInitialized())
		{
			waitForInitialize(null);
		}
		return new DStoreHostShell(getStatusMonitor(getDataStore()), getDataStore(), initialWorkingDirectory, ">", encoding, environment); //$NON-NLS-1$
	}

	public IHostShell runCommand(String initialWorkingDirectory, String command, String[] environment,
			IProgressMonitor monitor) throws SystemMessageException
	{
		if (!isInitialized())
		{
			waitForInitialize(null);
		}
		return runCommand(initialWorkingDirectory, command, null, environment, monitor);
	}

	public IHostShell runCommand(String initialWorkingDirectory, String command, String encoding,
			String[] environment, IProgressMonitor monitor) throws SystemMessageException
	{
		if (!isInitialized())
		{
			waitForInitialize(null);
		}
		return new DStoreHostShell(getStatusMonitor(getDataStore()), getDataStore(), initialWorkingDirectory, command, encoding, environment);
	}

	public String[] getHostEnvironment() throws SystemMessageException
	{
		if (_envVars == null || _envVars.length == 0)
		{
			if (!isInitialized()){
				waitForInitialize(null);
			}
			
			List envVars = new ArrayList();
			DataStore ds = getDataStore();
			DataElement envMinerData = ds.findMinerInformation(getEnvSystemMinerId());
			if (envMinerData == null){ // could be using an old server
				envMinerData = ds.findMinerInformation("com.ibm.etools.systems.dstore.miners.environment.EnvironmentMiner"); //$NON-NLS-1$
			}
			if (envMinerData != null)
			{
				DataElement systemEnvironment = ds.find(envMinerData, DE.A_NAME, "System Environment", 1); //$NON-NLS-1$
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
		return IUniversalDataStoreConstants.UNIVERSAL_COMMAND_MINER_ID;
	}

	protected String getEnvSystemMinerId()
	{
		return IUniversalDataStoreConstants.UNIVERSAL_ENVIRONMENT_MINER_ID;
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
				// cancel monitor if it's still not cancelled
				if (monitor != null && !monitor.isCanceled())
				{
					monitor.setCanceled(true);
				}

				//InterruptedException is used to report user cancellation, so no need to log
				//This should be reviewed (use OperationCanceledException) with bug #190750
			}
			getMinerElement(getEnvSystemMinerId());
		}
		super.waitForInitialize(monitor);
	}

	public void uninitService(IProgressMonitor monitor)
	{
		_envMinerElement = null;
		_envMinerStatus = null;
		_envVars = null;
		super.uninitService(monitor);
	}

	protected void initMiner(IProgressMonitor monitor)
	{
		// init env miner first

			if (getServerVersion() >= 8)
			{
				String minerId = getEnvSystemMinerId();
				String message = SystemMessage.sub(ServiceResources.DStore_Service_ProgMon_Initializing_Message, "&1", minerId); //$NON-NLS-1$
				monitor.beginTask(message, IProgressMonitor.UNKNOWN);
				DataStore ds = getDataStore();
				if (_envMinerElement == null || _envMinerElement.getDataStore() != ds)
				{
					if (ds != null && _envMinerStatus == null)
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
