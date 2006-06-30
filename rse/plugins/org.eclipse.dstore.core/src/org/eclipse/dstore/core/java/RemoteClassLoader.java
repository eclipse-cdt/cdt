/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.dstore.core.java;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

import org.eclipse.dstore.core.model.DataStore;



/**
 * This class loads a class from a remote peer.
 * This classloader is used just as any other classloader is used. However, 
 * when instantiating the RemoteClassLoader, a DataStore is associated with
 * it. The RemoteClassLoader goes through the following steps when trying to load
 * a class:
 *  1) Attempts to load the class from memory. Any class that had been loaded
 *     already this session would reside here.
 *  2) Attempts to find the class using its parent classloader.
 *  3) If caching preference is turned on:
 *     a) Attempts to find the class from the disk cache.
 *  4) Requests the class, through the DataStore, from the remote peer. Waits for
 *     response.
 *  5) If class cannot be found, throws ClassNotFoundException
 *  6) If class is found, loads the class.
 *  7) If caching preference is turned on:
 *     a) Caches new class in the disk cache for use in the next session.
 *  Notes:
 *  i) If there are one or more classes on which the target class depends,
 *     the RemoteClassLoader will attempt to load those classes too (possibly remotely),
 *     before loading the target class.
 *  ii) Since most implementations of Java use lazy classloading, the JVM may not
 *      attempt to load all required classes for the target class during class definition
 *      or instantiation. However, if the RemoteClassLoader was used to load the target
 *      class, then at any time, any additional classes required by the target class will
 *      be loaded using the RemoteClassLoader. Clients should be aware that this could trigger
 *      class requests and class transfers during the operation of objects of the target class,
 *      not just during definition and instantiation of it.
 *  iii) On the remote peer side, if you wish a class to be a candidate for transfer using the
 *       RemoteClassLoader on the opposite side of the connection, you MUST register the classloader
 *       for that class with the DataStore corresponding to the DataStore with which the RemoteClassLoader
 *       was instantiated. For example, in a client-server connection there is a "client" DataStore and a
 *       corresponding "server" DataStore. Suppose the server wishes to use the RemoteClassLoader to load
 *       class A from the client. Suppose A is loaded on the client using ClassLoaderForA. On the client
 *       side, ClassLoaderForA must be registered with the "client" DataStore so that when the
 *       class request for A comes in from the server, the client DataStore know how to load class A.
 *       
 *  Caching:
 *   To set your preference for caching, on either the client or server DataStore, use the following command:
 *    _dataStore.setPreference(RemoteClassLoader.CACHING_PREFERENCE, "true");   
 *   The cache of classes is kept in a jar in the following directory:
 *   $HOME/.eclipse/RSE/rmt_classloader_cache.jar
 *   To clear the cache, you must delete the jar.
 *   
 *  Threading Issues:
 *   It's safest to use the RemoteClassLoader on a separate thread, and preferably not
 *   from the CommandHandler or UpdateHandler threads. The RemoteClassLoader uses those
 *   threads to request and send the class. However, DataStore commands can be structured such that
 *   safe use of the RemoteClassLoader on these threads is possible. See below for an
 *   example.
 *   
 *  Using the RemoteClassLoader in your subsystem miner:
 *   Suppose you want the client to be able to kick off a class request in your host subsystem
 *   miner. In order to accomplish this, you would take the following steps:
 *   1) Add a command to your miner in the extendSchema() method. 
 *   2) Add logic in the handleCommand() method to route command to another method when handleCommand
 *      receives your new command.
 *   3) In your command handling method, get the name of the class to load from the subject
 *      DataElement.
 *   4) Load the class using the RemoteClassLoader.
 *   5) Make sure the class you are attempting to load exists on the client and that class's
 *      ClassLoader is registered with the DataStore!
 * 
 * @author mjberger
 *
 */
public class RemoteClassLoader extends ClassLoader
{ 
	public static String CACHING_PREFERENCE = "Class.Caching";
	private DataStore _dataStore;
	private boolean _useCaching = false;
	private CacheClassLoader _urlClassLoader;
	
	private class CacheClassLoader extends URLClassLoader
	{
		public CacheClassLoader(URL[] urls, ClassLoader parent)
		{
			super(urls, parent);
		}
		
		public Class findCachedClass(String className) throws ClassNotFoundException
		{
			return super.findClass(className);
		}
	}
	
	/**
	 * Constructor
	 * @param dataStore A reference to the datastore to be used by this
	 * RemoteClassLoader.
	 */
	public RemoteClassLoader(DataStore dataStore)
	{
		super(dataStore.getClass().getClassLoader());
		//_urlClassLoader = new URLClassLoader(new URL[0]);
		_dataStore = dataStore;
		useCaching();
	}
	
