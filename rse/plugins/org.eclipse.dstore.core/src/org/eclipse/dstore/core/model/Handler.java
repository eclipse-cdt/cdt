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

/**
 * The Handler class is the base class for the threaded mechanisms in
 * the DataStore.  This is a thread that periodically does some activity.
 * The frequency of handling can be configured.
 */
public abstract class Handler extends Thread
{


	protected int _waitIncrement;
	protected DataStore _dataStore;
	private boolean _keepRunning;

	/**
	 * Constructor
	 */
	public Handler()
	{
		_keepRunning = true;
		_waitIncrement = 100;
	}

	/**
	 * Sets the time interval to wait between handling
	 * @param value the wait interval
	 */
	public void setWaitTime(int value)
	{
		_waitIncrement = value;
	}

	/**
	 * Returns the time interval to wait between handling
	 * @return the wait interval
	 */
	public int getWaitTime()
	{
		return _waitIncrement;
	}

	/**
	 * Sets the associated DataStore
	 * @param dataStore
	 */
	public void setDataStore(DataStore dataStore)
	{
		_dataStore = dataStore;
	}

	/**
	 * Indicates whether the handler is finished or not
	 * @return whether the handler is finished or not
	 */
	public boolean isFinished()
	{
		return !_keepRunning;
	}

	/**
	 * Finish handling
	 */
	public void finish()
	{
		if (_keepRunning)
		{

			_waitIncrement = 0;
			_keepRunning = false;

			/* causes hang
			try
			{
			    interrupt();
			    join();
			}
			catch (InterruptedException e)
			{
			    System.out.println(e);
			}
			*/
			handle();
		}
	}

	/**
	 * Implemented to provide the periodic activity to be done in a handler.
	 * This method is called between wait intervals by the handler thread.
	 */
	public abstract void handle();

	/**
	 * Runs the handler loop in a thread.
	 */
	public void run()
	{
		while (_keepRunning)
		{
			/*
			try
			{
				Thread.sleep(_waitIncrement);
				Thread.yield();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
				finish();
				return;
			}
			*/
			waitForInput();

			handle();
		}
	}
	
	/**
	 * Causes the current thread to wait until this class request has been
	 * fulfilled.
	 */
	public synchronized void waitForInput()
	{
		try
		{
			wait();		
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
			finish();
			return;
		}
	}
	
	/**
	 * Causes all threads waiting for this class request to be filled
	 * to wake up.
	 */
	public synchronized void notifyInput()
	{
		notifyAll();
	}
}