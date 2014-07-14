/*******************************************************************************
 * Copyright (c) 2002, 2014 IBM Corporation and others.
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
 * David McKnight  (IBM)   [222168][dstore] Buffer in DataElement is not sent
 * David McKnight   (IBM) - [225507][api][breaking] RSE dstore API leaks non-API types
 * David McKnight  (IBM)   [246826][dstore] KeepAlive does not work correctly
 * David McKnight    (IBM)  - [358301] [DSTORE] Hang during debug source look up
 * David McKnight  (IBM)   [388873][dstore] ServerUpdateHandler _senders list should be synchronized
 * David McKnight  (IBM)   [404082][dstore] race condition on finish, removing senders
 * David McKnight  (IBM)   [439545][dstore] potential deadlock on senders during shutdown 
 *******************************************************************************/

package org.eclipse.dstore.internal.core.server;

import java.net.Socket;
import java.util.ArrayList;

import org.eclipse.dstore.core.java.IRemoteClassInstance;
import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.dstore.core.model.DataStoreResources;
import org.eclipse.dstore.core.model.UpdateHandler;
import org.eclipse.dstore.core.util.CommandGenerator;
import org.eclipse.dstore.internal.core.util.Sender;

/**
 * The ServerUpdateHandler is contains a queue of data update requests
 * and periodically transmits it's queue to the client
 */
public class ServerUpdateHandler extends UpdateHandler
{

	private Sender _primarySender; // there should really only be one

	private ArrayList _senders;
	private CommandGenerator _commandGenerator;
	protected DataElement _classDocumentElement;
	protected DataElement _keepAliveDocumentElement;
	protected DataElement _confirmKeepAliveDocumentElement;
	protected DataElement _pendingKeepAliveRequest;
	protected DataElement _pendingKeepAliveConfirmation;
	
	private static String[] _keepAliveAttributes =  {
			DataStoreResources.KEEPALIVE_TYPE, 
			"server.keepalive.root.id", //$NON-NLS-1$
			"server.keepalive", //$NON-NLS-1$
			"doc", //$NON-NLS-1$
			"", //$NON-NLS-1$
			"", //$NON-NLS-1$
			DataStoreResources.FALSE,
			"2"}; //$NON-NLS-1$
	
	private static String[] _confirmKeepAliveAttributes =  {
		DataStoreResources.KEEPALIVECONFIRM_TYPE, 
		"server.keepalive.confirm.root.id", //$NON-NLS-1$
		"server.confirmkeepalive", //$NON-NLS-1$
		"doc", //$NON-NLS-1$
		"", //$NON-NLS-1$
		"", //$NON-NLS-1$
		DataStoreResources.FALSE,
		"2"}; //$NON-NLS-1$
	
	private static String[] _docAttributes =  { 
		DataStoreResources.DOCUMENT_TYPE, 
		"server.doc.root.id", //$NON-NLS-1$
		"server.document", //$NON-NLS-1$
		"doc", //$NON-NLS-1$
		"", //$NON-NLS-1$
		"", //$NON-NLS-1$
		DataStoreResources.FALSE,
		"2"}; //$NON-NLS-1$
	
	private static String[] _fileAttributes =  {
		DataStoreResources.FILE_TYPE, 
		"server.file.root.id", //$NON-NLS-1$
		"server.file", //$NON-NLS-1$
		"doc", //$NON-NLS-1$
		"", //$NON-NLS-1$
		"", //$NON-NLS-1$
		DataStoreResources.FALSE,
		"2"}; //$NON-NLS-1$
	
	private static String[] _classAttributes =  {
		DataStoreResources.CLASS_TYPE, 
		"server.class.root.id", //$NON-NLS-1$
		"server.class", //$NON-NLS-1$
		"doc", //$NON-NLS-1$
		"", //$NON-NLS-1$
		"", //$NON-NLS-1$
		DataStoreResources.FALSE,
		"2"}; //$NON-NLS-1$
	
