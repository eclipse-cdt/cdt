/*******************************************************************************
 * Copyright (c) 2006, 2014 IBM Corporation and others.
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
 * David McKnight   (IBM)        - [207178] changing list APIs for file service and subsystems
 * Kevin Doyle (IBM) - [209355] Retrieving list of FILE_TYPE_FOLDERS should return Archive's
 * Xuan Chen (IBM) - [160775] [api] rename (at least within a zip) blocks UI thread
 * Xuan Chen        (IBM)        - [209828] Need to move the Create operation to a job.
 * David McKnight   (IBM)        - [210109] store constants in IFileService rather than IFileServiceConstants
 * Xuan Chen        (IBM)        - [210555] [regression] NPE when deleting a file on SSH
 * Kevin Doyle		(IBM)		 - [208778] [efs][api] RSEFileStore#getOutputStream() does not support EFS#APPEND
 * David McKnight   (IBM)        - [209593] [api] add support for "file permissions" and "owner" properties for unix files
 * Radoslav Gerganov (ProSyst)   - [218173] [local] non-generic filters don't work
 * Martin Oberhuber (Wind River) - [188330] Problems Copying files with $ in name
 * David McKnight   (IBM)        - [216252] use SimpleSystemMessage instead of getMessage()
 * David McKnight   (IBM)        - [220241] JJ: IRemoteFileSubSystem.list() on the Local file subsystem does not return correct results
 * David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 * Martin Oberhuber (Wind River) - [226262] Make IService IAdaptable
 * David McKnight   (IBM)        - [231211] Local xml file not opened when workspace encoding is different from local system encoding
 * Radoslav Gerganov (ProSyst)   - [230919] IFileService.delete() should not return a boolean
 * Martin Oberhuber (Wind River) - [233993] Improve EFS error reporting
 * Martin Oberhuber (Wind River) - [235360][ftp][ssh][local] Return proper "Root" IHostFile
 * David McKnight   (IBM)        - [238367] [regression] Error when deleting Archive Files
 * David McKnight   (IBM)        - [280899] RSE can't open files in some directory, which give the RSEG1067 error me
 * Martin Oberhuber (Wind River) - [285942] Throw exception when listing a non-folder
 * Martin Oberhuber (Wind River) - [286129][api] RemoteFileException(String) violates API contract
 * David McKnight   (IBM)        - [299140] Local Readonly file can't be copied/pasted twice
 * Martin Oberhuber (Wind River) - [314461] NPE deleting a folder w/o permission
 * David McKnight   (IBM)        - [279829] [local] Save conflict dialog keeps popping up on mounted drive
 * David McKnight   (IBM)        - [331247] Local file paste failed on Vista and Windows 7
 * Xuan Chen        (IBM)        - [222544] [testing] FileServiceArchiveTest leaves temporary files and folders behind in TEMP dir
 * David McKnight   (IBM)        - [337612] Failed to copy the content of a tar file
 * David McKnight   (IBM)        - [232084] [local] local file service should not throw operation cancelled exception due to file sizes
 * David McKnight   (IBM)        - [374538] [local] localFile service tries to set modified time on virtual files
 * Samuel Wu		(IBM)		 - [395981] Local file encoding is not handled properly 
 * David McKnight   (IBM)        - [422508] Unable to map A:\ and B:\ as selectable drives in RSE View
 * David McKnight   (IBM)        - [420798] Slow performances in RDz 9.0 with opening 7000 files located on a network driver.
 * David McKnight   (IBM)        - [427306] A couple cases where RSE doesn't indicate lack of space for upload
 *******************************************************************************/

package org.eclipse.rse.internal.services.local.files;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rse.internal.services.local.Activator;
import org.eclipse.rse.internal.services.local.ILocalMessageIds;
import org.eclipse.rse.internal.services.local.ILocalService;
import org.eclipse.rse.internal.services.local.LocalServiceResources;
import org.eclipse.rse.services.clientserver.FileTypeMatcher;
import org.eclipse.rse.services.clientserver.IMatcher;
import org.eclipse.rse.services.clientserver.ISystemFileTypes;
import org.eclipse.rse.services.clientserver.ISystemOperationMonitor;
import org.eclipse.rse.services.clientserver.NamePatternMatcher;
import org.eclipse.rse.services.clientserver.PathUtility;
import org.eclipse.rse.services.clientserver.SystemEncodingUtil;
import org.eclipse.rse.services.clientserver.SystemOperationMonitor;
import org.eclipse.rse.services.clientserver.archiveutils.AbsoluteVirtualPath;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler;
import org.eclipse.rse.services.clientserver.archiveutils.VirtualChild;
import org.eclipse.rse.services.clientserver.messages.CommonMessages;
import org.eclipse.rse.services.clientserver.messages.ICommonMessageIds;
import org.eclipse.rse.services.clientserver.messages.SimpleSystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemElementNotFoundException;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.clientserver.messages.SystemOperationCancelledException;
import org.eclipse.rse.services.clientserver.messages.SystemOperationFailedException;
import org.eclipse.rse.services.files.AbstractFileService;
import org.eclipse.rse.services.files.HostFilePermissions;
import org.eclipse.rse.services.files.IFilePermissionsService;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.services.files.IHostFilePermissions;
import org.eclipse.rse.services.files.IHostFilePermissionsContainer;
import org.eclipse.rse.services.files.RemoteFileException;
import org.eclipse.rse.services.files.RemoteFileIOException;
import org.eclipse.rse.services.files.RemoteFileSecurityException;

