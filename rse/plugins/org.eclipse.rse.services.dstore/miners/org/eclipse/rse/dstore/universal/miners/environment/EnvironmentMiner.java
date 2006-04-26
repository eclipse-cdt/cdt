/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.dstore.universal.miners.environment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.dstore.core.miners.miner.Miner;
import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;

public class EnvironmentMiner extends Miner
{
	public static final String MINER_ID = EnvironmentMiner.class.getName();//"org.eclipse.rse.dstore.universal.miners.environment.EnvironmentMiner";

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
    		_system = _dataStore.createObject(_minerData, "Environment Variable", "System Environment");	
    		_dataStore.refresh(_minerData);
    	}
    	return _system;
    }
	
    public void extendSchema(DataElement schemaRoot) 
    { 
		DataElement envVar = _dataStore.createObjectDescriptor(schemaRoot, "Environment Variable");
		_dataStore.createReference(envVar, _dataStore.createRelationDescriptor(schemaRoot,"Parent Environment"));
	 	DataElement containerObjectD = _dataStore.findObjectDescriptor("Container Object");
		_dataStore.createReference(containerObjectD, envVar, "abstracts", "abstracted by");
		
		createCommandDescriptor(containerObjectD, "Set Environment Variables", "C_SET_ENVIRONMENT_VARIABLES", false);
		createCommandDescriptor(containerObjectD, "Set Environment Variables", "C_SET_ENVIRONMENT_VARIABLES_NO_SYSTEM", false);
	
		DataElement fsObj = _dataStore.findObjectDescriptor("Filesystem Objects");
		DataElement inhabits = _dataStore.createRelationDescriptor(schemaRoot, "inhabits");
		DataElement sustains = _dataStore.createRelationDescriptor(schemaRoot, "sustains");
		
		_dataStore.createReference(envVar, sustains);
		_dataStore.createReference(fsObj, inhabits);

		
		/*	
		 * DY:  Retreive environment values required by user define actions
		 *  - temp directory	(user.temp)
		 *  - user's home directory  (user.home)
		 */
		DataElement systemInfo = _dataStore.createObject(_minerData, "dstore.structureNode", "systemInfo");

		_dataStore.createObject(systemInfo, "system.property", "user.home", System.getProperty("user.home"));
		_dataStore.createObject(systemInfo, "system.property", "temp.dir", System.getProperty("java.io.tmpdir"));
		
		_dataStore.createObject(systemInfo, "system.property", "os.name", System.getProperty("os.name"));
		_dataStore.createObject(systemInfo, "system.property", "os.version", System.getProperty("os.version"));
		
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
	
	
	if (name.equals("C_SET_ENVIRONMENT_VARIABLES"))
	{
		if (_system.getNestedSize() == 0)
		{
			getSystemEnvironment();	
		}
	    handleSetEnvironment(subject, env);
	}
	else if (name.equals("C_SET_ENVIRONMENT_VARIABLES_NO_SYSTEM"))
	{
	    handleSetEnvironment(subject, env);
	}	
	
	status.setAttribute(DE.A_NAME, "done");
	return status;
    }
    
    public void handleSetEnvironment(DataElement theElement, DataElement environment)
    {
	String envName = theElement.getValue() + ".env";
	
	//First check to see if we already have an Environment for theElement..and get rid of it if we do.
	DataElement envRoot = _dataStore.find(_minerData, DE.A_NAME, envName, 1);
	if (envRoot != null)
	    {
		_dataStore.deleteObject(_minerData, envRoot);
		_dataStore.refresh(_minerData);
		List theReferences = theElement.getAssociated("inhabits");
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
	_dataStore.createReference(theElement, environment, "inhabits", "sustains");
	_dataStore.refresh(environment);
	_dataStore.refresh(theElement);
    }
    
    //This sucks, but the best way to get the current list of environment variables is to run the "env" (or "set" on
    //windows), and grab the output.  Can't use System.properties since this list only includes environment variables
    //that you passed in as parameters when you started the VM.
    private void getSystemEnvironment()
    {

	String envCommand  = "sh -c env";

	String theOS = System.getProperty("os.name").toLowerCase();
	//If we're on windows, change the envCommand. 
	if (theOS.startsWith("win"))
	    envCommand = "cmd /C set"; 
	
	if (theOS.startsWith("os/400"))
	{
	    envCommand = "/QOpenSys/usr/bin/sh -c env";
	}
	
	String specialEncoding= System.getProperty("dstore.stdin.encoding");
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
			int eqIndex = curLine.indexOf("=");
			if (eqIndex > 0)
			{
				if (curLine.indexOf("=()") > 0)
				{
					String multiLine =new String(curLine);
					
					if (!curLine.endsWith("}"))
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
					DataElement var = _dataStore.createObject(_system, "Environment Variable", curLine, multiLine);
					var.setAttribute(DE.A_VALUE, multiLine);
				}
				else
				{
				    if (curLine.startsWith("PATH="))
				    {
				        curLine += ":.";
				    }
					_dataStore.createObject(_system, "Environment Variable", curLine, curLine);
				}
				
			}
		}
		_dataStore.refresh(_system);
		
	    }
	catch (IOException e) 
	    {
		System.err.println("Error getting System Environment Variables\n" + e.getMessage());
	    }
    }
    
	public String getVersion()
	{
		return "6.4.0";
	}
}