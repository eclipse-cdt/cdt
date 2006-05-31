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

package org.eclipse.rse.services.dstore.files;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.dstore.core.model.DataStoreAttributes;
import org.eclipse.dstore.core.model.DataStoreResources;
import org.eclipse.dstore.core.model.IDataStoreConstants;
import org.eclipse.dstore.core.model.IDataStoreProvider;
import org.eclipse.rse.dstore.universal.miners.IUniversalDataStoreConstants;
import org.eclipse.rse.dstore.universal.miners.filesystem.UniversalByteStreamHandler;
import org.eclipse.rse.dstore.universal.miners.filesystem.UniversalFileSystemMiner;
import org.eclipse.rse.services.clientserver.IServiceConstants;
import org.eclipse.rse.services.clientserver.ISystemFileTypes;
import org.eclipse.rse.services.clientserver.PathUtility;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.dstore.AbstractDStoreService;
import org.eclipse.rse.services.dstore.ServiceResources;
import org.eclipse.rse.services.dstore.util.DownloadListener;
import org.eclipse.rse.services.dstore.util.FileSystemMessageUtil;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.swt.widgets.Display;




public class DStoreFileService extends AbstractDStoreService implements IFileService, IUniversalDataStoreConstants
{

	protected org.eclipse.dstore.core.model.DataElement _uploadLogElement = null;
	protected Map _fileElementMap;
	private int _bufferUploadSize = IUniversalDataStoreConstants.BUFFER_SIZE;
	private int _bufferDownloadSize = IUniversalDataStoreConstants.BUFFER_SIZE;
	protected ISystemFileTypes _fileTypeRegistry;
	
	private static String _percentMsg = SystemMessage.sub(SystemMessage.sub(SystemMessage.sub(ServiceResources.DStore_Service_Percent_Complete_Message, "&0", "{0}"), "&1", "{1}"), "&2", "{2}");	

	private static String[] _filterAttributes =  {
		"attributes", 
		"filter",
		"filter.id",
		"doc",
		"",
		"",
		DataStoreResources.FALSE,
		"2"};
	
	public DStoreFileService(IDataStoreProvider dataStoreProvider, ISystemFileTypes fileTypeRegistry)
	{
		super(dataStoreProvider);
		_fileElementMap = new HashMap();
		_fileTypeRegistry = fileTypeRegistry;
	}
	
	public String getName()
	{
		return ServiceResources.DStore_File_Service_Label;
	}
	
	public String getDescription()
	{
		return ServiceResources.DStore_File_Service_Description;
	}
	
	public void setBufferUploadSize(int size)
	{
		_bufferUploadSize = size;
	}
	
	public void setBufferDownloadSize(int size)
	{
		_bufferDownloadSize = size;
	}
	
	protected int getBufferUploadSize()
	{
		return _bufferUploadSize;
	}
	
	protected int getBufferDownloadSize()
	{
		return _bufferDownloadSize;
	}
	
	protected String getMinerId()
	{
		return UniversalFileSystemMiner.MINER_ID;
	}
	
	protected String getByteStreamHandlerId()
	{
		return UniversalByteStreamHandler.class.getName();
	}
	
	protected String getDataStoreRoot()
	{
		return getDataStore().getAttribute(DataStoreAttributes.A_LOCAL_PATH);
	}
	

	protected String prepareForDownload(String localPath)
	{
		int index = localPath.lastIndexOf(File.separator);
		String parentDir = localPath.substring(0, index + 1);

		// change local root for datastore so that the file is downloaded
		// at the specified location
		setDataStoreRoot(parentDir);

		String dataStoreLocalPath = localPath.substring(index + 1);

		if (!dataStoreLocalPath.startsWith("/"))
			dataStoreLocalPath = "/" + dataStoreLocalPath;

		return dataStoreLocalPath;
	}

	protected void setDataStoreRoot(String root)
	{
		getDataStore().setAttribute(DataStoreAttributes.A_LOCAL_PATH, root);
	}
	
	protected DataElement findUploadLog()
	{
	    DataElement minerInfo = getMinerElement();
		if (_uploadLogElement ==  null || _uploadLogElement.getDataStore() != getDataStore())
		{
		    _uploadLogElement = getDataStore().find(minerInfo, DE.A_NAME, "universal.uploadlog", 2);
		}
		return _uploadLogElement;
	}
	
