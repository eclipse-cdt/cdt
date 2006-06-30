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

package org.eclipse.rse.services.local.files;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.services.clientserver.ISystemFileTypes;
import org.eclipse.rse.services.clientserver.NamePatternMatcher;
import org.eclipse.rse.services.clientserver.SystemEncodingUtil;
import org.eclipse.rse.services.clientserver.archiveutils.AbsoluteVirtualPath;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler;
import org.eclipse.rse.services.clientserver.archiveutils.VirtualChild;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.AbstractFileService;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.services.local.ILocalService;

public class LocalFileService extends AbstractFileService implements IFileService, ILocalService
{
	private static final String[] ALLDRIVES =
	{
		"c:\\",
		"d:\\",
		"e:\\",
		"f:\\",
		"g:\\",
		"h:\\",
		"i:\\",
		"j:\\",
		"k:\\",
		"l:\\",
		"m:\\",
		"n:\\",
		"o:\\",
		"p:\\",
		"q:\\",
		"r:\\",
		"s:\\",
		"t:\\",
		"u:\\",
		"v:\\",
		"w:\\",
		"x:\\",
		"y:\\",
		"z:\\" };
	
	private boolean _checkedOS = false;
	private boolean _isWindows = false;
	private boolean _isWin95 = false;
	private boolean _isWinNT = false;
	private String  _osCmdShell = null;
	protected ISystemFileTypes _fileTypeRegistry;
	
	public LocalFileService(ISystemFileTypes fileTypeRegistry)
	{
		
	
		_fileTypeRegistry = fileTypeRegistry;
	}
	
	public String getName()
	{
		return LocalServiceResources.Local_File_Service_Name;
	}
	
	public String getDescription()
	{
		return LocalServiceResources.Local_File_Service_Description;
	}
	
	public boolean isWindows()
	{
		if (!_checkedOS)
		{
			String osName = System.getProperty("os.name").toLowerCase();
			_isWindows = osName.startsWith("win");
			_isWin95 = _isWindows && ((osName.indexOf("95") >= 0) || (osName.indexOf("98") >= 0) || (osName.indexOf("me") >= 0));
			_isWinNT = _isWindows && ((osName.indexOf("nt") >= 0) || (osName.indexOf("2000") >= 0) || (osName.indexOf("xp") >= 0));
			
			if (_isWinNT)
			{
				_osCmdShell = "cmd /C ";
			}
			else if (_isWin95)
			{
				_osCmdShell = "start /B ";
			}
			_checkedOS = true;
		}
		return _isWindows;
	}
	
	public class LocalFileNameFilter implements FilenameFilter
	{
		private NamePatternMatcher _matcher;
		public LocalFileNameFilter(String filter)
		{
			_matcher = new NamePatternMatcher(filter);
		}
		public boolean accept(File dir, String name) 
		{
			return _matcher.matches(name);  
		}
		public boolean isGeneric()
		{
			return _matcher.isGeneric();
		}
		
	}

