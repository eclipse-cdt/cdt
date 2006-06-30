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

package org.eclipse.rse.services.dstore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.dstore.core.model.IDataStoreProvider;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.dstore.util.DStoreStatusMonitor;

public abstract class AbstractDStoreService implements IDStoreService
{
	protected IDataStoreProvider _dataStoreProvider;
	protected DataElement _minerElement;
	protected DStoreStatusMonitor _statusMonitor;
	protected Map _cmdDescriptorMap;
	protected DataElement _initializeStatus;
	
	public AbstractDStoreService(IDataStoreProvider dataStoreProvider)
	{
		_dataStoreProvider = dataStoreProvider;
		_cmdDescriptorMap = new HashMap();
	}
	
	public DStoreStatusMonitor getStatusMonitor(DataStore dataStore)
	{
		if (_statusMonitor == null || _statusMonitor.getDataStore() != dataStore)
		{
			_statusMonitor = new DStoreStatusMonitor(dataStore);
		}
		return _statusMonitor;
	}
	
	public DataStore getDataStore()
	{
		return _dataStoreProvider.getDataStore();
	}
	
	protected DataElement getMinerElement()
	{
		if (_minerElement == null || _minerElement.getDataStore() != getDataStore())
		{
			_minerElement = getMinerElement(getMinerId());
		}
		return _minerElement;
	}
	
	protected DataElement getMinerElement(String id)
	{	    
	    return getDataStore().findMinerInformation(id);	        
	}
	 
	protected DataElement[] dsQueryCommand(IProgressMonitor monitor, DataElement subject, ArrayList args, String command)
	{
		// query roots
		DataElement queryCmd = getCommandDescriptor(subject, command);
	
		if (queryCmd != null)
		{
			DataElement status = getDataStore().command(queryCmd, args, subject, true);
			try
			{
				DStoreStatusMonitor smon = getStatusMonitor(getDataStore());
				smon.waitForUpdate(status, monitor);
				int resultSize = subject.getNestedSize();
				if (resultSize == 0)
				{
					//System.out.println("status="+status);
					//System.out.println("subject="+subject);
				}
				checkHostJVM();
				// get results
				List nested = subject.getNestedData();
				if (nested != null)
				{
					return (DataElement[])nested.toArray(new DataElement[resultSize]);
				}
			}
			catch (Exception e)
			{				
			}			
		}
		return new DataElement[0];
	}
	
	protected DataElement dsStatusCommand(IProgressMonitor monitor, DataElement subject, ArrayList args, String command)
	{
		// query roots
		DataElement queryCmd = getCommandDescriptor(subject, command);
	
		if (queryCmd != null)
		{
			DataElement status = getDataStore().command(queryCmd, args, subject, true);
			try
			{
				DStoreStatusMonitor smon = getStatusMonitor(getDataStore());
				smon.waitForUpdate(status, monitor);
			}
			catch (Exception e)
			{				
			}	
			return status;
		}
		return null;
	}
	
	protected DataElement[] dsQueryCommand(IProgressMonitor monitor, DataElement subject, String command)
	{
		// query roots
		DataElement queryCmd = getCommandDescriptor(subject, command);
	
		if (queryCmd != null)
		{
			DataStore ds = getDataStore();
			DataElement status = ds.command(queryCmd, subject, true);
			try
			{
				getStatusMonitor(ds).waitForUpdate(status, monitor);
				checkHostJVM();
				// get results
				List nested = subject.getNestedData();
				if (nested != null)
				{
					return (DataElement[])nested.toArray(new DataElement[subject.getNestedSize()]);
				}
			}
			catch (Exception e)
			{				
			}			
		}
		return new DataElement[0];
	}
	
	protected DataElement dsStatusCommand(IProgressMonitor monitor, DataElement subject, String command)
	{
		// query roots
		DataElement queryCmd = getCommandDescriptor(subject, command);
	
		if (queryCmd != null)
		{
			DataStore ds = getDataStore();
			DataElement status = ds.command(queryCmd, subject, true);
			try
			{
				getStatusMonitor(ds).waitForUpdate(status, monitor);
			}
			catch (Exception e)
			{				
			}
			return status;
		}
		return null;
	}
	
	protected DataElement getCommandDescriptor(DataElement subject, String command)
	{
		DataStore ds = getDataStore();
		DataElement cmd = (DataElement)_cmdDescriptorMap.get(command);
		if (cmd == null || ds != cmd.getDataStore())
		{
			cmd = getDataStore().localDescriptorQuery(subject.getDescriptor(), command);
			_cmdDescriptorMap.put(command, cmd);
		}
		return cmd;
	}
	
	public int getServerVersion()
	{
		return getDataStore().getServerVersion();
	}
	
	public int getServerMinor()
	{
		return getDataStore().getServerMinor();
	}
	
	protected void checkHostJVM()
	{
		/*
		DataElement status = getDataStore().queryHostJVM();
		String source = status.getSource();
		String[] tokens = source.split(",");
		
		long freeMem = Long.parseLong(tokens[0]);
		long totalMem = Long.parseLong(tokens[1]);

		int numElements = Integer.parseInt(tokens[3]);
		
		System.out.println("Host JVM Stats:");
		System.out.println("\tfreeMem="+freeMem);
		System.out.println("\ttotalMem="+totalMem);
	
		System.out.println("\tnumber of elements="+numElements);
		
		String[] lastCreated = tokens[4].split(";");
		System.out.println("\tlast created:");
		for (int i = 0; i < lastCreated.length; i++)
		{
			System.out.println("\t\t" + lastCreated[i]);
		}
		*/
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
		if (_initializeStatus != null)
		{
			DStoreStatusMonitor smon = getStatusMonitor(getDataStore());
			try
			{
				smon.waitForUpdate(_initializeStatus, monitor, 100);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			
			getMinerElement();
		}
	}
	
	public void initService(IProgressMonitor monitor)
	{
		initMiner(monitor);
	}
	
	public void uninitService(IProgressMonitor monitor)
	{
		_initializeStatus = null;
		_cmdDescriptorMap.clear();
		_minerElement = null;
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
						_initializeStatus = ds.activateMiner(minerId);
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
						
						getMinerElement();
						*/
					}			
				}
			}
		}
		monitor.done();
	}
	
	protected abstract String getMinerId();
	
	/**
	 * For now just a dummy method
	 * @param messageID
	 * @return
	 */
	public SystemMessage getMessage(String messageID)
	{
		return null;
	}
}