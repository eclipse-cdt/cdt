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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.dstore.core.model.DataStoreAttributes;
import org.eclipse.dstore.core.model.ISSLProperties;
import org.eclipse.dstore.core.util.ExternalLoader;
import org.eclipse.dstore.core.util.Sender;
import org.eclipse.dstore.core.util.ssl.DStoreSSLContext;

/**
 * ConnectionEstablisher is responsible for managing the server DataStore and 
 * facilitating the communication between client and server DataStores.
 *
 */
public class ConnectionEstablisher
{


	private ServerSocket _serverSocket;
	private static boolean _continue;

	private ArrayList _receivers;

	private ServerCommandHandler _commandHandler;
	private ServerUpdateHandler _updateHandler;

	private ServerAttributes _serverAttributes = new ServerAttributes();
	private DataStore _dataStore;

	private int _maxConnections;
	private int _timeout;

	
	/**
	 * Creates the default ConnectionEstablisher.  Communication occurs
	 * on a default port, there is no timeout and no ticket is required
	 * for a client to work with the DataStore.
	 * 
	 */
	public ConnectionEstablisher()
	{
		String port = _serverAttributes.getAttribute(DataStoreAttributes.A_HOST_PORT);
		setup(port, null, null);
	}

	/**
	 * Creates a ConnectionEstablisher.  Communication occurs
	 * on the specified port, there is no timeout and no ticket is required
	 * for a client to work with the DataStore.
	 * 
	 * @param port the number of the socket port
	 */
	public ConnectionEstablisher(String port)
	{
		setup(port, null, null);
	}

	/**
	 * Creates a ConnectionEstablisher.  Communication occurs
	 * on the specified port, a timeout value indicates the idle wait
	 * time before shutting down, and no ticket is required
	 * for a client to work with the DataStore.
	 * 
	 * @param port the number of the socket port
	 * @param timeout the idle duration to wait before shutting down
	 */
	public ConnectionEstablisher(String port, String timeout)
	{
		setup(port, timeout, null);
	}
	
	/**
	 * Creates a ConnectionEstablisher.  Communication occurs
	 * on the specified port, a timeout value indicates the idle wait
	 * time before shutting down, and ticket specified the required
	 * ticket for a client to present in order to work with the DataStore.
	 *
	 * @param port the number of the socket port
	 * @param timeout the idle duration to wait before shutting down
	 * @param ticket validation id required by the client to access the DataStore
	 */
	public ConnectionEstablisher(String port, String timeout, String ticket)
	{
		setup(port, timeout, ticket);
	}

	
	/**
	 * Starts the run loop for the ConnectionEstablisher.
	 */
	public void start()
	{
		run();
	}



	/**
	 * Returns the DataStore.
	 *
	 * @return the DataStore
	 */
	public DataStore getDataStore()
	{
		return _dataStore;
	}

	/**
	 * Tells the connection establisher to clean up and shutdown
	 */
	public void finished(ServerReceiver receiver)
	{
		_updateHandler.removeSenderWith(receiver.socket());
		_receivers.remove(receiver);
		//if (_receivers.size() == 0)
		{
			_continue = false;
			_commandHandler.finish();
			_updateHandler.finish();
			_dataStore.finish();
			System.out.println(ServerReturnCodes.RC_FINISHED);
			System.exit(0);
		}
	}

