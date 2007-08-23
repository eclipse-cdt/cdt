/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
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
 * Martin Oberhuber (Wind River) - Fix 154874 - handle files with space or $ in the name 
 * Xuan Chen (IBM) - Fix 160768 - [refresh][dstore] Refresh on renamed node within a zip does not work; 
 * Xuan Chen (IBM) - Fix 189487 - copy and paste a folder did not work - workbench hang
 * Xuan Chen (IBM)        - [189681] [dstore][linux] Refresh Folder in My Home messes up Refresh in Root
 * Xuan Chen (IBM)        - [191280] [dstore] Expand fails for folder "/folk" with 3361 children
 * Kevin Doyle (IBM) - [195709] Windows Copying doesn't work when path contains space
 * Kevin Doyle (IBM) - [196211] DStore Move tries rename if that fails copy/delete
 * Xuan Chen (IBM)        - [198046] [dstore] Cannot copy a folder into an archive file
 * Xuan Chen (IBM)        - [191367] with supertransfer on, Drag & Drop Folder from DStore to DStore doesn't work
 * Martin Oberhuber (Wind River) - [199548] Avoid touching files on setReadOnly() if unnecessary
 * Kevin Doyle (IBM) - [191548]  Deleting Read-Only directory removes it from view and displays no error
 *******************************************************************************/

package org.eclipse.rse.dstore.universal.miners;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.dstore.core.miners.Miner;
import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStoreResources;
import org.eclipse.rse.internal.dstore.universal.miners.filesystem.ArchiveQueryThread;
import org.eclipse.rse.internal.dstore.universal.miners.filesystem.ClassFileParser;
import org.eclipse.rse.internal.dstore.universal.miners.filesystem.FileClassifier;
import org.eclipse.rse.internal.dstore.universal.miners.filesystem.FileDescriptors;
import org.eclipse.rse.internal.dstore.universal.miners.filesystem.FileQueryThread;
import org.eclipse.rse.internal.dstore.universal.miners.filesystem.UniversalDownloadHandler;
import org.eclipse.rse.internal.dstore.universal.miners.filesystem.UniversalSearchHandler;
import org.eclipse.rse.services.clientserver.IClientServerConstants;
import org.eclipse.rse.services.clientserver.IServiceConstants;
import org.eclipse.rse.services.clientserver.PathUtility;
import org.eclipse.rse.services.clientserver.SystemFileClassifier;
import org.eclipse.rse.services.clientserver.SystemSearchString;
import org.eclipse.rse.services.clientserver.archiveutils.AbsoluteVirtualPath;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler;
import org.eclipse.rse.services.clientserver.archiveutils.SystemJarHandler;
import org.eclipse.rse.services.clientserver.archiveutils.SystemTarHandler;
import org.eclipse.rse.services.clientserver.archiveutils.SystemZipHandler;
import org.eclipse.rse.services.clientserver.archiveutils.VirtualChild;
import org.eclipse.rse.services.clientserver.java.ClassFileUtil;

public class UniversalFileSystemMiner extends Miner {
    

	public static final String MINER_ID = UniversalFileSystemMiner.class.getName();


//	private DataElement deUFSfilters;

	private DataElement deUFSnode;

//	private DataElement deUFStemp;

	private DataElement deUFSuploadlog;

//	private DataElement dePropertyQuery;

//	private DataElement deFileClassificationQuery;

//	private DataElement deFolderClassificationQuery;
	

	

	protected String filterString = "*"; //$NON-NLS-1$

	protected ArchiveHandlerManager _archiveHandlerManager;

	protected boolean showHidden = false;


	public static final String CLASSNAME = "UniversalFileSystemMiner"; //$NON-NLS-1$

	protected HashMap _cancellableThreads;

	private boolean _isWindows = false;

	public UniversalFileSystemMiner() {
		_cancellableThreads = new HashMap();
		_isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows"); //$NON-NLS-1$ //$NON-NLS-2$
		_archiveHandlerManager = ArchiveHandlerManager.getInstance();
		_archiveHandlerManager.setRegisteredHandler("zip", SystemZipHandler.class); //$NON-NLS-1$
		_archiveHandlerManager.setRegisteredHandler("jar", SystemJarHandler.class); //$NON-NLS-1$
		_archiveHandlerManager.setRegisteredHandler("tar", SystemTarHandler.class); //$NON-NLS-1$
	}

	protected FileClassifier getFileClassifier(DataElement subject)
	{
	    return new FileClassifier(subject);
	}
	
	/**
	 * @see Miner#handleCommand(DataElement)
	 */
	public DataElement handleCommand(DataElement theElement) {
		String name = getCommandName(theElement);

		
		DataElement status = getCommandStatus(theElement);
		DataElement subject = getCommandArgument(theElement, 0);
		
		UniversalServerUtilities.logInfo(getName(), name + ":" + subject); //$NON-NLS-1$
		
		String queryType = (String) subject.getElementProperty(DE.P_TYPE);
		boolean caseSensitive = !_isWindows;
		// TODO: test on WINDOWS!

		if (IUniversalDataStoreConstants.C_QUERY_VIEW_ALL.equals(name)) { 
			    DataElement attributes = getCommandArgument(theElement, 1);
			    if (attributes != null && attributes.getType().equals("attributes")) //$NON-NLS-1$
			    {
			        return handleQueryAll(subject, attributes, status, queryType,
							caseSensitive);
			    }
			    else
			    {
			        return handleQueryAll(subject, null, status, queryType,
						caseSensitive);
			    }
		} else if (IUniversalDataStoreConstants.C_QUERY_VIEW_FILES.equals(name)) { 
			    DataElement attributes = getCommandArgument(theElement, 1);
			    if (attributes != null && attributes.getType().equals("attributes")) //$NON-NLS-1$
			    {
			        return handleQueryFiles(subject, attributes, status, queryType,
							caseSensitive);			        
			    }
			    else
			    {
			        return handleQueryFiles(subject, null, status, queryType,
						caseSensitive);
			    }
		} else if (IUniversalDataStoreConstants.C_QUERY_VIEW_FOLDERS.equals(name)) { 
			    DataElement attributes = getCommandArgument(theElement, 1);
			    if (attributes != null && attributes.getType().equals("attributes")) //$NON-NLS-1$
			    {
			        return handleQueryFolders(subject, attributes, status, queryType,
							caseSensitive);
			    }
			    else
			    {
			        return handleQueryFolders(subject, null, status, queryType,
						caseSensitive);
			    }
		} else if (IUniversalDataStoreConstants.C_QUERY_ROOTS.equals(name)) { 
				return handleQueryRoots(subject, status);
		} else if (IUniversalDataStoreConstants.C_SEARCH.equals(name)) { 
				return handleSearch(theElement, status, queryType,
						caseSensitive);
		} else if (IUniversalDataStoreConstants.C_CANCEL.equals(name)) { 
				subject.getName();
				return handleCancel(subject, status);
		} else if (IUniversalDataStoreConstants.C_RENAME.equals(name)) { 
				return handleRename(subject, status);
		} else if (IUniversalDataStoreConstants.C_DELETE.equals(name)) { 
				return handleDelete(subject, status, true);
		} else if (IUniversalDataStoreConstants.C_DELETE_BATCH.equals(name)) { 
				return handleDeleteBatch(theElement, status);
		} else if (IUniversalDataStoreConstants.C_COPY.equals(name)) { 
				return handleCopy(subject, getCommandArgument(theElement, 1),
						getCommandArgument(theElement, 2), status);
		} else if (IUniversalDataStoreConstants.C_COPY_BATCH.equals(name)) { 
				return handleCopyBatch(subject, theElement, status);
		} else if (IUniversalDataStoreConstants.C_CREATE_FILE.equals(name)) { 
				return handleCreateFile(subject, status, queryType);
		} else if (IUniversalDataStoreConstants.C_CREATE_FOLDER.equals(name)) { 
				return handleCreateFolder(subject, status, queryType);
		} else if (IUniversalDataStoreConstants.C_SET_READONLY.equals(name)) { 
				return handleSetReadOnly(subject, status);
		} else if (IUniversalDataStoreConstants.C_SET_LASTMODIFIED.equals(name)) { 
				return handleSetLastModified(subject, status);
		} else if (IUniversalDataStoreConstants.C_QUERY_BASIC_PROPERTY.equals(name)) { 
				return handleQueryBasicProperty(subject, status);
		} else if (IUniversalDataStoreConstants.C_QUERY_CAN_WRITE_PROPERTY.equals(name)) {
				return handleQuerycanWriteProperty(subject, status);
		} else if (IUniversalDataStoreConstants.C_QUERY_ADVANCE_PROPERTY.equals(name)) {
				return handleQueryAdvanceProperty(subject, status);
		} else if (IUniversalDataStoreConstants.C_QUERY_FILE_CLASSIFICATIONS.equals(name)) { 
				return handleQueryFileClassification(subject, status);
		} else if (IUniversalDataStoreConstants.C_QUERY_FILE_CLASSIFICATION.equals(name)) {
				return handleQueryFileClassification(subject, status);
		} else if (IUniversalDataStoreConstants.C_QUERY_EXISTS.equals(name)) { 
				return handleQueryExists(subject, status, queryType);
		} else if (IUniversalDataStoreConstants.C_QUERY_GET_REMOTE_OBJECT.equals(name)) { 
				return handleQueryGetRemoteObject(subject, status, queryType);
		} else if (IUniversalDataStoreConstants.C_GET_OSTYPE.equals(name)) { 
				return handleGetOSType(subject, status);
		} else if (IUniversalDataStoreConstants.C_DOWNLOAD_FILE.equals(name)) {
				return handleDownload(theElement, status);
		} else if (IUniversalDataStoreConstants.C_SYSTEM_ENCODING.equals(name)) {
				return handleQueryEncoding(subject, status);
		} else if (IUniversalDataStoreConstants.C_QUERY_UNUSED_PORT.equals(name)) {
				return handleQueryUnusedPort(subject, status);
		} else if (IUniversalDataStoreConstants.C_QUERY_CLASSNAME.equals(name)) { 
				return handleQueryClassName(subject, status);
		} else if (IUniversalDataStoreConstants.C_QUERY_QUALIFIED_CLASSNAME.equals(name)) {
				return handleQueryQualifiedClassName(subject, status);
		} else {
			UniversalServerUtilities.logError(CLASSNAME,
					"Invalid query to handlecommand", null); //$NON-NLS-1$
		}
		return statusDone(status);
	}

	private DataElement handleCopyBatch(DataElement targetFolder, DataElement theElement, DataElement status) 
	{
		String targetType = targetFolder.getType();
		File tgtFolder = getFileFor(targetFolder);
		int numOfSources = theElement.getNestedSize() - 2;
		
		if (targetType.equals(IUniversalDataStoreConstants.UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR) || targetType.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR)) 
		{
		    // if target is virtual or an archive, insert into an archive
			AbsoluteVirtualPath vpath = getAbsoluteVirtualPath(targetFolder);
			ISystemArchiveHandler handler = getArchiveHandlerFor(vpath.getContainingArchiveString());
			boolean result = true;
			
			if (handler == null) 
			{
				status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
				return statusDone(status);
			}

			List nonDirectoryArrayList = new ArrayList();
			List nonDirectoryNamesArrayList = new ArrayList();
			
			String virtualContainer = ""; //$NON-NLS-1$
			
			if (targetType.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR)) 
			{
				virtualContainer = vpath.getVirtualPart();
			}
			
