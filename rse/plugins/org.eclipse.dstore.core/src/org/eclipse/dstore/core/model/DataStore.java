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

package org.eclipse.dstore.core.model;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
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
import org.eclipse.dstore.core.util.ExternalLoader;
import org.eclipse.dstore.core.util.StringCompare;
import org.eclipse.dstore.core.util.XMLgenerator;
import org.eclipse.dstore.core.util.XMLparser;
import org.eclipse.dstore.extra.internal.extra.DomainNotifier;

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
 * <code>DataStore</code>, either directly via a <code>DomainNotifier</code> or indirectly over the communication
 * layer through a client <code>DataStore</code>.  
 * </p>
 *
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

	private DomainNotifier _domainNotifier;

	private ArrayList _loaders;
	private ArrayList _minersLocations;
	private ArrayList _localClassLoaders;
	private HashMap _dataStorePreferences;
	
	private ISSLProperties _sslProperties;

	private boolean _autoRefresh;

	private boolean _isConnected;
	private boolean _logTimes;
	private int _timeout;

	private HashMap _hashMap;
	private HashMap _objDescriptorMap;
	private HashMap _cmdDescriptorMap;
	private HashMap _relDescriptorMap;
	
	private ArrayList _recycled;

	private Random _random;
	private int _initialSize;

	private File _traceFileHandle;
	private RandomAccessFile _traceFile;
	private boolean _tracingOn;

	private ArrayList _waitingStatuses = null;
	
	private String _userPreferencesDirectory = null;
	
	private HashMap _classReqRepository;
	private File _cacheJar;
	public static final String REMOTE_CLASS_CACHE_JARFILE_NAME = "rmt_classloader_cache";
	public static final String JARFILE_EXTENSION = ".jar";	
	
	private int _serverVersion;
	private int _serverMinor;
	
	private List _lastCreatedElements;

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
		_initialSize = 100000;

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
		_initialSize = initialSize;

		initialize();
	}

	/**
	 * Creates a new <code>DataStore</code> instance
	 *
	 * @param attributes the default attributes of the <code>DataStore</code>
	 * @param commandHandler the DataStore's handler for sending commands
	 * @param updateHandler the DataStore's handler for doing updates
	 * @param domainNotifier the domain notifier 
	 */
	public DataStore(DataStoreAttributes attributes, CommandHandler commandHandler, UpdateHandler updateHandler, DomainNotifier domainNotifier)
	{
		_dataStoreAttributes = attributes;
		_commandHandler = commandHandler;
		_updateHandler = updateHandler;
		_domainNotifier = domainNotifier;
		_isConnected = true;
		_logTimes = false;
		_initialSize = 10000;

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
	 * @param initialSize the initialNumber of preallocated <code>DataElement</code>s 
	 */
	public DataStore(DataStoreAttributes attributes, CommandHandler commandHandler, UpdateHandler updateHandler, DomainNotifier domainNotifier, int initialSize)
	{
		_dataStoreAttributes = attributes;
		_commandHandler = commandHandler;
		_updateHandler = updateHandler;
		_domainNotifier = domainNotifier;
		_isConnected = true;
		_logTimes = false;
		_initialSize = initialSize;

		initialize();
		createRoot();
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
	 * @param loader the loader for the miners this <code>DataStore</code> will be using
	 */
	public void setLoaders(ArrayList loaders)
	{
		_loaders = loaders;
	}

	/**
	 * Adds a loader for this <code>DataStore</code>.  The loader is used to load miners (extension tools). 
	 *
	 * @param loader the loader for the miners this <code>DataStore</code> will be using
	 */
	public void addLoader(ExternalLoader loader)
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
			return _sslProperties.usingSSL();
		}
		return false;
	}
	
	/**
	 * Specifies the security properties of this DataStore.
	 * These properties indicate whether or not to use ssl,
	 * the keystore location and password.
	 * @param properties
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
				DataElement location = createObject(_tempRoot, "location", minersLocation);
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
	 * @param minersLocation a <code>DataElement</code> representing the location of the miners
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
	 * Sets the <code>DataStore</code>'s DomainNotifier 
	 *
	 * @param domainNotifier the domainNotifier
	 */
	public void setDomainNotifier(DomainNotifier domainNotifier)
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
		else if (_commandHandler instanceof org.eclipse.dstore.core.client.ClientCommandHandler)
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
	 */
	public DomainNotifier getDomainNotifier()
	{
		return _domainNotifier;
	}

	/**
	 * Returns the attribute indicated by an index.
	 *
	 * @param the index of the attribute to get
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
		return _hashMap.size();
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
				"rootID");
	

		_descriptorRoot = createObject(_root, DE.T_OBJECT_DESCRIPTOR, DataStoreResources.model_descriptors, "", "schemaID");

		_ticket = createObject(_root, DataStoreResources.model_ticket, "null", "", "ticketID");

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

			String sugId = reference.getId();
			_hashMap.put(sugId, reference);

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

			String sugId = reference.getId();
			_hashMap.put(sugId, reference);

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
	 * @return the new reference
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

			String toId = toReference.getId();
			_hashMap.put(toId, toReference);

			// reference with "from" relationship
			DataElement fromReference = createElement();

			fromReference.reInit(realObject, parent, fromRelation);

			realObject.addNestedData(fromReference, false);

			String fromId = fromReference.getId();
			_hashMap.put(fromId, fromReference);
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

			String toId = toReference.getId();
			_hashMap.put(toId, toReference);

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

			String fromId = fromReference.getId();
			_hashMap.put(fromId, fromReference);

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
		DataElement newObject = createElement();

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
		return createObject(parent, type, name, "");
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
		return createObject(parent, type, name, "");
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

		_hashMap.put(id, newObject);

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

		_hashMap.put(id, newObject);
		
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

		_hashMap.put(attributes[DE.A_ID], newObject);
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
		DataElement descriptor = createObject(parent, DE.T_ABSTRACT_OBJECT_DESCRIPTOR, name, "org.eclipse.rse.dstore.core", name);
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
	    	descriptor = createObject(parent, parentDescriptor, name, "org.eclipse.rse.dstore.core", name);
	    }
	    else
	    {
			descriptor = createObject(parent, DE.T_OBJECT_DESCRIPTOR, name, "org.eclipse.rse.dstore.core", name);
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
		DataElement descriptor = createObject(parent, DE.T_ABSTRACT_RELATION_DESCRIPTOR, name, "org.eclipse.rse.dstore.core", name);
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
		DataElement descriptor = createObject(parent, DE.T_RELATION_DESCRIPTOR, name, "org.eclipse.rse.dstore.core", name);
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
		DataElement cmd = createObject(parent, DE.T_ABSTRACT_COMMAND_DESCRIPTOR, name, "org.eclipse.rse.dstore.core", name);
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
	    	cmd = createObject(parent, parentDescriptor, name, "org.eclipse.rse.dstore.core", name);
	    }
	    else
	    {
	        cmd = createObject(parent, DE.T_COMMAND_DESCRIPTOR, name, "org.eclipse.rse.dstore.core", name);
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
		return _hashMap.containsKey(id);
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
		DataElement status = command(cmd, localObject, noRef);
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

	public void setPreference(String property, String value)
	{
		_dataStorePreferences.put(property, value);
		if (isVirtual())
		{
			DataElement cmd = findCommandDescriptor(DataStoreSchema.C_SET_PREFERENCE);
			if (cmd != null)
			{
				DataElement prefObj = createObject(null, "preference", property);
				prefObj.setAttribute(DE.A_VALUE, value);
				command(cmd, prefObj, true);
			}
		}
	}
	
	public String getPreference(String property)
	{
		return (String)_dataStorePreferences.get(property);
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
			ticketStr = "null";
		}
		return createObject(_tempRoot, DataStoreResources.model_ticket, ticketStr);
	}
	
	public DataElement queryShowTicket(DataElement ticket)
	{		
		DataElement cmd = findCommandDescriptor(DataStoreSchema.C_VALIDATE_TICKET);
		DataElement status = _commandHandler.command(cmd, ticket, false);

		if (ticket.getName().equals("null"))
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
			&& (_status == null || _status.getName().equals("okay"))
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
	 * @param dataObject the subject of the command
	 * @param noRef and indication of whether the subject should be referenced or not
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
		return command(commandDescriptor, arguments, dataObject, false);
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
			return _commandHandler.command(commandDescriptor, arguments, dataObject, true, immediate);
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
			return _commandHandler.command(commandDescriptor, arg, dataObject, true, immediate);
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
	 * @param object the object descriptor representing the type of object that can issue such a command
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
				DataElement subDescriptor = (DataElement) descriptor.get(i).dereference();
				String type = subDescriptor.getType();
				if (type == null)
				{
				}
				if (type.equals(DE.T_COMMAND_DESCRIPTOR))
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

		return null;
	}

	public void addToRecycled(DataElement toRecycle)
	{
		if (!_recycled.contains(toRecycle)) _recycled.add(0, toRecycle);
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
	 * @param type the descriptor representing the type of the objects to search for 
	 * @return a list of elements
	 */
	public synchronized List findDeleted(DataElement root)
	{
		return findDeleted(root, 10);
	}

	/**
	 * Finds all the deleted elements
	 *
	 * @param root where to search from 
	 * @param type the descriptor representing the type of the objects to search for 
	 * @return a list of elements
	 */
	public synchronized List findDeleted(DataElement root, int depth)
	{
		ArrayList results = new ArrayList();
		synchronized (root)
		{
			if (root != null && root.getDataStore() == this)
			{
				if (results.contains(root))
				{
					return results;
				}

				if (root != null && root.isDeleted())
				{
					results.add(root);
				}

				
				List searchList = root.getNestedData();

				if (searchList != null)
				{
					for (int i = 0; i < searchList.size(); i++)
					{
						DataElement child = (DataElement) searchList.get(i);
						if (child != null)
						{
							synchronized (child)
							{
								if (child != null && child.isDeleted() && !results.contains(child))
								{

									results.add(child);
									if (!child.isReference())
									{
										if (depth > 0)
										{
											List sResults = findDeleted(child, depth - 1);
											for (int j = 0; j < sResults.size(); j++)
											{
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
				DataElement object = ((DataElement) descriptor.get(i)).dereference();

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
				DataElement child = (DataElement) root.get(i);
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
		DataElement result = (DataElement) _hashMap.get(id);
		return result;
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
	}

	/**
	 * Persist the <code>DataStore</code> tree from a given root   
	 *
	 * @param root the element to persist from
	 * @param remotePath the path where the persisted file should be saved
	 * @param depth the depth of persistance from the root
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
	 * @param className the fully qualified name of the class
	 * @param buffer the contents of the class
	 * @param size the size of the buffer
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

			if (document != null)
			{
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
					if (filter((DataElement) descriptor.get(i), dataElement, depth))
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
		DataElement subject = (DataElement) commandObject.get(0);

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
			
			_userPreferencesDirectory = System.getProperty("user.home");
			String userID = System.getProperty("user.name");
			
 			// append a '/' if not there
  			if ( _userPreferencesDirectory.length() == 0 || 
  			     _userPreferencesDirectory.charAt( _userPreferencesDirectory.length() -1 ) != File.separatorChar ) {
  			     
				_userPreferencesDirectory = _userPreferencesDirectory + File.separator;
		    }
  		
  			_userPreferencesDirectory = _userPreferencesDirectory + ".eclipse" + File.separator + 
  																	"RSE" + File.separator + userID + File.separator;
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
		
		_hashMap = new HashMap(2 * _initialSize);
		_recycled = new ArrayList(_initialSize);
		initElements(_initialSize);

		_timeout = 20000;
		_autoRefresh = false;//true;
	

		_dataStoreSchema = new DataStoreSchema(this);

		String tracingProperty = System.getProperty("DSTORE_TRACING_ON");
		if (tracingProperty != null && tracingProperty.equals("true"))
		{
			_tracingOn = true;
		}
		else
		{
			_tracingOn = false;
		}
		
		String logDir = getUserPreferencesDirectory();
		if (_tracingOn)
		{
			
			_traceFileHandle = new File(logDir, ".dstoreTrace");

			try
			{
				_traceFile = new RandomAccessFile(_traceFileHandle, "rw");
				startTracing();
			}
			catch (IOException e)
			{
			}
		}

		//_remoteClassLoader = new RemoteClassLoader(this);
		_classReqRepository = new HashMap();
		
		_waitingStatuses = new ArrayList();
		
		_byteStreamHandlerRegistry = new ByteStreamHandlerRegistry();
		_classbyteStreamHandlerRegistry = new ClassByteStreamHandlerRegistry();
		setDefaultByteStreamHandler();
		setDefaultClassByteStreamHandler();
		
		assignCacheJar();
		
		registerLocalClassLoader(this.getClass().getClassLoader());
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
		for (int i = 0; i < size; i++)
		{
			_recycled.add(new DataElement(this));
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

		if (numRecycled > 1)
		{
			synchronized (_recycled)
			{

				/*
				if (numRecycled > _MAX_FREE)
				    {
					int numRemoved = numRecycled - _MAX_FREE;
					for (int i = 1; i <= numRemoved; i++)
					    {
						DataElement toRemove = (DataElement)_recycled.remove(numRemoved - i);
						toRemove = null;
					    }
				    }
				*/

				newObject = (DataElement) _recycled.remove((_recycled.size() - 1));
			}
		}
		else
		{
			newObject = new DataElement(this);
		}

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
		_externalRoot = createObject(_root, DataStoreResources.model_host, "External DataStores", "", "extID");

		_tempRoot = createObject(_root, "temp", "Temp Root", "", "tempID");
		_dummy = createObject(_root, "temp", "dummy");
		_logRoot = createObject(_root, DataStoreResources.model_log, DataStoreResources.model_Log_Root, "", "logID");

		_minerRoot = createObject(_root, DataStoreResources.model_miners, DataStoreResources.model_Tool_Root, "", "minersID");

		_hostRoot =
			createObject(
				_root,
				DataStoreResources.model_host,
				_dataStoreAttributes.getAttribute(DataStoreAttributes.A_HOST_NAME),
				_dataStoreAttributes.getAttribute(DataStoreAttributes.A_HOST_PATH),
				"hostID");

		_status = createObject(_root, DataStoreResources.model_status, "okay", "", "statusID");
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
				if (subDelete != null && subDelete.getDataStore() == this && !subDelete.isDeleted())
				{
					deleteObjectHelper(toDelete, subDelete, depth);
				}
			}

			String id = toDelete.getAttribute(DE.A_ID);
			_hashMap.remove(id);
			
		}
	}

	private String makeIdUnique(String id)
	{
		
		if (!_hashMap.containsKey(id))
		{
			return id;
		}
		else
		{
			return generateId();
			/*
			String newId = String.valueOf(_random.nextInt());
			while (_hashMap.containsKey(newId))
			{
				newId = String.valueOf(_random.nextInt());
			}

			return newId;
			*/
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
		//return "" + _uniqueNumber++;
		///*
		String newId = String.valueOf(_random.nextInt());
		while (_hashMap.containsKey(newId))
		{
			newId = String.valueOf(_random.nextInt());
		}

		return newId;
//		*/
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

			trace("-----------------------------------------");
			trace("Start Tracing at " + System.currentTimeMillis());
		}
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
					_traceFile.writeBytes(System.getProperty("line.separator"));
				}
				_traceFile.writeBytes(System.getProperty("line.separator"));
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
				_traceFile.writeBytes(message);
				_traceFile.writeBytes(System.getProperty("line.separator"));
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
		//flush();

		if (_tracingOn)
		{
			try
			{
				_traceFile.writeBytes("Finished Tracing");
				_traceFile.writeBytes(System.getProperty("line.separator"));
				_traceFile.close();
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
			System.out.println("Cache jarfile corrupted... erasing it.");
			if (!_cacheJar.delete()) System.out.println("Couldn't erase corrupted jarfile!");
			// try to make a new one again.
			assignCacheJar();
			return;
		}
		oldEntries = oldJarFile.entries();
			
		// create a new cache file to store the new class in
		File newJarFile = new File(getCacheDirectory() + REMOTE_CLASS_CACHE_JARFILE_NAME + "_next" + JARFILE_EXTENSION);
		JarOutputStream newJarOutput = null;
		
		try
		{
			newJarOutput = createNewCacheJar(newJarFile);
		}
		catch (IOException e)
		{
			System.out.println("Class caching failed. Could not create new cache jarfile.");
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
				System.out.println("Class caching failed. Could not recopy entry from old jar. Cleaning...");
				try { newJarOutput.close(); } catch (IOException ee) { }
				if (!newJarFile.delete()) System.out.println("Couldn't erase new jarfile!");
			}
		}
			
		// add the new class file
		JarEntry newEntry = new JarEntry(className.replace('.', '/') + ".class");
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
			System.out.println("Class caching failed. Could not cache new class into new jar. Cleaning...");
			try { newJarOutput.close(); } catch (IOException ee) { }
			if (!newJarFile.delete()) System.out.println("Couldn't erase new jarfile!");
		}

		// get rid of the old jar file
		try { oldJarFile.close(); } catch (IOException ee) { }
		if (!_cacheJar.delete()) System.out.println("Could not delete old cache jar.");
		if (!newJarFile.renameTo(_cacheJar)) System.out.println("Could not rename new cache jar.");
		
		System.out.println(className + " cached in " + _cacheJar.getAbsolutePath());
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
		File nextCacheJar = new File(cacheDirectory + REMOTE_CLASS_CACHE_JARFILE_NAME + "_next" + JARFILE_EXTENSION);
		if (nextCacheJar.exists()) nextCacheJar.renameTo(cacheJar);
		if (!cacheJar.exists())
		{
			try
			{
				JarOutputStream cacheOut = createNewCacheJar(cacheJar);
				cacheOut.putNextEntry(new JarEntry("/"));
				cacheOut.closeEntry();
				cacheOut.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
				_cacheJar = null;
				return;
			}
		}
		_cacheJar = cacheJar;
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

}