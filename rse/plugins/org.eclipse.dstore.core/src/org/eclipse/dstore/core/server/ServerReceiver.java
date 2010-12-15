/*******************************************************************************
 * Copyright (c) 2002, 2010 IBM Corporation and others.
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
 * David McKnight   (IBM) - [225507][api][breaking] RSE dstore API leaks non-API types
 * Noriaki Takatsu  (IBM) - [227905] prevent double invocations of finished in ConncetionEstablisher
 * David McKnight   (IBM) - [226561] [apidoc] Add API markup to RSE Javadocs where extend / implement is allowed
 * Noriaki Takatsu  (IBM) - [257666] [multithread] TCP/IP socket connection is not closed
 * David McKnight   (IBM) - [257666] modified original patch to simplify
 * Noriaki Takatsu  (IBM) - [283656] [dstore][multithread] Serviceability issue
 * Noriaki Takatsu  (IBM) - [289234][multithread][api] Reset and Restart KeepAliveRequestThread
 *******************************************************************************/

package org.eclipse.dstore.core.server;

import java.io.IOException;
import java.net.Socket;

import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.util.Receiver;

/**
 * The ServerReciever is responsible for recieving data from the client side.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @since 3.0 moved from non-API to API
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
			DataElement rootOutput = documentObject.get(a);

			DataElement log = _dataStore.getLogRoot();
			log.addNestedData(rootOutput, false);

			if (rootOutput.getName().equals("C_EXIT")) //$NON-NLS-1$
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
		_dataStore.setConnected(false);
	    super.finish();
	    _connection.finished(this);
	    try
	    {
	    	socket().close();
	    }
	    catch (IOException e){
	    	if (_dataStore.getClient() != null) {
				_dataStore.getClient().getLogger().logError(this.getClass().toString(), e.toString(), e);
			}
	    	System.out.println(e);
	    }
	}

	/**
	 * @see Receiver#finish()
	 */
	public void handleError(Throwable e)
	{
		if (_dataStore.getClient() != null) {
			_dataStore.getClient().getLogger().logError(this.getClass().toString(), e.toString(), e);
		}
		System.out.println("RECEIVER ERROR"); //$NON-NLS-1$
		e.printStackTrace();
		System.out.println(e);
		_connection.finished(this);
		try
	    {
	    	socket().close();
	    }
	    catch (IOException IOe){
	    	System.out.println(IOe);
	    }
	}
	
	/**
	 * Interrupt the current KeepAliveRequest thread and restart 
	 * the KeepAliveRequest thread with the specified timeout
	 *
	 * @param timeout when the KeepAliveRequest thread is expired
	 * @since 3.3
	 */
	public void resetKeepAliveRequest(long timeout) 
	{
		xmlParser().resetKeepAliveRequest(timeout, socket());
	}

}
