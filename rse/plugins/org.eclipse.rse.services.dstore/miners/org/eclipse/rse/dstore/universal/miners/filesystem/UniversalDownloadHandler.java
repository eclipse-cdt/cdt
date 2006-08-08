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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.dstore.core.model.IByteConverter;
import org.eclipse.rse.dstore.universal.miners.ICancellableHandler;
import org.eclipse.rse.dstore.universal.miners.IUniversalDataStoreConstants;
import org.eclipse.rse.dstore.universal.miners.UniversalServerUtilities;
import org.eclipse.rse.services.clientserver.IServiceConstants;
import org.eclipse.rse.services.clientserver.archiveutils.AbsoluteVirtualPath;
import org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler;
import org.eclipse.rse.services.clientserver.archiveutils.VirtualChild;


public class UniversalDownloadHandler extends Thread implements ICancellableHandler
{

	private boolean _isDone = false;
	private DataStore _dataStore;
	private UniversalFileSystemMiner _miner;
	private DataElement _status;
	private DataElement _cmdElement;
	private boolean _isCancelled= false;
	
	public UniversalDownloadHandler(DataStore dataStore, UniversalFileSystemMiner miner, DataElement cmdElement, 
			DataElement status)
	{
		_miner = miner;
		_dataStore = dataStore;	
		_status = status;
		_cmdElement = cmdElement;
	}

	public void run()
	{
		handleDownload(_cmdElement, _status);
		_isDone = true;
	}
	
	public boolean isDone()
	{
		return _isDone;
	}
	
	public boolean isCancelled()
	{
		return _isCancelled;
	}
	
	public void cancel()
	{
		_dataStore.trace("cancelling download");
		_isCancelled = true;
	}


