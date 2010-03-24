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
 * David McKnight  (IBM)   [220123][dstore] Configurable timeout on irresponsiveness
 * David McKnight  (IBM)   [222168][dstore] Buffer in DataElement is not sent
 * David McKnight  (IBM)   [246826][dstore] KeepAlive does not work correctly
 * David McKnight  (IBM)   [306853][dstore] RD/z client hang  after browse copy book command
 *******************************************************************************/

package org.eclipse.dstore.internal.core.client;
import org.eclipse.dstore.core.java.IRemoteClassInstance;
import org.eclipse.dstore.core.model.CommandHandler;
import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.dstore.core.model.DataStoreResources;
import org.eclipse.dstore.internal.core.util.Sender;



/**
 * The ClientCommandHandler is reponsible for maintaining
 * a queue of commands and periodically sending commands
 * from the queue to the server side.
 */
public class ClientCommandHandler extends CommandHandler
{

	private Sender _sender;
	protected DataElement _requestClassDocumentElement;
	protected DataElement _keepAliveDocumentElement;
	protected DataElement _confirmKeepAliveDocumentElement;
	protected DataElement _pendingKeepAliveRequest;
	protected DataElement _pendingKeepAliveConfirmation;

	private static String[] _docAttributes =  { 
		DataStoreResources.DOCUMENT_TYPE, 
		"client.doc.root.id", //$NON-NLS-1$
		"client.document", //$NON-NLS-1$
		"doc", //$NON-NLS-1$
		"", //$NON-NLS-1$
		"", //$NON-NLS-1$
		DataStoreResources.FALSE,
		"2"}; //$NON-NLS-1$
	
	private static String[] _fileAttributes =  {
		DataStoreResources.FILE_TYPE, 
		"client.file.root.id", //$NON-NLS-1$
		"client.file", //$NON-NLS-1$
		"doc", //$NON-NLS-1$
		"", //$NON-NLS-1$
		"", //$NON-NLS-1$
		DataStoreResources.FALSE,
		"2"}; //$NON-NLS-1$
	
	private static String[] _classAttributes =  {
		DataStoreResources.CLASS_TYPE, 
		"client.class.root.id", //$NON-NLS-1$
		"client.class", //$NON-NLS-1$
		"doc", //$NON-NLS-1$
		"", //$NON-NLS-1$
		"", //$NON-NLS-1$
		DataStoreResources.FALSE,
		"2"}; //$NON-NLS-1$
	
	private static String[] _serializeAttributes =  {
		DataStoreResources.SERIALIZED_TYPE, 
		"client.serialized.root.id", //$NON-NLS-1$
		"client.serialized", //$NON-NLS-1$
		"doc", //$NON-NLS-1$
		"", //$NON-NLS-1$
		"", //$NON-NLS-1$
		DataStoreResources.FALSE,
		"2"}; //$NON-NLS-1$
	
	private static String[] _requestClassAttributes =  {
		DataStoreResources.REQUEST_CLASS_TYPE, 
		"client.requestclass.root.id", //$NON-NLS-1$
		"client.requestclass", //$NON-NLS-1$
		"doc", //$NON-NLS-1$
		"", //$NON-NLS-1$
		"", //$NON-NLS-1$
		DataStoreResources.FALSE,
		"2"}; //$NON-NLS-1$
	
	private static String[] _keepAliveAttributes =  {
		DataStoreResources.KEEPALIVE_TYPE, 
		"client.keepalive.root.id", //$NON-NLS-1$
		"server.keepalive", //$NON-NLS-1$
		"doc", //$NON-NLS-1$
		"", //$NON-NLS-1$
		"", //$NON-NLS-1$
		DataStoreResources.FALSE,
		"2"}; //$NON-NLS-1$
		
