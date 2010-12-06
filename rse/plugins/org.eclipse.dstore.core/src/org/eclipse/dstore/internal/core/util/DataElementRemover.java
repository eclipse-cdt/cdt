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
 *  David McKnight  (IBM)  - [202822] don't need to remove children from map here
 *  David McKnight  (IBM)  - [255390] check memory to determine whether to queue
 *  David McKnight  (IBM)  - [261644] [dstore] remote search improvements
 *  David McKnight   (IBM) - [294933] [dstore] RSE goes into loop
 *  David McKnight   (IBM) - [331922] [dstore] enable DataElement recycling
 *******************************************************************************/

package org.eclipse.dstore.internal.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.dstore.core.model.Handler;
import org.eclipse.dstore.core.model.IDataStoreConstants;

public class DataElementRemover extends Handler 
{
	private LinkedList _queue;
	private static int numRemoved = 0;
	private static int numDisconnected = 0;
	private static int numCreated = 0;
	private static int numGCed = 0;
	
	// The following determine how DataElements are chosen to be removed once they
	// are in the queue for removal. 	
	// The queue is checked every _intervalTime milliseconds and all elements
	// that are older than _expiryTime milliseconds are removed.
	public static final int DEFAULT_EXPIRY_TIME = 600; // in seconds
	public static final int DEFAULT_INTERVAL_TIME = 60; // in seconds
	private int _intervalTime = DEFAULT_INTERVAL_TIME * 10;
	private int _expiryTime = DEFAULT_EXPIRY_TIME * 10;
	public static final String EXPIRY_TIME_PROPERTY_NAME = "SPIRIT_EXPIRY_TIME"; //$NON-NLS-1$
	public static final String INTERVAL_TIME_PROPERTY_NAME = "SPIRIT_INTERVAL_TIME"; //$NON-NLS-1$
	public MemoryManager _memoryManager;
	
	public DataElementRemover(DataStore dataStore)
	{
		super();
		_memoryManager = MemoryManager.getInstance(dataStore);
		_dataStore = dataStore;
		_queue = new LinkedList();
		getTimes();
		setWaitTime(_intervalTime);
		DataElement spiritnode = _dataStore.createObjectDescriptor(_dataStore.getDescriptorRoot(), IDataStoreConstants.DATASTORE_SPIRIT_DESCRIPTOR);
		_dataStore.createCommandDescriptor(spiritnode, "StartSpirit", "DataElementRemover", IDataStoreConstants.C_START_SPIRIT); //$NON-NLS-1$ //$NON-NLS-2$
		_dataStore.refresh(_dataStore.getDescriptorRoot());
	}
	
	protected void getTimes()
	{
		try
		{
			String expiryTime = System.getProperty(EXPIRY_TIME_PROPERTY_NAME);
			if (expiryTime != null && !expiryTime.equals("")) _expiryTime = Integer.parseInt(expiryTime) * 1000;			 //$NON-NLS-1$
		}
		catch (Exception e)
		{
			System.out.println("Invalid spirit expiry time property, using default."); //$NON-NLS-1$
			_expiryTime = DEFAULT_EXPIRY_TIME;
		}
		try
		{
			String intervalTime = System.getProperty(INTERVAL_TIME_PROPERTY_NAME);
			if (intervalTime != null && !intervalTime.equals("")) _intervalTime = Integer.parseInt(intervalTime) * 1000;			 //$NON-NLS-1$
		}
		catch (Exception e)
		{
			System.out.println("Invalid spirit interval time property, using default."); //$NON-NLS-1$
			_intervalTime = DEFAULT_INTERVAL_TIME;
		}
	}
	
	public static void addToRemovedCount()
	{
		numRemoved++;
	}
	
	public static void addToCreatedCount()
	{
		numCreated++;
	}
	
	public static void addToGCedCount()
	{
		numGCed++;
	}

	
	public synchronized void addToQueueForRemoval(DataElement element)
	{
		synchronized (_queue) 
		{
			if(isMemoryThresholdExceeded()) {
				if(element.isSpirit()) {
					unmap(element);
				}
				
				// do immediate clearing of queue since we're low on memory
				clearQueue(true);
				return;
			}
			if (_dataStore.isDoSpirit() && _dataStore == element.getDataStore())
			{
				QueueItem item = new QueueItem(element, System.currentTimeMillis());
				_queue.add(item);
			}
		}
		notifyInput();
	}

