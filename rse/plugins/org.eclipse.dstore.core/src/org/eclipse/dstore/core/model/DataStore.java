/*******************************************************************************
 * Copyright (c) 2002, 2014 IBM Corporation and others.
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
 * Michael Berger (IBM) - 146326 fixed erroneously disconnected dstore elements.
 * Michael Berger (IBM) - 145799 added refresh() method with depth parameter.
 * David McKnight (IBM) - 202822 findDeleted should not be synchronized
 * David McKnight  (IBM)   [220123][dstore] Configurable timeout on irresponsiveness
 * David McKnight  (IBM)  - [222168][dstore] Buffer in DataElement is not sent
 * Noriaki Takatsu (IBM)  - [220126] [dstore][api][breaking] Single process server for multiple clients
 * David McKnight     (IBM)   [224906] [dstore] changes for getting properties and doing exit due to single-process capability
 * David McKnight   (IBM) - [225507][api][breaking] RSE dstore API leaks non-API types
 * David McKnight   (IBM) - [227881] [dstore][threaded] There is no chance to set client object for getUserPreferencesDirectory()
 * Noriaki Takatsu  (IBM) - [228156] [dstore] DataElementRemover thread doesn't terminate after a client disconnects the server
 * David McKnight   (IBM) - [226561] [apidoc] Add API markup to RSE Javadocs where extend / implement is allowed
 * David McKnight   (IBM) - [231639] [dstore] in single-process multi-client mode tracing shouldn't start until the client is set
 * Noriaki Takatsu  (IBM) - [239073] [dstore] [multithread] In multithread, the cache jar should be assigned after the client is set
 * Noriaki Takatsu  (IBM) - [245069] [dstore] dstoreTrace has no timestamp
 * David McKnight   (IBM) - [282634] [dstore] IndexOutOfBoundsException on Disconnect
 * David McKnight   (IBM) - [282599] [dstore] log folder that is not a hidden one
 * David McKnight   (IBM) - [285151] [dstore] Potential threading problem in DataStore (open call)
 * David McKnight   (IBM) - [285301] [dstore] 100% CPU if user does not  have write access to $HOME
 * David McKnight   (IBM) - [287457] [dstore] problems with disconnect when readonly trace file
 * David McKnight   (IBM) - [289891] [dstore] StringIndexOutOfBoundsException in getUserPreferencesDirectory when DSTORE_LOG_DIRECTORY is ""
 * David McKnight   (IBM) - [294933] [dstore] RSE goes into loop
 * David McKnight   (IBM) - [331922] [dstore] enable DataElement recycling
 * David McKnight   (IBM) - [336257] [dstore] leading file.searator in DSTORE_LOG_DIRECTORY not handled
 * David McKnight   (IBM) - [283613] [dstore] Create a Constants File for all System Properties we support
 * David McKnight   (IBM) - [340080] [dstore] empty string should not be allowed as a DataElement ID
 * David McKnight  (IBM)  - [351993] [dstore] not able to connect to server if .eclipse folder not available
 * David McKnight   (IBM) - [366070] [dstore] fix for bug 351993 won't allow tracing if .dstoreTrace doesn't exist
 * David McKnight   (IBM) - [367096] [dstore] DataElement.isSpirit() may return true for newly created DStore objects
 * David McKnight   (IBM) - [370260] [dstore] log the RSE version in server traces
 * David McKnight   (IBM) - [373507] [dstore][multithread] reduce heap memory on disconnect for server
 * David McKnight   (IBM) - [385097] [dstore] DataStore spirit mechanism is not enabled
 * David McKnight   (IBM) - [385793] [dstore] DataStore spirit mechanism and other memory improvements needed
 * David McKnight   (IBM) - [390037] [dstore] Duplicated items in the System view
 * David McKnight   (IBM) - [396440] [dstore] fix issues with the spiriting mechanism and other memory improvements (phase 1)
 * David McKnight   (IBM) - [432875] [dstore] do not use rmt_classloader_cache*.jar
 * David McKnight   (IBM) - [432872] [dstore] enforce secure permission bits for .dstore* logs
 *******************************************************************************/

package org.eclipse.dstore.core.model;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipOutputStream;

import org.eclipse.dstore.core.java.ClassByteStreamHandler;
import org.eclipse.dstore.core.java.ClassByteStreamHandlerRegistry;
import org.eclipse.dstore.core.java.IClassByteStreamHandler;
import org.eclipse.dstore.core.java.IRemoteClassInstance;
import org.eclipse.dstore.core.java.RemoteClassLoader;
import org.eclipse.dstore.core.server.SystemServiceManager;
import org.eclipse.dstore.core.util.StringCompare;
import org.eclipse.dstore.extra.IDomainNotifier;
import org.eclipse.dstore.internal.core.client.ClientCommandHandler;
import org.eclipse.dstore.internal.core.model.DefaultByteConverter;
import org.eclipse.dstore.internal.core.model.IDataStoreSystemProperties;
import org.eclipse.dstore.internal.core.server.ServerUpdateHandler;
import org.eclipse.dstore.internal.core.util.DataElementRemover;
import org.eclipse.dstore.internal.core.util.XMLgenerator;
import org.eclipse.dstore.internal.core.util.XMLparser;

