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

package org.eclipse.rse.services.dstore.processes;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.dstore.core.model.IDataStoreProvider;
import org.eclipse.rse.dstore.universal.miners.processes.IUniversalProcessDataStoreConstants;
import org.eclipse.rse.dstore.universal.miners.processes.UniversalProcessMiner;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.clientserver.processes.IHostProcess;
import org.eclipse.rse.services.clientserver.processes.IHostProcessFilter;
import org.eclipse.rse.services.clientserver.processes.ISystemProcessRemoteConstants;
import org.eclipse.rse.services.dstore.ServiceResources;
import org.eclipse.rse.services.dstore.util.DStoreStatusMonitor;
import org.eclipse.rse.services.processes.AbstractProcessService;
import org.eclipse.rse.services.processes.IProcessService;


public class DStoreProcessService extends AbstractProcessService implements IProcessService, IUniversalProcessDataStoreConstants, ISystemProcessRemoteConstants
{
	protected IDataStoreProvider _provider;
	protected DataElement _minerElement = null;
	protected DStoreStatusMonitor _statusMonitor;
	protected DataElement _procMinerStatus;
	protected String[] _statusTypes;
	
	public DStoreProcessService(IDataStoreProvider provider)
	{
		_provider = provider;
	}
	
	public String getName()
	{
		return ServiceResources.DStore_Process_Service_Label;
	}
	
	public String getDescription()
	{
		return ServiceResources.DStore_Process_Service_Description;
	}
	
	public IHostProcess[] listAllProcesses(IProgressMonitor monitor, IHostProcessFilter filter) throws SystemMessageException 
	{
		if (!isInitialized())
		{
			waitForInitialize(monitor);
		}
		IHostProcess[] processes = null;

		DataStore ds = getDataStore();
		DataElement universaltemp = getMinerElement();
	
		// create filter descriptor
		DataElement deObj;
		deObj = ds.find(universaltemp, DE.A_NAME, UNIVERSAL_PROCESS_ROOT, 1);
		if (deObj == null) deObj = ds.createObject(universaltemp, UNIVERSAL_PROCESS_FILTER, UNIVERSAL_PROCESS_ROOT, "", "", false);
		deObj.setAttribute(DE.A_SOURCE, filter.toString());		

		// query
		DataElement queryCmd = ds.localDescriptorQuery(deObj.getDescriptor(), C_PROCESS_FILTER_QUERY_ALL);

		if (queryCmd != null)
		{
			DataElement status = ds.command(queryCmd, deObj, true);

		    DStoreStatusMonitor smon = getStatusMonitor(getDataStore());
			try
			{
				smon.waitForUpdate(status);
			}
			catch (InterruptedException e)
			{
				throw new SystemMessageException(getMessage("RSEG1067"));
			}
			// get results
			List nested = deObj.getNestedData();
			if (nested != null)
			{
				Object[] results = nested.toArray();
	
				String message = status.getAttribute(DE.A_VALUE);
				if (!message.equals(PROCESS_MINER_SUCCESS))
				{
					throw new SystemMessageException(getMessage("RSEPG1301"));
				}
						
				// convert objects to remote files
				processes = convertObjsToHostProcesses(filter, results);
			}
		}
		else
		{
			//SystemPlugin.logWarning(CLASSNAME + " queryCmd is null in listRoots");
		}

		return processes;
	}
	
	/**
	 * Helper method to convert DataElement objects to IRemoteClientProcess objects.
	 */
	protected IHostProcess[] convertObjsToHostProcesses(IHostProcessFilter processFilter, Object[] objs)
	{
		if (objs == null)
			return null;
				
		ArrayList list = new ArrayList(objs.length);

		for (int idx = 0; idx < objs.length; idx++)
		{
			DataElement de = (DataElement) objs[idx];
			if (!de.isDeleted())
			{
				if (processFilter == null || processFilter.allows(de.getValue()))
				{
					DStoreHostProcess newProcess = new DStoreHostProcess(de);				
					list.add(newProcess);
				}	
			}
		}

		IHostProcess[] processes = new IHostProcess[list.size()];

		for (int idx = 0; idx < list.size(); idx++)
		{
			processes[idx] = (IHostProcess) list.get(idx);
		}

		return processes;
	}
	
	/**
	 * Helper method to return the DataStore object needed by comm layer.
	 */
	protected DataStore getDataStore()
	{
		return _provider.getDataStore();
	}
	