	protected DataElement getAttributes(String fileNameFilter, boolean showHidden)
	{
		DataElement attributes = getDataStore().createTransientObject(_filterAttributes);
		String version = VERSION_1;
		StringBuffer buffer = new StringBuffer();
		String filter = ((fileNameFilter == null) ? "*" : fileNameFilter);
		buffer.append(version).append(TOKEN_SEPARATOR).append(filter).append(TOKEN_SEPARATOR).append(showHidden);
		attributes.setAttribute(DE.A_SOURCE, buffer.toString());
		return attributes;
	}

	

	public boolean upload(IProgressMonitor monitor, InputStream inputStream, String remoteParent, String remoteFile,
			boolean isBinary, String hostEncoding)
	{
		BufferedInputStream bufInputStream = null;

	
		boolean isCancelled = false;
		Display display = Display.getCurrent();
	
	
		try
		{	
			
			DataElement uploadLog = findUploadLog();
//			listener = new FileTransferStatusListener(remotePath, shell, monitor, getConnectorService(), ds, uploadLog);
	//		ds.getDomainNotifier().addDomainListener(listener);

			int buffer_size = getBufferUploadSize();
			
			// read in the file
			bufInputStream = new BufferedInputStream(inputStream, buffer_size);

			boolean first = true;
			byte[] buffer = new byte[buffer_size];
			byte[] convBytes;
			int numToRead = 0;

			int available = bufInputStream.available();

			long totalSent = 0;

			// upload bytes while available
			while (available > 0 && !isCancelled)
			{
				if (display != null && !display.isDisposed())
				{
					while (display.readAndDispatch()) {}
				}
				
				numToRead = (available < buffer_size) ? available : buffer_size;

				int bytesRead = bufInputStream.read(buffer, 0, numToRead);

				if (bytesRead == -1)
					break;
					
				totalSent += bytesRead;

				String byteStreamHandlerId = getByteStreamHandlerId();
				String remotePath = remoteParent + getSeparator(remoteParent) + remoteFile;
				
				if (!isBinary) 
				{
					String tempStr = new String(buffer, 0, bytesRead);

					// hack for zOS - \r causes problems for compilers
//					if (osName != null && (osName.startsWith("z") || osName.equalsIgnoreCase("aix")))
//					{
//						tempStr = tempStr.replace('\r', ' ');
//					}

					convBytes = tempStr.getBytes(hostEncoding);
				
					if (first)
					{ // send first set of bytes
						first = false;
						getDataStore().replaceFile(remotePath, convBytes, convBytes.length, true, byteStreamHandlerId);
					}
					else
					{ // append subsequent segments
						getDataStore().replaceAppendFile(remotePath, convBytes, convBytes.length, true, byteStreamHandlerId);
					}
				}
				else // binary
				{
					if (first)
					{ // send first set of bytes
						first = false;
						getDataStore().replaceFile(remotePath, buffer, bytesRead, true, byteStreamHandlerId);
					}
					else
					{ // append subsequent segments
						getDataStore().replaceAppendFile(remotePath, buffer, bytesRead, true, byteStreamHandlerId);
					}
				}			
		
				
				if (display != null && monitor != null)
				{

					isCancelled = monitor.isCanceled();
					if (isCancelled) 
					{						
						while (display.readAndDispatch()) 
						{
						}
					}
				}

				available = bufInputStream.available();
			}
//			if (listener.uploadHasFailed())
//			{
//				showUploadFailedMessage(listener, source);
//			}
//			else
			{
		//	    transferSuccessful = true;
			}
		}

		catch (FileNotFoundException e)
		{
//			UniversalSystemPlugin.logError(CLASSNAME + "." + "copy: " + "error writing file " + remotePath, e);
//			throw new RemoteFileIOException(e);
			return false;
		}
		catch (UnsupportedEncodingException e)
		{
//			UniversalSystemPlugin.logError(CLASSNAME + "." + "copy: " + "error writing file " + remotePath, e);
//			throw new RemoteFileIOException(e);
			return false;
		}
		catch (IOException e)
		{
//			UniversalSystemPlugin.logError(CLASSNAME + "." + "copy: " + "error writing file " + remotePath, e);
//			throw new RemoteFileIOException(e);
			return false;
		}
		catch (Exception e)
		{
//			UniversalSystemPlugin.logError(CLASSNAME + "." + "copy: " + "error writing file " + remotePath, e);
//			throw new RemoteFileIOException(e);
			return false;
		}
		finally
		{

			try
			{

				if (bufInputStream != null)
					bufInputStream.close();

				if (isCancelled)
				{
					return false;
					//throw new RemoteFileCancelledException();
				}
			}
			catch (IOException e)
			{
//				UniversalSystemPlugin.logError(CLASSNAME + "." + "copy: " + "error writing file " + remotePath, e);
//				throw new RemoteFileIOException(e);
				return false;
			}

		}
		
		if (display != null && monitor != null)
		{
		//	monitor.done();
			while (display.readAndDispatch()) {
			}
		}
		return true;
	}