	public boolean upload(IProgressMonitor monitor, InputStream stream, String remoteParent, String remoteFile, boolean isBinary, String hostEncoding) throws SystemMessageException 
	{
		boolean isCancelled = false;

		BufferedInputStream bufInputStream = null;
		FileOutputStream outputStream = null;
		BufferedOutputStream bufOutputStream = null;
		OutputStreamWriter outputWriter = null;
		BufferedWriter bufWriter = null;

		try
		{
			File destinationFile = new File(remoteParent, remoteFile);
			if (ArchiveHandlerManager.isVirtual(destinationFile.getAbsolutePath()))
			{
				VirtualChild child = ArchiveHandlerManager.getInstance().getVirtualObject(destinationFile.getAbsolutePath());
				ISystemArchiveHandler handler = child.getHandler();
				if (handler == null)
					throwCorruptArchiveException(this.getClass() + ".upload()");
				return handler.add(stream, child.path, remoteFile, SystemEncodingUtil.ENCODING_UTF_8, hostEncoding, !isBinary);
			}
			if (ArchiveHandlerManager.getInstance().isArchive(destinationFile))
			{
				ISystemArchiveHandler handler = ArchiveHandlerManager.getInstance().getRegisteredHandler(destinationFile);
				if (handler == null)
					throwCorruptArchiveException(this.getClass() + ".copyToArchive()");
				return handler.add(stream, "", remoteFile, SystemEncodingUtil.ENCODING_UTF_8, hostEncoding, !isBinary);
			}
			
			File destinationParent = destinationFile.getParentFile();
			if (!destinationParent.exists())
			{
				destinationParent.mkdirs();
			}

			bufInputStream = new BufferedInputStream(stream);
			outputStream = new FileOutputStream(destinationFile);
			
			// if encoding conversion required, then we need a writer
			boolean isEncodingConversionRequired = !isBinary;
			if (isEncodingConversionRequired) 
			{
				outputWriter = new OutputStreamWriter(outputStream, hostEncoding);
				bufWriter = new BufferedWriter(outputWriter);
			}
			else 
			{
				bufOutputStream = new BufferedOutputStream(outputStream);
			}
			

			
			byte[] buffer = new byte[512000];
			int readCount = 0;
			
			int available = bufInputStream.available();
			System.out.println("available = "+available);
			 while( (readCount = bufInputStream.read(buffer)) > 0 && !isCancelled) 
			 {
			      if (isEncodingConversionRequired) 
			      {
						String s = new String(buffer, 0, readCount, hostEncoding);
						bufWriter.write(s);
			      }
			      else 
			      {
						bufOutputStream.write(buffer, 0,readCount);
			      }
			      if (monitor != null) 
			      {
						monitor.worked(readCount);
						isCancelled = monitor.isCanceled();
			      }
			 }
	
		}
		catch (FileNotFoundException e)
		{
		}
		catch (UnsupportedEncodingException e)
		{
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{

			try
			{
				if (bufWriter != null)
					bufWriter.close();

				if (bufInputStream != null)
					bufInputStream.close();

				if (bufOutputStream != null)
					bufOutputStream.close();

				if (isCancelled)
				{
				//	throw new RemoteFileCancelledException();
					return false;
				}
			}
			catch (IOException e)
			{
			}
		}
		return true;
	}



	public boolean download(IProgressMonitor monitor, String remoteParent, String remoteFile, File destinationFile, boolean isBinary, String hostEncoding) throws SystemMessageException 
	{
		File file = new File(remoteParent, remoteFile);
		FileInputStream inputStream = null;
		BufferedInputStream bufInputStream = null;
		FileOutputStream outputStream = null;
		BufferedOutputStream bufOutputStream = null;
		OutputStreamWriter outputWriter = null;
		BufferedWriter bufWriter = null;
		boolean isCancelled = false;
		
		boolean sourceIsVirtual = ArchiveHandlerManager.isVirtual(file.getAbsolutePath());
		boolean targetIsVirtual = ArchiveHandlerManager.isVirtual(destinationFile.getParent());
		boolean targetIsArchive = ArchiveHandlerManager.getInstance().isArchive(destinationFile.getParentFile());
		if (sourceIsVirtual)
		{
			return copyFromArchive(file, destinationFile.getParentFile(), destinationFile.getName(), monitor, hostEncoding, SystemEncodingUtil.ENCODING_UTF_8, !isBinary);
		}
		if (targetIsVirtual || targetIsArchive)
		{
			return copyToArchive(file, destinationFile.getParentFile(), destinationFile.getName(), monitor, hostEncoding, SystemEncodingUtil.ENCODING_UTF_8, !isBinary);
		}
		
		try
		{
	

			if (!destinationFile.exists())
			{
				File parentDir = destinationFile.getParentFile();
				parentDir.mkdirs();
			}
		

			
			// encoding conversion required if it a text file but not an xml file
			boolean isEncodingConversionRequired = !isBinary;
			
			inputStream = new FileInputStream(file);
			bufInputStream = new BufferedInputStream(inputStream);
			outputStream = new FileOutputStream(destinationFile);
			
			if (isEncodingConversionRequired) 
			{
				outputWriter = new OutputStreamWriter(outputStream, hostEncoding);
				bufWriter = new BufferedWriter(outputWriter);				
			}
			else 
			{
				bufOutputStream = new BufferedOutputStream(outputStream);
			}


			byte[] buffer = new byte[512000];
			long totalSize = file.length();
			int totalRead = 0;

			while (totalRead < totalSize && !isCancelled) 
			{
				
				int available = bufInputStream.available();
				available = (available < 512000) ? available : 512000;

				int bytesRead = bufInputStream.read(buffer, 0, available);

				if (bytesRead == -1) {
					break;
				}
				
				// need to convert encoding, i.e. text file, but not xml
				// ensure we read in file using the encoding for the file system
				// which can be specified by user as text file encoding in preferences
				if (isEncodingConversionRequired) 
				{
					String s = new String(buffer, 0, bytesRead, hostEncoding);
					bufWriter.write(s);
				}
				else 
				{
					bufOutputStream.write(buffer, 0, bytesRead);					
				}

				totalRead += bytesRead;
					
				if (monitor != null) 
				{
					monitor.worked(bytesRead);
					isCancelled = monitor.isCanceled();
				}
			}
		}
		catch (FileNotFoundException e)
		{
//			SystemPlugin.logError("Local copy: " + file.getAbsolutePath(), e);
//			throw new RemoteFileIOException(e);
			return false;
		}
		catch (UnsupportedEncodingException e)
		{
//			SystemPlugin.logError("Local copy: " + file.getAbsolutePath(), e);
//			throw new RemoteFileIOException(e);
			return false;
		}
		catch (IOException e)
		{
	//		SystemPlugin.logError("Local copy: " + file.getAbsolutePath(), e);
	//		throw new RemoteFileIOException(e);
			return false;
		}
		finally
		{

			try
			{
				if (bufWriter != null)
					bufWriter.close();

				if (bufInputStream != null)
					bufInputStream.close();

				if (bufOutputStream != null)
					bufOutputStream.close();

				if (isCancelled)
				{
			//		throw new RemoteFileCancelledException();
					return false;
				} else if (destinationFile!=null && file.exists()) {
					destinationFile.setLastModified(file.lastModified());
					//TODO check if we want to preserve permissions
					//if(!file.canWrite()) destinationFile.setReadOnly();
					if (destinationFile.length() != file.length()) {
						//	throw new RemoteFileCancelledException();
						System.err.println("local.upload: size mismach on "+destinationFile.getAbsolutePath());
						return false;
					}
				}
			}
			catch (IOException e)
			{
			//	SystemPlugin.logError("Closing streams: " + file.getAbsolutePath(), e);
			//	throw new RemoteFileIOException(e);
			}
		}
		return true;
	}

	private boolean copyToArchive(File file, File destination, String newName, IProgressMonitor monitor, String sourceEncoding, String targetEncoding, boolean isText) throws SystemMessageException 
	{
		boolean ok = false;
		if (ArchiveHandlerManager.isVirtual(destination.getAbsolutePath()))
		{
			VirtualChild virtualChild = ArchiveHandlerManager.getInstance().getVirtualObject(destination.getAbsolutePath());
			String path = virtualChild.fullName;
			if (!virtualChild.isDirectory)
			{
				path = virtualChild.path;
			}
			ISystemArchiveHandler handler = virtualChild.getHandler();
			if (handler == null)
				throwCorruptArchiveException(this.getClass() + ".copyToArchive()");
			if (file.isDirectory())
			{
			    ok = handler.add(file, path, newName, sourceEncoding, targetEncoding, _fileTypeRegistry);
			}
			else
			{
				ok = handler.add(file, path, newName, sourceEncoding, targetEncoding, isText);
			}
		}
		else if (ArchiveHandlerManager.getInstance().isArchive(destination))
		{
			ISystemArchiveHandler handler = ArchiveHandlerManager.getInstance().getRegisteredHandler(destination);
			if (handler == null)
				throwCorruptArchiveException(this.getClass() + ".copyToArchive()");
			if (file.isDirectory())
			{
			    ok = handler.add(file, "", newName, sourceEncoding, targetEncoding, _fileTypeRegistry);			    
			}
			else
			{
				ok = handler.add(file, "", newName, sourceEncoding, targetEncoding, isText);
			}			    
		}
		if (!ok)
		{
			// SystemPlugin.logError("LocalFileSubSystemImpl.copyToArchive(): Handler's add() method returned false.");
			SystemMessage msg = getMessage("RSEF5006");
			msg.makeSubstitution(destination.getName(), "localhost");
			throw new SystemMessageException(msg);
		}
		else
			return true;
	}

	public boolean upload(IProgressMonitor monitor, File localFile, String remoteParent, String remoteFile, boolean isBinary, String srcEncoding, String hostEncoding) throws SystemMessageException
	{
		boolean isCancelled = false;
		FileInputStream inputStream = null;
		BufferedInputStream bufInputStream = null;
		FileOutputStream outputStream = null;
		BufferedOutputStream bufOutputStream = null;
		OutputStreamWriter outputWriter = null;
		BufferedWriter bufWriter = null;
		File destinationFile = null;
		
		File target = new File(remoteParent, remoteFile);
		boolean sourceIsVirtual = ArchiveHandlerManager.isVirtual(localFile.getAbsolutePath());
		boolean targetIsVirtual = ArchiveHandlerManager.isVirtual(target.getAbsolutePath());
		if (sourceIsVirtual)
		{
			return copyFromArchive(localFile, target, remoteFile, monitor, srcEncoding, hostEncoding, !isBinary);
		}
		if (targetIsVirtual)
		{
			return copyToArchive(localFile, target, remoteFile, monitor, srcEncoding, hostEncoding, !isBinary);
		}

		try
		{
			destinationFile = new File(remoteParent, remoteFile);
			int totalSize = (int) localFile.length();

			File destinationParent = destinationFile.getParentFile();
			if (!destinationParent.exists())
			{
				destinationParent.mkdirs();
			}

			inputStream = new FileInputStream(localFile);
			bufInputStream = new BufferedInputStream(inputStream);
			outputStream = new FileOutputStream(destinationFile);
			
			// if encoding conversion required, then we need a writer
			boolean isEncodingConversionRequired = !isBinary && !srcEncoding.equals(hostEncoding);
			if (isEncodingConversionRequired) 
			{
				outputWriter = new OutputStreamWriter(outputStream, hostEncoding);
				bufWriter = new BufferedWriter(outputWriter);
			}
			else 
			{
				bufOutputStream = new BufferedOutputStream(outputStream);
			}

			byte[] buffer = new byte[512000];
			int totalRead = 0;

			while (totalRead < totalSize && !isCancelled) {

				int available = bufInputStream.available();
				available = (available < 512000) ? available : 512000;

				int bytesRead = bufInputStream.read(buffer, 0, available);

				if (bytesRead == -1) {
					break;
				}

				if (isEncodingConversionRequired) {
					String s = new String(buffer, 0, bytesRead, srcEncoding);
					bufWriter.write(s);
				}
				else {
					bufOutputStream.write(buffer, 0, bytesRead);
				}

				totalRead += bytesRead;
				
				if (monitor != null) {
					monitor.worked(bytesRead);
					isCancelled = monitor.isCanceled();
				}
			}
		}
		catch (FileNotFoundException e)
		{
		}
		catch (UnsupportedEncodingException e)
		{
		}
		catch (IOException e)
		{
		}
		finally
		{

			try
			{
				if (bufWriter != null)
					bufWriter.close();

				if (bufInputStream != null)
					bufInputStream.close();

				if (bufOutputStream != null)
					bufOutputStream.close();

				if (isCancelled)
				{
				//	throw new RemoteFileCancelledException();
					return false;
				} else if (destinationFile!=null) {
					destinationFile.setLastModified(localFile.lastModified());
					//TODO check if we want to preserve permissions
					//if(!localFile.canWrite()) destinationFile.setReadOnly();
					if (destinationFile.length() != localFile.length()) {
						//	throw new RemoteFileCancelledException();
						System.err.println("local.upload: size mismach on "+destinationFile.getAbsolutePath());
						return false;
					}
				}
			}
			catch (IOException e)
			{
			}
		}
		return true;
	}
	
	protected IHostFile[] internalFetch(IProgressMonitor monitor, String remoteParent, String fileFilter, int type)
	{
		LocalFileNameFilter fFilter = new LocalFileNameFilter(fileFilter);
	
		File localParent = new File(remoteParent);

		if (remoteParent.endsWith(ArchiveHandlerManager.VIRTUAL_SEPARATOR))
		{
			remoteParent = remoteParent.substring(0, remoteParent.length() - ArchiveHandlerManager.VIRTUAL_SEPARATOR.length());
		}
		boolean isVirtual = ArchiveHandlerManager.isVirtual(remoteParent);
		boolean isArchive = ArchiveHandlerManager.getInstance().isArchive(localParent);
		if (isVirtual || isArchive)
		{
			try
			{
				VirtualChild[] contents = null;
				
				File theFile = getContainingArchive(localParent);
				
				if (isArchive)
				{
					contents = ArchiveHandlerManager.getInstance().getContents(localParent, "");
				}
				else if (isVirtual)
				{
					AbsoluteVirtualPath avp = new AbsoluteVirtualPath(remoteParent);
					contents = ArchiveHandlerManager.getInstance().getContents(theFile, avp.getVirtualPart());
				}
				
				if (contents == null)
				{
					return null;
				}
	
				IHostFile[] results = new LocalVirtualHostFile[contents.length];
	
				for (int i = 0; i < contents.length; i++)
				{
					results[i] = new LocalVirtualHostFile(contents[i]);
				}			
				return results;
			}
			catch (IOException e)
			{
				// FIXME: Do something!
				return null;
			}
		}
		else
		{
			if (!fFilter.isGeneric())
			{
				File file = new File(localParent, fileFilter);
				return convertToHostFiles(new File[] { file }, type);
			}
			if (localParent.exists())
			{		
				File[] files = localParent.listFiles(fFilter);
				return convertToHostFiles(files, type);
			}
			else
			{
				return new IHostFile[0];
			}
		}
	}
	
	protected IHostFile[] convertToHostFiles(File[] files, int type)
	{
		List results = new ArrayList();
		if (files != null)
		{
			for (int i = 0; i < files.length; i++)
			{
				File file = files[i];
				if (file.isDirectory())
				{
					if (type == FILE_TYPE_FILES_AND_FOLDERS || 
					    type == FILE_TYPE_FOLDERS)
					{
						results.add(new LocalHostFile(file));
					}
				}
				else if (file.isFile())
				{
					if (type == FILE_TYPE_FILES_AND_FOLDERS || 
						type == FILE_TYPE_FILES)
					{
						results.add(new LocalHostFile(file));
					}
				}
				else if (!file.exists())
				{
					results.add(new LocalHostFile(file));
				}
			}
		}
		return (IHostFile[])results.toArray(new IHostFile[results.size()]);
	}

	public IHostFile getUserHome() 
	{
		String userHome  =System.getProperty("user.home");
		File userHomeFile = new File(userHome);		
		return new LocalHostFile(userHomeFile);
	}



	public IHostFile[] getRoots(IProgressMonitor monitor) 
	{
		List v = new ArrayList();
		if (isWindows())
		{
			for (int idx = 0; idx < ALLDRIVES.length; idx++)
			{
				File drive = new File(ALLDRIVES[idx]);
				if (drive.exists())
				
					try 
					{
						v.add(drive.getAbsoluteFile());
						//v.addElement(drive.getCanonicalFile());
					}
					catch (Exception e) 
					{
					}
			}
		}
		else
		{
			v.add(new File("/"));
		}
		
		IHostFile[] fileObjs = new LocalHostFile[v.size()];
		for (int idx = 0; idx < v.size(); idx++)
		{
			fileObjs[idx] = new LocalHostFile((File) v.get(idx), true);
		}
		
		return fileObjs;
	}



	public IHostFile getFile(IProgressMonitor monitor, String remoteParent, String name) 
	{
		if (name.endsWith(ArchiveHandlerManager.VIRTUAL_SEPARATOR))
		{
			name = name.substring(0, name.length() - ArchiveHandlerManager.VIRTUAL_SEPARATOR.length());
		}
		boolean isVirtualParent = ArchiveHandlerManager.isVirtual(remoteParent);
		boolean isArchiveParent = ArchiveHandlerManager.getInstance().isArchive(new File(remoteParent));
		if (!isVirtualParent && !isArchiveParent)
		{
			File file = null;
			if (remoteParent == null)
			{
				file = new File(name);
			}
			else
			{
				file = new File(remoteParent, name);
			}
			File parent = file.getParentFile();
			return new LocalHostFile(file);
		}
		else
		{
			String fullpath = remoteParent;
			if (isArchiveParent)
			{
				fullpath = fullpath + ArchiveHandlerManager.VIRTUAL_SEPARATOR;
			}
			else
			{
				fullpath = fullpath + "/";
			}
			LocalVirtualHostFile results;
			VirtualChild vc = ArchiveHandlerManager.getInstance().getVirtualObject(fullpath + name);
			results = new LocalVirtualHostFile(vc);
			return results;
		}
	}



	public IHostFile createFile(IProgressMonitor monitor, String remoteParent, String fileName) throws SystemMessageException
	{
		File parentFile = new File(remoteParent);
		File fileToCreate = new File(parentFile, fileName);
		if (!fileToCreate.exists())
		{
			if (ArchiveHandlerManager.isVirtual(fileToCreate.getAbsolutePath()))
			{
				return createFileInArchive(fileToCreate);
			}
			else if (!parentFile.exists())
			{
				parentFile.mkdirs();
			}
			else 
			{
				try
				{
					fileToCreate.createNewFile();
					if (ArchiveHandlerManager.getInstance().isArchive(fileToCreate))
					{
						if (!ArchiveHandlerManager.getInstance().createEmptyArchive(fileToCreate))
						{
							// SystemPlugin.logError("LocalFileSubSystemImpl.createFile(): HandlerManager's createEmptyArchive() method returned false.");
							throw new SystemMessageException(getMessage("RSEG1122"));
						}
					}
				}
				catch (Exception e)
				{				
				}
			}
		}	
		return new LocalHostFile(fileToCreate);
	}

	protected LocalVirtualHostFile createFileInArchive(File newFile) throws SystemMessageException
	{
		VirtualChild child = ArchiveHandlerManager.getInstance().getVirtualObject(newFile.getAbsolutePath());
		ISystemArchiveHandler handler = child.getHandler();
		if (handler == null)
			throwCorruptArchiveException(this.getClass() + ".createFileInArchive()");
		if (!handler.createFile(child.fullName))
		{
			//SystemPlugin.logError("LocalFileSubSystemImpl.createFileInArchive(): Archive Handler's createFile method returned false. Couldn't create virtual object.");
			throw new SystemMessageException(getMessage("RSEG1124").makeSubstitution(newFile));
		}
		return new LocalVirtualHostFile(child);
	}
	
	private void throwCorruptArchiveException(String classAndMethod) throws SystemMessageException
	{
		// SystemPlugin.logError(classAndMethod + ": Couldn't instantiate archive handler. Archive could be corrupted.");
		throw new SystemMessageException(getMessage("RSEG1122"));
	}
	
	protected File getContainingArchive(File file)
	{
		String absPath = file.getAbsolutePath();
		AbsoluteVirtualPath avp = new AbsoluteVirtualPath(absPath);
		return new File(avp.getContainingArchiveString());
	}
	
	protected String getVirtualPart(String absPath)
	{
		AbsoluteVirtualPath avp = new AbsoluteVirtualPath(absPath);
		return avp.getVirtualPart();
	}

	public IHostFile createFolder(IProgressMonitor monitor, String remoteParent, String folderName) throws SystemMessageException
	{
		File folderToCreate = new File(remoteParent, folderName);
		if (!folderToCreate.exists())
		{
			if (ArchiveHandlerManager.isVirtual(folderToCreate.getAbsolutePath()))
			{
				return createFolderInArchive(folderToCreate);
			}
			else
			{
				folderToCreate.mkdirs();
			}
		}
		return new LocalHostFile(folderToCreate);
	}

	protected LocalVirtualHostFile createFolderInArchive(File newFolder) throws SystemMessageException
	{
		VirtualChild child = ArchiveHandlerManager.getInstance().getVirtualObject(newFolder.getAbsolutePath());
		ISystemArchiveHandler handler = child.getHandler();
		if (handler == null)
			throwCorruptArchiveException(this.getClass() + ".createFolderInArchive()");
		if (!handler.createFolder(child.fullName))
		{
			// SystemPlugin.logError("LocalFileSubSystemImpl.createFolderInArchive(): Archive Handler's createFolder method returned false. Couldn't create virtual object.");
			throw new SystemMessageException(getMessage("RSEG1124").makeSubstitution(newFolder));
		}
		return new LocalVirtualHostFile(child);
	}

	public boolean delete(IProgressMonitor monitor, String remoteParent, String fileName) throws SystemMessageException
	{
		if (fileName.endsWith(ArchiveHandlerManager.VIRTUAL_SEPARATOR))
		{
			fileName = fileName.substring(0, fileName.length() - ArchiveHandlerManager.VIRTUAL_SEPARATOR.length());
		}
		File fileToDelete = new File(remoteParent, fileName);
		if (ArchiveHandlerManager.isVirtual(fileToDelete.getAbsolutePath()))
		{
			return deleteFromArchive(fileToDelete);
		}
		else if (ArchiveHandlerManager.getInstance().isArchive(fileToDelete))
		{
			return deleteArchive(fileToDelete);
		}
		if (fileToDelete.isDirectory())
		{
			return deleteContents(monitor, fileToDelete);
		}
		else
		{
			return fileToDelete.delete();
		}
	}
	
	private boolean deleteContents(IProgressMonitor monitor, File folder)
	{
		boolean result = true;
		File[] files = folder.listFiles();
		for (int i = 0; i < files.length && result; i++)
		{
			File file = files[i];
			if (file.isDirectory())
			{
				result = deleteContents(monitor, file);
			}
			else
			{
				result = file.delete();
			}
		}
		if (result)
		{
			result = folder.delete();
		}
		return result;
	}

	/**
	 * Deletes a virtual file from its archive.
	 * 
	 * @param destination virtual file to delete from archive
	 */
	protected boolean deleteFromArchive(File destination) throws SystemMessageException
	{
		VirtualChild child = ArchiveHandlerManager.getInstance().getVirtualObject(destination.getAbsolutePath());
		ISystemArchiveHandler handler = child.getHandler();
		if (handler == null)
			throwCorruptArchiveException(this.getClass() + ".deleteFromArchive()");
		if (!handler.delete(child.fullName))
		{
			// SystemPlugin.logError("LocalFileSubSystemImpl.deleteFromArchive(): Archive Handler's delete method returned false. Couldn't delete virtual object.");
			throw new SystemMessageException(getMessage("RSEG1125").makeSubstitution(destination));
		}
		return true;
	}
	
	protected boolean deleteArchive(File file)
	{
		ArchiveHandlerManager.getInstance().disposeOfRegisteredHandlerFor(file);
		return file.delete();
	}

	public boolean rename(IProgressMonitor monitor, String remoteParent, String oldName, String newName) throws SystemMessageException
	{
		File fileToRename = new File(remoteParent, oldName);
		if (ArchiveHandlerManager.isVirtual(fileToRename.getAbsolutePath()))
		{
			return renameVirtualFile(fileToRename, newName);
		}
		File newFile = new File(remoteParent, newName);
		return fileToRename.renameTo(newFile);
	}
	
	public boolean rename(IProgressMonitor monitor, String remoteParent, String oldName, String newName, IHostFile oldFile) throws SystemMessageException
	{
		boolean retVal = rename(monitor, remoteParent, oldName, newName);
		File newFile = new File(remoteParent, newName);
		oldFile.renameTo(newFile.getAbsolutePath());
		return retVal;
	}

	/**
	 * Renames a virtual file
	 * 
	 * @param destination virtual file to rename
	 * @param newName the new name of the virtual file
	 * @return
	 */
	protected boolean renameVirtualFile(File destination, String newName) throws SystemMessageException
	{
		VirtualChild child = ArchiveHandlerManager.getInstance().getVirtualObject(destination.getAbsolutePath());
		ISystemArchiveHandler handler = child.getHandler();
		if (handler == null)
			throwCorruptArchiveException(this.getClass() + ".renameVirtualFile()");
		boolean retval = handler.rename(child.fullName, newName);
		if (!retval)
		{
			// SystemPlugin.logError("LocalFileSubSystemImpl.renameVirtualFile(): Archive Handler's rename method returned false. Couldn't rename virtual object.");
			throw new SystemMessageException(getMessage("RSEG1127").makeSubstitution(child.fullName));
		}
		else return retval;
	}

	public boolean move(IProgressMonitor monitor, String srcParent, String srcName, String tgtParent, String tgtName) throws SystemMessageException 
	{
		File sourceFolderOrFile = new File(srcParent, srcName);
		File targetFolder = new File(tgtParent, tgtName);
		boolean sourceIsVirtual = ArchiveHandlerManager.isVirtual(sourceFolderOrFile.getAbsolutePath());
		boolean targetIsVirtual = ArchiveHandlerManager.isVirtual(targetFolder.getAbsolutePath());
		boolean targetIsArchive = ArchiveHandlerManager.getInstance().isArchive(targetFolder);
		if (sourceIsVirtual || targetIsVirtual || targetIsArchive)
				/* DKM
				 * we shouldn't be moving archives like virtuals
				 *|| ArchiveHandlerManager.getInstance().isRegisteredArchive(newName)
				 *
				 */
		{
			if (copy(monitor, srcParent, srcName, tgtParent, tgtName))
			{
				return delete(monitor, srcParent, srcName);
			}
			else return false;
		}
		else
		{
			File fileToMove = new File(srcParent, srcName);
			File newFile = new File(tgtParent, tgtName);
			return fileToMove.renameTo(newFile);
		}
	}



	public boolean copy(IProgressMonitor monitor, String srcParent, String srcName, String tgtParent, String tgtName) throws SystemMessageException 
	{
		File srcFile = new File(srcParent, srcName);
		File tgtFile = new File(tgtParent, tgtName);
		
		String command = null;
		boolean folderCopy = srcFile.isDirectory();
		String src = srcFile.getAbsolutePath();
		

		String target = tgtFile.getAbsolutePath();

		boolean sourceIsVirtual = ArchiveHandlerManager.isVirtual(src);
		boolean targetIsVirtual = ArchiveHandlerManager.isVirtual(target);
		boolean targetIsArchive = ArchiveHandlerManager.getInstance().isArchive(new File(tgtParent));
		if (sourceIsVirtual)
		{
			return copyFromArchive(srcFile, new File(tgtParent), tgtName, monitor, SystemEncodingUtil.ENCODING_UTF_8, SystemEncodingUtil.ENCODING_UTF_8, false);
		}
		if (targetIsVirtual || targetIsArchive)
		{
			return copyToArchive(srcFile, new File(tgtParent), tgtName, monitor, SystemEncodingUtil.ENCODING_UTF_8, SystemEncodingUtil.ENCODING_UTF_8, false);
		}
		
		
//		 handle special characters in source and target strings 
		StringBuffer srcBuf = new StringBuffer(src);
		StringBuffer tgtBuf = new StringBuffer(target);
		handleSpecialChars(srcBuf);
		handleSpecialChars(tgtBuf);
		
		src = "\"" + srcBuf.toString() + "\"";
		target = "\"" + tgtBuf.toString() + "\"";
		/*
		// handle imbedded blanks of from or to name...
		if (src.indexOf(' ') >= 0)
			src = "\"" + src + "\"";
		if (target.indexOf(' ') >= 0)
			target = "\"" + target + "\"";
		*/
		boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("win");
		if (isWindows)
		{
			if (folderCopy)
			{
				command = "xcopy " + src + " " + target + " /S /E /K /Q /H /I";
			}
			else
				command = _osCmdShell + "copy " + src + " " + target;
		}
		else
		{
			
			if (folderCopy)
			{
				command = "cp  -r " + src + " " + target;
			}
			else // it is a file 	 
			{
				command = "cp " + src + " " + target;
			}
		}
		int rc = -1;
		try
		{
			Process p = null;
			Runtime runtime = Runtime.getRuntime();
			if (isWindows)
			{
				String theShell = "cmd /C ";
				p = runtime.exec(theShell + command);	
			}
			else
			{
				String theShell = "sh";
				String args[] = new String[3];
				args[0] = theShell;					
				args[1] = "-c";
				args[2] = command;
												
				p = runtime.exec(args);
			}
			
			//Process p = Runtime.getRuntime().exec(command);
			rc = p.waitFor();

			//rc = p.exitValue();
		}
		catch (Exception e)
		{
			
		}
		return (rc == 0);
	}
	
	protected void handleSpecialChars(StringBuffer buf)
	{
		for (int i = 0; i < buf.length(); i++)
		{
			char c = buf.charAt(i);
		
			boolean isSpecialChar = isSpecialChar(c);
		
			if (isSpecialChar)
			{
				buf.insert(i, "\\");
				i++;
			}
		}
	}
	
	/**
	 * Checks whether the given character is a special character in the shell. A special character is
	 * '$', '`', '"' and '\'.
	 * @param c the character to check.
	 * @return <code>true</code> if the character is a special character, <code>false</code> otherwise.
	 */
	protected boolean isSpecialChar(char c)  {
		   
		if ((c == '$') || (c == '`') || (c == '"') || (c == '\\') ) {
						
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Copy a file or folder to a new target parent folder, but if 
	 * copying from an archive, extract the file in the encoding specified
	 * 
	 * @param sourceFolderOrFile The file or folder to copy
	 * @param targetFolder The folder to copy to. No guarantee it is on the same system, so be sure to check getSystemConnection()!
	 * @param newName The new name for the copied file or folder
	 * @param encoding The encoding for the file once it is extracted from the archive
	 * @return true iff the copy succeeded
	 */
	public boolean copyFromArchive(File sourceFolderOrFile, File targetFolder, String newName, IProgressMonitor monitor, String sourceEncoding, String targetEncoding, boolean isText) throws SystemMessageException
	{
		if (sourceEncoding == null) sourceEncoding = SystemEncodingUtil.ENCODING_UTF_8;
		if (sourceEncoding == null) isText = _fileTypeRegistry.isText(sourceFolderOrFile);
		if (!(ArchiveHandlerManager.isVirtual(sourceFolderOrFile.getAbsolutePath()))) return false;
		String command = null;
		boolean folderCopy = sourceFolderOrFile.isDirectory();
		String src = sourceFolderOrFile.getAbsolutePath();
		VirtualChild child = ArchiveHandlerManager.getInstance().getVirtualObject(sourceFolderOrFile.getAbsolutePath());
		if (!(ArchiveHandlerManager.isVirtual(targetFolder.getAbsolutePath())) && !ArchiveHandlerManager.getInstance().isArchive(targetFolder))
		{
			// this is an optimization to speed up extractions from large zips. Instead of
			// extracting to a temp location and then copying the temp files to the target location
			// we simply instruct the handler to extract to the target location.
			return child.getExtractedFile(new File(targetFolder, child.name), sourceEncoding, isText);
		}
		
		src = child.getExtractedFile(sourceEncoding, isText).getAbsolutePath();

		if (child.isDirectory)
		{
			File tempSource = null;
			try
			{
				tempSource = File.createTempFile(child.name, "virtual");
				tempSource.deleteOnExit();
			}
			catch (IOException e)
			{
				// SystemPlugin.logError("LocalFileSubSystemImpl.copy(): Could not create temp file.", e);
				throw new SystemMessageException(getMessage("Copy failed"));
			}
			tempSource.delete();
			if (!tempSource.mkdir())
			{
				// SystemPlugin.logError("LocalFileSubSystemImpl.copy(): Couldn't create temp dir.");
				throw new SystemMessageException(getMessage("RSEG1306").makeSubstitution(sourceFolderOrFile));
			}
			ISystemArchiveHandler handler = child.getHandler();
			if (handler == null)
				throwCorruptArchiveException(this.getClass() + ".copy()");
			handler.extractVirtualDirectory(child.fullName, tempSource, sourceEncoding, isText);
			src = tempSource.getAbsolutePath() + File.separatorChar + child.name;
		}
		if (ArchiveHandlerManager.isVirtual(targetFolder.getAbsolutePath()) || ArchiveHandlerManager.getInstance().isArchive(targetFolder))
		{
			File source = new File(src);
			return copyToArchive(source, targetFolder, newName, monitor, SystemEncodingUtil.ENCODING_UTF_8, targetEncoding, isText);
		}

		String target = targetFolder.getAbsolutePath() + java.io.File.separator + newName;
		// handle embedded blanks of from or to name...
		if (src.indexOf(' ') >= 0)
			src = "\"" + src + "\"";
		if (target.indexOf(' ') >= 0)
			target = "\"" + target + "\"";
		if (System.getProperty("os.name").toLowerCase().startsWith("win"))
		{
			if (folderCopy)
			{
				command = "xcopy " + src + " " + target + " /S /E /K /Q /H /I";
			}
			else
				command = _osCmdShell + "copy " + src + " " + target;
		}
		else
		{
			if (folderCopy)
			{
				command = "cp  -r " + src + " " + target;
			}
			else // it is a file 	 
			{
				command = "cp " + src + " " + target;
			}
		}
		int rc = -1;
		try
		{
			Process p = Runtime.getRuntime().exec(command);
			rc = p.exitValue();
		}
		catch (Exception e)
		{
			
		}
		return (rc == 0);	
	}
	
	public void initService(IProgressMonitor monitor)
	{
		
	}
	
	public void uninitService(IProgressMonitor monitor)
	{
	}
	
	public boolean isCaseSensitive()
	{
		return true;
	}

	public boolean copyBatch(IProgressMonitor monitor, String[] srcParents, String[] srcNames, String tgtParent) throws SystemMessageException 
	{
		boolean ok = true;
		for (int i = 0; i < srcParents.length; i++)
		{
			ok = ok && copy(monitor, srcParents[i], srcNames[i], tgtParent, srcNames[i]);
		}
		return ok;
	}

	
}