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

package org.eclipse.rse.dstore.universal.miners.filesystem;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;

import org.eclipse.dstore.core.miners.miner.Miner;
import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.dstore.core.model.DataStoreResources;
import org.eclipse.dstore.core.util.StringCompare;
import org.eclipse.rse.dstore.universal.miners.ICancellableHandler;
import org.eclipse.rse.dstore.universal.miners.IUniversalDataStoreConstants;
import org.eclipse.rse.dstore.universal.miners.UniversalServerUtilities;
import org.eclipse.rse.services.clientserver.IClientServerConstants;
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

public class UniversalFileSystemMiner extends Miner implements
		IUniversalDataStoreConstants, IClientServerConstants {
    

	public static final String MINER_ID = UniversalFileSystemMiner.class.getName();


//	private DataElement deUFSfilters;

	private DataElement deUFSnode;

//	private DataElement deUFStemp;

	private DataElement deUFSuploadlog;

//	private DataElement dePropertyQuery;

//	private DataElement deFileClassificationQuery;

//	private DataElement deFolderClassificationQuery;
	
	private DataElement deUniversalFileObject;
	private DataElement deUniversalFolderObject;
	private DataElement deUniversalVirtualFileObject;
	private DataElement deUniversalVirtualFolderObject;
	private DataElement deUniversalArchiveFileObject;
	

	protected String filterString = "*";

	protected ArchiveHandlerManager _archiveHandlerManager;

	protected boolean showHidden = false;


	public static final String CLASSNAME = "UniversalFileSystemMiner";

	protected HashMap _cancellableThreads;

	private boolean _isWindows = false;

	public UniversalFileSystemMiner() {
		_cancellableThreads = new HashMap();
		_isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
		_archiveHandlerManager = ArchiveHandlerManager.getInstance();
		_archiveHandlerManager.setRegisteredHandler("zip", SystemZipHandler.class);
		_archiveHandlerManager.setRegisteredHandler("jar", SystemJarHandler.class);
		_archiveHandlerManager.setRegisteredHandler("tar", SystemTarHandler.class);
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
		
		UniversalServerUtilities.logInfo(getName(), name + ":" + subject);
		
		String queryType = (String) subject.getElementProperty(DE.P_TYPE);
		boolean caseSensitive = !_isWindows;
		// TODO: test on WINDOWS!

		if ("C_QUERY_VIEW_ALL".equals(name)) {
			if (subject != null)
			{
			    DataElement attributes = getCommandArgument(theElement, 1);
			    if (attributes != null && attributes.getType().equals("attributes"))
			    {
			        return handleQueryAll(subject, attributes, status, queryType,
							caseSensitive);
			    }
			    else
			    {
			        return handleQueryAll(subject, null, status, queryType,
						caseSensitive);
			    }
			}
			else
				UniversalServerUtilities.logError(CLASSNAME,
						"C_QUERY_VIEW_ALL - subject is null", null);
		} else if ("C_QUERY_VIEW_FILES".equals(name)) {
			if (subject != null)
			{
			    DataElement attributes = getCommandArgument(theElement, 1);
			    if (attributes != null && attributes.getType().equals("attributes"))
			    {
			        return handleQueryFiles(subject, attributes, status, queryType,
							caseSensitive);			        
			    }
			    else
			    {
			        return handleQueryFiles(subject, null, status, queryType,
						caseSensitive);
			    }
			}
			else
				UniversalServerUtilities.logError(CLASSNAME,
						"C_QUERY_VIEW_FILES - subject is null", null);
		} else if ("C_QUERY_VIEW_FOLDERS".equals(name)) {
			if (subject != null)
			{
			    DataElement attributes = getCommandArgument(theElement, 1);
			    if (attributes != null && attributes.getType().equals("attributes"))
			    {
			        return handleQueryFolders(subject, attributes, status, queryType,
							caseSensitive);
			    }
			    else
			    {
			        return handleQueryFolders(subject, null, status, queryType,
						caseSensitive);
			    }
			}
			else
				UniversalServerUtilities.logError(CLASSNAME,
						"C_QUERY_VIEW_FOLDERS - subject is null", null);
		} else if ("C_QUERY_ROOTS".equals(name)) {
			if (subject != null)
				return handleQueryRoots(subject, status);
			else
				UniversalServerUtilities.logError(CLASSNAME,
						"C_QUERY_ROOTS - subject is null", null);
		} else if ("C_SEARCH".equals(name)) {
			if (subject != null)
				return handleSearch(theElement, status, queryType,
						caseSensitive);
			else
				UniversalServerUtilities.logError(CLASSNAME,
						"C_SEARCH - subject is null", null);
		} else if ("C_CANCEL".equals(name)) {
			if (subject != null) {
//				String commandToCancel = subject.getName();
				subject.getName();
				return handleCancel(subject, status);
			} else
				UniversalServerUtilities.logError(CLASSNAME,
						"C_CANCEL - subject is null", null);
		} else if ("C_RENAME".equals(name)) {
			if (subject != null)
				return handleRename(subject, status);
			else
				UniversalServerUtilities.logError(CLASSNAME,
						"C_RENAME - subject is null", null);
		} else if ("C_DELETE".equals(name)) {
			if (subject != null)
				return handleDelete(subject, status, true);
			else
				UniversalServerUtilities.logError(CLASSNAME,
						"C_DELETE - subject is null", null);
		} else if ("C_DELETE_BATCH".equals(name)) {
			if (subject != null)
				return handleDeleteBatch(theElement, status);
			else
				UniversalServerUtilities.logError(CLASSNAME,
						"C_DELETE_BATCH - subject is null", null);
		} else if ("C_COPY".equals(name)) {
			if (subject != null)
				return handleCopy(subject, getCommandArgument(theElement, 1),
						getCommandArgument(theElement, 2), status);
			else
				UniversalServerUtilities.logError(CLASSNAME,
						"C_COPY - subject is null", null);
		} else if ("C_COPY_BATCH".equals(name)) {
			if (subject != null)
				return handleCopyBatch(subject, theElement, status);
			else
				UniversalServerUtilities.logError(CLASSNAME,
						"C_COPY_BATCH - subject is null", null);
		} else if ("C_CREATE_FILE".equals(name)) {
			if (subject != null)
				return handleCreateFile(subject, status, queryType);
			else
				UniversalServerUtilities.logError(CLASSNAME,
						"C_CREATE_FILE - subject is null", null);
		} else if ("C_CREATE_FOLDER".equals(name)) {
			if (subject != null)
				return handleCreateFolder(subject, status, queryType);
			else
				UniversalServerUtilities.logError(CLASSNAME,
						"C_CREATE_FOLDERS - subject is null", null);
		} else if ("C_SET_READONLY".equals(name)) {
			if (subject != null)
				return handleSetReadOnly(subject, status);
			else
				UniversalServerUtilities.logError(CLASSNAME,
						"C_SET_READONLY - subject is null", null);
		} else if ("C_SET_LASTMODIFIED".equals(name)) {
			if (subject != null)
				return handleSetLastModified(subject, status);
			else
				UniversalServerUtilities.logError(CLASSNAME,
						"C_SET_LASTMODIFIED - subject is null", null);
		} else if ("C_QUERY_BASIC_PROPERTY".equals(name)) {
			if (subject != null)
				return handleQueryBasicProperty(subject, status);
			else
				UniversalServerUtilities.logError(CLASSNAME,
						"C_QUERY_BASIC_PROPERTY - subject is null", null);
		} else if ("C_QUERY_CAN_WRITE_PROPERTY".equals(name)) {
			if (subject != null)
				return handleQuerycanWriteProperty(subject, status);
			else
				UniversalServerUtilities.logError(CLASSNAME,
						"C_QUERY_CAN_WRITE_PROPERTY - subject is null", null);
		} else if ("C_QUERY_ADVANCE_PROPERTY".equals(name)) {
			if (subject != null)
				return handleQueryAdvanceProperty(subject, status);
			else
				UniversalServerUtilities.logError(CLASSNAME,
						"C_QUERY_ADVANCE_PROPERTY - subject is null", null);
		} else if ("C_QUERY_FILE_CLASSIFICATIONS".equals(name)) {
			if (subject != null)
				return handleQueryFileClassification(subject, status);
			else
				UniversalServerUtilities.logError(CLASSNAME,
						"C_QUERY_FILE_CLASSIFICATION - subject is null", null);
		} else if ("C_QUERY_FILE_CLASSIFICATION".equals(name)) {
			if (subject != null)
				return handleQueryFileClassification(subject, status);
			else
				UniversalServerUtilities
						.logError(
								CLASSNAME,
								"C_QUERY_FOLDER_CLASSIFICATION - subject is null",
								null);
		} else if ("C_QUERY_EXISTS".equals(name)) {
			if (subject != null)
				return handleQueryExists(subject, status, queryType);
			else
				UniversalServerUtilities.logError(CLASSNAME,
						"C_QUERY_EXISTS - subject is null", null);
		} else if ("C_QUERY_GET_REMOTE_OBJECT".equals(name)) {
			if (subject != null)
				return handleQueryGetRemoteObject(subject, status, queryType);
			else
				UniversalServerUtilities.logError(CLASSNAME,
						"C_QUERY_GET_REMOTE_OBJECT- subject is null", null);
		} else if ("C_GET_OSTYPE".equals(name)) {
			if (subject != null)
				return handleGetOSType(subject, status);
			else
				UniversalServerUtilities.logError(CLASSNAME,
						"C_GET_OSTYPE - subject is null", null);
		} else if (C_DOWNLOAD_FILE.equals(name)) {
			if (subject != null)
			{ 
				return handleDownload(theElement, status);
			}
			else
				UniversalServerUtilities.logError(CLASSNAME, C_DOWNLOAD_FILE
						+ " - subject is null", null);
		} else if (C_SYSTEM_ENCODING.equals(name)) {
			if (subject != null)
				return handleQueryEncoding(subject, status);
			else
				UniversalServerUtilities.logError(CLASSNAME, C_SYSTEM_ENCODING
						+ " - subject is null", null);
		} else if (C_QUERY_UNUSED_PORT.equals(name)) {
			if (subject != null)
				return handleQueryUnusedPort(subject, status);
			else
				UniversalServerUtilities.logError(CLASSNAME, C_QUERY_UNUSED_PORT
						+ " - subject is null", null);
		} else if ("C_QUERY_CLASSNAME".equals(name)) {
			if (subject != null)
				return handleQueryClassName(subject, status);
			else
				UniversalServerUtilities.logError(CLASSNAME,
						"C_QUERY_CLASSNAME- subject is null", null);
		} else if (C_QUERY_QUALIFIED_CLASSNAME.equals(name)) {
			if (subject != null)
				return handleQueryQualifiedClassName(subject, status);
			else
				UniversalServerUtilities.logError(CLASSNAME,
						C_QUERY_QUALIFIED_CLASSNAME + " - subject is null",
						null);
		} else {
			UniversalServerUtilities.logError(CLASSNAME,
					"Invalid query to handlecommand", null);
		}
		return statusDone(status);
	}

	private DataElement handleCopyBatch(DataElement targetFolder, DataElement theElement, DataElement status) 
	{
		String targetType = targetFolder.getType();
		File tgtFolder = getFileFor(targetFolder);
		int numOfSources = theElement.getNestedSize() - 2;
		
		if (targetType.equals(UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR) || targetType.equals(UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR)) 
		{
		    // if target is virtual or an archive, insert into an archive
			AbsoluteVirtualPath vpath = getAbsoluteVirtualPath(targetFolder);
			ISystemArchiveHandler handler = getArchiveHandlerFor(vpath.getContainingArchiveString());
			
			if (handler == null) 
			{
				status.setAttribute(DE.A_SOURCE, FAILED);
				return statusDone(status);
			}

			File[] srcFiles = new File[numOfSources];
			String[] names = new String[numOfSources];
			
			for (int i = 0; i < numOfSources; i++)
			{
				DataElement sourceFile = getCommandArgument(theElement, i+1);
				String srcType = sourceFile.getType();
				names[i] = sourceFile.getName();

				if (srcType.equals(UNIVERSAL_FILE_DESCRIPTOR) || srcType.equals(UNIVERSAL_FOLDER_DESCRIPTOR)
					|| srcType.equals(UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR)) 
				{		
					srcFiles[i] = getFileFor(sourceFile);
				}
				else if (srcType.equals(UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR) || srcType.equals(UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR)) 
				{
					AbsoluteVirtualPath svpath = getAbsoluteVirtualPath(sourceFile);
					ISystemArchiveHandler shandler = getArchiveHandlerFor(svpath.getContainingArchiveString());
				
					if (shandler == null) 
					{
						status.setAttribute(DE.A_SOURCE, FAILED);
						return statusDone(status);
					}
				
					VirtualChild child = shandler.getVirtualFile(svpath.getVirtualPart());
					srcFiles[i] = child.getExtractedFile();
				}
			}
			String virtualContainer = "";
			
			if (targetType.equals(UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR)) 
			{
				virtualContainer = vpath.getVirtualPart();
			}

			boolean result = handler.add(srcFiles, virtualContainer, names);
			
			if (result) {
				status.setAttribute(DE.A_SOURCE, SUCCESS);
			}
			else {
				status.setAttribute(DE.A_SOURCE, FAILED);
			}
			return statusDone(status);
		}
		else // target is a regular folder
		{
			boolean folderCopy = false;
			String source = "";
			String tgt = tgtFolder.getAbsolutePath();
			StringBuffer tgtBuf = new StringBuffer(tgt);
			handleSpecialChars(tgtBuf);
			tgt = "\"" + tgtBuf.toString() + "\"";

			int numOfNonVirtualSources = 0;
			for (int i = 0; i < numOfSources; i++)
			{
				DataElement sourceFile = getCommandArgument(theElement, i+1);
				String srcType = sourceFile.getType();
				
				if (srcType.equals(UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR) || srcType.equals(UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR)) 
				{
					// extract from an archive to folder
					AbsoluteVirtualPath svpath = getAbsoluteVirtualPath(sourceFile);
					ISystemArchiveHandler shandler = getArchiveHandlerFor(svpath.getContainingArchiveString());
			
					if (shandler == null) 
					{
						status.setAttribute(DE.A_SOURCE, FAILED);
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
					StringBuffer srcBuf = new StringBuffer(src);
					handleSpecialChars(srcBuf);
					src = "\"" + srcBuf.toString() + "\"";
	
					if (numOfNonVirtualSources == 0)
					{
						source += src;
					}
					else
					{
						source = source + " " + src;
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
				command = "xcopy " + source + " " + tgt
					+ " /S /E /K /O /Q /H /I";
			}
			else {
				command = "copy " + source + " " + tgt;
			}
		}
		else {
			if (folderCopy) {
				command = "cp  -r " + source + " " + tgt;
			}
			else {
				command = "cp " + source + " " + tgt;
			}
		}

		// run copy command
		try
		{	
			Runtime runtime = Runtime.getRuntime();
			Process p = null;
				
			if (_isWindows)
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
					String newLine = System.getProperty("line.separator");
					
					if (newLine != null && err.endsWith(newLine)) {
					    err = err.substring(0, err.length() - newLine.length());
					}
					
					// if there is something in error buffer
					// there was something in the error stream of the process
					if (err.length() > 0) {
						status.setAttribute(DE.A_SOURCE, FAILED);
						status.setAttribute(DE.A_VALUE, err);
					}
					// otherwise, nothing in the error stream
					// but we know process did not exit normally, so we indicate an unexpected error
					else {
						status.setAttribute(DE.A_SOURCE, FAILED);
						status.setAttribute(DE.A_VALUE, UNEXPECTED_ERROR);
					}
			    }
			    // otherwise if exit value is 0, process terminated normally
			    else {
					status.setAttribute(DE.A_SOURCE, SUCCESS);
			    }
			}
			// no process, so something is wrong
			else {
				status.setAttribute(DE.A_SOURCE, FAILED);
				status.setAttribute(DE.A_VALUE, UNEXPECTED_ERROR);					
			}
		}
		catch (Exception e)
		{
			UniversalServerUtilities.logError(CLASSNAME, "Exception is handleCopy", e);
			status.setAttribute(DE.A_SOURCE, FAILED);
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
						status.setAttribute(DE.A_SOURCE, FAILED);
						UniversalServerUtilities.logWarning(CLASSNAME,
								"Deletion of dir failed");
					}
				} else {
					deleteDir(list[i], status);
					if (!(list[i].delete())) {
						status.setAttribute(DE.A_SOURCE, FAILED);
						UniversalServerUtilities.logWarning(CLASSNAME,
								"Deletion of dir failed");
					}
				}
			}
		} catch (Exception e) {
			status.setAttribute(DE.A_SOURCE, FAILED_WITH_EXCEPTION);
			status.setAttribute(DE.A_VALUE, e.getLocalizedMessage());
			UniversalServerUtilities.logError(CLASSNAME,
					"Deletion of dir failed", e);
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
		if (queryType.equals(UNIVERSAL_FOLDER_DESCRIPTOR)
				|| queryType.equals(UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR)
				|| queryType.equals(UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR)) {

			fileobj = new File(subject.getAttribute(DE.A_VALUE)
					+ File.separatorChar + subject.getName());
		}
		// otherwise log error, and return as done
		else {
			UniversalServerUtilities.logError(CLASSNAME, "Invalid query type to handleSearch", null);
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

			// save search thread in hashmap for retrieval during cancel
			_cancellableThreads.put(status.getParent(), searchThread);
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
		if (queryType.equals(UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR)
				|| queryType.equals(UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR)
				|| queryType.equals(UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR)) {
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
		
		if (queryType.equals(UNIVERSAL_FILTER_DESCRIPTOR))
			fileobj = new File(subject.getName());
		else if (queryType.equals(UNIVERSAL_FOLDER_DESCRIPTOR))
			fileobj = new File(subject.getAttribute(DE.A_VALUE)
					+ File.separatorChar + subject.getName());
		else
		{
			UniversalServerUtilities.logError(CLASSNAME,
					"Invalid query type to handleQueryAll", null);
		}

		if (fileobj != null)
			// query all files and folders for the filter
			internalQueryAll(subject, fileobj, queryType, filter,
					caseSensitive, INCLUDE_ALL);

			// refresh datastore
			_dataStore.refresh(subject);


		return statusDone(status);
	}

	protected void internalQueryAll(DataElement subject, File fileobj,
			String queryType, String filter, boolean caseSensitive,
			int inclusion) {
		if (fileobj.exists()) 
		{

			
			boolean filterFiles = (inclusion == INCLUDE_ALL) || (inclusion == INCLUDE_FILES_ONLY);
			boolean filterFolders = (inclusion == INCLUDE_ALL) || (inclusion == INCLUDE_FOLDERS_ONLY);
			
			UniversalFileSystemFilter filefilter = new UniversalFileSystemFilter(filter,filterFiles, filterFolders, caseSensitive);
			String theOS = System.getProperty("os.name");
			File[] list = null;
			if (theOS.equals("z/OS")) 
			{
				// filters not supported with z/OS jvm
				File[] tempList = fileobj.listFiles();
				List acceptedList = new ArrayList(tempList.length);
	
				for (int i = 0; i < tempList.length; i++) {
					File afile = tempList[i];
					if (filefilter.accept(fileobj, afile.getName())) {
						acceptedList.add(afile);
					}
				}
				list = new File[acceptedList.size()];
				for (int l = 0; l < acceptedList.size(); l++)
					list[l] = (File) acceptedList.get(l);
			} 
			else 
			{
				list = fileobj.listFiles(filefilter);
			}
	
			if (list != null)
			{
				createDataElement(_dataStore, subject, list, queryType, filter,inclusion);
				String folderProperties = setProperties(fileobj);
				if (subject.getSource() == null || subject.getSource().equals(""))
					subject.setAttribute(DE.A_SOURCE, folderProperties);
		
				FileClassifier clsfy = getFileClassifier(subject);
				clsfy.start();
			}
		}
		else {
			/*
			UniversalServerUtilities
					.logError(
							CLASSNAME,
							"The path specified in handleQueryAll does not exist",
							null);
			status.setAttribute(DE.A_SOURCE, FAILED_WITH_EXIST);
			*/
		}
		
	}
/*
	private void internalLSQueryAll(DataElement subject, File fileobj,
			String queryType, String filter, boolean caseSensitive, int include) {

		BufferedReader reader = null; // use 'ls -l' command
		String cmd = "ls -l";
		try {
			String rootSource = fileobj.getAbsolutePath();

			Process theProcess = Runtime.getRuntime().exec(cmd, null,
					new File(rootSource));
			String specialEncoding = System
					.getProperty("dstore.stdin.encoding");
			if (specialEncoding != null) {
				reader = new BufferedReader(new InputStreamReader(theProcess
						.getInputStream(), specialEncoding));
			} else {
				reader = new BufferedReader(new InputStreamReader(theProcess
						.getInputStream()));
			}

			String line = null;
			while ((line = reader.readLine()) != null) {
				createDataElementFromLSString(subject, rootSource, line,
						include);
			}
		} catch (Exception e) {
		}
		if (reader != null) {
			try {
				reader.close();
			} catch (Exception e) {
			}
		}
		_dataStore.refresh(subject);
	}

private DataElement createDataElementFromLSString(DataElement subject,
      String rootSource, String line, int include) 
{ 
	boolean isFolder = false;

      boolean isExecutable = false; 
      String name = line; 
      int length =line.length();
      
      if (line.charAt(length - 1) == '/') 
      { 
      	isFolder = true; name =line.substring(0, length - 1); 
      } 
      else if (line.charAt(length - 1) == '*') 
      {
      	isExecutable = true; name = line.substring(0, length - 1); 
      } 
      else if (line.charAt(length - 1) == '@') { name = line.substring(0, length - 1); 
      {
      	String filePath = rootSource + File.separatorChar + name; 
      	DataElement deObj = null; 
      	if (include == INCLUDE_ALL) 
      	{ 
      		if (isFolder)
      		{ 
      			deObj = _dataStore.createObject(subject, UNIVERSAL_FOLDER_DESCRIPTOR, name); 
      		}
      		else // file
      		{
      			//if (ArchiveHandlerManager.getInstance().isArchive(list[i])) 
      			//{  
      		//	deObj = ds.createObject(subject, UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR, name); 
      		//	}
      	//		else 
 //     			{ 
      				deObj = _dataStore.createObject(subject, UNIVERSAL_FILE_DESCRIPTOR, name); 
   //   			}
      		}
      	}
      	return deObj;
      }
      return null;
      }
  
      	
	  else if (include == INCLUDE_FILES_ONLY) 
	  { 
	  	if (!isFolder) 
	  	{ 
	  		if (ArchiveHandlerManager.getInstance().isArchive(list[i])) 
	  		{  
	  			deObj = ds.createObject(subject,  UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR, name); 
      		}
	  		else 
	  		{ 
	  			deObj = _dataStore.createObject(subject,
      			UNIVERSAL_FILE_DESCRIPTOR, name); 
	  		}
	  	}
	  }
      else if (include == INCLUDE_FOLDERS_ONLY) 
      { 
      	if (isFolder) 
      	{ 
      		deObj = _dataStore.createObject(subject, UNIVERSAL_FOLDER_DESCRIPTOR, name); 
      	} 
      }
      else
      {
      }
      */
     
	  
      
//      if (deObj != null) 
  //    { 
    //  	deObj.setAttribute(DE.A_VALUE, rootSource + File.separatorChar);
      
      //_dataStore.command(dePropertyQuery, deObj); //File fobj = new
     // File(rootSource + File.separatorChar + name);
      //deObj.setAttribute(DE.A_SOURCE, setProperties(fobj, isExecutable)); }
      
      //classifyExecutable(newObject, false); return deObj; }
     // }	
  
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
		
		if (queryType.equals(UNIVERSAL_FILTER_DESCRIPTOR))
			fileobj = new File(subject.getName());
		else if (queryType.equals(UNIVERSAL_FOLDER_DESCRIPTOR))
			fileobj = new File(subject.getAttribute(DE.A_VALUE)
					+ File.separatorChar + subject.getName());
		else
			UniversalServerUtilities.logError(CLASSNAME,
					"Invalid query type to handleQueryFiles", null);


		internalQueryAll(subject, fileobj, queryType, filter, caseSensitive, INCLUDE_FILES_ONLY);
	
		_dataStore.refresh(subject);
		return statusDone(status);
	}

	/**
	 * Method to list the folders for a given filter.
	 */
	public DataElement handleQueryFolders(DataElement subject, DataElement attributes,
			DataElement status, String queryType, boolean caseSensitive) {
		if (queryType.equals(UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR)
				|| queryType.equals(UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR)
				|| queryType.equals(UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR)) {
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

		if (queryType.equals(UNIVERSAL_FILTER_DESCRIPTOR))
			fileobj = new File(subject.getName());
		else if (queryType.equals(UNIVERSAL_FOLDER_DESCRIPTOR))
			fileobj = new File(subject.getAttribute(DE.A_VALUE)
					+ File.separatorChar + subject.getName());
		else
			UniversalServerUtilities.logError(CLASSNAME,
					"Invalid query type to handleQueryFolders", null);

		internalQueryAll(subject, fileobj, queryType, filter, caseSensitive, INCLUDE_FOLDERS_ONLY);

		_dataStore.refresh(subject);
		return statusDone(status);
	}

	/**
	 * Method to list the roots.
	 */
	public DataElement handleQueryRoots(DataElement subject, DataElement status) {
//		File fileobj = new File(subject.getName());
		new File(subject.getName());
		DataElement deObj = null;

		if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
			String[] ALLDRIVES = { "c:\\", "d:\\", "e:\\", "f:\\", "g:\\",
					"h:\\", "i:\\", "j:\\", "k:\\", "l:\\", "m:\\", "n:\\",
					"o:\\", "p:\\", "q:\\", "r:\\", "s:\\", "t:\\", "u:\\",
					"v:\\", "w:\\", "x:\\", "y:\\", "z:\\" };
			for (int idx = 0; idx < ALLDRIVES.length; idx++) {
				File drive = new File(ALLDRIVES[idx]);
				if (drive.exists()) {
					try {
						String path = drive.getCanonicalPath();
						deObj = _dataStore.createObject(subject,
								UNIVERSAL_FOLDER_DESCRIPTOR, path);
						deObj.setAttribute(DE.A_SOURCE, setProperties(drive));
						deObj.setAttribute(DE.A_NAME, "");
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
						UNIVERSAL_FOLDER_DESCRIPTOR, list[i].getAbsolutePath());
				deObj.setAttribute(DE.A_SOURCE, setProperties(list[i]));
				deObj.setAttribute(DE.A_NAME, "");
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
		if (type.equals(UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR)
				|| type.equals(UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR)) {
			return handleDeleteFromArchive(subject, status);
		}

		File deleteObj = new File(subject.getAttribute(DE.A_VALUE)
				+ File.separatorChar + subject.getName());
		DataElement deObj = null;
		if (!deleteObj.exists()) {
			status.setAttribute(DE.A_SOURCE, FAILED_WITH_DOES_NOT_EXIST + "|" + deleteObj.getAbsolutePath());
			UniversalServerUtilities.logError(CLASSNAME,
					"The object to delete does not exist", null);
		} else {
			try {
				if (deleteObj.isFile()) {
					if (deleteObj.delete() == false) {
						status.setAttribute(DE.A_SOURCE, FAILED + "|" + deleteObj.getAbsolutePath());
					} else {
						// delete was successful and delete the object from the
						// datastore
						deObj = _dataStore.find(subject, DE.A_NAME, subject
								.getName(), 1);
						_dataStore.deleteObject(subject, deObj);
						status.setAttribute(DE.A_SOURCE, SUCCESS + "|" + deleteObj.getAbsolutePath());
					}
					_dataStore.refresh(subject);
				} else if (deleteObj.isDirectory()) { // it is directory and
													  // need to delete the
													  // entire directory +
					// children
					deleteDir(deleteObj, status);
					if (deleteObj.delete() == false) {
						status.setAttribute(DE.A_SOURCE, FAILED + "|" + deleteObj.getAbsolutePath());
						UniversalServerUtilities.logError(CLASSNAME,
								"Deletion of dir fialed", null);
					} else {
						_dataStore.deleteObjects(subject);
						DataElement parent = subject.getParent();
						_dataStore.deleteObject(parent, subject);
						_dataStore.refresh(parent);
					}
				} else {
					UniversalServerUtilities
							.logError(
									CLASSNAME,
									"The object to delete is neither a File or Folder! in handleDelete",
									null);
				}
			} catch (Exception e) {
				status.setAttribute(DE.A_SOURCE, FAILED_WITH_EXCEPTION + "|" + deleteObj.getAbsolutePath());
				status.setAttribute(DE.A_VALUE, e.getLocalizedMessage());
				UniversalServerUtilities.logError(CLASSNAME,
						"Delete of the object failed", e);
			}
		}
		_dataStore.refresh(subject);
		return statusDone(status);
	}
	
	private DataElement handleDeleteBatch(DataElement theElement, DataElement status)
	{
		int numOfSources = theElement.getNestedSize() - 2;
		for (int i = 0; i < numOfSources; i++)
		{
			DataElement subject = getCommandArgument(theElement, i+1);
			handleDelete(subject, status, false);
			if (!status.getSource().startsWith(SUCCESS)) return statusDone(status);
		}
		return statusDone(status);
	}

	/**
	 * Method to Rename a file or folder.
	 */
	public DataElement handleRename(DataElement subject, DataElement status) {
		File fileoldname = new File(subject.getAttribute(DE.A_VALUE)
				+ File.separatorChar + subject.getName());
		File filerename = new File(subject.getAttribute(DE.A_VALUE)
				+ File.separatorChar + subject.getAttribute(DE.A_SOURCE));

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
			if (success) {
				subject.setAttribute(DE.A_NAME, filerename.getName());
				subject.setAttribute(DE.A_SOURCE, setProperties(handler
						.getVirtualFile(newAbsPath.getVirtualPart())));
				status.setAttribute(DE.A_SOURCE, SUCCESS);
				_dataStore.update(subject);
			} else {
				status.setAttribute(DE.A_SOURCE, FAILED);
			}
			_dataStore.refresh(subject);
			return statusDone(status);
		}
		if (filerename.exists())
			status.setAttribute(DE.A_SOURCE, FAILED_WITH_EXIST);
		else {
			try {
				boolean done = fileoldname.renameTo(filerename);
				if (done) {
					subject.setAttribute(DE.A_NAME, filerename.getName());
					subject
							.setAttribute(DE.A_SOURCE,
									setProperties(filerename));
					status.setAttribute(DE.A_SOURCE, SUCCESS);

					if (filerename.isDirectory()) {
						// update children's properties
						updateChildProperties(subject, filerename);
					}
					_dataStore.update(subject);
				} else
					status.setAttribute(DE.A_SOURCE, FAILED);
			} catch (Exception e) {
				status.setAttribute(DE.A_SOURCE, FAILED);
				UniversalServerUtilities.logError(CLASSNAME,
						"handleRename failed", e);
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
		boolean wasFilter = queryType.equals(UNIVERSAL_FILTER_DESCRIPTOR);
		if (queryType.equals(UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR)) {
			return handleCreateVirtualFile(subject, status, queryType);
		}

		File filename = null;
		if (queryType.equals(UNIVERSAL_FILTER_DESCRIPTOR)) {
			if (subject.getName().indexOf(
					ArchiveHandlerManager.VIRTUAL_SEPARATOR) > 0) {
				subject.setAttribute(DE.A_TYPE,
						UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR);
				return handleCreateVirtualFile(subject, status, queryType);
			} else {
				filename = new File(subject.getValue());
				subject.setAttribute(DE.A_TYPE, UNIVERSAL_FILE_DESCRIPTOR);
				subject.setAttribute(DE.A_SOURCE, setProperties(filename));
			}
		} else if (queryType.equals(UNIVERSAL_FILE_DESCRIPTOR))
			filename = new File(subject.getAttribute(DE.A_VALUE)
					+ File.separatorChar + subject.getName());
		else
			UniversalServerUtilities.logError(CLASSNAME,
					"Invalid query type to handleCreateFile", null);

		if (filename.exists())
			status.setAttribute(DE.A_SOURCE, FAILED_WITH_EXIST);
		else {
			try {
				boolean done = filename.createNewFile();
				if (ArchiveHandlerManager.getInstance().isArchive(filename)) {
					done = ArchiveHandlerManager.getInstance()
							.createEmptyArchive(filename);
					if (done)
						subject.setAttribute(DE.A_TYPE,
								UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR);
				} else {
					if (done)
					{
						subject.setAttribute(DE.A_TYPE,
								UNIVERSAL_FILE_DESCRIPTOR);
					}
				}
				subject.setAttribute(DE.A_SOURCE, setProperties(filename));
				if (done) {
					status.setAttribute(DE.A_SOURCE, SUCCESS);
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
					status.setAttribute(DE.A_SOURCE, FAILED);
			} catch (Exception e) {
				UniversalServerUtilities.logError(CLASSNAME,
						"handleCreateFile failed", e);
				status.setAttribute(DE.A_SOURCE, FAILED);
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
		if (queryType.equals(UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR)) {
			return handleCreateVirtualFolder(subject, status, queryType);
		}

		File filename = null;
		if (queryType.equals(UNIVERSAL_FILTER_DESCRIPTOR)) 
		{
			if (subject.getName().indexOf(
					ArchiveHandlerManager.VIRTUAL_SEPARATOR) > 0) 
			{
				subject.setAttribute(DE.A_TYPE,
						UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR);
				return handleCreateVirtualFolder(subject, status, queryType);
			} 
			else 
			{
				filename = new File(subject.getValue());
				subject.setAttribute(DE.A_TYPE, UNIVERSAL_FOLDER_DESCRIPTOR);
				subject.setAttribute(DE.A_SOURCE, setProperties(filename));
			}
		} 
		else if (queryType.equals(UNIVERSAL_FOLDER_DESCRIPTOR))
		{
			filename = new File(subject.getValue());
		}
		else
			UniversalServerUtilities.logError(CLASSNAME,
					"Invalid query type to handleCreateFolder", null);

		if (filename.exists())
			status.setAttribute(DE.A_SOURCE, FAILED_WITH_EXIST);
		else 
		{
			try {
				boolean done = filename.mkdirs();
				if (done) 
				{
					status.setAttribute(DE.A_SOURCE, SUCCESS);
					subject.setAttribute(DE.A_SOURCE, setProperties(filename));
					subject.setAttribute(DE.A_TYPE,UNIVERSAL_FOLDER_DESCRIPTOR);
					subject.setAttribute(DE.A_NAME, filename.getName());
					subject.setAttribute(DE.A_VALUE, filename.getParentFile().getAbsolutePath());
				} 
				else
				{
					status.setAttribute(DE.A_SOURCE, FAILED);
				}
				
			} catch (Exception e) {
				UniversalServerUtilities.logError(CLASSNAME,
						"handleCreateFolder failed", e);
				status.setAttribute(DE.A_SOURCE, FAILED);
			}
		}
		_dataStore.refresh(subject);
		return statusDone(status);
	}

	/**
	 * Method to set ReadOnly to a file or folder.
	 */
	public DataElement handleSetReadOnly(DataElement subject, DataElement status) {

		File filename = new File(subject.getAttribute(DE.A_VALUE)
				+ File.separatorChar + subject.getName());
		if (!filename.exists())
			status.setAttribute(DE.A_SOURCE, FAILED_WITH_DOES_NOT_EXIST);
		else {
			try {
				boolean done = filename.setReadOnly();
				if (done) {
					status.setAttribute(DE.A_SOURCE, SUCCESS);
					subject.setAttribute(DE.A_SOURCE, setProperties(filename));
					_dataStore.refresh(subject);
				} else {
					status.setAttribute(DE.A_SOURCE, FAILED);
				}
			} catch (Exception e) {
				UniversalServerUtilities.logError(CLASSNAME,
						"handleSetreadOnly", e);
			}
		}
		_dataStore.refresh(subject);
		return statusDone(status);
	}

	/**
	 * Method to set LastModified to a file or folder.
	 */
	public DataElement handleSetLastModified(DataElement subject,
			DataElement status) {

		File filename = new File(subject.getAttribute(DE.A_VALUE));
		if (!filename.exists())
			status.setAttribute(DE.A_SOURCE, FAILED_WITH_DOES_NOT_EXIST);
		else {
			try {
				String str = subject.getAttribute(DE.A_SOURCE);

				long date = Long.parseLong(str);
				boolean done = filename.setLastModified(date);

				filename = new File(subject.getAttribute(DE.A_VALUE));
				if (done) {
					status.setAttribute(DE.A_SOURCE, SUCCESS);
					subject.setAttribute(DE.A_SOURCE, setProperties(filename));
					_dataStore.refresh(subject);
				} else
					status.setAttribute(DE.A_SOURCE, FAILED);
			} catch (Exception e) {
				UniversalServerUtilities.logError(CLASSNAME,
						"handleSetLastModified", e);
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

		String version = VERSION_1;
		StringBuffer buffer = new StringBuffer(50);
		boolean canWrite = fileObj.canWrite();

		buffer.append(version).append(TOKEN_SEPARATOR).append(canWrite);
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
		if (queryType.equals(UNIVERSAL_FILTER_DESCRIPTOR)) {
			if (subject.getName().indexOf(
					ArchiveHandlerManager.VIRTUAL_SEPARATOR) > 0) {
				VirtualChild child = _archiveHandlerManager
						.getVirtualObject(subject.getName());
				if (child.exists()) {
					status.setAttribute(DE.A_SOURCE, "true");
					return statusDone(status);
				} else {
					status.setAttribute(DE.A_SOURCE, "false");
					return statusDone(status);
				}
			} else {
				fileobj = new File(subject.getName());
			}
		} else if (queryType.equals(UNIVERSAL_FILE_DESCRIPTOR)
				|| queryType.equals(UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR))
			fileobj = new File(subject.getAttribute(DE.A_VALUE)
					+ File.separatorChar + subject.getName());
		else if (queryType.equals(UNIVERSAL_FOLDER_DESCRIPTOR))
			fileobj = new File(subject.getAttribute(DE.A_VALUE));
		else if (queryType.equals(UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR)
				|| queryType.equals(UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR)) {
			AbsoluteVirtualPath vpath = getAbsoluteVirtualPath(subject);
			ISystemArchiveHandler handler = _archiveHandlerManager
					.getRegisteredHandler(new File(vpath
							.getContainingArchiveString()));
			if (handler == null) {
				status.setAttribute(DE.A_SOURCE, "false");
				return statusDone(status);
			}
			VirtualChild child = handler.getVirtualFile(vpath.getVirtualPart());
			if (child.exists()) {
				status.setAttribute(DE.A_SOURCE, "true");
				return statusDone(status);
			}

		}

		if (fileobj.exists())
			status.setAttribute(DE.A_SOURCE, "true");
		else
			status.setAttribute(DE.A_SOURCE, "false");
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
		if (queryType.equals(UNIVERSAL_FILTER_DESCRIPTOR)) {
			isVirtual = ArchiveHandlerManager.isVirtual(fullName);
			String filterValue = subject.getValue();
			// . translates to home dir
			if (filterValue.equals(".")) 
			{
				filterValue = System.getProperty("user.home");
				subject.setAttribute(DE.A_VALUE, filterValue);
			}
			if (!isVirtual)
				fileobj = new File(filterValue);
		} else {
			UniversalServerUtilities.logError(CLASSNAME,
					"Invalid query type to handleQueryGetRemoteObject", null);
			return statusDone(status);
		}

		if (!isVirtual && fileobj.exists()) {

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
					subject.setAttribute(DE.A_TYPE,UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR);
				} else {
					subject.setAttribute(DE.A_TYPE, UNIVERSAL_FILE_DESCRIPTOR);
				}
				String name = fullName
						.substring(
								fullName.lastIndexOf(File.separatorChar) + 1,
								fullName.length());
				String path = fullName.substring(0, fullName
						.lastIndexOf(File.separatorChar));
				subject.setAttribute(DE.A_NAME, name);
				subject.setAttribute(DE.A_VALUE, path);
			} 
			else { // directory
				subject.setAttribute(DE.A_TYPE, UNIVERSAL_FOLDER_DESCRIPTOR);
				subject.setAttribute(DE.A_NAME, "");
				subject.setAttribute(DE.A_VALUE, fullName);
			}

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

			status.setAttribute(DE.A_SOURCE, SUCCESS);
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
								UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR);
						subject.setAttribute(DE.A_NAME, child.name);
						if (child.path.equals("")) {
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
								UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR);
						String name = child.name;
						String path = avp.getContainingArchiveString();
						if (!child.path.equals("")) {
							path = path
									+ ArchiveHandlerManager.VIRTUAL_SEPARATOR
									+ child.path;
						}

						subject.setAttribute(DE.A_NAME, name);
						subject.setAttribute(DE.A_VALUE, path);
					}

					subject.setAttribute(DE.A_SOURCE, setProperties(child));
					status.setAttribute(DE.A_SOURCE, SUCCESS);
				} else {
					UniversalServerUtilities.logWarning(CLASSNAME,
							"object does not exist");
					subject.setAttribute(DE.A_SOURCE, setProperties(child));
					status
							.setAttribute(DE.A_SOURCE,
									FAILED_WITH_DOES_NOT_EXIST);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			subject.setAttribute(DE.A_SOURCE, setProperties(fileobj));
			status.setAttribute(DE.A_SOURCE, FAILED_WITH_DOES_NOT_EXIST);
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
				} else if (type.equals(UNIVERSAL_FOLDER_DESCRIPTOR)) {
					return child;
				}
			}
		}

		return null;
	}

	protected void createDataElement(DataStore ds, DataElement subject,
			File[] list, String queryType, String filter, int include)
	{
		createDataElement(ds, subject, list, queryType, filter, include, null);
	}
	/**
	 * Method to create the DataElement object in the datastore.
	 */

	protected void createDataElement(DataStore ds, DataElement subject,
			File[] list, String queryType, String filter, int include, String types[]) 
	{

		IdentityHashMap foundMap = new IdentityHashMap(list.length);
		List children = subject.getNestedData();
		if (children != null)
		{
			ArrayList filteredChildren = new ArrayList();
			for (int f = 0; f < children.size(); f++)
			{
				DataElement child = (DataElement)children.get(f);
				String type = child.getType();
				if (type.equals(UNIVERSAL_FILE_DESCRIPTOR) || type.equals(UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR))
				{
					if (StringCompare.compare(filter, child.getName(), false))
					{
						filteredChildren.add(child);
					}
				}
				else
				{
					filteredChildren.add(child);
				}				
			}
			
		
			if (filteredChildren.size() != 0) 
			{
				boolean found;
				Object[] currentObjList = filteredChildren.toArray();
						
				// Check if the current Objects in the DataStore are valid... exist
				// on the remote host
				try {
					for (int i = 0; i < currentObjList.length; ++i) 
					{
						found = false;
						DataElement previousElement = (DataElement) currentObjList[i];
						for (int j = 0; j < list.length && !found; ++j) 
						{
							
							if (previousElement.getName().equals(list[j].getName()) && !previousElement.isDeleted()) 
							{
								// Type have to be equal as well
								String type = ((DataElement) currentObjList[i]).getType();
								boolean isfile = list[j].isFile();
								if (((type.equals(UNIVERSAL_FILE_DESCRIPTOR) || type.equals(UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR)) && isfile)
										|| 
									(type.equals(UNIVERSAL_FOLDER_DESCRIPTOR) && !isfile))
								{
									if (types !=null)
									{
										String attributes = previousElement.getAttribute(DE.A_SOURCE);
										String thisType = types[j];
										if (attributes.indexOf(thisType) != -1)
										{
											foundMap.put(list[j], currentObjList[i]); // already exists so don't recreate the element
											found = true;
										}
									}
									else
									{
										foundMap.put(list[j], currentObjList[i]); // already exists so don't recreate the element
										found = true;
									}
								}
							}
						} // end for j
	
						// Object in DataStore no longer exists in the host. Remove
						// it from DataStore.
						if (found == false) 
						{
							//DataElement deObj = ds.find(subject, DE.A_NAME,((DataElement) currentObjList[i]).getName(), 1);
							ds.deleteObject(subject, previousElement);
						}
					} // end for i
				} catch (Exception e) {
					e.printStackTrace();
					UniversalServerUtilities.logError(CLASSNAME,
							"createDataElement failed with exception - isFile ", e);
				}
			} // end currentObj not 0
		}

		// Now query the other way. If the object queried, exists on the host,
		// then check if
		// it is already in datastore. If so do not recreate it.

		if (list != null) 
		{

			/*
			 * DKM: I've commented out this as we shouldn't need it any more
			 * with the above code working
			 * 
			 * if (subject.getNestedData().size() != 0) {
			 * _dataStore.deleteObjects(subject); // subject.removeNestedData();
			 * //System.out.println("removing nested data"); }
			 */
		    
			 
			// DKM - test - dummy object
			//ds.createObject(subject,UNIVERSAL_FILE_DESCRIPTOR,"I'm not really here!");

			for (int i = 0; i < list.length; i++) 
			{
				DataElement deObj = null;
				File file = list[i];
			
				try 
				{
					String fileName = file.getName();
					boolean isHidden = file.isHidden() || fileName.charAt(0) == '.';

					if (!isHidden || showHidden)
					{
						// check for duplicates
						Object obj = foundMap.get(file);
						if (obj != null && obj instanceof DataElement)
						{
							deObj = (DataElement)obj;
						}
						//deObj = getFileElement(subject, list[i]);
						
						if (deObj == null) 
						{
							if (include == INCLUDE_ALL) 
							{
								if (file.isDirectory())
								{
									deObj = ds.createObject(subject,deUniversalFolderObject,fileName);
								}
								else
								// file
								{
									if (ArchiveHandlerManager.getInstance().isArchive(file)) 
									{
										deObj = ds
												.createObject(
														subject,
														deUniversalArchiveFileObject,
														fileName);
									} 
									else 
									{
										deObj = ds.createObject(subject,
												deUniversalFileObject,
												fileName);
									}
								}
							} 
							else if (include == INCLUDE_FOLDERS_ONLY) 
							{
								if (ArchiveHandlerManager.getInstance().isArchive(file)) 
								{
									deObj = ds.createObject(subject,
											deUniversalArchiveFileObject,
											fileName);
								} 
								else 
								{
									deObj = ds.createObject(subject,
											deUniversalFolderObject,
											fileName);
								}
							} 
							else if (include == INCLUDE_FILES_ONLY) 
							{
								if (ArchiveHandlerManager.getInstance().isArchive(file)) 
								{
									deObj = ds.createObject(subject,
											deUniversalArchiveFileObject,
											fileName);
								} 
								else 
								{
									deObj = ds
											.createObject(subject,
													deUniversalFileObject,
													fileName);
								}
							}

							if (queryType.equals(UNIVERSAL_FILTER_DESCRIPTOR))
							{
								deObj.setAttribute(DE.A_VALUE, subject.getAttribute(DE.A_VALUE));
							}
							else 
							{
							
								if (subject.getName().length() > 0) 
								{
									String valueStr = subject.getAttribute(DE.A_VALUE);
									//String valueStr = list[i].getParentFile().getAbsolutePath();
									StringBuffer valueBuffer = new StringBuffer(valueStr);
									if ((_isWindows && valueStr.endsWith("\\"))|| valueStr.endsWith("/") || subject.getName().startsWith("/")) 
									{
										valueBuffer.append(subject.getName());
										deObj.setAttribute(DE.A_VALUE,valueBuffer.toString());
									} 
									else 
									{
										valueBuffer.append(File.separatorChar);
										valueBuffer.append(subject.getName());
										deObj.setAttribute(DE.A_VALUE,valueBuffer.toString());
									}
								} 
								else 
								{
									String valueStr = list[i].getParentFile().getAbsolutePath();
									deObj.setAttribute(DE.A_VALUE, valueStr);
								}
							}
						}
						// DKM - do basic property stuff here
						String properties = setProperties(file);
						
						if (types != null)
						{
						    /*
						    String oldClassification = getClassificationString(deObj.getAttribute(DE.A_SOURCE));
						    if (oldClassification != null && !oldClassification.equals(types[i]))
						    {
						        deObj.setAttribute(DE.A_SOURCE, properties + "|" + oldClassification + " " + types[i]);
						    }
						    else
						    */
						    {
						        deObj.setAttribute(DE.A_SOURCE, properties + "|" + types[i]);
						    }
						}
						else
						{
							deObj.setAttribute(DE.A_SOURCE, properties);
						}
				
					}
				} catch (Exception e) 
				{
					UniversalServerUtilities
							.logError(
									CLASSNAME,
									"createDataElement failed with exception - isHidden ",
									e);
				}
			} // end for
		} // if list !=null
		//*/

	}

	/**
	 * Method to create the DataElement object in the datastore out of a list of
	 * VirtualChildren
	 */

	protected void createDataElement(DataStore ds, DataElement subject,
			VirtualChild[] list, String filter, String rootPath,
			String virtualPath) 
	{

		IdentityHashMap foundMap = null;
		List children = subject.getNestedData();
		if (children != null)
		{
			ArrayList filteredChildren = new ArrayList();
			
			for (int f = 0; f < children.size(); f++)
			{
				DataElement child = (DataElement)children.get(f);
				String type = child.getType();
				if (type.equals(UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR) || 
				        type.equals(UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR))
				{
					if (StringCompare.compare(filter, child.getName(), false))
					{
						filteredChildren.add(child);
					}
				}
				else
				{
					filteredChildren.add(child);
				}				
			}
			
			foundMap = new IdentityHashMap(list.length);
			
			if (filteredChildren.size() != 0) 
			{
				boolean found;
				Object[] currentObjList = filteredChildren.toArray();
	
				// Check if the current Objects in the DataStore are valid... exist
				// on the remote host
				try {
					for (int i = 0; i < currentObjList.length; ++i) {
						found = false;
						DataElement previousElement = (DataElement) currentObjList[i];
						for (int j = 0; j < list.length && !found; ++j) 
						{
							
							if (!previousElement.isDeleted()
									&& previousElement.getName().equals(list[j].name)) 
							{
								// Type have to be equal as well
								String type = previousElement.getType();
								boolean isfile = !list[j].isDirectory;
								if (type.equals(UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR) 
										|| (type.equals(UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR) && !isfile)
										)
								{
								    foundMap.put(list[j], previousElement);
									found = true;
								}
							}
						} // end for j
	
						// Object in DataStore no longer exists in the host. Remove
						// it from DataStore.
						if (found == false) 
						{
							ds.deleteObject(subject, previousElement);
						}
					} // end for i
				} catch (Exception e) {
					e.printStackTrace();
					UniversalServerUtilities.logError(CLASSNAME,
							"createDataElement failed with exception - isFile ", e);
				}
			} // end currentObj not 0
		}

		
		// Now query the other way. If the object queried, exists on the host,
		// then check if
		// it is already in datastore. If so do not recreate it.

		if (list != null) 
		{
			for (int i = 0; i < list.length; i++) 
			{
			    DataElement deObj = null;
				VirtualChild child = list[i];
				Object obj = null;
				if (foundMap != null)
				{
					obj = foundMap.get(child);
				}
				if (obj != null && obj instanceof DataElement)
				{
					deObj = (DataElement)obj;
				}

				if (deObj == null) 
				{
					if (child.isDirectory) 
					{
						deObj = _dataStore.createObject(subject, UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR,child.name);
					} 
					else // file
					{
						deObj = _dataStore.createObject(subject,UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR, child.name);
					}
				
				}
				deObj.setAttribute(DE.A_VALUE, rootPath + ArchiveHandlerManager.VIRTUAL_SEPARATOR + virtualPath);
				deObj.setAttribute(DE.A_SOURCE, setProperties(child));
			}
		}

		//*/
	}

	public String setProperties(File fileObj) {
		return setProperties(fileObj, false);
	}

	/**
	 * Method to obtain the properties of file or folder.
	 */
	public String setProperties(File fileObj, boolean doArchiveProperties) {
		String version = VERSION_1;
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
			comment = " ";

		long compressedSize = size;
		String compressionMethod = " ";
		double compressionRatio = 0;

		long expandedSize;
		if (isArchive)
			expandedSize = ArchiveHandlerManager.getInstance().getExpandedSize(
					fileObj);
		else
			expandedSize = size;

		buffer.append(version).append(TOKEN_SEPARATOR).append(date).append(
				TOKEN_SEPARATOR).append(size).append(TOKEN_SEPARATOR);
		buffer.append(hidden).append(TOKEN_SEPARATOR).append(canWrite).append(
				TOKEN_SEPARATOR).append(canRead);

		// values might not be used but we set them here just so that there are right number
		// of properties
		buffer.append(TOKEN_SEPARATOR);
		buffer.append(comment).append(TOKEN_SEPARATOR).append(compressedSize)
				.append(TOKEN_SEPARATOR).append(compressionMethod).append(
						TOKEN_SEPARATOR);
		buffer.append(compressionRatio).append(TOKEN_SEPARATOR).append(
				expandedSize);
		

		String buf = buffer.toString();
		return buf;
	}

	public String setProperties(VirtualChild fileObj) {
		String version = VERSION_1;
		StringBuffer buffer = new StringBuffer(500);
		long date = fileObj.getTimeStamp();
		long size = fileObj.getSize();
		boolean hidden = false;
		boolean canWrite = fileObj.getContainingArchive().canWrite();
		boolean canRead = fileObj.getContainingArchive().canRead();

		// These extra properties here might cause problems for older clients,
		// ie: a IndexOutOfBounds in UniversalFileImpl.
		String comment = fileObj.getComment();
		if (comment.equals(""))
			comment = " "; // make sure this is still a
		// token
		long compressedSize = fileObj.getCompressedSize();
		String compressionMethod = fileObj.getCompressionMethod();
		if (compressionMethod.equals(""))
			compressionMethod = " ";
		double compressionRatio = fileObj.getCompressionRatio();
		long expandedSize = size;

		buffer.append(version).append(TOKEN_SEPARATOR).append(date).append(
				TOKEN_SEPARATOR).append(size).append(TOKEN_SEPARATOR);
		buffer.append(hidden).append(TOKEN_SEPARATOR).append(canWrite).append(
				TOKEN_SEPARATOR).append(canRead);

		buffer.append(TOKEN_SEPARATOR);
		buffer.append(comment).append(TOKEN_SEPARATOR).append(compressedSize)
				.append(TOKEN_SEPARATOR).append(compressionMethod).append(
						TOKEN_SEPARATOR);
		buffer.append(compressionRatio).append(TOKEN_SEPARATOR).append(
				expandedSize);

		return buffer.toString();
	}

	/**
	 * Method to obtain the classificatoin string of file or folder.
	 */
	protected String getClassificationString(String s) {

		//StringTokenizer tokenizer = new StringTokenizer(s, TOKEN_SEPARATOR);
		String[] str = s.split("\\"+TOKEN_SEPARATOR);
		int tokens = str.length;
		if (tokens < 10)
		    return null;
		/*
		int tokens = tokenizer.countTokens();
		if (tokens < 10)
		    return null;
		
		String[] str = new String[tokens];
		
		for (int i = 0; i < tokens; ++i) {
			str[i] = tokenizer.nextToken();
		}
*/
		
		return (str[10]);
	}
	/**
	 * Method to obtain the filter string of file or folder.
	 */
	protected String getFilterString(String s) {

		//StringTokenizer tokenizer = new StringTokenizer(s, TOKEN_SEPARATOR);
		String[] str = s.split("\\"+TOKEN_SEPARATOR);
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
		    System.out.println("problem with properties:"+s);
		    return "*";
		}
	}

	/**
	 * Method to obtain the show Hidden flag for file or folder.
	 */
	protected boolean getShowHiddenFlag(String s) {

		//StringTokenizer tokenizer = new StringTokenizer(s, TOKEN_SEPARATOR);
		String[] str = s.split("\\"+TOKEN_SEPARATOR);
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
		    System.out.println("show hidden flag problem:"+s);
		    return true;
		}
	}

	/**
	 * Method to obtain the depth for a search
	 */
	protected int getDepth(String s) 
	{
		String[] str = s.split("\\"+TOKEN_SEPARATOR);
		int tokens = str.length;
	    /*
		StringTokenizer tokenizer = new StringTokenizer(s, TOKEN_SEPARATOR);

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

		// save find thread in hashmap for retrieval during cancel
		_cancellableThreads.put(status.getParent(), downloadThread);
		return status;

		/*
		 * DataElement arg1 = getCommandArgument(theElement, 1); String
		 * elementType = arg1.getType(); String remotePath = arg1.getName();
		 * 
		 * String resultType = null; String resultMessage = null;
		 * 
		 * FileInputStream inputStream = null; BufferedInputStream
		 * bufInputStream = null;
		 * 
		 * try {
		 * 
		 * if (elementType.equals(UNIVERSAL_FILE_DESCRIPTOR) ||
		 * elementType.equals(UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR) ||
		 * elementType.equals(UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR)) {
		 * _dataStore.trace("download:" + remotePath + "," + elementType);
		 * 
		 * File file = new File(remotePath);
		 * 
		 * 
		 * if (elementType.equals(UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR)) {
		 * AbsoluteVirtualPath vpath = getAbsoluteVirtualPath(remotePath);
		 * 
		 * ISystemArchiveHandler handler =
		 * _archiveHandlerManager.getRegisteredHandler(new
		 * File(vpath.getContainingArchiveString())); VirtualChild vChild =
		 * handler.getVirtualFile(vpath.getVirtualPart()); file =
		 * vChild.getExtractedFile(); }
		 * 
		 * DataElement arg2 = getCommandArgument(theElement, 2); DataElement
		 * arg3 = getCommandArgument(theElement, 3);
		 * 
		 * int mode = (Integer.valueOf(arg1.getSource())).intValue(); String
		 * localPath = arg2.getName();
		 * 
		 * boolean isText = (mode == TEXT_MODE);
		 * 
		 * String clientEncoding = null;
		 * 
		 * if (isText) { clientEncoding = arg2.getSource(); } // Read in the
		 * file inputStream = new FileInputStream(file); bufInputStream = new
		 * BufferedInputStream(inputStream, BUFFER_SIZE);
		 * 
		 * boolean first = true; byte[] buffer = new byte[BUFFER_SIZE]; byte[]
		 * convBytes; int numToRead = 0;
		 * 
		 * int available = bufInputStream.available();
		 * 
		 * while (available > 0) { numToRead = (available < BUFFER_SIZE) ?
		 * available : BUFFER_SIZE;
		 * 
		 * int bytesRead = bufInputStream.read(buffer, 0, numToRead);
		 * 
		 * if (bytesRead == -1) break;
		 * 
		 * if (isText) { convBytes = (new String(buffer, 0,
		 * bytesRead)).getBytes(clientEncoding);
		 * 
		 * if (first) { // send first set of bytes first = false;
		 * _dataStore.updateFile(localPath, convBytes, convBytes.length, true); }
		 * else { // append subsequent segments
		 * _dataStore.updateAppendFile(localPath, convBytes, convBytes.length,
		 * true); } } else {
		 * 
		 * if (first) { // send first set of bytes first = false;
		 * _dataStore.updateFile(localPath, buffer, bytesRead, true); } else { //
		 * append subsequent segments _dataStore.updateAppendFile(localPath,
		 * buffer, bytesRead, true); } }
		 * 
		 * available = bufInputStream.available(); }
		 * 
		 * resultType = DOWNLOAD_RESULT_SUCCESS_TYPE; resultMessage =
		 * DOWNLOAD_RESULT_SUCCESS_MESSAGE; } } catch (FileNotFoundException e) {
		 * UniversalServerUtilities.logError(CLASSNAME, "handleDownload: error
		 * reading file " + remotePath, e); resultType =
		 * DOWNLOAD_RESULT_FILE_NOT_FOUND_EXCEPTION; resultMessage =
		 * e.getLocalizedMessage(); } catch (UnsupportedEncodingException e) {
		 * UniversalServerUtilities.logError(CLASSNAME, "handleDownload: error
		 * reading file " + remotePath, e); resultType =
		 * DOWNLOAD_RESULT_UNSUPPORTED_ENCODING_EXCEPTION; resultMessage =
		 * e.getLocalizedMessage(); } catch (IOException e) {
		 * UniversalServerUtilities.logError(CLASSNAME, "handleDownload: error
		 * reading file " + remotePath, e); resultType =
		 * DOWNLOAD_RESULT_IO_EXCEPTION; resultMessage =
		 * e.getLocalizedMessage(); } catch (Exception e) { e.printStackTrace(); }
		 * finally {
		 * 
		 * try {
		 * 
		 * if (bufInputStream != null) bufInputStream.close(); } catch
		 * (IOException e) { UniversalServerUtilities.logError(CLASSNAME,
		 * "handleDownload: error closing reader on " + remotePath, e);
		 * resultType = DOWNLOAD_RESULT_IO_EXCEPTION; resultMessage =
		 * e.getMessage(); } }
		 * 
		 * _dataStore.createObject(arg1, resultType, resultMessage);
		 * _dataStore.refresh(arg1); return statusDone(status);
		 */
	}

	/**
	 * Get the system encoding
	 */
	protected DataElement handleQueryEncoding(DataElement subject, DataElement status) {

		String encoding = System.getProperty("file.encoding");

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
			UniversalServerUtilities.logError(CLASSNAME, "Can not get unused port", e);
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
		status.setAttribute(DE.A_NAME, "cancelled");
		_dataStore.refresh(status);
		return status;
	}

	/**
	 * @see Miner#load()
	 */
	public void load() {
		// Create datastore tree structure for UniversalFileSystemMiner
		deUFSnode = _dataStore.createObject(_minerData, UNIVERSAL_NODE_DESCRIPTOR, "universal.node");
//		deUFStemp = _dataStore.createObject(deUFSnode, UNIVERSAL_NODE_DESCRIPTOR, "universal.temp");
		_dataStore.createObject(deUFSnode, UNIVERSAL_NODE_DESCRIPTOR, "universal.temp");
//		deUFSfilters = _dataStore.createObject(deUFSnode, UNIVERSAL_NODE_DESCRIPTOR, "universal.filters");
		_dataStore.createObject(deUFSnode, UNIVERSAL_NODE_DESCRIPTOR, "universal.filters");
		deUFSuploadlog = _dataStore.createObject(deUFSnode, UNIVERSAL_NODE_DESCRIPTOR, "universal.uploadlog");

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
				_dataStore.createObject(status, "qualifiedClassName", name);
			} else {
				_dataStore.createObject(status, "qualifiedClassName", "null");
			}
		} catch (java.io.IOException e) {
			_dataStore.createObject(status, "qualifiedClassName", "null");
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
			sep = "/";
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
				String execJava = "executable(java:";
				
				int idx = classification.indexOf(execJava);
				
				if (idx != -1) {
					idx = idx + execJava.length(); 
					int jdx = classification.indexOf(")", idx);
					
					if (jdx != -1) {
						
						if (jdx > idx) {
							className = classification.substring(idx, jdx);
						}
						else if (jdx == idx) {
							className = "";
						}
					}
				}
			}

			if (className != null) {
				_dataStore.createObject(status, TYPE_QUALIFIED_CLASSNAME, className);
			} else {
				_dataStore.createObject(status, TYPE_QUALIFIED_CLASSNAME, "null");
			}
		} catch (IOException e) {
			UniversalServerUtilities.logError(CLASSNAME,
					"I/O error occured trying to read class file " + filePath,
					null);
			
			_dataStore.createObject(status, TYPE_QUALIFIED_CLASSNAME, "null");
		}

		return statusDone(status);
	}

	/**
	 * Method to retrieve the OS that the miner is running.
	 */
	public DataElement handleGetOSType(DataElement subject, DataElement status) {
		String osType = System.getProperty("os.name").toLowerCase();
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
//		DataElement snode = createObjectDescriptor(schemaRoot, UNIVERSAL_NODE_DESCRIPTOR);
		createObjectDescriptor(schemaRoot, UNIVERSAL_NODE_DESCRIPTOR);

		DataElement tempnode = createObjectDescriptor(schemaRoot,
				UNIVERSAL_TEMP_DESCRIPTOR);

		// Define filesystem descriptors
		DataElement UniversalFilter = createObjectDescriptor(schemaRoot,
				UNIVERSAL_FILTER_DESCRIPTOR);
		deUniversalFileObject = createObjectDescriptor(schemaRoot,
				UNIVERSAL_FILE_DESCRIPTOR);
		deUniversalFolderObject = createObjectDescriptor(schemaRoot,
				UNIVERSAL_FOLDER_DESCRIPTOR);
		deUniversalArchiveFileObject = createObjectDescriptor(
				schemaRoot, UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR);
		deUniversalVirtualFileObject = createObjectDescriptor(
				schemaRoot, UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR);
		deUniversalVirtualFolderObject = createObjectDescriptor(
				schemaRoot, UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR);

		_dataStore.refresh(schemaRoot);

		// Define command descriptors
		createCommandDescriptor(UniversalFilter, "Filter", "C_QUERY_VIEW_ALL");
		createCommandDescriptor(UniversalFilter, "Filter", "C_QUERY_VIEW_FILES");
		createCommandDescriptor(UniversalFilter, "Filter",
				"C_QUERY_VIEW_FOLDERS");
		createCommandDescriptor(UniversalFilter, "Filter", "C_QUERY_ROOTS");

		createCommandDescriptor(UniversalFilter, "GetOSType", "C_GET_OSTYPE");
		createCommandDescriptor(UniversalFilter, "Exists", "C_QUERY_EXISTS");
		createCommandDescriptor(UniversalFilter, "GetRemoteObject",
				"C_QUERY_GET_REMOTE_OBJECT");
		createCommandDescriptor(UniversalFilter, "CreateNewFile",
				"C_CREATE_FILE");
		createCommandDescriptor(UniversalFilter, "CreateNewFolder",
				"C_CREATE_FOLDER");
		createCommandDescriptor(deUniversalFolderObject, "Filter",
				"C_QUERY_VIEW_ALL");
		createCommandDescriptor(deUniversalFolderObject, "Filter",
				"C_QUERY_VIEW_FILES");
		createCommandDescriptor(deUniversalFolderObject, "Filter",
				"C_QUERY_VIEW_FOLDERS");
		createCommandDescriptor(deUniversalArchiveFileObject, "Filter",
				"C_QUERY_VIEW_ALL");
		createCommandDescriptor(deUniversalArchiveFileObject, "Filter",
				"C_QUERY_VIEW_FILES");
		createCommandDescriptor(deUniversalArchiveFileObject, "Filter",
				"C_QUERY_VIEW_FOLDERS");

		_dataStore.createReference(deUniversalFileObject,
				deUniversalArchiveFileObject, "abstracts", "abstracted by");
		_dataStore.createReference(deUniversalFolderObject,
				deUniversalArchiveFileObject, "abstracts", "abstracted by");
		_dataStore.createReference(deUniversalFileObject,
				deUniversalVirtualFileObject, "abstracts", "abstracted by");
		_dataStore.createReference(deUniversalFolderObject,
				deUniversalVirtualFolderObject, "abstracts", "abstracted by");

		// create the search descriptor and make it cacnellable
		DataElement searchDescriptor = createCommandDescriptor(
				deUniversalFolderObject, "Search", "C_SEARCH");
		DataElement cancellable = _dataStore.find(schemaRoot, DE.A_NAME,
				DataStoreResources.model_Cancellable, 1);
		_dataStore.createReference(cancellable, searchDescriptor, "abstracts",
				"abstracted by");

		createCommandDescriptor(deUniversalFolderObject, "GetAdvanceProperty",
				"C_QUERY_ADVANCE_PROPERTY");
		createCommandDescriptor(tempnode, "Filter", "C_CREATE_TEMP");
		createCommandDescriptor(deUniversalFileObject, "Delete", "C_DELETE");
		createCommandDescriptor(deUniversalFileObject, "DeleteBatch", "C_DELETE_BATCH");
		createCommandDescriptor(deUniversalFileObject, "CreateNewFile",
				"C_CREATE_FILE");
		createCommandDescriptor(deUniversalFileObject, "CreateNewFolder",
				"C_CREATE_FOLDER");
		createCommandDescriptor(deUniversalFileObject, "Rename", "C_RENAME");
		createCommandDescriptor(deUniversalFileObject, "SetReadOnly",
				"C_SET_READONLY");
		createCommandDescriptor(deUniversalFileObject, "SetLastModified",
				"C_SET_LASTMODIFIED");
		createCommandDescriptor(deUniversalFileObject, "GetAdvanceProperty",
				"C_QUERY_ADVANCE_PROPERTY");
//		dePropertyQuery = createCommandDescriptor(deUniversalFileObject, "GetBasicProperty", "C_QUERY_BASIC_PROPERTY");
		createCommandDescriptor(deUniversalFileObject, "GetBasicProperty", "C_QUERY_BASIC_PROPERTY");

		createCommandDescriptor(deUniversalFileObject, "GetcanWriteProperty",
				"C_QUERY_CAN_WRITE_PROPERTY");
		createCommandDescriptor(deUniversalFileObject, "Exists", "C_QUERY_EXISTS");

		createCommandDescriptor(deUniversalFolderObject, "Delete", "C_DELETE");
		createCommandDescriptor(deUniversalFolderObject, "DeleteBatch", "C_DELETE_BATCH");
		createCommandDescriptor(deUniversalFolderObject, "Rename", "C_RENAME");
		createCommandDescriptor(deUniversalFolderObject, "Copy", "C_COPY");
		createCommandDescriptor(deUniversalFolderObject, "CopyBatch", "C_COPY_BATCH");
		createCommandDescriptor(deUniversalFolderObject, "CreateNewFolder",
				"C_CREATE_FOLDER");
		createCommandDescriptor(deUniversalFolderObject, "SetReadOnly",
				"C_SET_READONLY");
		createCommandDescriptor(deUniversalFolderObject, "SetLastModified",
				"C_SET_LASTMODIFIED");
		createCommandDescriptor(deUniversalFolderObject, "GetBasicProperty",
				"C_QUERY_BASIC_PROPERTY");
		createCommandDescriptor(deUniversalFolderObject, "GetcanWriteProperty",
				"C_QUERY_CAN_WRITE_PROPERTY");

//		deFileClassificationQuery = createCommandDescriptor(deUniversalFileObject, "GetFileClassifications", "C_QUERY_FILE_CLASSIFICATIONS");
		createCommandDescriptor(deUniversalFileObject, "GetFileClassifications", "C_QUERY_FILE_CLASSIFICATIONS");
//		deFolderClassificationQuery = createCommandDescriptor(deUniversalFolderObject, "GetFolderClassifications", "C_QUERY_FILE_CLASSIFICATION");
		createCommandDescriptor(deUniversalFolderObject, "GetFolderClassifications", "C_QUERY_FILE_CLASSIFICATION");
		createCommandDescriptor(deUniversalFolderObject, "Exists",
				"C_QUERY_EXISTS");
		createCommandDescriptor(deUniversalFolderObject, "CreateNewFile",
				"C_CREATE_FILE");
		createCommandDescriptor(deUniversalFolderObject, "CreateNewFolder",
				"C_CREATE_FOLDER");
		createCommandDescriptor(deUniversalFolderObject, "GetOSType",
				"C_GET_OSTYPE");
		createCommandDescriptor(deUniversalFileObject, "GetOSType",
				"C_GET_OSTYPE");

		// create a download command descriptor and make it cancellable
		DataElement downloadDescriptor = createCommandDescriptor(
				deUniversalFileObject, "DownloadFile", C_DOWNLOAD_FILE);
		_dataStore.createReference(cancellable, downloadDescriptor,
				"abstracts", "abstracted by");
		_dataStore.createReference(cancellable, downloadDescriptor,
				"abstracts", "abstracted by");

		DataElement adownloadDescriptor = createCommandDescriptor(
				deUniversalArchiveFileObject, "DownloadFile", C_DOWNLOAD_FILE);
		_dataStore.createReference(cancellable, adownloadDescriptor,
				"abstracts", "abstracted by");
		_dataStore.createReference(cancellable, adownloadDescriptor,
				"abstracts", "abstracted by");
		
		createCommandDescriptor(tempnode, "SystemEncoding", C_SYSTEM_ENCODING);
		
		createCommandDescriptor(tempnode, "UnusedPort", C_QUERY_UNUSED_PORT);

		// command descriptor to retrieve package name for a class file
		createCommandDescriptor(deUniversalFileObject, "GetQualifiedClassName",
				"C_QUERY_CLASSNAME");

		// command descriptor to retrieve qualified class name for class file
		createCommandDescriptor(deUniversalFileObject, "GetFullClassName",
				C_QUERY_QUALIFIED_CLASSNAME);
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
				status.setAttribute(DE.A_SOURCE, FAILED);
				_dataStore.refresh(subject);
				return statusDone(status);
			}

			if (type.equals(UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR)) {
				deObj = _dataStore.find(subject, DE.A_NAME, subject.getName(),
						1);
				_dataStore.deleteObject(subject, deObj);
				status.setAttribute(DE.A_SOURCE, SUCCESS);
			} else if (type.equals(UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR)) {
				_dataStore.deleteObjects(subject);
				DataElement parent = subject.getParent();
				_dataStore.deleteObject(parent, subject);
				_dataStore.refresh(parent);
			}
		}

		_dataStore.refresh(subject);
		return statusDone(status);
	}

	public DataElement handleQueryAllArchive(DataElement subject, DataElement attributes, 
			DataElement status, boolean caseSensitive, boolean foldersOnly) {
		File fileobj = null;
		try {
			ArchiveHandlerManager mgr = ArchiveHandlerManager.getInstance();
			char separatorChar = File.separatorChar;
			if (ArchiveHandlerManager.isVirtual(subject
					.getAttribute(DE.A_VALUE))) {
				separatorChar = '/';
			}

			String path = subject.getAttribute(DE.A_VALUE) + separatorChar
					+ subject.getName();
			String rootPath = path;
			String virtualPath = "";

			VirtualChild[] children = null;

			if (subject.getType().equals(UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR)) {
				// it's an archive file (i.e. file.zip)
				fileobj = new File(rootPath);
				subject.setAttribute(DE.A_SOURCE, setProperties(fileobj, true));
				
				if (foldersOnly) {
					children = mgr.getFolderContents(fileobj, "");
				} else {
					children = mgr.getContents(fileobj, "");
				}
				
			} else if (subject.getType().equals(
					UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR)) {
				// it's a virtual folder (i.e. a folder within zip)
				// need to determine the associate File object
				AbsoluteVirtualPath avp = new AbsoluteVirtualPath(path);
				rootPath = avp.getContainingArchiveString();
				virtualPath = avp.getVirtualPart();
				fileobj = new File(rootPath);
			
				if (fileobj.exists()) {
				    
					if (foldersOnly) {
						children = mgr.getFolderContents(fileobj, virtualPath);
					} else {
						children = mgr.getContents(fileobj, virtualPath);
					}
					
					subject.setAttribute(DE.A_SOURCE, setProperties(mgr.getVirtualObject(virtualPath)));
					if (children == null || children.length == 0) {
						_dataStore.trace("problem with virtual:" + virtualPath);
					}
				} else {
					_dataStore.trace("problem with File:" + rootPath);
				}
			}
			createDataElement(_dataStore, subject, children, "*", rootPath, virtualPath);
			
			_dataStore.refresh(subject);
			
			FileClassifier clsfy = getFileClassifier(subject);
			clsfy.start();
			
			return statusDone(status);
		} catch (Exception e) {
			if (!(fileobj == null)) {
				try {
					(new FileReader(fileobj)).read();
				} catch (IOException ex) {
					status.setAttribute(DE.A_VALUE, FILEMSG_NO_PERMISSION);
					status.setAttribute(DE.A_SOURCE, FAILED);
					_dataStore.refresh(subject);
					return statusDone(status);
				}
			}
			status.setAttribute(DE.A_VALUE, FILEMSG_ARCHIVE_CORRUPTED);
			status.setAttribute(DE.A_SOURCE, FAILED);
			return statusDone(status);
		}
	}