public class LocalFileService extends AbstractFileService implements ILocalService, IFilePermissionsService
{
	private static final String[] ALLDRIVES =
	{
		"A:\\", //$NON-NLS-1$
		"B:\\", //$NON-NLS-1$
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
			else if (_isWindows){ // newer version of windows (i.e. vista or 7)
				_osCmdShell = "cmd /C "; //$NON-NLS-1$
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
				boolean isDirectory = entry.isDirectory();
				boolean isFile = !isDirectory;
				if (isFile){
					isFile = entry.isFile();
				}
				
				if (isFile) {
					result = _matcher.matches(name);
				} else if (isDirectory) {
					if (type == IFileService.FILE_TYPE_FILES_AND_FOLDERS || type == IFileService.FILE_TYPE_FOLDERS) {
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

	private class CheckArchiveOperationStatusThread extends Thread {

		private ISystemOperationMonitor archiveOperationMonitor = null;
		private IProgressMonitor monitor = null;

		public CheckArchiveOperationStatusThread(ISystemOperationMonitor archiveOperationMonitor, IProgressMonitor monitor) {
			this.archiveOperationMonitor = archiveOperationMonitor;
			this.monitor = monitor;
		}

		public void run()
		{
			while(!monitor.isCanceled() && !archiveOperationMonitor.isDone())
			{
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {}
			}

			//evaluate result

			if(monitor.isCanceled() && !archiveOperationMonitor.isDone())
			{
				archiveOperationMonitor.setCancelled(true);
			}
		}
	}

	public void upload(InputStream stream, String remoteParent, String remoteFile, boolean isBinary, String hostEncoding, IProgressMonitor monitor) throws SystemMessageException
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
				else {
					handler.add(stream, child.path, remoteFile, SystemEncodingUtil.ENCODING_UTF_8, hostEncoding, !isBinary, null);
					return;
				}
			}
			if (ArchiveHandlerManager.getInstance().isArchive(destinationFile))
			{
				ISystemArchiveHandler handler = ArchiveHandlerManager.getInstance().getRegisteredHandler(destinationFile);
				if (handler == null)
					throwCorruptArchiveException(this.getClass() + ".copyToArchive()"); //$NON-NLS-1$
				else {
					handler.add(stream, "", remoteFile, SystemEncodingUtil.ENCODING_UTF_8, hostEncoding, !isBinary, null); //$NON-NLS-1$
					return;
				}
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
		catch (SystemMessageException e)
		{
		   throw e;
		}
		catch (Exception e)
		{
		   throw new RemoteFileIOException(Activator.PLUGIN_ID, e);
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
					throw new SystemOperationCancelledException();
				}
			}
			catch (IOException e)
			{
			}
		}
	}



	public void download(String remoteParent, String remoteFile, File destinationFile, boolean isBinary, String hostEncoding, IProgressMonitor monitor) throws SystemMessageException
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
			copyFromArchive(file, destinationFile.getParentFile(), destinationFile.getName(), monitor, hostEncoding, SystemEncodingUtil.ENCODING_UTF_8, !isBinary);
			return;
		}
		if (targetIsVirtual || targetIsArchive)
		{
			copyToArchive(file, destinationFile.getParentFile(), destinationFile.getName(), monitor, hostEncoding, SystemEncodingUtil.ENCODING_UTF_8, !isBinary);
			return;
		}

