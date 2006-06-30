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

package org.eclipse.rse.dstore.universal.miners.processes;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.dstore.core.miners.miner.Miner;
import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.rse.dstore.universal.miners.IUniversalDataStoreConstants;
import org.eclipse.rse.dstore.universal.miners.UniversalServerUtilities;
import org.eclipse.rse.services.clientserver.processes.HostProcessFilterImpl;
import org.eclipse.rse.services.clientserver.processes.IHostProcess;
import org.eclipse.rse.services.clientserver.processes.ISystemProcessRemoteConstants;
import org.eclipse.rse.services.clientserver.processes.handlers.IRemoteServerProcess;
import org.eclipse.rse.services.clientserver.processes.handlers.ProcessComparator;
import org.eclipse.rse.services.clientserver.processes.handlers.ProcessHandler;
import org.eclipse.rse.services.clientserver.processes.handlers.ProcessHandlerManager;
import org.eclipse.rse.services.clientserver.processes.handlers.UniversalServerProcessImpl;

/**
 * Miner for getting process information from a remote system.
 * @author mjberger
 */
public class UniversalProcessMiner extends Miner implements IUniversalProcessDataStoreConstants, IUniversalDataStoreConstants, ISystemProcessRemoteConstants
{
	
	public static final String MINER_ID = UniversalProcessMiner.class.getName();
	
	private ProcessHandler handler;
	
	private static final String _minerVersion = "7.0.0";
	
	private DataElement deUniversalProcessObject, deUniversalProcessFilter, deKillInfoNode;
	private ProcessDEComparator _processDEcomparator;
	
	public UniversalProcessMiner()
	{
		handler = ProcessHandlerManager.getInstance().getNewProcessHandler();
	}
	 