	public boolean useCaching()
	{
		boolean useCaching = false;
		String pref = _dataStore.getPreference(CACHING_PREFERENCE);
		if (pref != null && pref.equals("true"))
		{
			useCaching = true;
		}
		if (useCaching != _useCaching)
		{
			if (useCaching && _dataStore.getRemoteClassLoaderCache() != null)
			{
				try
				{
					URL cache = _dataStore.getRemoteClassLoaderCache().toURL();
					URL[] urls = new URL[] { cache };
					_urlClassLoader = new CacheClassLoader(urls, this);
				}
				catch (MalformedURLException e)
				{
					e.printStackTrace();
				}
			}
		}
		_useCaching = useCaching;
		return _useCaching;
	}

	/**
	 * Finds the specified class. If the class cannot be found locally, 
	 * a synchronous request for the class is sent to the client, and the calling thread
	 * waits for a response. If the client can find the class, it sends it back to
	 * the server. The server receives the class in a new thread, defines it, and
	 * then notifies this thread. The class is then returned by this method. If the class
	 * cannot be found, the client notifies the server, and this method throws a
	 * ClassNotFoundException.
	 * @param className the fully qualified classname to find
	 * @return the loaded class
	 * @throws ClassNotFoundException if the class cannot be found on either the client or the server.
	 * 
	 */
	protected Class findClass(String className) throws ClassNotFoundException
	{
		//System.out.println("finding "+className);
		
		// first try using the datastore's local classloaders
		
		ArrayList localLoaders = _dataStore.getLocalClassLoaders();
		if (localLoaders != null)
		{
			Class theClass = null;
			for (int i = 0; i < localLoaders.size(); i++)
			{
				try
				{
					theClass = ((ClassLoader)localLoaders.get(i)).loadClass(className);
					if (theClass != null) return theClass;
				}
				catch (Exception e)
				{
				}
			}
		}
		
		// next delegate the search to the superclass's find method.
		try
		{
			Class theClass = super.findClass(className);
			if (theClass != null) 
			{
				//System.out.println("Using super's: " + className);
				return theClass;
			}
		}
		catch (Exception e)
		{		
		}
		
		// DKM
		// only do lookup if the classname looks valid
		// don't want to be requesting rsecomm from client
		if (className.indexOf('.') == -1)
		{
			throw new ClassNotFoundException(className);
		}
		
		// if it cannot be found:

		// search the class request repository to see if the class has been requested
		// already
		ClassRequest request;
		request = (ClassRequest) _dataStore.getClassRequestRepository().get(className);

		if (request == null)
		{
			// the class has not been requested before
			// try to look in the disk cache first
			if (useCaching())
			{
				try
				{
					
					Class theClass = _urlClassLoader.findCachedClass(className);
			
					//System.out.println("Using cached: " + className);
					return theClass;
				}
				catch (Throwable e)
				{
					// its not in the disk cache, so request it synchronously
					return requestClass(className);
				}
			}
			else
			{
				return requestClass(className);
			}
		}
		else if (!request.isLoaded())
		{
			// the class has been requested before, but it has not yet been received
		//	System.out.println(className + " already requested but not loaded. Waiting for request to load.");
			request.waitForResponse(); // just wait until the class is received
			
			// after the class is received, get it from the repository and return it
			// or if the class failed to be received, throw an exception
			if (request.isLoaded()) return request.getLoadedClass(); 
			else throw new ClassNotFoundException(className); 
		}
		else if (request.isLoaded())
		{
			// the class has been requested before, and has already been received and loaded,
			// so just return it.
			return request.getLoadedClass();
		}
		// if we ever get to this point, the class has not been found, 
		// throw the exception
		else throw new ClassNotFoundException(className);
	}
	
	
	/**
	 * Receives a class sent by a remote agent and loads it.
	 * Notifies all threads waiting for this class to load that the
	 * class has been loaded.
	 * @param className the name of the class to receive
	 * @param bytes the bytes in the class
	 * @param the size of the class
	 */
	public synchronized void receiveClass(String className, byte[] bytes, int size)
	{
	//	System.out.println("receiving "+className);
		// check the class request repository to see if the class is there
		ClassRequest request = (ClassRequest) _dataStore.getClassRequestRepository().get(className);
		if (request != null)
		{
			if (request.isLoaded()) return; // do not attempt to reload the class
		}
		if (size == 0)
		{
			// this is the signal that the class could not be found on the client
		//	System.out.println("Empty class/class not found: "+className);
		//	System.out.println("notifying requester");
			request.notifyResponse(); // wake up the threads waiting for the class
			return;
		}
		Class receivedClass = null;
		try
		{
		//	System.out.println("defining "+className+"...");			
			// try to define the class. If any dependent classes cannot be
			// found the JRE implementation will call findClass to look for them.
			// Thus we could end up with a stack of requests all waiting until the
			// classes with no dependent classes load.
			receivedClass = defineClass(className, bytes, 0, size);
			
		//	System.out.println("...finished defining "+className);
		}
		catch (NoClassDefFoundError e)
		{
			// the JRE implementation could not find a dependent class.
			// (even after requesting it from the client). We must fail,
			// but wake up the threads waiting for this class.
			e.printStackTrace();
			request.notifyResponse();
			return;
		}
		catch (LinkageError e)
		{
			// this happens when the system tries to redefine a class.
			// dont try to redefine the class, just reload it.
			e.printStackTrace();
			try
			{
				receivedClass = loadClass(className);
			}
			catch (NoClassDefFoundError err)
			{
				// we shouldn't really get here unless it has tried
				// already to find the class on the client but couldnt,
				// so we might as well just fail here and notify threads waiting
				// for this class to load.
				err.printStackTrace();
				request.notifyResponse();
				return;				
			}
			catch (ClassNotFoundException ee)
			{
				// we definitely shouldnt get here
				request.notifyResponse();
				return;
			}
			
			// if after trying to define or trying to load the class
			// we still dont have it, notify the threads and fail.
			if (receivedClass == null) 
			{
				request.notifyResponse();
				return;
			}
		}
		if (request == null)
		{
			// not used right now, this is the case where a class was sent by
			// the client without us requesting it.
			request = new ClassRequest(className, false);
			request.setLoadedClass(receivedClass);
			_dataStore.getClassRequestRepository().put(className, request);
			return;
		}
		else
		{
			// SUCCESS! The class has been received, and defined, so just
			// load it into the class request object. This action will
			// also notify threads waiting for the class
	//		System.out.println("notifying requesters");
			request.setLoadedClass(receivedClass);
			if (useCaching())
			{
				_dataStore.cacheClass(className, bytes, size);
			}
		}
	}
	