	private static String[] _requestClassAttributes =  {
		DataStoreResources.REQUEST_CLASS_TYPE, 
		"server.requestclass.root.id", //$NON-NLS-1$
		"server.requestclass", //$NON-NLS-1$
		"doc", //$NON-NLS-1$
		"", //$NON-NLS-1$
		"", //$NON-NLS-1$
		DataStoreResources.FALSE,
		"2"}; //$NON-NLS-1$
	
	private static String[] _serializeAttributes =  {
		DataStoreResources.SERIALIZED_TYPE, 
		"server.serialized.root.id", //$NON-NLS-1$
		"server.serialized", //$NON-NLS-1$
		"doc", //$NON-NLS-1$
		"", //$NON-NLS-1$
		"", //$NON-NLS-1$
		DataStoreResources.FALSE,
		"2"}; //$NON-NLS-1$
	
	protected DataElement _fileDocumentElement;
	protected DataElement _docDocumentElement;
	protected DataElement _requestClassDocumentElement;
	protected DataElement _serializedDocumentElement;

	/**
	 * Constructor
	 */
	public ServerUpdateHandler()
	{
		_senders = new ArrayList();
		_commandGenerator = new CommandGenerator();

	}

	/**
	 * Sets the associated DataStore
	 */
	public void setDataStore(DataStore dataStore)
	{
		super.setDataStore(dataStore);
		_commandGenerator.setDataStore(dataStore);
		_fileDocumentElement = dataStore.createTransientObject(_fileAttributes);
		_docDocumentElement = dataStore.createObject(null, _docAttributes);
		_requestClassDocumentElement = dataStore.createTransientObject(_requestClassAttributes);
		_serializedDocumentElement = dataStore.createTransientObject(_serializeAttributes);
		_classDocumentElement = dataStore.createTransientObject(_classAttributes);
		_keepAliveDocumentElement = dataStore.createTransientObject(_keepAliveAttributes);
		_confirmKeepAliveDocumentElement = dataStore.createTransientObject(_confirmKeepAliveAttributes);

	}

	/**
	 * Add a sender to the list of senders.  Normally there is only one
	 * client for the server, which requires one <code>Sender</code>.  If
	 * there are more than one clients, then this is how senders are added.
	 * 
	 * @param sender a sender connected to a socket
	 */
	public void addSender(Sender sender)
	{
		synchronized(_senders){
			_senders.add(sender);
		}
		_primarySender = sender;
	}

	/**
	 * Remove a sender from the list of senders.
	 * @param sender the sender to remove
	 */
	public void removeSender(Sender sender)
	{
		synchronized (_senders){
			_senders.remove(sender);
		}
		if (sender == _primarySender){
			_primarySender = null;
		}
		if (_senders.size() == 0)
		{
			finish();
		}
	}

	/**
	 * Sends bytes to the specified file on the client.
	 * 
	 * @param path the name of the file on the client
	 * @param bytes the bytes to send
	 * @param size the number of bytes to send
	 * @param binary indicates whether to send the bytes and binary or text
	 */
	public synchronized void updateFile(String path, byte[] bytes, int size, boolean binary)
	{
		updateFile(path, bytes, size, binary, DataStoreResources.DEFAULT_BYTESTREAMHANDLER);
	}
	
	/**
	 * Sends bytes to the specified file on the client.
	 * 
	 * @param path the name of the file on the client
	 * @param bytes the bytes to send
	 * @param size the number of bytes to send
	 * @param binary indicates whether to send the bytes and binary or text
	 * @param byteStreamHandlerId indicates the byte stream handler to receive the bytes
	 * 
	 */
	public synchronized void updateFile(String path, byte[] bytes, int size, boolean binary, String byteStreamHandlerId)
	{
		//DataElement document = _dataStore.createObject(null, DataStoreResources.FILE_TYPE, byteStreamHandlerId, path, path);
		DataElement document = _fileDocumentElement;
		document.setAttribute(DE.A_NAME, byteStreamHandlerId);
		document.setAttribute(DE.A_VALUE, byteStreamHandlerId);
		document.setAttribute(DE.A_SOURCE, path);
		document.setPendingTransfer(true);
		document.setParent(null);
		
		_primarySender.sendFile(document, bytes, size, binary);
	}

