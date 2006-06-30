/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.dstore.universal.miners.filesystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.eclipse.dstore.core.model.ByteStreamHandler;
import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.rse.services.clientserver.IClientServerConstants;
import org.eclipse.rse.services.clientserver.IServiceConstants;
import org.eclipse.rse.services.clientserver.SystemEncodingUtil;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler;
import org.eclipse.rse.services.clientserver.archiveutils.VirtualChild;



/**
 * This class overrides ByteStreamHandler to handle cases where
 * virtual files are being transfered to the host
 */
public class UniversalByteStreamHandler extends ByteStreamHandler
{
	
	public UniversalByteStreamHandler(DataStore dataStore, DataElement log)
	{
		super(dataStore, log);
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
		boolean isVirtual = ArchiveHandlerManager.isVirtual(remotePath);
		
		if (!isVirtual)
		{
			super.receiveBytes(remotePath, buffer, size, binary);
			return;
		}

		remotePath = ArchiveHandlerManager.cleanUpVirtualPath(remotePath);
		DataElement status = findStatusFor(remotePath);
		String fileName = _dataStore.mapToLocalPath(remotePath);

		if (fileName != null)
		{
			String virtualFileName = fileName;

			ArchiveHandlerManager mgr = ArchiveHandlerManager.getInstance();
			VirtualChild child = mgr.getVirtualObject(virtualFileName);
			ISystemArchiveHandler handler = child.getHandler();

			try
			{
				File file = child.getExtractedFile();
				fileName = file.getAbsolutePath();

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
					String bufferString = new String(buffer, 0, size, SystemEncodingUtil.ENCODING_UTF_8);

					// hack for zOS
					String theOS = System.getProperty("os.name");
					if (theOS.toLowerCase().startsWith("z"))
					{
						bufferString = bufferString.replace('\r', ' ');
					}

					OutputStreamWriter writer = new OutputStreamWriter(fileStream);
					writer.write(bufferString, 0, size);
					writer.flush();
				}

				fileStream.close();

				// write the temp file to the archive
				if (handler == null)
				{
					int virtualIndex = virtualFileName.indexOf(ArchiveHandlerManager.VIRTUAL_SEPARATOR);
					String filePath = virtualFileName.substring(0, virtualIndex);
					handler = mgr.getRegisteredHandler(new File(filePath));
				}
					boolean success = handler != null && handler.add(newFile, child.path, child.name);
				if (!success)
				{
					if (status == null) return;
					status.setAttribute(DE.A_VALUE, IClientServerConstants.FILEMSG_REMOTE_SAVE_FAILED);
					status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
					_dataStore.refresh(status.getParent());
				}
				else
				{
					if (status == null) return;
					status.setAttribute(DE.A_SOURCE, IServiceConstants.SUCCESS);
					_dataStore.refresh(status.getParent());
				}
			}
			catch (IOException e)
			{
				_dataStore.trace(e);
				if (status == null) return;
				status.setAttribute(DE.A_VALUE, IClientServerConstants.FILEMSG_REMOTE_SAVE_FAILED);
				status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
				_dataStore.refresh(status.getParent());
			}
			catch (Exception e)
			{
				_dataStore.trace(e);
				if (status == null) return;
				status.setAttribute(DE.A_VALUE, IClientServerConstants.FILEMSG_REMOTE_SAVE_FAILED);
				status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
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
		
		boolean isVirtual = ArchiveHandlerManager.isVirtual(remotePath);
		if (!isVirtual)
		{
			super.receiveAppendedBytes(remotePath, buffer, size, binary);
			return;
		}

		remotePath = ArchiveHandlerManager.cleanUpVirtualPath(remotePath);
		DataElement status = findStatusFor(remotePath);
		String fileName = _dataStore.mapToLocalPath(remotePath);

		if (fileName != null)
		{
			String virtualFileName = fileName;

			ArchiveHandlerManager mgr = ArchiveHandlerManager.getInstance();
			VirtualChild child = mgr.getVirtualObject(virtualFileName);
			if (!child.exists())
			{
				//System.out.println(virtualFileName + " does not exist.");
				return;
			}
			ISystemArchiveHandler handler = child.getHandler();

			try
			{
				boolean success;

				File file = child.getExtractedFile();
				fileName = file.getAbsolutePath();

				if (!file.exists())
				{
					File parent = new File(file.getParent());
					parent.mkdirs();

					File newFile = new File(fileName);
					FileOutputStream fileStream = new FileOutputStream(newFile);

					//boolean binary = false;
					if (binary)
					{
						fileStream.write(buffer, 0, size);
					}
					else
					{
						String bufferString = new String(buffer, 0, size, SystemEncodingUtil.ENCODING_UTF_8);

						//						hack for zOS
						String theOS = System.getProperty("os.name");
						if (theOS.toLowerCase().startsWith("z"))
						{
							bufferString = bufferString.replace('\r', ' ');
						}

						OutputStreamWriter writer = new OutputStreamWriter(fileStream);
						writer.write(bufferString, 0, size);
						writer.flush();
					}

					fileStream.close();
					// write the temp file to the archive
					if (handler == null)
					{
						int virtualIndex = virtualFileName.indexOf(ArchiveHandlerManager.VIRTUAL_SEPARATOR);
						String filePath = virtualFileName.substring(0, virtualIndex);
						handler = mgr.getRegisteredHandler(new File(filePath));
					}
					success = handler != null && handler.add(newFile, child.path, child.name);

				}
				else
				{
					// need to reorganize this so that we don't use up all the memory
					// divide appendedBuffer into chunks
					// at > 50M this kills Eclipse
					File oldFile = new File(fileName);
					File newFile = new File(fileName + ".new");
					newFile.createNewFile();

					FileInputStream oldFileStream = new FileInputStream(oldFile);
					FileOutputStream newFileStream = new FileOutputStream(newFile);

					// write old file to new file
					int maxSize = 5000000;
					int written = 0;
					int oldSize = (int) oldFile.length();
					int bufferSize = (oldSize > maxSize) ? maxSize : oldSize;
					byte[] subBuffer = new byte[bufferSize];

					while (written < oldSize)
					{
						int subWritten = 0;

						while (written < oldSize && subWritten < bufferSize)
						{
							int available = oldFileStream.available();
							available = (bufferSize > available) ? available : bufferSize;
							int read = oldFileStream.read(subBuffer, subWritten, available);
							subWritten += read;
							written += subWritten;
						}

						newFileStream.write(subBuffer, 0, subWritten);
					}

					oldFileStream.close();

					// write new buffer to new file
					if (binary)
					{
						newFileStream.write(buffer, 0, size);
					}
					else
					{
						String bufferString = new String(buffer, 0, size, SystemEncodingUtil.ENCODING_UTF_8);

						// hack for zOS
						String theOS = System.getProperty("os.name");
						if (theOS.toLowerCase().startsWith("z"))
						{
							bufferString = bufferString.replace('\r', ' ');
						}

						OutputStreamWriter writer = new OutputStreamWriter(newFileStream);
						writer.write(bufferString, 0, size);
						writer.flush();
					}

					newFileStream.close();

					// remote old file
					oldFile.delete();

					// rename new file 
					newFile.renameTo(oldFile);

					//	write the temp file to the archive
					if (handler == null)
					{
						int virtualIndex = virtualFileName.indexOf(ArchiveHandlerManager.VIRTUAL_SEPARATOR);
						String filePath = virtualFileName.substring(0, virtualIndex);
						handler = mgr.getRegisteredHandler(new File(filePath));
					}
					success = handler != null && handler.add(newFile, child.path, child.name);

				}
				
				if (!success)
				{
					if (status == null) return;
					status.setAttribute(DE.A_VALUE, IClientServerConstants.FILEMSG_REMOTE_SAVE_FAILED);
					status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
					_dataStore.refresh(status.getParent());
				}
				else
				{
					if (status == null) return;
					status.setAttribute(DE.A_SOURCE, IServiceConstants.SUCCESS);
					_dataStore.refresh(status.getParent());
				}
			}
			catch (IOException e)
			{
				_dataStore.trace(e);
				if (status == null) return;
				status.setAttribute(DE.A_VALUE, IClientServerConstants.FILEMSG_REMOTE_SAVE_FAILED);
				status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
				_dataStore.refresh(status.getParent());
			}
		}
	}

}