	protected DataElement handleDownload(DataElement theElement, DataElement status)
	{
		DataElement arg1 = _miner.getCommandArgument(theElement, 1);
		String elementType = arg1.getType();
		String remotePath = arg1.getName();

		int buffer_size = IUniversalDataStoreConstants.BUFFER_SIZE;
		DataElement bufferSizeElement = _dataStore.find(theElement, DE.A_TYPE, "buffer_size", 1);
		if (bufferSizeElement != null)
		{
		    try
		    {
		        buffer_size = Integer.parseInt(bufferSizeElement.getName());
		    }
		    catch (Exception e)
		    {			        
		    }
		}
		
		String resultType = null;
		String resultMessage = null;						
		
		
		FileInputStream inputStream = null;
		BufferedInputStream bufInputStream = null;


		try
		{
			if (elementType.equals(IUniversalDataStoreConstants.UNIVERSAL_FILE_DESCRIPTOR) || elementType.equals(IUniversalDataStoreConstants.UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR) || elementType.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR))
			{

				_dataStore.trace("download:" + remotePath + "," + elementType);

				File file = new File(remotePath);


				if (elementType.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR))
				{
					AbsoluteVirtualPath vpath = _miner.getAbsoluteVirtualPath(remotePath);

					ISystemArchiveHandler handler = _miner.getArchiveHandlerFor(vpath.getContainingArchiveString());
					if (handler == null)
					{
						status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
						resultType = IUniversalDataStoreConstants.DOWNLOAD_RESULT_IO_EXCEPTION;
						resultMessage = "Corrupted archive.";
						_isDone = true;
						_dataStore.createObject(arg1, resultType, resultMessage);
						_dataStore.refresh(arg1);
						return _miner.statusDone(status);
					}
					VirtualChild vChild = handler.getVirtualFile(vpath.getVirtualPart());
					file = vChild.getExtractedFile();

				}

				DataElement arg2 = _miner.getCommandArgument(theElement, 2);
//				DataElement arg3 = _miner.getCommandArgument(theElement, 3);
				_miner.getCommandArgument(theElement, 3);

				int mode = (Integer.valueOf(arg1.getSource())).intValue();
				String localPath = arg2.getName();

				boolean isText = (mode == IUniversalDataStoreConstants.TEXT_MODE);

//				String clientEncoding = null;

				if (isText)
				{
//					clientEncoding = arg2.getSource();
					arg2.getSource();
				}

				// Read in the file
				inputStream = new FileInputStream(file);
				bufInputStream = new BufferedInputStream(inputStream, buffer_size);

				int totalBytes = (int)file.length();
				int totalWritten = 0;

				boolean first = true;
				byte[] buffer = new byte[buffer_size];
				byte[] convBytes;
				int numToRead = 0;

				IByteConverter byteConverter = _dataStore.getByteConverter();
				byteConverter.setContext(file);

				int available = bufInputStream.available();

				while (available > 0 && !_isCancelled)
				{
					numToRead = (available < buffer_size) ? available : buffer_size;

					int bytesRead = bufInputStream.read(buffer, 0, numToRead);

					if (bytesRead == -1)
						break;
					if (isText)
					{
						convBytes = byteConverter.convertHostBytesToClientBytes(buffer, 0, bytesRead);

						if (first)
						{ // send first set of bytes
							first = false;
							_dataStore.updateFile(localPath, convBytes, convBytes.length, true);
						}
						else
						{ // append subsequent segments
							_dataStore.updateAppendFile(localPath, convBytes, convBytes.length, true);
						}
						totalWritten += convBytes.length;
					}
					else
					{

						if (first)
						{ // send first set of bytes
							first = false;
							_dataStore.updateFile(localPath, buffer, bytesRead, true);
						}
						else
						{ // append subsequent segments
							_dataStore.updateAppendFile(localPath, buffer, bytesRead, true);
						}
						totalWritten +=bytesRead;
					}

					status.setAttribute(DE.A_SOURCE, "sent "+totalWritten + " of "+totalBytes);
					_dataStore.refresh(status);
					available = bufInputStream.available();
				}

				resultType = IUniversalDataStoreConstants.DOWNLOAD_RESULT_SUCCESS_TYPE;
				resultMessage = IUniversalDataStoreConstants.DOWNLOAD_RESULT_SUCCESS_MESSAGE;

			}
		}
		catch (FileNotFoundException e)
		{
			UniversalServerUtilities.logError(UniversalFileSystemMiner.CLASSNAME, "handleDownload: error reading file " + remotePath, e);
			resultType = IUniversalDataStoreConstants.DOWNLOAD_RESULT_FILE_NOT_FOUND_EXCEPTION;
			resultMessage = e.getLocalizedMessage();
		}
		catch (UnsupportedEncodingException e)
		{
			UniversalServerUtilities.logError(UniversalFileSystemMiner.CLASSNAME, "handleDownload: error reading file " + remotePath, e);
			resultType = IUniversalDataStoreConstants.DOWNLOAD_RESULT_UNSUPPORTED_ENCODING_EXCEPTION;
			resultMessage = e.getLocalizedMessage();
		}
		catch (IOException e)
		{
			UniversalServerUtilities.logError(UniversalFileSystemMiner.CLASSNAME, "handleDownload: error reading file " + remotePath, e);
			resultType = IUniversalDataStoreConstants.DOWNLOAD_RESULT_IO_EXCEPTION;
			resultMessage = e.getLocalizedMessage();
		}
		catch (Exception e)
		{
			UniversalServerUtilities.logError(UniversalFileSystemMiner.CLASSNAME, "handleDownload: error reading file " + remotePath, e);
			resultType = IUniversalDataStoreConstants.DOWNLOAD_RESULT_EXCEPTION;
			resultMessage = e.getLocalizedMessage();
		}
		finally
		{

			try
			{

				if (bufInputStream != null)
					bufInputStream.close();

			}
			catch (IOException e)
			{
				UniversalServerUtilities.logError(UniversalFileSystemMiner.CLASSNAME, "handleDownload: error closing reader on " + remotePath, e);
				resultType = IUniversalDataStoreConstants.DOWNLOAD_RESULT_IO_EXCEPTION;
				resultMessage = e.getMessage();
			}
		}
		_isDone = true;

		_dataStore.createObject(arg1, resultType, resultMessage);
		_dataStore.refresh(arg1);
		
		return _miner.statusDone(status);
	}


}