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

package org.eclipse.dstore.core.miners.miner;

/**
 * MinerThread is a utility class used for doing threaded operations in a miner. 
 */
public abstract class MinerThread extends Thread
{

	private volatile Thread minerThread;
	protected boolean _isCancelled;

	/**
	 * Constructor
	 */
	public MinerThread()
	{
		super();
		_isCancelled = false;
	}

	/**
	 * stops the thread
	 */
	public synchronized void stopThread()
	{
		if (minerThread != null)
		{
			_isCancelled = true;

			try
			{
				minerThread = null;
			}
			catch (Exception e)
			{
				System.out.println(e);
			}

		}
		notify();
	}

	/**
	 * runs the thread
	 */
	public void run()
	{
		Thread thisThread = Thread.currentThread();
		minerThread = thisThread;
		//thisThread.setPriority(thisThread.getPriority()+1);

		//This function lets derived classes do some initialization
		initializeThread();

		while (minerThread != null && minerThread == thisThread && minerThread.isAlive() && !_isCancelled)
		{
			try
			{
				sleep(100); 
				// yield();
			}
			catch (InterruptedException e)
			{
				System.out.println(e);
			}

			//This function is where the Threads do real work, and return false when finished
			if (!doThreadedWork())
			{
				try
				{
					minerThread = null;
				}
				catch (Exception e)
				{
					System.out.println(e);
				}
			}
		}

		//This function lets derived classes cleanup or whatever
		cleanupThread();
	}

	/**
	 * Implement this method to provide initialization of this thread.
	 */
	public abstract void initializeThread();

	/**
	 * Implement this method to provide the work implementation of this thread.
	 * This method gets called periodically by the miner thread so te work done
	 * here must be atomic.  Each time this is called a incremental unit of 
	 * work should be done.  Once all the work is done, <b>true</b> should be
	 * returned. 
	 * 
	 * @return <b>true</b> if all the work is done.
	 */
	public abstract boolean doThreadedWork();
	
	/**
	 * Implement this method to provide any cleanup that is required after
	 * all the work is done.
	 */
	public abstract void cleanupThread();
}