	private void waitForConnections()
	{
		while (_continue == true)
		{
			try
			{
				Socket newSocket = _serverSocket.accept();
				if (_dataStore.usingSSL())
				{
				
					// wait for connection
					SSLSocket sslSocket = (SSLSocket)newSocket;
					sslSocket.setUseClientMode(false);
					sslSocket.setNeedClientAuth(false);
					SSLSession session = sslSocket.getSession();

					if (session == null)
					{
						System.out.println("handshake failed");
						sslSocket.close();
						return;
					}
				}
				
				doHandShake(newSocket);
				newSocket.setKeepAlive(true);

				ServerReceiver receiver = new ServerReceiver(newSocket, this);
				Sender sender = new Sender(newSocket, _dataStore);

				// add this connection to list of elements
				_receivers.add(receiver);
				_updateHandler.addSender(sender);

				receiver.start();

				if (_receivers.size() == 1)
				{					
					_updateHandler.start();
					_commandHandler.start();
				}

				if (_receivers.size() == _maxConnections)
				{
					_continue = false;
					_serverSocket.close();

				}
			}
			catch (IOException ioe)
			{
				System.err.println(ServerReturnCodes.RC_CONNECTION_ERROR);
				System.err.println("Server: error initializing socket: " + ioe);
				_continue = false;
			}
		}
	}

	
	
	
	private ServerSocket createSocket(String portStr) throws UnknownHostException
	{
		ServerSocket serverSocket = null;
		SSLContext sslContext = null;
		// port	
		int port = 0;
		
		if (_dataStore.usingSSL())
		{
			String keyStoreFileName = _dataStore.getKeyStoreLocation();
			String keyStorePassword = _dataStore.getKeyStorePassword();
							
			try
			{
				sslContext = DStoreSSLContext.getServerSSLContext(keyStoreFileName, keyStorePassword);
			}
			catch (Exception e)
			{
				
			}
		}

		// determine if portStr is a port range or just a port
		String[] range = portStr.split("-");
		if (range.length == 2)
		{
			int lPort = 0;
			int hPort = 0;
			try
			{
				lPort = Integer.parseInt(range[0]);
				hPort = Integer.parseInt(range[1]);		
			}
			catch (Exception e)
			{
			}
		
			for (int i = lPort; i < hPort; i++)
			{							
				// create server socket from port
				try
				{
					if (_dataStore.usingSSL())
					{
						try
						{
							serverSocket = sslContext.getServerSocketFactory().createServerSocket(i);		
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
					else
					{
						serverSocket = new ServerSocket(i);
					}
				}
				catch (Exception e)
				{
					_dataStore.trace(e);					
				}
				if (serverSocket != null && serverSocket.getLocalPort() > 0)
				{
					return serverSocket;
				}
			}
		}
		else
		{
			port = Integer.parseInt(portStr);
	
			
			// create server socket from port
			if (_dataStore.usingSSL())
			{
				try
				{
					serverSocket = sslContext.getServerSocketFactory().createServerSocket(port);		
				}
				catch (Exception e)
				{
					_dataStore.trace(e);
				}
			}
			else
			{
				try
				{
					serverSocket = new ServerSocket(port);
				}
				catch (Exception e)
				{
					_dataStore.trace(e);
				}
			}
		}
		return serverSocket;
	}
	
	/**
	 * Create the DataStore and initializes it's handlers and communications.
	 *
	 * @param portStr the number of the socket port
	 * @param timeoutStr the idle duration to wait before shutting down
	 * @param ticketStr validation id required by the client to access the DataStore
	 */
	private void setup(String portStr, String timeoutStr, String ticketStr)
	{
		_maxConnections = 1;


		ArrayList loaders = new ArrayList();
		loaders.add(new ExternalLoader(getClass().getClassLoader(), "*"));
		_commandHandler = new ServerCommandHandler(loaders);
		_updateHandler = new ServerUpdateHandler();

		String pluginPath = System.getProperty("A_PLUGIN_PATH");
		ISSLProperties sslProperties = new ServerSSLProperties();
		
		_dataStore = new DataStore(_serverAttributes, _commandHandler, _updateHandler, null);
		_dataStore.setSSLProperties(sslProperties);
		
		DataElement ticket = _dataStore.getTicket();
		ticket.setAttribute(DE.A_NAME, ticketStr);

		_updateHandler.setDataStore(_dataStore);
		_commandHandler.setDataStore(_dataStore);

		_receivers = new ArrayList();
		_continue = true;

		try
		{
			
			_serverSocket = createSocket(portStr);
			if (_serverSocket == null)
			{
				System.err.println(ServerReturnCodes.RC_BIND_ERROR);
				_continue = false;
			}			
			else
			{
				// timeout
				if (timeoutStr != null)
				{
					_timeout = Integer.parseInt(timeoutStr);
				}
				else
				{
					_timeout = 120000;
				}
				
				if (_timeout > 0)
				{
					_serverSocket.setSoTimeout(_timeout);
				}
	
				System.err.println(ServerReturnCodes.RC_SUCCESS);
				System.err.println(_serverSocket.getLocalPort());
				try
				{
					System.err.println("Server running on: " + InetAddress.getLocalHost().getHostName());
				}
				catch (UnknownHostException e)
				{
					// keep running
				}
			}
		}
		catch (UnknownHostException e)
		{
			System.err.println(ServerReturnCodes.RC_UNKNOWN_HOST_ERROR);
			_continue = false;
		}
		catch (BindException e)
		{
			System.err.println(ServerReturnCodes.RC_BIND_ERROR);
			_continue = false;
		}
		catch (IOException e)
		{
			System.err.println(ServerReturnCodes.RC_GENERAL_IO_ERROR);
			_continue = false;
		}
		catch (SecurityException e)
		{
			System.err.println(ServerReturnCodes.RC_SECURITY_ERROR);
			_continue = false;
		}
	}

	private void run()
	{
		waitForConnections();
	}

	private void doHandShake(Socket socket)
	{ 
	   	try
	   	{
			BufferedWriter bwriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), DE.ENCODING_UTF_8));
	   		PrintWriter writer = new PrintWriter(bwriter);

			writer.println(DataStoreAttributes.DATASTORE_VERSION);
			writer.flush();
	   	}
	   	catch (IOException e)
	   	{
	   		System.out.println(e);
	   	}
	   	
	}
}