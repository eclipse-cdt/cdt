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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.eclipse.dstore.core.model.CommandHandler;
import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.dstore.core.model.DataStoreAttributes;
import org.eclipse.dstore.core.model.IDataStoreConstants;
import org.eclipse.dstore.core.model.ISSLProperties;
import org.eclipse.dstore.core.server.ServerCommandHandler;
import org.eclipse.dstore.core.server.ServerLauncher;
import org.eclipse.dstore.core.util.ExternalLoader;
import org.eclipse.dstore.core.util.Sender;
import org.eclipse.dstore.core.util.ssl.DStoreSSLContext;
import org.eclipse.dstore.core.util.ssl.DataStoreTrustManager;
import org.eclipse.dstore.extra.internal.extra.DomainNotifier;


/**
 * ClientConnection provides the standard means of creating a new connection to
 * a DataStore.  
 *
 * <li>
 * If a connection is local, then a DataStore is instantiated
 * in the same process to be used by the client to communicate with miners (tools).
 * </li>
 * 
 * <li>
 * If a connection is not local, then a virtual DataStore is instantiated in the 
 * current process to communicate with the remote DataStore.  If the client wishes
 * to instantiate a remote DataStore through a daemon, then ClientConnection first connects
 * to the daemon requesting a DataStore at a given port, and then connects to the
 * newly launched DataStore.  Otherwise, a DataStore is expected to be running on 
 * the remote machine under the same port that the client tries to connect to.
 * </li>
 * 
 *
 */
public class ClientConnection implements IDataStoreConstants
{


	private ClientAttributes _clientAttributes;

	private Socket _theSocket;
	private boolean _isConnected = false;
	private boolean _isRemote = false;
	private DataStore _dataStore;
	private DomainNotifier _domainNotifier;
	private Sender _sender;
	private ClientReceiver _receiver;
	private ClientUpdateHandler _updateHandler;
	private CommandHandler _commandHandler;
	
	private int _clientVersion;
	private int _clientMinor;


	private String _name;
	private String _host;
	private String _port;
	private String _hostDirectory;
	private Socket _launchSocket;
	
	private DataStoreTrustManager _trustManager;

	private ArrayList _loaders;

	private static final int HANDSHAKE_INCORRECT = 0;
	private static final int HANDSHAKE_SERVER_OLDER = 1;
	private static final int HANDSHAKE_CORRECT = 2;
	private static final int HANDSHAKE_UNEXPECTED = 3;
	private static final int HANDSHAKE_SERVER_NEWER = 4;
	private static final int HANDSHAKE_SERVER_RECENT_OLDER = 5;
	private static final int HANDSHAKE_SERVER_RECENT_NEWER = 6;
	private static final int HANDSHAKE_TIMEOUT = 7;
	
	private static final int VERSION_INDEX_PROTOCOL = 0;
	private static final int VERSION_INDEX_VERSION  = 1;
	private static final int VERSION_INDEX_MINOR    = 2;
	public static String INCOMPATIBLE_SERVER_UPDATE = "Incompatible DataStore.";
	public static String INCOMPATIBLE_CLIENT_UPDATE = "Incompatible DataStore.";
	public static String SERVER_OLDER = "Older DataStore Server.";
	public static String CLIENT_OLDER = "Older DataStore Client.";
	public static String INCOMPATIBLE_PROTOCOL = "Incompatible Protocol.";
	public static String CANNOT_CONNECT = "Cannot connect to server.";

	/**
	 * Creates a new ClientConnection instance
	 *
	 * @param name an identifier for this connection
	 */
	public ClientConnection(String name)
	{
		_domainNotifier = new DomainNotifier();
		_name = name;
		init();
	}

	/**
	 * Creates a new ClientConnection instance
	 *
	 * @param name an identifier for this connection
	 * @param initialSize the number of elements to preallocate in the DataStore
	 */
	public ClientConnection(String name, int initialSize)
	{
		_domainNotifier = new DomainNotifier();
		_name = name;
		init(initialSize);
	}