	private static String[] _confirmKeepAliveAttributes =  {
		DataStoreResources.KEEPALIVECONFIRM_TYPE, 
		"client.keepalive.confirm.root.id", //$NON-NLS-1$
		"server.confirmkeepalive", //$NON-NLS-1$
		"doc", //$NON-NLS-1$
		"", //$NON-NLS-1$
		"", //$NON-NLS-1$
		DataStoreResources.FALSE,
		"2"}; //$NON-NLS-1$
		
	
	protected DataElement _fileDocumentElement;
	protected DataElement _docDocumentElement;
	protected DataElement _classDocumentElement;
	protected DataElement _serializedDocumentElement;
	
	/**
	 * Constructor
	 * @param sender the Sender
	 */
	public ClientCommandHandler(Sender sender)
	{
		super();
		_sender = sender;
	}	
	
	
	public void setDataStore(DataStore dataStore)
	{
		super.setDataStore(dataStore);
		_fileDocumentElement = dataStore.createTransientObject(_fileAttributes);
		_docDocumentElement = dataStore.createObject(null, _docAttributes);
		_classDocumentElement = dataStore.createTransientObject(_classAttributes);
		_serializedDocumentElement = dataStore.createTransientObject(_serializeAttributes);
		_requestClassDocumentElement = dataStore.createTransientObject(_requestClassAttributes);
		_keepAliveDocumentElement = dataStore.createTransientObject(_keepAliveAttributes);
		_confirmKeepAliveDocumentElement = dataStore.createTransientObject(_confirmKeepAliveAttributes);
	}

	/**
	 * Transmits the bytes of a file from the client to the server
	 * @param bytes the bytes of a file to send
	 * @param size the number of bytes to send
	 * @param binary indicates whether to send the bytes as binary or unicode
	 * @param byteStreamHandlerId indicates wwhich byte stream handler should receive the bytes
	 */
	public synchronized void sendFile(String fileName, byte[] bytes, int size, boolean binary, String byteStreamHandlerId)
	{
		// send pending commands before file
		if (_commands.size() > 0)
			sendCommands();
		
		//DataElement document = _dataStore.createObject(null, DataStoreResources.FILE_TYPE, byteStreamHandlerId, fileName, fileName);
		DataElement document = _fileDocumentElement;
		document.setAttribute(DE.A_NAME, byteStreamHandlerId);
		document.setAttribute(DE.A_VALUE, byteStreamHandlerId);
		document.setAttribute(DE.A_SOURCE, fileName);
		document.setPendingTransfer(true);
		document.setParent(null);
		_sender.sendFile(document, bytes, size, binary);
	}
	
	/**
	 * Transmits the bytes of a file from the client to the server
	 * @param bytes the bytes of a file to send
	 * @param size the number of bytes to send
	 * @param binary indicates whether to send the bytes as binary or unicode
	 */
	public synchronized void sendFile(String fileName, byte[] bytes, int size, boolean binary)
	{
		sendFile(fileName, bytes, size, binary, "default"); //$NON-NLS-1$
	}

	/**
	 * Appends bytes of a file from the client to the server
	 * @param bytes the bytes of a file to send
	 * @param size the number of bytes to send
	 * @param binary indicates whether to send the bytes as binary or unicode
	 * @param byteStreamHandlerId indicates which byte stream handler should receive the bytes
	 */
	public synchronized void sendAppendFile(String fileName, byte[] bytes, int size, boolean binary, String byteStreamHandlerId)
	{
		// send pending commands before file
		if (_commands.size() > 0)
			sendCommands();
		
		//DataElement document = _dataStore.createObject(null, DataStoreResources.FILE_TYPE, byteStreamHandlerId, fileName, fileName);

		DataElement document = _fileDocumentElement;
		document.setAttribute(DE.A_NAME, byteStreamHandlerId);
		document.setAttribute(DE.A_VALUE, byteStreamHandlerId);
		document.setAttribute(DE.A_SOURCE, fileName);
		document.setPendingTransfer(true);
		document.setParent(null);
		_sender.sendAppendFile(document, bytes, size, binary);
	}

	
	/**
	 * Appends bytes of a file from the client to the server
	 * @param bytes the bytes of a file to send
	 * @param size the number of bytes to send
	 * @param binary indicates whether to send the bytes as binary or unicode
	 */
	public synchronized void sendAppendFile(String fileName, byte[] bytes, int size, boolean binary)
	{
		sendAppendFile(fileName, bytes, size, binary, "default"); //$NON-NLS-1$
	}
	
