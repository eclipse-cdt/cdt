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

package org.eclipse.dstore.core.util;

import java.util.ArrayList;

import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.dstore.core.model.DataStoreResources;

/**
 * This class is used to generate command object instances from command descriptors and arguments to commands.
 * Command instances are instances of command descriptors.  Each command instance contains a set of data arguments
 * and a status object, that represents the current state of a command.  After a command instance is created,
 * it is referenced in the command log for the DataStore.
 */
public class CommandGenerator
{
    private DataStore _dataStore = null;
    private DataElement _log = null;

    static private int _id = 0;

	/**
	 * Constructor
	 */
    public CommandGenerator()
    {
    }

	/**
	 * Sets the associated DataStore
	 * @param dataStore the associated DataStore
	 */
    public void setDataStore(DataStore dataStore)
    {
        _dataStore = dataStore;
        _log = _dataStore.getLogRoot();
    }

	/**
	 * This method logs the current command object in the DataStore command log.  For each
	 * logged command, a status object is created and returned.
	 * @param commandObject the commandObject to log
	 * @return the status object of the command
	 */
    public DataElement logCommand(DataElement commandObject)
    {
        try
        {
            // prevent duplicate queries
            String name = commandObject.getAttribute(DE.A_NAME);
 
            // create time and status objects
            DataElement status = null;
  
            if (status == null)
            {
            	StringBuffer id = new StringBuffer(commandObject.getId());
            	id.append(DataStoreResources.model_status);
                status =
                    _dataStore.createObject(
                        commandObject,
                       DataStoreResources.model_status,
                       DataStoreResources.model_start,
                        "",
                       	id.toString());
            }
            _log.addNestedData(commandObject, false);

        }
        catch (Exception e)
        {
            _dataStore.trace(e);
        }

        return commandObject;
    }

	/**
	 * Creates a new command instance object from a command descriptor
	 * @param commandDescriptor the descriptor of the command to create
	 * @return the new command instance
	 */
    public DataElement createCommand(DataElement commandDescriptor)
    {
        if (commandDescriptor != null)
        {
            if (commandDescriptor.getType().equals(DE.T_COMMAND_DESCRIPTOR))
            {
                DataElement commandInstance = _dataStore.createObject(null, commandDescriptor.getName(), commandDescriptor.getValue(), commandDescriptor.getSource());
                commandInstance.setDescriptor(commandDescriptor);
                return commandInstance;
            }
            else
            {
                System.out.println("not cd -> " + commandDescriptor);
                return null;
            }
        }
        else
        {
            return null;
        }
    }
    
    private void clearDeleted(DataElement element)
    {
    	for (int i = 0; i < element.getNestedSize(); i++)
    	{
    		DataElement child = element.get(i).dereference();
    		if (child.isDeleted())
    		{
    			element.removeNestedData(child);	
    		}	
    	}	
    }

	/**
	 * Creates a new command from a command descriptor and it's arguments.
	 * 
	 * @param commandDescriptor the command type of the new command
	 * @param arguments the arguments for the command, besides the subject
	 * @param dataObject the subject of the command
	 * @param refArg indicates whether the subject should be represented as a reference or directly  
	 * @return the status object of the command
	 */
    public DataElement generateCommand(DataElement commandDescriptor, ArrayList arguments, DataElement dataObject, boolean refArg)
    {

        DataElement commandObject = createCommand(commandDescriptor);
        if (commandObject != null)
        {
        	clearDeleted(dataObject);
            DataElement tempRoot = _dataStore.getTempRoot();

            commandObject.setAttribute(DE.A_VALUE, commandDescriptor.getName());

            if (dataObject.isUpdated() && !dataObject.isSpirit())
            {
                _dataStore.createReference(commandObject, dataObject,DataStoreResources.model_contents);
            }
            else
            {
            	dataObject.setPendingTransfer(true);
                commandObject.addNestedData(dataObject, false);
            }

            if (arguments != null)
            {
                for (int i = 0; i < arguments.size(); i++)
                {
                    DataElement arg = (DataElement) arguments.get(i);
                    if (arg != null)
                    {
                        if (!arg.isUpdated() || arg.isSpirit())
                        {
                            commandObject.addNestedData(arg, false);
                        }
                        else
                        {
                            _dataStore.createReference(commandObject, arg, "argument");
                        }
                    }
                }
            }

            return logCommand(commandObject);
        }
        else
        {
            return null;
        }
    }