	/**
	 * Creates a new ClientConnection instance
	 *
	 * @param name an identifier for this connection
	 * @param notifier the notifier used to keep the user interface in synch with the DataStore
	 */
	public ClientConnection(String name, DomainNotifier notifier)
	{
		_domainNotifier = notifier;
		_name = name;
		init();
	}

	/**
	 * Creates a new ClientConnection instance
	 *
	 * @param name an identifier for this connection
	 * @param notifier the notifier used to keep the user interface in synch with the DataStore
	 * @param initialSize the number of elements to preallocate in the DataStore
	 */
	public ClientConnection(String name, DomainNotifier notifier, int initialSize)
	{
		_domainNotifier = notifier;
		_name = name;
		init(initialSize);
	}

	
	public int getClientVersion()
	{
		return _clientVersion;
	}
	
	public int getClientMinor()
	{
		return _clientMinor;
	}
	
	public int getServerVersion()
	{
		return _dataStore.getServerVersion();
	}
	
	public int getServerMinor()
	{
		return _dataStore.getServerMinor();
	}
	
	public void setSSLProperties(ISSLProperties properties)
	{
		_dataStore.setSSLProperties(properties);
	}



	/**
	 * Specifies the loaders used to instantiate the miners
	 *
	 * @param loaders the loaders 
	 */
	public void setLoaders(ArrayList loaders)
	{
		_loaders = loaders;
	}

	/**
	 * Adds a loader to be used to instantiate the miners
	 *
	 * @param loader the loader 
	 */
	public void addLoader(ExternalLoader loader)
	{
		if (_loaders == null)
		{
			_loaders = new ArrayList();
		}
		_loaders.add(loader);
	}

	/**
	 * Specifies the hostname or IP of the host to connect to 
	 *
	 * @param host the hostname or IP of the machine to connect to
	 */
	public void setHost(String host)
	{
		_host = host;
		_clientAttributes.setAttribute(DataStoreAttributes.A_HOST_NAME, _host);
	}

	/**
	 * Specifies the number of the socket port to connect to 
	 *
	 * @param port the number of the socket port to connect to
	 */
	public void setPort(String port)
	{
		if (port == null || port.length() == 0)
		{
			port = "0";
		}

		_port = port;
		_clientAttributes.setAttribute(DataStoreAttributes.A_HOST_PORT, _port);
	}

	/**
	 * Specifies the default working directory on the remote machine 
	 *
	 * @param directory the remote working directory
	 */
	public void setHostDirectory(String directory)
	{
		_hostDirectory = directory;
		_clientAttributes.setAttribute(DataStoreAttributes.A_HOST_PATH, _hostDirectory);
	}

	/**
	 * Returns the hostname/IP of the host to connect to 
	 *
	 * @return the hostname/IP
	 */
	public String getHost()
	{
		return _host;
	}

	/**
	 * Returns the number of the socket port to connect to 
	 *
	 * @return the number of the socket port to connect to
	 */
	public String getPort()
	{
		return _port;
	}

	/**
	 * Returns the default working directory on the host machine 
	 *
	 * @return the working directory on the host
	 */
	public String getHostDirectory()
	{
		return _hostDirectory;
	}

	/**
	 * Indicates whether the client is connected to the DataStore 
	 *
	 * @return whether the client is connected
	 */
	public boolean isConnected()
	{
		if (_isConnected)
		{
			return _dataStore.isConnected();
		}

		return _isConnected;
	}

	/**
	 * Disconnects from the DataStore and cleans up DataStore meta-information 
	 */
	public void disconnect()
	{
		if (_isConnected)
		{
			_dataStore.setConnected(false);

			if (_isRemote)
			{
				_commandHandler.command(
					_dataStore.find(_dataStore.getRoot(), DE.A_NAME, "Exit"),
					_dataStore.getHostRoot(),
					false);
				_receiver.finish();
			}

			_commandHandler.finish();

			try
			{
				Thread.sleep(200);
			}
			catch (InterruptedException e)
			{
				System.out.println(e);
			}

			_updateHandler.finish();
			_dataStore.finish();

			_isConnected = false;
		}
	}