	private boolean isMemoryThresholdExceeded(){
		return _memoryManager.isThresholdExceeded();
	}
	
	public void handle()
	{
		clearQueue(false);
	}
	
	public synchronized void clearQueue(boolean force)
	{
		synchronized (_queue)
		{
			_dataStore.memLog("           "); //$NON-NLS-1$
			int disconnected = 0;
			if (!_dataStore.isDoSpirit())
			{
				if (_queue.size() > 0) 
				{
					_dataStore.memLog("Clearing queue of size " + _queue.size() + ". DSTORE_SPIRIT_ON not set or set to false."); //$NON-NLS-1$ //$NON-NLS-2$
					_queue.clear();
				}
				_dataStore.memLog("Total heap size: " + Runtime.getRuntime().totalMemory()); //$NON-NLS-1$
				_dataStore.memLog("Elements created so far: " + numCreated); //$NON-NLS-1$
				_dataStore.memLog("Elements disconnected so far: " + numDisconnected); //$NON-NLS-1$
				_dataStore.memLog("Spirit elements cleaned so far: " + numRemoved); //$NON-NLS-1$
				_dataStore.memLog("DataElements GCed so far: " + numGCed); //$NON-NLS-1$
				return;
			}
			_dataStore.memLog("Total heap size before disconnection: " + Runtime.getRuntime().totalMemory()); //$NON-NLS-1$
			
			_dataStore.memLog("Size of queue: " + _queue.size()); //$NON-NLS-1$
			
			ArrayList toRefresh = new ArrayList();
			while (_queue.size() > 0 && (force || System.currentTimeMillis() - ((QueueItem) _queue.getFirst()).timeStamp > _expiryTime))
			{
				DataElement toBeDisconnected = ((QueueItem) _queue.removeFirst()).dataElement;
				if (!toBeDisconnected.isSpirit()) 
				{
					toBeDisconnected.setSpirit(true);
					toBeDisconnected.setUpdated(false);
					DataElement parent = toBeDisconnected.getParent();
					if (!toRefresh.contains(parent))
					{
						//System.out.println("disconnect parent:"+parent.getName());
						toRefresh.add(toBeDisconnected.getParent());
					}
						//_dataStore.refresh(toBeDisconnected);
					disconnected++;
					numDisconnected++;
				}
				else
				{
					//_dataStore.memLog(toBeDisconnected.toString());
				}
				unmap(toBeDisconnected);
			}
	
			_dataStore.refresh(toRefresh);
			
			_dataStore.memLog("Disconnected " + disconnected + " DataElements."); //$NON-NLS-1$ //$NON-NLS-2$
			_dataStore.memLog("Elements created so far: " + numCreated); //$NON-NLS-1$
			_dataStore.memLog("Elements disconnected so far: " + numDisconnected); //$NON-NLS-1$
			_dataStore.memLog("Spirit elements cleaned so far: " + numRemoved); //$NON-NLS-1$
			_dataStore.memLog("DataElements GCed so far: " + numGCed); //$NON-NLS-1$
			System.gc();
		}
	}
	
	private void unmap(DataElement element)
	{	 
		HashMap map = _dataStore.getHashMap();					
		synchronized (map){
			map.remove(element.getId());
			_dataStore.addToRecycled(element);
		}
	}
	
	
	protected class QueueItem
	{
		public DataElement dataElement;
		public long timeStamp;
		
		public QueueItem(DataElement element, long stamp)
		{
			dataElement = element;
			timeStamp = stamp;
		}
	}
	
	/**
	 * Runs the handler loop in a thread.
	 */
	public void run()
	{
		while (_keepRunning)
		{
			try
			{
				Thread.sleep(100000); // wait 100 seconds
				Thread.yield();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
				finish();
				return;
			}
			handle();
		}
	}
}