	private ProcessDEComparator getProcessDEComparator()
	{
		if (_processDEcomparator == null)
		{
			try
			{
				_processDEcomparator = new ProcessDEComparator();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			catch (Error err)
			{
				err.printStackTrace();
			}
	
		}
		return _processDEcomparator;
	}

	public DataElement handleCommand(DataElement theCommand) 
	{
		String name = getCommandName(theCommand);
		DataElement status = getCommandStatus(theCommand);
		DataElement subject = getCommandArgument(theCommand, 0);
		
		//UniversalServerUtilities.logInfo(getMinerName(), name + ":" + subject);
				
		if (subject == null) {

			UniversalServerUtilities.logError(UNIVERSAL_PROCESS_MINER, "Subject for UniversalProcessMiner command " + name + " is null", null);
			status.setAttribute(DE.A_NAME, "done");
			return status;
		}
		
		String subjectType = (String) subject.getElementProperty(DE.P_TYPE);
		
		if (name.equals(C_PROCESS_QUERY_USERNAME))
		{
			return handleQueryUserName(subject, status);
		}
		
		if (subjectType.equals(UNIVERSAL_PROCESS_FILTER)) 
		{
			if (name.equals(C_PROCESS_FILTER_QUERY_ALL)) 
			{
				status = handleQuery(subject, status);
			} 
			else 
			{
				UniversalServerUtilities.logError(UNIVERSAL_PROCESS_MINER, "Unknown filter command: " + name, null);
				status.setAttribute(DE.A_NAME, "done");
			}
		}
		else if (subjectType.equals(UNIVERSAL_PROCESS_DESCRIPTOR)) 
		{
			if (name.equals(C_PROCESS_QUERY_ALL_PROPERTIES)) 
			{
				handleQueryJobAllProperties(subject, status);
			}
			else if (name.equals(C_PROCESS_KILL))
			{
				status = handleKill(subject, status);
			}
			else 
			{
				UniversalServerUtilities.logError(UNIVERSAL_PROCESS_MINER, "Unsupported process command: " + name, null);
				status.setAttribute(DE.A_NAME, "done");
			}
		}
		else 
		{
			UniversalServerUtilities.logError(UNIVERSAL_PROCESS_MINER, "Unsupported subject for command: " + subject, null);
			status.setAttribute(DE.A_NAME, "done");
		}
		
		return status;
	}
	
	/**
	 * Get the username
	 */
	protected DataElement handleQueryUserName(DataElement subject, DataElement status) {

		String encoding = System.getProperty("user.name");

		subject.setAttribute(DE.A_VALUE, encoding);
		_dataStore.refresh(subject);

		status.setAttribute(DE.A_NAME, "done");
		_dataStore.refresh(status);
		return status;
	}

	public void extendSchema(DataElement schemaRoot) 
	{
		
		// define process descriptors
		deUniversalProcessFilter = createObjectDescriptor(schemaRoot,
				UNIVERSAL_PROCESS_FILTER);
		deUniversalProcessObject = createObjectDescriptor(schemaRoot,
				UNIVERSAL_PROCESS_DESCRIPTOR);
		DataElement tempnode = createObjectDescriptor(schemaRoot, UNIVERSAL_PROCESS_TEMP);
		
        // define command descriptors
		createCommandDescriptor(deUniversalProcessFilter, "Filter", C_PROCESS_FILTER_QUERY_ALL);
		createCommandDescriptor(deUniversalProcessObject, "Kill", C_PROCESS_KILL);
		createCommandDescriptor(deUniversalProcessObject, "ProcessQueryAllProperties", C_PROCESS_QUERY_ALL_PROPERTIES);
		createCommandDescriptor(tempnode, "QueryUsername", C_PROCESS_QUERY_USERNAME);

		_dataStore.refresh(schemaRoot);
	}

	public void load()
	{
		deKillInfoNode = _dataStore.createObject(_minerData, UNIVERSAL_NODE_DESCRIPTOR, "universal.killinfo");
		deKillInfoNode.setAttribute(DE.A_VALUE, getSignalTypes());
		_dataStore.refresh(_minerData);
	
	}
	
	/**
	 * @return a String with a list of signal types that can be sent to processes on this
	 * system, separated by newline characters.
	 */
	protected String getSignalTypes()
	{
		try
		{
			Process kill = Runtime.getRuntime().exec("kill -l");
			InputStreamReader isr = new InputStreamReader(kill.getInputStream());
			if (isr == null) return "";
			BufferedReader reader = new BufferedReader(isr);
			if (reader == null) return "";
			String nextLine = reader.readLine();
			String output = "";
			while (nextLine != null)
			{
				output = output + nextLine + "\n";
				nextLine = reader.readLine();
			}
			reader.close();
			isr.close();
			return output;
		}
		catch (Exception e)
		{
			return "";
		}
	}
	
	/**
	 * Handle process queries
	 */
	private DataElement handleQuery(DataElement subject, DataElement status) 
	{
		try 
		{
			HostProcessFilterImpl pfs = new HostProcessFilterImpl(subject.getSource());
			lookupProcesses(pfs, subject);
		} catch (Exception e) {
			e.printStackTrace();
			UniversalServerUtilities.logError("UniversalProcessMiner", "handleQuery()", e);
			status.setAttribute(DE.A_VALUE, e.getMessage());
			status.setAttribute(DE.A_NAME, "done");
			_dataStore.refresh(status);
			return status;
		}

		_dataStore.refresh(subject);
		status.setAttribute(DE.A_NAME, "done");
		status.setAttribute(DE.A_VALUE, PROCESS_MINER_SUCCESS);

		return status;
	}
	
	private void lookupProcesses(HostProcessFilterImpl fsObj, DataElement subject) throws Exception 
	{
		// we need to synchronize the list of data elements with the fresh
		// results coming back from the query
		if (handler == null) throw new Exception(PROCESS_MINER_ERROR_NO_HANDLER);
		SortedSet processes = handler.lookupProcesses(fsObj);
		
		// sort the data elements
		SortedSet sortedDEs = null;
		List nested = subject.getNestedData();
		if (nested != null)
		{
			Object[] oldDEs = nested.toArray();
			if (oldDEs != null && oldDEs.length > 0)
			{
				sortedDEs = new TreeSet(getProcessDEComparator());
				for (int i = 0; i < oldDEs.length; i++)
				{
					if (fsObj.allows(((DataElement)oldDEs[i]).getValue()))
						sortedDEs.add(oldDEs[i]);
				}
			}
		}
		
		// take care of the special cases where one of the lists has no
		// elements
		if (processes == null || processes.size() == 0)
		{
			deleteDataElements(fsObj, subject);
			return;
		}
		if (sortedDEs == null || sortedDEs.size() == 0)
		{
			createDataElements(processes.toArray(), subject);
			return;
		}
		
		// now we merge the lists
		Iterator pIter = processes.iterator();
		Iterator deIter = sortedDEs.iterator();

		DataElement nextDE = null;
		IRemoteServerProcess nextP = null;
		IRemoteServerProcess nextDEP = null;
		try
		{
			// pop the first two elements off the lists
			ProcessComparator comparator = new ProcessComparator();
			nextDE = (DataElement) deIter.next();
			nextDEP = new UniversalServerProcessImpl(nextDE.getValue());
			nextP = (IRemoteServerProcess) pIter.next();
			do
			{
				// compare the pid's of the elements
				int comparison = comparator.compare(nextDEP, nextP);
				
				// data element exists in datastore but no longer returned in query,
				// so delete it, and pop it off the list
				if (comparison < 0)
				{
					_dataStore.deleteObject(subject, nextDE);
					deIter.remove();
					nextDE = (DataElement) deIter.next();
					nextDEP = new UniversalServerProcessImpl(nextDE.getValue());

				}
				
				// data element exists in both the query and datastore, so refresh its properties
				// and then pop both top items off their lists
				if (comparison == 0)
				{
					nextDE.setAttribute(DE.A_VALUE, nextP.getAllProperties());
					deIter.remove();
					pIter.remove();
					nextDE = (DataElement) deIter.next();
					nextDEP = new UniversalServerProcessImpl(nextDE.getValue());
					nextP = (IRemoteServerProcess) pIter.next();
				}
				
				// data element does not exist in the data store, so create it
				if (comparison > 0)
				{
					createDataElement(nextP, subject);
					pIter.remove();
					nextP = (IRemoteServerProcess) pIter.next();
				}
			} 
			while(true);
		}
		catch (NoSuchElementException e) // we have reached the tail of one of the lists, add or delete the rest
		{
			if (!pIter.hasNext() && !deIter.hasNext()) return;
			if (deIter.hasNext()) deleteRemainingElements(deIter, subject);
			else createDataElements(processes.toArray(), subject);
		}
	}
	
	private void deleteRemainingElements(Iterator iter, DataElement subject)
	{
		while (iter.hasNext())
		{
			DataElement next = (DataElement) iter.next();
			_dataStore.deleteObject(subject, next);
		}
	}
	
	private void deleteDataElements(HostProcessFilterImpl fsObj, DataElement subject)
	{
		List nested = subject.getNestedData();
		if (nested != null)
		{
			Object[] oldDEs = nested.toArray();
			if (oldDEs == null || oldDEs.length == 0) return;
			for (int i = 0; i < oldDEs.length; i++)
			{
				DataElement currentDE = (DataElement) oldDEs[i];
				if (fsObj.allows(currentDE.getValue()))
				_dataStore.deleteObject(subject, currentDE);
			}
		}
	}
	
	private void createDataElements(Object[] processes, DataElement subject)
	{
		if (processes == null || processes.length == 0) return;
		for (int i = 0; i < processes.length; i++)
		{
			createDataElement((IRemoteServerProcess)processes[i], subject);
		}		
	}
	
	private void createDataElement(IRemoteServerProcess process, DataElement subject)
	{
		DataElement dsObj = null;
		dsObj = _dataStore.createObject(subject, deUniversalProcessObject, "" + process.getPid());
		dsObj.setAttribute(DE.A_VALUE, process.getAllProperties());			
	}
	/**
	 * Query all properties of the process.
	 */
	private void handleQueryJobAllProperties(DataElement subject, DataElement status) 
	{
	}

	/**
	 * Kill a process.
	 */
	private DataElement handleKill(DataElement subject, DataElement status) 
	{
		try 
		{
			String statusLine = subject.getValue();
			UniversalServerProcessImpl usp = new UniversalServerProcessImpl(statusLine);
			if (handler == null) throw new Exception(PROCESS_MINER_ERROR_NO_HANDLER);
			IHostProcess result = handler.kill(usp, subject.getSource());
			
			if (result == null) _dataStore.deleteObject(subject.getParent(), subject);
			else
			{
				subject.setAttribute(DE.A_SOURCE, "");
				subject.setAttribute(DE.A_VALUE, result.getAllProperties());
			}	
		} catch (Exception e) {
			UniversalServerUtilities.logError("UniversalProcessMiner", "handleQuery()", e);
			status.setAttribute(DE.A_VALUE, e.getMessage());
			status.setAttribute(DE.A_NAME, "done");
			_dataStore.refresh(status);
			return status;
		}

		_dataStore.refresh(subject.getParent());
		status.setAttribute(DE.A_NAME, "done");
		status.setAttribute(DE.A_VALUE, PROCESS_MINER_SUCCESS);
		return status;		
	}

	public String getVersion()
	{
		return _minerVersion;
	}
}