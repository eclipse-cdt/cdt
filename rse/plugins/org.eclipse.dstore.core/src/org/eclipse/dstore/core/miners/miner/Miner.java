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

package org.eclipse.dstore.core.miners.miner;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.dstore.core.model.DataStoreResources;
import org.eclipse.dstore.core.model.DataStoreSchema;
import org.eclipse.dstore.core.model.Handler;
import org.eclipse.dstore.core.model.ISchemaExtender;
import org.eclipse.dstore.core.util.ExternalLoader;

/**
 * Miner is the abstact base class of all DataStore extensions).  
 * The DataStore framework knows how to load and route commands to miners
 * because it interfaces miners through the restricted set of interfaces declared here.
 * To add a new miner, developers must extend this class and implement the abstract methods declared here.
 */
public abstract class Miner extends Handler 
implements ISchemaExtender
{


	public DataStore _dataStore;
	public DataElement _minerElement;
	public DataElement _minerData;
	public DataElement _minerTransient;

	
	
	private boolean _initialized;
	private boolean _connected;
	private ExternalLoader _loader;

	protected String _name = null;
	protected String _value = null;
	protected ArrayList _dependencies;
	protected List _commandQueue;
	
	protected ResourceBundle _resourceBundle = null;

	/**
	 * Creates a new Miner
	 */
	protected Miner()
	{
		_initialized = false;
		_connected = false;
		_commandQueue = new ArrayList();
	}

	/**
	 * Returns the qualified names of all miners that
	 * this miner depends on.  A miner depends on another
	 * miner if it's schema extends or uses another's schema. 
	 * By default it returns an empty list.
	 * @return a list of miner dependencies, each represented as a qualified name
	 */
	public final ArrayList getMinerDependencies()
	{
		if (_dependencies == null)
		{
			_dependencies = getDependencies();
		}
		return _dependencies;
	}


	protected ArrayList getDependencies()
	{
		return new ArrayList();
	}

	/**
	 * Indicates whether the miner has been initialized yet
	 * @return whether the miner has been initialized
	 */
	public final boolean isInitialized()
	{
		return _initialized;
	}

	/**
	 * Indicates whether the miner has been connected to
	 * the DataStore yet.
	 * @return whether the miner has been connected to the DataStore
	 */
	public final boolean isConnected()
	{
		return _connected;
	}

	/**
	 * Shuts down the miner and cleans up it's meta-information.
	 * Override this function to do your own cleanup.
	 */
	public void finish()
	{
		DataElement root = _dataStore.getMinerRoot();

		_minerData.removeNestedData();
		_minerElement.removeNestedData();
		_dataStore.update(_minerElement);

		if (root.getNestedData() != null)
		{
			root.getNestedData().remove(_minerElement);
		}
		root.setExpanded(false);
		root.setUpdated(false);

		_dataStore.update(root);
	}

	/**
	 * Interface to retrieve an NL enabled resource bundle.  
	 * Override this function to get access to a real resource bundle.
	 */
	public ResourceBundle getResourceBundle()
	{
		return null;
	}

	/**
	 * Default method that gets called on a Miner when it is loaded.
	 * Override this function to perform some initialization at miner loading time.
	 */
	protected void load()
	{
	}

	/**
	 * Default method that gets called on a Miner when it is loaded.
	 * Override this function to perform some initialization at miner loading time.
	 * If loading the miner can result in some failure, set that status to incomplete
	 *
	 * @param status the status of the initialize miner command
	 */
	protected void load(DataElement status)
	{
		load();
	}

	/**
	 * This gets called after a miner is initialized.  
	 * If you need to update element information at that time, override this method.
	 */
	protected void updateMinerInfo()
	{
	}

	/**
	 * Returns the qualified name of this miner
	 *
	 * @return the qualified name of this miner
	 */
	public final String getMinerName()
	{
		if (_name == null)
			_name = getClass().getName();
		return _name;
	}

	/**
	 * Returns the name of this miner
	 *
	 * @return the name of this miner
	 */
	public final String getValue()
	{
		if (_value == null)
		{
			String name = getMinerName();
			int indexOfValue = name.lastIndexOf(".");
			_value = name.substring(indexOfValue + 1, name.length());
		}
		return _value;
	}
	
	public final void handle()
	{
		while (!_commandQueue.isEmpty())
		{
			DataElement cmd = (DataElement)_commandQueue.remove(0);
			command(cmd);			
		}
	}
	
	public final void requestCommand(DataElement command)
	{
		_commandQueue.add(command);
		notifyInput();
	}
	
	public final void initMiner(DataElement status)
	{
		try
		{
		//	System.out.println("initMiner:"+getMinerName());
		if (!_initialized)
		{
			load(status);
			_initialized = true;
		}
		updateMinerInfo();

		DataElement minerRoot = _dataStore.getMinerRoot();
		_dataStore.refresh(minerRoot);
		if (status.getAttribute(DE.A_VALUE).equals(DataStoreResources.model_incomplete))
		{
			_dataStore.refresh(status);
		}
		else
		{

			status.setAttribute(DE.A_VALUE, DataStoreResources.model_done);
		}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		

	}
	
	/**
	 * Issues a specified command on this miner from the DataStore framework.
	 * The base class handles "C_INIT_MINERS" but other commands are delegated to
	 * the concrete miner implementations through handleCommand()
	 *
	 * @param command the command that has been sent to this miner
	 * @return the status of the command
	 */
	protected final DataElement command(DataElement command)
	{
		String name = getCommandName(command);
		DataElement status = getCommandStatus(command);
		long startTime = System.currentTimeMillis();

		if (status == null)
		{
			_dataStore.trace("bad command: ");
			_dataStore.trace("\tcmd=" + command);
			_dataStore.trace("\tparent=" + command.getParent());
			return null;
		}

		if (status.getAttribute(DE.A_NAME).equals("start"))
		{
			status.setAttribute(DE.A_NAME, DataStoreResources.model_working);
		}

		if (name.equals(DataStoreSchema.C_INIT_MINERS))
		{
			initMiner(status);
		}
		else
		{
			try
			{
				status = handleCommand(command);
			}
			catch (Exception e)
			{
				//e.printStackTrace();
				_dataStore.trace(e);
				status.setAttribute(DE.A_VALUE, "Failed with Exception:"+getStack(e));
				status.setAttribute(DE.A_NAME, DataStoreResources.model_done);
				//status.setAttribute(DE.A_SOURCE, getStack(e));
				_dataStore.refresh(status);

				String exc = null;
				if (e.getMessage() != null)
					exc = e.getMessage();
				else
					exc = "Exception";
				DataElement exception = _dataStore.createObject(status, DataStoreResources.model_error, exc);
			}
			catch (Error er)
			{
			    er.printStackTrace();
				_dataStore.trace(er);
				_dataStore.finish();
				System.exit(-1);
			}
		}

		_dataStore.refresh(status);
		return status;
	}

	private String getStack(Throwable e)
	{
		StringBuffer buf = new StringBuffer();
		StackTraceElement[] stack = e.getStackTrace();
		for (int i = 0; i < stack.length; i++)
		{
			buf.append(stack[i].getClassName() + ":" + stack[i].getMethodName() + ":" + stack[i].getLineNumber());
			buf.append(",");
		}
		return buf.toString();
	}
	
	/**
	 * Sets the DataStore and performs some fundamental initialization for this miner.  
	 * The framework calls this method on a miner before any commands are issued.
	 * The extendSchema() is called on the miner.
	 *
	 * @param dataStore the DataStore that owns this miner
	 */
	public final void setDataStore(DataStore dataStore)
	{
		_dataStore = dataStore;

		DataElement root = _dataStore.getMinerRoot();
		String name = getMinerName();
		String value = getValue();

		_resourceBundle = getResourceBundle();

		// yantzi: Reuse existing miner root if found
		_minerElement = _dataStore.find(root, DE.A_NAME, name, 1);
		if (_minerElement == null || _minerElement.isDeleted())
		{	
			// Create new child for this miner
			_minerElement   = _dataStore.createObject(root, DataStoreResources.model_miner, name, name);
			_minerElement.setAttribute(DE.A_VALUE, value);
			_minerElement.setAttribute(DE.A_SOURCE, getVersion());
	
			_minerData      = _dataStore.createObject(_minerElement, DataStoreResources.model_data, DataStoreResources.model_Data, name);
			_minerTransient = _dataStore.createObject(_minerElement, DataStoreResources.model_transient, DataStoreResources.model_Transient_Objects, name);
		}
		else
		{
			// Reuse existing miner node
			_minerData = _dataStore.find(_minerElement, DE.A_NAME, DataStoreResources.model_Data, 1);
			if (_minerData == null || _minerData.isDeleted())
			{
				_minerData = _dataStore.createObject(_minerElement, DataStoreResources.model_data, DataStoreResources.model_Data, name);
			}
		
			_minerTransient = _dataStore.find(_minerElement, DE.A_NAME, DataStoreResources.model_Transient_Objects, 1);
			if (_minerTransient == null || _minerData.isDeleted())
			{
				_minerTransient = _dataStore.createObject(_minerElement, DataStoreResources.model_transient, DataStoreResources.model_Transient_Objects, name);
			}
		}

		_dataStore.refresh(root, true);
		_dataStore.refresh(_minerElement);

		_connected = true;
	}

	/**
	 * Creates an abstract command descriptor.  This is a helper method that miner may call
	 * when it creates or updates the schema for it's tool domain
	 *
	 * @param descriptor the parent descriptor for the new descriptor
	 * @param name the name of the command
	 * @param value the identifier for this command
	 * @return the new command descriptor
	 */
	public final DataElement createAbstractCommandDescriptor(DataElement descriptor, String name, String value)
	{
		return _dataStore.createAbstractCommandDescriptor(descriptor, name, getMinerName(), value);
	}

	/**
	 * Creates a command descriptor.  This is a helper method that miner may call
	 * when it creates or updates the schema for it's tool domain
	 *
	 * @param descriptor the parent descriptor for the new descriptor
	 * @param name the name of the command
	 * @param value the identifier for this command
	 * @return the new command descriptor
	 */
	public final DataElement createCommandDescriptor(DataElement descriptor, String name, String value)
	{
		return createCommandDescriptor(descriptor, name, value, true);
	}

	/**
	 * Creates a command descriptor.  This is a helper method that miner may call
	 * when it creates or updates the schema for it's tool domain
	 *
	 * @param descriptor the parent descriptor for the new descriptor
	 * @param name the name of the command
	 * @param value the identifier for this command
	 * @param visible an indication whether this command descriptor should be visible to an end-user
	 * @return the new command descriptor
	 */
	public final DataElement createCommandDescriptor(DataElement descriptor, String name, String value, boolean visible)
	{
		DataElement cmdD = _dataStore.createCommandDescriptor(descriptor, name, getMinerName(), value);
		if (!visible)
		{
			cmdD.setDepth(0);
		}

		return cmdD;
	}

	/**
	 * Creates an abstract object descriptor.  This is a helper method that miner may call
	 * when it creates or updates the schema for it's tool domain
	 *
	 * @param descriptor the parent descriptor for the new descriptor
	 * @param name the name of the object type
	 * @return the new object descriptor
	 */
	public final DataElement createAbstractObjectDescriptor(DataElement descriptor, String name)
	{
		return _dataStore.createAbstractObjectDescriptor(descriptor, name);
	}

	/**
	 * Creates an abstract object descriptor.  This is a helper method that miner may call
	 * when it creates or updates the schema for it's tool domain
	 *
	 * @param descriptor the parent descriptor for the new descriptor
	 * @param name the name of the object type
	 * @param source the plugin location of the miner that owns this object type
	 * @return the new object descriptor
	 */
	public final DataElement createAbstractObjectDescriptor(DataElement descriptor, String name, String source)
	{
		return _dataStore.createAbstractObjectDescriptor(descriptor, name, source);
	}

	/**
	 * Creates a object descriptor.  This is a helper method that miner may call
	 * when it creates or updates the schema for it's tool domain
	 *
	 * @param descriptor the parent descriptor for the new descriptor
	 * @param name the name of the object type
	 * @return the new object descriptor
	 */
	public final DataElement createObjectDescriptor(DataElement descriptor, String name)
	{
		return _dataStore.createObjectDescriptor(descriptor, name);
	}

	/**
	 * Creates a object descriptor.  This is a helper method that miner may call
	 * when it creates or updates the schema for it's tool domain
	 *
	 * @param descriptor the parent descriptor for the new descriptor
	 * @param name the name of the object type
	 * @param source the plugin location of the miner that owns this object type
	 * @return the new object descriptor
	 */
	public final DataElement createObjectDescriptor(DataElement descriptor, String name, String source)
	{
		return _dataStore.createObjectDescriptor(descriptor, name, source);
	}

	/**
	 * Creates a new type of relationship descriptor.  This is a helper method that miner may call
	 * when it creates or updates the schema for it's tool domain
	 *
	 * @param descriptor the parent descriptor for the new descriptor
	 * @param name the name of the relationship type
	 * @return the new relationship descriptor
	 */
	public final DataElement createRelationDescriptor(DataElement descriptor, String name)
	{
		return _dataStore.createRelationDescriptor(descriptor, name);
	}

	/**
	 * Creates an abstract relationship between two descriptors.  An abstract relationship between two descriptors
	 * indicates that the first descriptor abstracts the second, while the second inherits the
	 * properties of the first. This is a helper method that miner may call
	 * when it creates or updates the schema for it's tool domain.
	 *
	 * @param from the abstacting descriptor 
	 * @param to the descriptor that is abstracted
	 * @return the new relationship descriptor
	 */
	public final DataElement createAbstractRelationship(DataElement from, DataElement to)
	{
		return _dataStore.createReference(from, to, "abstracts", "abstracted by");
	}

	/**
	 * Creates a contents relationship between any two elements. 
	 *
	 * @param from the containing element 
	 * @param to the element that is contained
	 * @return the new relationship
	 */
	public final DataElement createReference(DataElement from, DataElement to)
	{
		return _dataStore.createReference(from, to);
	}

	

	/**
	 * Returns the element that represents this miner. 
	 *
	 * @return the miner element
	 */
	public final DataElement getMinerElement()
	{
		return _minerElement;
	}

	/**
	 * Returns the element that contains this miners meta-information. 
	 *
	 * @return the miner data element
	 */
	public final DataElement getMinerData()
	{
		return _minerData;
	}

	/**
	 * Returns the transient object container for this element. 
	 *
	 * @return the transient element
	 */
	public final DataElement getMinerTransient()
	{
		return _minerTransient;
	}

	/**
	 * Identifies a give object descriptor type to be transient in this miner. 
	 *
	 * @param objectDescriptor the object descriptor type that is transient
	 */
	public final void makeTransient(DataElement objectDescriptor)
	{
		_dataStore.createReference(_minerTransient, objectDescriptor);
	}

	/**
	 * Returns the name of a command. 
	 * This is a helper method to be used inside handleCommand().
	 *
	 * @param command a tree of elements representing a command
	 * @return the name of the command
	 */
	public final String getCommandName(DataElement command)
	{
		return (String) command.getAttribute(DE.A_NAME);
	}

	/**
	 * Returns the status of a command. 
	 * This is a helper method to be used inside handleCommand().
	 *
	 * @param command a tree of elements representing a command
	 * @return the status element for the command
	 */
	public final DataElement getCommandStatus(DataElement command)
	{
	    //DKM - status is always last
		return command.get(command.getNestedSize() - 1);
		//_dataStore.find(command, DE.A_TYPE, DataStoreResources.model_status"), 1);
	}



	/**
	 * Returns the number of arguments for this command. 
	 * This is a helper method to be used inside handleCommand().
	 *
	 * @param command a tree of elements representing a command
	 * @return the number of arguments for this command
	 */
	public final int getNumberOfCommandArguments(DataElement command)
	{
		return command.getNestedSize();
	}

	/**
	 * Returns the argument of a command specified at a given index. 
	 * This is a helper method to be used inside handleCommand().
	 *
	 * @param command a tree of elements representing a command
	 * @param arg the index into the commands children
	 * @return the argument of the command
	 */
	public final DataElement getCommandArgument(DataElement command, int arg)
	{
		if (command.getNestedSize() > 0)
		{
			DataElement argument = command.get(arg);
			if (argument != null)
			{
				return argument.dereference();
			}
		}

		return null;
	}



	/**
	 * Returns the descriptor root for the DataStore schema 
	 *
	 * @return the descriptor root
	 */
	public final DataElement getSchemaRoot()
	{
		return _dataStore.getDescriptorRoot();
	}

	public void setExternalLoader(ExternalLoader loader)
	{
		_loader = loader;
	}

	public ExternalLoader getExternalLoader()
	{
		return _loader;
	}
	
	public synchronized void waitForInput()
	{
		if (_commandQueue.size() == 0)
		{
			super.waitForInput();
		}
	}

	
	
	/**
	 * Handle commands that are routed to this miner.
	 * This interface must be implemented by each miner in order to
	 * perform tool actions driven from user interface interaction.
	 *
	 * @param theCommand an instance of a command containing a tree of arguments
	 */
	public abstract DataElement handleCommand(DataElement theCommand);
	

	/**
	 * Returns the version of this miner
	 * The expected format for this is "<version>.<major>.<minor>"
	 */
	public abstract String getVersion();
}