	public boolean upload(IProgressMonitor monitor, File file, String remoteParent, String remoteFile,
			boolean isBinary, String srcEncoding, String hostEncoding)
	{
		FileInputStream inputStream = null;
		BufferedInputStream bufInputStream = null;

	
		boolean isCancelled = false;
		boolean transferSuccessful = false;
		Display display = Display.getCurrent();
	
		long totalBytes = file.length();
	
		try
		{	
			// if the file is empty, create new empty file on host
			if (totalBytes == 0)
			{
				IHostFile created = createFile(monitor, remoteParent, remoteFile);
				return created.exists();
			}
		
			if (monitor != null)
			{
				monitor.setTaskName(file.getName());
				//subMonitor = new SubProgressMonitor(monitor, (int)totalBytes);
			}

			
			DataElement uploadLog = findUploadLog();
//			listener = new FileTransferStatusListener(remotePath, shell, monitor, getConnectorService(), ds, uploadLog);
	//		ds.getDomainNotifier().addDomainListener(listener);

			int buffer_size = getBufferUploadSize();
			
			// read in the file
			inputStream = new FileInputStream(file);
			bufInputStream = new BufferedInputStream(inputStream, buffer_size);

			boolean first = true;
			byte[] buffer = new byte[buffer_size];
			byte[] convBytes;
			int numToRead = 0;

			int available = bufInputStream.available();

			long totalSent = 0;

			// upload bytes while available
			while (available > 0 && !isCancelled)
			{
				if (display != null && !display.isDisposed())
				{
					while (display.readAndDispatch()) {}
				}
				
				numToRead = (available < buffer_size) ? available : buffer_size;

				int bytesRead = bufInputStream.read(buffer, 0, numToRead);

				if (bytesRead == -1)
					break;
					
				totalSent += bytesRead;

				String byteStreamHandlerId = getByteStreamHandlerId();
				String remotePath = remoteParent + getSeparator(remoteParent) + remoteFile;
				
				if (!isBinary) 
				{
					String tempStr = new String(buffer, 0, bytesRead, srcEncoding);

					// hack for zOS - \r causes problems for compilers
//					if (osName != null && (osName.startsWith("z") || osName.equalsIgnoreCase("aix")))
//					{
//						tempStr = tempStr.replace('\r', ' ');
//					}

					convBytes = tempStr.getBytes(hostEncoding);
				
					if (first)
					{ // send first set of bytes
						first = false;
						getDataStore().replaceFile(remotePath, convBytes, convBytes.length, true, byteStreamHandlerId);
					}
					else
					{ // append subsequent segments
						getDataStore().replaceAppendFile(remotePath, convBytes, convBytes.length, true, byteStreamHandlerId);
					}
				}
				else // binary
				{
					if (first)
					{ // send first set of bytes
						first = false;
						getDataStore().replaceFile(remotePath, buffer, bytesRead, true, byteStreamHandlerId);
					}
					else
					{ // append subsequent segments
						getDataStore().replaceAppendFile(remotePath, buffer, bytesRead, true, byteStreamHandlerId);
					}
				}			
		
				
				if (/*display != null &&*/ monitor != null)
				{
					long percent = (totalSent * 100) / totalBytes;

			
					StringBuffer totalSentBuf = new StringBuffer();
					totalSentBuf.append((totalSent / KB_IN_BYTES));
					totalSentBuf.append(" KB");
					
					StringBuffer totalBuf = new StringBuffer();
					totalBuf.append(totalBytes / KB_IN_BYTES);
					totalBuf.append(" KB");
					
					StringBuffer percentBuf = new StringBuffer();
					percentBuf.append(percent);
					percentBuf.append("%");
								
					monitor.worked(bytesRead);
					
					String str = MessageFormat.format(_percentMsg, new Object[] {totalSentBuf, totalBuf, percentBuf});
					monitor.subTask(str);					
					if (display != null)
					{
						while (display.readAndDispatch()) 
						{
						}
					}



					isCancelled = monitor.isCanceled();
					if (isCancelled && display != null) 
					{						
						while (display.readAndDispatch()) 
						{
						}
					}
				}

				available = bufInputStream.available();
			}
//			if (listener.uploadHasFailed())
//			{
//				showUploadFailedMessage(listener, source);
//			}
//			else
			{
			    transferSuccessful = true;
			}
		}

		catch (FileNotFoundException e)
		{
//			UniversalSystemPlugin.logError(CLASSNAME + "." + "copy: " + "error writing file " + remotePath, e);
//			throw new RemoteFileIOException(e);
			return false;
		}
		catch (UnsupportedEncodingException e)
		{
//			UniversalSystemPlugin.logError(CLASSNAME + "." + "copy: " + "error writing file " + remotePath, e);
//			throw new RemoteFileIOException(e);
			return false;
		}
		catch (IOException e)
		{
//			UniversalSystemPlugin.logError(CLASSNAME + "." + "copy: " + "error writing file " + remotePath, e);
//			throw new RemoteFileIOException(e);
			return false;
		}
		catch (Exception e)
		{
//			UniversalSystemPlugin.logError(CLASSNAME + "." + "copy: " + "error writing file " + remotePath, e);
//			throw new RemoteFileIOException(e);
			return false;
		}
		finally
		{

			try
			{

				if (bufInputStream != null)
					bufInputStream.close();

				if (isCancelled)
				{
					return false;
					//throw new RemoteFileCancelledException();
				}
			}
			catch (IOException e)
			{
//				UniversalSystemPlugin.logError(CLASSNAME + "." + "copy: " + "error writing file " + remotePath, e);
//				throw new RemoteFileIOException(e);
				return false;
			}

			if (totalBytes > 0)
			{
			    if (transferSuccessful)
			    {
	

//					try
//					{
//						listener.waitForUpdate(null, 2);
//
//					}
//					catch (InterruptedException e)
//					{
//						UniversalSystemPlugin.logError(CLASSNAME + " InterruptedException while waiting for command", e);
//					}
					
			    }
			
				//ds.getDomainNotifier().removeDomainListener(listener);

//				if (listener.uploadHasFailed())
//				{
//					showUploadFailedMessage(listener, source);
//				}
			}
		}
		
		if (display != null && monitor != null)
		{
		//	monitor.done();
			while (display.readAndDispatch()) {
			}
		}
		return true;
	}


