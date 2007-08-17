/********************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others. All rights reserved.
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
 * Javier Montalvo Orús (Symbian) - patch for bug 163103 - NPE in filters
 * Martin Oberhuber (Wind River) - fix 168586 - isCaseSensitive() on Windows
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Kevin Doyle (IBM) - [182221] Throwing Proper Exceptions on create file/folder
 * Xuan Chen (IBM) - Fix 189487 - copy and paste a folder did not work - workbench hang
 * David McKnight (IBM) - [192705] Exception needs to be thrown when rename fails
 * Kevin Doyle (IBM) - [196211] Move a folder to a directory that contains a folder by that name errors
 * Martin Oberhuber (Wind River) - [199394] Allow real files/folders containing String #virtual#
 * Martin Oberhuber (Wind River) - [199548] Avoid touching files on setReadOnly() if unnecessary
 * Kevin Doyle (IBM) - [199871] LocalFileService needs to implement getMessage()
 ********************************************************************************/

package org.eclipse.rse.internal.services.local.files;

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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.internal.services.local.ILocalService;
import org.eclipse.rse.internal.services.local.LocalServiceResources;
import org.eclipse.rse.services.clientserver.FileTypeMatcher;
import org.eclipse.rse.services.clientserver.IMatcher;
import org.eclipse.rse.services.clientserver.ISystemFileTypes;
import org.eclipse.rse.services.clientserver.NamePatternMatcher;
import org.eclipse.rse.services.clientserver.SystemEncodingUtil;
import org.eclipse.rse.services.clientserver.archiveutils.AbsoluteVirtualPath;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler;
import org.eclipse.rse.services.clientserver.archiveutils.VirtualChild;
import org.eclipse.rse.services.clientserver.messages.ISystemMessageProvider;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.AbstractFileService;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.services.files.RemoteFileException;
import org.eclipse.rse.services.files.RemoteFileIOException;
import org.eclipse.rse.services.files.RemoteFileSecurityException;

public class LocalFileService extends AbstractFileService implements IFileService, ILocalService
{
	private static final String[] ALLDRIVES =
	{
		"C:\\", //$NON-NLS-1$
		"D:\\", //$NON-NLS-1$
		"E:\\", //$NON-NLS-1$
		"F:\\", //$NON-NLS-1$
		"G:\\", //$NON-NLS-1$
		"H:\\", //$NON-NLS-1$
		"I:\\", //$NON-NLS-1$
		"J:\\", //$NON-NLS-1$
		"K:\\", //$NON-NLS-1$
		"L:\\", //$NON-NLS-1$
		"M:\\", //$NON-NLS-1$
		"N:\\", //$NON-NLS-1$
		"O:\\", //$NON-NLS-1$
		"P:\\", //$NON-NLS-1$
		"Q:\\", //$NON-NLS-1$
		"R:\\", //$NON-NLS-1$
		"S:\\", //$NON-NLS-1$
		"T:\\", //$NON-NLS-1$
		"U:\\", //$NON-NLS-1$
		"V:\\", //$NON-NLS-1$
		"W:\\", //$NON-NLS-1$
		"X:\\", //$NON-NLS-1$
		"Y:\\", //$NON-NLS-1$
		"Z:\\" }; //$NON-NLS-1$
	
	private boolean _checkedOS = false;
	private boolean _isWindows = false;
	private boolean _isWin95 = false;
	private boolean _isWinNT = false;
	private String  _osCmdShell = null;
	
	protected ISystemFileTypes _fileTypeRegistry;
	protected ISystemMessageProvider _msgProvider;
	
	public LocalFileService(ISystemFileTypes fileTypeRegistry)
	{
		_fileTypeRegistry = fileTypeRegistry;
	}
	
