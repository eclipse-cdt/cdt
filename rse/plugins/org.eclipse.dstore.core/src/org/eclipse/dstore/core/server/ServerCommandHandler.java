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

package org.eclipse.dstore.core.server;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.dstore.core.java.IRemoteClassInstance;
import org.eclipse.dstore.core.miners.miner.Miner;
import org.eclipse.dstore.core.model.CommandHandler;
import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.dstore.core.model.DataStoreAttributes;
import org.eclipse.dstore.core.model.DataStoreResources;
import org.eclipse.dstore.core.model.DataStoreSchema;
import org.eclipse.dstore.core.model.IDataStoreConstants;

/**
 * The ServerCommandHandler is reponsible for maintaining
 * a queue of commands and periodically routing commands
 * from the queue to the appropriate miners.
 */
public class ServerCommandHandler extends CommandHandler
{


	private ArrayList _loaders;
	private MinerLoader _minerLoader;

	/**
	 * Constructor
	 * 
	 * @param loaders a list of <code>ExternalLoader</code>s used for loading miners
	 */
	public ServerCommandHandler(ArrayList loaders)
	{
		super();
		_loaders = loaders;
	}

	/**
	 * Sets the associated DataStore
	 * 
	 * @param dataStore the associated DataStore
	 */
	public void setDataStore(DataStore dataStore)
	{
		super.setDataStore(dataStore);
	}

	/**
	 * Loads the miners
	 */
	public void loadMiners()
	{
		if (_dataStore != null)
		{
			if (_minerLoader == null)
			{
				_minerLoader = new MinerLoader(_dataStore, _loaders);
			}
			// load the miners
			_minerLoader.loadMiners();

		}
	}
	
	public Miner loadMiner(String minerId)
	{
	
		if (_dataStore != null)
		{
			if (_minerLoader == null)
			{
				_minerLoader = new MinerLoader(_dataStore, _loaders);
			}
			
			// load and connect the miner
			Miner miner = _minerLoader.loadMiner(minerId);
			if (miner != null)
			{
				_minerLoader.connectMiner(miner);
			}
			return miner;
		}
		return null;
	}

	/**
	 * Returns the list of loaded miners
	 * 
	 * @return the list of miners
	 */
	public ArrayList getMiners()
	{
		return _minerLoader.getMiners();
	}

	/**
	 * Returns the specified miner
	 * 
	 * @param name the qualified classname of the miner to return
	 * @return the miner
	 */
	public Miner getMiner(String name)
	{
		return _minerLoader.getMiner(name);
	}

	/**
	 * Terminates a specified miner
	 * 
	 * @param name the qualified classname of the miner to terminate
	 */
	public void finishMiner(String name)
	{
		_minerLoader.finishMiner(name);
	}

	/**
	 * Called when the DataStore session is finished or when there is
	 * an unexpected error.
	 */
	public void finish()
	{
		if (_minerLoader != null)
			_minerLoader.finishMiners();
		super.finish();
	}

	private void clearDeleted(DataElement element, int depth)
	{
		if (depth > 0 && element != null)
		{
			for (int i = 0; i < element.getNestedSize(); i++)
			{
				DataElement child = element.get(i);
				if (child != null)
				{
					if (child.isReference())
						child = child.dereference();
					
					
					if (child != null)
					{
						if (child.isDeleted())
						{
							element.removeNestedData(child);
						}
						else
						{
							clearDeleted(child, depth - 1);
						}
					}
				}
			}
		}
	}