	/**
	 * Kicks off a separate thread in which to request the class,
	 * rather than doing it synchronously.
	 * @param className The fully qualified name of the class to request.
	 */
	protected void requestClassInThread(String className)
	{
	//	System.out.println("requesting (in thread)"+className);
		LoadClassThread thread = new LoadClassThread(className);
		thread.start();
	}
	
	/**
	 * Requests a class (synchronously) from the client
	 * @param className The fully qualified name of the class to request.
	 * @return the requested class
	 * @throws ClassNotFoundException if the class was not found on the client
	 */
	public Class requestClass(String className) throws ClassNotFoundException
	{
		// first check to see if the class has been requested before
	//	System.out.println("requesting "+className);
		ClassRequest request;
		request = (ClassRequest) _dataStore.getClassRequestRepository().get(className);
		if (request == null)
		{
			// the class has not been requested yet, create a new ClassRequest
			// object to represent it
			request = new ClassRequest(className, true);
			_dataStore.getClassRequestRepository().put(className, request);
			request.setRequested(true);
			
			// put in the request for the class
			_dataStore.requestClass(className);
			
			// wait for a response
		//	System.out.println("thread to wait: "+Thread.currentThread().getName());
			if (!request.isLoaded()) request.waitForResponse();
		//	System.out.println("thread finished waiting: "+Thread.currentThread().getName());
			if (request.isLoaded()) return request.getLoadedClass();
			else throw new ClassNotFoundException(className);
		}
		else if (!request.isLoaded())
		{
			// class has already been requested, wait for it to load
	//		System.out.println("requested elsewhere, thread to wait: "+Thread.currentThread().getName());
			if (!request.isLoaded()) request.waitForResponse();
		//	System.out.println("requested elsewhere, thread finished waiting: "+Thread.currentThread().getName());
			if (request.isLoaded()) return request.getLoadedClass();
			else throw new ClassNotFoundException(className);
		}
		else if (request.isLoaded())
		{
			// class has already been requested and loaded, just return it
			return request.getLoadedClass();
		}
		// we shouldnt really get to this point, but if we do, it means the class
		// was not found.
		throw new ClassNotFoundException(className);
	}

	/**
	 * Causes a new thread to start in which the specified class is loaded
	 * into the repository. This method usually returns before the class has been
	 * loaded.
	 * @param className The fully qualified name of the class to load
	 */
	public synchronized void loadClassInThread(String className)
	{
		// check if the class has been requested before
		ClassRequest request;
		request = (ClassRequest) _dataStore.getClassRequestRepository().get(className);
		if (request == null)
		{
			// class has not been requested, request it in a thread
			request = new ClassRequest(className, false);
			_dataStore.getClassRequestRepository().put(className, request);
			request.setRequested(true);
			requestClassInThread(className);
			return;
		}
		else if (!request.isLoaded())
		{
			// class has been requested already, but not loaded. Just return.
			return;
		}
		else if (request.isLoaded())
		{
			// class has been requested already and already loaded. Just return.
			return;
		}
	}
	
	/**
	 * A new thread for loading classes in.
	 * @author mjberger
	 *
	 */
	protected class LoadClassThread extends Thread
	{
		private String _className;
		
		public LoadClassThread(String className)
		{
			_className = className;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		public void run()
		{
			_dataStore.requestClass(_className);
		}		
	}
}