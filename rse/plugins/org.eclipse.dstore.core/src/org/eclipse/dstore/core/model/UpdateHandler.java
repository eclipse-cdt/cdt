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
 * David McKnight   (IBM)   [202822] should not be synchronizing on clean method
 * David McKnight   (IBM) - [226561] [apidoc] Add API markup to RSE Javadocs where extend / implement is allowed
 * David McKnight   (IBM) - [385793] [dstore] DataStore spirit mechanism and other memory improvements needed
 * David McKnight   (IBM) - [389286] [dstore] element delete should not clear _attributes since elements get recycled
 * David McKnight   (IBM) - [390037] [dstore] Duplicated items in the System view
 * David McKnight   (IBM) - [396440] [dstore] fix issues with the spiriting mechanism and other memory improvements (phase 1)
 *******************************************************************************/

package org.eclipse.dstore.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.dstore.core.java.IRemoteClassInstance;

/**
 * <p>
 * Abstract class for handling updates. A <code>UpdateHandler</code> is a
 * <code>Handler</code> that contains a queue of data responses to be sent to
 * the client. Each DataStore instance uses a single update handler that
 * periodically sends it's data queue either to a client or directly to a domain
 * listener on the client.
 * </p>
 * <p>
 * The UpdateHandler is the means by which the DataStore sends information or
 * files from the remote tools to the client.
 * </p>
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public abstract class UpdateHandler extends Handler
{


	protected ArrayList _dataObjects;
	protected ArrayList _classesToSend;

	/**
	 * Constructor
	 */
	public UpdateHandler()
	{
		 setName("DStore UpdateHandler"+getName()); //$NON-NLS-1$
		_dataObjects = new ArrayList();
		_classesToSend = new ArrayList();
	}

	/**
	 * Periodically called on the handler thread to sends data updates.
	 */
	public void handle()
	{
		if (!_dataObjects.isEmpty() || !_classesToSend.isEmpty())
		{
			sendUpdates();
		}
	}

	protected void clean(DataElement object)
	{
		clean(object, 2);
	}

	
	private void unmap(DataElement element)
	{	
		if (element.isDeleted() || element.isSpirit()){
			HashMap map = _dataStore.getHashMap();					
			synchronized (map){
				String id = element.getId();
			//	_dataStore.memLog("unmapping " + id);
				map.remove(id);
				_dataStore.addToRecycled(element);
			}
		}
	}
	
	protected void clean(DataElement object, int depth)
	{
		boolean isServer = !_dataStore.isVirtual();
		if ((depth > 0) && object != null){
			if (object.isSpirit() || object.isDeleted()){
				DataElement parent = object.getParent();
				cleanChildren(object);
				unmap(object);			
				if (object.isSpirit() && isServer){						
					// officially delete this now 
					object.delete();								
				}
				if (isServer){
					object.clear();
				}
				if (parent != null){
					synchronized (parent){
						parent.removeNestedData(object);
					}
				}
			}
			else if (object.getNestedSize() > 0){
				cleanChildren(object);
			}		
		}
		// remove objects under temproot
		DataElement tmpRoot = _dataStore.getTempRoot();
		if (tmpRoot != null){
			tmpRoot.removeNestedData();
		}
	}

	
	/*
	protected void clean(DataElement object, int depth)
	{
		boolean isServer = !_dataStore.isVirtual();
		if ((depth > 0) && object != null){
			List deletedList = _dataStore.findDeleted(object);
			for (int i = 0; i < deletedList.size(); i++){
				DataElement child = (DataElement) deletedList.get(i);
				if (child != null && child.isDeleted()){
					DataElement parent = child.getParent();

					cleanChildren(child); // clean the children
					unmap(child);
					if (child.isSpirit() && isServer){						
						// officially delete this now 
						child.delete();						
					}

					if (parent != null){
						synchronized (parent){
							parent.removeNestedData(child);
						}
					}
				}
			}

			deletedList.clear();
		}
	}
*/

	/**
	 * Recursively clean children for deletion
	 * @param parent
	 */
	protected void cleanChildren(DataElement parent)
	{
		boolean isServer = !_dataStore.isVirtual();
		List nestedData = parent.getNestedData();
		if (nestedData != null){
			synchronized (nestedData){
				for (int i = nestedData.size() - 1; i >= 0; i--){
					DataElement child = (DataElement)nestedData.get(i);
					if (child.isSpirit() || child.isDeleted()){
						cleanChildren(child);
						unmap(child);
					
						if (isServer){
							// officially delete this now
							child.delete();
							child.clear();							
						}
						nestedData.remove(child);
					}
				}
			}
		}
	}

	/**
	 * Adds a set of data objects to the update queue
	 * @param objects a set of objects to get updated
	 */
	public void update(ArrayList objects)
	{
		for (int i = 0; i < objects.size(); i++)
		{
			update((DataElement) objects.get(i));
		}
	}

	/**
	 * Adds an object to the update queue
	 * @param object an object to get updated
	 */
	public void update(DataElement object)
	{
		update(object, false);
	}

	/**
	 * Adds an object to the update queue
	 * @param object an object to get updated
	 * @param immediate true indicates that this object should be first in the queue
	 */
	public void update(DataElement object, boolean immediate){
		if (object != null){
			String type = object.getType();
			boolean statusDone = false;
			boolean isStatus = DataStoreResources.model_status.equals(type);				
			if (isStatus){
				statusDone = DataStoreResources.model_done.equals(object.getName()) || DataStoreResources.model_done.equals(object.getValue());
			}
			synchronized (_dataObjects){
				if (immediate){
					_dataObjects.add(0, object);
				}
				else {
					if (!_dataObjects.contains(object)){
						_dataObjects.add(object);
					}
					else {
						if (_dataStore != null && object != null && !object.isDeleted()){
							if (isStatus && statusDone){
								// move to the back of the queue
								// this is done so that if status that was already queued changed to done in between
								// requests, and had not yet been transferred over comm layer, the completed status
								// object does not come back to client (as "done") before the results of a query
								_dataObjects.remove(object);
								_dataObjects.add(object);											
							}
						}
					}
				}
			}
			if (_dataStore != null && !_dataStore.isVirtual() && isStatus && statusDone){
				_dataStore.disconnectObject(object.getParent()); // spirit the command and its children
			//	_dataStore.disconnectObject(object);
			}
		}
		notifyInput();
	}

	/**
	 * Causes the current thread to wait until this class request has been
	 * fulfilled.
	 */
	public synchronized void waitForInput()
	{
		if (_dataObjects.size() == 0 && _classesToSend.size() == 0)
		{
			super.waitForInput();
		}
	}

	/**
	 * Implemented to provide the means by which updates on the queue are sent.
	 */
	public abstract void sendUpdates();


	/**
	 * Implemented to provide the means by which files are sent
	 * @param path the path of the file to send
	 * @param bytes the bytes to send
	 * @param size the number of bytes to send
	 * @param binary indicates whether to send the bytes as binary or text
	 */
	public abstract void updateFile(String path, byte[] bytes, int size, boolean binary);

	/**
	 * Implemented to provide the means by which files are sent and appended
	 * @param path the path of the file to send
	 * @param bytes the bytes to send
	 * @param size the number of bytes to send
	 * @param binary indicates whether to send the bytes as binary or text
	 */
	public abstract void updateAppendFile(String path, byte[] bytes, int size, boolean binary);

	/**
	 * Implemented to provide the means by which files are sent
	 * @param path the path of the file to send
	 * @param bytes the bytes to send
	 * @param size the number of bytes to send
	 * @param binary indicates whether to send the bytes as binary or text
	 * @param byteStreamHandlerId indicates the byte stream handler to receive the bytes
	 */
	public abstract void updateFile(String path, byte[] bytes, int size, boolean binary, String byteStreamHandlerId);

	/**
	 * Implemented to provide the means by which files are sent and appended
	 * @param path the path of the file to send
	 * @param bytes the bytes to send
	 * @param size the number of bytes to send
	 * @param binary indicates whether to send the bytes as binary or text
	 * @param byteStreamHandlerId indicates the byte stream handler to receive the bytes
	 */
	public abstract void updateAppendFile(String path, byte[] bytes, int size, boolean binary, String byteStreamHandlerId);

	/**
	 * Implemented to provide the means by which classes are requested
	 * across the comm channel.
	 * @param className the name of the class to request
	 */
	public abstract void requestClass(String className);

	/**
	 * Implemented to provide the means by which keepalive requests are sent
	 * across the comm channel.
	 */
	public abstract void sendKeepAliveRequest();

	/**
	 * Impleted to provide the means by which a class on the host is updated on the client
	 * @param runnable
	 * @param deserializebyteStreamHandlerId
	 */
	public abstract void updateClassInstance(IRemoteClassInstance runnable, String deserializebyteStreamHandlerId);

	/**
	 * Implemented to provide the means by which classes are sent
	 * across the comm channel.
	 * @param className the name of the class to send
	 */
	public abstract void sendClass(String className);

	/**
	 * Implemented to provide the means by which classes are sent
	 * across the comm channel.
	 * @param className the name of the class to send
	 * @param classByteStreamHandlerId indicates which class byte stream handler to receive the class with
	 */
	public abstract void sendClass(String className, String classByteStreamHandlerId);

	/**
	 * Implemented to provide the means by which keepalive confirmations are sent
	 * across the comm channel.
	 */
	public abstract void sendKeepAliveConfirmation();

}