	/**
	 * Creates a new command from a command descriptor and it's arguments.
	 * 
	 * @param commandDescriptor the command type of the new command
	 * @param arg the arguement for the command, besides the subject
	 * @param dataObject the subject of the command
	 * @param refArg indicates whether the subject should be represented as a reference or directly  
	 * @return the status object of the command
	 */
    public DataElement generateCommand(DataElement commandDescriptor, DataElement arg, DataElement dataObject, boolean refArg)
    {
        _id++;

        DataElement commandObject = createCommand(commandDescriptor);
        if (commandObject != null)
        {
            DataElement tempRoot = _dataStore.getTempRoot();
            commandObject.setAttribute(DE.A_VALUE, commandDescriptor.getName());
			clearDeleted(dataObject);
            if ((refArg || dataObject.isUpdated()) && !dataObject.isSpirit())
            {
                _dataStore.createReference(commandObject, dataObject,DataStoreResources.model_contents);
            }
            else
            {
            	dataObject.setPendingTransfer(true);
                commandObject.addNestedData(dataObject, false);
            }

            if (!arg.isUpdated() || arg.isSpirit())
            {
                commandObject.addNestedData(arg, false);
            }
            else
            {
                _dataStore.createReference(commandObject, arg, "argument");
            }
     

            return logCommand(commandObject);
        }
        else
        {
            return null;
        }
    }

	/**
	 * Creates a new command from a command descriptor and it's arguments.
	 * 
	 * @param commandDescriptor the command type of the new command
	 * @param dataObject the subject of the command
	 * @param refArg indicates whether the subject should be represented as a reference or directly  
	 * @return the status object of the command
	 */
    public DataElement generateCommand(DataElement commandDescriptor, DataElement dataObject, boolean refArg)
    {
    	_id++;

        DataElement commandObject = createCommand(commandDescriptor);
        if (commandObject != null)
        {
            commandObject.setAttribute(DE.A_VALUE, commandDescriptor.getName());

			clearDeleted(dataObject);
            if ((refArg || dataObject.isUpdated()) && !dataObject.isSpirit())
            {
                _dataStore.createReference(commandObject, dataObject,DataStoreResources.model_arguments);
            }
            else
            {
            	dataObject.setPendingTransfer(true);
                commandObject.addNestedData(dataObject, false);
            }

            return logCommand(commandObject);
        }
        else
        {
            return null;
        }
    }

	/**
	 * Creates a response tree for transmitting a set of data from a server to a client.
	 * 
	 * @param document the root of the response
	 * @param objects the data contained in the response
	 * @return the response tree root
	 */
    public DataElement generateResponse(DataElement document, ArrayList objects)
    {
        document.addNestedData(objects, false);
        return document;
    }

	/**
	 * Creates a response tree for transmitting a set of data from a server to a client.
	 * 
	 * @param responseType the type of data to respond with
	 * @param dataObject the child object in the response tree
	 * @return the response tree root
	 */
    public DataElement generateResponse(String responseType, DataElement dataObject)
    {
        if (dataObject != null)
        {
            DataElement commandObject = _dataStore.createObject(null, "RESPONSE", responseType);
            commandObject.addNestedData(dataObject, true);
            return commandObject;
        }
        else
        {
            return null;
        }
    }

	/**
	 * Creates a simple response object of the specified type
	 * 
	 * @param responseType the type of data to respond with
	 * @return the response object
	 */
    public DataElement generateResponse(String responseType)
    {
        DataElement commandObject = _dataStore.createObject(null, "RESPONSE", responseType);
        return commandObject;
    }
}