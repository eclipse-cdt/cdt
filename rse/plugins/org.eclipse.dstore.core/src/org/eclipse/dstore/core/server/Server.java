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
 *  Noriaki Takatsu (IBM)  - [220126] [dstore][api][breaking] Single process server for multiple clients
 *  David McKnight     (IBM)   [224906] [dstore] changes for getting properties and doing exit due to single-process capability
 *  David McKnight   (IBM) - [225507][api][breaking] RSE dstore API leaks non-API types
 *  David McKnight   (IBM) - [226561] [apidoc] Add API markup to RSE Javadocs where extend / implement is allowed
 *  Noriaki Takatsu (IBM)  - [289678][api][breaking] ServerSocket creation in multiple IP addresses
 *******************************************************************************/

package org.eclipse.dstore.core.server;

import java.net.InetAddress;
import java.util.StringTokenizer;

import org.eclipse.dstore.internal.core.server.ServerReturnCodes;

/**
 * Server is the standard way of instantiating and controlling a remote DataStore.
 * The server runs a ConnectionEstablisher which manages client connections to
 * the DataStore.
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class Server implements Runnable
{

	private ConnectionEstablisher _establisher;

	/**
	 * The startup interface to run the Server.
	 *
	 * @param args a list of arguments for running the server.  These consist of
	 * the socket port to wait on, the timeout value, and the the ticket
	 */
	public static void main(String[] args)
	{
		//Tell the Launcher that we are starting
		System.err.println(ServerReturnCodes.RC_DSTORE_SERVER_MAGIC);

		String jversion = System.getProperty("java.version"); //$NON-NLS-1$

		StringTokenizer tokenizer = new StringTokenizer(jversion, "."); //$NON-NLS-1$
		try
		{
			String[] vers = new String[3];
			vers[0] = tokenizer.nextToken();
			vers[1] = tokenizer.nextToken();

			int version = Integer.parseInt(vers[0]);
			int major = Integer.parseInt(vers[1]);


			if (version >= 9 || (version >= 1 && major >= 4))
			{
				// version is good
			}
			else
			{
				// version is bad
				System.err.println(ServerReturnCodes.RC_JRE_VERSION_ERROR);
				if (SystemServiceManager.getInstance().getSystemService() == null)
					System.exit(-1);
			}
		}
		catch (Exception e)
		{
			// version is bad
			System.err.println(ServerReturnCodes.RC_JRE_VERSION_ERROR);
			if (SystemServiceManager.getInstance().getSystemService() == null)
				System.exit(-1);
		}

		try
		{
			Server theServer = null;
			switch (args.length)
			{
				case 0 :
					theServer = new Server();
					break;
				case 1 :
					theServer = new Server(args[0]);
					break;
				case 2 :
					theServer = new Server(args[0], args[1]);
					break;
				case 3 :
					theServer = new Server(args[0], args[1], args[2]);
					break;
				default :
					break;
			}


			if (theServer != null)
			{
				theServer.run();
			}
		}
		catch (SecurityException e)
		{
			System.err.println(ServerReturnCodes.RC_SECURITY_ERROR);
			throw e; // Optional
		}
	}

	/**
	 * Creates a new Server with default DataStore and connection attributes.
	 *
	 */
	public Server()
	{
		_establisher = new ConnectionEstablisher();
	}

	/**
	 * Creates a new Server that waits on the specified socket port.
	 *
	 * @param port the number of the socket port to wait on
	 */
	public Server(String port)
	{
		_establisher = new ConnectionEstablisher(port);
	}

	/**
	 * Creates a new Server that waits on the specified socket port for
	 * the specified time interval before shutting down.
	 *
	 * @param port the number of the socket port to wait on
	 * @param timeout the idle time to wait before shutting down
	 */
	public Server(String port, String timeout)
	{
		_establisher = new ConnectionEstablisher(port, timeout);
	}

	/**
	 * Creates a new Server that waits on the specified socket port for
	 * the specified time interval before shutting down.
	 *
	 * @param port the number of the socket port to wait on
	 * @param timeout the idle time to wait before shutting down
	 * @param ticket the ticket that the client needs to interact with the DataStore
	 */
	public Server(String port, String timeout, String ticket)
	{
		_establisher = new ConnectionEstablisher(port, timeout, ticket);
	}
	
	/**
	 * Creates a new Server that waits on the specified socket port and
	 * the specified IP address with the backlog for
	 * the specified time interval before shutting down.
	 *
	 * @param port the number of the socket port to wait on
	 * @param backlog listen backlog
	 * @param bindAddr the local IP address to bind to
	 * @param timeout the idle time to wait before shutting down
	 * @param ticket the ticket that the client needs to interact with the DataStore
	 * @since 3.2
	 */
	public Server(String port, int backlog, InetAddress bindAddr, String timeout, String ticket)
	{
		_establisher = new ConnectionEstablisher(port, backlog, bindAddr, timeout, ticket);
	}



	/**
	 * Runs the server by starting the ConnectionEstablisher
	 */
	public void run()
	{
		_establisher.start();
	}

	
	/**
	 * Return the reference for the ConnectionEstablisher for this client
	 *
	 * @return the the reference for the ConnectionEstablisher instance for this
	 *         client
	 * @since 3.0
	 */
	public ConnectionEstablisher getEstablisher()
	{
		return _establisher;
	}

}
