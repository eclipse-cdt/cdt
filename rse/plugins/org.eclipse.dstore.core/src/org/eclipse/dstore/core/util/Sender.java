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

package org.eclipse.dstore.core.util;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;

import org.eclipse.dstore.core.java.IRemoteClassInstance;
import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;

/**
 * This class is used for sending data to a socket in the DataStore 
 * communication layer.
 */
public class Sender implements ISender
{

	private Socket _socket;
	private PrintStream _outFile;
	private BufferedWriter _outData;
	private XMLgenerator _xmlGenerator;
	private DataStore _dataStore;
	
	/**
	 * Constructor
	 * @param socket the associated socket
	 * @param dataStore the associated DataStore
	 */
	public Sender(Socket socket, DataStore dataStore)
	{
		_socket = socket;
		_dataStore = dataStore;

		_xmlGenerator = new XMLgenerator(_dataStore);
		try
		{
			int bufferSize = _socket.getSendBufferSize();

			_socket.setSendBufferSize(bufferSize);
			_xmlGenerator.setBufferSize(bufferSize);
		}
		catch (SocketException e)
		{
		}
		try
		{
			_outFile = new PrintStream(_socket.getOutputStream());
			_outData = new BufferedWriter(new OutputStreamWriter(_socket.getOutputStream(), DE.ENCODING_UTF_8));

			_xmlGenerator.setFileWriter(_outFile);
			_xmlGenerator.setDataWriter(_outData);
			_xmlGenerator.setGenerateBuffer(false);
			InetSocketAddress address = (InetSocketAddress)socket.getRemoteSocketAddress();
			if (address != null)
			{
				if (address.getAddress() != null)
				{
					String remoteIP = address.getAddress().getHostAddress();
					_dataStore.setRemoteIP(remoteIP);
				}
				else
				{
					String remoteIP = address.getHostName();
					_dataStore.setRemoteIP(remoteIP);
				}
			}
			else
			{
				String remoteIP = socket.getInetAddress().getHostAddress();
				_dataStore.setRemoteIP(remoteIP);
			}
		}
		catch (java.io.IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Returns the associated socket
	 * @return the socket
	 */
	public Socket socket()
	{
		return _socket;
	}

	/**
	 * Sends a string through the socket
	 * @param document the string to send
	 */
	public void sendDocument(String document)
	{
		synchronized (_outData)
		{
			try
			{
				_outData.write(document, 0, document.length());
				_outData.flush();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Sends the bytes of a file through the socket
	 * @param objectRoot the object representing the file to send
	 * @param bytes the bytes to send over the socket
	 * @param size the number of bytes to send over the socket
	 * @param binary indicates whether to send the bytes and binary or text
	 */
	public void sendFile(DataElement objectRoot, byte[] bytes, int size, boolean binary)
	{

		synchronized (_outData)
		{
			synchronized (_outFile)
			{

				_xmlGenerator.empty();
				_xmlGenerator.generate(objectRoot, bytes, size, false, binary);
				_xmlGenerator.flushData();
			}
		}
	}
	
	/**
	 * Sends a class through the socket
	 * @param classElement the object representing the class to send
	 */
	public void sendClass(DataElement classElement)
	{
		String className = classElement.getName();

		ArrayList loaders = _dataStore.getLocalClassLoaders();
		if (loaders == null) 
		{
			// could not get the registered classLoaders. Fail.
			generateEmptyClass(classElement);
			return;
		}
		
		InputStream classInStream = null;
		className = className.replace('.', '/');
		className = className + ".class";
		URL classLocation = null;
		for (int i = 0; i < loaders.size(); i++)
		{
			ClassLoader loader = (ClassLoader) loaders.get(i);
			
			classInStream = loader.getResourceAsStream(className);
			classLocation = loader.getResource(className);
			if (classInStream != null && classLocation != null) break;
		}
		if (classLocation == null || classInStream == null) 
		{
			// could not load the class with any of the class loaders. Fail.
			generateEmptyClass(classElement);
			return;
		}
		
		// got a stream to read the classfile. Now read the class into a buffer.
		BufferedInputStream bufInputStream = new BufferedInputStream(classInStream);
		if (bufInputStream == null) 
		{
			generateEmptyClass(classElement);
			return; // throw new IOException("BufferedInputStream could not be instantiated on " + className);
		}
		try
		{
			int classSize = bufInputStream.available();
			byte[] bytes = new byte[classSize];
			int result = 0;
			result = bufInputStream.read(bytes);
			
			if (result != classSize)
			{
				generateEmptyClass(classElement);
				return; // throw new IOException("Could not read class from BufferedInputStream: " + className);
			}
		
			synchronized (_outData)
			{
				synchronized (_outFile)
				{

					_xmlGenerator.empty();
					_xmlGenerator.generate(classElement, bytes, classSize);
					_xmlGenerator.flushData();
				}
			}
		}
		catch (IOException e)
		{
			generateEmptyClass(classElement);
			return;
		}
	}
	
	/**
	 * Generates an empty class and sends it across the pipe, as a signal that the
	 * class could not be found or loaded or read on the client.
	 * @param classElement
	 */
	private void generateEmptyClass(DataElement classElement)
	{
		_xmlGenerator.empty();
		_xmlGenerator.generate(classElement, new byte[0], 0);
		_xmlGenerator.flushData();
	}

	/**
	 * Sends the bytes of a file through the socket to be appended to a file on the other end
	 * 
	 * @param objectRoot the object representing the file to send
	 * @param bytes the bytes to send over the socket
	 * @param size the number of bytes to send over the socket
	 * @param binary indicates whether to send the bytes and binary or text
	 */
	public void sendAppendFile(DataElement objectRoot, byte[] bytes, int size, boolean binary)
	{
		synchronized (_outData)
		{

			synchronized (_outFile)
			{
				_xmlGenerator.empty();
				_xmlGenerator.generate(objectRoot, bytes, size, true, binary);
				_xmlGenerator.flushData();
			}
		}
	}

	/**
	 * Sends a DataStore tree of data through the socket
	 * 
	 * @param objectRoot the root of the tree to send
	 * @param depth the depth of the tree to send
	 */
	public void sendDocument(DataElement objectRoot, int depth)
	{
		synchronized (_outData)
		{
			synchronized (_outFile)
			{

				_xmlGenerator.empty();
				_xmlGenerator.generate(objectRoot, depth);
				_xmlGenerator.flushData();
			}

		}

//		if (objectRoot.getParent() != null)
	//		objectRoot.getDataStore().deleteObject(objectRoot.getParent(), objectRoot);
	}
	
	/**
	 * Requests a class from the client
	 */
	public void requestClass(DataElement classRequest)
	{
		synchronized (_outData)
		{
			synchronized (_outFile)
			{

				_xmlGenerator.empty();
				_xmlGenerator.generateClassRequest(classRequest);
				_xmlGenerator.flushData();
			}
		}
	}

	public void sendRemoteClassRunnable(DataElement objectRoot, IRemoteClassInstance runnable) 
	{
		synchronized (_outData)
		{
			synchronized (_outFile)
			{
				_xmlGenerator.empty();
				_xmlGenerator.generateSerializedObject(objectRoot, runnable);
				_xmlGenerator.flushData();
			}
		}
	}

	public void sendKeepAliveRequest(DataElement document) 
	{
		synchronized (_outData)
		{
			synchronized (_outFile)
			{
				_xmlGenerator.empty();
				_xmlGenerator.generate(document, 2);
				_xmlGenerator.flushData();
			}
		}
	}

	public void sendKeepAliveConfirmation(DataElement document) 
	{
		synchronized (_outData)
		{
			synchronized (_outFile)
			{
				_xmlGenerator.empty();
				_xmlGenerator.generate(document, 2);
				_xmlGenerator.flushData();
			}
		}
	}
}