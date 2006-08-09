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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;

/**
 * This class is used for receiving data from a socket in the DataStore 
 * communication layer.
 */
public abstract class Receiver extends Thread
{


	private Socket _socket;

	protected DataStore _dataStore;

	private XMLparser _xmlParser;
	private BufferedInputStream _in;

	protected boolean _canExit;

	/**
	 * Constructor
	 * @param socket the socket to read from
	 * @param dataStore the associated DataStore
	 */
	public Receiver(Socket socket, DataStore dataStore)
	{
		 setName("DStore Receiver"+getName());
		_socket = socket;
		_dataStore = dataStore;
		_canExit = false;
		_xmlParser = new XMLparser(dataStore);

		try
		{
			_in = new BufferedInputStream(socket.getInputStream());
		}
		catch (UnknownHostException uhe)
		{
			//System.out.println("Receiver:" + uhe);
		}
		catch (IOException ioe)
		{
			//System.out.println("Receiver:" + ioe);
		}
	}

	/**
	 * Called when a DataStore connection is terminated.
	 */
	public void finish()
	{
		_canExit = true;
	}

	/**
	 * Indicates that the receiver can stop receiving data from the socket.
	 * @return true if the receiver can stop
	 */
	public boolean canExit()
	{
		return _canExit;
	}

	/**
	 * Called when the receiver thread is running
	 */
	public void run()
	{
		try
		{
			while (!_canExit)
			{
				handleInput();
			}
		}
		catch (Exception e)
		{
			_canExit = true;
			e.printStackTrace();
			handleError(e);
		}
	}

	/**
	 * Periodically called to receive data from the socket
	 */
	public void handleInput()
	{
		try
		{
			// wait on the socket
			DataElement rootObject = _xmlParser.parseDocument(_in, _socket);

			if (rootObject != null)
			{
				String type = rootObject.getType();
				if (!type.equals("FILE"))
				{

					handleDocument(rootObject);
				}
			}
			else
			{
				// something really bad happened
				_canExit = true;
				if (_xmlParser.getPanicException() != null)
					handleError(_xmlParser.getPanicException());
			}
		}
		catch (IOException ioe)
		{
			_canExit = true;
			handleError(ioe);
		}
		catch (Exception e)
		{
			handleError(e);
		}
	}

	/**
	 * Returns the associated socket
	 * @return the socket
	 */
	public Socket socket()
	{
		return _socket;
	}

	/**
	 * Implemented to provide a means of handling received input
	 * @param documentObject the root object of the received data 
	 */
	public abstract void handleDocument(DataElement documentObject);

	/**
	 * Implemented to provide a means of handling errors in the communication layer
	 * @param e an exception that occurred
	 */
	public abstract void handleError(Throwable e);
}