		try
		{
			if (!destinationFile.exists())
			{
				File parentDir = destinationFile.getParentFile();
				parentDir.mkdirs();
			}
			// encoding conversion required if it a text file but not an xml file
			String systemEncoding = SystemEncodingUtil.getInstance().getLocalDefaultEncoding();
			boolean isEncodingConversionRequired = !isBinary && !systemEncoding.equals(hostEncoding); // should not convert if both encodings are the same

			inputStream = new FileInputStream(file);
			bufInputStream = new BufferedInputStream(inputStream);
			
			boolean wasReadonly = destinationFile.exists() && !destinationFile.canWrite();
			if (wasReadonly){ // tempfile is readonly
				// since we're replacing the tempfile that represents the real file, the readonly bit should be removed for the transfer
				//destinationFile.setWritable(true);
				setReadOnly(destinationFile.getParent(), destinationFile.getName(), false, monitor);
			}
			outputStream = new FileOutputStream(destinationFile);

			if (isEncodingConversionRequired)
			{
				outputWriter = new OutputStreamWriter(outputStream, systemEncoding);
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
			if (wasReadonly){
				destinationFile.setReadOnly();
			}
		}
		catch (Exception e)
		{
		   throw new RemoteFileIOException(Activator.PLUGIN_ID, e);
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
					throw new SystemOperationCancelledException();
//					return false;
				} else if (file.exists()) {
					destinationFile.setLastModified(file.lastModified());

					String systemEncoding = SystemEncodingUtil.getInstance().getEnvironmentEncoding();
					boolean sizeCheck = !isBinary && systemEncoding.equals(hostEncoding);

					if (sizeCheck && (destinationFile.length() != file.length())) {
						throw new RemoteFileIOException(new IOException(NLS.bind(LocalServiceResources.FILEMSG_ERROR_DOWNLOAD_SIZE,remoteFile)));
					}
				}
			}
			catch (IOException e)
			{
			//	SystemPlugin.logError("Closing streams: " + file.getAbsolutePath(), e);
				throw new RemoteFileIOException(e);
			}
		}
	}

	private boolean copyToArchive(File file, File destination, String newName, IProgressMonitor monitor, String sourceEncoding, String targetEncoding, boolean isText) throws SystemMessageException
	{
		ISystemArchiveHandler handler = null;
		String path = "";  //$NON-NLS-1$
		if (ArchiveHandlerManager.isVirtual(destination.getAbsolutePath()))
		{
			VirtualChild virtualChild = ArchiveHandlerManager.getInstance().getVirtualObject(destination.getAbsolutePath());
			handler = virtualChild.getHandler();
			path = virtualChild.fullName;
			if (!virtualChild.isDirectory)
			{
				path = virtualChild.path;
			}
		}
		else if (ArchiveHandlerManager.getInstance().isArchive(destination))
		{
			handler = ArchiveHandlerManager.getInstance().getRegisteredHandler(destination);
		}

		if (handler == null)
			throwCorruptArchiveException(this.getClass() + ".copyToArchive()"); //$NON-NLS-1$

		ISystemOperationMonitor archiveOperationMonitor = null;
		if (null != monitor)
		{
			archiveOperationMonitor = new SystemOperationMonitor();
			CheckArchiveOperationStatusThread checkArchiveOperationStatusThread = new CheckArchiveOperationStatusThread(archiveOperationMonitor, monitor);
			checkArchiveOperationStatusThread.start();
		}

		try {
			if (file.isDirectory()) {
				handler.add(file, path, newName, sourceEncoding, targetEncoding, _fileTypeRegistry, archiveOperationMonitor);
			} else {
				handler.add(file, path, newName, sourceEncoding, targetEncoding, isText, archiveOperationMonitor);
			}
		} catch (SystemMessageException e) {
			//e.printStackTrace();
			if (null != monitor && monitor.isCanceled())
			{
				//This operation has been cancelled by the user.
				throw getCancelledException();
			}
			// SystemPlugin.logError("LocalFileSubSystemImpl.copyToArchive(): Handler's add() method returned false.");
			String msgTxt = NLS.bind(LocalServiceResources.FILEMSG_FILE_NOT_SAVED, destination.getName(), "localhost"); //$NON-NLS-1$
			//String msgDetails = LocalServiceResources.FILEMSG_FILE_NOT_SAVED_DETAILS;
			SystemMessage msg = new SimpleSystemMessage(Activator.PLUGIN_ID,
					ILocalMessageIds.FILEMSG_FILE_NOT_SAVED,
					IStatus.ERROR, msgTxt, e);
			throw new SystemMessageException(msg);
		}
		return true;
	}

	public void upload(File localFile, String remoteParent, String remoteFile, boolean isBinary, String srcEncoding, String hostEncoding, IProgressMonitor monitor) throws SystemMessageException
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
			copyFromArchive(localFile, target, remoteFile, monitor, srcEncoding, hostEncoding, !isBinary);
			return;
		}
		if (targetIsVirtual)
		{
			copyToArchive(localFile, target, remoteFile, monitor, srcEncoding, hostEncoding, !isBinary);
			return;
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
			destinationFile.delete();
			throw new RemoteFileIOException(e);
		}
		catch (UnsupportedEncodingException e)
		{
			destinationFile.delete();
			throw new RemoteFileIOException(e);
		}
		catch (IOException e)
		{
			destinationFile.delete();
			throw new RemoteFileIOException(e);
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
					throw new SystemOperationCancelledException();
				} else if (destinationFile!=null) {
					// commented out as per the following bug:
					//	[279829] [local] Save conflict dialog keeps popping up on mounted drive
					// destinationFile.setLastModified(localFile.lastModified());
					//TODO check if we want to preserve permissions
					//if(!localFile.canWrite()) destinationFile.setReadOnly();

					// File lengths can be different if the encodings are different
					String systemEncoding = SystemEncodingUtil.getInstance().getEnvironmentEncoding();
					boolean sizeCheck = !isBinary && systemEncoding.equals(hostEncoding);
					if (sizeCheck && destinationFile.length() != localFile.length()) {
						throw new RemoteFileIOException(new IOException(NLS.bind(LocalServiceResources.FILEMSG_ERROR_UPLOAD_SIZE,remoteFile)));
					}
				}
			}
			catch (IOException e)
			{
			}
		}
	}

	protected IHostFile[] internalFetch(String remoteParent, String fileFilter, int type, IProgressMonitor monitor) throws SystemMessageException {
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
					localParent = localParent.getCanonicalFile(); // can this be avoided for network drives?
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
		} else {
			//	allow cancel before doing the os query
			if (monitor != null && monitor.isCanceled()) {
				throw new SystemOperationCancelledException();
			}
			/* bug 220241 - don't need this block of code
			 *  listFiles() with a filter will still return all folders (they don't have to match)
			if (!fFilter.isGeneric()) {
				File file = new File(localParent, fileFilter);
				return convertToHostFiles(new File[] { file }, type);
			}
			*/
			if (localParent.exists()) {
				File[] files = localParent.listFiles(fFilter);
				if (files == null) {
					//throw new RemoteFileException("Error listing: " + localParent.getAbsolutePath());
					throw new RemoteFileIOException(new IOException("Error listing: " + localParent.getAbsolutePath()));
				}
				return convertToHostFiles(files, type);
			} else {
				throw new SystemElementNotFoundException(localParent.getAbsolutePath(), "list");
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
				boolean isDirectory = file.isDirectory();
				boolean isFile = !isDirectory;
				if (isFile){
					isFile = file.isFile();
				}
				
				if (isDirectory)
				{
					if (type == IFileService.FILE_TYPE_FILES_AND_FOLDERS ||
					    type == IFileService.FILE_TYPE_FOLDERS)
					{
						results.add(new LocalHostFile(file, false, isFile));
					}
				}
				else if (isFile)
				{
					if (type == IFileService.FILE_TYPE_FILES_AND_FOLDERS ||
						type == IFileService.FILE_TYPE_FILES)
					{
						results.add(new LocalHostFile(file, false, isFile));
					} else if (type == IFileService.FILE_TYPE_FOLDERS &&
						ArchiveHandlerManager.getInstance().isArchive(file)) {
						// On Local Archive's should be considered Folders
						// as they are containers that can be opened.
						results.add(new LocalHostFile(file, false, isFile));
					}
				}
				else if (file.exists())
				{
					results.add(new LocalHostFile(file, false, isFile));
				}
			}
		}
		return (IHostFile[])results.toArray(new IHostFile[results.size()]);
	}

	public IHostFile getUserHome()
	{
		String userHome  =System.getProperty("user.home"); //$NON-NLS-1$
		File userHomeFile = new File(userHome);
		return new LocalHostFile(userHomeFile, (userHomeFile.getParent() == null), userHomeFile.isFile());
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
			fileObjs[idx] = new LocalHostFile((File) v.get(idx), true, false);
		}

		return fileObjs;
	}



	public IHostFile getFile(String remoteParent, String name, IProgressMonitor monitor) throws SystemMessageException
	{
		if (name.endsWith(ArchiveHandlerManager.VIRTUAL_SEPARATOR))
		{
			name = name.substring(0, name.length() - ArchiveHandlerManager.VIRTUAL_SEPARATOR.length());
		}

		boolean isVirtualParent = false;
		boolean isArchiveParent = false;
		boolean isRoot = (remoteParent == null || remoteParent.length() == 0);
		if (!isRoot) {
			File remoteParentFile = new File(remoteParent);
			if (!remoteParentFile.exists()) {
				isVirtualParent = ArchiveHandlerManager.isVirtual(remoteParent);
			} else if (remoteParentFile.isFile()) {
				isArchiveParent = ArchiveHandlerManager.getInstance().isArchive(remoteParentFile);
			}
		}
		if (!isVirtualParent && !isArchiveParent)
		{
			File file = isRoot ? new File(name) : new File(remoteParent, name);
			return new LocalHostFile(file, isRoot, file.isFile());
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
				return createFileInArchive(fileToCreate, monitor);
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
				}
				catch (IOException e)
				{
					throw new RemoteFileSecurityException(e);
				}
				if (ArchiveHandlerManager.getInstance().isArchive(fileToCreate)) {
					try {
						ArchiveHandlerManager.getInstance().createEmptyArchive(fileToCreate);
					} catch (SystemMessageException e) {
						SystemMessage msg = new SimpleSystemMessage(Activator.PLUGIN_ID, ILocalMessageIds.FILEMSG_ARCHIVE_CORRUPTED, IStatus.ERROR,
								LocalServiceResources.FILEMSG_ARCHIVE_CORRUPTED, e);
						throw new SystemMessageException(msg);
					}
				}
			}
		}
		else
		{
			throw new RemoteFileIOException(new IOException());
		}
		return new LocalHostFile(fileToCreate);
	}

	protected LocalVirtualHostFile createFileInArchive(File newFile, IProgressMonitor monitor) throws SystemMessageException
	{
		VirtualChild child = ArchiveHandlerManager.getInstance().getVirtualObject(newFile.getAbsolutePath());
		ISystemArchiveHandler handler = child.getHandler();
		if (handler == null)
			throwCorruptArchiveException(this.getClass() + ".createFileInArchive()"); //$NON-NLS-1$
		else
		{
			ISystemOperationMonitor archiveOperationMonitor = null;
			if (null != monitor)
			{
				archiveOperationMonitor = new SystemOperationMonitor();
				CheckArchiveOperationStatusThread checkArchiveOperationStatusThread = new CheckArchiveOperationStatusThread(archiveOperationMonitor, monitor);
				checkArchiveOperationStatusThread.start();
			}
			try {
				handler.createFile(child.fullName, archiveOperationMonitor);
			} catch (SystemMessageException e) {
				if (null != monitor && monitor.isCanceled())
				{
					//This operation has been cancelled by the user.
					throw getCancelledException();
				}
				String msgTxt = NLS.bind(LocalServiceResources.FILEMSG_CREATE_VIRTUAL_FAILED, newFile);
				//String msgDetails = LocalServiceResources.FILEMSG_CREATE_VIRTUAL_FAILED_DETAILS;
				SystemMessage msg = new SimpleSystemMessage(Activator.PLUGIN_ID,
						ILocalMessageIds.FILEMSG_CREATE_VIRTUAL_FAILED,
						IStatus.ERROR, msgTxt, e);
				throw new SystemMessageException(msg);
			}
		}
		return new LocalVirtualHostFile(child);
	}

	private void throwCorruptArchiveException(String classAndMethod) throws SystemMessageException
	{
		SystemMessage msg = new SimpleSystemMessage(Activator.PLUGIN_ID,
				ILocalMessageIds.FILEMSG_ARCHIVE_CORRUPTED,
				IStatus.ERROR,
				LocalServiceResources.FILEMSG_ARCHIVE_CORRUPTED, LocalServiceResources.FILEMSG_ARCHIVE_CORRUPTED_DETAILS);
		throw new SystemMessageException(msg);
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
				return createFolderInArchive(folderToCreate, monitor);
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

	protected LocalVirtualHostFile createFolderInArchive(File newFolder, IProgressMonitor monitor) throws SystemMessageException
	{
		VirtualChild child = ArchiveHandlerManager.getInstance().getVirtualObject(newFolder.getAbsolutePath());
		ISystemArchiveHandler handler = child.getHandler();
		if (handler == null)
			throwCorruptArchiveException(this.getClass() + ".createFolderInArchive()"); //$NON-NLS-1$
		else
		{
			ISystemOperationMonitor archiveOperationMonitor = null;
			if (null != monitor)
			{
				archiveOperationMonitor = new SystemOperationMonitor();
				CheckArchiveOperationStatusThread checkArchiveOperationStatusThread = new CheckArchiveOperationStatusThread(archiveOperationMonitor, monitor);
				checkArchiveOperationStatusThread.start();
			}
			try {
				handler.createFolder(child.fullName, archiveOperationMonitor);
			} catch (SystemMessageException e) {
				if (null != monitor && monitor.isCanceled())
				{
					//This operation has been cancelled by the user.
					throw getCancelledException();
				}

				String msgTxt = NLS.bind(LocalServiceResources.FILEMSG_CREATE_VIRTUAL_FAILED, newFolder);
				//String msgDetails = LocalServiceResources.FILEMSG_CREATE_VIRTUAL_FAILED_DETAILS;
				SystemMessage msg = new SimpleSystemMessage(Activator.PLUGIN_ID,
						ILocalMessageIds.FILEMSG_CREATE_VIRTUAL_FAILED,
						IStatus.ERROR, msgTxt, e);
				throw new SystemMessageException(msg);

			}
		}
		return new LocalVirtualHostFile(child);
	}

	public void delete(String remoteParent, String fileName, IProgressMonitor monitor) throws SystemMessageException
	{
		if (fileName.endsWith(ArchiveHandlerManager.VIRTUAL_SEPARATOR))
		{
			fileName = fileName.substring(0, fileName.length() - ArchiveHandlerManager.VIRTUAL_SEPARATOR.length());
		}
		File remoteParentFile = new File(remoteParent);
		if (ArchiveHandlerManager.getInstance().isArchive(remoteParentFile))
		{
			remoteParent = remoteParent + ArchiveHandlerManager.VIRTUAL_SEPARATOR;
		}
		boolean result = true;
		File fileToDelete = new File(remoteParent, fileName);
		if (ArchiveHandlerManager.isVirtual(fileToDelete.getAbsolutePath()))
		{
			result = deleteFromArchive(fileToDelete, monitor);
		}
		else if (ArchiveHandlerManager.getInstance().isArchive(fileToDelete))
		{
			result = deleteArchive(fileToDelete);
		}
		if (fileToDelete.isDirectory())
		{
			result = deleteContents(fileToDelete, monitor);
		}
		else
		{
			if (fileToDelete.exists())
				result = fileToDelete.delete();
		}
		if (!result) {
			if (fileToDelete.exists()) {
				// Deletion failed without specification why... likely a Security
				// problem, or an open file in the files to be deleted.
				// TODO Externalize Message
				throw new SystemOperationFailedException(Activator.PLUGIN_ID, "Failed to delete: " + fileToDelete.getAbsolutePath());
			} else {
				throw new SystemElementNotFoundException(fileToDelete.getAbsolutePath(), "delete");
			}
		}
	}

	public void deleteBatch(String[] remoteParents, String[] fileNames, IProgressMonitor monitor) throws SystemMessageException
	{
		String deletingMessage = NLS.bind(LocalServiceResources.FILEMSG_DELETING, ""); //$NON-NLS-1$
		monitor.beginTask(deletingMessage, remoteParents.length);
		for (int i = 0; i < remoteParents.length; i++)
		{
			deletingMessage = NLS.bind(LocalServiceResources.FILEMSG_DELETING, fileNames[i]);
			monitor.subTask(deletingMessage);
			delete(remoteParents[i], fileNames[i], monitor);
			monitor.worked(1);
		}
	}

	private boolean deleteContents(File folder, IProgressMonitor monitor)
	{
		boolean result = true;
		File[] files = folder.listFiles();
		if (files!=null)
		{
			//bug 314461: java.io.File returns null if folder has no permissions
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
	protected boolean deleteFromArchive(File destination, IProgressMonitor monitor) throws SystemMessageException
	{
		VirtualChild child = ArchiveHandlerManager.getInstance().getVirtualObject(destination.getAbsolutePath());
		ISystemArchiveHandler handler = child.getHandler();
		if (handler == null)
			throwCorruptArchiveException(this.getClass() + ".deleteFromArchive()"); //$NON-NLS-1$
		ISystemOperationMonitor archiveOperationMonitor = null;
		if (null != monitor)
		{
			archiveOperationMonitor = new SystemOperationMonitor();
			CheckArchiveOperationStatusThread checkArchiveOperationStatusThread = new CheckArchiveOperationStatusThread(archiveOperationMonitor, monitor);
			checkArchiveOperationStatusThread.start();
		}
		try {
			return handler.delete(child.fullName, archiveOperationMonitor);
		} catch (SystemMessageException e) {
			if (monitor != null && monitor.isCanceled())
			{
				//This operation has been cancelled by the user.
				throw getCancelledException();
			}
			// SystemPlugin.logError("LocalFileSubSystemImpl.deleteFromArchive(): Archive Handler's delete method returned false. Couldn't delete virtual object.");
			String msgTxt = NLS.bind(LocalServiceResources.FILEMSG_DELETE_VIRTUAL_FAILED, destination);
			//String msgDetails = LocalServiceResources.FILEMSG_DELETE_VIRTUAL_FAILED_DETAILS;
			throw new SystemMessageException(new SimpleSystemMessage(Activator.PLUGIN_ID,
					ILocalMessageIds.FILEMSG_DELETE_VIRTUAL_FAILED,
					IStatus.ERROR,
					msgTxt, e));
		}
	}

	protected boolean deleteArchive(File file)
	{
		ArchiveHandlerManager.getInstance().disposeOfRegisteredHandlerFor(file);
		return file.delete();
	}

	public void rename(String remoteParent, String oldName, String newName, IProgressMonitor monitor) throws SystemMessageException
	{
		File fileToRename = new File(remoteParent, oldName);
		if (ArchiveHandlerManager.isVirtual(fileToRename.getAbsolutePath()))
		{
			renameVirtualFile(fileToRename, newName, monitor);
			return;
		}
		File newFile = new File(remoteParent, newName);
		boolean result =  fileToRename.renameTo(newFile);
		if (!result)
		{
			// for 192705, we need to throw an exception when rename fails
			String msgTxt = NLS.bind(LocalServiceResources.FILEMSG_RENAME_FILE_FAILED, newFile);
			String msgDetails = LocalServiceResources.FILEMSG_RENAME_FILE_FAILED_DETAILS;
			throw new SystemMessageException(new SimpleSystemMessage(Activator.PLUGIN_ID,
					ILocalMessageIds.FILEMSG_RENAME_FILE_FAILED,
					IStatus.ERROR, msgTxt, msgDetails));
		}
	}

	public void rename(String remoteParent, String oldName, String newName, IHostFile oldFile, IProgressMonitor monitor) throws SystemMessageException
	{
		rename(remoteParent, oldName, newName, monitor);
		File newFile = new File(remoteParent, newName);
		oldFile.renameTo(newFile.getAbsolutePath());
	}

	/**
	 * Renames a virtual file
	 *
	 * @param destination virtual file to rename
	 * @param newName the new name of the virtual file
	 * @return whether the operation was successful or not
	 */
	protected boolean renameVirtualFile(File destination, String newName, IProgressMonitor monitor) throws SystemMessageException
	{
		VirtualChild child = ArchiveHandlerManager.getInstance().getVirtualObject(destination.getAbsolutePath());
		ISystemArchiveHandler handler = child.getHandler();
		if (handler == null)
		{
			throwCorruptArchiveException(this.getClass() + ".renameVirtualFile()"); //$NON-NLS-1$
		}
		else
		{
			ISystemOperationMonitor archiveOperationMonitor = null;
			if (null != monitor)
			{
				archiveOperationMonitor = new SystemOperationMonitor();
				CheckArchiveOperationStatusThread checkArchiveOperationStatusThread = new CheckArchiveOperationStatusThread(archiveOperationMonitor, monitor);
				checkArchiveOperationStatusThread.start();
			}

			try {
				handler.rename(child.fullName, newName, archiveOperationMonitor);
			} catch (SystemMessageException e) {
				if (null != monitor && monitor.isCanceled())
				{
					//This operation has been cancelled by the user.
					throw getCancelledException();
				}

				// for 192705, we need to throw an exception when rename fails
				String msgTxt = NLS.bind(LocalServiceResources.FILEMSG_RENAME_FILE_FAILED, child.fullName);
				//String msgDetails = LocalServiceResources.FILEMSG_RENAME_FILE_FAILED_DETAILS;
				//e.printStackTrace();
				throw new SystemMessageException(new SimpleSystemMessage(Activator.PLUGIN_ID,
						ILocalMessageIds.FILEMSG_RENAME_FILE_FAILED,
						IStatus.ERROR,
						msgTxt, e));
			}
		}
		return false;
	}

	public void move(String srcParent, String srcName, String tgtParent, String tgtName, IProgressMonitor monitor) throws SystemMessageException
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
			//Try plain Java Filesystem move first
			movedOk = fileToMove.renameTo(newFile);
		}

		if (!movedOk)
		{
			copy(srcParent, srcName, tgtParent, tgtName, monitor);
			try {
				delete(srcParent, srcName, monitor);
			} catch (SystemMessageException exc)
			{
				if (monitor.isCanceled())
				{
					//This mean the copy operation is ok, but delete operation has been cancelled by user.
					//The delete() call will take care of recovered from the cancel operation.
					//So we need to make sure to remove the already copied file/folder.
					delete(tgtParent, tgtName, null);
				}
				throw exc;
			}
		}
	}

	public void copy(String srcParent, String srcName, String tgtParent, String tgtName, IProgressMonitor monitor) throws SystemMessageException
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
			copyFromArchive(srcFile, new File(tgtParent), tgtName, monitor, SystemEncodingUtil.ENCODING_UTF_8, SystemEncodingUtil.ENCODING_UTF_8, false);
			return;
		}
		if (targetIsVirtual || targetIsArchive)
		{
			copyToArchive(srcFile, new File(tgtParent), tgtName, monitor, SystemEncodingUtil.ENCODING_UTF_8, SystemEncodingUtil.ENCODING_UTF_8, false);
			return;
		}

