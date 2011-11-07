/*******************************************************************************
 * Copyright (c) 2008, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 *  The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: Noriaki Takatsu and Masao Nishimoto
 *
 * Contributors:
 *   Noriaki Takatsu (IBM)  - [220126] [dstore][api][breaking] Single process server for multiple clients
 *   Noriaki Takatsu (IBM)  - [228335] [dstore][multithread] start() in SecuredThread class
 *   David McKnight  (IBM)  - [358301] [DSTORE] Hang during debug source look up
 *******************************************************************************/

package org.eclipse.dstore.core.server;


import java.io.PrintWriter;

import org.eclipse.dstore.core.model.DataStore;

/**
 * @since 3.0
 */
public class SecuredThread extends Thread
{

	public DataStore _dataStore;


	/**
	 * 	Constructs a new SecuredThread without a DataStore.  In this case, the DataStore
	 * needs to be set sometime after creation via <code>setDataStore(DataStore)</code>.
	 */
	public SecuredThread() {
	}

	/**
	 * Constructs a new SecuredThread given a DataStore.
	 *
	 * @param dataStore used to extract user id and password for a client
	 */
	public SecuredThread(DataStore dataStore)
	{
		this(null, dataStore);
	}


	/**
	 * 	Constructs a new SecuredThread with a DataStore and a runnable.  After
	 * the thread starts, the runnable will be implicitly executed.
	 *
	 * @param runnable the runnable to be executed by the thread
	 * @param dataStore used to extract user id and password for a client
	 */
	public SecuredThread(Runnable runnable, DataStore dataStore) {
		super(runnable);
		_dataStore = dataStore;
	}


	/**
	 * Constructs a new SecuredThread with a DataStore, a runnable and name for the thread.
	 * After the thread starts, the runnable will be implicitly executed.
	 *
	 * @param runnable	the runnable to be executed by the thread
	 * @param threadName the name for the SecuredThread being created
	 * @param dataStore used to extract user id and password for a client
	 */
	public SecuredThread(Runnable runnable, String threadName, DataStore dataStore) {
		this(null, runnable, threadName, dataStore);
	}

	/**
	 * Constructs a new SecuredThread with a DataStore, a runnable and a ThreadGroup.
	 * After the thread starts, the runnable will be implicitly executed.
	 *
	 * @param group the thread group for which this thread will belong
	 * @param runnable	the runnable to be executed by the thread
	 * @param dataStore used to extract user id and password for a client
	 */
	public SecuredThread(ThreadGroup group, Runnable runnable, DataStore dataStore) {
		super(group, runnable);
		_dataStore = dataStore;
	}


	/**
	 * Constructs a new SecuredThread with a DataStore, a runnable, a name and a ThreadGroup.
	 * After the thread starts, the runnable will be implicitly executed.
	 *
	 * @param group the thread group for which this thread will belong
	 * @param runnable	the runnable to be executed by the thread
	 * @param threadName the name for the SecuredThread being created
	 * @param dataStore used to extract user id and password for a client
	 */
	public SecuredThread(ThreadGroup group, Runnable runnable, String threadName, DataStore dataStore) {
		super(group, runnable, threadName);
		_dataStore = dataStore;
	}


	/**
	 * Sets the DataStore associated with the client
	 * @param dataStore
	 */
	public void setDataStore(DataStore dataStore)
	{
		_dataStore = dataStore;
	}


	/**
	 * When run() is called, a check is made to see if there is an ISystemService.   If there is
	 * the <code>ISystemService.setThreadSecurity(Client)</code> is called before <code>Thread.run()</code>
	 * is called.
	 *
	 * If a Runnable was passed into the constructor for SecuredThread then, when <code>Thread.run()</code>
	 * is called, the Runnable will be invoked.
	 */
	public void run()
	{
		try
		{
			ISystemService systemService = SystemServiceManager.getInstance().getSystemService();
			if (systemService != null){
				systemService.setThreadSecurity(_dataStore.getClient());
			}
		}
		catch (OutOfMemoryError err){
			System.exit(-1);
		}
		catch (Throwable e)
		{
			e.printStackTrace(new PrintWriter(System.err));
		}

		super.run();
	}

	 /**
	  *
	  * As per bug 228335, this is commented out.
      *
	  * When start() is called, a check is made to see if there is an ISystemService.
	  * If there is, the <code>ISystemService.executeThread(SecuredThread)</code> is called.
	  * In this case, the run() method is invoked in a thread assigned from the running
	  * work threads
	  * If there isn't, the <code>super.start()</code> is called.
	  * In this case. the run() method is invoked as a new thread.

	 public void start()
	 {

	  try
	  {

	   ISystemService systemService = SystemServiceManager.getInstance().getSystemService();
	   if (systemService != null){
	    systemService.executeThread(this);
	   }
	   else
	   {
	    super.start();
	   }
	  }
	  catch(Throwable e)
	  {
	   e.printStackTrace(new PrintWriter(System.err));
	  }
	 }
	 */
	
	public void start(){
		try {
			super.start();
		}
		catch (OutOfMemoryError e){
			System.exit(-1);
		}
	}
}

