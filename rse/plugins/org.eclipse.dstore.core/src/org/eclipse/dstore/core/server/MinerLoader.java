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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.eclipse.dstore.core.java.RemoteClassLoader;
import org.eclipse.dstore.core.miners.miner.Miner;
import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.dstore.core.model.DataStoreAttributes;
import org.eclipse.dstore.core.model.ISchemaExtender;
import org.eclipse.dstore.core.model.ISchemaRegistry;
import org.eclipse.dstore.core.util.ExternalLoader;

/**
 * MinerLoader is an implementation of <code>ISchemaRegistry</code> used for
 * loading and initializing miners. 
 */
public class MinerLoader implements ISchemaRegistry
{


	private DataStore _dataStore;
	private ArrayList _miners;
	private ArrayList _minerList;
	private ArrayList _minerFileList;
	private ArrayList _connectedList;
	private ArrayList _loaders;
	private RemoteClassLoader _remoteLoader;
	private ExternalLoader _externalRemoteLoader;

	/**
	 * Constructor
	 * 
	 * @param dataStore the associated DataStore
	 * @param loaders the list of <code>ExternalLoader</code>s used be the miner loader
	 */
	public MinerLoader(DataStore dataStore, ArrayList loaders)
	{
		_dataStore = dataStore;
		_loaders = loaders;
		_miners = new ArrayList();
		_minerList = new ArrayList();
		_minerFileList = new ArrayList();
		_connectedList = new ArrayList();
	}

	/**
	 * Loads all miners that are specified in the default <i>minerFile.dat</i> as
	 * well as any others indicated by <code>DataStore.getMinersLocation</code> that
	 * have not yet been loaded.
	 */
	public void loadMiners()
	{
		// load the miners
		String pluginDir = _dataStore.getAttribute(DataStoreAttributes.A_PLUGIN_PATH);

		// default location
		String defaultMinerFile = pluginDir + File.separator + "minerFile.dat";
		File defaultMF = new File(defaultMinerFile);
		if (defaultMF.exists())
		{
			try
			{
				loadMiners(defaultMinerFile, DE.ENCODING_UTF_8);
			}
			catch (Exception e)
			{
				_dataStore.trace("failed to load minerFile.data with UTF-8.  Trying with native encoding");

				try
				{
					loadMiners(defaultMinerFile, null);
				}
				catch (Exception ex)
				{
					_dataStore.trace(ex);
				}				
			}
			_minerFileList.add(defaultMinerFile);
		}

		ArrayList minerLocations = _dataStore.getMinersLocation();

		for (int i = 0; i < minerLocations.size(); i++)
		{
			String minersDir = (String) minerLocations.get(i);
			String minerFile = null;
			if (minersDir.endsWith(".dat"))
			{
				minerFile = pluginDir + File.separator + minersDir;
			}
			else
			{
				minerFile = pluginDir + File.separator + minersDir + File.separator + "minerFile.dat";
			}
			//_dataStore.trace("load miners for " + minerFile);
			if (!_minerFileList.contains(minerFile))
			{
				try
				{
					loadMiners(minerFile, DE.ENCODING_UTF_8);
				}
				catch (Exception e)
				{
					_dataStore.trace("failed to load minerFile.data with UTF-8.  Trying with native encoding");
					try
					{
						loadMiners(minerFile, null);
					}
					catch (Exception ex)
					{
						_dataStore.trace(ex);
					}
				}
				_minerFileList.add(minerFile);
			}
		}
	}

	/**
	 * Loads that miners specified in a particular miner configuration file (i.e. <i>minerFile.dat</i>)
	 * @param minerFile a file specifying a list of miners
	 * @return a list of the loaded miners
	 */
	public ArrayList loadMiners(String minerFile, String encoding) throws Exception
	{
		// load the miners
		ArrayList unconnectedMiners = new ArrayList();
		File file = new File(minerFile);

		FileInputStream inFile = new FileInputStream(file);
		BufferedReader in = null;
		if (encoding == null)
		{
			in = new BufferedReader(new InputStreamReader(inFile));
		}
		else
		{
			in = new BufferedReader(new InputStreamReader(inFile, encoding));
		}

		String name = null;
		while ((name = in.readLine()) != null)
		{
			// check name
			name = name.trim();

			if (!name.startsWith("#") && (name.length() > 5))
			{
				Miner miner = loadMiner(name);
				if (miner != null)
				{
					unconnectedMiners.add(miner);
				}				
			}
		}

		connectMiners(unconnectedMiners);
		return _miners;
	}
	
	
	