	/**
	 * Called periodically to route the current queue of commands to the appropriate miners
	 */
	public void sendCommands()
	{
		// send commands to the appropriate miners
		while (_commands.size() > 0)
		{
			DataElement command = null;

			synchronized (_commands)
			{
				command = (DataElement) _commands.get(0);
				clearDeleted(command, 2);
				_commands.remove(command);
			}

			//DKM-status is always last
			DataElement status = command.get(command.getNestedSize() - 1);
			    //_dataStore.find(command, DE.A_TYPE,DataStoreResources.model_status"), 1);

			String commandSource = command.getSource();
			String commandName = command.getName();
			_dataStore.trace("command: " + commandName);
			//System.out.println(commandName);


			if (commandName.equals(DataStoreSchema.C_VALIDATE_TICKET))
			{
				DataElement serverTicket = _dataStore.getTicket();
				DataElement clientTicket = (DataElement) command.get(0);
				String st = serverTicket.getName();
				String ct = clientTicket.getName();
				if (ct.equals(st))
				{
					serverTicket.setAttribute(DE.A_VALUE,DataStoreResources.model_valid);
					clientTicket.setAttribute(DE.A_VALUE,DataStoreResources.model_valid);

					DataElement host = _dataStore.getHostRoot();
					_dataStore.getHashMap().remove(host.getId());
					host.setAttribute(DE.A_ID, "host." + serverTicket.getName());

					_dataStore.getHashMap().put(host.getId(), host);
					_dataStore.update(host);
				}
				else
				{
					serverTicket.setAttribute(DE.A_VALUE,DataStoreResources.model_invalid);
					clientTicket.setAttribute(DE.A_VALUE,DataStoreResources.model_invalid);
				}
				_dataStore.update(clientTicket);
				_dataStore.startDataElementRemoverThread();
				status.setAttribute(DE.A_NAME,DataStoreResources.model_done);
			}
			else if (commandName.equals(DataStoreSchema.C_SET))
			{
				DataElement dataObject = (DataElement) command.get(0);
				status.setAttribute(DE.A_NAME,DataStoreResources.model_done);
			}
			else if (commandName.equals(DataStoreSchema.C_MODIFY))
			{
				DataElement dataObject = (DataElement) command.get(0);
				DataElement original = _dataStore.find(dataObject.getId());
				original.setAttributes(dataObject.getAttributes());
				status.setAttribute(DE.A_NAME,DataStoreResources.model_done);
			}
			else if (commandName.equals(DataStoreSchema.C_SET_HOST))
			{
				DataElement dataObject = (DataElement) command.get(0);

				DataElement original = _dataStore.getHostRoot();
				original.setAttributes(dataObject.getAttributes());

				_dataStore.setAttribute(DataStoreAttributes.A_LOCAL_PATH, dataObject.getSource());
				_dataStore.setAttribute(DataStoreAttributes.A_HOST_PATH, dataObject.getSource());
				status.setAttribute(DE.A_NAME,DataStoreResources.model_done);
			}
			else if (commandName.equals(DataStoreSchema.C_ADD_MINERS))
			{
				DataElement location = (DataElement) command.get(1);
				_dataStore.addMinersLocation(location);
				status.setAttribute(DE.A_NAME,DataStoreResources.model_done);
			}
			else if (commandName.equals(DataStoreSchema.C_ACTIVATE_MINER))
			{
				DataElement minerId = (DataElement) command.get(0);
				String minerName = minerId.getName();
				Miner miner = loadMiner(minerName);		
				miner.initMiner(status);
				//System.out.println("finished initing "+miner.getMinerName());
				//status.setAttribute(DE.A_NAME,DataStoreResources.model_done);
			}
			else if (commandName.equals(DataStoreSchema.C_SET_PREFERENCE))
			{
				DataElement dataObject = (DataElement) command.get(0);
				String property = dataObject.getName();
				String value = dataObject.getValue();
				_dataStore.setPreference(property, value);
			}
			else if (commandName.equals(DataStoreSchema.C_QUERY_INSTALL))
			{
				// determine where dstore is located
				status.setAttribute(DE.A_SOURCE, _dataStore.getAttribute(DataStoreAttributes.A_PLUGIN_PATH));
				status.setAttribute(DE.A_NAME,DataStoreResources.model_done);
			}
			else if (commandName.equals(DataStoreSchema.C_QUERY_CLIENT_IP))
			{
				// determine where dstore is connected to
				status.setAttribute(DE.A_SOURCE, _dataStore.getRemoteIP());
				status.setAttribute(DE.A_NAME,DataStoreResources.model_done);
			}
			else if (commandName.equals(DataStoreSchema.C_QUERY_JVM))
			{
				// get jvm stats
				// check memory consuption
				// if we're running low, try to free some
				Runtime runtime = Runtime.getRuntime();
				runtime.gc();
				long freeMem = runtime.freeMemory();
				long totalMem = runtime.totalMemory();
				long maxMem = runtime.maxMemory();
			
			
				StringBuffer statsBuffer = new StringBuffer();
				statsBuffer.append(freeMem);
				statsBuffer.append(',');
				statsBuffer.append(totalMem);
				statsBuffer.append(',');
				statsBuffer.append(maxMem);
				statsBuffer.append(',');
				statsBuffer.append(_dataStore.getNumElements());
				statsBuffer.append(',');
				
				// last 7 dataelements created
				List lastCreated = _dataStore.getLastCreatedElements();
				for (int i = 0; i < lastCreated.size(); i++)
				{
					DataElement element = (DataElement)lastCreated.get(i);
					statsBuffer.append(element.getName());
					statsBuffer.append(":");
					statsBuffer.append("id="+element.getId());
					statsBuffer.append(";");
				}
			
				
				status.setAttribute(DE.A_SOURCE, statsBuffer.toString());
				status.setAttribute(DE.A_NAME,DataStoreResources.model_done);
			}
			else if (commandName.equals(DataStoreSchema.C_SCHEMA))
			{
				loadMiners();

				DataElement schemaRoot = _dataStore.getDescriptorRoot();

				// update all descriptor objects
				_dataStore.refresh(schemaRoot);
				status.setAttribute(DE.A_NAME,DataStoreResources.model_done);
			}
			else if (commandName.equals(IDataStoreConstants.C_START_SPIRIT))
			{
				_dataStore.receiveStartSpiritCommand();
				status.setAttribute(DE.A_NAME, DataStoreResources.model_done);
			}
			else if (_dataStore.validTicket() && _minerLoader != null)
			{
				if (status != null)
				{
					boolean failure = false;
					ArrayList miners = _minerLoader.getMiners();
					for (int j = 0;(j < miners.size()) && !failure; j++)
					{
						Miner miner = (Miner) miners.get(j);
					
						if (commandSource.equals("*") || commandSource.equals(miner.getClass().getName()))
						{
							if (_dataStore.isAutoRefreshOn())
							{
								_dataStore.enableAutoRefresh(false);
							}
							/*
							status = miner.command(command);

							if ((status != null) && status.getAttribute(DE.A_NAME).equals(DataStoreResources.model_incomplete))
							{
								failure = true;
							}
							*/
							miner.requestCommand(command);
							
							
						
						}

					}
					if (commandName.equals(DataStoreSchema.C_INIT_MINERS))
					{
						// old way was to submit this command for all miners at once
						// now we wait til activateMiner call is made per each miner
						// for backward compatibility, we still call init miners
						// so we need to make sure, in cases were miners are loaded dynamically,
						// that we set this to done if there's no miners to init
						status.setAttribute(DE.A_NAME, DataStoreResources.model_done);
					}
				}

			}

			_dataStore.refresh(status);

		}
	}

