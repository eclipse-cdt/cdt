package org.eclipse.dstore.core.util;

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
	private int _intervalTime = DEFAULT_INTERVAL_TIME * 1000;
	private int _expiryTime = DEFAULT_EXPIRY_TIME * 1000;
	public static final String EXPIRY_TIME_PROPERTY_NAME = "SPIRIT_EXPIRY_TIME";
	public static final String INTERVAL_TIME_PROPERTY_NAME = "SPIRIT_INTERVAL_TIME";
	
	public DataElementRemover(DataStore dataStore)
	{
		super();
		_dataStore = dataStore;
		_queue = new LinkedList();
		getTimes();
		setWaitTime(_intervalTime);
		DataElement spiritnode = _dataStore.createObjectDescriptor(_dataStore.getDescriptorRoot(), IDataStoreConstants.DATASTORE_SPIRIT_DESCRIPTOR);
		_dataStore.createCommandDescriptor(spiritnode, "StartSpirit", "DataElementRemover", IDataStoreConstants.C_START_SPIRIT);
		_dataStore.refresh(_dataStore.getDescriptorRoot());
	}
	
	protected void getTimes()
	{
		try
		{
			String expiryTime = System.getProperty(EXPIRY_TIME_PROPERTY_NAME);
			if (expiryTime != null && !expiryTime.equals("")) _expiryTime = Integer.parseInt(expiryTime) * 1000;			
		}
		catch (Exception e)
		{
			System.out.println("Invalid spirit expiry time property, using default.");
			_expiryTime = DEFAULT_EXPIRY_TIME;
		}
		try
		{
			String intervalTime = System.getProperty(INTERVAL_TIME_PROPERTY_NAME);
			if (intervalTime != null && !intervalTime.equals("")) _intervalTime = Integer.parseInt(intervalTime) * 1000;			
		}
		catch (Exception e)
		{
			System.out.println("Invalid spirit interval time property, using default.");
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
			if (_dataStore.isDoSpirit() && _dataStore == element.getDataStore())
			{
				QueueItem item = new QueueItem(element, System.currentTimeMillis());
				_queue.add(item);
			}
		}
	}
	
	public void handle()
	{
		clearQueue();
	}
	
	public synchronized void clearQueue()
	{
		synchronized (_queue)
		{
			_dataStore.memLog("           ");
			int disconnected = 0;
			if (!_dataStore.isDoSpirit())
			{
				if (_queue.size() > 0) 
				{
					_dataStore.memLog("Clearing queue of size " + _queue.size() + ". DSTORE_SPIRIT_ON not set or set to false.");
					_queue.clear();
				}
				_dataStore.memLog("Total heap size: " + Runtime.getRuntime().totalMemory());
				_dataStore.memLog("Elements created so far: " + numCreated);
				_dataStore.memLog("Elements disconnected so far: " + numDisconnected);
				_dataStore.memLog("Spirit elements cleaned so far: " + numRemoved);
				_dataStore.memLog("DataElements GCed so far: " + numGCed);
				return;
			}
			_dataStore.memLog("Total heap size before disconnection: " + Runtime.getRuntime().totalMemory());
			
			_dataStore.memLog("Size of queue: " + _queue.size());
			
			while (_queue.size() > 0 && System.currentTimeMillis() - ((QueueItem) _queue.getFirst()).timeStamp > _expiryTime)
			{
				DataElement toBeDisconnected = ((QueueItem) _queue.removeFirst()).dataElement;
				if (!toBeDisconnected.isSpirit()) 
				{
					toBeDisconnected.setSpirit(true);
					_dataStore.refresh(toBeDisconnected);
					disconnected++;
					numDisconnected++;
				}
				else
				{
					//_dataStore.memLog(toBeDisconnected.toString());
				}
				_dataStore.getHashMap().remove(toBeDisconnected.getId());
			}
			_dataStore.memLog("Disconnected " + disconnected + " DataElements.");
			_dataStore.memLog("Elements created so far: " + numCreated);
			_dataStore.memLog("Elements disconnected so far: " + numDisconnected);
			_dataStore.memLog("Spirit elements cleaned so far: " + numRemoved);
			_dataStore.memLog("DataElements GCed so far: " + numGCed);
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
				Thread.sleep(_waitIncrement);
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

