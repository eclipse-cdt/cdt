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
 * David McKnight  (IBM)   [220123][dstore] Configurable timeout on irresponsiveness
 * David McKnight  (IBM)   [222003] Client remains connected after server terminates
 * Noriaki Takatsu (IBM)  - [220126] [dstore][api][breaking] Single process server for multiple clients
 * David McKnight   (IBM) - [225507][api][breaking] RSE dstore API leaks non-API types
 * David McKnight   (IBM) - [226561] [apidoc] Add API markup to RSE Javadocs where extend / implement is allowed
 * Noriaki Takatsu  (IBM) - [289234][multithread][api] Reset and Restart KeepAliveRequestThread
 *******************************************************************************/

package org.eclipse.dstore.core.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.dstore.core.model.IDataStorePreferenceListener;
import org.eclipse.dstore.core.server.SecuredThread;
import org.eclipse.dstore.internal.core.util.XMLparser;

/**
 * This class is used for receiving data from a socket in the DataStore
 * communication layer.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @since 3.0 Moved from non-API to API
 */
public abstract class Receiver extends SecuredThread implements IDataStorePreferenceListener
{


	private Socket _socket;

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
		super(dataStore);
		 setName("DStore Receiver"+getName()); //$NON-NLS-1$
		_socket = socket;
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

		// keepalive preferences
		String keepAliveResponseTimeout = System.getProperty(XMLparser.KEEPALIVE_RESPONSE_TIMEOUT_PREFERENCE);
		if (keepAliveResponseTimeout != null){
			preferenceChanged(XMLparser.KEEPALIVE_RESPONSE_TIMEOUT_PREFERENCE, keepAliveResponseTimeout);
		}
		String iosocketReadTimeout = System.getProperty(XMLparser.IO_SOCKET_READ_TIMEOUT_PREFERENCE);
		if (iosocketReadTimeout != null){
			preferenceChanged(XMLparser.IO_SOCKET_READ_TIMEOUT_PREFERENCE, iosocketReadTimeout);
		}
		String enableKeepAlive = System.getProperty(XMLparser.KEEPALIVE_ENABLED_PREFERENCE);
		if (enableKeepAlive != null){
			preferenceChanged(XMLparser.KEEPALIVE_ENABLED_PREFERENCE, enableKeepAlive);
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
		super.run();
		try
		{
			while (!_canExit)
			{
				handleInput();
			}

			if (_canExit){
				// is this an unexpected exit?
				if (_dataStore.isConnected()){
					// server exited without client exit
					Exception e = new Exception("Server terminated unexpectedly"); //$NON-NLS-1$
					handleError(e);
				}
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
				if (!type.equals("FILE")) //$NON-NLS-1$
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


	public void preferenceChanged(String property, String value)
	{
		//System.out.println("setting preference: "+property + "="+value);
		if (property.equals(XMLparser.IO_SOCKET_READ_TIMEOUT_PREFERENCE)){
			int timeout = Integer.parseInt(value);
			_xmlParser.setIOSocketReadTimeout(timeout);
		}
		else if (property.equals(XMLparser.KEEPALIVE_RESPONSE_TIMEOUT_PREFERENCE)){
			int timeout = Integer.parseInt(value);
			_xmlParser.setKeepaliveResponseTimeout(timeout);
		}
		else if (property.equals(XMLparser.KEEPALIVE_ENABLED_PREFERENCE)){
			boolean enable = true;
			if (value.equals("false")) //$NON-NLS-1$
				enable = false;
			_xmlParser.setEnableKeepalive(enable);
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
		_xmlParser.resetKeepAliveRequest(timeout, socket());
	}
}
