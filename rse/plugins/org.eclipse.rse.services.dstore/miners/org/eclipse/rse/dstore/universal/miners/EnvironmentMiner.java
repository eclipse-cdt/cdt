/*******************************************************************************
 * Copyright (c) 2002, 2011 IBM Corporation and others.
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
 * Noriaki Takatsu (IBM)  - [220126] [dstore][api][breaking] Single process server for multiple clients
 * David McKnight     (IBM)   [224906] [dstore] changes for getting properties and doing exit due to single-process capability
 * Noriaki Takatsu (IBM)  - [226237] [dstore] Move the place where the ServerLogger instance is made
 * David McKnight  (IBM)  - [226561] [apidoc] Add API markup to RSE Javadocs where extend / implement is allowed
 * David McKnight   (IBM) - [283613] [dstore] Create a Constants File for all System Properties we support
 *******************************************************************************/

package org.eclipse.rse.dstore.universal.miners;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.dstore.core.miners.Miner;
import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.internal.core.model.IDataStoreSystemProperties;

/**
 * The environment miner provides access to the environment variables
 * on a remote system and allows the store environment variables to be altered
 * for use in other miners that launch shells.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class EnvironmentMiner extends Miner
{
    private DataElement _system;
    
    public void load() 
    {
    	getSystemNode();
    }
    
    protected ArrayList getDependencies()
    {
	ArrayList dependencies = new ArrayList();
	return dependencies;
    } 

    private DataElement getSystemNode()
    {
    	if (_system == null)
    	{
    		_system = _dataStore.createObject(_minerData, "Environment Variable", "System Environment");	 //$NON-NLS-1$ //$NON-NLS-2$
    		_dataStore.refresh(_minerData);
    	}
    	return _system;
    }
	
    public void extendSchema(DataElement schemaRoot) 
    { 
		DataElement envVar = _dataStore.createObjectDescriptor(schemaRoot, "Environment Variable"); //$NON-NLS-1$
		_dataStore.createReference(envVar, _dataStore.createRelationDescriptor(schemaRoot,"Parent Environment")); //$NON-NLS-1$
	 	DataElement containerObjectD = _dataStore.findObjectDescriptor("Container Object"); //$NON-NLS-1$
		_dataStore.createReference(containerObjectD, envVar, "abstracts", "abstracted by"); //$NON-NLS-1$ //$NON-NLS-2$
		
		createCommandDescriptor(containerObjectD, "Set Environment Variables", "C_SET_ENVIRONMENT_VARIABLES", false); //$NON-NLS-1$ //$NON-NLS-2$
		createCommandDescriptor(containerObjectD, "Set Environment Variables", "C_SET_ENVIRONMENT_VARIABLES_NO_SYSTEM", false); //$NON-NLS-1$ //$NON-NLS-2$
	
		DataElement fsObj = _dataStore.findObjectDescriptor("Filesystem Objects"); //$NON-NLS-1$
		DataElement inhabits = _dataStore.createRelationDescriptor(schemaRoot, "inhabits"); //$NON-NLS-1$
		DataElement sustains = _dataStore.createRelationDescriptor(schemaRoot, "sustains"); //$NON-NLS-1$
		
		_dataStore.createReference(envVar, sustains);
		_dataStore.createReference(fsObj, inhabits);

		
		/*	
		 * DY:  Retreive environment values required by user define actions
		 *  - temp directory	(user.temp)
		 *  - user's home directory  (user.home)
		 */
		DataElement systemInfo = _dataStore.createObject(_minerData, "dstore.structureNode", "systemInfo"); //$NON-NLS-1$ //$NON-NLS-2$

		if (_dataStore.getClient() != null){
			_dataStore.createObject(systemInfo, "system.property", "user.home", _dataStore.getClient().getProperty("user.home")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			_dataStore.createObject(systemInfo, "system.property", "temp.dir", _dataStore.getClient().getProperty("java.io.tmpdir")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			
		}
		else {
			_dataStore.createObject(systemInfo, "system.property", "user.home", System.getProperty("user.home")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			_dataStore.createObject(systemInfo, "system.property", "temp.dir", System.getProperty("java.io.tmpdir")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		
		_dataStore.createObject(systemInfo, "system.property", "os.name", System.getProperty("os.name")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		_dataStore.createObject(systemInfo, "system.property", "os.version", System.getProperty("os.version")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
    	getSystemNode();
    	_dataStore.refresh(_minerData);
		getSystemEnvironment();	
    }
    
    public DataElement handleCommand (DataElement theElement)
    {
	String         name = getCommandName(theElement);
	DataElement  status = getCommandStatus(theElement);
	DataElement subject = getCommandArgument(theElement, 0);
	DataElement     env = getCommandArgument(theElement, 1);
	
	
	if (name.equals("C_SET_ENVIRONMENT_VARIABLES")) //$NON-NLS-1$
	{
		if (_system.getNestedSize() == 0)
		{
			getSystemEnvironment();	
		}
	    handleSetEnvironment(subject, env);
	}
	else if (name.equals("C_SET_ENVIRONMENT_VARIABLES_NO_SYSTEM")) //$NON-NLS-1$
	{
	    handleSetEnvironment(subject, env);
	}	
	
	status.setAttribute(DE.A_NAME, "done"); //$NON-NLS-1$
	return status;
    }
    
    public void handleSetEnvironment(DataElement theElement, DataElement environment)
    {
	String envName = theElement.getValue() + ".env"; //$NON-NLS-1$
	
	//First check to see if we already have an Environment for theElement..and get rid of it if we do.
	DataElement envRoot = _dataStore.find(_minerData, DE.A_NAME, envName, 1);
	if (envRoot != null)
	    {
		_dataStore.deleteObject(_minerData, envRoot);
		_dataStore.refresh(_minerData);
		List theReferences = theElement.getAssociated("inhabits"); //$NON-NLS-1$
		if (theReferences.size() > 0)
		    {
			_dataStore.deleteObject(theElement, (DataElement)theReferences.get(0));
			_dataStore.refresh(theElement);
		    }
	    }
	
	environment.setAttribute(DE.A_NAME, envName);
	environment.setAttribute(DE.A_VALUE, envName);
	
	_minerData.addNestedData(environment, false);
	environment.setParent(_minerData);
	_dataStore.refresh(_minerData);
	_dataStore.createReference(theElement, environment, "inhabits", "sustains"); //$NON-NLS-1$ //$NON-NLS-2$
	_dataStore.refresh(environment);
	_dataStore.refresh(theElement);
    }
    
    //This sucks, but the best way to get the current list of environment variables is to run the "env" (or "set" on
    //windows), and grab the output.  Can't use System.properties since this list only includes environment variables
    //that you passed in as parameters when you started the VM.
    private void getSystemEnvironment()
    {

	String envCommand  = "sh -c env"; //$NON-NLS-1$

	String theOS = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$
	//If we're on windows, change the envCommand. 
	if (theOS.startsWith("win")) //$NON-NLS-1$
	    envCommand = "cmd /C set";  //$NON-NLS-1$
	
	if (theOS.startsWith("os/400")) //$NON-NLS-1$
	{
	    envCommand = "/QOpenSys/usr/bin/sh -c env"; //$NON-NLS-1$
	}
	
	String specialEncoding= System.getProperty(IDataStoreSystemProperties.DSTORE_STDIN_ENCODING); 
	try
	    {
		Process        _process = Runtime.getRuntime().exec(envCommand);
		
		BufferedReader _output = null;
		if (specialEncoding != null)
		{
			_output  = new BufferedReader(new InputStreamReader(_process.getInputStream(), specialEncoding));
		}
		else
		{
			_output  = new BufferedReader(new InputStreamReader(_process.getInputStream()));
		}
		
		String curLine;
		while ( (curLine = _output.readLine()) != null)
		{
			int eqIndex = curLine.indexOf("="); //$NON-NLS-1$
			if (eqIndex > 0)
			{
				if (curLine.indexOf("=()") > 0) //$NON-NLS-1$
				{
					String multiLine =new String(curLine);
					
					if (!curLine.endsWith("}")) //$NON-NLS-1$
					{
						boolean complete = false;
						String subLine = null;
						while (!complete && (subLine = _output.readLine()) != null)
						{
							multiLine += subLine;
							if (subLine.indexOf('}') > -1)
							{ 
								complete = true;		
							}
						}
					}
				
					//String name = curLine.substring(0, eqIndex);
					DataElement var = _dataStore.createObject(_system, "Environment Variable", curLine, multiLine); //$NON-NLS-1$
					var.setAttribute(DE.A_VALUE, multiLine);
				}
				else
				{
				    if (curLine.startsWith("PATH=")) //$NON-NLS-1$
				    {
				        curLine += ":."; //$NON-NLS-1$
				    }
					_dataStore.createObject(_system, "Environment Variable", curLine, curLine); //$NON-NLS-1$
				}
				
			}
		}
		_dataStore.refresh(_system);
		
	    }
	catch (IOException e) 
	    {
		System.err.println("Error getting System Environment Variables\n" + e.getMessage()); //$NON-NLS-1$
	    }
    }
    
	public String getVersion()
	{
		return "6.4.0"; //$NON-NLS-1$
	}

}