	/**
	 * Creates and connects to a local DataStore to work with in the current process.
	 *
	 * @return the status of the DataStore connection
	 */
	public ConnectionStatus localConnect()
	{
		_updateHandler = new ClientUpdateHandler();
		_updateHandler.start();

		if (_loaders == null)
		{
			_loaders = new ArrayList();
			_loaders.add(new ExternalLoader(getClass().getClassLoader(), "*"));
		}

		_commandHandler = new ServerCommandHandler(_loaders);
		_commandHandler.start();

		_dataStore.setCommandHandler(_commandHandler);
		_dataStore.setUpdateHandler(_updateHandler);
		_dataStore.setConnected(true);
		_dataStore.setLoaders(_loaders);
		_dataStore.getDomainNotifier().enable(true);

		_commandHandler.setDataStore(_dataStore);
		_updateHandler.setDataStore(_dataStore);
		((ServerCommandHandler) _commandHandler).loadMiners();

		_clientAttributes.setAttribute(
			DataStoreAttributes.A_LOCAL_NAME,
			_clientAttributes.getAttribute(DataStoreAttributes.A_HOST_NAME));
		_clientAttributes.setAttribute(
			DataStoreAttributes.A_LOCAL_PATH,
			_clientAttributes.getAttribute(DataStoreAttributes.A_HOST_PATH));

		_isConnected = true;

		DataElement ticket = _dataStore.getTicket();
		ticket.setAttribute(DE.A_NAME, "null");

		ConnectionStatus result = new ConnectionStatus(_isConnected);
		result.setTicket(ticket.getName());

		return result;
	}

	/**
	 * Connects to a remote DataStore by first communicating with a remote daemon and then
	 * connecting to the DataStore.
	 *
	 * @param launchServer an indication of whether to launch a DataStore on the daemon on not
	 * @param user the user ID of the current user on the remote machine
	 * @param password the password of the current user on the remote machine
	 * @return the status of the connection
	 */
	public ConnectionStatus connect(boolean launchServer, String user, String password)
	{
		ConnectionStatus launchStatus = null;
		if (launchServer)
		{
			launchStatus = launchServer(user, password);
			if (!launchStatus.isConnected())
			{
				return launchStatus;
			}
		}
		else
		{
			launchStatus = new ConnectionStatus(true);
			launchStatus.setTicket("null");
		}

		return connect(launchStatus.getTicket());
	}
	
	public DataStoreTrustManager getTrustManager()
	{
		if (_trustManager == null)
		{
			_trustManager = new DataStoreTrustManager();
		}
		return _trustManager;
	}

	/**
	 * Connects to a remote DataStore.   
	 * A socket is created and the virtual DataStore is initialized with an update handler, command handler, 
	 * socket sender and socket receiver. 
	 *
	 * @param ticket the ticket required to be granted access to the remote DataStore
	 * @return the status of the connection
	 */
	public ConnectionStatus connect(String ticket)
	{
		return connect(ticket, 0);
	}
	
