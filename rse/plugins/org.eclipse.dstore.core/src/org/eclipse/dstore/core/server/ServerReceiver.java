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

import java.net.Socket;

import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.util.Receiver;

/**
 * The ServerReciever is responsible for recieving data from
 * the client side.
 */
public class ServerReceiver extends Receiver
{

	private ConnectionEstablisher _connection;

	/**
	 * Constructor
	 * 
	 * @param socket the socket to receive from
	 * @param connection the connection establisher
	 */
	public ServerReceiver(Socket socket, ConnectionEstablisher connection)
	{
		super(socket, connection.getDataStore());
		_connection = connection;		
	}


	/**
	 * Implementation for handling the receiving on documents on 
	 * the server side.
	 * 
	 * @param documentObject to tree root of received data.
	 */
	public void handleDocument(DataElement documentObject)
	{
		// parse request and determine what is wanted
		for (int a = 0; a < documentObject.getNestedSize(); a++)
		{
			DataElement rootOutput = (DataElement) documentObject.get(a);

			DataElement log = _dataStore.getLogRoot();
			log.addNestedData(rootOutput, false);

			if (rootOutput.getName().equals("C_EXIT"))
			{
				finish();
				
			}
			else
			{
				_dataStore.command(rootOutput);
			}
		}
	}
	
	public void finish()
	{
	    super.finish();
	    _connection.finished(this);
	}

	/**
	 * @see Receiver#finish()
	 */
	public void handleError(Throwable e)
	{
		System.out.println("RECEIVER ERROR");
	//	e.printStackTrace();
		System.out.println(e);
		_connection.finished(this);
	}

}