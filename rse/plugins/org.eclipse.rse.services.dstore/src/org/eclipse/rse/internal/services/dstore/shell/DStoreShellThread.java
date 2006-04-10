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

package org.eclipse.rse.internal.services.dstore.shell;

import java.io.File;

import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.dstore.core.model.DataStoreSchema;
import org.eclipse.rse.dstore.universal.miners.command.CommandMiner;
import org.eclipse.rse.dstore.universal.miners.environment.EnvironmentMiner;



public class DStoreShellThread
{
	protected DataElement _runCmdDescriptor;
	protected DataElement _runShellDescriptor;
	protected DataElement _setEnvironmentDescriptor;
	protected DataElement _sendInputDescriptor;
	protected DataElement _cmdMinerElement;
	protected DataElement _envMinerElement;
	
	private String _encoding;
	private String _cwd;
	private String[] _envVars;
	private DataStore _dataStore;
	private DataElement _status;
	
	/**
	 * @param cwd initial working directory
	 * @param invocation launch shell command
	 * @param encoding
	 * @param patterns patterns file for output interpretation
	 * @param envVars user and system environment variables to launch shell with
	 */
	public DStoreShellThread(DataStore dataStore, String cwd, String invocation, String encoding, String[] envVars)
	{
		super();
		_dataStore = dataStore;
		_encoding = encoding;
		_cwd = cwd;
		_envVars = envVars;
		init();
	}
	
	protected void init()
	{
		// make this subsystem a communications listener
		DataElement contextDir = _dataStore.createObject(null, "directory", (new File(_cwd)).getName(), _cwd);
		_dataStore.setObject(contextDir);
		setRemoteEnvironment(contextDir);

		sendShellToMiner(contextDir);
	}
	
	public DataElement getStatus()
	{
		return _status;
	}
	
	protected void sendShellToMiner(DataElement contextDir)
	{
		DataElement cmdD = getRunShellDescriptor(contextDir);

		if (cmdD != null)
		{
			String encoding = _encoding;
			if (encoding != null && encoding.length() > 0)
			{			
				DataElement arg = _dataStore.createObject(null, "shell.encoding", encoding);
				_status = _dataStore.command(cmdD, arg, contextDir);
			}
			else
			{
				_status = _dataStore.command(cmdD, contextDir);
			}
		}
	}
	
	/**
	 * Set the environment variables for this connection.  For universal this sets them in the
	 * DataStore tree.  When a new shell is launched the environment variables are passed to the
	 * shell.
	 */
	public void setRemoteEnvironment(DataElement theObject)
	{
		
		if (_envVars != null && _envVars.length > 0)
		{
			DataElement theEnvironment = _dataStore.createObject(null, "Environment Variable", theObject.getName());
			for (int i = 0; i < _envVars.length; i++)
			{
				String var = _envVars[i];
				_dataStore.createObject(theEnvironment, "Environment Variable", var, var);
			}
	
			theEnvironment.setAttribute(DE.A_NAME, theObject.getId());
			DataElement contObj = _dataStore.findObjectDescriptor("Container Object");
			DataElement setD = getSetEnvironmentDescriptor(contObj);
			if (setD != null)
			{
				_dataStore.command(setD, theEnvironment, theObject, false);
			}
		}
	}
	

	protected DataElement getRunCommandDescriptor(DataElement remoteObject)
	{
	    if (_runCmdDescriptor == null || _dataStore != remoteObject.getDataStore())
	    {
	        _runCmdDescriptor = _dataStore.localDescriptorQuery(remoteObject.getDescriptor(), getRunCommandId());
	    }
	    return _runCmdDescriptor;
	}
	
	protected DataElement getRunShellDescriptor(DataElement remoteObject)
	{
	    if (_runShellDescriptor == null || _dataStore != remoteObject.getDataStore())
	    {
	        _runShellDescriptor = _dataStore.localDescriptorQuery(remoteObject.getDescriptor(), getRunShellId(), 2);
	    }
	    return _runShellDescriptor;
	}
	
	protected DataElement getSetEnvironmentDescriptor(DataElement remoteObject)
	{
	    if (_setEnvironmentDescriptor == null || _dataStore != remoteObject.getDataStore())
	    {
	        _setEnvironmentDescriptor = _dataStore.localDescriptorQuery(remoteObject.getDescriptor(), getSetEnvironmentId(), 2);
	    }
	    return _setEnvironmentDescriptor;
	}	
	
	
	protected DataElement getSendInputDescriptor(DataElement remoteObject)
	{
	    if (_sendInputDescriptor == null || _dataStore != remoteObject.getDataStore())
	    {
	        _sendInputDescriptor = _dataStore.findCommandDescriptor(DataStoreSchema.C_SEND_INPUT);
	    }
	
	    return _sendInputDescriptor;
	    }
	
	
	protected DataElement getCmdSystemMinerElement()
	{

	    if (_cmdMinerElement == null || _cmdMinerElement.getDataStore() != _dataStore)
	    {
	        _cmdMinerElement = _dataStore.findMinerInformation(getCmdSystemMinerId());
	        if (_cmdMinerElement == null)
	        {
	        	_cmdMinerElement = _dataStore.findMinerInformation(getOldCmdSystemMinerId());
	        }
	    }
	    return _cmdMinerElement;
	}
	
	protected DataElement getEnvSystemMinerElement()
	{

	    if (_envMinerElement == null || _envMinerElement.getDataStore() != _dataStore)
	    {
	        _envMinerElement = _dataStore.findMinerInformation(getEnvSystemMinerId());
	        
	        if (_envMinerElement == null)
	        {
	        	_envMinerElement = _dataStore.findMinerInformation(getOldEnvSystemMinerId());
	        }
	    }
	    return _envMinerElement;
	}
	

	protected String getCmdSystemMinerId()
	{
		return CommandMiner.MINER_ID;
	}
	
	protected String getOldCmdSystemMinerId()
	{
		return "com.ibm.etools.systems.dstore.miners.command.CommandMiner";
	}
	
	protected String getEnvSystemMinerId()
	{
		return EnvironmentMiner.MINER_ID;
	}
	
	protected String getRunShellId()
	{
		return "C_SHELL";
	}

	protected String getRunCommandId()
	{
		return "C_COMMAND";
	}

	protected String getSetEnvironmentId()
	{
		return "C_SET_ENVIRONMENT_VARIABLES";
	}
	
	protected String getOldEnvSystemMinerId()
	{
		return "com.ibm.etools.systems.dstore.miners.environment.EnvironmentMiner";
	}
	
	public void writeToShell(String command)
	{
		DataElement commandElement = _status.getParent();
		DataStore dataStore = commandElement.getDataStore();

		if (command.equals("") || command.equals("#break"))
		{
		    String cmd = command;
		    if (cmd.equals(""))
		        cmd = "#enter";
			DataElement commandDescriptor = getSendInputDescriptor(commandElement);
			if (commandDescriptor != null)
			{
				DataElement in = dataStore.createObject(null, "input", cmd);
				dataStore.command(commandDescriptor, in, commandElement);
			}
		}
		else
		{
		    String[] tokens = command.split("\n\r");
			for (int i = 0; i <tokens.length; i++)
			{
			    String cmd = tokens[i];

				if (cmd != null)
				{	
				    DataElement commandDescriptor = getSendInputDescriptor(commandElement);
					if (commandDescriptor != null)
					{
						DataElement in = dataStore.createObject(null, "input", cmd);
						dataStore.command(commandDescriptor, in, commandElement);
					}
				}
			}
		}
	}
}