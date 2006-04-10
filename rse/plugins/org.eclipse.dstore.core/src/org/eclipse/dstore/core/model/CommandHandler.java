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

package org.eclipse.dstore.core.model;

import java.util.ArrayList;

import org.eclipse.dstore.core.java.IRemoteClassInstance;
import org.eclipse.dstore.core.util.CommandGenerator;

/**
 * <p>
 * Abtract class for handling commands.  A <code>CommandHandler</code> is a <code>Handler</code> that 
 * contains a queue of commands to be sent to miners.  Each DataStore instance uses a single
 * command handler that periodically sends it's queue either to a server or directly
 * to miners.
 * </p>
 * <p>
 * The CommandHandler is the means by which the DataStore sends information or files from
 * the client to the remote tools.
 * </p>
 */
public abstract class CommandHandler extends Handler
{

	protected ArrayList _commands;
	protected ArrayList _classesToSend;

	private CommandGenerator _commandGenerator;

	/**
	 * Constructor
	 */
	public CommandHandler()
	{
		super();
		_commands = new ArrayList();
		_classesToSend = new ArrayList();
		_commandGenerator = new CommandGenerator();
	}

	/**
	 * Sets the associated DataStore
	 */
	public void setDataStore(DataStore dataStore)
	{
		super.setDataStore(dataStore);
		_commandGenerator.setDataStore(dataStore);
	}

	/**
	 * Returns the associated DataStore
	 * @return the associated DataStore
	 */
	public DataStore getDataStore()
	{
		return _dataStore;
	}

	/**
	 * Adds a command object to the queue
	 * @param command the command to add to the queue
	 * @param immediate indicates whether the command should be inserted first in the queue
	 * 			or whether it should be appended.
	 */
	public void addCommand(DataElement command, boolean immediate)
	{
		synchronized (_commands)
		{
			if (!_commands.contains(command))
			{
				if (immediate)
				{
					_commands.add(0, command);
				}
				else
				{
					_commands.add(command);
				}
			}
			notifyInput();
		}
	}

	/**
	 * Periodically called to send commands from the queue.
	 */
	public void handle()
	{
		if (!_commands.isEmpty() || !_classesToSend.isEmpty())
		{
			sendCommands();
		}
	}

	/**
	 * Create and add a new command object to the command queue.
	 * 
	 * @param commandDescriptor the descriptor for the new command
	 * @param arguments the arguments for the command
	 * @param object the subject of the command
	 * @param refArg indicates whether the subject should be represented in the command as a
	 * 			reference to the subject or the actual subject, itself
	 * @param immediate indicates whether the command should be first in the queue or appended to it
	 * @return the status object of the command
	 */
	public DataElement command(DataElement commandDescriptor, ArrayList arguments, DataElement object, boolean refArg, boolean immediate)
	{
		DataElement command = _commandGenerator.generateCommand(commandDescriptor, arguments, object, refArg);
		return command(command, immediate);
	}

	/**
	 * Create and add a new command object to the command queue.
	 * 
	 * @param commandDescriptor the descriptor for the new command
	 * @param arg the arg for the command
	 * @param object the subject of the command
	 * @param refArg indicates whether the subject should be represented in the command as a
	 * 			reference to the subject or the actual subject, itself
	 * @param immediate indicates whether the command should be first in the queue or appended to it
	 * @return the status object of the command
	 */
	public DataElement command(DataElement commandDescriptor, DataElement arg, DataElement object, boolean refArg, boolean immediate)
	{
		DataElement command = _commandGenerator.generateCommand(commandDescriptor, arg, object, refArg);
		return command(command, immediate);
	}

	/**
	 * Create and add a new command object to the command queue.
	 * 
	 * @param commandDescriptor the descriptor for the new command
	 * @param object the subject of the command
	 * @param refArg indicates whether the subject should be represented in the command as a
	 * 			reference to the subject or the actual subject, itself
	 * @return the status object of the command
	 */
	public DataElement command(DataElement commandDescriptor, DataElement object, boolean refArg)
	{
		DataElement command = _commandGenerator.generateCommand(commandDescriptor, object, refArg);
		return command(command);
	}