	/**
	 * Appends bytes to the specified file on the client.
	 * 
	 * @param path the name of the file on the client
	 * @param bytes the bytes to send
	 * @param size the number of bytes to send
	 * @param binary indicates whether to send the bytes and binary or text
	 */
	public synchronized void updateAppendFile(String path, byte[] bytes, int size, boolean binary)
	{
		updateAppendFile(path, bytes, size, binary, DataStoreResources.DEFAULT_BYTESTREAMHANDLER);
	}
	
/**
	 * Appends bytes to the specified file on the client.
	 * 
	 * @param path the name of the file on the client
	 * @param bytes the bytes to send
	 * @param size the number of bytes to send
	 * @param binary indicates whether to send the bytes and binary or text
	 * @param byteStreamHandlerId indicates the byte stream handler to receive the bytes
	 */
	public synchronized void updateAppendFile(String path, byte[] bytes, int size, boolean binary, String byteStreamHandlerId)
	{
		//DataElement document = _dataStore.createObject(null, DataStoreResources.FILE_TYPE, byteStreamHandlerId, path, path);
		DataElement document = _fileDocumentElement;
		document.setAttribute(DE.A_NAME, byteStreamHandlerId);
		document.setAttribute(DE.A_VALUE, byteStreamHandlerId);
		document.setAttribute(DE.A_SOURCE, path);
		document.setPendingTransfer(true);
		document.setParent(null);
		
		_primarySender.sendAppendFile(document, bytes, size, binary);
	}
	

	/**
	 * Periodically called on the handler thread to sends data updates.
	 */
	public void handle()
	{
		if (!_dataObjects.isEmpty() || _pendingKeepAliveConfirmation != null || _pendingKeepAliveRequest != null || !_classesToSend.isEmpty())
		{
			try {
				sendUpdates();
			}
			catch (OutOfMemoryError e){
				System.exit(-1);
			}
		}
	}

	/**
	 * Periodically called to send data in the queue from the server to the client
     */
	public void sendUpdates()
	{
		synchronized (_dataObjects)
		{
			//DataElement document = _dataStore.createObject(null, DataStoreResources.DOCUMENT_TYPE, "server.doc");
			DataElement document = _docDocumentElement;
			document.removeNestedData();
			document.setPendingTransfer(true);
			document.setUpdated(true);
			document.setParent(null);
			
			_commandGenerator.generateResponse(document, _dataObjects);

			_primarySender.sendDocument(document, 20);
			if (_pendingKeepAliveConfirmation != null)
			{
				_primarySender.sendKeepAliveConfirmation(_pendingKeepAliveConfirmation);
				_pendingKeepAliveConfirmation = null;
			}
			if (_pendingKeepAliveRequest != null)
			{
				_primarySender.sendKeepAliveRequest(_pendingKeepAliveRequest);
				_pendingKeepAliveRequest = null;
			}


			for (int i = 0; i < _dataObjects.size(); i++)
			{
				DataElement obj = (DataElement) _dataObjects.get(i);
				clean(obj);
			}

			_dataObjects.clear();
			//_dataStore.getLogRoot().removeNestedData();
			//_dataStore.getTempRoot().removeNestedData();
		}
		
		// finished sending updates, now send all classes that are waiting
		// in the queue
		while (_classesToSend.size() > 0)
		{
			DataElement document = null;
			synchronized (_classesToSend)
			{
				document = (DataElement)_classesToSend.remove(0);
				synchronized (_senders){
				for (int i = 0; i < _senders.size(); i++)
				{
					Sender sender = (Sender) _senders.get(i);
					sender.sendClass(document);
				}
				}
			}
		}

	}

	/**
	 * Removes the sender that is associated with the specified socket.  This causes
	 * A disconnect for the client that is associated with this socket.
	 * 
	 * @param socket the socket on which a sender communicates
	 */
	public void removeSenderWith(Socket socket)
	{
		for (int i = 0; i < _senders.size(); i++)
		{
			Sender sender = (Sender) _senders.get(i);
			if (sender.socket() == socket)
			{
				// sender sends last ack before death
				DataElement document = _dataStore.createObject(null, DataStoreResources.DOCUMENT_TYPE, "exit", "exit"); //$NON-NLS-1$ //$NON-NLS-2$
				sender.sendDocument(document, 2);
				removeSender(sender);
			}
		}	
		if (_primarySender != null && _primarySender.socket() == socket){
			_primarySender = null;
		}
	}
	
