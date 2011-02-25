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
 * David McKnight  (IBM)  - [191599] use specified encoding for shell
 * David McKnight  (IBM)  - [202822] cancelled output should be created before thread cleanup
 * David McKnight   (IBM)        - [196624] dstore miner IDs should be String constants rather than dynamic lookup
 * Noriaki Takatsu (IBM)  - [220126] [dstore][api][breaking] Single process server for multiple clients
 * Noriaki Takatsu (IBM)  - [230399] [multithread] changes to stop CommandMiner threads when clients disconnect
 * David McKnight  (IBM)  - [226561] [apidoc] Add API markup to RSE Javadocs where extend / implement is allowed
 * David McKnight (IBM) - [286671] Dstore shell service interprets &lt; and &gt; sequences - cmd descriptor to identify ability
 * David McKnight   (IBM)     [312415] [dstore] shell service interprets &lt; and &gt; sequences - handle old client/new server case
 * David McKnight   (IBM)     [320624] [dstore] shell &lt; and &gt; sequence conversion not being applied to thread
 * David McKnight   (IBM) - [283613] [dstore] Create a Constants File for all System Properties we support
 *******************************************************************************/

package org.eclipse.rse.dstore.universal.miners;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.dstore.core.miners.Miner;
import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStoreAttributes;
import org.eclipse.dstore.core.model.DataStoreResources;
import org.eclipse.dstore.internal.core.model.IDataStoreSystemProperties;
import org.eclipse.rse.internal.dstore.universal.miners.command.CommandMinerThread;
import org.eclipse.rse.internal.dstore.universal.miners.command.QueryPathThread;
import org.eclipse.rse.internal.dstore.universal.miners.command.patterns.Patterns;



