/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
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
 * Martin Oberhuber (Wind River) - [186128][refactoring] Move IProgressMonitor last in public base classes
 * David McKnight   (IBM)        - [190803] Canceling a long-running dstore job prints "InterruptedException" to stdout
 * David McKnight   (IBM)        - [207095] check for null datastore
 * David McKnight   (IBM)        - [209593] [api] check for existing query to avoid duplicates
 * David McKnight   (IBM)        - [216252] use SimpleSystemMessage instead of getMessage()
 * Martin Oberhuber (Wind River) - [226262] Make IService IAdaptable
 * David McKnight   (IBM)        - [272882] [api] Handle exceptions in IService.initService()
 *******************************************************************************/

package org.eclipse.rse.services.dstore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.dstore.core.model.IDataStoreProvider;
import org.eclipse.rse.internal.services.dstore.ServiceResources;
import org.eclipse.rse.services.AbstractService;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.dstore.util.DStoreStatusMonitor;

public abstract class AbstractDStoreService extends AbstractService implements IDStoreService
{
	protected IDataStoreProvider _dataStoreProvider;
	protected DataElement _minerElement;
	protected DStoreStatusMonitor _statusMonitor;
	protected Map _cmdDescriptorMap;
	protected DataElement _initializeStatus;

	/**
	 * @since 3.0 got rid of ISystemMessageProvider argument
	 * @param dataStoreProvider
	 */
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
		DataStore ds = getDataStore();
		if (ds != null)
		{
			return ds.findMinerInformation(id);
		}
		else
		{
			return null;
		}
	}

	protected DataElement[] dsQueryCommand(DataElement subject, String command, IProgressMonitor monitor)
	{
		return dsQueryCommand(subject, null, command, monitor);
	}

	/**
	 * query the the remote system
	 * @param subject the subject of the query
	 * @param args the arguments for the query
	 * @param command the query command
	 * @param monitor
	 * @return the array of results
	 */
	protected DataElement[] dsQueryCommand(DataElement subject, ArrayList args, String command, IProgressMonitor monitor)
	{
		// query roots
		DataElement queryCmd = getCommandDescriptor(subject, command);
		DataStore ds = getDataStore();
		DStoreStatusMonitor smonitor = getStatusMonitor(ds);

		if (queryCmd != null && ds != null)
		{
			// check if there already is an active command for this query
			DataElement status = smonitor.getCommandStatus(queryCmd, subject);
			if (args != null)
			{
				status = ds.command(queryCmd, args, subject, true);
			}
			else
			{
				status = ds.command(queryCmd, subject, true);
			}
			try
			{
				smonitor.waitForUpdate(status, monitor);

				int resultSize = subject.getNestedSize();

				checkHostJVM();
				// get results
				List nested = subject.getNestedData();
				if (nested != null)
				{
					return (DataElement[])nested.toArray(new DataElement[resultSize]);
				}
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
		}

		return new DataElement[0];
	}

	/** @since 3.0 */
	protected List dsQueryCommandMulti(DataElement[] subjects, String[] commands, IProgressMonitor monitor)
	{
		return dsQueryCommandMulti(subjects, null, commands, monitor);
	}

	/**
	 * Query multiple subjects in one shot
	 * 
	 * @param subjects the subjects to query
	 * @param commands the query commands
	 * @param argses arguments for the command - may be null
	 * @param monitor the progress monitor
	 * @return a list of DataElement[]s containing the results of each query
	 * @since 3.0
	 */
	protected List dsQueryCommandMulti(DataElement[] subjects, ArrayList[] argses, String[] commands, IProgressMonitor monitor)
	{
		List statuses = new ArrayList();
		DataStore ds = getDataStore();
		DStoreStatusMonitor smonitor = getStatusMonitor(ds);


		for (int i = 0; i < subjects.length && !monitor.isCanceled(); i++)
		{
			DataElement subject = subjects[i];

			DataElement queryCmd = getCommandDescriptor(subject, commands[i]);
			if (queryCmd != null && ds != null)
			{
				// check if there already is an active command for this query
				DataElement status = smonitor.getCommandStatus(queryCmd, subject);

				if (status == null){
					if (argses != null){
						status = ds.command(queryCmd, argses[i], subject, true);
					}
					else{
						status = ds.command(queryCmd, subject, true);
					}
				}
				statuses.add(status);
			}
		}

		List consolidatedResults = new ArrayList();

		// wait for each command to complete
		for (int i = 0; i < statuses.size() && !monitor.isCanceled(); i++)
		{
		    DataElement status = (DataElement)statuses.get(i);
		    DataElement deObj = subjects[i];

		    try
		    {
		    	smonitor.waitForUpdate(status, monitor);

			    if (!monitor.isCanceled() && smonitor.determineStatusDone(status))
			    {
			    	List nested = deObj.getNestedData();
			    	if (nested != null)
			    	{
			    		consolidatedResults.add(nested.toArray(new DataElement[nested.size()]));
			    	}
			    }
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
		}

		return consolidatedResults;
	}






	protected DataElement dsStatusCommand(DataElement subject, ArrayList args, String command, IProgressMonitor monitor)
	{
		DataStore ds = getDataStore();
		DStoreStatusMonitor smonitor = getStatusMonitor(ds);

		DataElement queryCmd = getCommandDescriptor(subject, command);

		if (queryCmd != null && ds != null)
		{
			// check if there already is an active command for this query
			DataElement status = smonitor.getCommandStatus(queryCmd, subject);

			if (status == null){
				status = ds.command(queryCmd, args, subject, true);
			}
			try
			{
				smonitor.waitForUpdate(status, monitor);
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
			return status;
		}
		return null;
	}


	protected DataElement dsStatusCommand(DataElement subject, String command, IProgressMonitor monitor)
	{
		DataStore ds = getDataStore();
		DStoreStatusMonitor smonitor = getStatusMonitor(ds);

		DataElement queryCmd = getCommandDescriptor(subject, command);

		if (queryCmd != null && ds != null)
		{
			// check if there already is an active command for this query
			DataElement status = smonitor.getCommandStatus(queryCmd, subject);

			if (status == null){
				status = ds.command(queryCmd, subject, true);
			}
			try
			{
				smonitor.waitForUpdate(status, monitor);
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
			if (ds != null)
			{
				cmd = ds.localDescriptorQuery(subject.getDescriptor(), command);
				_cmdDescriptorMap.put(command, cmd);
			}
		}
		return cmd;
	}

	public int getServerVersion()
	{
		DataStore ds = getDataStore();
		if (ds != null)
			return ds.getServerVersion();
		return 0;
	}

	public int getServerMinor()
	{
		DataStore ds = getDataStore();
		if (ds != null)
			return ds.getServerMinor();
		return 0;
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
			DataStore ds = getDataStore();
			if (ds != null)
			{
				DStoreStatusMonitor smon = getStatusMonitor(ds);
				return smon.determineStatusDone(_initializeStatus);
			}
		}
		return false;
	}

	protected void waitForInitialize(IProgressMonitor monitor)
	{
		if (_initializeStatus != null)
		{
			DataStore ds = getDataStore();
			if (ds != null)
			{
				DStoreStatusMonitor smon = getStatusMonitor(getDataStore());
				try
				{
					smon.waitForUpdate(_initializeStatus, monitor, 100);
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

				getMinerElement();
			}
		}
	}

	public void initService(IProgressMonitor monitor) throws SystemMessageException
	{
		super.initService(monitor);
		initMiner(monitor);
	}

	public void uninitService(IProgressMonitor monitor)
	{
		_initializeStatus = null;
		_cmdDescriptorMap.clear();
		_minerElement = null;
		_statusMonitor = null;
		super.uninitService(monitor);
	}

	protected void initMiner(IProgressMonitor monitor)
	{
		DataElement fsElement = getMinerElement();
		if (fsElement == null)
		{
			if (getServerVersion() >= 8)
			{
				String minerId = getMinerId();
				String message = SystemMessage.sub(ServiceResources.DStore_Service_ProgMon_Initializing_Message, "&1", minerId); //$NON-NLS-1$
				monitor.beginTask(message, IProgressMonitor.UNKNOWN);
				DataStore ds = getDataStore();
				if (_minerElement == null || _minerElement.getDataStore() != ds)
				{
					if (ds != null && _initializeStatus == null)
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

}