/**
 * <code>DataStore</code> is the heart of the <code>DataStore</code> Distributed Tooling Framework.
 * This class is used for creating, deleting and accessing <code>DataElement</code>s and for communicating commands
 * to miners (tools).
 *
 * <p>
 * Every <code>DataStore</code> has both a command handler and an update handler.  The command
 * handler is responsible for sending commands, in the form of <code>DataElement</code> trees, to the appropriate
 * implementer, either directly to the miner, or indirectly over the communication layer through a server
 * <code>DataStore</code>.  The update handler is responsible for notifying listeners about changes in the
 * <code>DataStore</code>, either directly via a <code>IDomainNotifier</code> or indirectly over the communication
 * layer through a client <code>DataStore</code>.
 * </p>
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class DataStore
{


	private DataStoreAttributes _dataStoreAttributes;

	private RemoteClassLoader _remoteLoader;

	private DataElement _root;
	private DataElement _descriptorRoot;
	private DataElement _logRoot;
	private DataElement _hostRoot;
	private DataElement _minerRoot;
	private DataElement _tempRoot;
	private DataElement _dummy;
	private DataElement _externalRoot;
	private DataElement _status;

	private DataElement _ticket;
	private String  _remoteIP;
	private DataStoreSchema _dataStoreSchema;
	private CommandHandler _commandHandler;
	private UpdateHandler _updateHandler;

	private IByteConverter _byteConverter;
	private ByteStreamHandlerRegistry _byteStreamHandlerRegistry;
	private ClassByteStreamHandlerRegistry _classbyteStreamHandlerRegistry;

	private IDomainNotifier _domainNotifier;

	private ArrayList _loaders;
	private ArrayList _minersLocations;
	private ArrayList _localClassLoaders;
	private HashMap _dataStorePreferences;

	private List _dataStorePreferenceListeners;


	private ISSLProperties _sslProperties;

	private boolean _autoRefresh;

	private boolean _isConnected;
	private boolean _logTimes;
	private int _timeout;

	private int _serverIdleShutdownTimeout = 0;

	private HashMap _hashMap;
	private HashMap _objDescriptorMap;
	private HashMap _cmdDescriptorMap;
	private HashMap _relDescriptorMap;

	private ArrayList _recycled;

	private Random _random;
	private int _initialSize;
	private int _MAX_FREE = 10000;

	private File _traceFileHandle;
	private RandomAccessFile _traceFile;
	private boolean _tracingOn;

	private boolean _queriedSpiritState = false; // for the client - so we don't keep sending down the same query

	private boolean _spiritModeOn = false;
	private boolean _spiritCommandReceived = false;
	private File _memLoggingFileHandle;
	private RandomAccessFile _memLogFile;
	private boolean _memLoggingOn;

	private ArrayList _waitingStatuses = null;

	private String _userPreferencesDirectory = null;
	private IDataStoreCompatibilityHandler _compatibilityHandler;


	private HashMap _classReqRepository;
	private File _cacheJar;
	public static final String REMOTE_CLASS_CACHE_JARFILE_NAME = "rmt_classloader_cache"; //$NON-NLS-1$
	public static final String JARFILE_EXTENSION = ".jar";	 //$NON-NLS-1$
	private DataElementRemover _deRemover;
	public static final int SPIRIT_ON_INITIAL_SIZE = 1000;
	private String referenceTag = null;

	private int _serverVersion;
	private int _serverMinor;

	private List _lastCreatedElements;
	private Client _client;

	/**
	 * Indicates the RSE plugin version that corresponds with this DataStore.  This is only used
	 * for tracing so that users can determine which version of the org.eclipse.dstore.core plugin
	 * this came from. 
	 * 
	 *  This needs to be updated for each major release.
	 */
	private String _RSE_version = "3.4.2"; //$NON-NLS-1$
	
	/**
	 * Creates a new <code>DataStore</code> instance
	 *
	 * @param attributes the default attributes of the <code>DataStore</code>
	 */
	public DataStore(DataStoreAttributes attributes)
	{
		_dataStoreAttributes = attributes;
		_commandHandler = null;
		_updateHandler = null;
		_domainNotifier = null;
		_isConnected = false;
		_logTimes = false;
		setSpiritModeOnState();
		_initialSize = _spiritModeOn && !isVirtual() ? SPIRIT_ON_INITIAL_SIZE : 10000;
		initialize();
	}

	/**
	 * Creates a new DataStore instance
	 *
	 * @param attributes the default attributes of the <code>DataStore</code>
	 * @param initialSize the initial number of preallocated <code>DataElement</code>s
	 */
	public DataStore(DataStoreAttributes attributes, int initialSize)
	{
		_dataStoreAttributes = attributes;
		_commandHandler = null;
		_updateHandler = null;
		_domainNotifier = null;
		_isConnected = false;
		_logTimes = false;
		setSpiritModeOnState();
		_initialSize = _spiritModeOn && !isVirtual() ? SPIRIT_ON_INITIAL_SIZE : initialSize;
		initialize();
	}

	/**
	 * Creates a new <code>DataStore</code> instance
	 *
	 * @param attributes the default attributes of the <code>DataStore</code>
	 * @param commandHandler the DataStore's handler for sending commands
	 * @param updateHandler the DataStore's handler for doing updates
	 * @param domainNotifier the domain notifier
	 * @since 3.0 replaced DomainNotifier with IDomainNotifier
	 */
	public DataStore(DataStoreAttributes attributes, CommandHandler commandHandler, UpdateHandler updateHandler, IDomainNotifier domainNotifier)
	{
		_dataStoreAttributes = attributes;
		_commandHandler = commandHandler;
		_updateHandler = updateHandler;
		_domainNotifier = domainNotifier;
		_isConnected = true;
		_logTimes = false;
		setSpiritModeOnState();
		_initialSize = _spiritModeOn && !isVirtual() ? SPIRIT_ON_INITIAL_SIZE : 10000;
		initialize();
		createRoot();
	}

	/**
	 * Creates a new DataStore instance
	 *
	 * @param attributes the default attributes of the <code>DataStore</code>
	 * @param commandHandler the DataStore's handler for sending commands
	 * @param updateHandler the DataStore's handler for doing updates
	 * @param domainNotifier the domain notifier
	 * @param initialSize the initialNumber of preallocated
	 *            <code>DataElement</code>s
	 * @since 3.0 replaced DomainNotifier with IDomainNotifier
	 */
	public DataStore(DataStoreAttributes attributes, CommandHandler commandHandler, UpdateHandler updateHandler, IDomainNotifier domainNotifier, int initialSize)
	{
		_dataStoreAttributes = attributes;
		_commandHandler = commandHandler;
		_updateHandler = updateHandler;
		_domainNotifier = domainNotifier;
		_isConnected = true;
		_logTimes = false;
		setSpiritModeOnState();
		_initialSize = _spiritModeOn && !isVirtual() ? SPIRIT_ON_INITIAL_SIZE : initialSize;
		initialize();
		createRoot();
	}

	protected void setSpiritModeOnState()
	{
		if (isVirtual()) _spiritModeOn = true;
		else
		{
			String doSpirit = System.getProperty(IDataStoreSystemProperties.DSTORE_SPIRIT_ON); 
			_spiritModeOn = (doSpirit != null && doSpirit.equals("true")); //$NON-NLS-1$
		}
	}

	public void setServerVersion(int version)
	{
		_serverVersion = version;
	}

	public void setServerMinor(int minor)
	{
		_serverMinor = minor;
	}

	public int getServerVersion()
	{
		return _serverVersion;
	}

	public int getServerMinor()
	{
		return _serverMinor;
	}

	/**
	 * Sets the ticket for this <code>DataStore</code>.  A ticket is used to prevent unauthorized users
	 * from accessing the <code>DataStore</code>
	 *
	 * @param ticket the <code>DataElement</code> representing the ticket
	 */
	public void setTicket(DataElement ticket)
	{
		_ticket = ticket;
	}

	/**
	 * Sets the loaders for this <code>DataStore</code>.  The loaders are used to load miners (extension tools).
	 *
	 * @param loaders the loaders for the miners this <code>DataStore</code> will be using
	 */
	public void setLoaders(ArrayList loaders)
	{
		_loaders = loaders;
	}

	/**
	 * Adds a loader for this <code>DataStore</code>.  The loader is used to load miners (extension tools).
	 *
	 * @param loader the loader for the miners this <code>DataStore</code> will be using
	 * @since 3.0 replaced ExternalLoader by IExternalLoader
	 */
	public void addLoader(IExternalLoader loader)
	{
		if (_loaders == null)
		{
			_loaders = new ArrayList();
		}
		_loaders.add(loader);
	}



	public boolean usingSSL()
	{
		if (_sslProperties != null)
		{
			return _sslProperties.usingSSL() && _sslProperties.usingServerSSL();
		}
		return false;
	}
	/**
	 * Specifies the security properties of this DataStore. These properties
	 * indicate whether or not to use SSL, the keystore location and password.
	 * 
	 * @param properties the properties to set
	 */
	public void setSSLProperties(ISSLProperties properties)
	{
		_sslProperties = properties;
	}

	/*
	 * Returns the location for the keystore associated with the DataStore.
	 * The keystore is used when using SSL for remote communications.  On the
	 * host the file typically resides in the server directory.  On the client,
	 * the keystore location is normally customized vi <code>setKeyStoreLocation</code>.
	 */
	public String getKeyStoreLocation()
	{
		if (_sslProperties != null)
		{
			return _sslProperties.getServerKeyStorePath();
		}
		return null;
	}

	/*
	 *  Returns the password to use when accessing the DataStore keystore.
	 */
	public String getKeyStorePassword()
	{
		if (_sslProperties != null)
		{
			return _sslProperties.getServerKeyStorePassword();
		}
		return null;
	}

	/**
	 * Tells the <code>DataStore</code> where to find the miners which it needs to load.
	 *
	 * @param minersLocation a string representing the location of the miners
	 */
	public DataElement addMinersLocation(String minersLocation)
	{
		if (_minersLocations == null)
		{
			_minersLocations = new ArrayList();
		}
		if (!_minersLocations.contains(minersLocation))
		{
			_minersLocations.add(minersLocation);

			if (isVirtual())
			{
				DataElement location = createObject(_tempRoot, "location", minersLocation); //$NON-NLS-1$
				DataElement cmd = findCommandDescriptor(DataStoreSchema.C_ADD_MINERS);//localDescriptorQuery(_root.getDescriptor(), DataStoreSchema.C_ADD_MINERS, 1);
				ArrayList args = new ArrayList();
				args.add(location);
				return command(cmd, args, _dummy);
			}
		}

		return null;
	}

	/**
	 * Tells the <code>DataStore</code> where to find the miners which it needs to load.
	 *
	 * @param location a <code>DataElement</code> representing the location of the miners
	 */
	public void addMinersLocation(DataElement location)
	{
		String name = location.getName();
		if (_minersLocations == null)
		{
			_minersLocations = new ArrayList();
		}

		if (!_minersLocations.contains(name))
		{
			_minersLocations.add(name);
		}
	}

	/**
	 * Tells the <code>DataStore</code> that it is connected to it's tools
	 *
	 * @param isConnected indicates whether it is connected or not
	 */
	public void setConnected(boolean isConnected)
	{
		_isConnected = isConnected;
	}

	/**
	 * Sets the <code>DataStore</code>'s IDomainNotifier
	 *
	 * @param domainNotifier the domainNotifier
	 * @since 3.0 replaced DomainNotifier by IDomainNotifier
	 */
	public void setDomainNotifier(IDomainNotifier domainNotifier)
	{
		_domainNotifier = domainNotifier;
	}

	/**
	 * Sets the <code>DataStore</code>'s handler for doing updates
	 *
	 * @param updateHandler the handler for doing updates
	 */
	public void setUpdateHandler(UpdateHandler updateHandler)
	{
		_updateHandler = updateHandler;
	}

	/**
	 * Sets the <code>DataStore</code>'s handler for sending commands to miners
	 *
	 * @param commandHandler the handler for sending commands to miners
	 */
	public void setCommandHandler(CommandHandler commandHandler)
	{
		_commandHandler = commandHandler;
	}

	/**
	 * Set the compatibility handler for the client. This is used when potential
	 * compatibility problems are run into - i.e. localDescriptorQuery fails
	 *
	 * @param handler the compatibilityHandler to use
	 * @since 3.0
	 */
	public void setCompatibilityHandler(IDataStoreCompatibilityHandler handler){
		_compatibilityHandler = handler;
	}

	/**
	 * Get the compatibility handler for the client. This is used when potential
	 * compatibility problems are run into - i.e. localDescriptorQuery fails
	 *
	 * @return the compatibilityHandler
	 * @since 3.0
	 */
	public IDataStoreCompatibilityHandler getCompatibilityHandler(){
		if (_compatibilityHandler == null){
			_compatibilityHandler = new DefaultDataStoreCompatibilityHandler(this);
		}
		return _compatibilityHandler;
	}


	/**
	 * Sets the time the update handler sleeps in between update requests
	 *
	 * @param time interval to wait
	 */
	public void setUpdateWaitTime(int time)
	{
		_updateHandler.setWaitTime(time);
	}

	/**
	 * Sets the time the command handler sleeps in between command requests
	 *
	 * @param time interval to wait
	 */
	public void setCommandWaitTime(int time)
	{
		_commandHandler.setWaitTime(time);
	}

	/**
	 * Sets the maximum amount of time that the <code>DataStore</code> will wait to receive a response
	 * for a synchronous command
	 *
	 * @param time interval to wait
	 */
	public void setTimeoutValue(int time)
	{
		_timeout = time;
	}

	public int getTimeoutValue()
	{
		return _timeout;
	}

	/**
	 * Gets the time the server may remain idle before shutting down
	 *
	 * @return the idle time before shutdown
	 * @since 3.0
	 */
	public int getServerIdleShutdownTimeout()
	{
		return _serverIdleShutdownTimeout;
	}

	/**
	 * Sets an attribute of the <code>DataStore</code>
	 *
	 * @param attribute index of the attribute to set
	 * @param value value to set the attribute at the give index
	 */
	public void setAttribute(int attribute, String value)
	{
		_dataStoreAttributes.setAttribute(attribute, value);
	}

	/**
	 * Tells the <code>DataStore</code> to log durations of commands
	 *
	 * @param flag whether to log times or not
	 */
	public void setLogTimes(boolean flag)
	{
		_logTimes = flag;
	}

	/**
	 * Indicates whether this <code>DataStore</code> is virtual or not.  A virtual <code>DataStore</code>
	 * is one that does not have it's own tools, but rather communicates with a non-virtual
	 * <code>DataStore</code> that does.
	 *
	 * @return whether the <code>DataStore</code> is virtual or not
	 */
	public boolean isVirtual()
	{
		if (_commandHandler == null)
		{
			return true;
		}
		else if (_commandHandler instanceof org.eclipse.dstore.internal.core.client.ClientCommandHandler)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * Indicates whether this <code>DataStore</code> is connected to it's miners or another <code>DataStore</code>
	 *
	 * @return whether the <code>DataStore</code> is connected or not
	 */
	public boolean isConnected()
	{
		return _isConnected;
	}

	/**
	 * Indicates whether this <code>DataStore</code> logs the durations of commands
	 *
	 * @return whether the <code>DataStore</code> logs command times or not
	 */
	public boolean logTimes()
	{
		return _logTimes;
	}

	/**
	 * Returns the <code>DataStore</code>'s ticket
	 *
	 * @return the ticket
	 */
	public DataElement getTicket()
	{
		return _ticket;
	}

	/**
	 * Returns the time the update handler waits between requests
	 *
	 * @return wait time
	 */
	public int getUpdateWaitTime()
	{
		return _updateHandler.getWaitTime();
	}

	/**
	 * Returns the time the command handler waits between requests
	 *
	 * @return wait time
	 */
	public int getCommandWaitTime()
	{
		return _commandHandler.getWaitTime();
	}





	/**
	 * Returns the name of the <code>DataStore</code>
	 *
	 * @return the name of the <code>DataStore</code>
	 */
	public String getName()
	{
		return getAttribute(DataStoreAttributes.A_HOST_NAME);
	}

	/**
	 * Returns the root <code>DataElement</code> in the <code>DataStore</code>.
	 * The root <code>DataElement</code> has no parent and contains every <code>DataElement</code>
	 * in the <code>DataStore</code> through a <code>DataElement</code> tree
	 *
	 * @return the root <code>DataElement</code>
	 */
	public DataElement getRoot()
	{
		return _root;
	}

	public DataElement getDummy()
	{
	    return _dummy;
	}

	/**
	 * Returns the host root <code>DataElement</code> in the <code>DataStore</code>.
	 * The host root <code>DataElement</code> is a child of root and references
	 * <code>DataElement</code>s in the <code>DataStore</code> that are related to host information
	 *
	 * @return the host root <code>DataElement</code>
	 */
	public DataElement getHostRoot()
	{
		return _hostRoot;
	}

	public DataElement getExternalRoot()
	{
		return _externalRoot;
	}

	/**
	 * Returns the miner root <code>DataElement</code> in the <code>DataStore</code>.
	 * The miner root <code>DataElement</code> is a child of root and contains
	 * <code>DataElement</code>s the represent tools and the information that tools possess
	 *
	 * @return the miner root <code>DataElement</code>
	 */
	public DataElement getMinerRoot()
	{
		return _minerRoot;
	}

	/**
	 * Returns the status of the <code>DataStore</code>.
	 *
	 * @return the status of the <code>DataStore</code>
	 */
	public DataElement getStatus()
	{
		return _status;
	}

	/**
	 * Returns the log root <code>DataElement</code> of the <code>DataStore</code>.
	 * The log root contains all commands that are issued from the <code>DataStore</code>
	 *
	 * @return the log root
	 */
	public DataElement getLogRoot()
	{
		return _logRoot;
	}

	/**
	 * Returns the descriptor root <code>DataElement</code> of the <code>DataStore</code>.
	 * The descriptor root contains the schema for the <code>DataStore</code> and it's tools
	 *
	 * @return the descriptor root
	 */
	public DataElement getDescriptorRoot()
	{
		return _descriptorRoot;
	}

	/**
	 * Returns the temp root <code>DataElement</code> of the <code>DataStore</code>.
	 * The temp root contains temporary information.
	 *
	 * @return the temp root
	 */
	public DataElement getTempRoot()
	{
		return _tempRoot;
	}

	/**
	 * Returns the handler for sending commands.
	 *
	 * @return the command handler
	 */
	public CommandHandler getCommandHandler()
	{
		return _commandHandler;
	}

	/**
	 * Returns the handler for doing updates.
	 *
	 * @return the update handler
	 */
	public UpdateHandler getUpdateHandler()
	{
		return _updateHandler;
	}

	/**
	 * Returns the loader that is used for loading miners.
	 *
	 * @return the loader
	 */
	public ArrayList getLoaders()
	{
		return _loaders;
	}

	/**
	 * Returns registered local classloaders.
	 */
	public ArrayList getLocalClassLoaders()
	{
		return _localClassLoaders;
	}

	/**
	 * Registers a local class loader. On the client, each subsystem
	 * must register its local class loader using this method so that
	 * if the subsystem's classes cannot be found on the server, they can
	 * be requested from the client, loaded using <code>loader</code>,
	 * transferred to the server, and then loaded on the server.
	 */
	public void registerLocalClassLoader(ClassLoader loader)
	{
		if (_localClassLoaders == null)
		{
			_localClassLoaders = new ArrayList();
		}
		if (!_localClassLoaders.contains(loader)) _localClassLoaders.add(loader);
	}

	public DataElement getContentsRelation()
	{
		return _dataStoreSchema.getContentsRelation();
	}

	public DataElement getAttributesRelation()
	{
		return _dataStoreSchema.getAttributesRelation();
	}

	public DataElement getAbstractedByRelation()
	{
		return _dataStoreSchema.getAbstractedByRelation();
	}

	public DataElement getAbstractsRelation()
	{
		return _dataStoreSchema.getAbstractsRelation();
	}

	/**
	 * Returns the location of the miners.
	 *
	 * @return the location of the miners
	 */
	public ArrayList getMinersLocation()
	{
		return _minersLocations;
	}

	/**
	 * Returns the domain notifier.
	 *
	 * @return the domain notifier
	 * @since 3.0 replaced DomainNotifier by IDomainNotifier
	 */
	public IDomainNotifier getDomainNotifier()
	{
		return _domainNotifier;
	}

	/**
	 * Returns the attribute indicated by an index.
	 *
	 * @param attribute the index of the attribute to get
	 * @return the attribute
	 */
	public String getAttribute(int attribute)
	{
		return _dataStoreAttributes.getAttribute(attribute);
	}

	/**
	 * Returns the number of live elements in the <code>DataStore</code>.
	 *
	 * @return the number of live elements
	 */
	public int getNumElements()
	{
		synchronized (_hashMap){	
			return _hashMap.size();
		}
	}

	/**
	* Returns the number of recycled elements in the <code>DataStore</code>.
	*
	* @return the number of recycled elements
	*/
	public int getNumRecycled()
	{
		return _recycled.size();
	}
	/**
	 * Returns the table of live elements in the <code>DataStore</code>.
	 *
	 * @return the table of live elements
	 */
	public HashMap getHashMap()
	{
		return _hashMap;
	}

	/**
	 * Initializes the <code>DataStore</code> by creating the root elements
	 *
	 */
	public void createRoot()
	{
		_root =
			createObject(
				null,
				DataStoreResources.model_root,
				_dataStoreAttributes.getAttribute(DataStoreAttributes.A_ROOT_NAME),
				_dataStoreAttributes.getAttribute(DataStoreAttributes.A_ROOT_PATH),
				"rootID"); //$NON-NLS-1$


		_descriptorRoot = createObject(_root, DE.T_OBJECT_DESCRIPTOR, DataStoreResources.model_descriptors, "", "schemaID"); //$NON-NLS-1$ //$NON-NLS-2$

		_ticket = createObject(_root, DataStoreResources.model_ticket, "null", "", "ticketID"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		createRoots();
		initializeDescriptors();
	}

	/**
	 * Creates a contents relationship between two <code>DataElement</code>s
	 *
	 * @param from the element that contains the other
	 * @param to the element that is contained by the other
	 * @return the new reference
	 */
	public DataElement createReference(DataElement from, DataElement to)
	{
		// default reference is a containment relationship
		return createReference(from, to, getContentsRelation());
	}

	/**
	 * Creates a relationship between two <code>DataElement</code>s given a type of relationship
	 *
	 * @param parent the element that references the other element
	 * @param realObject the element that is referenced by the parent element
	 * @param relationType the descriptor element that represents the type of relationship between parent and realObject
	 * @return the new reference
	 */
	public DataElement createReference(DataElement parent, DataElement realObject, DataElement relationType)
	{
		if (parent != null)
		{

			// reference with a specified type of relationship
			DataElement reference = createElement();

			reference.reInit(parent, realObject, relationType);
			parent.addNestedData(reference, false);

			refresh(parent);

			return reference;
		}
		else
		{
			return null;
		}
	}

	/**
	 * Creates a relationship between two <code>DataElement</code>s given a type of relationship
	 *
	 * @param parent the element that references the other element
	 * @param realObject the element that is referenced by the parent element
	 * @param relationType the string that represents the type of relationship between parent and realObject
	 * @return the new reference
	 */
	public DataElement createReference(DataElement parent, DataElement realObject, String relationType)
	{
	    return createReference(parent, realObject, relationType, true);
	}

	/**
	 * Creates a relationship between two <code>DataElement</code>s given a type of relationship
	 *
	 * @param parent the element that references the other element
	 * @param realObject the element that is referenced by the parent element
	 * @param relationType the string that represents the type of relationship between parent and realObject
	 * @param doRefresh indicates whether or not to refresh the parent of the new reference
	 * @return the new reference
	 */
	public DataElement createReference(DataElement parent, DataElement realObject, String relationType, boolean doRefresh)
	{
		if (parent != null)
		{
			// reference with a specified type of relationship
			DataElement reference = createElement();

			DataElement toDescriptor = findDescriptor(DE.T_RELATION_DESCRIPTOR, relationType);
			if (toDescriptor != null)
			{
				reference.reInit(parent, realObject, toDescriptor);
			}
			else
			{
				reference.reInit(parent, realObject, relationType);
			}

			parent.addNestedData(reference, false);

			if (doRefresh)
			{
			    refresh(parent);
			}

			return reference;
		}
		return null;
	}

	/**
	 * Creates a set of  relationships between one <code>DataElement</code> and a set of <code>DataElement</code>s given a type of relationship
	 *
	 * @param from the element that references the other elements
	 * @param to a list of elements that from references
	 * @param type the string that represents the type of relationships between from and to
	 */
	public void createReferences(DataElement from, ArrayList to, String type)
	{
		DataElement toDescriptor = findDescriptor(DE.T_RELATION_DESCRIPTOR, type);
		if (toDescriptor != null)
		{
			createReferences(from, to, toDescriptor);
		}
		else
		{
			for (int i = 0; i < to.size(); i++)
			{
				DataElement toObject = (DataElement) to.get(i);
				createReference(from, toObject, type);
			}
		}
	}

	/**
	 * Creates a set of  relationships between one <code>DataElement</code> and a set of <code>DataElement</code>s given a type of relationship
	 *
	 * @param from the element that references the other elements
	 * @param to a list of elements that from references
	 * @param type the descriptor element that represents the type of relationships between from and to
	 */
	public void createReferences(DataElement from, ArrayList to, DataElement type)
	{
		for (int i = 0; i < to.size(); i++)
		{
			DataElement toObject = (DataElement) to.get(i);
			createReference(from, toObject, type);
		}
	}

	/**
	 * Creates a two-way relationship between two elements
	 *
	 * @param parent an element that references the other element
	 * @param realObject an element that references the other element
	 * @param toRelation the descriptor element that represents the type of relationship between parent and realObject
	 * @param fromRelation the descriptor element that represents the type of relationship between realObject and parent
	 * @return the new reference
	 */
	public DataElement createReference(DataElement parent, DataElement realObject, DataElement toRelation, DataElement fromRelation)
	{
		if (parent != null)
		{
			// reference with "to" relationship
			DataElement toReference = createElement();
			toReference.reInit(parent, realObject, toRelation);

			parent.addNestedData(toReference, false);
			// reference with "from" relationship
			DataElement fromReference = createElement();

			fromReference.reInit(realObject, parent, fromRelation);

			realObject.addNestedData(fromReference, false);
			refresh(parent);


			return toReference;
		}
		return null;
	}

	/**
	 * Creates a two-way relationship between two elements
	 *
	 * @param parent an element that references the other element
	 * @param realObject an element that references the other element
	 * @param toRelation the string that represents the type of relationship between parent and realObject
	 * @param fromRelation the string that represents the type of relationship between realObject and parent
	 * @return the new reference
	 */
	public DataElement createReference(DataElement parent, DataElement realObject, String toRelation, String fromRelation)
	{
		if (parent != null)
		{
			// reference with "to" relationship
			DataElement toReference = createElement();
			DataElement toDescriptor = findDescriptor(DE.T_RELATION_DESCRIPTOR, toRelation);
			if (toDescriptor != null)
			{
				toReference.reInit(parent, realObject, toDescriptor);
			}
			else
			{
				toReference.reInit(parent, realObject, toRelation);
			}

			parent.addNestedData(toReference, false);
			// reference with "from" relationship
			DataElement fromReference = createElement();

			DataElement fromDescriptor = findDescriptor(DE.T_RELATION_DESCRIPTOR, fromRelation);
			if (fromDescriptor != null)
			{
				fromReference.reInit(realObject, parent, fromDescriptor);
			}
			else
			{
				fromReference.reInit(realObject, parent, fromRelation);
			}

			realObject.addNestedData(fromReference, false);

			 refresh(parent);


			return toReference;
		}
		return null;
	}

	/**
	 * Creates a set of two-way relationship between a <code>DataElement</code> and a list of elements
	 *
	 * @param from an element that references the other elements
	 * @param to a list of elements that reference from
	 * @param toRel the descriptor element that represents the type of relationship between from and to
	 * @param fromRel the descriptor element that represents the type of relationship between to and from
	 */
	public void createReferences(DataElement from, ArrayList to, DataElement toRel, DataElement fromRel)
	{
		for (int i = 0; i < to.size(); i++)
		{
			DataElement toObject = (DataElement) to.get(i);
			createReference(from, toObject, toRel, fromRel);
		}
	}

	/**
	 * Creates a set of two-way relationship between a DataElement and a list of elements
	 *
	 * @param from an element that references the other elements
	 * @param to a list of elements that reference from
	 * @param toRel the string that represents the type of relationship between from and to
	 * @param fromRel the string that represents the type of relationship between to and from
	 */
	public void createReferences(DataElement from, ArrayList to, String toRel, String fromRel)
	{
		DataElement toDescriptor = findDescriptor(DE.T_RELATION_DESCRIPTOR, toRel);
		DataElement fromDescriptor = findDescriptor(DE.T_RELATION_DESCRIPTOR, fromRel);

		if ((toDescriptor != null) && (fromDescriptor != null))
		{
			createReferences(from, to, toDescriptor, fromDescriptor);
		}
		else
		{
			for (int i = 0; i < to.size(); i++)
			{
				DataElement toObject = (DataElement) to.get(i);
				createReference(from, toObject, toRel, fromRel);
			}
		}
	}

	public DataElement createTransientObject(String attributes[])
	{
		DataElement newObject = new DataElement(this);

		newObject.reInitAsTransient(attributes);
		return newObject;
	}

	/**
	 * Creates a new <code>DataElement</code>
	 *
	 * @param parent the parent of the new element
	 * @param type the descriptor representing the type of the new element
	 * @param name the name of the new element
	 * @return the new element
	 */
	public DataElement createObject(DataElement parent, DataElement type, String name)
	{
		return createObject(parent, type, name, ""); //$NON-NLS-1$
	}

	/**
	 * Creates a new <code>DataElement</code>
	 *
	 * @param parent the parent of the new element
	 * @param type the string representing the type of the new element
	 * @param name the name of the new element
	 * @return the new element
	 */
	public DataElement createObject(DataElement parent, String type, String name)
	{
		return createObject(parent, type, name, ""); //$NON-NLS-1$
	}

	/**
	 * Creates a new <code>DataElement</code>
	 *
	 * @param parent the parent of the new element
	 * @param type the descriptor element representing the type of the new element
	 * @param name the name of the new element
	 * @param source the source location of the new element
	 * @return the new element
	 */
	public DataElement createObject(DataElement parent, DataElement type, String name, String source)
	{
		String id = generateId();
		return createObject(parent, type, name, source, id);
	}

	/**
	 * Creates a new <code>DataElement</code>
	 *
	 * @param parent the parent of the new element
	 * @param type the string representing the type of the new element
	 * @param name the name of the new element
	 * @param source the source location of the new element
	 * @return the new element
	 */
	public DataElement createObject(DataElement parent, String type, String name, String source)
	{
		String id = generateId(parent, type, name);
		if (id == null)
		{
			return null;
		}

		return createObject(parent, type, name, source, id);
	}

	/**
	 * Creates a new <code>DataElement</code>
	 *
	 * @param parent the parent of the new element
	 * @param type the descriptor element representing the type of the new element
	 * @param name the name of the new element
	 * @param source the source location of the new element
	 * @param sugId the suggested ID for the new element
	 * @return the new element
	 */
	public DataElement createObject(DataElement parent, DataElement type, String name, String source, String sugId)
	{
		return createObject(parent, type, name, source, sugId, false);
	}

	/**
	 * Creates a new <code>DataElement</code>
	 *
	 * @param parent the parent of the new element
	 * @param type the string representing the type of the new element
	 * @param name the name of the new element
	 * @param source the source location of the new element
	 * @param sugId the suggested ID for the new element
	 * @return the new element
	 */
	public DataElement createObject(DataElement parent, String type, String name, String source, String sugId)
	{
		return createObject(parent, type, name, source, sugId, false);
	}

	/**
	 * Creates a new <code>DataElement</code>
	 *
	 * @param parent the parent of the new element
	 * @param type the descriptor element representing the type of the new element
	 * @param name the name of the new element
	 * @param source the source location of the new element
	 * @param sugId the suggested ID for the new element
	 * @param isReference an indication whether the new element is a reference
	 * @return the new element
	 */
	public DataElement createObject(DataElement parent, DataElement type, String name, String source, String sugId, boolean isReference)
	{
		String id = makeIdUnique(sugId);

		DataElement newObject = createElement();
		if (parent == null)
		{
			parent = _tempRoot;
		}

		newObject.reInit(parent, type, id, name, source, isReference);

		if (parent != null)
		{
			parent.addNestedData(newObject, false);
		}

		synchronized (_hashMap)
		{
			_hashMap.put(id, newObject);
		}

		if (_autoRefresh)
			refresh(parent);
		return newObject;
	}

	/**
	 * Creates a new <code>DataElement</code>
	 *
	 * @param parent the parent of the new element
	 * @param type the string representing the type of the new element
	 * @param name the name of the new element
	 * @param source the source location of the new element
	 * @param sugId the suggested ID for the new element
	 * @param isReference an indication whether the new element is a reference
	 * @return the new element
	 */
	public DataElement createObject(DataElement parent, String type, String name, String source, String sugId, boolean isReference)
	{
		String id = makeIdUnique(sugId);

		DataElement newObject = createElement();
		if (parent == null)
		{
			parent = _tempRoot;
		}


		DataElement descriptor = findDescriptor(DE.T_OBJECT_DESCRIPTOR, type);
		if (descriptor != null && (parent != _descriptorRoot))
		{
			newObject.reInit(parent, descriptor, id, name, source, isReference);
		}
		else
		{
			newObject.reInit(parent, type, id, name, source, isReference);
		}

		if (parent != null)
		{
			parent.addNestedData(newObject, false);
		}

		synchronized(_hashMap)
		{
			_hashMap.put(id, newObject);
		}

		if (_autoRefresh)
			refresh(parent);
		return newObject;
	}

	/**
	 * Creates a new <code>DataElement</code>.  This is normally called on client side via xml parser
	 *
	 * @param parent the parent of the new element
	 * @param attributes the attributes to use in this new element
	 * @return the new element
	 */
	public DataElement createObject(DataElement parent, String attributes[])
	{
		DataElement newObject = createElement();

		if (parent == null)
		{
			parent = _tempRoot;
		}

		DataElement descriptor = findObjectDescriptor(attributes[DE.A_TYPE]);

		if (descriptor != null && (parent != _descriptorRoot))
		{
			newObject.reInit(parent, descriptor, attributes);
		}
		else
		{
			newObject.reInit(parent, attributes);
		}

		if (parent != null)
		{
			parent.addNestedData(newObject, false);
		}

		// cache descriptors in map for faster access
		if (descriptor == _dataStoreSchema.getObjectDescriptor() || descriptor == _dataStoreSchema.getAbstractObjectDescriptor())
		{
		    _objDescriptorMap.put(attributes[DE.A_NAME], newObject);
		}
		else if (descriptor == _dataStoreSchema.getCommandDescriptor() || descriptor == _dataStoreSchema.getAbstractCommandDescriptor())
		{
		    _cmdDescriptorMap.put(attributes[DE.A_NAME], newObject);
		}
		else if (descriptor == _dataStoreSchema.getRelationDescriptor() || descriptor == _dataStoreSchema.getAbstractRelationDescriptor())
		{
		    _relDescriptorMap.put(attributes[DE.A_NAME], newObject);
		}

		synchronized (_hashMap)
		{
			_hashMap.put(attributes[DE.A_ID], newObject);
		}
		return newObject;
	}

	/**
	 * Creates a new abstract object descriptor <code>DataElement</code>
	 *
	 * @param parent the parent of the new element
	 * @param name the name of the new element
	 * @return the new descriptor element
	 */
	public DataElement createAbstractObjectDescriptor(DataElement parent, String name)
	{
		DataElement descriptor = createObject(parent, DE.T_ABSTRACT_OBJECT_DESCRIPTOR, name, "org.eclipse.rse.dstore.core", name); //$NON-NLS-1$
		_objDescriptorMap.put(name, descriptor);
		return descriptor;
	}

	/**
	 * Creates a new abstract object descriptor <code>DataElement</code>
	 *
	 * @param parent the parent of the new element
	 * @param name the name of the new element
	 * @param source the source location of the new element
	 * @return the new descriptor element
	 */
	public DataElement createAbstractObjectDescriptor(DataElement parent, String name, String source)
	{
		DataElement descriptor = createObject(parent, DE.T_ABSTRACT_OBJECT_DESCRIPTOR, name, source, name);
		_objDescriptorMap.put(name, descriptor);
		return descriptor;
	}

	/**
	 * Creates a new object descriptor <code>DataElement</code>
	 *
	 * @param parent the parent of the new element
	 * @param name the name of the new element
	 * @return the new descriptor element
	 */
	public DataElement createObjectDescriptor(DataElement parent, String name)
	{
	    DataElement parentDescriptor = _dataStoreSchema.getObjectDescriptor();
	    DataElement descriptor = null;
	    if (parentDescriptor != null)
	    {
	    	descriptor = createObject(parent, parentDescriptor, name, "org.eclipse.rse.dstore.core", name); //$NON-NLS-1$
	    }
	    else
	    {
			descriptor = createObject(parent, DE.T_OBJECT_DESCRIPTOR, name, "org.eclipse.rse.dstore.core", name); //$NON-NLS-1$
	    }
		_objDescriptorMap.put(name, descriptor);
		return descriptor;
	}

	/**
	 * Creates a new object descriptor <code>DataElement</code>
	 *
	 * @param parent the parent of the new element
	 * @param name the name of the new element
	 * @param source the name of the new element
	 * @return the new descriptor element
	 */
	public DataElement createObjectDescriptor(DataElement parent, String name, String source)
	{
	    DataElement parentDescriptor = _dataStoreSchema.getObjectDescriptor();
	    DataElement descriptor = null;
	    if (parentDescriptor != null)
	    {
	    	descriptor = createObject(parent, parentDescriptor, name, source, name);
	    }
	    else
	    {
	        descriptor = createObject(parent, DE.T_OBJECT_DESCRIPTOR, name, source, name);
	    }
		_objDescriptorMap.put(name, descriptor);
		return descriptor;
	}

	/**
	 * Creates a new abstract relation descriptor <code>DataElement</code>
	 *
	 * @param parent the parent of the new element
	 * @param name the name of the new element
	 * @return the new descriptor element
	 */
	public DataElement createAbstractRelationDescriptor(DataElement parent, String name)
	{
		DataElement descriptor = createObject(parent, DE.T_ABSTRACT_RELATION_DESCRIPTOR, name, "org.eclipse.rse.dstore.core", name); //$NON-NLS-1$
		_relDescriptorMap.put(name, descriptor);
		return descriptor;
	}

	/**
	 * Creates a new abstract relation descriptor <code>DataElement</code>
	 *
	 * @param parent the parent of the new element
	 * @param name the name of the new element
	 * @param source the source location of the new element
	 * @return the new descriptor element
	 */
	public DataElement createAbstractRelationDescriptor(DataElement parent, String name, String source)
	{
		DataElement descriptor = createObject(parent, DE.T_ABSTRACT_RELATION_DESCRIPTOR, name, source, name);
		_relDescriptorMap.put(name, descriptor);
		return descriptor;
	}

	/**
	 * Creates a new relation descriptor <code>DataElement</code>
	 *
	 * @param parent the parent of the new element
	 * @param name the name of the new element
	 * @return the new descriptor element
	 */
	public DataElement createRelationDescriptor(DataElement parent, String name)
	{
		DataElement descriptor = createObject(parent, DE.T_RELATION_DESCRIPTOR, name, "org.eclipse.rse.dstore.core", name); //$NON-NLS-1$
		_relDescriptorMap.put(name, descriptor);
		return descriptor;
	}

	/**
	 * Creates a new relation descriptor <code>DataElement</code>
	 *
	 * @param parent the parent of the new element
	 * @param name the name of the new element
	 * @param source the source location of the new element
	 * @return the new descriptor element
	 */
	public DataElement createRelationDescriptor(DataElement parent, String name, String source)
	{
		DataElement descriptor =  createObject(parent, DE.T_RELATION_DESCRIPTOR, name, source, name);
		_relDescriptorMap.put(name, descriptor);
		return descriptor;
	}

	/**
	 * Creates a new abstract command descriptor <code>DataElement</code>
	 *
	 * @param parent the parent of the new element
	 * @param name the name of the new element
	 * @return the new descriptor element
	 */
	public DataElement createAbstractCommandDescriptor(DataElement parent, String name)
	{
		DataElement descriptor = createAbstractCommandDescriptor(parent, name, name);
		_cmdDescriptorMap.put(name, descriptor);
		return descriptor;
	}

	/**
	 * Creates a new abstract command descriptor <code>DataElement</code>
	 *
	 * @param parent the parent of the new element
	 * @param name the name of the new element
	 * @param value the value used to identify the command
	 * @return the new descriptor element
	 */
	public DataElement createAbstractCommandDescriptor(DataElement parent, String name, String value)
	{
		DataElement cmd = createObject(parent, DE.T_ABSTRACT_COMMAND_DESCRIPTOR, name, "org.eclipse.rse.dstore.core", name); //$NON-NLS-1$
		cmd.setAttribute(DE.A_VALUE, value);
		_cmdDescriptorMap.put(value, cmd);
		return cmd;
	}

	/**
	 * Creates a new abstract command descriptor <code>DataElement</code>
	 *
	 * @param parent the parent of the new element
	 * @param name the name of the new element
	 * @param source the source location of the new element
	 * @param value the value used to identify the command
	 * @return the new descriptor element
	 */
	public DataElement createAbstractCommandDescriptor(DataElement parent, String name, String source, String value)
	{
		DataElement cmd = createObject(parent, DE.T_ABSTRACT_COMMAND_DESCRIPTOR, name, source, name);
		cmd.setAttribute(DE.A_VALUE, value);
		_cmdDescriptorMap.put(value, cmd);
		return cmd;
	}

	/**
	 * Creates a new command descriptor <code>DataElement</code>
	 *
	 * @param parent the parent of the new element
	 * @param name the name of the new element
	 * @return the new descriptor element
	 */
	public DataElement createCommandDescriptor(DataElement parent, String name)
	{
		DataElement descriptor = createCommandDescriptor(parent, name, name);
		_cmdDescriptorMap.put(name, descriptor);
		return descriptor;
	}

	/**
	 * Creates a new command descriptor <code>DataElement</code>
	 *
	 * @param parent the parent of the new element
	 * @param name the name of the new element
	 * @param value the value used to identify the command
	 * @return the new descriptor element
	 */
	public DataElement createCommandDescriptor(DataElement parent, String name, String value)
	{
	    DataElement parentDescriptor = _dataStoreSchema.getCommandDescriptor();
	    DataElement cmd = null;
	    if (parentDescriptor != null)
	    {
	    	cmd = createObject(parent, parentDescriptor, name, "org.eclipse.rse.dstore.core", name); //$NON-NLS-1$
	    }
	    else
	    {
	        cmd = createObject(parent, DE.T_COMMAND_DESCRIPTOR, name, "org.eclipse.rse.dstore.core", name); //$NON-NLS-1$
	    }
		cmd.setAttribute(DE.A_VALUE, value);
		_cmdDescriptorMap.put(value, cmd);
		return cmd;
	}

	/**
	 * Creates a new command descriptor <code>DataElement</code>
	 *
	 * @param parent the parent of the new element
	 * @param name the name of the new element
	 * @param source the source location of the new element
	 * @param value the value used to identify the command
	 * @return the new descriptor element
	 */
	public DataElement createCommandDescriptor(DataElement parent, String name, String source, String value)
	{
	    DataElement parentDescriptor = _dataStoreSchema.getCommandDescriptor();
	    DataElement cmd = null;
	    if (parentDescriptor != null)
	    {
	    	cmd = createObject(parent, parentDescriptor, name, source, name);
	    }
	    else
	    {
	        cmd = createObject(parent, DE.T_COMMAND_DESCRIPTOR, name, source, name);
	    }
		cmd.setAttribute(DE.A_VALUE, value);
		_cmdDescriptorMap.put(value, cmd);
		return cmd;
	}

	/**
	 * Creates a new command descriptor <code>DataElement</code>
	 *
	 * @param parent the parent of the new element
	 * @param name the name of the new element
	 * @param source the source location of the new element
	 * @param value the value used to identify the command
	 * @param visible indicates whether the command is visible or not
	 * @return the new descriptor element
	 */
	public DataElement createCommandDescriptor(DataElement parent, String name, String source, String value, boolean visible)
	{
	    DataElement parentDescriptor = _dataStoreSchema.getCommandDescriptor();
	    DataElement cmd = null;
	    if (parentDescriptor != null)
	    {
	    	cmd = createObject(parent, parentDescriptor, name, source, name);
	    }
	    else
	    {
			cmd = createObject(parent, DE.T_COMMAND_DESCRIPTOR, name, source, name);
	    }
		cmd.setAttribute(DE.A_VALUE, value);
		if (!visible)
		{
			cmd.setDepth(0);
		}
		_cmdDescriptorMap.put(value, cmd);

		return cmd;
	}

	/**
	 * Moves a element from one location in the <code>DataStore</code> tree to another
	 *
	 * @param source the element to move
	 * @param target the element to move source to
	 */
	public void moveObject(DataElement source, DataElement target)
	{
		DataElement oldParent = source.getParent();
		List nested = oldParent.getNestedData();
		if (nested != null)
		{
			nested.remove(source);
		}
		refresh(oldParent, true);

		target.addNestedData(source, false);
		source.setParent(target);
		refresh(target, true);
	}

	/**
	 * Deletes all the elements contained in from
	 *
	 * @param from the element from which to delete objects from
	 */
	public void deleteObjects(DataElement from)
	{
		if (from != null)
		{
			for (int i = from.getNestedSize() - 1; i >= 0; i--)
			{
				DataElement deletee = from.get(i);
				if (deletee != null)
				{
					deleteObjectHelper(from, deletee, 5);
				}
			}

			//			refresh(from);
		}
	}

	/**
	 * Disconnect all the elements contained in from
	 *
	 * @param from the element from which to disconnect objects
	 */
	public void disconnectObjects(DataElement from)
	{
		if (!isDoSpirit())
			{
			return;
			}
		if (from != null)
		{
			for (int i = from.getNestedSize() - 1; i >= 0; i--)
			{
				DataElement disconnectee = from.get(i);
				if (disconnectee != null)
				{
					disconnectObjectHelper(disconnectee, 5);
				}
			}

			//			refresh(from);
		}
	}

	/**
	 * Deletes an element from another element
	 *
	 * @param from the element from which to delete an object from
	 * @param toDelete the element to remove
	 */
	public void deleteObject(DataElement from, DataElement toDelete)
	{
		if (toDelete != null)
		{
			deleteObjectHelper(from, toDelete, 5);
			//			refresh(toDelete);
			//			refresh(from);
		}
	}

	/**
	 * Disconnects an element and makes it a "spirit"
	 *
	 * @param toDisconnect the element to disconnect
	 */
	public void disconnectObject(DataElement toDisconnect)
	{
		if (!isDoSpirit()) return;
		if (toDisconnect != null)
		{
			disconnectObjectHelper(toDisconnect, 5);
			// refresh(toDisconnect);
			//			refresh(from);
		}
	}

	/**
	 * Replaces a deleted object
	 */
	public DataElement replaceDeleted(DataElement deletedObject)
	{
		if (deletedObject != null)
		{
			synchronized (deletedObject)
			{
				String name = deletedObject.getName();
				String type = deletedObject.getType();

				// find undeleted ancestor
				DataElement parent = deletedObject.getParent();
				if ((parent != null) && parent.isDeleted())
				{
					parent = replaceDeleted(parent);
				}
				if ((parent != null) && !parent.isDeleted())
				{
					for (int i = 0; i < parent.getNestedSize(); i++)
					{
						DataElement child = parent.get(i);
						if (!child.isDeleted())
						{
							if (child.getName().equals(name) && child.getType().equals(type))
							{
								return child;
							}
						}
					}
				}
			}
		}

		return null;
	}

	/**
	 * Checks if a <code>DataElement</code> with a given ID exists in the <code>DataStore</code>
	 *
	 * @param id the id to look for
	 * @return whether it exists or not
	 */
	public boolean contains(String id)
	{
		synchronized (_hashMap){
			return _hashMap.containsKey(id);
		}
	}

	/**
	 * Refresh a set of <code>DataElement</code>s
	 *
	 * @param elements a list of elements to refresh
	 */
	public void refresh(ArrayList elements)
	{
		// this gets called in response to a query
		for (int i = 0; i < elements.size(); i++)
		{
			refresh((DataElement) elements.get(i));
		}
	}

	/**
	 * Refresh a <code>DataElement</code>
	 *
	 * @param element an element to refresh
	 */
	public void refresh(DataElement element)
	{
		if (element != null)
		{
			if (element.isReference())
			{
				refresh(element.dereference(), false);
			}
			refresh(element, false);
		}
	}

	/**
	 * Refresh a <code>DataElement</code> - immediately if indicated
	 *
	 * @param element an element to refresh
	 * @param immediate indicates to do the refresh immediately
	 */
	public void refresh(DataElement element, boolean immediate)
	{
		if ((_updateHandler != null) && (element != null))
		{
			//element = findLastUpdatedAncestor(element, 5);

			// update either client or ui
			//element.setUpdated(false);
			_updateHandler.update(element, immediate);
		}
	}

	/**
	 * Refresh a <code>DataElement</code> and its children to a certain depth - immediately if indicated
	 *
	 * @param element an element to refresh
	 * @param depth The depth to refresh the elements descendants. A depth of 0 means only the element itself
	 * is refreshed; 1 means the element and its children are refreshed, 2 - it, its children, and its grandchildren,
	 * etc.
	 * @param immediate indicates to do the refresh immediately. If true, the update handler will send updates
	 * for each refreshed element in the subtree of depth <code>depth</code> below the element.
	 */
	public void refresh(DataElement element, int depth, boolean immediate)
	{
		if (depth < 0) return;

		if (depth == 0) refresh(element, immediate);

		if (depth > 0)
		{
			if (element.getNestedSize() > 0)
			{
				List children = element.getNestedData();
				for (int i = 0; i < children.size(); i++)
				{
					refresh((DataElement)children.get(i), depth-1, immediate);
				}
			}
			refresh(element, immediate);
		}
	}

	public void update(ArrayList objects)
	{
		// this gets called in response to a query
		for (int i = 0; i < objects.size(); i++)
		{
			update((DataElement) objects.get(i));
		}
	}

	public void update(DataElement dataElement)
	{
		refresh(dataElement);
	}

	public void updateRemoteClassInstance(IRemoteClassInstance instance, String byteStreamHandlerId)
	{
		getUpdateHandler().updateClassInstance(instance, byteStreamHandlerId);
	}

	/**
	 * Transfers a file from a server to a client.  This should only be called from
	 * a miner on a different machine from the client.  If a file exists on the client
	 * side that the server file maps to then the existing client file will be replaced.
	 *
	 * @param remotePath the path of the file on the client side
	 * @param bytes an array of bytes representing a file
	 * @param size the number of bytes to transfer
	 * @param binary indicates whether to send the bytes as binary or text
	 */
	public void updateFile(String remotePath, byte[] bytes, int size, boolean binary)
	{
		updateFile(remotePath, bytes, size, binary, DataStoreResources.DEFAULT_BYTESTREAMHANDLER);
	}

	/**
	 * Transfers a file from a server to a client.  This should only be called from
	 * a miner on a different machine from the client.  If a file exists on the client
	 * side that the server file maps to then the existing client file will be replaced.
	 *
	 * @param remotePath the path of the file on the client side
	 * @param bytes an array of bytes representing a file
	 * @param size the number of bytes to transfer
	 * @param binary indicates whether to send the bytes as binary or text
	 * @param byteStreamHandlerId indicates the client byte stream handler to receive the bytes
	 */
	public void updateFile(String remotePath, byte[] bytes, int size, boolean binary, String byteStreamHandlerId)
	{
		remotePath = new String(remotePath.replace('\\', '/'));
		String fileName = mapToLocalPath(remotePath);
		if (fileName != null)
		{
			_updateHandler.updateFile(remotePath, bytes, size, binary, byteStreamHandlerId);
		}
	}

	/**
	 * Transfers and appends a file from a server to a client.  This should only be called from
	 * a miner on a different machine from the client.  If a file exists on the client
	 * side that the server file maps to then the existing client file will be appended to
	 *
	 * @param remotePath the path of the file on the client side
	 * @param bytes an array of bytes representing a file
	 * @param size the number of bytes to transfer
	 * @param binary indicates whether to send the bytes as binary or text
	 */
	public void updateAppendFile(String remotePath, byte[] bytes, int size, boolean binary)
	{
		updateAppendFile(remotePath, bytes, size, binary, DataStoreResources.DEFAULT_BYTESTREAMHANDLER);
	}

	/**
	 * Transfers and appends a file from a server to a client.  This should only be called from
	 * a miner on a different machine from the client.  If a file exists on the client
	 * side that the server file maps to then the existing client file will be appended to
	 *
	 * @param remotePath the path of the file on the client side
	 * @param bytes an array of bytes representing a file
	 * @param size the number of bytes to transfer
	 * @param binary indicates whether to send the bytes as binary or text
	 * @param byteStreamHandlerId indicates the client byte stream handler to receive the bytes
	 */
	public void updateAppendFile(String remotePath, byte[] bytes, int size, boolean binary, String byteStreamHandlerId)
	{
		remotePath = new String(remotePath.replace('\\', '/'));
		String fileName = mapToLocalPath(remotePath);
		if (fileName != null)
		{
			_updateHandler.updateAppendFile(remotePath, bytes, size, binary, byteStreamHandlerId);
		}
	}



	/**
	 * Transfers a file from a client to a server.  The should only be called from
	 * a client on a different machine from the server.  If a file exists on the server
	 * side that the client file maps to then the existing server file will be replaced.
	*
	 * @param remotePath the path of the file on the server side
	 * @param bytes an array of bytes representing a file
	 * @param size the number of bytes to transfer
	 * @param binary indicates whether to send the bytes as binary or text
	 * @param byteStreamHandlerId indicates which byteStreamHandler to use to receive the bytes
	 */
	public void replaceFile(String remotePath, byte[] bytes, int size, boolean binary, String byteStreamHandlerId)
	{
		remotePath = new String(remotePath.replace('\\', '/'));

		_commandHandler.sendFile(remotePath, bytes, size, binary, byteStreamHandlerId);
	}


	/**
	 * Transfers a file from a client to a server.  The should only be called from
	 * a client on a different machine from the server.  If a file exists on the server
	 * side that the client file maps to then the existing server file will be replaced.
	*
	 * @param remotePath the path of the file on the server side
	 * @param bytes an array of bytes representing a file
	 * @param size the number of bytes to transfer
	 * @param binary indicates whether to send the bytes as binary or text
	 */
	public void replaceFile(String remotePath, byte[] bytes, int size, boolean binary)
	{
		replaceFile(remotePath, bytes, size, binary, DataStoreResources.DEFAULT_BYTESTREAMHANDLER);
	}

	/**
	 * Transfers a file from a client to a server.  The should only be called from
	 * a client on a different machine from the server.  If a file exists on the server
	 * side that the client file maps to then the existing server file will be appended to.
	 *
	 * @param remotePath the path of the file on the server side
	 * @param bytes an array of bytes representing a file
	 * @param size the number of bytes to transfer
	 * @param binary indicates whether to send the bytes as binary or text
	 * @param byteStreamHandlerId indicates which byte stream handler to use to receive the bytes
	 */
	public void replaceAppendFile(String remotePath, byte[] bytes, int size, boolean binary, String byteStreamHandlerId)
	{
		remotePath = new String(remotePath.replace('\\', '/'));

		_commandHandler.sendAppendFile(remotePath, bytes, size, binary, byteStreamHandlerId);
	}

	/**
	 * Transfers a file from a client to a server.  The should only be called from
	 * a client on a different machine from the server.  If a file exists on the server
	 * side that the client file maps to then the existing server file will be appended to.
	 *
	 * @param remotePath the path of the file on the server side
	 * @param bytes an array of bytes representing a file
	 * @param size the number of bytes to transfer
	 * @param binary indicates whether to send the bytes as binary or text
	 */
	public void replaceAppendFile(String remotePath, byte[] bytes, int size, boolean binary)
	{
		replaceAppendFile(remotePath, bytes, size, binary, DataStoreResources.DEFAULT_BYTESTREAMHANDLER);
	}

	/**
	 * Makes a given client element available on the server
	 *
	 * @param localObject the element to transfer
	 */
	public void setObject(DataElement localObject)
	{
		setObject(localObject, true);
	}

	/**
	 * Makes a given client element available on the server
	 *
	 * @param localObject the element to transfer
	 * @param noRef indicates whether the element is a reference or not
	 */
	public void setObject(DataElement localObject, boolean noRef)
	{
		DataElement cmd = findCommandDescriptor(DataStoreSchema.C_SET);
		    //localDescriptorQuery(_root.getDescriptor(), DataStoreSchema.C_SET, 1);
		command(cmd, localObject, noRef);
	}

	public void modifyObject(DataElement localObject)
	{
		DataElement cmd = find(_descriptorRoot, DE.A_NAME, DataStoreResources.model_Modify, 2);
		DataElement status = _commandHandler.command(cmd, localObject, true);
		waitUntil(status, DataStoreResources.model_done);
	}

	/**
	 * Used at <code>DataStore</code> initialization time to indicate where to point the host root
	 *
	 * @param localHostObject the client host element to transfer to the server site
	 */
	public DataElement setHost(DataElement localHostObject)
	{
		DataElement cmd = findCommandDescriptor(DataStoreSchema.C_SET_HOST);//localDescriptorQuery(_root.getDescriptor(), DataStoreSchema.C_SET_HOST, 1);
		DataElement status = _commandHandler.command(cmd, localHostObject, false);
		waitUntil(status, DataStoreResources.model_done);
		return status;
	}

	/**
	 * Used at <code>DataStore</code> initialization time to setup the schema
	 *
	 */
	public DataElement getSchema()
	{
		DataElement cmd = findCommandDescriptor(DataStoreSchema.C_SCHEMA);//localDescriptorQuery(_root.getDescriptor(), DataStoreSchema.C_SCHEMA, 1);
		return command(cmd, _dummy);
	}

	/**
	 * Sets a property value preference on the client and server datastore
	 * @param property the property to set
	 * @param value the value of the property
	 */
	public void setPreference(String property, String value)
	{
		setPreference(property, value, true);
	}

	/**
	 * Sets a property value preference on the client datastore. If
	 * remoteAndLocal is set then the property get set on the server side as
	 * well as the client.
	 *
	 * @param property the property to set
	 * @param value the value of the property
	 * @param remoteAndLocal whether to propagate the change to the server
	 * @since 3.0
	 */
	public void setPreference(String property, String value, boolean remoteAndLocal)
	{
		_dataStorePreferences.put(property, value);
		if (isVirtual() && remoteAndLocal)
		{
			DataElement cmd = findCommandDescriptor(DataStoreSchema.C_SET_PREFERENCE);
			if (cmd != null)
			{
				DataElement prefObj = createObject(null, "preference", property); //$NON-NLS-1$
				prefObj.setAttribute(DE.A_VALUE, value);
				command(cmd, prefObj, true);
			}
		}

		// notify that preferences have changed
		IDataStorePreferenceListener[] listeners = null;
		synchronized (_dataStorePreferenceListeners){
			listeners = (IDataStorePreferenceListener[])_dataStorePreferenceListeners.toArray(new IDataStorePreferenceListener[_dataStorePreferenceListeners.size()]);
		}
			
		for (int i = 0; i < listeners.length; i++){
			IDataStorePreferenceListener listener = listeners[i];
			listener.preferenceChanged(property, value);
		}
	}

	public String getPreference(String property)
	{
		return (String)_dataStorePreferences.get(property);
	}

	/**
	 * Adds a preference change listener to the DataStore
	 * 
	 * @param listener the listener to add
	 * @since 3.0
	 */
	public void addDataStorePreferenceListener(IDataStorePreferenceListener listener){
		synchronized (_dataStorePreferenceListeners){	
			_dataStorePreferenceListeners.add(listener);
		}
	}

	/**
	 * Removes a specific preference change listener from the Datastore
	 * 
	 * @param listener the listener to remove
	 * @since 3.0
	 */
	public void removeDataStorePreferenceListener(IDataStorePreferenceListener listener){		
		synchronized (_dataStorePreferenceListeners){	
			_dataStorePreferenceListeners.remove(listener);	
		}
	}

	/**
	 * Removes all the preference change listeners
	 * @since 3.0
	 */
	public void removeAllDataStorePreferenceListeners(){
		synchronized (_dataStorePreferenceListeners){
			_dataStorePreferenceListeners.clear();
		}
	}

	/**
	 * Used to load and initialize a new miner on the host
	 * @param minerId the qualified classname of the miner to load
	 * @return the status of the activate miner command
	 */
	public DataElement activateMiner(String minerId)
	{
		// check if the miner is loaded
		DataElement minerInfo = findMinerInformation(minerId);
		if (minerInfo == null)
		{
			if (isVirtual())
			{
				DataElement cmd = findCommandDescriptor(DataStoreSchema.C_ACTIVATE_MINER);
				DataElement minerObj = createObject(null, DataStoreResources.model_miner, minerId);
				return command(cmd, minerObj, true);
			}
			else
			{
			}
		}
		return null;
	}



	/**
	 * Used at <code>DataStore</code> initialization time to initialize the miners
	 *
	 * @return the status element for the initMiners command
	 */
	public DataElement initMiners()
	{
		DataElement cmd = findCommandDescriptor(DataStoreSchema.C_INIT_MINERS);//localDescriptorQuery(_root.getDescriptor(), DataStoreSchema.C_INIT_MINERS, 1);

		return command(cmd, _dummy);
	}


	public DataElement queryInstall()
	{
		DataElement cmd = findCommandDescriptor(DataStoreSchema.C_QUERY_INSTALL);//localDescriptorQuery(_root.getDescriptor(), DataStoreSchema.C_QUERY_INSTALL, 1);
		return synchronizedCommand(cmd, _dummy);
	}

	public DataElement queryClientIP()
	{
		DataElement cmd = findCommandDescriptor(DataStoreSchema.C_QUERY_CLIENT_IP);//localDescriptorQuery(_root.getDescriptor(), DataStoreSchema.C_QUERY_INSTALL, 1);
		return synchronizedCommand(cmd, _dummy);
	}

	/**
	 * Client calls this to start the spiriting mechanism on the server.  The return value shouldn't be reliable here.  
	 * Originally this was a synchronized command but that can slow connect time.  Since no one should use the return value here,
	 * 
	 * @return whether the server spirit state has been queried
	 */
	public boolean queryServerSpiritState()
	{
		if (!_queriedSpiritState){
			DataElement spirittype = findObjectDescriptor(IDataStoreConstants.DATASTORE_SPIRIT_DESCRIPTOR);
			if (spirittype != null){		
				DataElement cmd = localDescriptorQuery(spirittype, IDataStoreConstants.C_START_SPIRIT, 2);
			
				if (cmd != null){
					command(cmd, _dummy); // start 
					_queriedSpiritState = true;
				}
			}
		}
		return _queriedSpiritState;
	}

	public DataElement queryHostJVM()
	{
		DataElement cmd = findCommandDescriptor(DataStoreSchema.C_QUERY_JVM);
		return synchronizedCommand(cmd, _dummy);
	}


	public void runRemoteClassInstance(IRemoteClassInstance runnable)
	{
		getCommandHandler().sendClassInstance(runnable, ClassByteStreamHandler.class.getName());
	}

	/**
	 * Used at <code>DataStore</code> initialization validate access to the <code>DataStore</code>
	 *
	 * @param ticketStr ticket string
	 * @return and indication of whether the ticket is valid or not
	 */
	public boolean showTicket(String ticketStr)
	{
		DataElement ticket = createTicket(ticketStr);
		DataElement status = queryShowTicket(ticket);

		if (status != null)
		{
			waitUntil(status, DataStoreResources.model_done);
		}
		else
		{
			return true;
		}

		return ticketValid(ticket);
	}

	public boolean ticketValid(DataElement ticket)
	{
		return ticket.getAttribute(DE.A_VALUE).equals(DataStoreResources.model_valid);
	}

	public DataElement createTicket(String ticketStr)
	{
		if (ticketStr == null)
		{
			ticketStr = "null"; //$NON-NLS-1$
		}
		return createObject(_tempRoot, DataStoreResources.model_ticket, ticketStr);
	}

	public DataElement queryShowTicket(DataElement ticket)
	{
		DataElement cmd = findCommandDescriptor(DataStoreSchema.C_VALIDATE_TICKET);
		DataElement status = _commandHandler.command(cmd, ticket, false);

		if (ticket.getName().equals("null")) //$NON-NLS-1$
		{
			return null;
		}
		return status;
	}

	/**
	 * Indicates whether a client has permission to access the <code>DataStore</code>
	 *
	 * @return and indication of whether the ticket is valid or not
	 */
	public boolean validTicket()
	{
		if (_ticket.getAttribute(DE.A_VALUE).equals(DataStoreResources.model_valid))
		{
			return true;
		}
		else
		{
			return false;
		}
	}



	/**
	 * Wait until a given status element reached the specified state.
	 * This is used for issuing synchronized commands
	 *
	 * @param status the status element
	 * @param state the state to wait until
	 */
	public void waitUntil(DataElement status, String state)
	{
		waitUntil(status, state, _timeout);
	}

	public boolean isWaiting(DataElement status)
	{
		return _waitingStatuses.contains(status);
	}

	public void stopWaiting(DataElement status)
	{
		_waitingStatuses.remove(status);
	}

	public void startWaiting(DataElement status)
	{
		_waitingStatuses.add(status);
	}

	public void waitUntil(DataElement status, String state, int timeout)
	{
		int timeToWait = 500;
		int timeWaited = 0;
		boolean timedOut = false;
		startWaiting(status);

		while ((status != null)
			&& (_status == null || _status.getName().equals("okay")) //$NON-NLS-1$
			&& !status.getName().equals(state)
			&& !status.getValue().equals(state)
			&& !status.getName().equals(DataStoreResources.model_incomplete)
			&& !timedOut)
		{
			if ((timeout != -1) && (timeWaited > timeout))
			{
				// waited too long!
				timedOut = true;
			}

			status.waitForUpdate(timeToWait);


			timeWaited += timeToWait;

			if (!isWaiting(status))
			{
				// stopped waiting
				return;
			}
		}

		stopWaiting(status);

		if (timedOut)
		{
			if (status != null)
				status.setAttribute(DE.A_NAME, DataStoreResources.model_timeout);
		}

	}

	public void cleanBadReferences(DataElement root)
	{
	}

	/**
	 * Tells the command handler to cancel all pending commands.
	 */
	public void cancelAllCommands()
	{
		_commandHandler.cancelAllCommands();
	}

	/**
	 * Creates and issues a synchronized command.
	 *
	 * @param commandDescriptor the comamnd descriptor for the command
	 * @param dataObject the subject of the command
	 * @return the status of the command
	 */
	public DataElement synchronizedCommand(DataElement commandDescriptor, DataElement dataObject)
	{
		return synchronizedCommand(commandDescriptor, dataObject, false);
	}

	/**
	 * Creates and issues a synchronized command.
	 *
	 * @param commandDescriptor the comamnd descriptor for the command
	 * @param dataObject the subject of the command
	 * @param noRef and indication of whether the subject should be referenced or not
	 * @return the status of the command
	 */
	public DataElement synchronizedCommand(DataElement commandDescriptor, DataElement dataObject, boolean noRef)
	{
		DataElement status = command(commandDescriptor, dataObject, noRef, true);
		waitUntil(status, DataStoreResources.model_done);

		return status;
	}

	/**
	 * Creates and issues a synchronized command.
	 *
	 * @param commandDescriptor the comamnd descriptor for the command
	 * @param arguments the arguments for the command
	 * @param dataObject the subject of the command
	 * @return the status of the command
	 */
	public DataElement synchronizedCommand(DataElement commandDescriptor, ArrayList arguments, DataElement dataObject)
	{
		DataElement status = command(commandDescriptor, arguments, dataObject, true);
		waitUntil(status, DataStoreResources.model_done);

		return status;
	}

	/**
	 * Creates and issues a command.
	 *
	 * @param commandDescriptor the comamnd descriptor for the command
	 * @param arguments the arguments for the command
	 * @param dataObject the subject of the command
	 * @return the status of the command
	 */
	public DataElement command(DataElement commandDescriptor, ArrayList arguments, DataElement dataObject)
	{
		return command(commandDescriptor, arguments, dataObject, true);
	}

	/**
	 * Creates and issues a command.
	 *
	 * @param commandDescriptor the comamnd descriptor for the command
	 * @param arguments the arguments for the command
	 * @param dataObject the subject of the command
	 * @param immediate indicates whether the command should be placed first on the request queue
	 * @return the status of the command
	 */
	public DataElement command(DataElement commandDescriptor, ArrayList arguments, DataElement dataObject, boolean immediate)
	{
		if (_commandHandler != null)
		{
			// as per bug #396440, default is now to not use references
			return _commandHandler.command(commandDescriptor, arguments, dataObject, false, immediate);
		}
		return null;
	}

	/**
	 * Creates and issues a command.
	 *
	 * @param commandDescriptor the comamnd descriptor for the command
	 * @param arg an argument for the command
	 * @param dataObject the subject of the command
	 * @return the status of the command
	 */
	public DataElement command(DataElement commandDescriptor, DataElement arg, DataElement dataObject)
	{
		return command(commandDescriptor, arg, dataObject, false);
	}

	/**
	 * Creates and issues a command.
	 *
	 * @param commandDescriptor the comamnd descriptor for the command
	 * @param arg an argument for the command
	 * @param dataObject the subject of the command
	 * @param immediate indicates whether the command should be placed first on the request queue
	 * @return the status of the command
	 */
	public DataElement command(DataElement commandDescriptor, DataElement arg, DataElement dataObject, boolean immediate)
	{
		if (_commandHandler != null)
		{
			// as per bug #396440, default is now to not use references
			return _commandHandler.command(commandDescriptor, arg, dataObject, false, immediate);
		}
		return null;
	}

	/**
	 * Creates and issues a command.
	 *
	 * @param commandDescriptor the comamnd descriptor for the command
	 * @param dataObject the subject of the command
	 * @return the status of the command
	 */
	public DataElement command(DataElement commandDescriptor, DataElement dataObject)
	{
		// as per bug #396440, default is now to not use references
		return command(commandDescriptor, dataObject, false);
	}

	/**
	 * Creates and issues a command.
	 *
	 * @param commandDescriptor the comamnd descriptor for the command
	 * @param dataObject the subject of the command
	 * @param noRef an indication of whether to reference the subject or not
	 * @return the status of the command
	 */
	public DataElement command(DataElement commandDescriptor, DataElement dataObject, boolean noRef)
	{
		return command(commandDescriptor, dataObject, noRef, false);
	}

	/**
	 * Creates and issues a command.
	 *
	 * @param commandDescriptor the comamnd descriptor for the command
	 * @param dataObject the subject of the command
	 * @param noRef an indication of whether to reference the subject or not
	 * @param immediate an indication of whether
	 * @return the status of the command
	 */
	public DataElement command(DataElement commandDescriptor, DataElement dataObject, boolean noRef, boolean immediate)
	{
		if (_commandHandler != null)
		{
			return _commandHandler.command(commandDescriptor, dataObject, !noRef);
		}

		return null;
	}

	/**
	 * Issues a command.
	 *
	 * @param commandObject an instance of a command
	 * @return the status of the command
	 */
	public DataElement command(DataElement commandObject)
	{
		return _commandHandler.command(commandObject);
	}

	/**
	 * Delete information from the <code>DataStore</code>.
	 *
	 */
	public void flush()
	{
		// flush the whole thing
		flush(_logRoot);
		flush(_hostRoot);
		flush(_minerRoot);
		flush(_tempRoot);
		flush(_descriptorRoot);
		flush(_dummy);
		flush(_root);
		flush(_externalRoot);
		
		// make sure these aren't null set since
		// Miners need them on shutdown
		// _logRoot = null;
		// _minerRoot = null;
		
		_hostRoot = null;
		_tempRoot = null;
		_descriptorRoot = null;
		_dummy = null;
		_root = null;
		_externalRoot = null;
		_status = null;
		_ticket = null;

		// clear the maps
		if (_classReqRepository != null){
			_classReqRepository.clear();
		}
		_cmdDescriptorMap.clear();
		_hashMap.clear();
		_lastCreatedElements.clear();
		
		if (_localClassLoaders != null){
			_localClassLoaders.clear();
		}
		_objDescriptorMap.clear();
		_relDescriptorMap.clear();
		
		_remoteLoader = null;
	}


	/**
	 * Delete information from the <code>DataStore</code> contained by an element.
	 *
	 * @param element the element from which to delete
	 */
	public void flush(DataElement element)
	{
		if (element != null)
		{
			deleteObjects(element);
		}
	}

	/**
	 * Find a command descriptor element in the schema with the given value.
	 *
	 * @param object the object descriptor representing the type of object that can issue such a command
	 * @param keyName the value of the command to search for
	 * @return the command descriptor for the specified command
	 */
	public DataElement localDescriptorQuery(DataElement object, String keyName)
	{
		return localDescriptorQuery(object, keyName, 5);
	}

	/**
	 * Find a command descriptor element in the schema with the given value.
	 *
	 * @param descriptor the object descriptor representing the type of object that can issue such a command
	 * @param keyName the value of the command to search for
	 * @param depth the depth of abstraction to search
	 * @return the command descriptor for the specified command
	 */
	public DataElement localDescriptorQuery(DataElement descriptor, String keyName, int depth)
	{
		if ((descriptor != null) && (depth > 0))
		{
			for (int i = 0; i < descriptor.getNestedSize(); i++)
			{
				DataElement subDescriptor = descriptor.get(i).dereference();
				String type = subDescriptor.getType();
				if (type == null)
				{
				}
				else if (type.equals(DE.T_COMMAND_DESCRIPTOR))
				{
					if (keyName.equals(subDescriptor.getValue()))
						return subDescriptor;
				}
				else if (type.equals(DE.T_ABSTRACT_COMMAND_DESCRIPTOR))
				{
					DataElement result = localDescriptorQuery(subDescriptor, keyName, depth - 1);
					if (result != null)
						return result;
				}
			}

			DataElement abstractedBy = getAbstractedByRelation();
			List abstractDescriptors = descriptor.getAssociated(abstractedBy);
			int numInherited = abstractDescriptors.size();

			for (int j = 0; j < numInherited; j++)
			{
				DataElement abstractDescriptor = (DataElement) abstractDescriptors.get(j);

				DataElement result = localDescriptorQuery(abstractDescriptor, keyName, depth - 1);
				if (result != null)
				{
					return result;
				}
			}
		}

		getCompatibilityHandler().handleMissingCommand(descriptor, keyName);
		return null;
	}

	public void addToRecycled(DataElement toRecycle)
	{
		synchronized (_recycled){
			if (!_recycled.contains(toRecycle)) 
				_recycled.add(0, toRecycle);
		}
	}

	/**
	 * Finds the element that represents the miner that implements a particular command.
	 *
	 * @param commandDescriptor a command descriptor
	 * @return the element representing a miner
	 */
	public DataElement getMinerFor(DataElement commandDescriptor)
	{
		String minerName = commandDescriptor.getSource();
		DataElement theMinerElement = find(_minerRoot, DE.A_NAME, minerName, 1);
		return theMinerElement;
	}

	/**
	 * Finds all the elements that are of a given type from a specified element.
	 *
	 * @param root where to search from
	 * @param type the descriptor representing the type of the objects to search for
	 * @return a list of elements
	 */
	public List findObjectsOfType(DataElement root, DataElement type)
	{
		ArrayList results = new ArrayList();
		List searchList = root.getAssociated(getContentsRelation());
		if (searchList != null)
		{
			for (int i = 0; i < searchList.size(); i++)
			{
				DataElement child = (DataElement) searchList.get(i);
				if (child.isOfType(type))
				{
					results.add(child);
				}

				List subResults = findObjectsOfType(child, type);
				for (int j = 0; j < subResults.size(); j++)
				{
					results.add(subResults.get(j));
				}
			}
		}

		return results;
	}

	/**
	 * Finds all the elements that are of a given type from a specified element.
	 *
	 * @param root where to search from
	 * @param type the descriptor representing the type of the objects to search for
	 * @return a list of elements
	 */
	public List findObjectsOfType(DataElement root, String type)
	{
		ArrayList results = new ArrayList();
		List searchList = root.getAssociated(getContentsRelation());
		if (searchList != null)
		{
			for (int i = 0; i < searchList.size(); i++)
			{
				DataElement child = (DataElement) searchList.get(i);
				if (child.getType().equals(type) || child.isOfType(type))
				{
					results.add(child);
				}

				List subResults = findObjectsOfType(child, type);
				for (int j = 0; j < subResults.size(); j++)
				{
					results.add(subResults.get(j));
				}
			}
		}

		return results;
	}

	/**
	 * Finds all the deleted elements
	 *
	 * @param root where to search from
	 * @return a list of elements
	 */
	public List findDeleted(DataElement root)
	{
		return findDeleted(root, 10);
	}

	/**
	 * Finds all the deleted elements
	 *
	 * @param root where to search from
	 * @param depth the depth to search
	 * @return a list of elements
	 */
	public List findDeleted(DataElement root, int depth)
	{
		ArrayList results = new ArrayList();
//		synchronized (root)
		// synchronized can cause hang here..but may not be necessary anyway since
		// we're not adding or removing anything here
		{
			if (root != null && root.getDataStore() == this)
			{
				if (results.contains(root))
				{
					return results;
				}

				if (root.isDeleted() && !results.contains(root))
				{
					results.add(root);
				}


				List searchList = root.getNestedData();
				if (searchList != null){
					for (int i = 0; i < searchList.size(); i++){
						DataElement child = (DataElement) searchList.get(i);
						if (child != null){
							if (child.isDeleted() && !results.contains(child)){
								results.add(child);
								if (!child.isReference()){
									if (depth > 0){
										List sResults = findDeleted(child, depth - 1);
										for (int j = 0; j < sResults.size(); j++){
											results.add(sResults.get(j));
										}
									}									
								}
							}
						}
					}
				}
			}
		}
		return results;
	}

	/**
	 * Finds all relationship descriptor types that can be applied to a particular element.
	 *
	 * @param descriptor the object descriptor that uses relationships
	 * @param fixateOn a filter for the type of relationships to look for
	 * @return a list of relationship descriptor elements
	 */
	public ArrayList getRelationItems(DataElement descriptor, String fixateOn)
	{
		ArrayList result = new ArrayList();
		if (descriptor != null)
		{
			// contained relationships
			for (int i = 0; i < descriptor.getNestedSize(); i++)
			{
				DataElement object = descriptor.get(i).dereference();

				String objType = (String) object.getElementProperty(DE.P_TYPE);
				if (objType.equals(DE.T_RELATION_DESCRIPTOR) || objType.equals(DE.T_ABSTRACT_RELATION_DESCRIPTOR))
				{
					if (fixateOn != null)
					{
						String objName = (String) object.getElementProperty(DE.P_NAME);
						if (objName.equals(fixateOn))
						{
							if (!result.contains(object))
								result.add(object);
						}
					}
					else
					{
						if (!result.contains(object))
							result.add(object);
					}
				}
			}

			// abstracted relationships
			List baseDescriptors = descriptor.getAssociated(getAbstractedByRelation());
			for (int j = 0; j < baseDescriptors.size(); j++)
			{

				DataElement baseDescriptor = (DataElement) baseDescriptors.get(j);
				ArrayList baseRelations = getRelationItems(baseDescriptor, fixateOn);
				for (int k = 0; k < baseRelations.size(); k++)
				{
					DataElement relation = (DataElement) baseRelations.get(k);
					if (!result.contains(relation))
					{
						result.add(relation);
					}
				}
			}
		}

		return result;
	}



	/**
	 * Find all elements from a given element that match a certain attribute.
	 *
	 * @param root the element to search from
	 * @param attribute the index of the attribute to match
	 * @param pattern the value to compare with the attribute
	 * @param ignoreCase an indication whether to ignore case for the attribute or not
	 * @return the list of matches
	 */
	public ArrayList searchForPattern(DataElement root, int attribute, String pattern, boolean ignoreCase)
	{
		int attributes[] = { attribute };
		String patterns[] = { pattern };
		return searchForPattern(root, attributes, patterns, 1, ignoreCase);
	}

	/**
	 * Find all elements from a given element that match a certain set of attributes.
	 *
	 * @param root the element to search from
	 * @param attributes a list of attributes to match
	 * @param patterns a list of values to compare with the attributes
	 * @param ignoreCase an indication whether to ignore case for the attributes or not
	 * @return the list of matches
	 */
	public ArrayList searchForPattern(DataElement root, ArrayList attributes, ArrayList patterns, boolean ignoreCase)
	{
		int att[] = new int[attributes.size()];
		String ptn[] = new String[attributes.size()];
		for (int i = 0; i < attributes.size(); i++)
		{
			att[i] = ((Integer) attributes.get(i)).intValue();
			ptn[i] = (String) (patterns.get(i));
		}

		return searchForPattern(root, att, ptn, attributes.size(), ignoreCase);
	}

	/**
	 * Find all elements from a given element that match a certain set of attributes.
	 *
	 * @param root the element to search from
	 * @param attributes a list of attribute indexes to match
	 * @param patterns a list of values to compare with the attributes
	 * @param numAttributes the number of attributes to match
	 * @param ignoreCase an indication whether to ignore case for the attributes or not
	 * @return the list of matches
	 */
	public ArrayList searchForPattern(DataElement root, int attributes[], String patterns[], int numAttributes, boolean ignoreCase)
	{
		return searchForPattern(root, attributes, patterns, numAttributes, ignoreCase, 1);
	}

	/**
	 * Find all elements from a given element that match a certain set of attributes.
	 *
	 * @param root the element to search from
	 * @param attributes a list of attribute indexes to match
	 * @param patterns a list of values to compare with the attributes
	 * @param numAttributes the number of attributes to match
	 * @param ignoreCase an indication whether to ignore case for the attributes or not
	 * @param depth how deep to search
	 * @return the list of matches
	 */
	public ArrayList searchForPattern(DataElement root, int attributes[], String patterns[], int numAttributes, boolean ignoreCase, int depth)
	{
		ArrayList searched = new ArrayList();
		return searchForPattern(root, attributes, patterns, numAttributes, ignoreCase, depth, searched);
	}

	/**
	 * Find all elements from a given element that match a certain set of attributes.
	 *
	 * @param root the element to search from
	 * @param attributes a list of attribute indexes to match
	 * @param patterns a list of values to compare with the attributes
	 * @param numAttributes the number of attributes to match
	 * @param ignoreCase an indication whether to ignore case for the attributes or not
	 * @param depth how deep to search
	 * @param searched a list of objects already searched
	 * @return the list of matches
	 */
	public ArrayList searchForPattern(DataElement root, int attributes[], String patterns[], int numAttributes, boolean ignoreCase, int depth, ArrayList searched)
	{
		ArrayList result = new ArrayList();
		if (depth > 0)
		{
			for (int i = 0; i < root.getNestedSize(); i++)
			{
				DataElement child = root.get(i);
				child = child.dereference();
				if ((child != null) && !searched.contains(child))
				{
					searched.add(child);
					if (child.patternMatch(attributes, patterns, numAttributes, ignoreCase))
					{
						result.add(child);
					}

					ArrayList subResults = searchForPattern(child, attributes, patterns, numAttributes, ignoreCase, depth - 1, searched);
					for (int j = 0; j < subResults.size(); j++)
					{
						result.add(subResults.get(j));
					}
				}
			}
		}

		return result;
	}

	/**
	 * Returns the element that represents the specified miner's data.
	 *
	 * @param minerName the qualified name of the miner
	 * @return the element representing the miner information
	 */
	public DataElement findMinerInformation(String minerName)
	{
		DataElement information = null;
		DataElement minerElement = find(_minerRoot, DE.A_NAME, minerName, 1);
		if (minerElement != null)
		{
			information = find(minerElement, DE.A_TYPE, DataStoreResources.model_data, 1);
		}

		return information;
	}

	/**
	 * Finds a descriptor element with a specified type and name.
	 *
	 * @param type the type of the descriptor
	 * @param name the name of the descriptor
	 * @return the found descriptor
	 */
	public DataElement findDescriptor(String type, String name)
	{
		if (_descriptorRoot != null)
		{
			synchronized (_descriptorRoot)
			{
			    if (type.equals(DE.T_OBJECT_DESCRIPTOR))
			    {
			        return (DataElement)_objDescriptorMap.get(name);
			    }
			    else if (type.equals(DE.T_COMMAND_DESCRIPTOR))
			    {
			        return (DataElement)_cmdDescriptorMap.get(name);

			    }
			    else if (type.equals(DE.T_RELATION_DESCRIPTOR))
			    {
			        return (DataElement)_relDescriptorMap.get(name);
			    }
			    else
			    {
					for (int i = 0; i < _descriptorRoot.getNestedSize(); i++)
					{
						DataElement descriptor = _descriptorRoot.get(i);

						if (descriptor.getName().equals(name) && descriptor.getType().equals(type))
						{
							return descriptor;
						}
					}
			    }
			}
		}

		return null;
	}

	/**
	 * Finds an object descriptor element with a specified name.
	 *
	 * @param name the name of the descriptor
	 * @return the found descriptor
	 */
	public DataElement findObjectDescriptor(String name)
	{
	    return (DataElement)_objDescriptorMap.get(name);
	}

	/**
	 * Finds an relation descriptor element with a specified name.
	 *
	 * @param name the name of the descriptor
	 * @return the found descriptor
	 */
	public DataElement findRelationDescriptor(String name)
	{
	    return (DataElement)_relDescriptorMap.get(name);
	}

	/**
	 * Finds an command descriptor element with a specified name.
	 *
	 * @param name the name of the descriptor
	 * @return the found descriptor
	 */
	public DataElement findCommandDescriptor(String name)
	{
	    return (DataElement)_cmdDescriptorMap.get(name);
	}

	/**
	 * Finds an element with the specified ID.
	 *
	 * @param id the ID of the descriptor
	 * @return the found element
	 */
	public DataElement find(String id)
	{
		synchronized (_hashMap){
			DataElement result = (DataElement) _hashMap.get(id);
			return result;
		}
	}

	/**
	 * Finds an element matching a specified attribute and name.
	 *
	 * @param root the element to search from
	 * @param attribute the index of the attribute to compare
	 * @param name the name of the element
	 * @return the first found element
	 */
	public DataElement find(DataElement root, int attribute, String name)
	{
		return find(root, attribute, name, 10);
	}

	/**
	 * Finds an element matching a specified attribute and name.
	 *
	 * @param root the element to search from
	 * @param attribute the index of the attribute to compare
	 * @param name the name of the element
	 * @param depth the depth of the search
	 * @return the first found element
	 */
	public DataElement find(DataElement root, int attribute, String name, int depth)
	{
		if ((root != null) && (name != null) && !root.isReference() && depth > 0)
		{

			if (StringCompare.compare(name, root.getAttribute(attribute), false))
			{
				return root;
			}
			else if (depth > 0)
			{
				for (int h = 0; h < root.getNestedSize(); h++)
				{
					DataElement nestedObject = root.get(h);
					String compareName = nestedObject.getAttribute(attribute);

					if (!nestedObject.isReference() && (compareName != null))
					{

						if (name.compareTo(compareName) == 0)
						{
							return nestedObject;
						}
						else
						{
							DataElement foundObject = find(nestedObject, attribute, name, depth - 1);
							if (foundObject != null)
							{
								return foundObject;
							}
						}
					}
				}
			}
		}

		return null;
	}

	/**
	 * Get the mapping from a remote path to a local path.
	 *
	 * @param aPath the remote path
	 * @return the local path
	 */
	public String mapToLocalPath(String aPath)
	{
		/*
		String result = null;

		char slash = '/';
		String remotePath = aPath.replace('\\', slash);
		String localRoot = _dataStoreAttributes.getAttribute(DataStoreAttributes.A_LOCAL_PATH).replace('\\', slash);
		String remoteRoot = getHostRoot().getSource().replace('\\', slash);

		if (localRoot.equals(remoteRoot))
		{
			result = remotePath;
		}
		else if (remotePath.startsWith(localRoot))
		{
			result = remotePath;
		}
		else if (remotePath.startsWith(remoteRoot))
		{
			result = new String(localRoot + slash + remotePath.substring(remoteRoot.length(), remotePath.length()));
		}
		else
		{
			// file is outside of scope
			// create temporary location
			int indexOfDrive = remotePath.indexOf(":");
			if (indexOfDrive > 0)
			{
				remotePath = remotePath.substring(indexOfDrive + 1, remotePath.length());
			}

			result = new String(localRoot + remotePath);
		}

		return result;
		*/

		// no more mapping here - expect actual paths
		return aPath;
	}

	/**
	 * Persist the <code>DataStore</code> tree from a given root
	 *
	 * @param root the element to persist from
	 * @param remotePath the path where the persisted file should be saved
	 * @param depth the depth of persistence from the root
	 */
	public void saveFile(DataElement root, String remotePath, int depth)
	{
		remotePath = new String(remotePath.replace('\\', '/'));
		String fileName = mapToLocalPath(remotePath);
		try
		{
			// need to create directories as well
			File file = new File(fileName);
			try
			{
				file = file.getCanonicalFile();
			}
			catch (IOException e)
			{
			}

			if (!file.exists())
			{
				File dir = new File(file.getParent());
				dir.mkdirs();
				file.createNewFile();
			}

			File newFile = new File(file.getCanonicalPath());

			if (newFile.canWrite())
			{
				FileOutputStream fileStream = new FileOutputStream(newFile);
				PrintStream fileWriter = new PrintStream(fileStream);
				BufferedWriter dataWriter = new BufferedWriter(new OutputStreamWriter(fileStream, DE.ENCODING_UTF_8));

				XMLgenerator generator = new XMLgenerator(this);
				generator.setIgnoreDeleted(true);
				generator.setFileWriter(fileWriter);
				generator.setDataWriter(dataWriter);
				generator.setBufferSize(1000);
				generator.generate(root, depth);
				generator.flushData();

				fileStream.close();
			}
		}
		catch (IOException e)
		{
			System.out.println(e);
		}
	}

	/**
	 * Save a file in the specified location
	 *
	 * @param remotePath the path where to save the file
	 * @param buffer the buffer to save in the file
	 */
	public void saveFile(String remotePath, byte[] buffer, int size, boolean binary)
	{
		getDefaultByteStreamHandler().receiveBytes(remotePath, buffer, size, binary);
	}

	/**
	 * Save a file in the specified location
	 *
	 * @param remotePath the path where to save the file
	 * @param buffer the buffer to save in the file
	 * @param byteStreamHandlerId indicates which byte stream handler to receive the bytes
	 */
	public void saveFile(String remotePath, byte[] buffer, int size, boolean binary, String byteStreamHandlerId)
	{

		getByteStreamHandler(byteStreamHandlerId).receiveBytes(remotePath, buffer, size, binary);
	}

	/**
	 * Saves a class to memory (but not to disk) where it can then be loaded by
	 * the RemoteClassLoaders. The class will be saved in a new thread, so this method
	 * will potentially return before the class has been saved.
	 *
	 * @param className the fully qualified name of the class
	 * @param buffer the contents of the class
	 * @param size the size of the buffer
	 */
	public void saveClass(String className, byte[] buffer, int size)
	{
		getDefaultClassByteStreamHandler().receiveBytes(className, buffer, size);
	}


	/**
	 * Saves a class instance
	 *
	 * @param buffer the contents of the class
	 * @param size the size of the buffer
	 * @param classbyteStreamHandlerId the id for the byte stream handler
	 */
	public void saveClassInstance(byte[] buffer, int size, String classbyteStreamHandlerId)
	{
		getDefaultClassByteStreamHandler().receiveInstanceBytes(buffer, size);
	}


	/**
	 * Saves a class to memory (but not to disk) where it can then be loaded by
	 * the RemoteClassLoaders. The class will be saved in a new thread, so this method
	 * will potentially return before the class has been saved.
	 *
	 * @param className the fully qualified name of the class
	 * @param buffer the contents of the class
	 * @param size the size of the buffer
	 * @param classbyteStreamHandlerId indicates which class byte stream handler to receive the bytes
	 */
	public void saveClass(String className, byte[] buffer, int size, String classbyteStreamHandlerId)
	{
		getClassByteStreamHandler(classbyteStreamHandlerId).receiveBytes(className, buffer, size);
	}

	/**
	 * Append a file to the specified location
	 *
	 * @param remotePath the path where to save the file
	 * @param buffer the buffer to append into the file
	 */
	public void appendToFile(String remotePath, byte[] buffer, int size, boolean binary)
	{
		getDefaultByteStreamHandler().receiveAppendedBytes(remotePath, buffer, size, binary);
	}

	/**
	 * Append a file to the specified location
	 *
	 * @param remotePath the path where to save the file
	 * @param buffer the buffer to append into the file
	 * @param byteStreamHandlerId indicates which byte stream handler to receive the bytes
	 */
	public void appendToFile(String remotePath, byte[] buffer, int size, boolean binary, String byteStreamHandlerId)
	{
		getByteStreamHandler(byteStreamHandlerId).receiveAppendedBytes(remotePath, buffer, size, binary);
	}

	/**
	 * Load a persisted <code>DataStore</code> tree into the specified <code>DataElement</code>
	 *
	 * @param root the root element of the persisted tree
	 * @param pathName the location of the persisted file
	 */
	public void load(DataElement root, String pathName)
	{
		String fileName = pathName;

		FileInputStream inFile = loadFile(fileName);
		if (inFile != null)
		{
			BufferedInputStream document = new BufferedInputStream(inFile);
			try
			{
				XMLparser parser = new XMLparser(this);
				DataElement subRoot = parser.parseDocument(document, null);
				if (subRoot != null)
				{
					root.removeNestedData();
					List nestedData = subRoot.getNestedData();
					if (nestedData != null)
					{
						root.addNestedData(nestedData, true);
					}
					refresh(root);
				}
			}
			catch (IOException e)
			{
			}
		}

	}

	public static FileInputStream loadFile(String fileName)
	{
		File file = new File(fileName);
		if (file.exists() && (file.length() > 0))
		{
			try
			{
				FileInputStream inFile = new FileInputStream(file);

				return inFile;
			}
			catch (FileNotFoundException e)
			{
				return null;
			}
		}
		else
		{
			return null;
		}
	}

	/**
	 * Indicate whether a given descriptor can contain the specified element
	 *
	 * @param descriptor the object descriptor to test
	 * @param dataElement the object to test against
	 * @return and indication whether dataElement can be in an object of type descriptor
	 */
	public boolean filter(DataElement descriptor, DataElement dataElement)
	{
		return filter(descriptor, dataElement, 2);
	}

	/**
	 * Indicate whether a given descriptor can contain the specified element
	 *
	 * @param descriptor the object descriptor to test
	 * @param dataElement the object to test against
	 * @param depth how far to search
	 * @return and indication whether dataElement can be in an object of type descriptor
	 */
	public boolean filter(DataElement descriptor, DataElement dataElement, int depth)
	{
		if (depth > 0)
		{
			depth--;

			String dataType = (String) dataElement.getElementProperty(DE.P_TYPE);
			String typeStr = (String) descriptor.getElementProperty(DE.P_NAME);

			if (((dataType != null) && (typeStr != null)) && (dataType.equals(typeStr) || typeStr.equals(DataStoreResources.model_all)))
			{
				return true;
			}
			else
			{
				for (int i = 0; i < descriptor.getNestedSize(); i++)
				{
					if (filter(descriptor.get(i), dataElement, depth))
					{
						return true;
					}
				}

				return false;
			}
		}

		return false;
	}

	/**
	 * Indicate whether a given set of descriptors can contain the specified element
	 *
	 * @param descriptors the object descriptors to test
	 * @param dataElement the object to test against
	 * @return and indication whether dataElement can be in an object of type descriptor
	 */
	public boolean filter(ArrayList descriptors, DataElement dataElement)
	{
		for (int i = 0; i < descriptors.size(); i++)
		{
			if (filter((DataElement) descriptors.get(i), dataElement))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Indicate whether an command is specified as transient
	 *
	 * @param commandObject the object descriptors to test
	 * @return and indication whether the command is transient
	 */
	public boolean isTransient(DataElement commandObject)
	{
		boolean isTransient = false;
		DataElement subject = commandObject.get(0);

		DataElement subjectDescriptor = subject.getDescriptor();
		if (subjectDescriptor != null)
		{
			DataElement minerElement = getMinerFor(commandObject);
			DataElement transientObjects = find(minerElement, DE.A_TYPE, DataStoreResources.model_transient, 1);
			if (transientObjects != null)
			{
				for (int i = 0; i < transientObjects.getNestedSize(); i++)
				{
					DataElement transientDescriptor = transientObjects.get(i).dereference();
					if (transientDescriptor == subjectDescriptor)
					{
						isTransient = true;
					}
				}
			}
		}

		return isTransient;
	}

	private void initializeDescriptors()
	{
		_dataStoreSchema.extendSchema(_descriptorRoot);
	}

	public void enableAutoRefresh(boolean flag)
	{
		_autoRefresh = flag;
	}

	public boolean isAutoRefreshOn()
	{
		return _autoRefresh;
	}
	/**
	 * getUserPreferencesDirectory() - returns directory on IFS where to store user settings
	 */
	public String getUserPreferencesDirectory()
	{
		if (_userPreferencesDirectory == null) {

			String clientUserID;
			if (_client != null){
				_userPreferencesDirectory = _client.getProperty("user.home"); //$NON-NLS-1$
				clientUserID = _client.getProperty("client.username"); //$NON-NLS-1$
			}
			else {
				_userPreferencesDirectory = System.getProperty("user.home"); //$NON-NLS-1$
				clientUserID = System.getProperty("client.username"); //$NON-NLS-1$
			}

			if (clientUserID == null || clientUserID.equals("")) //$NON-NLS-1$
			{
				clientUserID = ""; //$NON-NLS-1$
			}
			else
			{
				clientUserID += File.separator;
			}

 			// append a '/' if not there
  			if ( _userPreferencesDirectory.length() == 0 ||
  			     _userPreferencesDirectory.charAt( _userPreferencesDirectory.length() -1 ) != File.separatorChar ) {

				_userPreferencesDirectory = _userPreferencesDirectory + File.separator;
		    }

  			// for bug 282599, 
  			String logDirectory = System.getProperty(IDataStoreSystemProperties.DSTORE_LOG_DIRECTORY); 
  			if (logDirectory == null){
  				logDirectory = ".eclipse" + File.separator + "RSE" + File.separator;  //$NON-NLS-1$//$NON-NLS-2$
  			}
  			
  			if (logDirectory.length() > 0){
  				// append a '/' if not there
  				if (logDirectory.charAt( logDirectory.length() -1 ) != File.separatorChar ) {
  					logDirectory = logDirectory + File.separator;
  				}
  				
  				// remove the '/' if first char
  				if (logDirectory.charAt(0) == File.separatorChar){
  					logDirectory = logDirectory.substring(1);
  				}
  			}
  			
  			_userPreferencesDirectory = _userPreferencesDirectory + logDirectory + clientUserID;  
  			
	  		File dirFile = new File(_userPreferencesDirectory);
	  		if (!dirFile.exists()) {
	 	 		dirFile.mkdirs();
	  		}
		}
	  return _userPreferencesDirectory;
	}

	private void initialize()
	{
		_lastCreatedElements = new ArrayList();
		_minersLocations = new ArrayList();


		_random = new Random(System.currentTimeMillis());

		_objDescriptorMap = new HashMap(100);
		_cmdDescriptorMap = new HashMap(100);
		_relDescriptorMap = new HashMap(100);
		_dataStorePreferences = new HashMap(10);
		_dataStorePreferenceListeners = new ArrayList();

		_hashMap = new HashMap(2 * _initialSize);
		_recycled = new ArrayList(_initialSize);
		initElements(_initialSize);

		_timeout = 20000;
		_autoRefresh = false;//true;


		_dataStoreSchema = new DataStoreSchema(this);

		if (!isVirtual())
		{
			// get the time the server can remain idle before automatically shutting down
			// if the idle is 0 or not set then it is considered indefinite.
			// The server is considered idle for the period of which no commands are
			// received in server command handler
			String serverIdleShutdownTimeout = System.getProperty(IDataStoreSystemProperties.DSTORE_IDLE_SHUTDOWN_TIMEOUT); 
			if (serverIdleShutdownTimeout != null)
			{
				_serverIdleShutdownTimeout = Integer.parseInt(serverIdleShutdownTimeout);
			}

		}


		String tracingProperty = System.getProperty(IDataStoreSystemProperties.DSTORE_TRACING_ON);
		if (tracingProperty != null && tracingProperty.equals("true")) //$NON-NLS-1$
		{
			_tracingOn = true;
		}
		else
		{
			_tracingOn = false;
		}
		if (_tracingOn)
		{
			// only start tracing now if we're in one process per server mode
			if (SystemServiceManager.getInstance().getSystemService() == null){
				String logDir = getUserPreferencesDirectory();
				_traceFileHandle = new File(logDir, ".dstoreTrace"); //$NON-NLS-1$
				if (!_traceFileHandle.exists()){
					try { // try to create it
						_traceFileHandle.createNewFile();
					} catch (IOException e) {
					}
				}
				if (_traceFileHandle.canWrite() && setLogPermissions(_traceFileHandle)){
					try
					{
						_traceFile = new RandomAccessFile(_traceFileHandle, "rw"); //$NON-NLS-1$
						startTracing();
					}
					catch (IOException e)
					{
						// turn tracing off if there's a problem
						_tracingOn = false;
					}
				}
				else {
					_tracingOn = false;
				}
			}
		}

		_waitingStatuses = new ArrayList();

		_byteStreamHandlerRegistry = new ByteStreamHandlerRegistry();
		setDefaultByteStreamHandler();

		// remote class loading
		// only supported when -DDSTORE_REMOTE_CLASS_LOADING_ON=true
		//
		String remoteClassLoading = System.getProperty("DSTORE_REMOTE_CLASS_LOADING_ON"); //$NON-NLS-1$
		if (remoteClassLoading != null && remoteClassLoading.equals("true")){ //$NON-NLS-1$
			_classReqRepository = new HashMap();
			_classbyteStreamHandlerRegistry = new ClassByteStreamHandlerRegistry();
			setDefaultClassByteStreamHandler();
	
			// only allow remote class loading if this is 1 client per server
			if (SystemServiceManager.getInstance().getSystemService() == null){
				assignCacheJar();
			}	
			registerLocalClassLoader(this.getClass().getClassLoader());
		}
	}

	public void startDataElementRemoverThread()
	{
		if (!isVirtual() && _deRemover == null)
		{
			String memLogging = System.getProperty(IDataStoreSystemProperties.DSTORE_MEMLOGGING_ON); 
			_memLoggingOn = (memLogging != null && memLogging.equals("true")); //$NON-NLS-1$

			if (_memLoggingOn)
			{
				String logDir = getUserPreferencesDirectory();
				_memLoggingFileHandle = new File(logDir, ".dstoreMemLogging"); //$NON-NLS-1$
				// need this check, otherwise, we don't create this log file
				if (!_memLoggingFileHandle.exists()){
					try { // try to create it
						_memLoggingFileHandle.createNewFile();
					} catch (IOException e) {
					}
				}
				if (_memLoggingFileHandle.canWrite() && setLogPermissions(_memLoggingFileHandle)){
					try
					{
						_memLogFile = new RandomAccessFile(_memLoggingFileHandle, "rw"); //$NON-NLS-1$
						startMemLogging();
					}
					catch (IOException e)
					{
						// turn mem logging off if there's a problem
						_memLoggingOn = false;
					}
				}
				else {
					_memLoggingOn = false;
				}
			}
			_deRemover = new DataElementRemover(this);
			_deRemover.start();
		}
	}

	public boolean isDoSpirit()
	{
		if (isVirtual()) return _spiritModeOn;
		else return _spiritModeOn && _spiritCommandReceived;
	}

	public void receiveStartSpiritCommand()
	{
		_spiritCommandReceived = true;
	}

	public IByteStreamHandler getDefaultByteStreamHandler()
	{
	    return _byteStreamHandlerRegistry.getDefault();
	}

	public IClassByteStreamHandler getDefaultClassByteStreamHandler()
	{
	    return _classbyteStreamHandlerRegistry.getDefault();
	}

	public IByteStreamHandler getByteStreamHandler(String id)
	{
		return _byteStreamHandlerRegistry.getByteStreamHandler(id);
	}

	public IClassByteStreamHandler getClassByteStreamHandler(String id)
	{
		return _classbyteStreamHandlerRegistry.getClassByteStreamHandler(id);
	}

	public void setRemoteIP(String remoteIP)
	{
		_remoteIP = remoteIP;
	}

	public String getRemoteIP()
	{
		return _remoteIP;
	}

	/**
	 * Sets the current <code>ByteStreamHandler</code> to be the default.
	 */
	public void setDefaultByteStreamHandler()
	{
		setDefaultByteStreamHandler(null);
	}

	/**
	 * Sets the current <code>ClassByteStreamHandler</code> to be the default.
	 */
	public void setDefaultClassByteStreamHandler()
	{
		setDefaultClassByteStreamHandler(null);
	}

	/**
	 * Sets the current <code>ByteStreamHandler</code> to use for sending and receiving
	 * files.
	 * @param handler the <code>ByteStreamHandler</code> to use
	 */
	public void setDefaultByteStreamHandler(IByteStreamHandler handler)
	{
		if (handler == null)
		{
			DataElement log = null;
			handler = new ByteStreamHandler(this, log);
		}
		_byteStreamHandlerRegistry.setDefaultByteStreamHandler(handler);
	}

	public RemoteClassLoader getRemoteClassLoader()
	{
		if (_remoteLoader == null)
		{

			_remoteLoader = new RemoteClassLoader(this);
		}
		return _remoteLoader;
	}

	/**
	 * Sets the current <code>ClassByteStreamHandler</code> to use for sending and receiving
	 * classes.
	 * @param handler the <code>ClassByteStreamHandler</code> to use
	 */
	public void setDefaultClassByteStreamHandler(IClassByteStreamHandler handler)
	{
		if (handler == null)
		{
			DataElement log = null;
			handler = new ClassByteStreamHandler(this, log);
		}
		_classbyteStreamHandlerRegistry.setDefaultClassByteStreamHandler(handler);
	}

	/**
	 * Registers a byte stream handler.
	 * @param handler the handler to register
	 */
	public void registerByteStreamHandler(IByteStreamHandler handler)
	{
		_byteStreamHandlerRegistry.registerByteStreamHandler(handler);
	}

	/**
	 * Registers a class byte stream handler.
	 * @param handler the handler to register
	 */
	public void registerClassByteStreamHandler(IClassByteStreamHandler handler)
	{
		_classbyteStreamHandlerRegistry.registerClassByteStreamHandler(handler);
	}

	public void setByteConverter(IByteConverter converter)
	{
		_byteConverter = converter;
	}

	public IByteConverter getByteConverter()
	{
		if (_byteConverter == null)
		{
			_byteConverter = new DefaultByteConverter();
		}
		return _byteConverter;
	}


	/**
	 * Preallocates a set of <code>DataElement</code>s.
	 *
	 * @param the number of elements to preallocate
	 */
	private void initElements(int size)
	{
		synchronized (_recycled){
			for (int i = 0; i < size; i++)
			{
				_recycled.add(new DataElement(this));
			}
		}
	}

	/**
	 * Returns a new <code>DataElement</code> by either using an existing preallocated <code>DataElement</code> or
	 * by creating a new one.
	 *
	 * @return the new <code>DataElement</code>
	 */
	private synchronized DataElement createElement()
	{
		DataElement newObject = null;

		int numRecycled = _recycled.size();

		if (numRecycled > 0){
			synchronized (_recycled){				
				if (numRecycled > _MAX_FREE){
					int numRemoved = numRecycled - _MAX_FREE;
					for (int i = numRemoved - 1; i >=0; i--){
						_recycled.remove(i);						
					 }
				  }			
				newObject = (DataElement) _recycled.remove((_recycled.size() - 1));
			}
		}
		if (newObject == null)
		{
			newObject = new DataElement(this);
		}
		newObject.setSpirit(false);
		newObject.setUpdated(false);
		updateLastCreated(newObject);
		return newObject;
	}

	private void updateLastCreated(DataElement element)
	{
		_lastCreatedElements.add(0, element);
		if (_lastCreatedElements.size() > 4)
		{
			for (int i = _lastCreatedElements.size() - 1; i > 4; i--)
			{
				_lastCreatedElements.remove(i);
			}
		}
	}

	public List getLastCreatedElements()
	{
		return _lastCreatedElements;
	}

	private void createRoots()
	{
		_externalRoot = createObject(_root, DataStoreResources.model_host, "External DataStores", "", "extID"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		_tempRoot = createObject(_root, "temp", "Temp Root", "", "tempID"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		_dummy = createObject(_root, "temp", "dummy"); //$NON-NLS-1$ //$NON-NLS-2$
		_logRoot = createObject(_root, DataStoreResources.model_log, DataStoreResources.model_Log_Root, "", "logID"); //$NON-NLS-1$ //$NON-NLS-2$

		_minerRoot = createObject(_root, DataStoreResources.model_miners, DataStoreResources.model_Tool_Root, "", "minersID"); //$NON-NLS-1$ //$NON-NLS-2$

		_hostRoot =
			createObject(
				_root,
				DataStoreResources.model_host,
				_dataStoreAttributes.getAttribute(DataStoreAttributes.A_HOST_NAME),
				_dataStoreAttributes.getAttribute(DataStoreAttributes.A_HOST_PATH),
				"hostID"); //$NON-NLS-1$

		_status = createObject(_root, DataStoreResources.model_status, "okay", "", "statusID"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	private void deleteObjectHelper(DataElement from, DataElement toDelete, int depth)
	{
		if (depth > 0)
		{
			depth--;
			toDelete.delete();
			for (int i = 0; i < toDelete.getNestedSize(); i++)
			{
				DataElement subDelete = toDelete.get(i);
				if (subDelete != null && subDelete.getDataStore() == this && !subDelete.isDeleted()) 				{
					deleteObjectHelper(toDelete, subDelete, depth);
				}
			}

			String id = toDelete.getAttribute(DE.A_ID);
			synchronized (_hashMap)
			{
				_hashMap.remove(id);
				addToRecycled(toDelete);
			}

			if (!isConnected() && from != null)
			{
				from.removeNestedData(toDelete);
			}
		}
	}

	private void disconnectObjectHelper(DataElement toDisconnect, int depth)
	{
		if (depth > 0){
			depth--;
			_deRemover.addToQueueForRemoval(toDisconnect);
			List nestedData = toDisconnect.getNestedData();
			if (nestedData != null){
				for (int i = 0; i < nestedData.size(); i++){
					DataElement subDisconnect = (DataElement)nestedData.get(i);
					if (subDisconnect != null && !subDisconnect.isSpirit() && !subDisconnect.isDescriptor() && !subDisconnect.isReference()){
						disconnectObjectHelper(subDisconnect, depth);
					}
				}
			}
		}
	}

	private String makeIdUnique(String id)
	{
		boolean containsKey = false;
		synchronized (_hashMap){
			containsKey = _hashMap.containsKey(id);
		}
		
		if (!containsKey && id.length() > 0)
		{
			return id;
		}
		else
		{
			return generateId();
		}

	}

	private String generateId(DataElement parent, String type, String name)
	{
		// by default, name will be the id
		//return name;
		return generateId();
	}

	/**
	 * Generates a new unique ID to be used by a <code>DataElement</code>
	 *
	 * @return the new id
	 */
	protected String generateId()
	{
		String newId = String.valueOf(_random.nextInt());
		while (contains(newId))
		{
			newId = String.valueOf(_random.nextInt());
		}

		return newId;
	}

	public void startTracing()
	{
		if (_tracingOn && _traceFile != null && _traceFileHandle != null)
		{
			try
			{
				_traceFile.seek(_traceFileHandle.length());
			}
			catch (IOException e)
			{
			}

			trace("-----------------------------------------"); //$NON-NLS-1$
			trace("Start Tracing at " + System.currentTimeMillis()); //$NON-NLS-1$
			trace("DataStore version: "+ _RSE_version); //$NON-NLS-1$
		}
	}

	public void startMemLogging()
	{
		if (_memLoggingOn && _memLogFile != null && _memLoggingFileHandle != null)
		{
			try
			{
				_memLogFile.seek(_memLoggingFileHandle.length());
			}
			catch (IOException e)
			{
			}

			memLog("-----------------------------------------"); //$NON-NLS-1$
			memLog("Start Memory Logging at " + System.currentTimeMillis()); //$NON-NLS-1$
		}
	}

	public void memLog(String str)
	{
		internalMemLog(str);
	}

	public void trace(String str)
	{
		internalTrace(str);
	}

	public void trace(Throwable e)
	{
		internalTrace(e.getMessage());
		internalTrace(e);
	}

	private void internalTrace(Throwable e)
	{
		if (_tracingOn && _traceFile != null && e != null)
		{
			try
			{
				StackTraceElement[] stack = e.getStackTrace();
				for (int i = 0;i<stack.length;i++)
				{
					_traceFile.writeBytes(stack[i].toString());
					_traceFile.writeBytes(System.getProperty("line.separator")); //$NON-NLS-1$
				}
				_traceFile.writeBytes(System.getProperty("line.separator")); //$NON-NLS-1$
			}
			catch (IOException ex)
			{
			}
		}
	}

	private void internalTrace(String message)
	{
		if (_tracingOn && _traceFile != null && message != null)
		{
			try
			{
				_traceFile.writeBytes((new Date()).toString() + ": "); //$NON-NLS-1$
				_traceFile.writeBytes(message);
				_traceFile.writeBytes(System.getProperty("line.separator")); //$NON-NLS-1$
			}
			catch (IOException e)
			{
			}
		}
	}

	private void internalMemLog(String message)
	{
		if (_memLoggingOn && _memLogFile != null && message != null)
		{
			try
			{
				_memLogFile.writeBytes((new Date()).toString() + ": "); //$NON-NLS-1$
				_memLogFile.writeBytes(message);
				_memLogFile.writeBytes(System.getProperty("line.separator")); //$NON-NLS-1$
			}
			catch (IOException e)
			{
			}
		}
	}

	public void finish()
	{
		// dy: the call to flush deletes all the elements in the tree
		// which causes havoc for iSeries caching when switching between offline / online
		//if (isVirtual())
		//	flush();
		
		if (!isVirtual()){ // only on server
			if (getClient() != null){
				getClient().getLogger().logInfo(this.getName(), "DataStore.finish() - flush()"); //$NON-NLS-1$
			}
			flush();
		}
		
		if (_deRemover != null){
			_deRemover.finish();
		}

		if (_tracingOn)
		{
			try
			{
				if (_traceFile != null){
					_traceFile.writeBytes("Finished Tracing"); //$NON-NLS-1$
					_traceFile.writeBytes(System.getProperty("line.separator")); //$NON-NLS-1$
					_traceFile.close();
				}
			}
			catch (IOException e)
			{
			}
		}
	}

	/**
	 * On the server, sends a class request through the ServerUpdateHandler
	 * to the client.
	 * On the client, sends the request through the ClientCommandHandler
	 * @param className The fully qualified name of the class to request.
	 */
	public void requestClass(String className)
	{
		if (isVirtual())
		{
			_commandHandler.requestClass(className);
		}
		else
		{
			_updateHandler.requestClass(className);
		}
	}

	/**
	 * On the server, sends a keepalive request through the ServerUpdateHandler
	 * to the client.
	 */
	public void sendKeepAliveRequest()
	{
		if (isVirtual())
		{
			_commandHandler.sendKeepAliveRequest();
		}
		else
		{
			_updateHandler.sendKeepAliveRequest();
		}
	}

	/**
	 * On the server, sends a class through the ServerCommandHandler
	 * to the client.
	 * On the client, sends the class through the ClientUpdateHandler
	 * @param className The fully qualified name of the class to request.
	 */
	public void sendClass(String className)
	{
		if (isVirtual())
		{
			_commandHandler.sendClass(className);
		}
		else
		{
			_updateHandler.sendClass(className);
		}
	}


	/**
	 * @return the central repository for all class requests initiated by
	 * this server and its RemoteClassLoaders.
	 */
	public HashMap getClassRequestRepository()
	{
		return _classReqRepository;
	}


	/**
	 * Saves a class to disk (caches it) so that it can be loaded by the classloader without needing to
	 * download it again
	 */
	public synchronized void cacheClass(String className, byte[] bytes, int size)
	{
		if (_cacheJar == null) return;

		// get the entries from the old cache file
		Enumeration oldEntries;
		JarFile oldJarFile = null;

		try
		{
			oldJarFile = new JarFile(_cacheJar);
		}
		catch (IOException e)
		{
			// the jar must be corrupted, so we must erase it.
			System.out.println("Cache jarfile corrupted... erasing it."); //$NON-NLS-1$
			if (!_cacheJar.delete()) System.out.println("Couldn't erase corrupted jarfile!"); //$NON-NLS-1$
			// try to make a new one again.
			assignCacheJar();
			return;
		}
		oldEntries = oldJarFile.entries();

		// create a new cache file to store the new class in
		File newJarFile = new File(getCacheDirectory() + REMOTE_CLASS_CACHE_JARFILE_NAME + "_next" + JARFILE_EXTENSION); //$NON-NLS-1$
		JarOutputStream newJarOutput = null;

		try
		{
			newJarOutput = createNewCacheJar(newJarFile);
		}
		catch (IOException e)
		{
			System.out.println("Class caching failed. Could not create new cache jarfile."); //$NON-NLS-1$
			return;
		}

		// transfer the old entries to the new cache file
		while (oldEntries.hasMoreElements())
		{
			JarEntry nextEntry = (JarEntry) oldEntries.nextElement();
			BufferedInputStream source = null;
			try
			{
				 source = new BufferedInputStream(oldJarFile.getInputStream(nextEntry));
			}
			catch (Exception e)
			{
				continue;
			}
			nextEntry.setCompressedSize(-1);
			try
			{
				newJarOutput.putNextEntry(nextEntry);

				byte[] buf = new byte[1024];
				int numRead = source.read(buf);

				while (numRead > 0)
				{
					newJarOutput.write(buf, 0, numRead);
					numRead = source.read(buf);
				}

				newJarOutput.closeEntry();
				source.close();
			}
			catch (IOException e)
			{
				System.out.println("Class caching failed. Could not recopy entry from old jar. Cleaning..."); //$NON-NLS-1$
				try { newJarOutput.close(); } catch (IOException ee) { }
				if (!newJarFile.delete()) System.out.println("Couldn't erase new jarfile!"); //$NON-NLS-1$
			}
		}

		// add the new class file
		JarEntry newEntry = new JarEntry(className.replace('.', '/') + ".class"); //$NON-NLS-1$
		newEntry.setCompressedSize(-1);

		try
		{
			newJarOutput.putNextEntry(newEntry);
			newJarOutput.write(bytes, 0, size);
			newJarOutput.closeEntry();
			newJarOutput.close();
		}
		catch (IOException e)
		{
			System.out.println("Class caching failed. Could not cache new class into new jar. Cleaning..."); //$NON-NLS-1$
			try { newJarOutput.close(); } catch (IOException ee) { }
			if (!newJarFile.delete()) System.out.println("Couldn't erase new jarfile!"); //$NON-NLS-1$
		}

		// get rid of the old jar file
		try { oldJarFile.close(); } catch (IOException ee) { }
		if (!_cacheJar.delete()) System.out.println("Could not delete old cache jar."); //$NON-NLS-1$
		if (!newJarFile.renameTo(_cacheJar)) System.out.println("Could not rename new cache jar."); //$NON-NLS-1$

		System.out.println(className + " cached in " + _cacheJar.getAbsolutePath()); //$NON-NLS-1$
	}

	protected JarOutputStream createNewCacheJar(File newJar) throws IOException
	{
		newJar.createNewFile();
		JarOutputStream dest = new JarOutputStream(
				  new FileOutputStream(newJar));
		dest.setMethod(ZipOutputStream.DEFLATED);
		return dest;
	}

	protected void assignCacheJar()
	{		
		String cacheDirectory = getCacheDirectory();
		File cacheJar = new File(cacheDirectory + REMOTE_CLASS_CACHE_JARFILE_NAME + JARFILE_EXTENSION);
		File nextCacheJar = new File(cacheDirectory + REMOTE_CLASS_CACHE_JARFILE_NAME + "_next" + JARFILE_EXTENSION); //$NON-NLS-1$
		if (nextCacheJar.exists()) 
			nextCacheJar.renameTo(cacheJar);
		if (!cacheJar.exists() && cacheJar.canWrite())
		{
			try
			{
				JarOutputStream cacheOut = createNewCacheJar(cacheJar);
				cacheOut.putNextEntry(new JarEntry("/")); //$NON-NLS-1$
				cacheOut.closeEntry();
				cacheOut.close();

				_cacheJar = cacheJar;
			}
			catch (IOException e)
			{
				e.printStackTrace();
				_cacheJar = null;
				return;
			}
		}
		else {
			_cacheJar = null;
		}

		
	}

	protected String getCacheDirectory()
	{
		String cacheDirectory = getUserPreferencesDirectory();
		if (!cacheDirectory.endsWith(File.separator))
		{
			cacheDirectory = cacheDirectory + File.separator;
		}
		return cacheDirectory;
	}

	public File getRemoteClassLoaderCache()
	{
		return _cacheJar;
	}

	public void sendKeepAliveConfirmation()
	{
		if (isVirtual())
		{
			_commandHandler.sendKeepAliveConfirmation();
		}
		else
		{
			_updateHandler.sendKeepAliveConfirmation();
		}
	}

	/**
	 * @return what type of attribute tag is used on the peer DataStore to indicate whether dataelements
	 * are references, values, or spirit elements. If the peer DataStore is an older one, this will return
	 * "isRef", if its up-to-date, it will return "refType", and if the tag hasnt been determined yet, this method
	 * will return null.
	 */
	public String getReferenceTag()
	{
		return referenceTag;
	}

	/**
	 * Sets what type of attribute tag is used on the peer DataStore to indicate whether dataelements
	 * are references, values, or spirit elements.
	 */
	public void setReferenceTag(String tag)
	{
		referenceTag = tag;
	}

	/**
	 * @since 3.0
	 */
	public int printTree(String indent, DataElement root)
	{
		return printTree(indent, 0, root);
	}

	/**
	 * @since 3.0
	 */
	public int printTree(String indent, int number, DataElement root)
	{
		int total = number;
		if (root != null)
		{
			total++;
			boolean isSpirit = root.isSpirit();
			boolean isDeleted = root.isDeleted();
			String prefix = "DataElement"; //$NON-NLS-1$
			if (isSpirit)
				prefix += "<spirit>"; //$NON-NLS-1$
			if (isDeleted)
				prefix += "<deleted>"; //$NON-NLS-1$

			String msg = indent + prefix + "["+ total + "]("+root.getType()+", "+root.getName()+")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			System.out.println(msg);
			for (int i = 0; i < root.getNestedSize(); i++)
			{
				DataElement currentElement = root.get(i);
				total = printTree(indent + " ", total, currentElement); //$NON-NLS-1$
			}
		}
		return total;
	}

	/**
	 * Indicates that the datastore should transfer a DataElement's buffer
	 * attribute in the communication layer
	 * 
	 * @param flag true if the DataElement buffer attribute should be transfered
	 * @since 3.0
	 */
	public void setGenerateBuffer(boolean flag)
	{
		if (isVirtual())
		{
			// client side
			((ClientCommandHandler)_commandHandler).setGenerateBuffer(flag);
		}
		else
		{
			// server side
			((ServerUpdateHandler)_updateHandler).setGenerateBuffer(flag);
		}
	}

	/**
	 * This method is used to set the Client object for each user. The _client
	 * variable is null until setClient() is called. After _client is set, it
	 * can not be changed.
	 * 
	 * This method should only be called once to associate a particular client
	 * with a DataStore. By default, the client for the user of the DataStore
	 * process is set but, when there is an ISystemService, the daemon sets the
	 * client.
	 * 
	 * @param client the object of the Client class
	 * @since 3.0
	 */
	public void setClient(Client client)
	{
		// if client is not null, then this method gets called once.
		// subsequent calls will have no effect.
		if (_client == null){
			_client = client;
			_userPreferencesDirectory = null;
			String logDir = getUserPreferencesDirectory();

			// single process server?
			if (SystemServiceManager.getInstance().getSystemService() != null)
			{
				if (_tracingOn) {
					_traceFileHandle = new File(logDir, ".dstoreTrace"); //$NON-NLS-1$
					if (_traceFileHandle.canWrite() && setLogPermissions(_traceFileHandle)){
						try
						{
							_traceFile = new RandomAccessFile(_traceFileHandle, "rw"); //$NON-NLS-1$
							startTracing();
						}
						catch (IOException e)
						{
							// turn tracing off if there's a problem
							_tracingOn = false;
						}
					}
					else {
						_tracingOn = false;
					}
				}
			}
		}
	}

	/**
	 * This method is used to get the object of the Client stored for each user.
	 * 
	 * @return the object of the Client stored for each user
	 * @since 3.0
	 */
	public Client getClient()
	{
		return _client;
	}

	/**
	 * Sets the log file permissions for a file based on the "log.file.mode" system property.  If no
	 * such property exists, this just returns true.
	 * @param file the file to change permissions on
	 * @return true if successful or log.file.mode is turned off
	 */
	private static boolean setLogPermissions(File file){
		String fileMode = System.getProperty("log.file.mode"); //$NON-NLS-1$
		if (fileMode != null && fileMode.length() > 0){
			// just default to 600 for older levels of RSE
			String mode = "600"; //$NON-NLS-1$
			String chmodCmd = "chmod " + mode + ' ' + file.getAbsolutePath(); //$NON-NLS-1$
			try {
				Process p = Runtime.getRuntime().exec(chmodCmd);
				return p.exitValue() == 0;
			}
			catch (Exception e){				
				return false;
			}
		}
		return true;
	}

}