			for (int i = 0; i < numOfSources; i++)
			{
				DataElement sourceFile = getCommandArgument(theElement, i+1);
				String srcType = sourceFile.getType();
				String srcName = sourceFile.getName();
				File srcFile;

				if (srcType.equals(IUniversalDataStoreConstants.UNIVERSAL_FILE_DESCRIPTOR) || srcType.equals(IUniversalDataStoreConstants.UNIVERSAL_FOLDER_DESCRIPTOR)
					|| srcType.equals(IUniversalDataStoreConstants.UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR)) 
				{		
					srcFile = getFileFor(sourceFile);
				}
				else if (srcType.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR) || srcType.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR)) 
				{
					AbsoluteVirtualPath svpath = getAbsoluteVirtualPath(sourceFile);
					ISystemArchiveHandler shandler = getArchiveHandlerFor(svpath.getContainingArchiveString());
				
					if (shandler == null) 
					{
						status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
						return statusDone(status);
					}
				
					VirtualChild child = shandler.getVirtualFile(svpath.getVirtualPart());
					srcFile = child.getExtractedFile();
				}
				else {
					//invalid source type
					status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
					return statusDone(status);
				}
				
				//If this source file object is directory, we will call ISystemArchiveHandler#add(File ...) method to 
				//it and all its descendants into the archive file.
				//If this source file object is not a directory, we will add it into a list, and then
				//call ISystemArchiveHandler#add(File[] ...) to add them in batch.
				if (srcFile.isDirectory())
				{
					result = handler.add(srcFile, virtualContainer, srcName);
					if (!result) {
						status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
						return statusDone(status);
					}
				}
				else
				{
					nonDirectoryArrayList.add(srcFile);
					nonDirectoryNamesArrayList.add(srcName);
				}
			}
			
			if (nonDirectoryArrayList.size() > 0)
			{
				File[] resultFiles = (File[])nonDirectoryArrayList.toArray(new File[nonDirectoryArrayList.size()]);
				String[] resultNames = (String[])nonDirectoryNamesArrayList.toArray(new String[nonDirectoryNamesArrayList.size()]);
				//we need to add those files into the archive file as well.
				result = handler.add(resultFiles, virtualContainer, resultNames);
			}
			
			if (result)
			{
				status.setAttribute(DE.A_SOURCE, IServiceConstants.SUCCESS);
			}
			else
			{
				status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
			}
			return statusDone(status);
		}
		else // target is a regular folder
		{
			boolean folderCopy = false;
			String source = ""; //$NON-NLS-1$
			String tgt = enQuote(tgtFolder.getAbsolutePath());

			int numOfNonVirtualSources = 0;
			for (int i = 0; i < numOfSources; i++)
			{
				DataElement sourceFile = getCommandArgument(theElement, i+1);
				String srcType = sourceFile.getType();
				
				if (srcType.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR) || srcType.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR)) 
				{
					// extract from an archive to folder
					AbsoluteVirtualPath svpath = getAbsoluteVirtualPath(sourceFile);
					ISystemArchiveHandler shandler = getArchiveHandlerFor(svpath.getContainingArchiveString());
			
					if (shandler == null) 
					{
						status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
						return statusDone(status);
					}
			
					VirtualChild child = shandler.getVirtualFile(svpath.getVirtualPart());

					File parentDir = getFileFor(targetFolder);
					File destination = new File(parentDir, sourceFile.getName());
			
					if (child.isDirectory) 
					{
						shandler.extractVirtualDirectory(svpath.getVirtualPart(), parentDir, destination);
					}
					else 
					{
						shandler.extractVirtualFile(svpath.getVirtualPart(), destination);
					}
				}
				else // source is regular file or folder
				{
					File srcFile = getFileFor(sourceFile);
					folderCopy = folderCopy || srcFile.isDirectory();
					String src = srcFile.getAbsolutePath();
				
					// handle special characters in source and target strings 
					src = enQuote(src);
					
					// handle window case separately, since xcopy command could not handler
					// multiple source names
					if (_isWindows)
					{
						tgt = tgtFolder.getAbsolutePath() + File.separatorChar + srcFile.getName();
						// Both unix and windows need src quoted, so it's already done
						doCopyCommand(src, enQuote(tgt), folderCopy, status);
						if (status.getAttribute(DE.A_SOURCE) == IServiceConstants.FAILED)
						{
							break;
						}
						continue;
					}
					if (numOfNonVirtualSources == 0)
					{
						source += src;
					}
					else
					{
						source = source + " " + src; //$NON-NLS-1$
					}
					numOfNonVirtualSources++;
				} 
			} // end for loop iterating through sources
			
			if (numOfNonVirtualSources > 0)
			{
				doCopyCommand(source, tgt, folderCopy, status);
			} 
		} // end if/then/else (target is regular folder)
		return statusDone(status);
	}

	protected void doCopyCommand(String source, String tgt, boolean folderCopy, DataElement status)
	{
		String command = null;
		if (_isWindows) {
			
			if (folderCopy) {
				command = "xcopy " + source //$NON-NLS-1$
					+ " " + tgt //$NON-NLS-1$
					+ " /S /E /K /Q /H /I /Y"; //$NON-NLS-1$
			}
			else {
				String unquotedTgt = tgt.substring(1, tgt.length() - 1);
				
				File targetFile = new File(unquotedTgt);
				if (!targetFile.exists())
				{
					// create file so as to avoid ambiguity
					try
					{
						targetFile.createNewFile();
					}
					catch (Exception e)
					{
						status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
						status.setAttribute(DE.A_VALUE, e.getMessage());		
						return;
					}
				}				
				command = "xcopy " + source + " " + tgt + " /Y /K /Q /H"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
			}
		}
		else {
			if (folderCopy) {
				command = "cp  -Rp " + source + " " + tgt; //$NON-NLS-1$ //$NON-NLS-2$
			}
			else {
				command = "cp -p " + source + " " + tgt; //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		// run copy command
		try
		{	
			Runtime runtime = Runtime.getRuntime();
			Process p = null;
				
			if (_isWindows)
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
			
			// ensure there is a process
			if (p != null) {
			    
			    // wait for process to finish
			    p.waitFor();
			    
			    // get the exit value of the process
			    int result = p.exitValue();
			    
			    // if the exit value is not 0, then the process did not terminate normally
			    if (result != 0) {
			        
			        // get the error stream
					InputStream errStream = p.getErrorStream();
					
					// error buffer
					StringBuffer errBuf = new StringBuffer();
					
					byte[] bytes = null;
					
					int numOfBytesRead = 0;
					
					int available = errStream.available();
					
					// read error stream and store in error buffer
					while (available > 0) {
						
						bytes = new byte[available];
						
						numOfBytesRead = errStream.read(bytes);
						
						if (numOfBytesRead > -1) {
						    errBuf.append(new String(bytes, 0, numOfBytesRead));
						}
						else {
						    break;
						}
						
						available = errStream.available();
					}
					
					String err = errBuf.toString();
					
					// omit new line if there is one at the end because datastore does not
					// handle new line in the attributes
					// TODO: what to do if newline occurs in the middle of the string?
					String newLine = System.getProperty("line.separator"); //$NON-NLS-1$
					
					if (newLine != null && err.endsWith(newLine)) {
					    err = err.substring(0, err.length() - newLine.length());
					}
					
					// if there is something in error buffer
					// there was something in the error stream of the process
					if (err.length() > 0) {
						status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
						status.setAttribute(DE.A_VALUE, err);
					}
					// otherwise, nothing in the error stream
					// but we know process did not exit normally, so we indicate an unexpected error
					else {
						status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
						status.setAttribute(DE.A_VALUE, IServiceConstants.UNEXPECTED_ERROR);
					}
			    }
			    // otherwise if exit value is 0, process terminated normally
			    else {
					status.setAttribute(DE.A_SOURCE, IServiceConstants.SUCCESS);
			    }
			}
			// no process, so something is wrong
			else {
				status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
				status.setAttribute(DE.A_VALUE, IServiceConstants.UNEXPECTED_ERROR);					
			}
		}
		catch (Exception e)
		{
			UniversalServerUtilities.logError(CLASSNAME, "Exception is handleCopy", e); //$NON-NLS-1$
			status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
			status.setAttribute(DE.A_VALUE, e.getMessage());
		}	
	}
	
	/**
	 * Delete directory and its children.
	 *  
	 */
	public void deleteDir(File fileObj, DataElement status) {
		try {
			File list[] = fileObj.listFiles();
			for (int i = 0; i < list.length; ++i) {
				if (list[i].isFile()) {
					if (!(list[i].delete())) {
						status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
						UniversalServerUtilities.logWarning(CLASSNAME,
								"Deletion of dir failed"); //$NON-NLS-1$
					}
				} else {
					deleteDir(list[i], status);
					if (!(list[i].delete())) {
						status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
						UniversalServerUtilities.logWarning(CLASSNAME,
								"Deletion of dir failed"); //$NON-NLS-1$
					}
				}
			}
		} catch (Exception e) {
			status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED_WITH_EXCEPTION);
			status.setAttribute(DE.A_VALUE, e.getLocalizedMessage());
			UniversalServerUtilities.logError(CLASSNAME,
					"Deletion of dir failed", e); //$NON-NLS-1$
		}
	}

	/**
	 * Method to do a search.
	 */
	public DataElement handleSearch(DataElement theElement, DataElement status,
			String queryType, boolean fileNamesCaseSensitive) {
		File fileobj = null;

		DataElement subject = getCommandArgument(theElement, 0);

		// if the query type is against a folder, archive or a virtual folder,
		// we know to handle it
		if (queryType.equals(IUniversalDataStoreConstants.UNIVERSAL_FOLDER_DESCRIPTOR)
				|| queryType.equals(IUniversalDataStoreConstants.UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR)
				|| queryType.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR)) {

			fileobj = new File(subject.getAttribute(DE.A_VALUE)
					+ File.separatorChar + subject.getName());
		}
		// otherwise log error, and return as done
		else {
			UniversalServerUtilities.logError(CLASSNAME, "Invalid query type to handleSearch", null); //$NON-NLS-1$
			return statusDone(status);
		}

		if (fileobj.exists()) {
			DataElement arg1 = getCommandArgument(theElement, 1);
			DataElement arg2 = getCommandArgument(theElement, 2);
			DataElement arg3 = getCommandArgument(theElement, 3);

			String textString = arg1.getType();
			boolean isCaseSensitive = Boolean.valueOf(arg1.getName()).booleanValue();
			boolean isTextRegex = Boolean.valueOf(arg1.getSource()).booleanValue();
			
			String fileNamesString = arg2.getType();
			
			boolean isFileNamesRegex = Boolean.valueOf(arg2.getName()).booleanValue();
			String classification = arg2.getSource();
			
			boolean isIncludeArchives = Boolean.valueOf(arg3.getType()).booleanValue();
			boolean isIncludeSubfolders = Boolean.valueOf(arg3.getName()).booleanValue();
//			boolean showHidden = Boolean.valueOf(arg3.getSource()).booleanValue();
			Boolean.valueOf(arg3.getSource()).booleanValue();
			
			SystemSearchString searchString = new SystemSearchString(
					textString, isCaseSensitive, isTextRegex, fileNamesString,
					isFileNamesRegex, isIncludeArchives, isIncludeSubfolders, classification);
			
			UniversalSearchHandler searchThread = new UniversalSearchHandler(
					_dataStore, this, searchString, !_isWindows, fileobj,
					status);
			
			searchThread.start();

			updateCancellableThreads(status.getParent(), searchThread);
			return status;
		}

		return statusDone(status);
	}

	public DataElement handleCancel(DataElement subject, DataElement status) {
		ICancellableHandler thread = (ICancellableHandler) _cancellableThreads
				.get(subject);

		
		if (thread != null) {
			if (!thread.isDone()) {
				thread.cancel();
			}
		}

		// indicate status cancelled before indicating we are done
		statusCancelled(status);

		// indicate status done
		return statusDone(status);
	}
	

	/**
	 * Method to list the files and folders for a given filter.
	 */
	public DataElement handleQueryAll(DataElement subject, DataElement attributes, DataElement status,
			String queryType, boolean caseSensitive) 
	{
		if (queryType.equals(IUniversalDataStoreConstants.UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR)
				|| queryType.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR)
				|| queryType.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR)) {
			return handleQueryAllArchive(subject, attributes, status, caseSensitive, false);
		}

		File fileobj = null;


		String filter = null;
		if (attributes != null)
		{
			filter = getFilterString(attributes.getAttribute(DE.A_SOURCE));
			showHidden = getShowHiddenFlag(attributes.getAttribute(DE.A_SOURCE));
		}
		else
		{
		    filter = getFilterString(subject.getAttribute(DE.A_SOURCE));
			showHidden = getShowHiddenFlag(subject.getAttribute(DE.A_SOURCE));
		}
		
		if (queryType.equals(IUniversalDataStoreConstants.UNIVERSAL_FILTER_DESCRIPTOR))
			fileobj = new File(subject.getName());
		else if (queryType.equals(IUniversalDataStoreConstants.UNIVERSAL_FOLDER_DESCRIPTOR))
			fileobj = new File(subject.getAttribute(DE.A_VALUE)
					+ File.separatorChar + subject.getName());
		else
		{
			UniversalServerUtilities.logError(CLASSNAME,
					"Invalid query type to handleQueryAll", null); //$NON-NLS-1$
		}

		if (fileobj != null)
		{
			if (!fileobj.exists())
			{
				subject.setAttribute(DE.A_TYPE, IUniversalDataStoreConstants.UNIVERSAL_FILTER_DESCRIPTOR);
				subject.setAttribute(DE.A_SOURCE, setProperties(fileobj));
				status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED_WITH_DOES_NOT_EXIST);	
				
				if (subject.getNestedSize() > 0)
				{
					List children = subject.getNestedData();
					for (int i = children.size() - 1; i >= 0; i--)
					{
						_dataStore.deleteObject(subject, (DataElement)children.get(i));
					}					
				}
				_dataStore.refresh(subject);
			}
			else
			{
				// query all files and folders for the filter
				internalQueryAll(subject, fileobj, queryType, filter,
						caseSensitive, IClientServerConstants.INCLUDE_ALL, status);
				return status; // query done in a thread so don't mark done
			}
		}
		
		return statusDone(status);
	}

	protected void internalQueryAll(DataElement subject, File fileobj,
			String queryType, String filter, boolean caseSensitive,
			int inclusion, DataElement status) {
		
		// do query on a thread
		FileQueryThread queryThread = new FileQueryThread(subject, fileobj, queryType, filter, caseSensitive, inclusion, showHidden, _isWindows, status);
		queryThread.start();		
		
		updateCancellableThreads(status.getParent(), queryThread);
	}

	private void updateCancellableThreads(DataElement command, ICancellableHandler thread)
	{
		//First Check to make sure that there are no "zombie" threads
		Iterator iter = _cancellableThreads.keySet().iterator();
		try
		{
			while (iter.hasNext())
			{
				String threadName = (String) iter.next();
				ICancellableHandler theThread = (ICancellableHandler) _cancellableThreads.get(threadName);
				if ((theThread == null) || 
						theThread.isDone() || theThread.isCancelled())
				{
					_cancellableThreads.remove(threadName);
				}
			}
		}
		catch (Exception e)
		{
			_dataStore.trace(e);
		}
		// save find thread in hashmap for retrieval during cancel
		_cancellableThreads.put(command, thread);
	}
	
	
	  /**
		    * Method to list the files for a given filter.
		    */
	public DataElement handleQueryFiles(DataElement subject, DataElement attributes, 
			DataElement status, String queryType, boolean caseSensitive) {

		File fileobj = null;

		String filter = null;
		if (attributes != null)
		{
			filter = getFilterString(attributes.getAttribute(DE.A_SOURCE));
			showHidden = getShowHiddenFlag(attributes.getAttribute(DE.A_SOURCE));
		}
		else
		{
		    filter = getFilterString(subject.getAttribute(DE.A_SOURCE));
			showHidden = getShowHiddenFlag(subject.getAttribute(DE.A_SOURCE));
		}
		
		if (queryType.equals(IUniversalDataStoreConstants.UNIVERSAL_FILTER_DESCRIPTOR))
			fileobj = new File(subject.getName());
		else if (queryType.equals(IUniversalDataStoreConstants.UNIVERSAL_FOLDER_DESCRIPTOR))
			fileobj = new File(subject.getAttribute(DE.A_VALUE)
					+ File.separatorChar + subject.getName());
		else
			UniversalServerUtilities.logError(CLASSNAME,
					"Invalid query type to handleQueryFiles", null); //$NON-NLS-1$


		if (!fileobj.exists())
		{
			subject.setAttribute(DE.A_TYPE, IUniversalDataStoreConstants.UNIVERSAL_FILTER_DESCRIPTOR);
			subject.setAttribute(DE.A_SOURCE, setProperties(fileobj));
			status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED_WITH_DOES_NOT_EXIST);	
			if (subject.getNestedSize() > 0)
			{
				List children = subject.getNestedData();
				for (int i = children.size() - 1; i >= 0; i--)
				{
					_dataStore.deleteObject(subject, (DataElement)children.get(i));
				}
			}
		}
		else
		{
			internalQueryAll(subject, fileobj, queryType, filter, caseSensitive, IClientServerConstants.INCLUDE_FILES_ONLY, status);
			return status; // query done in a thread so not marking done here
		}
		return statusDone(status);
	}

	/**
	 * Method to list the folders for a given filter.
	 */
	public DataElement handleQueryFolders(DataElement subject, DataElement attributes,
			DataElement status, String queryType, boolean caseSensitive) {
		if (queryType.equals(IUniversalDataStoreConstants.UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR)
				|| queryType.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR)
				|| queryType.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR)) {
			return handleQueryAllArchive(subject, attributes, status, caseSensitive, true);
		}

		File fileobj = null;
		String filter = null;
		
		if (attributes != null)
		{
			filter = getFilterString(attributes.getAttribute(DE.A_SOURCE));
			showHidden = getShowHiddenFlag(attributes.getAttribute(DE.A_SOURCE));
		}
		else
		{
		    filter = getFilterString(subject.getAttribute(DE.A_SOURCE));
			showHidden = getShowHiddenFlag(subject.getAttribute(DE.A_SOURCE));
		}

		if (queryType.equals(IUniversalDataStoreConstants.UNIVERSAL_FILTER_DESCRIPTOR))
			fileobj = new File(subject.getName());
		else if (queryType.equals(IUniversalDataStoreConstants.UNIVERSAL_FOLDER_DESCRIPTOR))
			fileobj = new File(subject.getAttribute(DE.A_VALUE)
					+ File.separatorChar + subject.getName());
		else
			UniversalServerUtilities.logError(CLASSNAME,
					"Invalid query type to handleQueryFolders", null); //$NON-NLS-1$

		if (!fileobj.exists())
		{
			subject.setAttribute(DE.A_TYPE, IUniversalDataStoreConstants.UNIVERSAL_FILTER_DESCRIPTOR);
			subject.setAttribute(DE.A_SOURCE, setProperties(fileobj));
			status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED_WITH_DOES_NOT_EXIST);	
			if (subject.getNestedSize() > 0)
			{
				List children = subject.getNestedData();
				for (int i = children.size() - 1; i >= 0; i--)
				{
					_dataStore.deleteObject(subject, (DataElement)children.get(i));
				}
			}
		}
		else
		{
			internalQueryAll(subject, fileobj, queryType, filter, caseSensitive, IClientServerConstants.INCLUDE_FOLDERS_ONLY, status);
			return status; // query done in a thread so not marking done here
		}
		
		return statusDone(status);
	}

	/**
	 * Method to list the roots.
	 */
	public DataElement handleQueryRoots(DataElement subject, DataElement status) {
//		File fileobj = new File(subject.getName());
		new File(subject.getName());
		DataElement deObj = null;

		if (System.getProperty("os.name").toLowerCase().startsWith("windows")) { //$NON-NLS-1$ //$NON-NLS-2$
			String[] ALLDRIVES = { "c:\\", "d:\\", "e:\\", "f:\\", "g:\\", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
					"h:\\", "i:\\", "j:\\", "k:\\", "l:\\", "m:\\", "n:\\", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
					"o:\\", "p:\\", "q:\\", "r:\\", "s:\\", "t:\\", "u:\\", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
					"v:\\", "w:\\", "x:\\", "y:\\", "z:\\" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			for (int idx = 0; idx < ALLDRIVES.length; idx++) {
				File drive = new File(ALLDRIVES[idx]);
				if (drive.exists()) {
					try {
						String path = drive.getCanonicalPath();
						deObj = _dataStore.createObject(subject,
								IUniversalDataStoreConstants.UNIVERSAL_FOLDER_DESCRIPTOR, path);
						deObj.setAttribute(DE.A_SOURCE, setProperties(drive));
						deObj.setAttribute(DE.A_NAME, ""); //$NON-NLS-1$
						deObj.setAttribute(DE.A_VALUE, path);
					} catch (IOException e) {
						return statusDone(status);
					}
				}
			}
		} else { // not windows
			File[] list = File.listRoots();

			for (int i = 0; i < list.length; ++i) {
				deObj = _dataStore.createObject(subject,
						IUniversalDataStoreConstants.UNIVERSAL_FOLDER_DESCRIPTOR, list[i].getAbsolutePath());
				deObj.setAttribute(DE.A_SOURCE, setProperties(list[i]));
				deObj.setAttribute(DE.A_NAME, ""); //$NON-NLS-1$
				deObj.setAttribute(DE.A_VALUE, list[i].getAbsolutePath());
			}
		}
		_dataStore.refresh(subject);
		return statusDone(status);
	}

	/**
	 * Method to Delete a file or folder.
	 */
	public DataElement handleDelete(DataElement subject, DataElement status, boolean refreshDataStore) {
		String type = subject.getType();
		if (type.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR)
				|| type.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR)) {
			return handleDeleteFromArchive(subject, status);
		}

		File deleteObj = new File(subject.getAttribute(DE.A_VALUE)
				+ File.separatorChar + subject.getName());
		DataElement deObj = null;
		if (!deleteObj.exists()) {
			status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED_WITH_DOES_NOT_EXIST + "|" + deleteObj.getAbsolutePath()); //$NON-NLS-1$
			UniversalServerUtilities.logError(CLASSNAME,
					"The object to delete does not exist", null); //$NON-NLS-1$
		} else {
			try {
				if (deleteObj.isFile()) {
					if (deleteObj.delete() == false) {
						status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED + "|" + deleteObj.getAbsolutePath()); //$NON-NLS-1$
					} else {
						// delete was successful and delete the object from the
						// datastore
						deObj = _dataStore.find(subject, DE.A_NAME, subject
								.getName(), 1);
						_dataStore.deleteObject(subject, deObj);
						status.setAttribute(DE.A_SOURCE, IServiceConstants.SUCCESS + "|" + deleteObj.getAbsolutePath()); //$NON-NLS-1$
					}
					_dataStore.refresh(subject);
				} else if (deleteObj.isDirectory()) { // it is directory and
													  // need to delete the
													  // entire directory +
					// children
					deleteDir(deleteObj, status);
					if (deleteObj.delete() == false) {
						status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED + "|" + deleteObj.getAbsolutePath()); //$NON-NLS-1$
						UniversalServerUtilities.logError(CLASSNAME,
								"Deletion of dir fialed", null); //$NON-NLS-1$
					} else {
						_dataStore.deleteObjects(subject);
						DataElement parent = subject.getParent();
						_dataStore.deleteObject(parent, subject);
						_dataStore.refresh(parent);
						status.setAttribute(DE.A_SOURCE, IServiceConstants.SUCCESS + "|" + deleteObj.getAbsolutePath()); //$NON-NLS-1$
					}
				} else {
					UniversalServerUtilities
							.logError(
									CLASSNAME,
									"The object to delete is neither a File or Folder! in handleDelete", //$NON-NLS-1$
									null);
				}
			} catch (Exception e) {
				status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED_WITH_EXCEPTION + "|" + deleteObj.getAbsolutePath()); //$NON-NLS-1$
				status.setAttribute(DE.A_VALUE, e.getLocalizedMessage());
				UniversalServerUtilities.logError(CLASSNAME,
						"Delete of the object failed", e); //$NON-NLS-1$
			}
		}
		_dataStore.refresh(subject);
		return statusDone(status);
	}
	
	private DataElement handleDeleteBatch(DataElement theElement, DataElement status)
	{
		DataElement substatus = _dataStore.createObject(null, "status", "substatus"); //$NON-NLS-1$ //$NON-NLS-2$
		int numOfSources = theElement.getNestedSize() - 2;
		for (int i = 0; i < numOfSources; i++)
		{
			DataElement subject = getCommandArgument(theElement, i+1);
			handleDelete(subject, substatus, false);
			/*
			if (!substatus.getSource().startsWith(IServiceConstants.SUCCESS)) 
			{
				status.setAttribute(DE.A_SOURCE, substatus.getSource());
				return statusDone(status);
			}
			*/
		}
		status.setAttribute(DE.A_SOURCE, substatus.getSource());
		return statusDone(status);
	}

	/**
	 * Method to Rename a file or folder.
	 */
	public DataElement handleRename(DataElement subject, DataElement status) {
		File fileoldname = new File(subject.getAttribute(DE.A_VALUE)
				+ File.separatorChar + subject.getName());
		File filerename = new File(subject.getAttribute(DE.A_SOURCE));

	//	System.out.println(ArchiveHandlerManager.isVirtual(fileoldname
		//		.getAbsolutePath()));
		if (ArchiveHandlerManager.isVirtual(fileoldname.getAbsolutePath())) {
			AbsoluteVirtualPath oldAbsPath = new AbsoluteVirtualPath(
					fileoldname.getAbsolutePath());
			AbsoluteVirtualPath newAbsPath = new AbsoluteVirtualPath(filerename
					.getAbsolutePath());
			ISystemArchiveHandler handler = _archiveHandlerManager
					.getRegisteredHandler(new File(oldAbsPath
							.getContainingArchiveString()));
			boolean success = !(handler == null)
					&& handler.fullRename(oldAbsPath.getVirtualPart(),
							newAbsPath.getVirtualPart());
			if (success && handler != null) {
				subject.setAttribute(DE.A_NAME, filerename.getName());
				subject.setAttribute(DE.A_SOURCE, setProperties(handler
						.getVirtualFile(newAbsPath.getVirtualPart())));
				status.setAttribute(DE.A_SOURCE, IServiceConstants.SUCCESS);
				_dataStore.update(subject);
			} else {
				status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
			}
			_dataStore.refresh(subject);
			return statusDone(status);
		}
		if (filerename.exists())
			status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED_WITH_EXIST);
		else {
			try {
				boolean done = fileoldname.renameTo(filerename);
				if (done) {
					subject.setAttribute(DE.A_NAME, filerename.getName());
					subject
							.setAttribute(DE.A_SOURCE,
									setProperties(filerename));
					status.setAttribute(DE.A_SOURCE, IServiceConstants.SUCCESS);

					if (filerename.isDirectory()) {
						// update children's properties
						updateChildProperties(subject, filerename);
					}
					_dataStore.update(subject);
				} else
					status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
			} catch (Exception e) {
				status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
				UniversalServerUtilities.logError(CLASSNAME,
						"handleRename failed", e); //$NON-NLS-1$
			}
		}
		_dataStore.refresh(subject);
		return statusDone(status);
	}

	// DKM: during folder rename we need to recursively update all the parent
	// paths
	private void updateChildProperties(DataElement subject, File filerename) {

		int nestedSize = subject.getNestedSize();
		for (int i = 0; i < nestedSize; i++) {
			DataElement child = subject.get(i);
			child.setAttribute(DE.A_VALUE, filerename.getAbsolutePath());

			if (child.getNestedSize() > 0) {
				File childFile = new File(filerename, child.getName());
				updateChildProperties(child, childFile);
			}
		}
	}

	/**
	 * Method to create a new file.
	 */
	public DataElement handleCreateFile(DataElement subject,
			DataElement status, String queryType) {
		boolean wasFilter = queryType.equals(IUniversalDataStoreConstants.UNIVERSAL_FILTER_DESCRIPTOR);
		if (queryType.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR)) {
			return handleCreateVirtualFile(subject, status, queryType);
		}

		File filename = null;
		if (queryType.equals(IUniversalDataStoreConstants.UNIVERSAL_FILTER_DESCRIPTOR)) {
			if (subject.getName().indexOf(
					ArchiveHandlerManager.VIRTUAL_SEPARATOR) > 0) {
				subject.setAttribute(DE.A_TYPE,
						IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR);
				return handleCreateVirtualFile(subject, status, queryType);
			} else {
				filename = new File(subject.getValue());
				subject.setAttribute(DE.A_TYPE, IUniversalDataStoreConstants.UNIVERSAL_FILE_DESCRIPTOR);
				subject.setAttribute(DE.A_SOURCE, setProperties(filename));
			}
		} else if (queryType.equals(IUniversalDataStoreConstants.UNIVERSAL_FILE_DESCRIPTOR))
			filename = new File(subject.getAttribute(DE.A_VALUE)
					+ File.separatorChar + subject.getName());
		else
			UniversalServerUtilities.logError(CLASSNAME,
					"Invalid query type to handleCreateFile", null); //$NON-NLS-1$

		if (filename != null)
		{
			if (filename.exists())
				status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED_WITH_EXIST);
			else {
				try {
					boolean done = filename.createNewFile();
					if (ArchiveHandlerManager.getInstance().isArchive(filename)) {
						done = ArchiveHandlerManager.getInstance()
								.createEmptyArchive(filename);
						if (done)
							subject.setAttribute(DE.A_TYPE,
									IUniversalDataStoreConstants.UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR);
					} else {
						if (done)
						{
							subject.setAttribute(DE.A_TYPE,
									IUniversalDataStoreConstants.UNIVERSAL_FILE_DESCRIPTOR);
						}
					}
					subject.setAttribute(DE.A_SOURCE, setProperties(filename));
					if (done) {
						status.setAttribute(DE.A_SOURCE, IServiceConstants.SUCCESS);
						if (wasFilter) {
							String fullName = subject.getValue();
							String name = fullName.substring(fullName
									.lastIndexOf(File.separatorChar) + 1, fullName
									.length());
							String path = fullName.substring(0, fullName
									.lastIndexOf(File.separatorChar));
							subject.setAttribute(DE.A_NAME, name);
							subject.setAttribute(DE.A_VALUE, path);
						}
					} else
						status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
				} catch (Exception e) {
					UniversalServerUtilities.logError(CLASSNAME,
							"handleCreateFile failed", e); //$NON-NLS-1$
					status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
				}
			}
		}
		_dataStore.refresh(subject);
		return statusDone(status);
	}

	/**
	 * Method to create a new folder.
	 */
	public DataElement handleCreateFolder(DataElement subject,
			DataElement status, String queryType) {
		if (queryType.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR)) {
			return handleCreateVirtualFolder(subject, status, queryType);
		}

		File filename = null;
		if (queryType.equals(IUniversalDataStoreConstants.UNIVERSAL_FILTER_DESCRIPTOR)) 
		{
			if (subject.getName().indexOf(
					ArchiveHandlerManager.VIRTUAL_SEPARATOR) > 0) 
			{
				subject.setAttribute(DE.A_TYPE,
						IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR);
				return handleCreateVirtualFolder(subject, status, queryType);
			} 
			else 
			{
				filename = new File(subject.getValue());
				subject.setAttribute(DE.A_TYPE, IUniversalDataStoreConstants.UNIVERSAL_FOLDER_DESCRIPTOR);
				subject.setAttribute(DE.A_SOURCE, setProperties(filename));
			}
		} 
		else if (queryType.equals(IUniversalDataStoreConstants.UNIVERSAL_FOLDER_DESCRIPTOR))
		{
			filename = new File(subject.getValue());
		}
		else
			UniversalServerUtilities.logError(CLASSNAME,
					"Invalid query type to handleCreateFolder", null); //$NON-NLS-1$

		if (filename != null)
		{
			if (filename.exists())
				status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED_WITH_EXIST);
			else 
			{
				try {
					boolean done = filename.mkdirs();
					if (done) 
					{
						status.setAttribute(DE.A_SOURCE, IServiceConstants.SUCCESS);
						subject.setAttribute(DE.A_SOURCE, setProperties(filename));
						subject.setAttribute(DE.A_TYPE,IUniversalDataStoreConstants.UNIVERSAL_FOLDER_DESCRIPTOR);
						subject.setAttribute(DE.A_NAME, filename.getName());
						subject.setAttribute(DE.A_VALUE, filename.getParentFile().getAbsolutePath());
					} 
					else
					{
						status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
					}
					
				} catch (Exception e) {
					UniversalServerUtilities.logError(CLASSNAME,
							"handleCreateFolder failed", e); //$NON-NLS-1$
					status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
				}
			}
		}
		_dataStore.refresh(subject);
		return statusDone(status);
	}

	/**
	 * Method to set ReadOnly to a file or folder.
	 */
	public DataElement handleSetReadOnly(DataElement subject, DataElement status) {

		File filename = new File(subject.getAttribute(DE.A_VALUE), subject.getAttribute(DE.A_NAME));
		
		if (!filename.exists())
		{
			filename = new File(subject.getAttribute(DE.A_VALUE));
		}
		if (!filename.exists())
			status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED_WITH_DOES_NOT_EXIST);
		else {
			try {
				String str = subject.getAttribute(DE.A_SOURCE);
				boolean readOnly = "true".equals(str); //$NON-NLS-1$
				boolean done = false;
				if (readOnly != filename.canWrite())
				{
					done = true;
				}
				else if (readOnly)
				{
					done = filename.setReadOnly();
				}
				else
				{
					// doesn't handle non-unix
					if (!_isWindows)
					{
						// make this read-write
						String[] cmd = new String[3];
						cmd[0] = "chmod"; //$NON-NLS-1$
						cmd[1] = "u+w"; //$NON-NLS-1$
						cmd[2] = filename.getAbsolutePath();
						Process p = Runtime.getRuntime().exec(cmd);
						int exitValue = p.waitFor();
						done = (exitValue == 0);
					}
					else
					{
						// windows version
						String[] cmd = new String[3];
						cmd[0] = "attrib"; //$NON-NLS-1$
						cmd[1] = "-R"; //$NON-NLS-1$
						cmd[2] = filename.getAbsolutePath();
						Process p = Runtime.getRuntime().exec(cmd);
						int exitValue = p.waitFor();
						done = (exitValue == 0);
					}
				}
				if (done) 
				{
					status.setAttribute(DE.A_SOURCE, IServiceConstants.SUCCESS);
				}
				else
				{
					status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
				}
				
				// update filename?				
				filename = new File(filename.getAbsolutePath());
				subject.setAttribute(DE.A_SOURCE, setProperties(filename));
				_dataStore.refresh(subject);
				
			} catch (Exception e) {
				UniversalServerUtilities.logError(CLASSNAME,
						"handleSetreadOnly", e); //$NON-NLS-1$
			}
		}
		_dataStore.refresh(subject);
		return statusDone(status);
	}

	/**
	 * Method to set LastModified to a file or folder.
	 */
	public DataElement handleSetLastModified(DataElement subject,
			DataElement status) 
	{	
		File filename = new File(subject.getAttribute(DE.A_VALUE), subject.getAttribute(DE.A_NAME));
		
		if (!filename.exists())
		{
			filename = new File(subject.getAttribute(DE.A_VALUE));
		}
		if (!filename.exists())
			status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED_WITH_DOES_NOT_EXIST);
		else {
			try {
				String str = subject.getAttribute(DE.A_SOURCE);
			
				long date = Long.parseLong(str);
				boolean done = filename.setLastModified(date);

				if (done) {
					status.setAttribute(DE.A_SOURCE, IServiceConstants.SUCCESS);
				} 
				else
				{
					status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
				}
				
				filename = new File(filename.getAbsolutePath());
				subject.setAttribute(DE.A_SOURCE, setProperties(filename));
				_dataStore.refresh(subject);
				
			} catch (Exception e) {
				UniversalServerUtilities.logError(CLASSNAME,
						"handleSetLastModified", e); //$NON-NLS-1$
			}
		}
		_dataStore.refresh(subject);
		return statusDone(status);
	}

	/**
	 * Method to Retrieve properties of the file or folder.
	 */
	protected DataElement handleQueryBasicProperty(DataElement subject,
			DataElement status) {
		File fileobj = new File(subject.getAttribute(DE.A_VALUE)
				+ File.separatorChar + subject.getName());
		subject.setAttribute(DE.A_SOURCE, setProperties(fileobj));
		_dataStore.refresh(subject);
		return statusDone(status);
	}

	/**
	 * Method to Retrieve canWrite property of the file or folder.
	 */
	protected DataElement handleQuerycanWriteProperty(DataElement subject,
			DataElement status) {
		File fileObj = new File(subject.getAttribute(DE.A_VALUE)
				+ File.separatorChar + subject.getName());

		String version = IServiceConstants.VERSION_1;
		StringBuffer buffer = new StringBuffer(50);
		boolean canWrite = fileObj.canWrite();

		buffer.append(version).append(IServiceConstants.TOKEN_SEPARATOR).append(canWrite);
		subject.setAttribute(DE.A_SOURCE, buffer.toString());
		_dataStore.refresh(subject);
		return statusDone(status);
	}

	/**
	 * Method to query advance properties.
	 */

	protected DataElement handleQueryAdvanceProperty(DataElement subject,
			DataElement status) {
		// log error currently there are no advance properties for Universal
		// Files
		return statusDone(status);
	}

	protected DataElement handleQueryFileClassification(DataElement subject, DataElement status) {

		FileClassifier classifier = getFileClassifier(subject);
		classifier.start();
		statusDone(status);

		return status;
	}

	/**
	 * Method to query existence of the file or folder.
	 */
	protected DataElement handleQueryExists(DataElement subject,
			DataElement status, String queryType) {

		File fileobj = null;
		if (queryType.equals(IUniversalDataStoreConstants.UNIVERSAL_FILTER_DESCRIPTOR)) {
			if (subject.getName().indexOf(
					ArchiveHandlerManager.VIRTUAL_SEPARATOR) > 0) {
				VirtualChild child = _archiveHandlerManager
						.getVirtualObject(subject.getName());
				if (child.exists()) {
					status.setAttribute(DE.A_SOURCE, "true"); //$NON-NLS-1$
					return statusDone(status);
				} else {
					status.setAttribute(DE.A_SOURCE, "false"); //$NON-NLS-1$
					return statusDone(status);
				}
			} else {
				fileobj = new File(subject.getName());
			}
		} else if (queryType.equals(IUniversalDataStoreConstants.UNIVERSAL_FILE_DESCRIPTOR)
				|| queryType.equals(IUniversalDataStoreConstants.UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR))
			fileobj = new File(subject.getAttribute(DE.A_VALUE)
					+ File.separatorChar + subject.getName());
		else if (queryType.equals(IUniversalDataStoreConstants.UNIVERSAL_FOLDER_DESCRIPTOR))
			fileobj = new File(subject.getAttribute(DE.A_VALUE));
		else if (queryType.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR)
				|| queryType.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR)) {
			AbsoluteVirtualPath vpath = getAbsoluteVirtualPath(subject);
			ISystemArchiveHandler handler = _archiveHandlerManager
					.getRegisteredHandler(new File(vpath
							.getContainingArchiveString()));
			if (handler == null) {
				status.setAttribute(DE.A_SOURCE, "false"); //$NON-NLS-1$
				return statusDone(status);
			}
			VirtualChild child = handler.getVirtualFile(vpath.getVirtualPart());
			if (child.exists()) {
				status.setAttribute(DE.A_SOURCE, "true"); //$NON-NLS-1$
				return statusDone(status);
			}

		}

		if (fileobj != null && fileobj.exists())
			status.setAttribute(DE.A_SOURCE, "true"); //$NON-NLS-1$
		else
			status.setAttribute(DE.A_SOURCE, "false"); //$NON-NLS-1$
		return statusDone(status);
	}

	/**
	 * Method to get remote object
	 */
	public DataElement handleQueryGetRemoteObject(DataElement subject,
			DataElement status, String queryType) {
		File fileobj = null;
		boolean isVirtual = false;
		String fullName = subject.getValue();
		if (queryType.equals(IUniversalDataStoreConstants.UNIVERSAL_FILTER_DESCRIPTOR)) 
		{
			isVirtual = ArchiveHandlerManager.isVirtual(fullName);
			String filterValue = subject.getValue();
			// . translates to home dir
			if (filterValue.equals("."))  //$NON-NLS-1$
			{
				filterValue = System.getProperty("user.home"); //$NON-NLS-1$
				subject.setAttribute(DE.A_VALUE, filterValue);
			}
			if (!isVirtual)
				fileobj = new File(filterValue);
		} 
		else if (queryType.equals(IUniversalDataStoreConstants.UNIVERSAL_FILE_DESCRIPTOR))
		{
			String name = subject.getName();
			String path = subject.getValue();
			fileobj = new File(path, name);	
		}
		else if (queryType.equals(IUniversalDataStoreConstants.UNIVERSAL_FOLDER_DESCRIPTOR))
		{
			String name = subject.getName();			
			String path = subject.getValue();
			if (name.length() == 0)
			{
				fileobj = new File(path);
			}
			else
			{
				fileobj = new File(path, name);		
			}
		}
		else {
			UniversalServerUtilities.logError(CLASSNAME,
					"Invalid query type to handleQueryGetRemoteObject", null); //$NON-NLS-1$
			return statusDone(status);
		}

		if (!isVirtual && fileobj != null && fileobj.exists()) {

			// Get the canonical path name so that we preserve case for Windows
			// systems.
			// Even though Windows is case insensitive, we still want to
			// preserve case
			// when we show the path as a property to the user
			try {
				fullName = fileobj.getCanonicalPath();

			} catch (IOException e) {
				return statusDone(status);
			}

			if (fileobj.isFile()) 
			{
				if (_archiveHandlerManager.isArchive(fileobj)) {
					subject.setAttribute(DE.A_TYPE,IUniversalDataStoreConstants.UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR);
				} else {
					subject.setAttribute(DE.A_TYPE, IUniversalDataStoreConstants.UNIVERSAL_FILE_DESCRIPTOR);
				}
			} 
			else { // directory
				subject.setAttribute(DE.A_TYPE, IUniversalDataStoreConstants.UNIVERSAL_FOLDER_DESCRIPTOR);
			}
			
			String name = fullName
			.substring(
					fullName.lastIndexOf(File.separatorChar) + 1,
					fullName.length());
			int lastFileSeparatorIndex = fullName.lastIndexOf(File.separatorChar);
			String path = "";  //$NON-NLS-1$
			if (-1 != lastFileSeparatorIndex)
			{
				if (0 == lastFileSeparatorIndex)
				{
					//Need to handle the case like "/backup".  Its parent is "/", not ""
					path = Character.toString(File.separatorChar);
				}
				else
				{
					path = fullName.substring(0, fullName.lastIndexOf(File.separatorChar));
				}
			}
			subject.setAttribute(DE.A_NAME, name);
			subject.setAttribute(DE.A_VALUE, path);
			
				

			// DKM - do basic property stuff here
			subject.setAttribute(DE.A_SOURCE, setProperties(fileobj));


			/*
			// classify the file too 
			if (fileobj.isFile()) {
				subject.setAttribute(DE.A_SOURCE, subject
						.getAttribute(DE.A_SOURCE)
						+ "|" + FileClassifier.classifyFile(fileobj));
			}
			*/

			status.setAttribute(DE.A_SOURCE, IServiceConstants.SUCCESS);
		} else if (isVirtual) {
			try {
				String goodFullName = ArchiveHandlerManager
						.cleanUpVirtualPath(fullName);
				AbsoluteVirtualPath avp = new AbsoluteVirtualPath(goodFullName);
				VirtualChild child = _archiveHandlerManager
						.getVirtualObject(goodFullName);
				if (child.exists()) {

					if (child.isDirectory) {
						subject.setAttribute(DE.A_TYPE,
								IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR);
						subject.setAttribute(DE.A_NAME, child.name);
						if (child.path.equals("")) { //$NON-NLS-1$
							subject.setAttribute(DE.A_VALUE, avp
									.getContainingArchiveString());
						} else {
							subject.setAttribute(DE.A_VALUE, avp
									.getContainingArchiveString()
									+ ArchiveHandlerManager.VIRTUAL_SEPARATOR
									+ child.path);
						}

					} else {
						subject.setAttribute(DE.A_TYPE,
								IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR);
						String name = child.name;
						String path = avp.getContainingArchiveString();
						if (!child.path.equals("")) { //$NON-NLS-1$
							path = path
									+ ArchiveHandlerManager.VIRTUAL_SEPARATOR
									+ child.path;
						}

						subject.setAttribute(DE.A_NAME, name);
						subject.setAttribute(DE.A_VALUE, path);
					}

					subject.setAttribute(DE.A_SOURCE, setProperties(child));
					status.setAttribute(DE.A_SOURCE, IServiceConstants.SUCCESS);
				} else {
					UniversalServerUtilities.logWarning(CLASSNAME,
							"object does not exist"); //$NON-NLS-1$
					subject.setAttribute(DE.A_SOURCE, setProperties(child));
					status
							.setAttribute(DE.A_SOURCE,
									IServiceConstants.FAILED_WITH_DOES_NOT_EXIST);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			// change the file type
			subject.setAttribute(DE.A_TYPE, IUniversalDataStoreConstants.UNIVERSAL_FILTER_DESCRIPTOR);
			subject.setAttribute(DE.A_SOURCE, setProperties(fileobj));
			status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED_WITH_DOES_NOT_EXIST);
		}

		_dataStore.refresh(subject);
		return statusDone(status);
	}

	protected DataElement getFileElement(DataElement subject, File file) {
		String fileName = file.getName();

		for (int i = 0; i < subject.getNestedSize(); i++) {
			DataElement child = subject.get(i);
			if (child.getName().equals(fileName)) {
				String type = subject.getType();
				boolean isfile = file.isFile();

				if (isfile) {
					return child;
				} else if (type.equals(IUniversalDataStoreConstants.UNIVERSAL_FOLDER_DESCRIPTOR)) {
					return child;
				}
			}
		}

		return null;
	}


	
	/**
	 * Method to obtain the classificatoin string of file or folder.
	 */
	protected String getClassificationString(String s) {

		//StringTokenizer tokenizer = new StringTokenizer(s, IServiceConstants.TOKEN_SEPARATOR);
		String[] str = s.split("\\"+IServiceConstants.TOKEN_SEPARATOR); //$NON-NLS-1$
		int tokens = str.length;
		if (tokens < 10)
		    return null;

		
		return (str[10]);
	}
	/**
	 * Method to obtain the filter string of file or folder.
	 */
	protected String getFilterString(String s) {

		//StringTokenizer tokenizer = new StringTokenizer(s, IServiceConstants.TOKEN_SEPARATOR);
		String[] str = s.split("\\"+IServiceConstants.TOKEN_SEPARATOR); //$NON-NLS-1$
		int tokens = str.length;

		/*
		int tokens = tokenizer.countTokens();
		String[] str = new String[tokens];

		for (int i = 0; i < tokens; ++i) {
			str[i] = tokenizer.nextToken();
		}
		*/
		if (tokens > 1)	
		{
		    return (str[1]);
		}
		else
		{
		    System.out.println("problem with properties:"+s); //$NON-NLS-1$
		    return "*"; //$NON-NLS-1$
		}
	}

	/**
	 * Method to obtain the show Hidden flag for file or folder.
	 */
	protected boolean getShowHiddenFlag(String s) {

		//StringTokenizer tokenizer = new StringTokenizer(s, IServiceConstants.TOKEN_SEPARATOR);
		String[] str = s.split("\\"+IServiceConstants.TOKEN_SEPARATOR); //$NON-NLS-1$
		int tokens = str.length;
		/*
		int tokens = tokenizer.countTokens();
		String[] str = new String[tokens];

		for (int i = 0; i < tokens; ++i) {
			str[i] = tokenizer.nextToken();
		}
		*/
		if (tokens > 2)
		{
		    return ((new Boolean(str[2])).booleanValue());
		}
		else
		{
		    System.out.println("show hidden flag problem:"+s); //$NON-NLS-1$
		    return true;
		}
	}

	/**
	 * Method to obtain the depth for a search
	 */
	protected int getDepth(String s) 
	{
		String[] str = s.split("\\"+IServiceConstants.TOKEN_SEPARATOR); //$NON-NLS-1$
		int tokens = str.length;
	    /*
		StringTokenizer tokenizer = new StringTokenizer(s, IServiceConstants.TOKEN_SEPARATOR);

		int tokens = tokenizer.countTokens();
		*/
		if (tokens < 4) {
			return 1;
		}
/*
		String[] str = new String[tokens];

		for (int i = 0; i < tokens; ++i) {
			str[i] = tokenizer.nextToken();
		}
*/
		return ((new Integer(str[3])).intValue()); 
	}

	/**
	 * Method to download a file.
	 */
	protected DataElement handleDownload(DataElement theElement,  DataElement status) 
	{

		UniversalDownloadHandler downloadThread = new UniversalDownloadHandler(
				_dataStore, this, theElement, status);
		downloadThread.start();

		updateCancellableThreads(status.getParent(), downloadThread);
		return status;
	}

	/**
	 * Get the system encoding
	 */
	protected DataElement handleQueryEncoding(DataElement subject, DataElement status) {

		String encoding = System.getProperty("file.encoding"); //$NON-NLS-1$

		subject.setAttribute(DE.A_VALUE, encoding);
		_dataStore.refresh(subject);

		return statusDone(status);
	}
	
	/**
	 * Get an unused port number.
	 */
	protected DataElement handleQueryUnusedPort(DataElement subject, DataElement status) {
		
		int port = -1;

		// create a server socket with port 0 (i.e. use any free port)
		try {
			ServerSocket socket = new ServerSocket(0);
			port = socket.getLocalPort();
			socket.close();
		}
		catch (IOException e) {
			UniversalServerUtilities.logError(CLASSNAME, "Can not get unused port", e); //$NON-NLS-1$
			port = -1;
		}
		
		String portNum = String.valueOf(port);
		subject.setAttribute(DE.A_VALUE, portNum);
		_dataStore.refresh(subject);

		return statusDone(status);
	}

	/**
	 * Complete status.
	 */
	public DataElement statusDone(DataElement status) {
		status.setAttribute(DE.A_NAME, DataStoreResources.model_done);
		_dataStore.refresh(status);
		return status;
	}

	/**
	 * Cancel status.
	 */
	public DataElement statusCancelled(DataElement status) {
		status.setAttribute(DE.A_NAME, "cancelled"); //$NON-NLS-1$
		_dataStore.refresh(status);
		return status;
	}

	/**
	 * @see Miner#load()
	 */
	public void load() {
		// Create datastore tree structure for UniversalFileSystemMiner
		deUFSnode = _dataStore.createObject(_minerData, IUniversalDataStoreConstants.UNIVERSAL_NODE_DESCRIPTOR, "universal.node"); //$NON-NLS-1$
//		deUFStemp = _dataStore.createObject(deUFSnode, IUniversalDataStoreConstants.UNIVERSAL_NODE_DESCRIPTOR, "universal.temp");
		_dataStore.createObject(deUFSnode, IUniversalDataStoreConstants.UNIVERSAL_NODE_DESCRIPTOR, "universal.temp"); //$NON-NLS-1$
//		deUFSfilters = _dataStore.createObject(deUFSnode, IUniversalDataStoreConstants.UNIVERSAL_NODE_DESCRIPTOR, "universal.filters");
		_dataStore.createObject(deUFSnode, IUniversalDataStoreConstants.UNIVERSAL_NODE_DESCRIPTOR, "universal.filters"); //$NON-NLS-1$
		deUFSuploadlog = _dataStore.createObject(deUFSnode, IUniversalDataStoreConstants.UNIVERSAL_NODE_DESCRIPTOR, "universal.uploadlog"); //$NON-NLS-1$

		UniversalByteStreamHandler universalHandler = new UniversalByteStreamHandler(_dataStore, deUFSuploadlog);		
		
		//_dataStore.setByteStreamHandler(new UniversalByteStreamHandler(_dataStore, deUFSuploadlog));
		_dataStore.registerByteStreamHandler(universalHandler);
				
		_dataStore.refresh(_minerData);
		_dataStore.refresh(deUFSuploadlog);
	}

	public void finish() {
		//_archiveHandlerManager.dispose();
		super.finish();
	}

	/**
	 * Retrieve the fully qualified class name (including package) for the
	 * specified .class file. This information is required by the distributed
	 * debugger.
	 */
	protected DataElement handleQueryClassName(DataElement subject,
			DataElement status) {
		String filename = subject.getAttribute(DE.A_VALUE) + File.separatorChar
				+ subject.getName();

		try {
			ClassFileParser parser = new ClassFileParser(
					getInputStreamForFile(filename));
			String name = parser.getPackageName();
			if (name != null) {
				_dataStore.createObject(status, "qualifiedClassName", name); //$NON-NLS-1$
			} else {
				_dataStore.createObject(status, "qualifiedClassName", "null"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		} catch (java.io.IOException e) {
			_dataStore.createObject(status, "qualifiedClassName", "null"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		return statusDone(status);
	}

	/**
	 * Retrieve the fully qualified class name (including package) for a class
	 * file.
	 */
	protected DataElement handleQueryQualifiedClassName(DataElement subject, DataElement status) {

		// first get parent path
		String parentPath = subject.getAttribute(DE.A_VALUE);
		
		// get system separator
		String sep = File.separator;
		
		boolean isParentArchive = ArchiveHandlerManager.getInstance().isRegisteredArchive(parentPath);
		
		boolean isParentVirtual = ArchiveHandlerManager.isVirtual(parentPath);
		
		// parent is virtual folder, so make separator "/"
		if (isParentVirtual) {
			sep = "/"; //$NON-NLS-1$
		}
		
		// file path
		String filePath = null;
		
		// parent is not a virtual archive
		if (!isParentArchive) {
		
			// if parent path does not end with separator, then add it
			if (!parentPath.endsWith(sep)) {
				parentPath = parentPath + sep;
			}

			// add file name to get the file path
			filePath = parentPath + subject.getName();
		}
		// parent is an archive, so add virtual file separator, then the file name
		else {
			filePath = parentPath + ArchiveHandlerManager.VIRTUAL_SEPARATOR + subject.getName();
		}

		try {
			
			String className = null;
			
			// if parent is not an archive or a virtual folder, then file must be
			// a file
			if (!(isParentArchive || isParentVirtual)) {
				className = ClassFileUtil.getInstance().getQualifiedClassName(filePath);
			}
			// otherwise, file is a virtual file
			else {
				String classification = SystemFileClassifier.getInstance().classifyFile(filePath);
				String execJava = "executable(java:"; //$NON-NLS-1$
				
				int idx = classification.indexOf(execJava);
				
				if (idx != -1) {
					idx = idx + execJava.length(); 
					int jdx = classification.indexOf(")", idx); //$NON-NLS-1$
					
					if (jdx != -1) {
						
						if (jdx > idx) {
							className = classification.substring(idx, jdx);
						}
						else if (jdx == idx) {
							className = ""; //$NON-NLS-1$
						}
					}
				}
			}

			if (className != null) {
				_dataStore.createObject(status, IUniversalDataStoreConstants.TYPE_QUALIFIED_CLASSNAME, className);
			} else {
				_dataStore.createObject(status, IUniversalDataStoreConstants.TYPE_QUALIFIED_CLASSNAME, "null"); //$NON-NLS-1$
			}
		} catch (IOException e) {
			UniversalServerUtilities.logError(CLASSNAME,
					"I/O error occured trying to read class file " + filePath, //$NON-NLS-1$
					null);
			
			_dataStore.createObject(status, IUniversalDataStoreConstants.TYPE_QUALIFIED_CLASSNAME, "null"); //$NON-NLS-1$
		}

		return statusDone(status);
	}

	/**
	 * Method to retrieve the OS that the miner is running.
	 */
	public DataElement handleGetOSType(DataElement subject, DataElement status) {
		String osType = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$
		status.setAttribute(DE.A_SOURCE, osType);
		_dataStore.refresh(subject);
		return statusDone(status);
	}

	protected java.io.InputStream getInputStreamForFile(String filename)
			throws java.io.IOException {
		File file = new File(filename);
		return file.toURL().openStream();
	}

	/**
	 * @see Miner#extendSchema(DataElement)
	 */
	public void extendSchema(DataElement schemaRoot) {
//		DataElement root = _dataStore.find(schemaRoot, DE.A_NAME, DataStoreResources.model_root, 1);
		_dataStore.find(schemaRoot, DE.A_NAME, DataStoreResources.model_root, 1);
//		DataElement snode = createObjectDescriptor(schemaRoot, IUniversalDataStoreConstants.UNIVERSAL_NODE_DESCRIPTOR);
		createObjectDescriptor(schemaRoot, IUniversalDataStoreConstants.UNIVERSAL_NODE_DESCRIPTOR);

		DataElement tempnode = createObjectDescriptor(schemaRoot,
				IUniversalDataStoreConstants.UNIVERSAL_TEMP_DESCRIPTOR);

		// Define filesystem descriptors
		DataElement UniversalFilter = createObjectDescriptor(schemaRoot,
				IUniversalDataStoreConstants.UNIVERSAL_FILTER_DESCRIPTOR);
		FileDescriptors._deUniversalFileObject = createObjectDescriptor(schemaRoot,
				IUniversalDataStoreConstants.UNIVERSAL_FILE_DESCRIPTOR);
		FileDescriptors._deUniversalFolderObject = createObjectDescriptor(schemaRoot,
				IUniversalDataStoreConstants.UNIVERSAL_FOLDER_DESCRIPTOR);
		FileDescriptors._deUniversalArchiveFileObject = createObjectDescriptor(
				schemaRoot, IUniversalDataStoreConstants.UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR);
		FileDescriptors._deUniversalVirtualFileObject = createObjectDescriptor(
				schemaRoot, IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR);
		FileDescriptors._deUniversalVirtualFolderObject = createObjectDescriptor(
				schemaRoot, IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR);

		_dataStore.refresh(schemaRoot);

		// the cancellable object descriptor
		DataElement cancellable = _dataStore.find(schemaRoot, DE.A_NAME, DataStoreResources.model_Cancellable, 1);
		
		// Define command descriptors
		DataElement queryAllFilterDescriptor = createCommandDescriptor(UniversalFilter, "Filter", IUniversalDataStoreConstants.C_QUERY_VIEW_ALL); //$NON-NLS-1$
		_dataStore.createReference(cancellable, queryAllFilterDescriptor, DataStoreResources.model_abstracts, DataStoreResources.model_abstracted_by); 		
		
		DataElement queryFilesFilterDescriptor = createCommandDescriptor(UniversalFilter, "Filter", IUniversalDataStoreConstants.C_QUERY_VIEW_FILES); //$NON-NLS-1$
		_dataStore.createReference(cancellable, queryFilesFilterDescriptor, DataStoreResources.model_abstracts, DataStoreResources.model_abstracted_by);
		
		DataElement queryFolderFilterDescriptor = createCommandDescriptor(UniversalFilter, "Filter", IUniversalDataStoreConstants.C_QUERY_VIEW_FOLDERS); //$NON-NLS-1$
		_dataStore.createReference(cancellable, queryFolderFilterDescriptor, DataStoreResources.model_abstracts, DataStoreResources.model_abstracted_by);		
		
		createCommandDescriptor(UniversalFilter, "Filter", IUniversalDataStoreConstants.C_QUERY_ROOTS); //$NON-NLS-1$ 

		
		DataElement queryAllDescriptor = createCommandDescriptor(FileDescriptors._deUniversalFolderObject, "Filter", IUniversalDataStoreConstants.C_QUERY_VIEW_ALL); //$NON-NLS-1$
		_dataStore.createReference(cancellable, queryAllDescriptor, DataStoreResources.model_abstracts, DataStoreResources.model_abstracted_by); 		
		
		DataElement queryFilesDescriptor = createCommandDescriptor(FileDescriptors._deUniversalFolderObject, "Filter", IUniversalDataStoreConstants.C_QUERY_VIEW_FILES); //$NON-NLS-1$
		_dataStore.createReference(cancellable, queryFilesDescriptor, DataStoreResources.model_abstracts, DataStoreResources.model_abstracted_by); 		

		DataElement queryFolderDescriptor = createCommandDescriptor(FileDescriptors._deUniversalFolderObject, "Filter", IUniversalDataStoreConstants.C_QUERY_VIEW_FOLDERS); //$NON-NLS-1$
		_dataStore.createReference(cancellable, queryFolderDescriptor, DataStoreResources.model_abstracts, DataStoreResources.model_abstracted_by); 		

		
		DataElement queryAllArchiveDescriptor = createCommandDescriptor(FileDescriptors._deUniversalArchiveFileObject, "Filter", IUniversalDataStoreConstants.C_QUERY_VIEW_ALL); //$NON-NLS-1$
		_dataStore.createReference(cancellable, queryAllArchiveDescriptor, DataStoreResources.model_abstracts, DataStoreResources.model_abstracted_by); 		

		DataElement queryFilesArchiveDescriptor = createCommandDescriptor(FileDescriptors._deUniversalArchiveFileObject, "Filter", IUniversalDataStoreConstants.C_QUERY_VIEW_FILES); //$NON-NLS-1$
		_dataStore.createReference(cancellable, queryFilesArchiveDescriptor, DataStoreResources.model_abstracts, DataStoreResources.model_abstracted_by); 		

		DataElement queryFolderArchiveDescriptor = createCommandDescriptor(FileDescriptors._deUniversalArchiveFileObject, "Filter", IUniversalDataStoreConstants.C_QUERY_VIEW_FOLDERS); //$NON-NLS-1$
		_dataStore.createReference(cancellable, queryFolderArchiveDescriptor, DataStoreResources.model_abstracts, DataStoreResources.model_abstracted_by); 		
		
		createCommandDescriptor(UniversalFilter, "GetOSType", IUniversalDataStoreConstants.C_GET_OSTYPE); //$NON-NLS-1$ 
		createCommandDescriptor(UniversalFilter, "Exists", IUniversalDataStoreConstants.C_QUERY_EXISTS); //$NON-NLS-1$ 
		createCommandDescriptor(UniversalFilter, "GetRemoteObject", IUniversalDataStoreConstants.C_QUERY_GET_REMOTE_OBJECT); //$NON-NLS-1$
		createCommandDescriptor(UniversalFilter, "CreateNewFile", IUniversalDataStoreConstants.C_CREATE_FILE); //$NON-NLS-1$
		createCommandDescriptor(UniversalFilter, "CreateNewFolder", IUniversalDataStoreConstants.C_CREATE_FOLDER); //$NON-NLS-1$
		createCommandDescriptor(UniversalFilter, "SetLastModified", IUniversalDataStoreConstants.C_SET_LASTMODIFIED); //$NON-NLS-1$


		_dataStore.createReference(FileDescriptors._deUniversalFileObject,
				FileDescriptors._deUniversalArchiveFileObject, DataStoreResources.model_abstracts, DataStoreResources.model_abstracted_by); 
		_dataStore.createReference(FileDescriptors._deUniversalFolderObject,
				FileDescriptors._deUniversalArchiveFileObject, DataStoreResources.model_abstracts, DataStoreResources.model_abstracted_by); 
		_dataStore.createReference(FileDescriptors._deUniversalFileObject,
				FileDescriptors._deUniversalVirtualFileObject, DataStoreResources.model_abstracts, DataStoreResources.model_abstracted_by); 
		_dataStore.createReference(FileDescriptors._deUniversalFolderObject,
				FileDescriptors._deUniversalVirtualFolderObject, DataStoreResources.model_abstracts, DataStoreResources.model_abstracted_by); 

		// create the search descriptor and make it cancelable
		DataElement searchDescriptor = createCommandDescriptor(FileDescriptors._deUniversalFolderObject, "Search", IUniversalDataStoreConstants.C_SEARCH); //$NON-NLS-1$ 
		_dataStore.createReference(cancellable, searchDescriptor, DataStoreResources.model_abstracts, DataStoreResources.model_abstracted_by); 
		

		createCommandDescriptor(FileDescriptors._deUniversalFolderObject, "GetAdvanceProperty", IUniversalDataStoreConstants.C_QUERY_ADVANCE_PROPERTY); //$NON-NLS-1$
		createCommandDescriptor(tempnode, "Filter", IUniversalDataStoreConstants.C_CREATE_TEMP); //$NON-NLS-1$ 
		createCommandDescriptor(FileDescriptors._deUniversalFileObject, "Delete", IUniversalDataStoreConstants.C_DELETE); //$NON-NLS-1$
		createCommandDescriptor(FileDescriptors._deUniversalFileObject, "DeleteBatch", IUniversalDataStoreConstants.C_DELETE_BATCH); //$NON-NLS-1$
		createCommandDescriptor(FileDescriptors._deUniversalFileObject, "CreateNewFile", IUniversalDataStoreConstants.C_CREATE_FILE); //$NON-NLS-1$
		createCommandDescriptor(FileDescriptors._deUniversalFileObject, "CreateNewFolder", IUniversalDataStoreConstants.C_CREATE_FOLDER); //$NON-NLS-1$
		createCommandDescriptor(FileDescriptors._deUniversalFileObject, "Rename", IUniversalDataStoreConstants.C_RENAME); //$NON-NLS-1$ 
		createCommandDescriptor(FileDescriptors._deUniversalFileObject, "SetReadOnly", IUniversalDataStoreConstants.C_SET_READONLY); //$NON-NLS-1$
		createCommandDescriptor(FileDescriptors._deUniversalFileObject, "SetLastModified", IUniversalDataStoreConstants.C_SET_LASTMODIFIED); //$NON-NLS-1$
		createCommandDescriptor(FileDescriptors._deUniversalFileObject, "GetAdvanceProperty", IUniversalDataStoreConstants.C_QUERY_ADVANCE_PROPERTY); //$NON-NLS-1$
		createCommandDescriptor(FileDescriptors._deUniversalFileObject, "GetBasicProperty", IUniversalDataStoreConstants.C_QUERY_BASIC_PROPERTY); //$NON-NLS-1$ 

		createCommandDescriptor(FileDescriptors._deUniversalFileObject, "GetcanWriteProperty", IUniversalDataStoreConstants.C_QUERY_CAN_WRITE_PROPERTY); //$NON-NLS-1$
		createCommandDescriptor(FileDescriptors._deUniversalFileObject, "Exists", IUniversalDataStoreConstants.C_QUERY_EXISTS); //$NON-NLS-1$

		createCommandDescriptor(FileDescriptors._deUniversalFolderObject, "Delete", IUniversalDataStoreConstants.C_DELETE); //$NON-NLS-1$
		createCommandDescriptor(FileDescriptors._deUniversalFolderObject, "DeleteBatch", IUniversalDataStoreConstants.C_DELETE_BATCH); //$NON-NLS-1$ 
		createCommandDescriptor(FileDescriptors._deUniversalFolderObject, "Rename", IUniversalDataStoreConstants.C_RENAME); //$NON-NLS-1$
		createCommandDescriptor(FileDescriptors._deUniversalFolderObject, "Copy", IUniversalDataStoreConstants.C_COPY); //$NON-NLS-1$ 
		createCommandDescriptor(FileDescriptors._deUniversalFolderObject, "CopyBatch", IUniversalDataStoreConstants.C_COPY_BATCH); //$NON-NLS-1$ 
		createCommandDescriptor(FileDescriptors._deUniversalFolderObject, "CreateNewFolder", IUniversalDataStoreConstants.C_CREATE_FOLDER); //$NON-NLS-1$
		createCommandDescriptor(FileDescriptors._deUniversalFolderObject, "SetReadOnly", IUniversalDataStoreConstants.C_SET_READONLY); //$NON-NLS-1$
		createCommandDescriptor(FileDescriptors._deUniversalFolderObject, "SetLastModified", IUniversalDataStoreConstants.C_SET_LASTMODIFIED); //$NON-NLS-1$
		createCommandDescriptor(FileDescriptors._deUniversalFolderObject, "GetBasicProperty", IUniversalDataStoreConstants.C_QUERY_BASIC_PROPERTY); //$NON-NLS-1$
		createCommandDescriptor(FileDescriptors._deUniversalFolderObject, "GetcanWriteProperty", IUniversalDataStoreConstants.C_QUERY_CAN_WRITE_PROPERTY); //$NON-NLS-1$

		createCommandDescriptor(FileDescriptors._deUniversalFileObject, "GetFileClassifications", IUniversalDataStoreConstants.C_QUERY_FILE_CLASSIFICATIONS); //$NON-NLS-1$ 
		createCommandDescriptor(FileDescriptors._deUniversalFolderObject, "GetFolderClassifications", IUniversalDataStoreConstants.C_QUERY_FILE_CLASSIFICATION); //$NON-NLS-1$ 
		createCommandDescriptor(FileDescriptors._deUniversalFolderObject, "Exists", IUniversalDataStoreConstants.C_QUERY_EXISTS); //$NON-NLS-1$
		createCommandDescriptor(FileDescriptors._deUniversalFolderObject, "CreateNewFile", IUniversalDataStoreConstants.C_CREATE_FILE); //$NON-NLS-1$
		createCommandDescriptor(FileDescriptors._deUniversalFolderObject, "CreateNewFolder", IUniversalDataStoreConstants.C_CREATE_FOLDER); //$NON-NLS-1$
		createCommandDescriptor(FileDescriptors._deUniversalFolderObject, "GetOSType", IUniversalDataStoreConstants.C_GET_OSTYPE); //$NON-NLS-1$
		createCommandDescriptor(FileDescriptors._deUniversalFileObject, "GetOSType", IUniversalDataStoreConstants.C_GET_OSTYPE); //$NON-NLS-1$

		// create a download command descriptor and make it cancelable
		DataElement downloadDescriptor = createCommandDescriptor(
				FileDescriptors._deUniversalFileObject, "DownloadFile", IUniversalDataStoreConstants.C_DOWNLOAD_FILE); //$NON-NLS-1$
		_dataStore.createReference(cancellable, downloadDescriptor,
				DataStoreResources.model_abstracts, DataStoreResources.model_abstracted_by); 


		DataElement adownloadDescriptor = createCommandDescriptor(
				FileDescriptors._deUniversalArchiveFileObject, "DownloadFile", IUniversalDataStoreConstants.C_DOWNLOAD_FILE); //$NON-NLS-1$
		_dataStore.createReference(cancellable, adownloadDescriptor,
				DataStoreResources.model_abstracts, DataStoreResources.model_abstracted_by); 

		
		createCommandDescriptor(tempnode, "SystemEncoding", IUniversalDataStoreConstants.C_SYSTEM_ENCODING); //$NON-NLS-1$
		
		createCommandDescriptor(tempnode, "UnusedPort", IUniversalDataStoreConstants.C_QUERY_UNUSED_PORT); //$NON-NLS-1$

		// command descriptor to retrieve package name for a class file
		createCommandDescriptor(FileDescriptors._deUniversalFileObject, "GetQualifiedClassName", IUniversalDataStoreConstants.C_QUERY_CLASSNAME); //$NON-NLS-1$

		// command descriptor to retrieve qualified class name for class file
		createCommandDescriptor(FileDescriptors._deUniversalFileObject, "GetFullClassName", //$NON-NLS-1$
				IUniversalDataStoreConstants.C_QUERY_QUALIFIED_CLASSNAME);
	}

	private AbsoluteVirtualPath getAbsoluteVirtualPath(DataElement subject) {
		StringBuffer path = new StringBuffer(subject.getAttribute(DE.A_VALUE));
		if (ArchiveHandlerManager.getInstance().isArchive(
				new File(path.toString()))) {
			path.append(ArchiveHandlerManager.VIRTUAL_SEPARATOR);
		} else {
			path.append('/');
		}
		path.append(subject.getName());
		return getAbsoluteVirtualPath(path.toString());
	}

	public AbsoluteVirtualPath getAbsoluteVirtualPath(String path) {
		AbsoluteVirtualPath vp = new AbsoluteVirtualPath(path);
		return vp;
	}

	public DataElement handleDeleteFromArchive(DataElement subject,
			DataElement status) {
		String type = subject.getType();
		DataElement deObj = null;

		AbsoluteVirtualPath vpath = getAbsoluteVirtualPath(subject);
		if (vpath != null) {
			ISystemArchiveHandler handler = _archiveHandlerManager
					.getRegisteredHandler(new File(vpath
							.getContainingArchiveString()));
			if (handler == null || !handler.delete(vpath.getVirtualPart())) {
				status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED + "|" + vpath.toString()); //$NON-NLS-1$
				_dataStore.refresh(subject);
				return statusDone(status);
			}

			if (type.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR)) {
				deObj = _dataStore.find(subject, DE.A_NAME, subject.getName(),
						1);
				_dataStore.deleteObject(subject, deObj);
				status.setAttribute(DE.A_SOURCE, IServiceConstants.SUCCESS);
			} else if (type.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR)) {
				_dataStore.deleteObjects(subject);
				DataElement parent = subject.getParent();
				_dataStore.deleteObject(parent, subject);
				_dataStore.refresh(parent);
				status.setAttribute(DE.A_SOURCE, IServiceConstants.SUCCESS);
			}
		}

		_dataStore.refresh(subject);
		return statusDone(status);
	}

	public DataElement handleQueryAllArchive(DataElement subject, DataElement attributes, 
			DataElement status, boolean caseSensitive, boolean foldersOnly) 
	{
		// do query on a thread
		ArchiveQueryThread queryThread = new ArchiveQueryThread(subject, attributes, caseSensitive, foldersOnly, showHidden, _isWindows, status);
		queryThread.start();		
		
		updateCancellableThreads(status.getParent(), queryThread);
		return status; // query is in thread so not updating status here
	}



	public ISystemArchiveHandler getArchiveHandlerFor(String archivePath) {
		File file = new File(archivePath);
		return _archiveHandlerManager.getRegisteredHandler(file);
	}

	public DataElement handleCreateVirtualFile(DataElement subject,
			DataElement status, String type) {

		AbsoluteVirtualPath vpath = null;
		if (type.equals(IUniversalDataStoreConstants.UNIVERSAL_FILTER_DESCRIPTOR)) {
			vpath = getAbsoluteVirtualPath(subject.getValue());
		} else {
			vpath = getAbsoluteVirtualPath(subject);
		}
		ISystemArchiveHandler handler = getArchiveHandlerFor(vpath
				.getContainingArchiveString());
		if (handler == null) {
			status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
			return statusDone(status);
		}
//		VirtualChild child = handler.getVirtualFile(vpath.getVirtualPart());
		handler.getVirtualFile(vpath.getVirtualPart());
		handler.createFile(vpath.getVirtualPart());

		status.setAttribute(DE.A_SOURCE, IServiceConstants.SUCCESS);
		if (type.equals(IUniversalDataStoreConstants.UNIVERSAL_FILTER_DESCRIPTOR)) {
			String fullName = subject.getValue();
			String name = fullName.substring(fullName
					.lastIndexOf(File.separatorChar) + 1, fullName.length());
			String path = fullName.substring(0, fullName
					.lastIndexOf(File.separatorChar));
			subject.setAttribute(DE.A_NAME, name);
			subject.setAttribute(DE.A_VALUE, path);
			subject.setAttribute(DE.A_TYPE, IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR);
		}
		_dataStore.refresh(subject);
		return statusDone(status);
	}

	public DataElement handleCreateVirtualFolder(DataElement subject,
			DataElement status, String type) {

		AbsoluteVirtualPath vpath = null;
		if (type.equals(IUniversalDataStoreConstants.UNIVERSAL_FILTER_DESCRIPTOR)) {
			vpath = getAbsoluteVirtualPath(subject.getValue());
		} else {
			vpath = getAbsoluteVirtualPath(subject);
		}
		ISystemArchiveHandler handler = getArchiveHandlerFor(vpath
				.getContainingArchiveString());
		if (handler == null) {
			status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
			return statusDone(status);
		}
//		VirtualChild child = handler.getVirtualFile(vpath.getVirtualPart());
		handler.getVirtualFile(vpath.getVirtualPart());
		handler.createFolder(vpath.getVirtualPart());

		status.setAttribute(DE.A_SOURCE, IServiceConstants.SUCCESS);
		if (type.equals(IUniversalDataStoreConstants.UNIVERSAL_FILTER_DESCRIPTOR)) {
			String fullName = subject.getValue();
			String name = fullName.substring(fullName
					.lastIndexOf(File.separatorChar) + 1, fullName.length());
			String path = fullName.substring(0, fullName
					.lastIndexOf(File.separatorChar));
			subject.setAttribute(DE.A_NAME, name);
			subject.setAttribute(DE.A_VALUE, path);
			subject
					.setAttribute(DE.A_TYPE,
							IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR);
		}
		_dataStore.refresh(subject);
		return statusDone(status);
	}

	private File getFileFor(DataElement element) {
		File result = null;
		String type = element.getType();
		if (type.equals(IUniversalDataStoreConstants.UNIVERSAL_FILTER_DESCRIPTOR)) {
			result = new File(element.getName());
		} else if (type.equals(IUniversalDataStoreConstants.UNIVERSAL_FILE_DESCRIPTOR)
				|| type.equals(IUniversalDataStoreConstants.UNIVERSAL_FOLDER_DESCRIPTOR)
				|| type.equals(IUniversalDataStoreConstants.UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR)) {
			StringBuffer buf = new StringBuffer(element
					.getAttribute(DE.A_VALUE));
			buf.append(File.separatorChar);
			buf.append(element.getName());
			result = new File(buf.toString());
		}

		return result;
	}

	public DataElement handleCopy(DataElement targetFolder, DataElement sourceFile, DataElement nameObj, DataElement status) {
		
	    String newName = nameObj.getName();
		String targetType = targetFolder.getType();
		String srcType = sourceFile.getType();
		//In the case of super transfer, the source file is a virtual file/folder inside the temporary zip file, and its type information is set to 
		//default UNIVERSAL_FILTER_DESCRIPTOR since its information never been cached before.
		//We need to find out its real type first before going to different if statement.
		File srcFile = null;
		VirtualChild child = null;
		if (IUniversalDataStoreConstants.UNIVERSAL_FILTER_DESCRIPTOR == srcType)
		{
			if (ArchiveHandlerManager.isVirtual(sourceFile.getValue()))
			{
				String goodFullName = ArchiveHandlerManager.cleanUpVirtualPath(sourceFile.getValue());
				child = _archiveHandlerManager.getVirtualObject(goodFullName);
				if (child.exists()) 
				{
					if (child.isDirectory) 
					{
						srcType = IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR;
					} else 
					{
						srcType = IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR;
					}
				}
			}
		}
		
		if (targetType.equals(IUniversalDataStoreConstants.UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR) || targetType.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR)) {
			
		    // insert into an archive
			AbsoluteVirtualPath vpath = getAbsoluteVirtualPath(targetFolder);
			ISystemArchiveHandler handler = getArchiveHandlerFor(vpath.getContainingArchiveString());
			
			if (handler == null) {
				status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
				return statusDone(status);
			}

			if (srcType.equals(IUniversalDataStoreConstants.UNIVERSAL_FILE_DESCRIPTOR) || srcType.equals(IUniversalDataStoreConstants.UNIVERSAL_FOLDER_DESCRIPTOR)
					|| srcType.equals(IUniversalDataStoreConstants.UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR)) {
				
			    srcFile = getFileFor(sourceFile);
			}
			else if (srcType.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR) || srcType.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR)) {
				ISystemArchiveHandler shandler = null;
				if (null == child)
				{
					AbsoluteVirtualPath svpath = getAbsoluteVirtualPath(sourceFile);
					shandler = getArchiveHandlerFor(svpath.getContainingArchiveString());
				
					if (shandler == null) {
						status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
						return statusDone(status);
					}
					child = shandler.getVirtualFile(svpath.getVirtualPart());
				}
				else
				{
					//If child is not null, it means the sourceFile is a type of UNIVERSAL_FILTER_DESCRIPTOR, and has already been handled
					shandler = child.getHandler();
				}
				srcFile = child.getExtractedFile();
			}

			String virtualContainer = ""; //$NON-NLS-1$
			
			if (targetType.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR)) {
				virtualContainer = vpath.getVirtualPart();
			}

			boolean result = handler.add(srcFile, virtualContainer, newName);
			
			if (result) {
				status.setAttribute(DE.A_SOURCE, IServiceConstants.SUCCESS);
			}
			else {
				status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
			}
		}
		else if (srcType.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR) || srcType.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR)) {
			ISystemArchiveHandler shandler = null;
			AbsoluteVirtualPath svpath = null;
			if (null == child)
			{
				svpath = getAbsoluteVirtualPath(sourceFile);
				shandler = getArchiveHandlerFor(svpath.getContainingArchiveString());
			
				if (shandler == null) {
					status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED);
					return statusDone(status);
				}
				child = shandler.getVirtualFile(svpath.getVirtualPart());
			}
			else
			{
				//If child is not null, it means the sourceFile is a type of UNIVERSAL_FILTER_DESCRIPTOR, and has already been handled
				shandler = child.getHandler();
				svpath = getAbsoluteVirtualPath(sourceFile.getValue());
			}

			File parentDir = getFileFor(targetFolder);
			File destination = new File(parentDir, newName);
			
			if (child.isDirectory) {
				shandler.extractVirtualDirectory(svpath.getVirtualPart(), parentDir, destination);
			}
			else {
				shandler.extractVirtualFile(svpath.getVirtualPart(), destination);
			}
		}
		else {
			File tgtFolder = getFileFor(targetFolder);
			srcFile = getFileFor(sourceFile);

			// regular copy
			boolean folderCopy = srcFile.isDirectory();
			String src = srcFile.getAbsolutePath();
			String tgt = tgtFolder.getAbsolutePath() + File.separatorChar + newName;
			File tgtFile = new File(tgt);
			
			if (tgtFile.exists() && tgtFile.isDirectory())
			{
				//For Windows, we need to use xcopy command, which require the new directory
				//name be part of the target.
				if (newName.equals(srcFile.getName()) && !_isWindows)
				{
					tgt =  tgtFolder.getAbsolutePath();
				}
			}
			
			doCopyCommand(enQuote(src), enQuote(tgt), folderCopy, status);
		}
		
		return statusDone(status);
	}
	
	/**
	 * Method to obtain the properties of file or folder.
	 */
	public String setProperties(File fileObj, boolean doArchiveProperties) {
		String version = IServiceConstants.VERSION_1;
		StringBuffer buffer = new StringBuffer(500);
		long date = fileObj.lastModified();
		long size = fileObj.length();
		boolean hidden = fileObj.isHidden();
		boolean canWrite = fileObj.canWrite() ;
		boolean canRead = fileObj.canRead();

		// These extra properties here might cause problems for older clients,
		// ie: a IndexOutOfBounds in UniversalFileImpl.

		// DKM: defer this until later as it is bad for performacnes..
		// I think we're doing the full query on an archive by instantiating a
		// handler
		boolean isArchive = false;//ArchiveHandlerManager.getInstance().isArchive(fileObj);

		String comment;
		if (isArchive)
			comment = ArchiveHandlerManager.getInstance().getComment(fileObj);
		else
			comment = " "; //$NON-NLS-1$

		long compressedSize = size;
		String compressionMethod = " "; //$NON-NLS-1$
		double compressionRatio = 0;

		long expandedSize;
		if (isArchive)
			expandedSize = ArchiveHandlerManager.getInstance().getExpandedSize(
					fileObj);
		else
			expandedSize = size;

		buffer.append(version).append(IServiceConstants.TOKEN_SEPARATOR).append(date).append(
				IServiceConstants.TOKEN_SEPARATOR).append(size).append(IServiceConstants.TOKEN_SEPARATOR);
		buffer.append(hidden).append(IServiceConstants.TOKEN_SEPARATOR).append(canWrite).append(
				IServiceConstants.TOKEN_SEPARATOR).append(canRead);

		// values might not be used but we set them here just so that there are right number
		// of properties
		buffer.append(IServiceConstants.TOKEN_SEPARATOR);
		buffer.append(comment).append(IServiceConstants.TOKEN_SEPARATOR).append(compressedSize)
				.append(IServiceConstants.TOKEN_SEPARATOR).append(compressionMethod).append(
						IServiceConstants.TOKEN_SEPARATOR);
		buffer.append(compressionRatio).append(IServiceConstants.TOKEN_SEPARATOR).append(
				expandedSize);
		

		String buf = buffer.toString();
		return buf;
	}

	public String setProperties(VirtualChild fileObj) {
		String version = IServiceConstants.VERSION_1;
		StringBuffer buffer = new StringBuffer(500);
		long date = fileObj.getTimeStamp();
		long size = fileObj.getSize();
		boolean hidden = false;
		boolean canWrite = fileObj.getContainingArchive().canWrite();
		boolean canRead = fileObj.getContainingArchive().canRead();

		// These extra properties here might cause problems for older clients,
		// ie: a IndexOutOfBounds in UniversalFileImpl.
		String comment = fileObj.getComment();
		if (comment.equals("")) //$NON-NLS-1$
			comment = " "; // make sure this is still a //$NON-NLS-1$
		// token
		long compressedSize = fileObj.getCompressedSize();
		String compressionMethod = fileObj.getCompressionMethod();
		if (compressionMethod.equals("")) //$NON-NLS-1$
			compressionMethod = " "; //$NON-NLS-1$
		double compressionRatio = fileObj.getCompressionRatio();
		long expandedSize = size;

		buffer.append(version).append(IServiceConstants.TOKEN_SEPARATOR).append(date).append(
				IServiceConstants.TOKEN_SEPARATOR).append(size).append(IServiceConstants.TOKEN_SEPARATOR);
		buffer.append(hidden).append(IServiceConstants.TOKEN_SEPARATOR).append(canWrite).append(
				IServiceConstants.TOKEN_SEPARATOR).append(canRead);

		buffer.append(IServiceConstants.TOKEN_SEPARATOR);
		buffer.append(comment).append(IServiceConstants.TOKEN_SEPARATOR).append(compressedSize)
				.append(IServiceConstants.TOKEN_SEPARATOR).append(compressionMethod).append(
						IServiceConstants.TOKEN_SEPARATOR);
		buffer.append(compressionRatio).append(IServiceConstants.TOKEN_SEPARATOR).append(
				expandedSize);

		return buffer.toString();
	}
	
	public String setProperties(File fileObj) {
		return setProperties(fileObj, false);
	}

	
	
	/**
	 * Quote a file name such that it is valid in a shell
	 * @param s file name to quote
	 * @return quoted file name
	 */
	protected String enQuote(String s)
	{
		if(_isWindows) {
			return '"' + s + '"';
		} else {
			return PathUtility.enQuoteUnix(s);
		}
	}

	public String getVersion()
	{
		return "7.0.0"; //$NON-NLS-1$
	}
}