	public Miner loadMiner(String name)
	{
		Miner miner = null;
		if (!_minerList.contains(name))
		{
			// only load new miners 
			try
			{
				ExternalLoader loader = getLoaderFor(name);
				if (loader != null)
				{
					// try to load and instantiate the miner
					// the RemoteClassLoader will kick off a synchronous
					// request to the client for any classes that cannot be found
					// on the host.
					Class theClass = loader.loadClass(name);
					miner = (Miner) theClass.newInstance();
					if (miner != null)
					{
						miner.setExternalLoader(loader);
						_minerList.add(name);
					}
				}
			}
			catch (NoClassDefFoundError e)
			{
				e.printStackTrace();
				handleNoClassFound(e.getMessage().replace('/','.'));
			}
			catch (ClassNotFoundException e)
			{
				e.printStackTrace();
				handleNoClassFound(name);
			}					
			catch (InstantiationException e)
			{
				e.printStackTrace();
			}
			catch (IllegalAccessException e)
			{
				e.printStackTrace();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
		}
		return miner;
	}
	
	private void handleNoClassFound(String name)
	{
		_remoteLoader.loadClassInThread(name);			
	}

	private void connectMiners(ArrayList unconnectedMiners)
	{
		// init list
		for (int i = 0; i < _miners.size(); i++)
		{
			_connectedList.add(((Miner) _miners.get(i)).getMinerName());
		}

		while (unconnectedMiners.size() > 0)
		{
			Miner miner = (Miner) unconnectedMiners.get(0);
			unconnectedMiners.remove(miner);
			if (connectMiner(miner))
			{
				_dataStore.trace("connected " + miner.getMinerName());
			}
			else
			{
				unconnectedMiners.add(miner);
			}
		}

	}

	public boolean connectMiner(Miner miner)
	{
		boolean canConnect = true;
		ArrayList dependencies = miner.getMinerDependencies();
		for (int i = 0; i < dependencies.size(); i++)
		{
			String dependency = (String) dependencies.get(i);
			if (!_connectedList.contains(dependency))
			{
				canConnect = false;
			}
		}

		if (canConnect)
		{
			// set the datastore for the miner 
			miner.setDataStore(_dataStore);
			miner.extendSchema(_dataStore.getDescriptorRoot());
			_dataStore.refresh(_dataStore.getDescriptorRoot());
			_miners.add(miner);
			_connectedList.add(miner.getMinerName());
			miner.start();
		}
		return canConnect;
	}

	/**
	 * Currently not used for the miner loader
	 */
	public void registerSchemaExtender(ISchemaExtender extender)
	{
	}

	/**
	 * Calls <code>extendSchema</code> on each of the loaded miners
	 * 
	 * @param dataStore the DataStore containing the base schema to extend
	 */
	public void extendSchema(DataStore dataStore)
	{
		DataElement schemaRoot = dataStore.getDescriptorRoot();
		for (int i = 0; i < _miners.size(); i++)
		{
			Miner miner = (Miner) _miners.get(i);
			miner.extendSchema(schemaRoot);
		}
		
	}
	
	public ExternalLoader getExternalRemoteLoader()
	{
		if (_externalRemoteLoader == null)
		{
			_externalRemoteLoader = new ExternalLoader(getRemoteLoader(), "*");
		}
		return _externalRemoteLoader;
	}
	
	public RemoteClassLoader getRemoteLoader()
	{ 
		return _dataStore.getRemoteClassLoader();
	}

	/**
	 * Returns the <code>ExternalLoader</code> for a particular
	 * class.
	 * 
	 * @param source a qualified classname
	 * @return the loader for the specified class
	 */
	public ExternalLoader getLoaderFor(String source)
	{
		ExternalLoader remoteLoader = getExternalRemoteLoader();
		
		// for now we always return the RemoteClassLoader
		
		//if (remoteLoader.canLoad(source))
		if(true)
		{
			//System.out.println("using RemoteClassLoader");
			return remoteLoader;
		}
		
		for (int i = 0; i < _loaders.size(); i++)
		{
			ExternalLoader loader = (ExternalLoader) _loaders.get(i);
			if (loader.canLoad(source))
			{
			//	System.out.println("using local loader");
				return loader;
			}
			else
			{
			}
		}

		return null;
	}

	/**
	 * Returns the loaded miners
	 * 
	 * @return the loaded miners
	 */
	public ArrayList getMiners()
	{
		return _miners;
	}

	/**
	 * Returns the miner indicated with the specified name
	 * 
	 * @param name the qualified classname of the miner
	 * @return the miner
	 */
	public Miner getMiner(String name)
	{
		for (int i = 0; i < _miners.size(); i++)
		{
			Miner miner = (Miner) _miners.get(i);
			if (miner.getClass().getName().equals(name))
			{
				return miner;
			}
		}

		return null;
	}

	/**
	 * Terminates the specified miner
	 * 
	 * @param name the qualified classname of the miner to terminate
	 */
	public void finishMiner(String name)
	{
		Miner miner = getMiner(name);
		miner.finish();
		_miners.remove(miner);
	}

	/**
	 * Terminate all the miners
	 */
	public void finishMiners()
	{
		for (int i = 0; i < _miners.size(); i++)
		{
			Miner miner = (Miner) _miners.get(i);
			miner.finish();
		}
	}
}