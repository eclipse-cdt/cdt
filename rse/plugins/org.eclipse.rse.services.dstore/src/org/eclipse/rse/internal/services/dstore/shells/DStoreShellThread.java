/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
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
 * {Name} (company) - description of contribution.
 * David McKnight   (IBM)        - [196624] dstore miner IDs should be String constants rather than dynamic lookup
 * David McKnight (IBM) - [286671] Dstore shell service interprets &lt; and &gt; sequences
 *******************************************************************************/

package org.eclipse.rse.internal.services.dstore.shells;

import java.io.File;

import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.dstore.core.model.DataStoreResources;
import org.eclipse.dstore.core.model.DataStoreSchema;
import org.eclipse.rse.dstore.universal.miners.IUniversalDataStoreConstants;



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
	private String _invocation;
	
	/**
	 * @param cwd initial working directory
	 * @param invocation launch shell command
	 * @param encoding
	 * @param envVars user and system environment variables to launch shell with
	 */
	public DStoreShellThread(DataStore dataStore, String cwd, String invocation, String encoding, String[] envVars)
	{
		super();
		_dataStore = dataStore;
		_encoding = encoding;
		_cwd = cwd;
		_envVars = envVars;
		_invocation = invocation;
		init();
	}
	
	protected void init()
	{
		// make this subsystem a communications listener
		DataElement contextDir = _dataStore.createObject(null, "directory", (new File(_cwd)).getName(), _cwd); //$NON-NLS-1$
		_dataStore.setObject(contextDir);
		setRemoteEnvironment(contextDir);
		if (_invocation== null || _invocation.equals(">")) //$NON-NLS-1$
		{
			sendShellToMiner(contextDir);
		}
		else
		{
			sendCommandToMiner(contextDir, _invocation);
			
		}
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
				DataElement arg = _dataStore.createObject(null, "shell.encoding", encoding); //$NON-NLS-1$
				_status = _dataStore.command(cmdD, arg, contextDir);
			}
			else
			{
				_status = _dataStore.command(cmdD, contextDir);
			}
		}
	}
	
	protected void sendCommandToMiner(DataElement contextDir, String invocation)
	{
		DataElement cmdD = getRunCommandDescriptor(contextDir);

		if (cmdD != null)
		{

			if (invocation != null && invocation.length() > 0)
			{			
				DataElement arg = _dataStore.createObject(null, "command", invocation); //$NON-NLS-1$
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
			DataElement theEnvironment = _dataStore.createObject(null, "Environment Variable", theObject.getName()); //$NON-NLS-1$
			for (int i = 0; i < _envVars.length; i++)
			{
				String var = _envVars[i];
				_dataStore.createObject(theEnvironment, "Environment Variable", var, var); //$NON-NLS-1$
			}
	
			theEnvironment.setAttribute(DE.A_NAME, theObject.getId());
			DataElement contObj = _dataStore.findObjectDescriptor("Container Object"); //$NON-NLS-1$
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
	    }
	    return _cmdMinerElement;
	}
	
	protected DataElement getEnvSystemMinerElement()
	{

	    if (_envMinerElement == null || _envMinerElement.getDataStore() != _dataStore)
	    {
	        _envMinerElement = _dataStore.findMinerInformation(getEnvSystemMinerId());
	        
	    }
	    return _envMinerElement;
	}
	

	protected String getCmdSystemMinerId()
	{
		return IUniversalDataStoreConstants.UNIVERSAL_COMMAND_MINER_ID;
	}
	
	
	protected String getEnvSystemMinerId()
	{
		return IUniversalDataStoreConstants.UNIVERSAL_ENVIRONMENT_MINER_ID;
	}
	
	protected String getRunShellId()
	{
		return "C_SHELL"; //$NON-NLS-1$
	}

	protected String getRunCommandId()
	{
		return "C_COMMAND"; //$NON-NLS-1$
	}

	protected String getSetEnvironmentId()
	{
		return "C_SET_ENVIRONMENT_VARIABLES"; //$NON-NLS-1$
	}

	
	public void writeToShell(String command)
	{
		DataElement commandElement = _status.getParent();
		DataStore dataStore = commandElement.getDataStore();

		if (command.equals("") || command.equals("#break")) //$NON-NLS-1$ //$NON-NLS-2$
		{
		    String cmd = command;
		    if (cmd.equals("")) //$NON-NLS-1$
		        cmd = "#enter"; //$NON-NLS-1$
			DataElement commandDescriptor = getSendInputDescriptor(commandElement);
			if (commandDescriptor != null)
			{
				DataElement in = dataStore.createObject(null, "input", cmd); //$NON-NLS-1$
				dataStore.command(commandDescriptor, in, commandElement);
			}
		}
		else
		{
		    String[] tokens = command.split("\n\r"); //$NON-NLS-1$
			for (int i = 0; i <tokens.length; i++)
			{
			    String cmd = tokens[i];

				if (cmd != null)
				{	
					// first, find out if the server support conversion
					DataElement fsD= dataStore.findObjectDescriptor(DataStoreResources.model_directory);
					DataElement convDes = dataStore.localDescriptorQuery(fsD, "C_CHAR_CONVERSION", 1); //$NON-NLS-1$
					if (convDes != null){
						cmd = convertSpecialCharacters(cmd);
					}
					
				    DataElement commandDescriptor = getSendInputDescriptor(commandElement);
					if (commandDescriptor != null)
					{
						DataElement in = dataStore.createObject(null, "input", cmd); //$NON-NLS-1$
						dataStore.command(commandDescriptor, in, commandElement);
					}
				}
			}
		}
	}
	
	private String convertSpecialCharacters(String input){
	   // needed to ensure xml characters aren't converted in xml layer	
	
		StringBuffer output = new StringBuffer();

		for (int idx = 0; idx < input.length(); idx++)
		{
			char currChar = input.charAt(idx);
			switch (currChar)
			{
			case '&' :
				output.append("&#38;");
				break;
			case ';' :
				output.append("&#59;");
				break;
			default :
				output.append(currChar);
				break;
			}
		}
		return output.toString();
	}
}