	public boolean download(IProgressMonitor monitor, String remoteParent, String remoteFile, File localFile,
			boolean isBinary, String encoding)
	{
		DataElement universaltemp = getMinerElement();

		if (!localFile.exists())
		{
			File parentDir = localFile.getParentFile();
			parentDir.mkdirs();
		}

		try
		{
			if (localFile.exists())
				localFile.delete();
			localFile.createNewFile();
		}
		catch (IOException e)
		{
//			UniversalSystemPlugin.logError(CLASSNAME + "." + "copy: " + "error creating local file " + destination, e);
//			throw new RemoteFileIOException(e);
			return false;
		}

		int mode;

		if (isBinary)
		{
			mode = BINARY_MODE;
		}
		else
		{
			mode = TEXT_MODE;
		}

		DataStore ds = getDataStore();
		String remotePath = remoteParent + getSeparator(remoteParent) + remoteFile;
		
		DataElement de = getElementFor(remotePath);
		if (de.getType().equals(IUniversalDataStoreConstants.UNIVERSAL_FILTER_DESCRIPTOR))
		{
			// need to refetch
			DStoreHostFile hostFile = (DStoreHostFile)getFile(monitor, remoteParent, remoteFile);
			de = hostFile._element;
		}
		long fileLength = DStoreHostFile.getFileLength(de.getSource());
		
		
		DataElement remoteElement = ds.createObject(universaltemp, de.getType(), remotePath, String.valueOf(mode));
	
		String tempRoot = getDataStoreRoot();
				
		String dataStoreLocalPath = prepareForDownload(localFile.getAbsolutePath());
		
		DataElement localElement = ds.createObject(universaltemp, de.getType(), dataStoreLocalPath, encoding);
		
		DataElement bufferSizeElement = ds.createObject(universaltemp, "buffer_size", "" + getBufferDownloadSize(), "");
		DataElement queryCmd = getCommandDescriptor(de,C_DOWNLOAD_FILE);

		ArrayList argList = new ArrayList();
		argList.add(remoteElement);
		argList.add(localElement);
		argList.add(bufferSizeElement);
		
		DataElement subject = ds.createObject(universaltemp, de.getType(), remotePath, String.valueOf(mode));
		
		DataElement status = ds.command(queryCmd, argList, subject);
		if (status == null)
		{
			System.out.println("no download descriptor for "+remoteElement);
		}
		try
		{
			DownloadListener dlistener = new DownloadListener(monitor, status, localFile, remotePath, (long) fileLength);
			try
			{
				dlistener.waitForUpdate();
			}
			catch (InterruptedException e)
			{
			}

			//getStatusMonitor(ds).waitForUpdate(status, monitor);
		}
		catch (Exception e)
		{
			return false;
		}

		// now wait till we have all the bytes local
		long localBytes = localFile.length();
		long lastLocalBytes = 0;
		while (localBytes < fileLength && (monitor == null || !monitor.isCanceled()) && lastLocalBytes != localBytes)
		{
			try
			{
				lastLocalBytes= localBytes;
				Thread.sleep(100);
				localBytes = localFile.length();
				
			}
			catch (Exception e)
			{				
			}
		}

//		DownloadListener dlistener = new DownloadListener(shell, monitor, getConnectorService(), status, localFile, remotePath, (long) universalFile.getLength());
//		try
//		{
//			dlistener.waitForUpdate();
//		}
//		catch (InterruptedException e)
//		{
//			UniversalSystemPlugin.logError(CLASSNAME + " InterruptedException while waiting for command", e);
//		}

//		if (!dlistener.isCancelled())
//		{
//
//			setDataStoreRoot(tempRoot);
//
//			ArrayList resultList = remoteElement.getNestedData();
//			DataElement resultChild = null;
//
//			for (int i = 0; i < resultList.size(); i++)
//			{
//
//				resultChild = (DataElement) resultList.get(i);
//
//				if (resultChild.getType().equals(DOWNLOAD_RESULT_SUCCESS_TYPE))
//				{
//					return;
//				}
//				else if (resultChild.getType().equals(DOWNLOAD_RESULT_FILE_NOT_FOUND_EXCEPTION))
//				{
//					FileNotFoundException e = new FileNotFoundException(resultChild.getName());
//					UniversalSystemPlugin.logError(CLASSNAME + "." + "copy: " + "error reading file " + remotePath, e);
//					throw new RemoteFileIOException(e);
//				}
//				else if (resultChild.getType().equals(DOWNLOAD_RESULT_UNSUPPORTED_ENCODING_EXCEPTION))
//				{
//					UnsupportedEncodingException e = new UnsupportedEncodingException(resultChild.getName());
//					UniversalSystemPlugin.logError(CLASSNAME + "." + "copy: " + "error reading file " + remotePath, e);
//					throw new RemoteFileIOException(e);
//				}
//				else if (resultChild.getType().equals(DOWNLOAD_RESULT_IO_EXCEPTION))
//				{
//					IOException e = new IOException(resultChild.getName());
//					UniversalSystemPlugin.logError(CLASSNAME + "." + "copy: " + "error reading file " + remotePath, e);
//					throw new RemoteFileIOException(e);
//				}
//			}
//		}
//		else
//		{
//			throw new RemoteFileCancelledException();
//		}
		if (monitor != null)
		{
			//monitor.done();
		}
		return true;
	}