//	private DataElement findExistingVirtual(DataElement subject, VirtualChild vchild) {
//		String name = vchild.name;
//		for (int i = 0; i < subject.getNestedSize(); i++) {
//			DataElement child = subject.get(i);
//			String deName = child.getName();
//			if (name.equals(deName)) {
//				if (vchild.isDirectory) {
//					if (child.getType().equals(
//							UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR)) {
//						return child;
//					}
//				} else {
//					if (child.getType().equals(
//							UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR)) {
//						return child;
//					}
//				}
//			}
//		}
//		return null;
//	}

	public ISystemArchiveHandler getArchiveHandlerFor(String archivePath) {
		File file = new File(archivePath);
		return _archiveHandlerManager.getRegisteredHandler(file);
	}

	public DataElement handleCreateVirtualFile(DataElement subject,
			DataElement status, String type) {

		AbsoluteVirtualPath vpath = null;
		if (type.equals(UNIVERSAL_FILTER_DESCRIPTOR)) {
			vpath = getAbsoluteVirtualPath(subject.getValue());
		} else {
			vpath = getAbsoluteVirtualPath(subject);
		}
		ISystemArchiveHandler handler = getArchiveHandlerFor(vpath
				.getContainingArchiveString());
		if (handler == null) {
			status.setAttribute(DE.A_SOURCE, FAILED);
			return statusDone(status);
		}
//		VirtualChild child = handler.getVirtualFile(vpath.getVirtualPart());
		handler.getVirtualFile(vpath.getVirtualPart());
		handler.createFile(vpath.getVirtualPart());

		status.setAttribute(DE.A_SOURCE, SUCCESS);
		if (type.equals(UNIVERSAL_FILTER_DESCRIPTOR)) {
			String fullName = subject.getValue();
			String name = fullName.substring(fullName
					.lastIndexOf(File.separatorChar) + 1, fullName.length());
			String path = fullName.substring(0, fullName
					.lastIndexOf(File.separatorChar));
			subject.setAttribute(DE.A_NAME, name);
			subject.setAttribute(DE.A_VALUE, path);
			subject.setAttribute(DE.A_TYPE, UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR);
		}
		_dataStore.refresh(subject);
		return statusDone(status);
	}

	public DataElement handleCreateVirtualFolder(DataElement subject,
			DataElement status, String type) {

		AbsoluteVirtualPath vpath = null;
		if (type.equals(UNIVERSAL_FILTER_DESCRIPTOR)) {
			vpath = getAbsoluteVirtualPath(subject.getValue());
		} else {
			vpath = getAbsoluteVirtualPath(subject);
		}
		ISystemArchiveHandler handler = getArchiveHandlerFor(vpath
				.getContainingArchiveString());
		if (handler == null) {
			status.setAttribute(DE.A_SOURCE, FAILED);
			return statusDone(status);
		}
//		VirtualChild child = handler.getVirtualFile(vpath.getVirtualPart());
		handler.getVirtualFile(vpath.getVirtualPart());
		handler.createFolder(vpath.getVirtualPart());

		status.setAttribute(DE.A_SOURCE, SUCCESS);
		if (type.equals(UNIVERSAL_FILTER_DESCRIPTOR)) {
			String fullName = subject.getValue();
			String name = fullName.substring(fullName
					.lastIndexOf(File.separatorChar) + 1, fullName.length());
			String path = fullName.substring(0, fullName
					.lastIndexOf(File.separatorChar));
			subject.setAttribute(DE.A_NAME, name);
			subject.setAttribute(DE.A_VALUE, path);
			subject
					.setAttribute(DE.A_TYPE,
							UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR);
		}
		_dataStore.refresh(subject);
		return statusDone(status);
	}

	private File getFileFor(DataElement element) {
		File result = null;
		String type = element.getType();
		if (type.equals(UNIVERSAL_FILTER_DESCRIPTOR)) {
			result = new File(element.getName());
		} else if (type.equals(UNIVERSAL_FILE_DESCRIPTOR)
				|| type.equals(UNIVERSAL_FOLDER_DESCRIPTOR)
				|| type.equals(UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR)) {
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
		
		if (targetType.equals(UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR) || targetType.equals(UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR)) {
			
		    // insert into an archive
			AbsoluteVirtualPath vpath = getAbsoluteVirtualPath(targetFolder);
			ISystemArchiveHandler handler = getArchiveHandlerFor(vpath.getContainingArchiveString());
			
			if (handler == null) {
				status.setAttribute(DE.A_SOURCE, FAILED);
				return statusDone(status);
			}

			File srcFile = null;
			
			if (srcType.equals(UNIVERSAL_FILE_DESCRIPTOR) || srcType.equals(UNIVERSAL_FOLDER_DESCRIPTOR)
					|| srcType.equals(UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR)) {
				
			    srcFile = getFileFor(sourceFile);
			}
			else if (srcType.equals(UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR) || srcType.equals(UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR)) {
				
			    AbsoluteVirtualPath svpath = getAbsoluteVirtualPath(sourceFile);
				ISystemArchiveHandler shandler = getArchiveHandlerFor(svpath.getContainingArchiveString());
				
				if (shandler == null) {
					status.setAttribute(DE.A_SOURCE, FAILED);
					return statusDone(status);
				}
				
				VirtualChild child = shandler.getVirtualFile(svpath.getVirtualPart());
				srcFile = child.getExtractedFile();
			}

			String virtualContainer = "";
			
			if (targetType.equals(UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR)) {
				virtualContainer = vpath.getVirtualPart();
			}

			boolean result = handler.add(srcFile, virtualContainer, newName);
			
			if (result) {
				status.setAttribute(DE.A_SOURCE, SUCCESS);
			}
			else {
				status.setAttribute(DE.A_SOURCE, FAILED);
			}
		}
		else if (srcType.equals(UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR) || srcType.equals(UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR)) {
			
		    // extract from an archive to folder
			AbsoluteVirtualPath svpath = getAbsoluteVirtualPath(sourceFile);
			ISystemArchiveHandler shandler = getArchiveHandlerFor(svpath.getContainingArchiveString());
			
			if (shandler == null) {
				status.setAttribute(DE.A_SOURCE, FAILED);
				return statusDone(status);
			}
			
			VirtualChild child = shandler.getVirtualFile(svpath.getVirtualPart());

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
			File srcFile = getFileFor(sourceFile);

			// regular copy
			boolean folderCopy = srcFile.isDirectory();
			String src = srcFile.getAbsolutePath();
			String tgt = tgtFolder.getAbsolutePath() + File.separatorChar + newName;
			File tgtFile = new File(tgt);
			
			if (tgtFile.exists() && tgtFile.isDirectory() && newName.equals(srcFile.getName()))
			{
			    tgt =  tgtFolder.getAbsolutePath();
			}
			
			// handle special characters in source and target strings 
			StringBuffer srcBuf = new StringBuffer(src);
			StringBuffer tgtBuf = new StringBuffer(tgt);
			handleSpecialChars(srcBuf);
			handleSpecialChars(tgtBuf);

			src = "\"" + srcBuf.toString() + "\"";
			tgt = "\"" + tgtBuf.toString() + "\"";

			doCopyCommand(src, tgt, folderCopy, status);
		}
		
		return statusDone(status);
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
		   
		if ((c == '$') || (c == '`') || (c == '"') || (c == '\\')) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public String getVersion()
	{
		return "7.0.0";
	}
}