	/**
	 * Set the contents of a file with the specified file
	 * @param fileName the name of the target file
	 * @param file the source file 
	 */
	public void sendFile(String fileName, File file)
	{
		//_dataStore.saveFile(fileName, file);
	}

	/**
	 * Sets the contents of a file with bytes sent from the client
	 * @param fileName the name of the file to append to
	 * @param bytes the bytes of a file to insert
	 * @param size the number of bytes to insert
	 * @param binary indicates whether to insert the bytes as binary or unicode
	 */
	public void sendFile(String fileName, byte[] bytes, int size, boolean binary)
	{
		sendFile(fileName, bytes, size, binary, "default");
	}
	
/**
	 * Sets the contents of a file with bytes sent from the client
	 * @param fileName the name of the file to append to
	 * @param bytes the bytes of a file to insert
	 * @param size the number of bytes to insert
	 * @param binary indicates whether to insert the bytes as binary or unicode
	 * @param byteStreamHandlerId indicates which byte stream handler should receive the bytes
	 */
	public void sendFile(String fileName, byte[] bytes, int size, boolean binary, String byteStreamHandlerId)
	{
		_dataStore.saveFile(fileName, bytes, size, binary, byteStreamHandlerId);
	}

	/**
	 * Appends bytes sent from the client to a file
	 * @param fileName the name of the file to append to
	 * @param bytes the bytes of a file to append
	 * @param size the number of bytes to append
	 * @param binary indicates whether to append the bytes as binary or unicode
	 */
	public void sendAppendFile(String fileName, byte[] bytes, int size, boolean binary)
	{
		sendAppendFile(fileName, bytes, size, binary, "default");
	}

	/**
	 * Appends bytes sent from the client to a file
	 * @param fileName the name of the file to append to
	 * @param bytes the bytes of a file to append
	 * @param size the number of bytes to append
	 * @param binary indicates whether to append the bytes as binary or unicode
	 * @param byteStreamHandlerId indicates which byte stream handler should receive the bytes
	 */
	public void sendAppendFile(String fileName, byte[] bytes, int size, boolean binary, String byteStreamHandlerId)
	{
		_dataStore.appendToFile(fileName, bytes, size, binary);
	}

	/**
	 * Implemented to provide the means by which classes are requested and sent
	 * across the comm channel.
	 * @param className the name of the class to request
	 */
	public synchronized void sendClass(String className)
	{
		sendClass(className, "default");
	}
	
	/**
	 * Implemented to provide the means by which classes are requested and sent
	 * across the comm channel.
	 * @param className the name of the class to request
	 */
	public synchronized void sendClass(String className, String classByteStreamHandlerId)
	{
		//_dataStore.sendClass(className, classByteStreamHandlerId);
	}

	public void sendClassInstance(IRemoteClassInstance runnable, String classByteStreamHandlerId) 
	{
		notifyInput();
	}

	/**
	 * Does not apply to server. Use ServerUpdateHandler.requestClass().
	 */
	public void requestClass(String className) 
	{
	}
	
	/**
	 * Does not apply to server. Use ServerUpdateHandler.sendKeepAliveConfirmation().
	 */
	public void sendKeepAliveConfirmation() 
	{
	}
	
	/**
	 * Does not apply to server. Use ServerUpdateHandler.sendKeepAliveRequest().
	 */
	public void sendKeepAliveRequest() 
	{
	}
	

	
}