	public IHostFile getFile(IProgressMonitor monitor, String remoteParent, String name)
	{
		DataElement de = null;
		if (name.equals(".") && name.equals(remoteParent))
		{
			de = getElementFor(name);
		}
		else
		{
			de = getElementFor(remoteParent + getSeparator(remoteParent) + name);
		}
		dsQueryCommand(monitor, de, C_QUERY_GET_REMOTE_OBJECT);
		return new DStoreHostFile(de);
	}

	/**
	 * Returns what the next part of the path should be, given the current
	 * path as parentPath. Returns different separators based on whether the path
	 * appears to be a windows, linux, or virtual path.
	 * Pass in null to just get the default separator.
	 */
	protected String getSeparator(String parentPath)
	{
		if (parentPath == null || parentPath.length() < 2) return "/";
		if (parentPath.endsWith(ArchiveHandlerManager.VIRTUAL_SEPARATOR))
			return "";
		if (parentPath.endsWith(ArchiveHandlerManager.VIRTUAL_CANONICAL_SEPARATOR))
			return "/";
		if (parentPath.charAt(1) == ':') //Windows path
			if (parentPath.indexOf(ArchiveHandlerManager.VIRTUAL_CANONICAL_SEPARATOR) != -1)
				if (parentPath.endsWith("/"))
					return ""; //already ends in separator
				else return "/";
			else if (ArchiveHandlerManager.getInstance().isArchive(new File(parentPath)))
				return ArchiveHandlerManager.VIRTUAL_SEPARATOR;
			else
				if (parentPath.endsWith("\\"))
					return ""; //already ends in separator
				else return "\\";
		else if (parentPath.charAt(0) == '/') //UNIX path
			if (ArchiveHandlerManager.getInstance().isArchive(new File(parentPath)))
				return ArchiveHandlerManager.VIRTUAL_SEPARATOR;
			else
				if (parentPath.endsWith("/"))
					return ""; //already ends in separator
				else return "/";
		else return "/"; //unrecognized path
	}
	
