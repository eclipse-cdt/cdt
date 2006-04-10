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

package org.eclipse.dstore.core.client;

import java.io.File;

import org.eclipse.dstore.core.java.IRemoteClassInstance;
import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.UpdateHandler;
import org.eclipse.dstore.extra.internal.extra.DomainEvent;
import org.eclipse.dstore.extra.internal.extra.IDomainNotifier;

/**
 * The ClientUpdateHandler is contains a queue of data update requests
 * and periodically sends out domain notifications to domain listeners
 */
public class ClientUpdateHandler extends UpdateHandler
{
	
	/**
	 * Constructor
	 */
	public ClientUpdateHandler()
	{
		super();
		_waitIncrement = 200;
	}

	/**
	 * Not applicable - this is only applicable on the server side
	 */
	public void updateFile(String path, byte[] bytes, int size, boolean binary)
	{
	}

	/**
	 * Not applicable - this is only applicable on the server side
	 */
	public void updateAppendFile(String path, byte[] bytes, int size, boolean binary)
	{
	}

	/**
	 * Not applicable - this is only applicable on the server side
	 */
	public void updateFile(String path, byte[] bytes, int size, boolean binary, String byteStreamHandlerId)
	{
	}

	/**
	 * Not applicable - this is only applicable on the server side
	 */
	public void updateAppendFile(String path, byte[] bytes, int size, boolean binary, String byteStreamHandlerId)
	{
	}
	
	/**
	 * Notifies domain listeners that a file has been updated
	 * @param file the updated file
	 * @param object the element associated with the updated file
	 */	
	public void updateFile(File file, DataElement object)
	{
		IDomainNotifier notifier = _dataStore.getDomainNotifier();
		notifier.fireDomainChanged(new DomainEvent(DomainEvent.FILE_CHANGE, object, DE.P_NESTED));
	}

	/**
	 * Periodically called to notify domain listeners of updated data from the
	 * server
	 */
	public void sendUpdates()
	{
		if (_dataStore != null && !isFinished())
		{
			IDomainNotifier notifier = _dataStore.getDomainNotifier();
			while (_dataObjects.size() > 0)
			{
				DataElement object = null;
				synchronized (_dataObjects)
				{
					if (_dataObjects.size() > 0)
					{
						object = (DataElement) _dataObjects.get(0);
						_dataObjects.remove(object);
					}
				}

				if ((object != null))
				{
					
					if (!object.isUpdated() && !object.isDescriptor())
					{

						//DataElement parent = object.getParent();
						//System.out.println("notifying "+parent);
						notify(object);				
					}
					clean(object);
				}
			}
		}
	}

	private void notify(DataElement object)
	{	
		if (object.isExpanded())
		{
			object.setUpdated(true);
		}

		object.setExpanded(true);

		IDomainNotifier notifier = _dataStore.getDomainNotifier();

		if (object.getNestedSize() == 0)
		{
			notifier.fireDomainChanged(new DomainEvent(DomainEvent.NON_STRUCTURE_CHANGE, object, DE.P_NESTED));

		}
		else
		{
			notifier.fireDomainChanged(new DomainEvent(DomainEvent.INSERT, object, DE.P_NESTED));
		}
	}
	
	/**
	 * Implemented to provide the means by which classes are requested
	 * across the comm channel. (Only applies to ServerUpdateHandler, so is a dummy method here)
	 * @param className the name of the class to request
	 */
	public void requestClass(String className)
	{
	}

	/**
	 * Impleted to provide the means by which a class on the host is updated on the client
	 * @param runnable
	 * @param deserializebyteStreamHandlerId
	 */
	public synchronized void updateClassInstance(IRemoteClassInstance runnable, String deserializebyteStreamHandlerId)
	{
		notifyInput();
	}

	/**
	 * Does not apply in this case. Use ClientCommandHandler.sendClass().
	 */
	public void sendClass(String className, String classByteStreamHandlerId) 
	{		
	}

	/**
	 * Does not apply in this case. Use ClientCommandHandler.sendClass().
	 */
	public void sendClass(String className) 
	{
	}

	/**
	 * Does not apply in this case. Use ClientCommandHandler.sendKeepAliveRequest().
	 */
	public void sendKeepAliveRequest() 
	{
	}

	/**
	 * Does not apply in this case. Use ClientCommandHandler.sendKeepAliveConfirmation().
	 */
	public void sendKeepAliveConfirmation() 
	{
	}

	
}