	public ConnectionStatus connect(String ticket, int timeout)
	{
		boolean doTimeOut = (timeout > 0);
		ConnectionStatus result = null;
		try
		{
			int port = 0;
			if (_port != null && _port.length() > 0)
			{
				port = Integer.parseInt(_port);
			}

			if (!_dataStore.usingSSL())
			{
				_theSocket = new Socket(_host, port);
				if (doTimeOut && (_theSocket != null))
					_theSocket.setSoTimeout(timeout);
			}
			else
			{
				String location = _dataStore.getKeyStoreLocation();
				String pw = _dataStore.getKeyStorePassword();
				DataStoreTrustManager mgr = getTrustManager();
				SSLContext context = DStoreSSLContext.getClientSSLContext(location, pw, mgr);
				SSLSocketFactory factory = context.getSocketFactory();	

				_theSocket = factory.createSocket(_host, port);
				if (doTimeOut && (_theSocket != null))
					_theSocket.setSoTimeout(timeout);
				try
				{

					((SSLSocket) _theSocket).startHandshake();
					SSLSession session = ((SSLSocket) _theSocket).getSession();

				}
				catch (SSLHandshakeException e)
				{
					result = new ConnectionStatus(false, e, true, mgr.getUntrustedCerts());
					return result;
				}
				catch (Exception e)
				{
					e.printStackTrace();
					_theSocket.close();
					result = new ConnectionStatus(false, e);
					return result;
				}
			}

			String msg = null;
			int handshakeResult = doHandShake();
			switch (handshakeResult)
			{
			case HANDSHAKE_CORRECT:
				result = doConnect(ticket);
				break;
			case HANDSHAKE_SERVER_RECENT_NEWER:
				result = doConnect(ticket);
				result.setMessage(CLIENT_OLDER);
				break;
			case HANDSHAKE_SERVER_RECENT_OLDER:
				result = doConnect(ticket);
				result.setMessage(SERVER_OLDER);
				break;
			case HANDSHAKE_SERVER_NEWER:
			{
				msg = INCOMPATIBLE_CLIENT_UPDATE;
				msg += "\nThe server running on "
					+ _host
					+ " under port "
					+ _port
					+ " is a newer DataStore server.";
				break;
			}
			case HANDSHAKE_SERVER_OLDER:
			{
				msg = INCOMPATIBLE_SERVER_UPDATE;
				msg += "\nThe server running on "
					+ _host
					+ " under port "
					+ _port
					+ " is an older DataStore server.";
				break;
			}
			case HANDSHAKE_INCORRECT:
			{
				msg = CANNOT_CONNECT;
				msg += INCOMPATIBLE_PROTOCOL;
				msg += "\nThe server running on "
					+ _host
					+ " under port "
					+ _port
					+ " is not a valid DataStore server.";
				break;
			}
			case HANDSHAKE_UNEXPECTED:
			{
				msg = CANNOT_CONNECT;
				msg += "Unexpected exception.";
				break;
			}
			case HANDSHAKE_TIMEOUT:
			{
				msg = CANNOT_CONNECT;
				msg += "Timeout waiting for socket activity.";
				break;
			}	
			default:
				break;
			}
	
			if (result == null && msg != null)
			{
				result = new ConnectionStatus(false, msg);
			}
		}
		catch (java.net.ConnectException e)
		{
			String msg = "Connection Refused.";
			msg += "\nMake sure that the DataStore server is running on " + _host + " under port " + _port + ".";
			result = new ConnectionStatus(false, msg);
		}
		catch (UnknownHostException uhe)
		{
			_isConnected = false;
			result = new ConnectionStatus(_isConnected, uhe);
		}
		catch (IOException ioe)
		{
			_isConnected = false;
			result = new ConnectionStatus(_isConnected, ioe);
		}

		return result;		
	}
	
	protected ConnectionStatus doConnect(String ticket)
	{
		_sender = new Sender(_theSocket, _dataStore);
		_updateHandler = new ClientUpdateHandler();
		_updateHandler.start();

		_commandHandler = new ClientCommandHandler(_sender);
		_commandHandler.start();

		_dataStore.setCommandHandler(_commandHandler);
		_dataStore.setUpdateHandler(_updateHandler);
		_dataStore.setConnected(true);
		_dataStore.getDomainNotifier().enable(true);

		_commandHandler.setDataStore(_dataStore);
		_updateHandler.setDataStore(_dataStore);

		_receiver = new ClientReceiver(_theSocket, _dataStore);
		_receiver.start();

		_isConnected = true;
		_isRemote = true;
		ConnectionStatus result = new ConnectionStatus(_isConnected);
		result.setTicket(ticket);
		return result;
	}

	/**
	 * Connects to a remote daemon and tells the daemon to launch
	 * a DataStore server.   
	 *
	 * @param user the user ID of the current user on the remote machine
	 * @param password the password of the current user on the remote machine
	 * @return the status of the connection
	 */
	public ConnectionStatus launchServer(String user, String password)
	{
		// default daemon port is 4035
		return launchServer(user, password, ServerLauncher.DEFAULT_DAEMON_PORT);
	}