//		 handle special characters in source and target strings
		src = enQuote(src);
		target = enQuote(target);
		if (isWindows())
		{
			if (folderCopy)
			{
				command = "xcopy " + src + " " + target + " /S /E /K /Q /H /I /Y"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			else
			{
				// create target first so that not prompted
				if (!tgtFile.exists())
				{
					// create file so as to avoid ambiguity
					try
					{
						tgtFile.createNewFile();
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
		try
		{
			Process p = null;
			Runtime runtime = Runtime.getRuntime();
			if (isWindows())
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
			p.waitFor();

			//rc = p.exitValue();
		}
		catch (Exception e)
		{
			throw new RemoteFileException(e.getMessage(), e);
		}
	}

	/**
	 * Quote a file name such that it is valid in a shell
	 * @param s file name to quote
	 * @return quoted file name
	 */
	protected String enQuote(String s)
	{
		if(isWindows()) {
			return '"' + s + '"';
		} else {
			return PathUtility.enQuoteUnix(s);
		}
	}

	private boolean isTempFile(File resource){
		try {
			URI wsURI = URIUtil.toURI(Platform.getInstanceLocation().getURL());
			File wsRoot = URIUtil.toFile(wsURI);
			if (wsRoot!=null) {
			  File rsProj = new File(wsRoot, "RemoteSystemsTempFiles"); //$NON-NLS-1$
			  IPath rsProjPath = new Path(rsProj.getAbsolutePath());
			  IPath resPath = new Path(resource.getAbsolutePath());
			  return rsProjPath.isPrefixOf(resPath);
			  //could also compare canonical paths at this point but won't do here
			  //since it is costly and most likely not needed for the Tempfiles project.
			}
		} catch (URISyntaxException e) {
		}
		return false;
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
	 * @return true if the copy succeeded
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
		ISystemOperationMonitor archiveOperationMonitor = null;
		CheckArchiveOperationStatusThread checkArchiveOperationStatusThread = null;
		if (null != monitor)
		{
			archiveOperationMonitor = new SystemOperationMonitor();
			checkArchiveOperationStatusThread = new CheckArchiveOperationStatusThread(archiveOperationMonitor, monitor);
		}

		boolean isTempFile = isTempFile(targetFolder);
		if (!((ArchiveHandlerManager.isVirtual(targetFolder.getAbsolutePath()) && !isTempFile)) && !ArchiveHandlerManager.getInstance().isArchive(targetFolder))
		{
			// this is an optimization to speed up extractions from large zips. Instead of
			// extracting to a temp location and then copying the temp files to the target location
			// we simply instruct the handler to extract to the target location.
			if (null != monitor)
			{
				checkArchiveOperationStatusThread.start();
			}
			File destinationFile = new File(targetFolder, child.name);
			try {
				child.getExtractedFile(destinationFile, sourceEncoding, isText, archiveOperationMonitor);
			} catch (SystemMessageException e) {
				if (destinationFile.isDirectory())
				{
					deleteContents(destinationFile, monitor);
				}
				else
				{
					destinationFile.delete();
				}

				if (monitor != null && monitor.isCanceled())
				{
					//This operation has been cancelled by the user.
					throw getCancelledException();
				}

				// for 192705, we need to throw an exception when rename fails
				String msgTxt = NLS.bind(LocalServiceResources.FILEMSG_RENAME_FILE_FAILED, child.fullName);
				//String msgDetails = LocalServiceResources.FILEMSG_RENAME_FILE_FAILED_DETAILS;
				throw new SystemMessageException(new SimpleSystemMessage(Activator.PLUGIN_ID,
						ILocalMessageIds.FILEMSG_RENAME_FILE_FAILED,
						IStatus.ERROR,
						msgTxt, e));
			}
			return true;
		}

		if (null != monitor)
		{
			checkArchiveOperationStatusThread.start();
		}
		
		//Don't think we need this call.
		//src = child.getExtractedFile(sourceEncoding, isText, archiveOperationMonitor).getAbsolutePath();
		if (monitor != null && monitor.isCanceled())
		{
			//This operation has been cancelled by the user.
			throw getCancelledException();
		}
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
				String msgTxt = NLS.bind(LocalServiceResources.FILEMSG_COPY_FILE_FAILED, sourceFolderOrFile);
				String msgDetails = LocalServiceResources.FILEMSG_COPY_FILE_FAILED_DETAILS;

				throw new SystemMessageException(new SimpleSystemMessage(Activator.PLUGIN_ID,
						ILocalMessageIds.FILEMSG_COPY_FILE_FAILED,
						IStatus.ERROR, msgTxt, msgDetails));
			}
			tempSource.delete();
			if (!tempSource.mkdir())
			{
				String msgTxt = NLS.bind(LocalServiceResources.FILEMSG_COPY_FILE_FAILED, sourceFolderOrFile);
				String msgDetails = LocalServiceResources.FILEMSG_COPY_FILE_FAILED_DETAILS;

				// SystemPlugin.logError("LocalFileSubSystemImpl.copy(): Couldn't create temp dir.");
				throw new SystemMessageException(new SimpleSystemMessage(Activator.PLUGIN_ID,
						ILocalMessageIds.FILEMSG_COPY_FILE_FAILED,
						IStatus.ERROR, msgTxt, msgDetails));
			}
			ISystemArchiveHandler handler = child.getHandler();
			if (handler == null)
				throwCorruptArchiveException(this.getClass() + ".copy()"); //$NON-NLS-1$
			else
				handler.extractVirtualDirectory(child.fullName, tempSource, sourceEncoding, isText, archiveOperationMonitor);
			src = tempSource.getAbsolutePath() + File.separatorChar + child.name;
		}
		if ((ArchiveHandlerManager.isVirtual(targetFolder.getAbsolutePath()) && !isTempFile) || ArchiveHandlerManager.getInstance().isArchive(targetFolder))
		{
			File source = new File(src);
			boolean returnValue = copyToArchive(source, targetFolder, newName, monitor, SystemEncodingUtil.ENCODING_UTF_8, targetEncoding, isText);
			deleteContents(source, monitor);
			if (!returnValue)
			{
				if (monitor != null && monitor.isCanceled())
				{
					//This operation has been cancelled by the user.
					throw getCancelledException();
				}
			}
			return returnValue;
		}

		//Don't think the code below here ever got executed, since it scenario has been covered by extract directly to the destination archive file.
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

	public boolean isCaseSensitive()
	{
		return !isWindows();
	}

	public void copyBatch(String[] srcParents, String[] srcNames, String tgtParent, IProgressMonitor monitor) throws SystemMessageException
	{
		String deletingMessage = NLS.bind(LocalServiceResources.FILEMSG_COPYING, ""); //$NON-NLS-1$
		monitor.beginTask(deletingMessage, srcParents.length);
		for (int i = 0; i < srcParents.length; i++)
		{
			deletingMessage = NLS.bind(LocalServiceResources.FILEMSG_COPYING, srcNames[i]);
			monitor.subTask(deletingMessage);
			copy(srcParents[i], srcNames[i], tgtParent, srcNames[i], monitor);
			monitor.worked(1);
		}
	}

	public void setLastModified(String parent, String name, long timestamp, IProgressMonitor monitor) throws SystemMessageException
	{
		File file = new File(parent, name);
		if (ArchiveHandlerManager.isVirtual(file.getAbsolutePath())){
			return; // don't support setting modified date on virtuals
		}
		if (!file.setLastModified(timestamp)) {
			if (!file.exists()) {
				// TODO externalize message
				throw new SystemElementNotFoundException(Activator.PLUGIN_ID, file.getAbsolutePath(), "setLastModified");
			} else {
				throw new SystemOperationFailedException(Activator.PLUGIN_ID, "setLastModified: " + file.getAbsolutePath());
			}
		}
	}

	public void setReadOnly(String parent, String name,
			boolean readOnly, IProgressMonitor monitor) throws SystemMessageException
	{
		File file = new File(parent, name);
		if (!file.exists()) {
			//TODO Externalize message, and/or centralize e.g. RemoteFileNotFoundException
			//See org.eclipse.core.filesystem/src/org/eclipse/core/internal/filesystem/Messages.java - fileNotFound
			String messageText = "File not found";
			//TODO throw new RemoteFileNotFoundException
			throw new SystemElementNotFoundException(Activator.PLUGIN_ID, file.getAbsolutePath(), "setReadOnly");
		}
		if (readOnly != file.canWrite()) {
			return;
		}
		if (readOnly)
		{
			if (!file.setReadOnly()) {
				//TODO Externalize message
				throw new SystemOperationFailedException(Activator.PLUGIN_ID, "Failed to setReadOnly: " + file.getAbsolutePath());
			}
			return;
		}
		else
		{
			Exception remoteException = null;
			String remoteError = ""; //$NON-NLS-1$
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
					if (p.getErrorStream().available() > 0) {
						remoteError = ": " + new BufferedReader(new InputStreamReader(p.getErrorStream())).readLine(); //$NON-NLS-1$
					} else if (p.getInputStream().available() > 0) {
						remoteError = ": " + new BufferedReader(new InputStreamReader(p.getInputStream())).readLine(); //$NON-NLS-1$
					}
				}
				catch (Exception e)
				{
					remoteException = e;
				}
				if (exitValue != 0) {
					//TODO Externalize message
					throw new SystemOperationFailedException(Activator.PLUGIN_ID, "Failed to setWritable: " + remoteError, remoteException);
				}
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
					if (p.getErrorStream().available() > 0) {
						remoteError = ": " + new BufferedReader(new InputStreamReader(p.getErrorStream())).readLine(); //$NON-NLS-1$
					} else if (p.getInputStream().available() > 0) {
						remoteError = ": " + new BufferedReader(new InputStreamReader(p.getInputStream())).readLine(); //$NON-NLS-1$
					}
				}
				catch (Exception e)
				{
					remoteException = e;
				}
				if (exitValue != 0) {
					//TODO Externalize String
					throw new SystemOperationFailedException(Activator.PLUGIN_ID, "Failed to setWritable: " + remoteError, remoteException);
				}
			}
			//Verify that it actually worked
			if (!file.canWrite()) {
				if (remoteError.length() == 0) {
					// TODO Externalize String
					remoteError = "Failed to setWritable: " + file.getAbsolutePath();
				} else {
					remoteError = remoteError.substring(2);
				}
				throw new SystemOperationFailedException(Activator.PLUGIN_ID, remoteError);
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
		catch (FileNotFoundException e) {
			if (!file.exists()) {
				throw new SystemElementNotFoundException(Activator.PLUGIN_ID, file.getAbsolutePath(), "getInputStream");
			} else {
				throw new RemoteFileIOException(e);
			}
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
		int options = isBinary ? IFileService.NONE : IFileService.TEXT_MODE;
		return getOutputStream(remoteParent, remoteFile, options, monitor);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.services.files.AbstractFileService#getOutputStream(java.lang.String, java.lang.String, boolean, int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public OutputStream getOutputStream(String remoteParent, String remoteFile, int options, IProgressMonitor monitor) throws SystemMessageException {
		File file = new File(remoteParent, remoteFile);
		OutputStream stream = null;

		try {
			if ((options & IFileService.APPEND) == 0) {
				stream = new FileOutputStream(file);
			} else {
				stream = new FileOutputStream(file, true);
			}
		}
		catch (FileNotFoundException e) {
			if (!file.exists()) {
				throw new SystemElementNotFoundException(Activator.PLUGIN_ID, file.getAbsolutePath(), "getOutputStream");
			} else {
				throw new RemoteFileIOException(e);
			}
		}
		catch (Exception e) {
			throw new RemoteFileIOException(e);
		}

		return stream;
	}


	public int getCapabilities(IHostFile file) {
		int capabilities = 0;
		if (_isWindows){
			return capabilities; // no windows support
		}
		else if (file instanceof LocalVirtualHostFile) {
			return capabilities; // no virtual file support
		}
		else {
			return FS_CAN_GET_ALL | FS_CAN_SET_ALL;
		}
	}

	public IHostFilePermissions getFilePermissions(IHostFile rfile,
			IProgressMonitor monitor) throws SystemMessageException {
		if (!_isWindows){

			File file = new File(rfile.getParentPath(), rfile.getName());

			// permissions in form  "drwxrwxrwx ..."
			String ldStr = simpleShellCommand("ls -ld", file); //$NON-NLS-1$

			StringTokenizer tokenizer = new StringTokenizer(ldStr, " \t"); //$NON-NLS-1$

			// permissions in form "rwxrwxrwx"
			String permString = tokenizer.nextToken().substring(1);

			// user and group
			tokenizer.nextToken(); // nothing important
			String user = tokenizer.nextToken(); // 3rd
			String group = tokenizer.nextToken(); // 4th

			IHostFilePermissions permissions = new HostFilePermissions(permString, user, group);
			if (rfile instanceof IHostFilePermissionsContainer)
			{
				((IHostFilePermissionsContainer)rfile).setPermissions(permissions);
			}
			return permissions;
		}
		return null;
	}

	public void setFilePermissions(IHostFile rfile,
			IHostFilePermissions newPermissions, IProgressMonitor monitor)
			throws SystemMessageException {
		if (!_isWindows){
			File file = new File(rfile.getParentPath(), rfile.getName());

			int bits = newPermissions.getPermissionBits();
			String permissionsInOctal = Integer.toOctalString(bits); // from decimal to octal
			String user = newPermissions.getUserOwner();
			String group = newPermissions.getGroupOwner();

			// set the permissions
			simpleShellCommand("chmod " + permissionsInOctal, file); //$NON-NLS-1$

			// set the user
			simpleShellCommand("chown " + user, file); //$NON-NLS-1$

			// set the group
			simpleShellCommand("chown :" + group, file); //$NON-NLS-1$
		}
	}


	private String simpleShellCommand(String cmd, File file)
	{
		String result = null;
	    String args[] = new String[3];
        args[0] = "sh"; //$NON-NLS-1$
        args[1] = "-c"; //$NON-NLS-1$
        args[2] = cmd + " " + PathUtility.enQuoteUnix(file.getAbsolutePath()); //$NON-NLS-1$

        BufferedReader childReader = null;
		try {
        	Process childProcess = Runtime.getRuntime().exec(args);

        	childReader = new BufferedReader(new InputStreamReader(childProcess.getInputStream()));

        	result = childReader.readLine().trim();
        	childReader.close();
		}
		catch (Exception e){
			try {
				childReader.close();
			}
			catch (IOException ex){}
		}
		return result;

	}

	private SystemMessageException getCancelledException()
	{
		//This operation has been cancelled by the user.
		return new SystemMessageException(new SimpleSystemMessage(Activator.PLUGIN_ID,
				ICommonMessageIds.MSG_OPERATION_CANCELLED,
			IStatus.CANCEL, CommonMessages.MSG_OPERATION_CANCELLED));
	}

}