	/**
	 * Called periodically to send the current queue of commands to the server
	 */
	public synchronized void sendCommands()
	{
		//DataElement commandRoot = _dataStore.createObject(null, DataStoreResources.DOCUMENT_TYPE, "client.doc"/*"client.doc." + _requests++*/);
		DataElement commandRoot = _docDocumentElement;
		commandRoot.removeNestedData();
		commandRoot.setPendingTransfer(true);
		commandRoot.setParent(null);
		while (_commands.size() > 0)
		{
			DataElement command = null;
			synchronized (_commands)
			{
				command = (DataElement)_commands.remove(0);
			}

			commandRoot.addNestedData(command, false);
		}
		
		_sender.sendDocument(commandRoot, 3);
		
		if (_pendingKeepAliveConfirmation != null)
		{
			_sender.sendKeepAliveConfirmation(_pendingKeepAliveConfirmation);
			_pendingKeepAliveConfirmation = null;
		}
		if (_pendingKeepAliveRequest != null)
		{
			_sender.sendKeepAliveRequest(_pendingKeepAliveRequest);
			_pendingKeepAliveRequest = null;
		}
		
		// finished sending commands, now send all classes that are waiting
		// in the queue
		while (_classesToSend != null && _classesToSend.size() > 0)
		{
			DataElement document = null;
			synchronized (_classesToSend)
			{
				document = (DataElement)_classesToSend.remove(0);
			}
			_sender.sendClass(document);
		}


	}
	
	public void handle() 
	{
		if (!_commands.isEmpty() || _pendingKeepAliveConfirmation != null || _pendingKeepAliveRequest != null || !_classesToSend.isEmpty())
		{
			sendCommands();
		}
	}
	
	/**
	 * Implemented to provide the means by which classes are sent
	 * across the comm channel.
	 * @param className the name of the class to send
	 * @param classbyteStreamHandlerId the name of the byte stream handler to use to receive the class
	 */
	public synchronized void sendClass(String className, String classbyteStreamHandlerId)
	{
		// send pending commands before sending class
		if (_commands.size() > 0)
			sendCommands();
				
		DataElement document = _classDocumentElement;
		document.setAttribute(DE.A_NAME, className);
		document.setAttribute(DE.A_SOURCE, classbyteStreamHandlerId);
		//document.setAttribute(DE.A_SOURCE, className);
		document.setPendingTransfer(true);
		document.setParent(null);
		
		addClassToSend(document);
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

	/**
	 * Adds a class to the queue of classes (represented by DataElements) to
	 * be sent to the server.
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
		notifyInput();
	}

 
	public synchronized void sendClassInstance(IRemoteClassInstance runnable, String deserializebyteStreamHandlerId) 
	{
		// send pending commands before sending class
		if (_commands.size() > 0)
			sendCommands();
				
		DataElement document = _serializedDocumentElement;
		document.setAttribute(DE.A_NAME, runnable.toString());
		document.setAttribute(DE.A_SOURCE, deserializebyteStreamHandlerId);
		document.setPendingTransfer(true);
		document.setParent(null);		

		
		_sender.sendRemoteClassRunnable(document, runnable);
	}

	/**
	 * Implemented to provide the means by which classes are requested
	 * across the comm channel.
	 * @param className the name of the class to request
	 */
	public void requestClass(String className) 
	{
		DataElement document = _requestClassDocumentElement;
		document.setPendingTransfer(true);
		document.setAttribute(DE.A_NAME, className);
		document.setAttribute(DE.A_VALUE, className);
		document.setParent(null);

		_sender.requestClass(document);		
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
	
	public synchronized void waitForInput() 
	{
		if (_commands.size() == 0 && _classesToSend.size() == 0 && _pendingKeepAliveConfirmation == null && _pendingKeepAliveRequest == null)
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
		_sender.setGenerateBuffer(flag);
	}
}