	/**
	 * Add a command object to the command queue
	 * @param cmd the command object to add to the queue
	 * @return the status object of the command
	 */
	public DataElement command(DataElement cmd)
	{
		return command(cmd, false);
	}

	/**
	 * Add a command object to the command queue
	 * @param cmd the command object to add to the queue
	 * @param immediate indicates whether the command is to be inserted first in the queue or appended
	 * @return the status object of the command
	 */
	public DataElement command(DataElement cmd, boolean immediate)
	{
		DataElement status = null;
		if ((cmd != null) && _dataStore != null)
		{
		    
			status = cmd.get(cmd.getNestedSize() -1);
			if (status != null && !status.getName().equals(DataStoreResources.model_done))
			{
				addCommand(cmd, immediate);
			}
		}

		return status;
	}

	/**
	 * Removes and affectively cancels all commands from the current queue of commands
	 */
	public synchronized void cancelAllCommands()
	{
		DataElement log = _dataStore.getLogRoot();
		for (int i = 0; i < _commands.size(); i++)
		{
			log.removeNestedData((DataElement) _commands.get(i));
		}

		_commands.clear();
	}
	
	public CommandGenerator getCommandGenerator()
	{
		return _commandGenerator;
	}

	/**
	 * Implemented to provide the means by which commands in the queue are sent
	 */
	public abstract void sendCommands();
	
	
	/**
	 * Implemented to provide the means by which file bytes are sent
	 * @param fileName the name of the file to send
	 * @param bytes to bytes of the file to send
	 * @param size the number of bytes to send
	 * @param binary indicates whether to send the bytes as binary or text
	 */		
	public abstract void sendFile(String fileName, byte[] bytes, int size, boolean binary);
	
	
	/**
	 * Implemented to provide the means by which file bytes are sent
	 * @param fileName the name of the file to send
	 * @param bytes to bytes of the file to send
	 * @param size the number of bytes to send
	 * @param binary indicates whether to send the bytes as binary or text
	 * @param byteStreamHandlerId indicates which byte stream handler to receive the bytes with
	 */		
	public abstract void sendFile(String fileName, byte[] bytes, int size, boolean binary, String byteStreamHandlerId);
	
	/**
	 * Implemented to provide the means by which file bytes are sent and appended
	 * @param fileName the name of the file to send
	 * @param bytes to bytes of the file to send
	 * @param size the number of bytes to send
	 * @param binary indicates whether to send the bytes as binary or text
	 */		
	public abstract void sendAppendFile(String fileName, byte[] bytes, int size, boolean binary);
	
	/**
	 * Implemented to provide the means by which file bytes are sent and appended
	 * @param fileName the name of the file to send
	 * @param bytes to bytes of the file to send
	 * @param size the number of bytes to send
	 * @param binary indicates whether to send the bytes as binary or text
	 * @param byteStreamHandlerId indicates which byte stream handler to receive the bytes with
	 */		
	public abstract void sendAppendFile(String fileName, byte[] bytes, int size, boolean binary, String byteStreamHandlerId);

	/**
	 * Implemented to provide the means by which classes are sent
	 * across the comm channel.
	 * @param className the name of the class to send
	 */
	public abstract void sendClass(String className);
	
	/**
	 * Implemented to provide the means by which classes are sent
	 * across the comm channel.
	 * @param className the name of the class to send
	 * @param classByteStreamHandlerId indicates which class byte stream handler to receive the class with
	 */
	public abstract void sendClass(String className, String classByteStreamHandlerId);
	
	
	/**
	 * Runs the specified class on the remote system
	 */
	public abstract void sendClassInstance(IRemoteClassInstance runnable, String classByteStreamHandlerId);
	
	
	/**
	 * Causes the current thread to wait until this class request has been
	 * fulfilled.
	 */
	public synchronized void waitForInput()
	{
		if (_commands.size() == 0 && _classesToSend.size() == 0)
		{
			super.waitForInput();
		}
	}
	
	/**
	 * Implemented to provide the means by which classes are requested
	 * across the comm channel.
	 * @param className the name of the class to request
	 */
	public abstract void requestClass(String className);

	public abstract void sendKeepAliveConfirmation();

	public abstract void sendKeepAliveRequest();
}