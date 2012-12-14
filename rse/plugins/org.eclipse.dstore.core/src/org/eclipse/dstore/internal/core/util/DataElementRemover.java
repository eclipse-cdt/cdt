/*******************************************************************************
 * Copyright (c) 2002, 2012 IBM Corporation and others.
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
 *  David McKnight   (IBM) - [371401] [dstore][multithread] avoid use of static variables - causes memory leak after disconnect
 *  David McKnight   (IBM)  - [373507] [dstore][multithread] reduce heap memory on disconnect for server
 *  David McKnight   (IBM) - [385097] [dstore] DataStore spirit mechanism is not enabled
 *  David McKnight   (IBM) - [390037] [dstore] Duplicated items in the System view
 *  David McKnight   (IBM) - [396440] [dstore] fix issues with the spiriting mechanism and other memory improvements (phase 1)
 *******************************************************************************/

package org.eclipse.dstore.internal.core.util;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.dstore.core.model.DataStoreResources;
import org.eclipse.dstore.core.model.Handler;


public class DataElementRemover extends Handler 
{	
	protected class QueueItem
	{
		public DataElement dataElement;
		public long timeStamp;
		
		public QueueItem(DataElement element, long stamp)
		{
			dataElement = element;
			timeStamp = stamp;
		}
		
		public boolean equals(QueueItem item){
			return item.dataElement == dataElement;
		}
	}

	private ArrayList _queue;
	
	// The following determine how DataElements are chosen to be removed once they
	// are in the queue for removal. 	
	// The queue is checked every _intervalTime milliseconds and all elements
	// that are older than _expiryTime milliseconds are removed.
	public static final int DEFAULT_EXPIRY_TIME = 60; // in seconds
	public static final int DEFAULT_INTERVAL_TIME = 60; // in seconds
	private int _intervalTime = DEFAULT_INTERVAL_TIME * 1000;
	private int _expiryTime = DEFAULT_EXPIRY_TIME * 1000;
	public static final String EXPIRY_TIME_PROPERTY_NAME = "SPIRIT_EXPIRY_TIME"; //$NON-NLS-1$
	public static final String INTERVAL_TIME_PROPERTY_NAME = "SPIRIT_INTERVAL_TIME"; //$NON-NLS-1$
	public MemoryManager _memoryManager;
	
	private int _lastLive = 0;
	private int _lastFree = 0;
	private long _lastMem = 0;
	
	private boolean DEBUG = false; // extra tracing of hashmap when on
	private long _lastDumpTime = System.currentTimeMillis();
	
