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

/**
 * Represents a remote class request in the RemoteClassLoader. Contains
 * methods for getting the status of the request, as well as getting the
 * actual class after it has been loaded.
 * @author mjberger
 *
 */
public class ClassRequest
{
	private boolean _requested;
	private boolean _loaded;
	private boolean _synchronous;
	private String _className;
	private Class _class = null;
	
	/**
	 * Constructs a new ClassRequest
	 * @param className The name of the class requested
	 * @param synchronous whether or not the request is synchronous
	 */
	public ClassRequest(String className, boolean synchronous)
	{
		_synchronous = synchronous;
		_className = className;
		_requested = false;
		_loaded = false;
	}

	/**
	 * Causes the current thread to wait until this class request has been
	 * fulfilled.
	 */
	public synchronized void waitForResponse()
	{
		try
		{
			if (!_loaded) wait();		
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Causes all threads waiting for this class request to be filled
	 * to wake up.
	 */
	public synchronized void notifyResponse()
	{
		notifyAll();
	}
	
	/**
	 * Returns the class loaded, or null if it has not been loaded yet.
	 */
	public Class getLoadedClass()
	{
		return _class;
	}
	
	/**
	 * Returns whether or not the class has been loaded yet.
	 */
	public boolean isLoaded()
	{
		return _loaded;
	}
	
	/**
	 * Returns the name of the class requested/loaded.
	 */
	public String getClassName()
	{
		return _className;
	}
	
	/**
	 * Returns whether or not the class has been requested yet.
	 */
	public boolean isRequested()
	{
		return _requested;
	}
	
	/**
	 * Returns whether or not the class request is synchronous.
	 */
	public boolean isSynchronous()
	{
		return _synchronous;
	}
	
	/**
	 * Call this method when the request for the class has been sent.
	 */
	public void setRequested(boolean requested)
	{
		_requested = requested;
	}
	
	/**
	 * Call this method when the class has been received and loaded.
	 */
	public void setLoaded(boolean loaded)
	{
		_loaded = loaded;
	}
	
	/**
	 * Sets the class represented by this object after it has been loaded.
	 * (Sets loaded to be true and requested to be false). Notifies all threads
	 * waiting on this class request that the class has been loaded.
	 */
	public synchronized void setLoadedClass(Class loadedClass)
	{
		_class = loadedClass;
		setRequested(false);
		setLoaded(true);
		notifyAll();
	}
}