	/**
	 * Implemented to provide the means by which classes are sent
	 * across the comm channel.
	 * @param className the name of the class to request
	 */
	public synchronized void requestClass(String className)
	{
		DataElement document = _requestClassDocumentElement;
		document.setPendingTransfer(true);
		document.setAttribute(DE.A_NAME, className);
		document.setAttribute(DE.A_VALUE, className);
		document.setParent(null);
		//DataElement document = _dataStore.createObject(null, DataStoreResources.REQUEST_CLASS_TYPE, className);

		_primarySender.requestClass(document);	
	}
	

	public synchronized void updateClassInstance(IRemoteClassInstance runnable, String deserializebyteStreamHandlerId) 
	{
		DataElement document = _serializedDocumentElement;
		document.setAttribute(DE.A_NAME, runnable.toString());
		document.setAttribute(DE.A_SOURCE, deserializebyteStreamHandlerId);
		document.setPendingTransfer(true);
		document.setParent(null);		

		_primarySender.sendRemoteClassRunnable(document, runnable);	

		notifyInput();
	}

	/**
	 * Implemented to provide the means by which classes are sent
	 * across the comm channel.
	 * @param className the name of the class to send
	 * @param classByteStreamHandlerId the name of the byte stream handler to use to receive the class
	 */
	public synchronized void sendClass(String className, String classByteStreamHandlerId) 
	{
		// send pending updates before sending class
		if (_dataObjects.size() > 0)
			sendUpdates();
				
		DataElement document = _classDocumentElement;
		document.setAttribute(DE.A_NAME, className);
		document.setAttribute(DE.A_SOURCE, classByteStreamHandlerId);
		document.setPendingTransfer(true);
		document.setParent(null);
		
		addClassToSend(document);
	}
	
	/**
	 * Adds a class to the queue of classes (represented by DataElements) to
	 * be sent to the client.
	 * @param classElement the DataElement representing the class to be sent
	 */
	public void addClassToSend(DataElement classElement)
	{
		synchronized (_classesToSend)
		{
			if (!_classesToSend.contains(classElement))
			{
				_classesToSend.add(classElement);
			}
		}
	}

	/**
	 * Implemented to provide the means by which classes are requested and sent
	 * across the comm channel.
	 * @param className the name of the class to send
	 */
	public synchronized void sendClass(String className) 
	{
		sendClass(className, "default"); //$NON-NLS-1$
	}

	public void sendKeepAliveRequest() 
	{
		DataElement document = _keepAliveDocumentElement;
		document.setPendingTransfer(true);
		document.setAttribute(DE.A_NAME, "request"); //$NON-NLS-1$
		document.setAttribute(DE.A_VALUE, "request"); //$NON-NLS-1$
		document.setParent(null);
		_pendingKeepAliveRequest = document;
		
		handle(); // bypassing threading
	}

	public void sendKeepAliveConfirmation() 
	{
		DataElement document = _confirmKeepAliveDocumentElement;
		document.setPendingTransfer(true);
		document.setAttribute(DE.A_NAME, "confirm"); //$NON-NLS-1$
		document.setAttribute(DE.A_VALUE, "confirm"); //$NON-NLS-1$
		document.setParent(null);
		_pendingKeepAliveConfirmation = document;
				
		handle(); // bypassing threading
	}

	public synchronized void waitForInput()
	{
		if (_dataObjects.size() == 0 && _classesToSend.size() == 0 && _pendingKeepAliveConfirmation == null && _pendingKeepAliveRequest == null)
		{
			super.waitForInput();
		}
	}
	
	/**
	 * Indicates whether the xml generator should transfer the buffer attribute of a DataElement
	 * @param flag true to transfer the buffer attribute
	 */
	public void setGenerateBuffer(boolean flag)
	{
		_primarySender.setGenerateBuffer(flag);
	}
}