	public DataElementRemover(DataStore dataStore)
	{
		super();
		_memoryManager = new MemoryManager(dataStore);
		_dataStore = dataStore;
		_queue = new ArrayList();
		getTimes();
		setWaitTime(_intervalTime);
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
		}
		try
		{
			String intervalTime = System.getProperty(INTERVAL_TIME_PROPERTY_NAME);
			if (intervalTime != null && !intervalTime.equals("")) _intervalTime = Integer.parseInt(intervalTime) * 1000;			 //$NON-NLS-1$
		}
		catch (Exception e)
		{
			System.out.println("Invalid spirit interval time property, using default."); //$NON-NLS-1$
		}
	}
	

	
	public synchronized void addToQueueForRemoval(DataElement element){
		if(isMemoryThresholdExceeded()) {
			// do immediate clearing of queue since we're low on memory
			clearQueue(true);
			return;
		}
		if (_dataStore.isDoSpirit() &&
				!element.isReference() &&
				!element.isSpirit() &&
				!element.isDescriptor() && 
				!element.isDeleted()){
			//_dataStore.memLog("queuing " + element);
			QueueItem item = new QueueItem(element, System.currentTimeMillis());			
			if (!_queue.contains(item)){
				synchronized (_queue){
					_queue.add(item);				
				}
				notifyInput();
			}
		}
	}

	private boolean isMemoryThresholdExceeded(){
		return _memoryManager.isThresholdExceeded();
	}
	
	public void handle()
	{
		clearQueue(false);
	}
	
	private void logMemory(){
		long mem = Runtime.getRuntime().totalMemory();
		int liveElements = _dataStore.getNumElements();
		int freeElements = _dataStore.getNumRecycled();
		
		if (mem != _lastMem || liveElements != _lastLive || freeElements != _lastFree){
			_dataStore.memLog("                                        "); //$NON-NLS-1$
			_dataStore.memLog("Total heap size: " + mem); //$NON-NLS-1$
			_dataStore.memLog("Number of live DataStore elements: " + liveElements);  //$NON-NLS-1$
			_dataStore.memLog("Number of free DataStore elements: " + freeElements);  //$NON-NLS-1$
			
			_lastMem = mem;
			_lastLive = liveElements;
			_lastFree = freeElements;
		}
	}
	
	public synchronized void clearQueue(boolean force){
		if (!_dataStore.isDoSpirit()){ // spiriting disabled
			if (_queue.size() > 0) {
				_dataStore.memLog("Clearing queue of size " + _queue.size() + ". DSTORE_SPIRIT_ON not set or set to false."); //$NON-NLS-1$ //$NON-NLS-2$
				synchronized (_queue){
					_queue.clear();		
				}
			}				
			logMemory();				
			return;
		}
		else { // spiriting enabled
			if (_queue.size() > 0){
				int queueSize = _queue.size();

				ArrayList toRefresh = new ArrayList();
				long currentTime = System.currentTimeMillis();
				for (int i = queueSize - 1; i >= 0; i--){
					QueueItem qitem = null;
					synchronized (_queue){
						qitem = (QueueItem)_queue.get(i);
					}
					long deltaTime = currentTime - qitem.timeStamp;
					if (force || (deltaTime > _expiryTime)){
						DataElement toBeDisconnected = qitem.dataElement;
						toBeDisconnected.setSpirit(true);
						toBeDisconnected.setUpdated(false);
						DataElement parent = toBeDisconnected.getParent();
						if (!toRefresh.contains(parent)){
							toRefresh.add(parent);
						}
						synchronized (_queue){ // if spirited, dequeue
							_queue.remove(i);
						}
					}
				}
				if (!toRefresh.isEmpty()){
					// refresh parents of spirited items
					_dataStore.refresh(toRefresh);
					System.gc();
				}
				// print dump of elements on interval
				if (DEBUG){
					if (currentTime - _lastDumpTime > 100000){
						_lastDumpTime = currentTime;
						printHashmap();											
					}
				}
				
				logMemory();															
			}
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
				Thread.sleep(_intervalTime);
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
	
	// just used for tracing 
	private void printHashmap(){
		_dataStore.memLog("                                        "); //$NON-NLS-1$	
		_dataStore.memLog("------------------------------Current Hashmap--------------------------------:"); //$NON-NLS-1$	
		HashMap map = _dataStore.getHashMap();
		synchronized (map){
			DataElement[] elements = (DataElement[])map.values().toArray(new DataElement[map.size()]);
			for (int i = 0; i < elements.length; i++){
				DataElement element = elements[i];
				if (!element.isDescriptor()){
					String type = element.getType();
					if (type.equals(DataStoreResources.model_abstracted_by) || 
							type.equals(DataStoreResources.model_abstracts) ||
							type.equals("Environment Variable") ||
							type.equals("system.property")){
						// schema and environment stuff
					}
					else {
						if (type.equals(DataStoreResources.model_status)){
							String value = element.getValue();
							DataElement parent = element.getParent();
							_dataStore.memLog("Command: " + parent.getName() + " is " + value);							
						}		
						else {
							_dataStore.memLog(element.toString());
						}
					}
				}
			}
		}		
		_dataStore.memLog("-----------------------------------------------------------------------------:"); //$NON-NLS-1$	
	}
}

