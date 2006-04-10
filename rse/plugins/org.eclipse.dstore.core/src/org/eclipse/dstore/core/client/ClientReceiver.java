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

import java.net.Socket;

import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.dstore.core.util.Receiver;

/*
 * The ClientReciever is responsible for recieving data from
 * the server side.
 */
public class ClientReceiver extends Receiver
{

	/**
	 * Constructor
	 */
	public ClientReceiver(Socket socket, DataStore dataStore)
	{
		super(socket, dataStore);
	}

	/**
	 * Called when new data is received from the server side.
	 * @param documentObject the root object of incoming data
	 */
	public void handleDocument(DataElement documentObject)
	{
		if (documentObject.getName().equals("exit"))
		{
			_canExit = true;
		}
		else
		{
			synchronized (documentObject)
			{
				for (int i = 0; i < documentObject.getNestedSize(); i++)
				{
					DataElement rootOutput = documentObject.get(i);
					_dataStore.refresh(rootOutput);
				}
				documentObject.removeNestedData();
				//_dataStore.deleteObject(documentObject.getParent(), documentObject);
			}
		}
	}

	/**
	 * Called when an error occurs
	 * @param e the exception that occurred
	 */
	public void handleError(Throwable e)
	{
		DataElement status = _dataStore.getStatus();
		status.setAttribute(DE.A_NAME, e.getMessage());
		_dataStore.refresh(status);
		_dataStore.setConnected(false);
	}
}