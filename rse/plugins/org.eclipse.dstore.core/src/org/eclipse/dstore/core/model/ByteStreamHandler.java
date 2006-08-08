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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * <p>
 * The ByteStreamHandler class is used to abstract file read and write operations
 * across the network.  By default this is used for sending and receiving files
 * on the client and the server.  The class can be extended if the default byte stream
 * implementations are not sufficient for a particular platform or use.  
 * </p>
 * <p>
 * If ByteStreamHandler is extended, you need to tell the DataStore to use the
 * extended implementation.  To do that, call <code>DataStore.setByteStreamHandler(ByteStreamHandler)</code>.
 * </p>
 * 
 */
public class ByteStreamHandler implements IByteStreamHandler
{

	protected DataStore _dataStore;
	protected DataElement _log;
	protected static final String FILEMSG_REMOTE_SAVE_FAILED = "RSEF5006";

	/**
	 * Contructor
	 * @param dataStore the DataStore instance
	 */
	public ByteStreamHandler(DataStore dataStore, DataElement log)
	{
		_dataStore = dataStore;
		_log = log;
	}

	public String getId()
	{
		return getClass().getName();
	}
	
	/**
	 * Save a file in the specified location.  This method is called by the
	 * DataStore when the communication layer receives a file transfer    
	 *
	 * @param remotePath the path where to save the file
	 * @param buffer the bytes to insert in the file
	 * @param size the number of bytes to insert
	 * @param binary indicates whether to save the bytes as binary or text
	 */
	public void receiveBytes(String remotePath, byte[] buffer, int size, boolean binary)
	{
		remotePath = new String(remotePath.replace('\\', '/'));
		DataElement status = findStatusFor(remotePath);
		String fileName = _dataStore.mapToLocalPath(remotePath);
	
		if (fileName != null)
		{
			try
			{
				// need to create directories as well
				File file = new File(fileName);
				if (!file.exists())
				{
					File parent = new File(file.getParent());
					parent.mkdirs();
				}
				else
				{
				}

				File newFile = new File(fileName);
				FileOutputStream fileStream = new FileOutputStream(newFile);

				if (binary)
				{
					fileStream.write(buffer, 0, size);
				}
				else
				{
					IByteConverter byteConverter = _dataStore.getByteConverter();
					byteConverter.setContext(file);
					byte[] convertedBytes = byteConverter.convertClientBytesToHostBytes(buffer, 0, size);
					fileStream.write(convertedBytes, 0, convertedBytes.length);
					
					/*
					String bufferString = new String(buffer, 0, size, DE.ENCODING_UTF_8);

					// hack for zOS
					String theOS = System.getProperty("os.name");
					if (theOS.toLowerCase().startsWith("z"))
					{
						bufferString = bufferString.replace('\r', ' ');
					}

					OutputStreamWriter writer = new OutputStreamWriter(fileStream);
					writer.write(bufferString, 0, size);
					writer.flush();
					*/
				}

				fileStream.close();
				if (status == null)
					return;
				status.setAttribute(DE.A_SOURCE, "success");
				_dataStore.refresh(status.getParent());
			}
			catch (IOException e)
			{
				System.out.println(e);
				if (status == null)
					return;
				status.setAttribute(DE.A_VALUE, FILEMSG_REMOTE_SAVE_FAILED);
				status.setAttribute(DE.A_SOURCE, "failed");
				_dataStore.refresh(status.getParent());
			}
			catch (Exception e)
			{
				System.out.println(e);
				if (status == null)
					return;
				status.setAttribute(DE.A_VALUE, FILEMSG_REMOTE_SAVE_FAILED);
				status.setAttribute(DE.A_SOURCE, "failed");
				_dataStore.refresh(status.getParent());
			}
		}
	}

	/**
	 * Append a bytes to a file at a specified location. This method is called by the
	 * DataStore when the communication layer receives a file transfer append.      
	 *
	 * @param remotePath the path where to save the file
	 * @param buffer the bytes to append in the file
	 * @param size the number of bytes to append in the file
	 * @param binary indicates whether to save the bytes as binary or text
	 */
	public void receiveAppendedBytes(String remotePath, byte[] buffer, int size, boolean binary)
	{
		remotePath = new String(remotePath.replace('\\', '/'));
		DataElement status = findStatusFor(remotePath);
		String fileName = _dataStore.mapToLocalPath(remotePath);

		if (fileName != null)
		{
			try
			{
				// need to create directories as well
				File file = new File(fileName);
				if (!file.exists())
				{
					File parent = new File(file.getParent());
					parent.mkdirs();

					File newFile = new File(fileName);
					FileOutputStream fileStream = new FileOutputStream(newFile);

					if (binary)
					{
						fileStream.write(buffer, 0, size);
					}
					else
					{
						IByteConverter byteConverter = _dataStore.getByteConverter();
						byteConverter.setContext(file);
					
						byte[] convertedBytes = byteConverter.convertClientBytesToHostBytes(buffer, 0, size);
						fileStream.write(convertedBytes, 0, convertedBytes.length);
					}

					fileStream.close();
				}
				else
				{
					FileOutputStream outStream = new FileOutputStream(fileName, true);
					
					try
					{
					if (binary) 
					{
						outStream.write(buffer, 0, size);
					}
					else 
					{
						IByteConverter byteConverter = _dataStore.getByteConverter();
						byteConverter.setContext(file);
						byte[] convertedBytes = byteConverter.convertClientBytesToHostBytes(buffer, 0, size);
						outStream.write(convertedBytes, 0, convertedBytes.length);
					}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					
					outStream.close();

				}
				if (status == null)
					return;
				status.setAttribute(DE.A_SOURCE, "success");
				_dataStore.refresh(status.getParent());
			}
			catch (IOException e)
			{
				System.out.println(e);
				if (status == null)
					return;
				status.setAttribute(DE.A_VALUE, FILEMSG_REMOTE_SAVE_FAILED);
				status.setAttribute(DE.A_SOURCE, "failed");
				_dataStore.refresh(status.getParent());
			}
		}
	}

	

	/**
	 * Called by <code>sendBytes</code> to either save the bytes to a local file or transmit
	 * them to a remote file.
	 * @param path the path of the file
	 * @param bytes the bytes of the file
	 * @param size the size of the file
	 * @param binary indicates whether the bytes are to be sent as binary or text
	 */
	protected void internalSendBytes(String path, byte[] bytes, int size, boolean binary)
	{
		if (_dataStore.isVirtual())
		{
			_dataStore.replaceFile(path, bytes, size, binary);
		}
		else
		{
			_dataStore.updateFile(path, bytes, size, binary);
		}
	}

	/**
	 * Called by <code>sendBytes</code> to either append the bytes to a local file or transmit
	 * them and append them to a remote file.
	 * @param path the path of the file
	 * @param bytes the bytes of the file
	 * @param size the size of the file
	 * @param binary indicates whether the bytes are to be sent as binary or text
	 */
	protected void internalSendAppendBytes(String path, byte[] bytes, int size, boolean binary)
	{
		if (_dataStore.isVirtual())
		{
			_dataStore.replaceAppendFile(path, bytes, size, binary);
		}
		else
		{
			_dataStore.updateAppendFile(path, bytes, size, binary);
		}
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

}