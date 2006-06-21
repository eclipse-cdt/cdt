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

package org.eclipse.rse.dstore.universal.miners.command;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.dstore.core.miners.miner.Miner;
import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStoreAttributes;
import org.eclipse.dstore.core.model.DataStoreResources;
import org.eclipse.rse.dstore.universal.miners.command.patterns.Patterns;
import org.eclipse.rse.dstore.universal.miners.environment.EnvironmentMiner;



/**
 * The CommandMiner provides the ability to run remote interactive shell 
 * commands via the DataStore.
 */
public class CommandMiner extends Miner
{
	public static final String MINER_ID = CommandMiner.class.getName();//"org.eclipse.rse.dstore.universal.miners.command.CommandMiner";

    public class CommandMinerDescriptors
    {
        public DataElement _stdout;
        public DataElement _stderr;
        public DataElement _prompt;
        public DataElement _grep;
        public DataElement _pathenvvar;
        public DataElement _envvar;
        public DataElement _libenvvar;
        public DataElement _error;
        public DataElement _warning;
        public DataElement _informational;
        public DataElement _process;
        
        public DataElement getDescriptorFor(String type)
        {
            DataElement descriptor = null;
            if (type.equals("stdout"))
            {
                descriptor = _stdout;
            }
            else if  (type.equals("pathenvvar"))
            {
                descriptor = _pathenvvar;
            }
            else if (type.equals("envvar"))
            {
                descriptor = _envvar;
            }
            else if  (type.equals("libenvvar"))
            {
                descriptor = _libenvvar;
            }
            else if (type.equals("error"))
            {
                descriptor = _error;
            }
            else if  (type.equals("warning"))
            {
                descriptor = _warning;
            }
            else if (type.equals("informational"))
            {
                descriptor = _informational;
            }
            else if  (type.equals("process"))
            {
                descriptor = _process;
            }
            else if (type.equals("grep"))
            {
                descriptor = _grep;
            }
            else if (type.equals("stderr"))
            {
                descriptor = _stderr;
            }
            return descriptor;
        }
    }
    
	private HashMap _threads = new HashMap();
	private Patterns _patterns;
	private CommandMinerDescriptors _descriptors;
	
	
	public Patterns getPatterns()
	{
		if (_patterns == null)
		{
			_patterns = new Patterns(_dataStore);
		}
		return _patterns;
	}

	protected ArrayList getDependencies()
	{
		ArrayList dependencies = new ArrayList();
		dependencies.add(EnvironmentMiner.MINER_ID);
		return dependencies;
	}

	public void extendSchema(DataElement schemaRoot)
	{
		//DataElement fsD = _dataStore.findObjectDescriptor("Filesystem Objects");
		DataElement fsD= _dataStore.findObjectDescriptor(DataStoreResources.model_directory);
		DataElement cancellable = _dataStore.findObjectDescriptor(DataStoreResources.model_Cancellable);

		DataElement cmdD = createCommandDescriptor(fsD, "Command", "C_COMMAND", false);
		_dataStore.createReference(cancellable, cmdD, "abstracts", "abstracted by");

		DataElement shellD = createCommandDescriptor(fsD, "Shell", "C_SHELL", false);
		_dataStore.createReference(cancellable, shellD, "abstracts", "abstracted by");

//		DataElement inputD = _dataStore.createObject(cmdD, "input", "Enter command");
		_dataStore.createObject(cmdD, "input", "Enter command");
//		DataElement outputD = _dataStore.createObject(cmdD, "output", "Command Output");
		_dataStore.createObject(cmdD, "output", "Command Output");
		
		_descriptors = new CommandMinerDescriptors();
		_descriptors._stdout = _dataStore.createObjectDescriptor(schemaRoot, "stdout");
		_descriptors._stderr = _dataStore.createObjectDescriptor(schemaRoot, "stderr");
		_descriptors._prompt = _dataStore.createObjectDescriptor(schemaRoot, "prompt");
		_descriptors._grep = _dataStore.createObjectDescriptor(schemaRoot, "grep");
		_descriptors._pathenvvar = _dataStore.createObjectDescriptor(schemaRoot, "pathenvvar");
		_descriptors._envvar = _dataStore.createObjectDescriptor(schemaRoot, "envvar");
		_descriptors._libenvvar = _dataStore.createObjectDescriptor(schemaRoot, "libenvvar");
		_descriptors._error = _dataStore.createObjectDescriptor(schemaRoot, "error");
		_descriptors._warning = _dataStore.createObjectDescriptor(schemaRoot, "warning");
		_descriptors._informational = _dataStore.createObjectDescriptor(schemaRoot, "informational");
		_descriptors._process =_dataStore.createObjectDescriptor(schemaRoot, "process");
		

//		DataElement getPossibleCmds = createCommandDescriptor(fsD, "Get Commands", "C_GET_POSSIBLE_COMMANDS", false);
		createCommandDescriptor(fsD, "Get Commands", "C_GET_POSSIBLE_COMMANDS", false);
		_dataStore.refresh(schemaRoot);
	}