	/**
	 * Connects to a remote daemon and tells the daemon to launch
	 * a DataStore server.   
	 *
	 * @param user the user ID of the current user on the remote machine
	 * @param password the password of the current user on the remote machine
	 * @param daemonPort the port of the daemon
	 * @return the status of the connection
	 */
	public ConnectionStatus launchServer(String user, String password, int daemonPort)
	{
		return launchServer(user, password, daemonPort, 0);
	}
	
	public ConnectionStatus launchServer(String user, String password, int daemonPort, int timeout)
	{
		ConnectionStatus result = connectDaemon(daemonPort);
		boolean doTimeOut = timeout > 0;
		if (!result.isConnected()) {
			return result;
		}
		try
		{
			PrintWriter writer = null;
			BufferedReader reader = null;

			// create output stream for server launcher
			try
			{
				if (doTimeOut) _launchSocket.setSoTimeout(timeout);
				writer = new PrintWriter(new OutputStreamWriter(_launchSocket.getOutputStream(), DE.ENCODING_UTF_8));
				writer.println(user);
				writer.println(password);
				writer.println(_port);
				writer.flush();

				reader = new BufferedReader(new InputStreamReader(_launchSocket.getInputStream(), DE.ENCODING_UTF_8));
				String status = null;
				try
				{
					status = reader.readLine();
				}
				catch (InterruptedIOException e)
				{
					result = new ConnectionStatus(false, e);
				}


				if (status != null && !status.equals(CONNECTED))
				{
					result = new ConnectionStatus(false, status);
				}
				else
				{
					result = new ConnectionStatus(true);

					_port = reader.readLine();
					String ticket = reader.readLine();
					result.setTicket(ticket);
				}
			}
			catch (java.io.IOException e)
			{
				e.printStackTrace();
				result = new ConnectionStatus(false, e);
			}

			if (reader != null)
				reader.close();

			if (writer != null)
				writer.close();
			_launchSocket.close();
		}
		catch (IOException ioe)
		{
			System.out.println(ioe);
			ioe.printStackTrace();
			result = new ConnectionStatus(false, ioe);
		}

		return result;
	}