	protected IHostFile convertToHostFile(DataElement element)
	{
		String type = element.getType();
		IHostFile file = null;
		if (type.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR) ||
				type.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR))
		{
			file = new DStoreVirtualHostFile(element);
		}						
		else
		{
			file = new DStoreHostFile(element);
		}
		String path =  file.getAbsolutePath();
		_fileElementMap.put(path, element);
		return file;
	}
	
	protected IHostFile[] convertToHostFiles(DataElement[] elements)
	{
		ArrayList results = new ArrayList();
		for (int i = 0; i < elements.length; i++)
		{
			if (!elements[i].isDeleted())
			results.add(convertToHostFile(elements[i]));
		}
		return (IHostFile[]) results.toArray(new IHostFile[results.size()]);
	}

	public IHostFile getUserHome()
	{
		return getFile(null, ".",".");
	}

	public IHostFile createFile(IProgressMonitor monitor, String remoteParent, String fileName)
	{
		String remotePath = remoteParent + getSeparator(remoteParent) + fileName;
		DataElement de = getElementFor(remotePath);
		dsQueryCommand(monitor, de, C_CREATE_FILE);
		return new DStoreHostFile(de);
	}

	public IHostFile createFolder(IProgressMonitor monitor, String remoteParent, String folderName)
	{
		String remotePath = remoteParent + getSeparator(remoteParent) + folderName;
		DataElement de = getElementFor(remotePath);
		dsQueryCommand(monitor, de, C_CREATE_FOLDER);
		return new DStoreHostFile(de);
	}

	public boolean delete(IProgressMonitor monitor, String remoteParent, String fileName) throws SystemMessageException
	{
		String remotePath = remoteParent + getSeparator(remoteParent) + fileName;
		DataElement de = getElementFor(remotePath);
		DataElement status = dsStatusCommand(monitor, de, C_DELETE);
		if (status == null) return false;
		if (de.getType().equals(IUniversalDataStoreConstants.UNIVERSAL_FILE_DESCRIPTOR))
		{
			if (FileSystemMessageUtil.getSourceMessage(status).equals(IServiceConstants.SUCCESS)) return true;
			else throw new SystemMessageException(getMessage("RSEF1300").makeSubstitution(FileSystemMessageUtil.getSourceLocation(status)));	
		}
		else
		{
			return true;
		}

	}
	
	public boolean deleteBatch(IProgressMonitor monitor, String[] remoteParents, String[] fileNames) throws SystemMessageException
	{
		if (remoteParents.length == 1) return delete(monitor, remoteParents[0], fileNames[0]);
		
		ArrayList dataElements = new ArrayList(remoteParents.length);
		for (int i = 0; i < remoteParents.length; i++)
		{
			String remotePath = remoteParents[i] + getSeparator(remoteParents[i]) + fileNames[i];
			DataElement de = getElementFor(remotePath);
			if (de != null) dataElements.add(de);
		}	
		DataElement status = dsStatusCommand(monitor, (DataElement) dataElements.get(0), dataElements, C_DELETE_BATCH);
		if (status == null) return false;
		if (FileSystemMessageUtil.getSourceMessage(status).equals(IServiceConstants.SUCCESS)) return true;
		else throw new SystemMessageException(getMessage("RSEF1300").makeSubstitution(FileSystemMessageUtil.getSourceLocation(status)));	
	}

	public boolean rename(IProgressMonitor monitor, String remoteParent, String oldName, String newName)
	{
		String remotePath = remoteParent + getSeparator(remoteParent) + oldName;
		DataElement de = getElementFor(remotePath);
		de.setAttribute(DE.A_SOURCE, newName);
		dsQueryCommand(monitor, de, C_RENAME);
		return true;
	}
	
	public boolean rename(IProgressMonitor monitor, String remoteParent, String oldName, String newName, IHostFile oldFile)
	{
		boolean retVal = rename(monitor, remoteParent, oldName, newName);
		String newPath = remoteParent + getSeparator(remoteParent) + newName;
		oldFile.renameTo(newPath);
		return retVal;
	}

	public boolean move(IProgressMonitor monitor, String srcParent, String srcName, String tgtParent, String tgtName)
	{
		String src = srcParent + getSeparator(srcParent) + srcName;
		String tgt = tgtParent + getSeparator(tgtParent) + tgtName;
		boolean isVirtual = ArchiveHandlerManager.isVirtual(src) || ArchiveHandlerManager.isVirtual(tgt);
		//if (isVirtual || isArchive)
		{
			if (copy(monitor, srcParent, srcName, tgtParent, tgtName))
			{
				try
				{
					delete(monitor, srcParent, srcName);
				}
				catch (Exception e)
				{
					return false;
				}
				return true;
			}
			return false;
		}

/*
		// handle special characters in source and target strings 
		StringBuffer srcBuf = new StringBuffer(src);
		StringBuffer tgtBuf = new StringBuffer(tgt);
		
		for (int i = 0; i < srcBuf.length(); i++)
		{
			char c = srcBuf.charAt(i);
			
			boolean isSpecialChar = isSpecialChar(c);
			
			if (isSpecialChar)
			{
				srcBuf.insert(i, "\\");
				i++;
			}
		}

		for (int i = 0; i < tgtBuf.length(); i++)
		{
			char c = tgtBuf.charAt(i);
			
			boolean isSpecialChar = isSpecialChar(c);
			
			if (isSpecialChar)
			{
				tgtBuf.insert(i, "\\");
				i++;
			}
		}

		src = "\"" + srcBuf.toString() + "\"";
		tgt = "\"" + tgtBuf.toString() + "\"";

		if (systemType.equals(SYSTEMTYPE_WINDOWS))
		{
			if (sourceFolderOrFile.isDirectory() && sourceFolderOrFile.getAbsolutePath().charAt(0) != targetFolder.getAbsolutePath().charAt(0))
			{
				// special case - move across drives
				command = "xcopy " + src + " " + tgt + " /S /E /K /O /Q /H /I && rmdir /S /Q " + src;
			}
			else
			{
				command = "move " + src + " " + tgt;
			}
		}
		else
		{
			command = "mv " + src + " " + tgt;
		}

		UniversalCmdSubSystemImpl cmdSubSystem = getUniversalCmdSubSystem();
		IRemoteFile runFile = sourceFolderOrFile;

		if (cmdSubSystem != null)
		{
			try
			{
				done = cmdSubSystem.runRemoteCommand(runFile, command);
				runFile.getParentRemoteFile().markStale(true);
				runFile.markStale(true);
		
			}
			catch (InterruptedException e)
			{
				done = false;
			}
		}
		else
			SystemPlugin.logWarning(CLASSNAME + " cmdSubSystem is null in move");

		return done;
		*/

	}
	
	/**
	 * Checks whether the given character is a special character in the shell. A special character is
	 * '$', '`', '"' and '\'.
	 * @param c the character to check.
	 * @return <code>true</code> if the character is a special character, <code>false</code> otherwise.
	 */
	protected boolean isSpecialChar(char c)  {
		   
		if ((c == '$') || (c == '`') || (c == '"') || (c == '\\')) {
			return true;
		}
		else {
			return false;
		}
	}

	public boolean copy(IProgressMonitor monitor, String srcParent, String srcName, String tgtParent, String tgtName)
	{
		DataStore ds = getDataStore();
		String srcRemotePath = srcParent + getSeparator(srcParent) + srcName;
		DataElement srcDE = getElementFor(srcRemotePath);
		
		DataElement tgtDE = getElementFor(tgtParent);
		
		DataElement cpCmd = getCommandDescriptor(tgtDE, C_COPY);
	
		if (cpCmd != null)
		{
			ArrayList args = new ArrayList();
			args.add(srcDE);
			DataElement nameObj = ds.createObject(null, "name", tgtName);
			args.add(nameObj);
			DataElement status = ds.command(cpCmd, args, tgtDE, true);
			

			try
			{
				getStatusMonitor(ds).waitForUpdate(status, monitor);
				
				if (status.getAttribute(DE.A_SOURCE).equals(FAILED)) {
					
					String errMsg = status.getAttribute(DE.A_VALUE);
					
					/*
					// for an unexpected error, we don't have an error message from the server
					if (errMsg.equals(UNEXPECTED_ERROR)) {
						msg = SystemPlugin.getPluginMessage(MSG_ERROR_UNEXPECTED).getLevelOneText();
					}
					else {
						msg = errMsg;
					}
					
					
					throw new RemoteFileIOException(new Exception(msg));
					*/
					return false;
				}
			}
			catch (InterruptedException e)
			{
//				UniversalSystemPlugin.logError(CLASSNAME + " InterruptedException while waiting for command", e);
			}
			return true;
		}
		return false;
	}

	public boolean copyBatch(IProgressMonitor monitor, String[] srcParents, String[] srcNames, String tgtParent)
	{
		DataStore ds = getDataStore();
		
		DataElement tgtDE = getElementFor(tgtParent);
		DataElement cpCmd = getCommandDescriptor(tgtDE, C_COPY_BATCH);

		if (cpCmd != null)
		{
			ArrayList args = new ArrayList();
			for (int i = 0; i < srcParents.length; i++)
			{
				String srcRemotePath = srcParents[i] + getSeparator(srcParents[i]) + srcNames[i];
				DataElement srcDE = getElementFor(srcRemotePath);
				args.add(srcDE);
			}
			DataElement status = ds.command(cpCmd, args, tgtDE, true);

			try
			{
				getStatusMonitor(ds).waitForUpdate(status, monitor);
				
				if (status.getAttribute(DE.A_SOURCE).equals(FAILED)) {
					
					String errMsg = status.getAttribute(DE.A_VALUE);
					
					/*
					// for an unexpected error, we don't have an error message from the server
					if (errMsg.equals(UNEXPECTED_ERROR)) {
						msg = SystemPlugin.getPluginMessage(MSG_ERROR_UNEXPECTED).getLevelOneText();
					}
					else {
						msg = errMsg;
					}
					
					
					throw new RemoteFileIOException(new Exception(msg));
					*/
					return false;
				}
			}
			catch (InterruptedException e)
			{
//				UniversalSystemPlugin.logError(CLASSNAME + " InterruptedException while waiting for command", e);
			}
			return true;
		}
		return false;
	}




	
	public IHostFile[] getRoots(IProgressMonitor monitor)
	{
		if (!isInitialized())
		{
			waitForInitialize(null);
		}
		DataStore ds = getDataStore();
		DataElement universaltemp = getMinerElement();
	
		// create filter descriptor
		DataElement deObj = ds.createObject(universaltemp, UNIVERSAL_FILTER_DESCRIPTOR, "", "", "", false);
		DataElement[] results = dsQueryCommand(monitor, deObj, C_QUERY_ROOTS);
		
		return convertToHostFiles(results);
	}
	
	

	
	public IHostFile[] getFolders(IProgressMonitor monitor, String remoteParent, String fileFilter)
	{
		return fetch(monitor, remoteParent, fileFilter, C_QUERY_VIEW_FOLDERS);
	}
	
	public IHostFile[] getFiles(IProgressMonitor monitor, String remoteParent, String fileFilter)
	{
		return fetch(monitor, remoteParent, fileFilter, C_QUERY_VIEW_FILES);
	}
	
	public IHostFile[] getFilesAndFolders(IProgressMonitor monitor, String remoteParent, String fileFilter)
	{
		return fetch(monitor, remoteParent, fileFilter, C_QUERY_VIEW_ALL);
	}
	
	protected DataElement getElementFor(String path)
	{
		if (!isInitialized())
		{
			waitForInitialize(null);
		}
	
		
		String normalizedPath = PathUtility.normalizeUnknown(path);
		DataElement element = (DataElement)_fileElementMap.get(normalizedPath);
		if (element != null && element.isDeleted())
		{
			_fileElementMap.remove(normalizedPath);
			element = null;
		}
		if (element == null || element.isDeleted())
		{
			DataElement universaltemp = getMinerElement();
			element = getDataStore().createObject(universaltemp, UNIVERSAL_FILTER_DESCRIPTOR, normalizedPath, normalizedPath, "", false);
		}
		return element;
	}
	
	protected IHostFile[] fetch(IProgressMonitor monitor, String remoteParent, String fileFilter, String queryType)
	{
		DataStore ds = getDataStore();
	
	
		// create filter descriptor
		DataElement deObj = getElementFor(remoteParent);
		if (deObj == null)
		{
			DataElement universaltemp = getMinerElement();
			ds.createObject(universaltemp, UNIVERSAL_FILTER_DESCRIPTOR, remoteParent, remoteParent, "", false);
		}
		
		DataElement attributes = getAttributes(fileFilter, true);
		ArrayList args = new ArrayList(1);
		args.add(attributes);
		
		DataElement[] results = dsQueryCommand(monitor, deObj, args, queryType);		
		return convertToHostFiles(results);
	}

	public boolean isCaseSensitive()
	{
		return true;
	}



}