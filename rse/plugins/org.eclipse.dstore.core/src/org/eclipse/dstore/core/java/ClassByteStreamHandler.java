/********************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation. All rights reserved.
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

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;


/**
 * <p>
 * The ClassByteStreamHandler class is used to abstract classfile read and write operations
 * across the network.  By default this is used for sending and receiving class files
 * on the client and the server.  The class can be extended if the default byte stream
 * implementations are not sufficient for a particular platform or use.  
 * </p>
 * <p>
 * If ClassByteStreamHandler is extended, you need to tell the DataStore to use the
 * extended implementation.  To do that, call <code>DataStore.setClassByteStreamHandler(ClassByteStreamHandler)</code>.
 * </p>
 * 
 */
public class ClassByteStreamHandler implements IClassByteStreamHandler
{

	protected DataStore _dataStore;
	protected DataElement _log;
	protected static final String FILEMSG_REMOTE_SAVE_FAILED = "RSEF5006";

	/**
	 * Contructor
	 * @param dataStore the DataStore instance
	 * @param the log in which to log status and messages
	 */
	public ClassByteStreamHandler(DataStore dataStore, DataElement log)
	{
		_dataStore = dataStore;
		_log = log;
	}
	
	public String getIdentifier()
	{
		return getClass().getName();
	}
	
	/**
	 * Receive a class and load it.  This method is called by the
	 * DataStore when the communication layer receives a class file transfer
	 * This method kicks off a new thread so that the receiver thread can be free
	 * to receive other data.
	 *
	 * @param className the name of the class to receive
	 * @param buffer the bytes that comprise the class
	 * @param size the number of bytes in the class
	 */
	public void receiveBytes(String className, byte[] buffer, int size)
	{
		ReceiveClassThread rct = new ReceiveClassThread(className, buffer, size);
		rct.start();
	}
	
	
	/**
	 * Receive a class instance and load it.  This method is called by the
	 * DataStore when the communication layer receives a class file transfer
	 * This method kicks off a new thread so that the receiver thread can be free
	 * to receive other data.
	 *
	 * @param buffer the bytes that comprise the class instance
	 * @param size the number of bytes in the class instance
	 */
	public void receiveInstanceBytes(byte[] buffer, int size)
	{
		ReceiveClassInstanceThread rct = new ReceiveClassInstanceThread(buffer, size);
		rct.start();
	}
	
	protected DataElement findStatusFor(String remotePath)
	{
		if (_log != null)
		{
			for (int i = 0; i < _log.getNestedSize(); i++)
			{
				DataElement child = _log.get(i);
				if (child.getName().equals(remotePath))
				{
					return child;
				}
			}
		}
		return null;
	}
	
	/**
	 * A new thread that can be spawned to receive the class
	 *
	 */
	protected class ReceiveClassThread extends Thread
	{
		private String _className;
		private byte[] _buffer;
		private int _size;
		
		public ReceiveClassThread(String className, byte[] buffer, int size)
		{
			_className = className;
			_buffer = buffer;
			_size = size;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		public void run()
		{
			RemoteClassLoader remoteLoader = new RemoteClassLoader(_dataStore);
			remoteLoader.receiveClass(_className, _buffer, _size);
		}
		
	}
	
	/**
	 * A new thread that can be spawned to receive the class
	 *
	 */
	protected class ReceiveClassInstanceThread extends Thread
	{
		private byte[] _buffer;
		private int _size;
		
		public ReceiveClassInstanceThread(byte[] buffer, int size)
		{
			_buffer = buffer;
			_size = size;	
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		public void run()
		{
			try
			{
				PipedInputStream ins = new PipedInputStream();
				
				PipedOutputStream outStream = new PipedOutputStream(ins);
				outStream.write(_buffer, 0, _size);
				outStream.flush();
				outStream.close();				

				IRemoteClassInstance instance = loadInstance(ins);
				runInstance(instance);
				
			}
			catch (Exception e)
			{			
				e.printStackTrace();
			}
		}
		
		protected IRemoteClassInstance loadInstance(InputStream ins)
		{
			ObjectInputStream inStream = null;
			try
			{
				inStream = new RemoteObjectInputStream(ins, _dataStore.getRemoteClassLoader());		
			
				return (IRemoteClassInstance)inStream.readObject();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return null;
		}
	
		protected void runInstance(IRemoteClassInstance instance) 
		{
			
			if (_dataStore.isVirtual())
			{
				// on client notify
				instance.updatedOnClient();
			}
			else
			{
				// on server run and update client
				instance.arrivedOnServer();			
				_dataStore.updateRemoteClassInstance(instance, getIdentifier());
			}
		}
	}
}