	protected DataElement getMinerElement()
	{

	    if (_minerElement == null || _minerElement.getDataStore() != getDataStore())
	    {
	        _minerElement = getDataStore()
				.findMinerInformation(
						"org.eclipse.rse.dstore.universal.miners.processes.UniversalProcessMiner");
	    }
	    return _minerElement;
	}
	
	public DStoreStatusMonitor getStatusMonitor(DataStore dataStore)
	{
		if (_statusMonitor == null || _statusMonitor.getDataStore() != dataStore)
		{
			_statusMonitor = new DStoreStatusMonitor(dataStore);
		}
		return _statusMonitor;
	}

	public boolean kill(IProgressMonitor monitor, long PID, String signal) throws SystemMessageException 
	{
		try
		{
			DataStore ds = getDataStore();
		
			// run kill command on host
			DStoreHostProcess process = (DStoreHostProcess) getProcess(monitor, PID);
			DataElement deObj = (DataElement) process.getObject();
			DataElement killCmd = ds.localDescriptorQuery(deObj.getDescriptor(), C_PROCESS_KILL);
			deObj.setAttribute(DE.A_SOURCE, signal);
			
			if (killCmd != null)
			{
				DataElement status = ds.command(killCmd, deObj, true);
	
			    DStoreStatusMonitor smon = getStatusMonitor(getDataStore());
				smon.waitForUpdate(status);
				
				// get results
				String message = status.getAttribute(DE.A_VALUE);
				if (message.equals(PROCESS_MINER_SUCCESS)) return true;
				else
				{
					SystemMessage msg = getMessage("RSEPG1300");
					msg.makeSubstitution(process.getName() + " (" + process.getPid() + ")", message);
					throw new SystemMessageException(msg);
				}
			}
			else
			{
				// SystemPlugin.logWarning(CLASSNAME + " queryCmd is null in listRoots");
			}
		}
		catch (InterruptedException e)
		{
			throw new SystemMessageException(getMessage("RSEG1067"));
		}
		return false;	
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
	 * Returns a list of the types of signals that can be sent to
	 * a process on the remote system.
	 * @return the signal types, or null if there are none, or they cannot be found.
	 */
	private String[] internalGetSignalTypes()
	{
		try
		{
			DataElement sigTypesElement = getSignalTypesMinerElement();
			String sigTypesOutput = sigTypesElement.getValue();
			String[] lines = sigTypesOutput.trim().split("\\s+");
			if (lines == null) throw new Exception();
			return lines;
		}
		catch (Exception e)
		{
			// SystemPlugin.logError("UniversalProcessSubSystemImpl.getSignalTypes() 'kill -l' command failed.", e);
			return null;
		}
	}
	
	protected DataElement getSignalTypesMinerElement()
	{
		return getDataStore().find(_minerElement, DE.A_NAME, "universal.killinfo");
	}

	public void initService(IProgressMonitor monitor)
	{
		initMiner(monitor);
	}
	
	public boolean isInitialized()
	{
		if (_procMinerStatus != null)
		{
			DStoreStatusMonitor smon = getStatusMonitor(getDataStore());
			return smon.determineStatusDone(_procMinerStatus);
		}
		return false;
	}
	
	protected void waitForInitialize(IProgressMonitor monitor)
	{
		if (_procMinerStatus!= null)
		{
			DStoreStatusMonitor smon = getStatusMonitor(getDataStore());
			try
			{
				smon.waitForUpdate(_procMinerStatus, monitor);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			getMinerElement();
		}
	}
	
	protected void initMiner(IProgressMonitor monitor)
	{
		DataElement fsElement = getMinerElement();
		if (fsElement == null)
		{
			if (getServerVersion() >= 8)
			{
				String minerId = getMinerId();		
				String message = SystemMessage.sub(ServiceResources.DStore_Service_ProgMon_Initializing_Message, "&1", minerId);
				monitor.beginTask(message, IProgressMonitor.UNKNOWN);
				DataStore ds = getDataStore();
				if (_minerElement == null || _minerElement.getDataStore() != ds)
				{	
					if (ds != null)
					{				
						_procMinerStatus = ds.activateMiner(minerId);					
						
					}			
				}
			}
		}
		monitor.done();
	}
	
	protected String getMinerId()
	{
		return UniversalProcessMiner.MINER_ID;
	}
	
	public int getServerVersion()
	{
		return getDataStore().getServerVersion();
	}
	
	public int getServerMinor()
	{
		return getDataStore().getServerMinor();
	}
	
	protected String getProcessMinerId()
	{
		return UniversalProcessMiner.MINER_ID;
	}
}