	/**
	 * Connect to a remote daemon
	 * 
	 * @param daemonPort the port of the daemon
	 * @return the status of the connection
	 */
	public ConnectionStatus connectDaemon(int daemonPort) {
		ConnectionStatus result = new ConnectionStatus(true);
		try
		{
			_launchSocket = null;
			if (_dataStore.usingSSL())
			{
				try
				{
					String location = _dataStore.getKeyStoreLocation();
					String pw = _dataStore.getKeyStorePassword();
					DataStoreTrustManager mgr = getTrustManager();
					SSLContext context = DStoreSSLContext.getClientSSLContext(location, pw, mgr);
				
					try
					{
						SocketFactory factory = context.getSocketFactory();
						SSLSocket lSocket = (SSLSocket) factory.createSocket(_host, daemonPort);
						_launchSocket = lSocket;

						lSocket.startHandshake();
		

						SSLSession session = lSocket.getSession();
						if (session == null)
						{
							System.out.println("handshake failed");
							lSocket.close();
						}
					}
					catch (SSLHandshakeException e)
					{						

						result = new ConnectionStatus(false, e, true, mgr.getUntrustedCerts());
						return result;
					}
					catch (Exception e)
					{
						if (_launchSocket != null)
						{
							_launchSocket.close();
						}
						e.printStackTrace();
						result = new ConnectionStatus(false, e);
						
						return result;
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			else
			{
				_launchSocket = new Socket(_host, daemonPort);
			}
		}
		catch (java.net.ConnectException e)
		{
			String msg = "Connection Refused.";
			msg += "\nMake sure that the DataStore daemon is running on " + _host + ".";
			result = new ConnectionStatus(false, msg);
		}
		catch (UnknownHostException uhe)
		{
			result = new ConnectionStatus(false, uhe);
		}
		catch (IOException ioe)
		{
			System.out.println(ioe);
			ioe.printStackTrace();
			result = new ConnectionStatus(false, ioe);
		}

		return result;
	}
	
	/**
	 * Reeturns the launch socket
	 * 
	 * @return the launch socket
	 */
	public Socket getLaunchSocket() {
		return _launchSocket;
	}
	
	/**
	 * Returns the DataStore that the client is connected to.
	 * @return the DataStore
	 */
	public DataStore getDataStore()
	{
		return _dataStore;
	}

	private void init()
	{
		init(10000);
	}

	private void init(int initialSize)
	{
		_clientAttributes = new ClientAttributes();
		_clientAttributes.setAttribute(ClientAttributes.A_ROOT_NAME, _name);


		_dataStore = new DataStore(_clientAttributes, initialSize);
		_dataStore.setDomainNotifier(_domainNotifier);
		_dataStore.createRoot();

		_host = _clientAttributes.getAttribute(ClientAttributes.A_HOST_NAME);
		_hostDirectory = _clientAttributes.getAttribute(ClientAttributes.A_HOST_PATH);
		_port = _clientAttributes.getAttribute(ClientAttributes.A_HOST_PORT);
	}

	private void flush(DataElement object)
	{
		_dataStore.flush(object);
	}

	private void flush()
	{
		_dataStore.flush(_dataStore.getHostRoot());
		_dataStore.flush(_dataStore.getLogRoot());
		_dataStore.flush(_dataStore.getDescriptorRoot());
		_dataStore.createRoot();
	}
	private int doHandShake()
	{
		try
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(_theSocket.getInputStream(), DE.ENCODING_UTF_8));
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(_theSocket.getOutputStream(), DE.ENCODING_UTF_8));
			writer.println("");
			writer.println("");
			writer.println("");
			writer.flush();
			
			String handshake = null;
			try
			{
				handshake = reader.readLine();
			}
			catch (InterruptedIOException e)
			{
				return HANDSHAKE_TIMEOUT;
			}
			_theSocket.setSoTimeout(0);

			String[] clientVersionStr = DataStoreAttributes.DATASTORE_VERSION.split("\\.");			
			String[] serverVersionStr = handshake.split("\\.");

			_dataStore.setServerVersion(Integer.parseInt(serverVersionStr[VERSION_INDEX_VERSION]));
			_dataStore.setServerMinor(Integer.parseInt(serverVersionStr[VERSION_INDEX_MINOR]));
			
			
			if (handshake.equals(DataStoreAttributes.DATASTORE_VERSION))
			{
				return HANDSHAKE_CORRECT;
			}
			else
			{
				if (handshake.startsWith("<DataElement"))
				{
					return HANDSHAKE_SERVER_OLDER;
				}
				else if (serverVersionStr[VERSION_INDEX_PROTOCOL].equals(clientVersionStr[VERSION_INDEX_PROTOCOL]))
				{
					
					
					if (_dataStore.getServerVersion()== _clientVersion)
					{
						// major versions match so should be compatible
						return HANDSHAKE_CORRECT;
					}
					else
					{
						if (_dataStore.getServerVersion()> _clientVersion)
						{
							// newer server
							if (_dataStore.getServerVersion() - 1 == _clientVersion)
							{
								return HANDSHAKE_SERVER_RECENT_NEWER;
							}
							else
							{
								return HANDSHAKE_SERVER_NEWER;
							}
						}
						else
						{
							// newer client
							if (_dataStore.getServerVersion() + 1 == _clientVersion)
							{
								return HANDSHAKE_SERVER_RECENT_OLDER;
							}
							else
							{							
								return HANDSHAKE_SERVER_OLDER;
							}
						}
					}
				}
				else
				{
					System.out.println("handshake=" + handshake);
					return HANDSHAKE_INCORRECT;
				}
			}

		}
		catch (Exception e)
		{
			return HANDSHAKE_UNEXPECTED;
		}

	}
		
	public boolean isKnownStatus(String status)
	{
		return  status.equals(CONNECTED) ||
				status.equals(AUTHENTICATION_FAILED) ||
				status.equals(UNKNOWN_PROBLEM) ||
				status.startsWith(SERVER_FAILURE) ||
				status.equals(PASSWORD_EXPIRED) ||
				status.equals(NEW_PASSWORD_INVALID);
	}
}