	public LocalFileService (ISystemFileTypes fileTypeRegistry, ISystemMessageProvider msgProvider) {
		this(fileTypeRegistry);
		_msgProvider = msgProvider;
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
			String osName = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$
			_isWindows = osName.startsWith("win"); //$NON-NLS-1$
			_isWin95 = _isWindows && ((osName.indexOf("95") >= 0) || (osName.indexOf("98") >= 0) || (osName.indexOf("me") >= 0)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			_isWinNT = _isWindows && ((osName.indexOf("nt") >= 0) || (osName.indexOf("2000") >= 0) || (osName.indexOf("xp") >= 0)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			
			if (_isWinNT)
			{
				_osCmdShell = "cmd /C "; //$NON-NLS-1$
			}
			else if (_isWin95)
			{
				_osCmdShell = "start /B "; //$NON-NLS-1$
			}
			_checkedOS = true;
		}
		return _isWindows;
	}
	
	public class LocalFileNameFilter implements FilenameFilter {
		private IMatcher _matcher;
		private int type;

		public LocalFileNameFilter(String filter, int type) {
			if (filter == null) {
				filter = "*"; //$NON-NLS-1$
			}
			if (filter.endsWith(",")) { //$NON-NLS-1$
				String[] types = filter.split(","); //$NON-NLS-1$
				_matcher = new FileTypeMatcher(types);
			} else {
				_matcher = new NamePatternMatcher(filter);
			}
			this.type = type;
		}

		public boolean accept(File dir, String name) {
			boolean result = false;
			File entry = new File(dir, name);
			if (entry.exists()) {
				if (entry.isFile()) {
					result = _matcher.matches(name);
				} else if (entry.isDirectory()) {
					if (type == FILE_TYPE_FILES_AND_FOLDERS || type == FILE_TYPE_FOLDERS) {
						result = true;
					}
				}
			}
			return result;
		}

		public boolean isGeneric() {
			boolean result = true;
			if (_matcher instanceof NamePatternMatcher) {
				NamePatternMatcher new_name = (NamePatternMatcher) _matcher;
				result = new_name.isGeneric();
			}
			return result;
		}
		
	}

	public boolean upload(InputStream stream, String remoteParent, String remoteFile, boolean isBinary, String hostEncoding, IProgressMonitor monitor) throws SystemMessageException 
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
					throwCorruptArchiveException(this.getClass() + ".upload()"); //$NON-NLS-1$
				else 
					return handler.add(stream, child.path, remoteFile, SystemEncodingUtil.ENCODING_UTF_8, hostEncoding, !isBinary);
			}
			if (ArchiveHandlerManager.getInstance().isArchive(destinationFile))
			{
				ISystemArchiveHandler handler = ArchiveHandlerManager.getInstance().getRegisteredHandler(destinationFile);
				if (handler == null)
					throwCorruptArchiveException(this.getClass() + ".copyToArchive()"); //$NON-NLS-1$
				else 
					return handler.add(stream, "", remoteFile, SystemEncodingUtil.ENCODING_UTF_8, hostEncoding, !isBinary); //$NON-NLS-1$
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

			 while( (readCount = bufInputStream.read(buffer)) > 0 && !isCancelled) 
			 {
			      if (isEncodingConversionRequired && bufWriter != null) 
			      {
						String s = new String(buffer, 0, readCount, hostEncoding);
						bufWriter.write(s);
			      }
			      else if (bufOutputStream != null)
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



	public boolean download(String remoteParent, String remoteFile, File destinationFile, boolean isBinary, String hostEncoding, IProgressMonitor monitor) throws SystemMessageException 
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
				if (isEncodingConversionRequired && bufWriter != null) 
				{
					String s = new String(buffer, 0, bytesRead, hostEncoding);
					bufWriter.write(s);
				}
				else if (bufOutputStream != null)
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
				} else if (file.exists()) {
					destinationFile.setLastModified(file.lastModified());
					//TODO check if we want to preserve permissions
					//if(!file.canWrite()) destinationFile.setReadOnly();
					if (destinationFile.length() != file.length()) {
						//	throw new RemoteFileCancelledException();
						System.err.println("local.upload: size mismach on "+destinationFile.getAbsolutePath()); //$NON-NLS-1$
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
				throwCorruptArchiveException(this.getClass() + ".copyToArchive()"); //$NON-NLS-1$
			else
			{
				if (file.isDirectory())
				{
				    ok = handler.add(file, path, newName, sourceEncoding, targetEncoding, _fileTypeRegistry);
				}
				else
				{
					ok = handler.add(file, path, newName, sourceEncoding, targetEncoding, isText);
				}
			}
		}
		else if (ArchiveHandlerManager.getInstance().isArchive(destination))
		{
			ISystemArchiveHandler handler = ArchiveHandlerManager.getInstance().getRegisteredHandler(destination);
			if (handler == null)
				throwCorruptArchiveException(this.getClass() + ".copyToArchive()"); //$NON-NLS-1$
			else
			{
				if (file.isDirectory())
				{
				    ok = handler.add(file, "", newName, sourceEncoding, targetEncoding, _fileTypeRegistry); //$NON-NLS-1$			    
				}
				else
				{
					ok = handler.add(file, "", newName, sourceEncoding, targetEncoding, isText); //$NON-NLS-1$
				}
			}
		}
		if (!ok)
		{
			// SystemPlugin.logError("LocalFileSubSystemImpl.copyToArchive(): Handler's add() method returned false.");
			SystemMessage msg = getMessage("RSEF5006"); //$NON-NLS-1$
			msg.makeSubstitution(destination.getName(), "localhost"); //$NON-NLS-1$
			throw new SystemMessageException(msg);
		}
		else
			return true;
	}

	public boolean upload(File localFile, String remoteParent, String remoteFile, boolean isBinary, String srcEncoding, String hostEncoding, IProgressMonitor monitor) throws SystemMessageException
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

				if (isEncodingConversionRequired && bufWriter != null) {
					String s = new String(buffer, 0, bytesRead, srcEncoding);
					bufWriter.write(s);
				}
				else if (bufOutputStream != null)
				{
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
					
					// File lengths can be different if the encodings are different
/*					if (destinationFile.length() != localFile.length()) {
						//	throw new RemoteFileCancelledException();
						System.err.println("local.upload: size mismach on "+destinationFile.getAbsolutePath()); //$NON-NLS-1$
						return false;
					}*/
				}
			}
			catch (IOException e)
			{
			}
		}
		return true;
	}
	
	protected IHostFile[] internalFetch(String remoteParent, String fileFilter, int type, IProgressMonitor monitor) {
		LocalFileNameFilter fFilter = new LocalFileNameFilter(fileFilter, type);
		File localParent = new File(remoteParent);
		boolean isArchive = false;
		boolean isVirtual = false;
		if (localParent.exists()) {
			if (localParent.isFile()) {
				isArchive = ArchiveHandlerManager.getInstance().isArchive(localParent);
			}
			// if the system type is Windows, we get the canonical path so that we have the correct case in the path
			// this is needed because Windows paths are case insensitive
			if (isWindows()) {
				try {
					localParent = localParent.getCanonicalFile();
				} catch (IOException e) {
					System.out.println("Can not get canonical path: " + localParent.getAbsolutePath()); //$NON-NLS-1$
				}
			}
		}
		else {
			// does not exist: is it virtual?
			if (remoteParent.endsWith(ArchiveHandlerManager.VIRTUAL_SEPARATOR)) {
				remoteParent = remoteParent.substring(0, remoteParent.length() - ArchiveHandlerManager.VIRTUAL_SEPARATOR.length());
			}
			isVirtual = ArchiveHandlerManager.isVirtual(remoteParent);
		}
		if (isVirtual || isArchive) {
			try {
				VirtualChild[] contents = null;
				File theFile = getContainingArchive(localParent);
				if (isArchive) {
					contents = ArchiveHandlerManager.getInstance().getContents(localParent, ""); //$NON-NLS-1$
				} else if (isVirtual) {
					AbsoluteVirtualPath avp = new AbsoluteVirtualPath(remoteParent);
					contents = ArchiveHandlerManager.getInstance().getContents(theFile, avp.getVirtualPart());
				}
				if (contents == null) {
					return null;
				}
				IHostFile[] results = new LocalVirtualHostFile[contents.length];
				for (int i = 0; i < contents.length; i++) {
					results[i] = new LocalVirtualHostFile(contents[i]);
				}
				return results;
			} catch (IOException e) {
				// FIXME: Do something!
				return null;
			}
		} else {
			//	allow cancel before doing the os query
			if (monitor != null && monitor.isCanceled()) {
				return null;
			}
			if (!fFilter.isGeneric()) {
				File file = new File(localParent, fileFilter);
				return convertToHostFiles(new File[] { file }, type);
			}
			if (localParent.exists()) {
				File[] files = localParent.listFiles(fFilter);
				return convertToHostFiles(files, type);
			} else {
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
		String userHome  =System.getProperty("user.home"); //$NON-NLS-1$
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
					}
					catch (Exception e) 
					{
					}
			}
		}
		else
		{
			v.add(new File("/")); //$NON-NLS-1$
		}
		
		IHostFile[] fileObjs = new LocalHostFile[v.size()];
		for (int idx = 0; idx < v.size(); idx++)
		{
			fileObjs[idx] = new LocalHostFile((File) v.get(idx), true);
		}
		
		return fileObjs;
	}



	public IHostFile getFile(String remoteParent, String name, IProgressMonitor monitor) 
	{
		if (name.endsWith(ArchiveHandlerManager.VIRTUAL_SEPARATOR))
		{
			name = name.substring(0, name.length() - ArchiveHandlerManager.VIRTUAL_SEPARATOR.length());
		}
		
		boolean isVirtualParent = false;
		boolean isArchiveParent = false;
		if (remoteParent != null) {
			File remoteParentFile = new File(remoteParent);
			if (!remoteParentFile.exists()) {
				isVirtualParent = ArchiveHandlerManager.isVirtual(remoteParent);
			} else if (remoteParentFile.isFile()) {
				isArchiveParent = ArchiveHandlerManager.getInstance().isArchive(remoteParentFile);
			}
		}
		if (!isVirtualParent && !isArchiveParent)
		{
			File file = remoteParent==null ? new File(name) : new File(remoteParent, name);
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
				fullpath = fullpath + "/"; //$NON-NLS-1$
			}
			LocalVirtualHostFile results;
			VirtualChild vc = ArchiveHandlerManager.getInstance().getVirtualObject(fullpath + name);
			results = new LocalVirtualHostFile(vc);
			return results;
		}
	}



	public IHostFile createFile(String remoteParent, String fileName, IProgressMonitor monitor) throws SystemMessageException
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
							throw new SystemMessageException(getMessage("RSEG1122")); //$NON-NLS-1$
						}
					}
				}
				catch (Exception e)
				{				
					throw new RemoteFileSecurityException(e);
				}
			}
		}
		else
		{
			throw new RemoteFileIOException(new IOException());
		}
		return new LocalHostFile(fileToCreate);
	}

	protected LocalVirtualHostFile createFileInArchive(File newFile) throws SystemMessageException
	{
		VirtualChild child = ArchiveHandlerManager.getInstance().getVirtualObject(newFile.getAbsolutePath());
		ISystemArchiveHandler handler = child.getHandler();
		if (handler == null)
			throwCorruptArchiveException(this.getClass() + ".createFileInArchive()"); //$NON-NLS-1$
		else
		{
			if (!handler.createFile(child.fullName))
			{
				//SystemPlugin.logError("LocalFileSubSystemImpl.createFileInArchive(): Archive Handler's createFile method returned false. Couldn't create virtual object.");
				throw new SystemMessageException(getMessage("RSEG1124").makeSubstitution(newFile)); //$NON-NLS-1$
			}
		}
		return new LocalVirtualHostFile(child);
	}
	
	private void throwCorruptArchiveException(String classAndMethod) throws SystemMessageException
	{
		// SystemPlugin.logError(classAndMethod + ": Couldn't instantiate archive handler. Archive could be corrupted.");
		throw new SystemMessageException(getMessage("RSEG1122")); //$NON-NLS-1$
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

	public IHostFile createFolder(String remoteParent, String folderName, IProgressMonitor monitor) throws SystemMessageException
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
				if(!folderToCreate.mkdirs())
					throw new RemoteFileSecurityException(new IOException());
			}
		}
		else
		{
			throw new RemoteFileIOException(new IOException());
		}
		return new LocalHostFile(folderToCreate);
	}

	protected LocalVirtualHostFile createFolderInArchive(File newFolder) throws SystemMessageException
	{
		VirtualChild child = ArchiveHandlerManager.getInstance().getVirtualObject(newFolder.getAbsolutePath());
		ISystemArchiveHandler handler = child.getHandler();
		if (handler == null)
			throwCorruptArchiveException(this.getClass() + ".createFolderInArchive()"); //$NON-NLS-1$
		else if (!handler.createFolder(child.fullName))
		{
			// SystemPlugin.logError("LocalFileSubSystemImpl.createFolderInArchive(): Archive Handler's createFolder method returned false. Couldn't create virtual object.");
			throw new SystemMessageException(getMessage("RSEG1124").makeSubstitution(newFolder)); //$NON-NLS-1$
		}
		return new LocalVirtualHostFile(child);
	}

	public boolean delete(String remoteParent, String fileName, IProgressMonitor monitor) throws SystemMessageException
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
			return deleteContents(fileToDelete, monitor);
		}
		else
		{
			return fileToDelete.delete();
		}
	}
	
	private boolean deleteContents(File folder, IProgressMonitor monitor)
	{
		boolean result = true;
		File[] files = folder.listFiles();
		for (int i = 0; i < files.length && result; i++)
		{
			File file = files[i];
			if (file.isDirectory())
			{
				result = deleteContents(file, monitor);
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
			throwCorruptArchiveException(this.getClass() + ".deleteFromArchive()"); //$NON-NLS-1$
		else if (!handler.delete(child.fullName))
		{
			// SystemPlugin.logError("LocalFileSubSystemImpl.deleteFromArchive(): Archive Handler's delete method returned false. Couldn't delete virtual object.");
			throw new SystemMessageException(getMessage("RSEG1125").makeSubstitution(destination)); //$NON-NLS-1$
		}
		return true;
	}
	
	protected boolean deleteArchive(File file)
	{
		ArchiveHandlerManager.getInstance().disposeOfRegisteredHandlerFor(file);
		return file.delete();
	}

	public boolean rename(String remoteParent, String oldName, String newName, IProgressMonitor monitor) throws SystemMessageException
	{
		File fileToRename = new File(remoteParent, oldName);
		if (ArchiveHandlerManager.isVirtual(fileToRename.getAbsolutePath()))
		{
			return renameVirtualFile(fileToRename, newName);
		}
		File newFile = new File(remoteParent, newName);
		boolean result =  fileToRename.renameTo(newFile);
		if (!result)
		{
			// for 192705, we need to throw an exception when rename fails
			throw new SystemMessageException(getMessage("RSEF1301").makeSubstitution(newFile)); //$NON-NLS-1$
		}
		return result;
	}
	
	public boolean rename(String remoteParent, String oldName, String newName, IHostFile oldFile, IProgressMonitor monitor) throws SystemMessageException
	{
		boolean retVal = rename(remoteParent, oldName, newName, monitor);
		File newFile = new File(remoteParent, newName);
		oldFile.renameTo(newFile.getAbsolutePath());
		return retVal;
	}

	/**
	 * Renames a virtual file
	 * 
	 * @param destination virtual file to rename
	 * @param newName the new name of the virtual file
	 * @return whether the operation was successful or not
	 */
	protected boolean renameVirtualFile(File destination, String newName) throws SystemMessageException
	{
		VirtualChild child = ArchiveHandlerManager.getInstance().getVirtualObject(destination.getAbsolutePath());
		ISystemArchiveHandler handler = child.getHandler();
		if (handler == null)
		{
			throwCorruptArchiveException(this.getClass() + ".renameVirtualFile()"); //$NON-NLS-1$
		}
		else
		{
			boolean retval = handler.rename(child.fullName, newName);
			if (!retval)
			{
				// SystemPlugin.logError("LocalFileSubSystemImpl.renameVirtualFile(): Archive Handler's rename method returned false. Couldn't rename virtual object.");
				throw new SystemMessageException(getMessage("RSEG1127").makeSubstitution(child.fullName)); //$NON-NLS-1$
			}
			return retval;
		}
		return false;
	}

	public boolean move(String srcParent, String srcName, String tgtParent, String tgtName, IProgressMonitor monitor) throws SystemMessageException 
	{
		File sourceFolderOrFile = new File(srcParent, srcName);
		File targetFolder = new File(tgtParent, tgtName);
		boolean movedOk = false;
		boolean sourceIsVirtual = ArchiveHandlerManager.isVirtual(sourceFolderOrFile.getAbsolutePath());
		boolean targetIsVirtual = ArchiveHandlerManager.isVirtual(targetFolder.getAbsolutePath());
		boolean targetIsArchive = ArchiveHandlerManager.getInstance().isArchive(targetFolder);
		if (!sourceIsVirtual && !targetIsVirtual && !targetIsArchive)
				/* DKM
				 * we shouldn't be moving archives like virtuals
				 *|| ArchiveHandlerManager.getInstance().isRegisteredArchive(newName)
				 *
				 */
		{
			File fileToMove = new File(srcParent, srcName);
			File newFile = new File(tgtParent, tgtName);
			movedOk = fileToMove.renameTo(newFile);
		}
		
		if (!movedOk)
		{	
			if (copy(srcParent, srcName, tgtParent, tgtName, monitor))
			{
				movedOk = delete(srcParent, srcName, monitor);
			}
		}
		return movedOk;
	}



	public boolean copy(String srcParent, String srcName, String tgtParent, String tgtName, IProgressMonitor monitor) throws SystemMessageException 
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
		
		src = "\"" + srcBuf.toString() + "\""; //$NON-NLS-1$ //$NON-NLS-2$
		target = "\"" + tgtBuf.toString() + "\""; //$NON-NLS-1$ //$NON-NLS-2$
		/*
		// handle imbedded blanks of from or to name...
		if (src.indexOf(' ') >= 0)
			src = "\"" + src + "\"";
		if (target.indexOf(' ') >= 0)
			target = "\"" + target + "\"";
		*/
		boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("win"); //$NON-NLS-1$ //$NON-NLS-2$
		if (isWindows)
		{
			if (folderCopy)
			{
				command = "xcopy " + src + " " + target + " /S /E /K /Q /H /I /Y"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			else
			{
				//command = _osCmdShell + "copy " + src + " " + target; //$NON-NLS-1$ //$NON-NLS-2$
				// create target first so that not prompted
				File targetFile = new File(tgtBuf.toString());
				if (!targetFile.exists())
				{
					// create file so as to avoid ambiguity
					try
					{
						targetFile.createNewFile();
				
					}
					catch (Exception e)
					{
						throw new RemoteFileException(e.getMessage(), e);
					}
				}				
				command = _osCmdShell + "xcopy " + src + " " + target + " /Y /K /Q /H"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}			
		}
		else
		{
			
			if (folderCopy)
			{
				command = "cp  -Rp " + src + " " + target; //$NON-NLS-1$ //$NON-NLS-2$
			}
			else // it is a file 	 
			{
				command = "cp -p " + src + " " + target; //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		int rc = -1;
		try
		{
			Process p = null;
			Runtime runtime = Runtime.getRuntime();
			if (isWindows)
			{
				String theShell = "cmd /C "; //$NON-NLS-1$
				p = runtime.exec(theShell + command);		
			}
			else
			{
				String theShell = "sh"; //$NON-NLS-1$
				String args[] = new String[3];
				args[0] = theShell;					
				args[1] = "-c"; //$NON-NLS-1$
				args[2] = command;
												
				p = runtime.exec(args);
			}
			
			//Process p = Runtime.getRuntime().exec(command);
			rc = p.waitFor();

			//rc = p.exitValue();
		}
		catch (Exception e)
		{
			throw new RemoteFileException(e.getMessage(), e);
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
				buf.insert(i, "\\"); //$NON-NLS-1$
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
		   
		if ((c == '$') || (c == '`') || (c == '"') || (!isWindows() && (c == '\\')) ) {
						
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
	 * @param sourceEncoding encoding of source file
	 * @param targetEncoding desired encoding of target file
	 * @param isText currently unused
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
				tempSource = File.createTempFile(child.name, "virtual"); //$NON-NLS-1$
				tempSource.deleteOnExit();
			}
			catch (IOException e)
			{
				// SystemPlugin.logError("LocalFileSubSystemImpl.copy(): Could not create temp file.", e);
				throw new SystemMessageException(getMessage("Copy failed")); //$NON-NLS-1$
			}
			tempSource.delete();
			if (!tempSource.mkdir())
			{
				// SystemPlugin.logError("LocalFileSubSystemImpl.copy(): Couldn't create temp dir.");
				throw new SystemMessageException(getMessage("RSEG1306").makeSubstitution(sourceFolderOrFile)); //$NON-NLS-1$
			}
			ISystemArchiveHandler handler = child.getHandler();
			if (handler == null)
				throwCorruptArchiveException(this.getClass() + ".copy()"); //$NON-NLS-1$
			else 
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
			src = "\"" + src + "\""; //$NON-NLS-1$ //$NON-NLS-2$
		if (target.indexOf(' ') >= 0)
			target = "\"" + target + "\""; //$NON-NLS-1$ //$NON-NLS-2$
		if (System.getProperty("os.name").toLowerCase().startsWith("win")) //$NON-NLS-1$ //$NON-NLS-2$
		{
			if (folderCopy)
			{
				command = "xcopy " + src + " " + target + " /S /E /K /Q /H /I /Y"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			else
				command = _osCmdShell + "copy " + src + " " + target; //$NON-NLS-1$ //$NON-NLS-2$
		}
		else
		{
			if (folderCopy)
			{
				command = "cp  -r " + src + " " + target; //$NON-NLS-1$ //$NON-NLS-2$
			}
			else // it is a file 	 
			{
				command = "cp " + src + " " + target; //$NON-NLS-1$ //$NON-NLS-2$
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
		return !isWindows();
	}

	public boolean copyBatch(String[] srcParents, String[] srcNames, String tgtParent, IProgressMonitor monitor) throws SystemMessageException 
	{
		boolean ok = true;
		for (int i = 0; i < srcParents.length; i++)
		{
			ok = ok && copy(srcParents[i], srcNames[i], tgtParent, srcNames[i], monitor);
		}
		return ok;
	}

	public boolean setLastModified(String parent, String name, long timestamp, IProgressMonitor monitor) 
	{
		File file = new File(parent, name);
		return file.setLastModified(timestamp);
	}

	public boolean setReadOnly(String parent, String name,
			boolean readOnly, IProgressMonitor monitor) throws SystemMessageException 
	{
		File file = new File(parent, name);
		if (!file.exists()) {
			return false;
		}
		if (readOnly != file.canWrite()) {
			return true;
		}
		if (readOnly)
		{
			return file.setReadOnly();
		}
		else
		{
			if (!_isWindows)
			{
				// make this read-write
				String[] cmd = new String[3];
				cmd[0] = "chmod"; //$NON-NLS-1$
				cmd[1] = "u+w"; //$NON-NLS-1$
				cmd[2] = file.getAbsolutePath();
				int exitValue = -1;
				try
				{
					Process p = Runtime.getRuntime().exec(cmd);
					exitValue = p.waitFor();
				}
				catch (Exception e)
				{					
				}
				return (exitValue == 0);
			}
			// windows version
			else
			{
				String[] cmd = new String[3];
				cmd[0] = "attrib"; //$NON-NLS-1$
				cmd[1] = "-R"; //$NON-NLS-1$
				cmd[2] = file.getAbsolutePath();
				int exitValue = -1;
				try
				{
					Process p = Runtime.getRuntime().exec(cmd);
					exitValue = p.waitFor();
				}
				catch (Exception e)
				{					
				}
				return (exitValue == 0);
			}
		}
	}

	/**
	 * Gets the input stream to access the contents of a remote file.
	 * @since 2.0
	 * @see org.eclipse.rse.services.files.AbstractFileService#getInputStream(String, String, boolean, IProgressMonitor)
	 */
	public InputStream getInputStream(String remoteParent, String remoteFile, boolean isBinary, IProgressMonitor monitor) throws SystemMessageException {
		
		File file = new File(remoteParent, remoteFile);
		InputStream stream = null;
		
		try {
			stream = new FileInputStream(file);
		}
		catch (Exception e) {
			throw new RemoteFileIOException(e);
		}
		
		return stream;
	}

	/**
	 * Gets the output stream to write to a remote file.
	 * @since 2.0
	 * @see org.eclipse.rse.services.files.AbstractFileService#getOutputStream(String, String, boolean, IProgressMonitor)
	 */
	public OutputStream getOutputStream(String remoteParent, String remoteFile, boolean isBinary, IProgressMonitor monitor) throws SystemMessageException {
		File file = new File(remoteParent, remoteFile);
		OutputStream stream = null;
		
		try {
			stream = new FileOutputStream(file);
		}
		catch (Exception e) {
			throw new RemoteFileIOException(e);
		}
		
		return stream;
	}	
	
	public SystemMessage getMessage(String messageID) {
		return (_msgProvider != null ? _msgProvider.getMessage(messageID) : super.getMessage(messageID));
	}
}