/**
 * The CommandMiner provides the ability to run remote interactive shell 
 * commands via the DataStore.
 * 
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CommandMiner extends Miner
{

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
            if (type.equals("stdout")) //$NON-NLS-1$
            {
                descriptor = _stdout;
            }
            else if  (type.equals("pathenvvar")) //$NON-NLS-1$
            {
                descriptor = _pathenvvar;
            }
            else if (type.equals("envvar")) //$NON-NLS-1$
            {
                descriptor = _envvar;
            }
            else if  (type.equals("libenvvar")) //$NON-NLS-1$
            {
                descriptor = _libenvvar;
            }
            else if (type.equals("error")) //$NON-NLS-1$
            {
                descriptor = _error;
            }
            else if  (type.equals("warning")) //$NON-NLS-1$
            {
                descriptor = _warning;
            }
            else if (type.equals("informational")) //$NON-NLS-1$
            {
                descriptor = _informational;
            }
            else if  (type.equals("process")) //$NON-NLS-1$
            {
                descriptor = _process;
            }
            else if (type.equals("grep")) //$NON-NLS-1$
            {
                descriptor = _grep;
            }
            else if (type.equals("stderr")) //$NON-NLS-1$
            {
                descriptor = _stderr;
            }
            return descriptor;
        }
    }
    
	private HashMap _threads = new HashMap();
	private Patterns _patterns;
	private CommandMinerDescriptors _descriptors;
	
	
	private Patterns getPatterns()
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
		dependencies.add(IUniversalDataStoreConstants.UNIVERSAL_ENVIRONMENT_MINER_ID);
		return dependencies;
	}

	public void extendSchema(DataElement schemaRoot)
	{
		//DataElement fsD = _dataStore.findObjectDescriptor("Filesystem Objects");
		DataElement fsD= _dataStore.findObjectDescriptor(DataStoreResources.model_directory);
		DataElement cancellable = _dataStore.findObjectDescriptor(DataStoreResources.model_Cancellable);

		DataElement cmdD = createCommandDescriptor(fsD, "Command", "C_COMMAND", false); //$NON-NLS-1$ //$NON-NLS-2$
		_dataStore.createReference(cancellable, cmdD, "abstracts", "abstracted by"); //$NON-NLS-1$ //$NON-NLS-2$

		DataElement shellD = createCommandDescriptor(fsD, "Shell", "C_SHELL", false); //$NON-NLS-1$ //$NON-NLS-2$
		_dataStore.createReference(cancellable, shellD, "abstracts", "abstracted by"); //$NON-NLS-1$ //$NON-NLS-2$
		
		// indicates support for char conversion in version 3.2
		createCommandDescriptor(fsD, "CharConversion", "C_CHAR_CONVERSION", false); //$NON-NLS-1$ //$NON-NLS-2$
		

//		DataElement inputD = _dataStore.createObject(cmdD, "input", "Enter command");
		_dataStore.createObject(cmdD, "input", "Enter command"); //$NON-NLS-1$ //$NON-NLS-2$
//		DataElement outputD = _dataStore.createObject(cmdD, "output", "Command Output");
		_dataStore.createObject(cmdD, "output", "Command Output"); //$NON-NLS-1$ //$NON-NLS-2$
		
		_descriptors = new CommandMinerDescriptors();
		_descriptors._stdout = _dataStore.createObjectDescriptor(schemaRoot, "stdout"); //$NON-NLS-1$
		_descriptors._stderr = _dataStore.createObjectDescriptor(schemaRoot, "stderr"); //$NON-NLS-1$
		_descriptors._prompt = _dataStore.createObjectDescriptor(schemaRoot, "prompt"); //$NON-NLS-1$
		_descriptors._grep = _dataStore.createObjectDescriptor(schemaRoot, "grep"); //$NON-NLS-1$
		_descriptors._pathenvvar = _dataStore.createObjectDescriptor(schemaRoot, "pathenvvar"); //$NON-NLS-1$
		_descriptors._envvar = _dataStore.createObjectDescriptor(schemaRoot, "envvar"); //$NON-NLS-1$
		_descriptors._libenvvar = _dataStore.createObjectDescriptor(schemaRoot, "libenvvar"); //$NON-NLS-1$
		_descriptors._error = _dataStore.createObjectDescriptor(schemaRoot, "error"); //$NON-NLS-1$
		_descriptors._warning = _dataStore.createObjectDescriptor(schemaRoot, "warning"); //$NON-NLS-1$
		_descriptors._informational = _dataStore.createObjectDescriptor(schemaRoot, "informational"); //$NON-NLS-1$
		_descriptors._process =_dataStore.createObjectDescriptor(schemaRoot, "process"); //$NON-NLS-1$
		

//		DataElement getPossibleCmds = createCommandDescriptor(fsD, "Get Commands", "C_GET_POSSIBLE_COMMANDS", false);
		createCommandDescriptor(fsD, "Get Commands", "C_GET_POSSIBLE_COMMANDS", false); //$NON-NLS-1$ //$NON-NLS-2$
		_dataStore.refresh(schemaRoot);
	}

	public DataElement handleCommand(DataElement theElement)
	{
		String name = getCommandName(theElement);
		DataElement status = getCommandStatus(theElement);
		DataElement subject = getCommandArgument(theElement, 0);

		if (name.equals("C_COMMAND")) //$NON-NLS-1$
		{
			DataElement invArg = getCommandArgument(theElement, 1);
			if (invArg != null)
			{
				String invocation = invArg.getName();
				//Remove All extra whitespace from the command
				if (invocation.trim().length() > 0)
				{
					if (invocation.equals("?") || invocation.equals("help")) //$NON-NLS-1$ //$NON-NLS-2$
						invocation = "cat " + theElement.getDataStore().getAttribute(DataStoreAttributes.A_PLUGIN_PATH) + "/org.eclipse.rse.services.dstore/patterns.dat"; //$NON-NLS-1$ //$NON-NLS-2$
					launchCommand(subject, invocation, status);
				}
				return status;
			}
			else
			{
				status.setAttribute(DE.A_NAME, "done"); //$NON-NLS-1$
			}
		}
		else if (name.equals("C_SHELL")) //$NON-NLS-1$
		{
			String invocation = ">"; //$NON-NLS-1$
			DataElement encodingArg = getCommandArgument(theElement, 1);
			if (encodingArg.getType().equals("shell.encoding")) //$NON-NLS-1$
			{
				// fix for 191599
				 System.setProperty(IDataStoreSystemProperties.DSTORE_STDIN_ENCODING,encodingArg.getValue()); 
			}
			launchCommand(subject, invocation, status);
		}
		else if (name.equals("C_SEND_INPUT")) //$NON-NLS-1$
		{
			DataElement input = getCommandArgument(theElement, 1);
//			DataElement de = (DataElement) subject.dereference().get(1);
			subject.dereference().get(1);
			sendInputToCommand(input.getName(), getCommandStatus(subject));
			status.setAttribute(DE.A_NAME, "done"); //$NON-NLS-1$
		}
		else if (name.equals("C_CANCEL")) //$NON-NLS-1$
		{
			DataElement de = subject.dereference().get(1);
			DataElement cancelStatus = getCommandStatus(subject);
			cancelCommand(de.getName().trim(), cancelStatus);
			return status;
		}
		else if (name.equals("C_GET_POSSIBLE_COMMANDS")) //$NON-NLS-1$
		{
			getPossibleCommands(status);
			return status;
		}
		else if (name.equals("C_CHAR_CONVERSION")) //$NON-NLS-1$
		{
			DataElement cmdStatus = getCommandArgument(theElement, 0);
			CommandMinerThread theThread = (CommandMinerThread) _threads.get(cmdStatus.getAttribute(DE.A_ID));

			if (theThread != null)
			{	
				theThread._supportsCharConversion = true;
			}
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
		try
		{
			while (iter.hasNext())
			{
				String threadName = (String) iter.next();
				CommandMinerThread theThread = (CommandMinerThread) _threads.get(threadName);
				if ((theThread == null) || (!theThread.isAlive()))
				{
					_threads.remove(threadName);
				}
			}
		}
		catch (Exception e)
		{
			_dataStore.trace(e);
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
			_dataStore.createObject(status, "stdout", "Command Cancelled by User Request"); //$NON-NLS-1$ //$NON-NLS-2$

			theThread.stopThread();
			theThread.sendExit();
			
			boolean done = false;
			long stopIn = System.currentTimeMillis() + 3000;
			while (!done)
				if ((!theThread.isAlive()) || (stopIn < System.currentTimeMillis()))
					done = true;
		}
	}
	
	public void finish()
	{
		Iterator pools = _threads.entrySet().iterator();
		while (pools.hasNext())
		{
			Map.Entry entry = (Map.Entry)pools.next();
			CommandMinerThread process = (CommandMinerThread) entry.getValue();
			process.sendExit();;
		}
		
		_threads.clear();
		super.finish();
	}

	public String getVersion()
	{
		return "8.0.0"; //$NON-NLS-1$
	}

}