	public DataElement handleCommand(DataElement theElement)
	{
		String name = getCommandName(theElement);
		DataElement status = getCommandStatus(theElement);
		DataElement subject = getCommandArgument(theElement, 0);

		if (name.equals("C_COMMAND"))
		{
			DataElement invArg = getCommandArgument(theElement, 1);
			if (invArg != null)
			{
				String invocation = invArg.getName();
				//Remove All extra whitespace from the command
				if (invocation.trim().length() > 0)
				{
					if (invocation.equals("?") || invocation.equals("help"))
						invocation = "cat " + theElement.getDataStore().getAttribute(DataStoreAttributes.A_PLUGIN_PATH) + "/org.eclipse.rse.services.dstore/patterns.dat";
					launchCommand(subject, invocation, status);
				}
				return status;
			}
			else
			{
				status.setAttribute(DE.A_NAME, "done");
			}
		}
		else if (name.equals("C_SHELL"))
		{
			String invocation = ">";
			launchCommand(subject, invocation, status);
		}
		else if (name.equals("C_SEND_INPUT"))
		{
			DataElement input = getCommandArgument(theElement, 1);
//			DataElement de = (DataElement) subject.dereference().get(1);
			subject.dereference().get(1);
			sendInputToCommand(input.getName(), getCommandStatus(subject));
		}
		else if (name.equals("C_CANCEL"))
		{
			DataElement de = (DataElement) subject.dereference().get(1);
			DataElement cancelStatus = getCommandStatus(subject);
			cancelCommand(de.getName().trim(), cancelStatus);
			return status;
		}
		else if (name.equals("C_GET_POSSIBLE_COMMANDS"))
		{
			getPossibleCommands(status);
			return status;
		}

		return status;
	}
	
	public void getPossibleCommands(DataElement status)
	{
		QueryPathThread qpt = new QueryPathThread(status);
		qpt.start();
	}

	public void launchCommand(DataElement subject, String invocation, DataElement status)
	{
		//First Check to make sure that there are no "zombie" threads
		Iterator iter = _threads.keySet().iterator();
		while (iter.hasNext())
		{
			String threadName = (String) iter.next();
			CommandMinerThread theThread = (CommandMinerThread) _threads.get(threadName);
			if ((theThread == null) || (!theThread.isAlive()))
			{
				_threads.remove(threadName);
			}
		}
		CommandMinerThread newCommand = new CommandMinerThread(subject, invocation, status, getPatterns(), _descriptors);
		_threads.put(status.getAttribute(DE.A_ID), newCommand);
		newCommand.start();
	}

	private void sendInputToCommand(String input, DataElement status)
	{
		CommandMinerThread theThread = (CommandMinerThread) _threads.get(status.getAttribute(DE.A_ID));
		if (theThread != null)
		{
			theThread.sendInput(input);
		}
	}

	private void cancelCommand(String theCommand, DataElement status)
	{
		CommandMinerThread theThread = (CommandMinerThread) _threads.get(status.getAttribute(DE.A_ID));
		if (theThread != null)
		{		    
			theThread.stopThread();
			theThread.sendExit();
			
			boolean done = false;
			long stopIn = System.currentTimeMillis() + 3000;
			while (!done)
				if ((!theThread.isAlive()) || (stopIn < System.currentTimeMillis()))
					done = true;
			_dataStore.createObject(status, "stdout", "Command Cancelled by User Request");
			_dataStore.refresh(status);
		}
	}

	public String getVersion()
	{
		return "6.4.0";
	}

}