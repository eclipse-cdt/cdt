/*******************************************************************************
 * Copyright (c) 2002, 2013 IBM Corporation and others.
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
 * Xuan Chen (IBM) - [189681] [dstore][linux] Refresh Folder in My Home messes up Refresh in Root
 * Xuan Chen (IBM) - [191280] [dstore] Expand fails for folder "/folk" with 3361 children
 * Kevin Doyle (IBM) - [195709] Windows Copying doesn't work when path contains space
 * Kevin Doyle (IBM) - [196211] DStore Move tries rename if that fails copy/delete
 * Xuan Chen (IBM) - [198046] [dstore] Cannot copy a folder into an archive file
 * Xuan Chen (IBM) - [191367] with supertransfer on, Drag & Drop Folder from DStore to DStore doesn't work
 * Martin Oberhuber (Wind River) - [199548] Avoid touching files on setReadOnly() if unnecessary
 * Kevin Doyle (IBM) - [191548]  Deleting Read-Only directory removes it from view and displays no error
 * Xuan Chen (IBM) - [202949] [archives] copy a folder from one connection to an archive file in a different connection does not work
 * Xuan Chen (IBM) - [160775] [api] rename (at least within a zip) blocks UI thread
 * David McKnight (IBM) - [196624] dstore miner IDs should be String constants rather than dynamic lookup
 * Xuan Chen (IBM) - [209827] Update DStore command implementation to enable cancelation of archive operations
 * Xuan Chen (IBM) - [194481] [dstore][Archive] Save Conflict After Renaming a File that is Open
 * David McKnight (IBM) - [209593] [api] add support for "file permissions" and "owner" properties for unix files
 * Johnson Ma (Wind River) - [195402] Add tar.gz archive support
 * Noriaki Takatsu (IBM)  - [220126] [dstore][api][breaking] Single process server for multiple clients
 * David McKnight     (IBM)   [224906] [dstore] changes for getting properties and doing exit due to single-process capability
 * David McKnight     (IBM)   [225507] [api][breaking] RSE dstore API leaks non-API types
 * Martin Oberhuber (Wind River) - [199854][api] Improve error reporting for archive handlers
 * David McKnight  (IBM)  - [226561] [apidoc] Add API markup to RSE Javadocs where extend / implement is allowed
 * David McKnight  (IBM)  - [244277] [dstore] NPE on file save from old client
 * David McKnight  (IBM)  - [246234] Change of file permissions changes the file owner
 * David McKnight  (IBM)  - [250168] handleCommand should not blindly set the status to "done"
 * David McKnight  (IBM)  - [251729][dstore] problems querying symbolic link folder
 * David McKnight  (IBM)  - [243495] [api] New: Allow file name search in Remote Search to not be case sensitive
 * David McKnight  (IBM)  - [283617] [dstore] UniversalFileSystemMiner.handleQueryGetRemoteObject does not return correct result when the queried file does not exist.
 * David McKnight  (IBM)  - [dstore] cancelable threads not removed fast enough from Hashmap, resulting in OOM
 * David McKnight   (IBM) - [371401] [dstore][multithread] avoid use of static variables - causes memory leak after disconnect
 * Noriaki Takatsu  (IBM) - [380562] [multithread][dstore] File Search is not canceled by the client UI on disconnect
 * David McKnight   (IBM)        - [390037] [dstore] Duplicated items in the System view
 * David McKnight   (IBM)        - [392012] [dstore] make server safer for delete operations
 * David McKnight   (IBM) - [414016] [dstore] new server audit log requirements
 *******************************************************************************/

package org.eclipse.rse.dstore.universal.miners;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.dstore.core.miners.Miner;
import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.dstore.core.model.DataStoreResources;
import org.eclipse.rse.internal.dstore.universal.miners.filesystem.ArchiveQueryThread;
import org.eclipse.rse.internal.dstore.universal.miners.filesystem.ClassFileParser;
import org.eclipse.rse.internal.dstore.universal.miners.filesystem.CopyBatchThread;
import org.eclipse.rse.internal.dstore.universal.miners.filesystem.CopySingleThread;
import org.eclipse.rse.internal.dstore.universal.miners.filesystem.CreateFileThread;
import org.eclipse.rse.internal.dstore.universal.miners.filesystem.CreateFolderThread;
import org.eclipse.rse.internal.dstore.universal.miners.filesystem.DeleteThread;
import org.eclipse.rse.internal.dstore.universal.miners.filesystem.FileClassifier;
import org.eclipse.rse.internal.dstore.universal.miners.filesystem.FileDescriptors;
import org.eclipse.rse.internal.dstore.universal.miners.filesystem.FileQueryThread;
import org.eclipse.rse.internal.dstore.universal.miners.filesystem.RenameThread;
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
import org.eclipse.rse.services.clientserver.archiveutils.SystemTgzHandler;
import org.eclipse.rse.services.clientserver.archiveutils.SystemZipHandler;
import org.eclipse.rse.services.clientserver.archiveutils.VirtualChild;
import org.eclipse.rse.services.clientserver.java.ClassFileUtil;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;

/**
 * This miner allows for remote file browsing and management.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class UniversalFileSystemMiner extends Miner {

	private DataElement deUFSnode;

	private DataElement deUFSuploadlog;

	protected String filterString = "*"; //$NON-NLS-1$

	protected ArchiveHandlerManager _archiveHandlerManager;

	protected boolean showHidden = false;


	public static final String CLASSNAME = "UniversalFileSystemMiner"; //$NON-NLS-1$

	protected HashMap _cancellableThreads;
	private FileDescriptors _fileDescriptors;
	
	private static final int PERMISSION_OWNER = 0;
	private static final int PERMISSION_GROUP = 1; 
	private static final int PERMISSION_BITS = 2;
	private static final int PERMISSION_ALL = 3;

	private boolean _isWindows = false;

	public UniversalFileSystemMiner() {
		_cancellableThreads = new HashMap();
		_isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows"); //$NON-NLS-1$ //$NON-NLS-2$
		_archiveHandlerManager = ArchiveHandlerManager.getInstance();
		_archiveHandlerManager.setRegisteredHandler("zip", SystemZipHandler.class); //$NON-NLS-1$
		_archiveHandlerManager.setRegisteredHandler("jar", SystemJarHandler.class); //$NON-NLS-1$
		_archiveHandlerManager.setRegisteredHandler("tar.gz", SystemTgzHandler.class); //$NON-NLS-1$
		_archiveHandlerManager.setRegisteredHandler("tgz", SystemTgzHandler.class); //$NON-NLS-1$
		_archiveHandlerManager.setRegisteredHandler("tar", SystemTarHandler.class); //$NON-NLS-1$
		
		_fileDescriptors = new FileDescriptors();
	}

	/**
	 * @see Miner#handleCommand(DataElement)
	 */
	public DataElement handleCommand(DataElement theElement) throws SystemMessageException {
		String name = getCommandName(theElement);


		DataElement status = getCommandStatus(theElement);
		DataElement subject = getCommandArgument(theElement, 0);

		UniversalServerUtilities.logInfo(getName(), name + ":" + subject, _dataStore); //$NON-NLS-1$

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
		} else if (IUniversalDataStoreConstants.C_QUERY_FILE_PERMISSIONS.equals(name)) {
				return handleQueryFilePermissions(subject, status);
		} else if (IUniversalDataStoreConstants.C_SET_FILE_PERMISSIONS.equals(name)) {
				DataElement newPermissions = getCommandArgument(theElement, 1);
                return handleSetFilePermissions(subject, newPermissions, status);
		} else {
			UniversalServerUtilities.logError(CLASSNAME,
					"Invalid query to handlecommand", null, _dataStore); //$NON-NLS-1$
		}
		//return statusDone(status);
		return status; // can't assume operation is done since it could be done via a thread
	}

	private DataElement handleCopyBatch(DataElement targetFolder, DataElement theElement, DataElement status)
	{

		CopyBatchThread copyBatchThread = new CopyBatchThread(targetFolder, theElement, this, _isWindows, status);
		copyBatchThread.start();

		updateCancellableThreads(status.getParent(), copyBatchThread);

		return status;
	}





	/**
	 * Method to do a search.
	 */
	public DataElement handleSearch(DataElement theElement, DataElement status,
			String queryType, boolean systemFileNamesCaseSensitive) {
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
			UniversalServerUtilities.logError(CLASSNAME, "Invalid query type to handleSearch", null, _dataStore); //$NON-NLS-1$
			return statusDone(status);
		}
		//If the subject is a virtual folder, we could not just use check file.exists() to determine if we need
		//to continue process this request or not.
		boolean continueSearch = true;
		if (!queryType.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR) && !fileobj.exists())
		{
			continueSearch = false;
		}
		if (continueSearch) {
			DataElement arg1 = getCommandArgument(theElement, 1);
			DataElement arg2 = getCommandArgument(theElement, 2);
			DataElement arg3 = getCommandArgument(theElement, 3);
			DataElement arg4 = getCommandArgument(theElement, 4);

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

			boolean isFileNamesCaseSensitive = true;
			if (arg4 != null && arg4.getType().equals("file.name.case.sensitive")){ //$NON-NLS-1$
				isFileNamesCaseSensitive = Boolean.valueOf(arg4.getName()).booleanValue();
			}
			
			SystemSearchString searchString = new SystemSearchString(
					textString, isCaseSensitive, isTextRegex, fileNamesString, isFileNamesCaseSensitive,
					isFileNamesRegex, isIncludeArchives, isIncludeSubfolders, classification);

			UniversalSearchHandler searchThread = new UniversalSearchHandler(
					_dataStore, this, searchString, !_isWindows, fileobj,
					status);

			searchThread.start();

			updateCancellableThreads(status.getParent(), searchThread);
			//return status;
		}

		return status; // search is in the thread, so it's not done yet
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
			throws SystemMessageException
	{
		boolean isArchive = false;
		String fullName = subject.getValue();

		if (queryType.equals(IUniversalDataStoreConstants.UNIVERSAL_FILTER_DESCRIPTOR))
		{
			//check if it is a archive file
			if (ArchiveHandlerManager.getInstance().isArchive(new File(fullName)))
			{
				isArchive = true;
			}
			else
			{
				isArchive = ArchiveHandlerManager.isVirtual(fullName);
			}
		}
		else if (queryType.equals(IUniversalDataStoreConstants.UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR)
				|| queryType.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR)
				|| queryType.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR)) {
			isArchive = true;
		}
		if (isArchive)
		{
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
					"Invalid query type to handleQueryAll", null, _dataStore); //$NON-NLS-1$
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
		FileQueryThread queryThread = new FileQueryThread(subject, fileobj, queryType, filter, caseSensitive, inclusion, showHidden, _isWindows, status, _fileDescriptors);
		queryThread.start();

		updateCancellableThreads(status.getParent(), queryThread);
	}

	/**
	 * @since 3.2
	 */
	public void updateCancellableThreads(DataElement command, ICancellableHandler thread)
	{
		//First Check to make sure that there are no "zombie" threads
		List threadsToRemove = new ArrayList();
		Iterator iter = _cancellableThreads.keySet().iterator();
		try
		{
			while (iter.hasNext())
			{
				DataElement threadElement = (DataElement) iter.next();
				ICancellableHandler theThread = (ICancellableHandler) _cancellableThreads.get(threadElement);
				if ((theThread == null) ||
						theThread.isDone() || theThread.isCancelled())
				{
					threadsToRemove.add(threadElement);
				}
			}
			if (!threadsToRemove.isEmpty()){
				for (int i = 0; i < threadsToRemove.size(); i++){
					_cancellableThreads.remove(threadsToRemove.get(i));
				}
			}
		}
		catch (Exception e)
		{
			_dataStore.trace(e);
		}
		// save find thread in hashmap for retrieval during cancel
		if (!thread.isDone() && !thread.isCancelled()){
			_cancellableThreads.put(command, thread);
		}
	}
	

	  /**
		    * Method to list the files for a given filter.
		    */
	public DataElement handleQueryFiles(DataElement subject, DataElement attributes,
			DataElement status, String queryType, boolean caseSensitive)
			throws SystemMessageException {

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
					"Invalid query type to handleQueryFiles", null, _dataStore); //$NON-NLS-1$


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
			DataElement status, String queryType, boolean caseSensitive)
			throws SystemMessageException {
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
					"Invalid query type to handleQueryFolders", null, _dataStore); //$NON-NLS-1$

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
	public DataElement handleQueryRoots(DataElement subject, DataElement status) throws SystemMessageException {
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
		// first make sure this is a valid object to delete
		String type = subject.getType();	
		if (IUniversalDataStoreConstants.UNIVERSAL_FOLDER_DESCRIPTOR.equals(type) ||
			IUniversalDataStoreConstants.UNIVERSAL_FILE_DESCRIPTOR.equals(type) ||
			IUniversalDataStoreConstants.UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR.equals(type) ||
			IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR.equals(type) ||
			IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR.equals(type) ||
			IUniversalDataStoreConstants.UNIVERSAL_FILTER_DESCRIPTOR.equals(type)){
			
			DeleteThread deleteThread = new DeleteThread(subject,  this, _dataStore, false, status);
			deleteThread.start();

			updateCancellableThreads(status.getParent(), deleteThread);
		}
		else {
			UniversalServerUtilities.logWarning(getName(), "illegal deletion type: " + type, _dataStore); //$NON-NLS-1$
			statusCancelled(status);
		}

		return status;
	}

	private DataElement handleDeleteBatch(DataElement theElement, DataElement status)
	{
		DeleteThread deleteThread = new DeleteThread(theElement,  this, _dataStore, true, status);
		deleteThread.start();

		updateCancellableThreads(status.getParent(), deleteThread);
		return status;
	}
	

	/**
	 * Method to Rename a file or folder.
	 */
	public DataElement handleRename(DataElement subject, DataElement status) {

		RenameThread renameThread = new RenameThread(subject,  this, _dataStore, status);
		renameThread.start();

		updateCancellableThreads(status.getParent(), renameThread);

		return status;
	}

	/**
	 * Method to create a new file.
	 */
	public DataElement handleCreateFile(DataElement subject,
			DataElement status, String queryType) {

		CreateFileThread createFileThread = new CreateFileThread(subject,  queryType, this, _dataStore, status);
		createFileThread.start();

		updateCancellableThreads(status.getParent(), createFileThread);

		return status;
	}

	/**
	 * Method to create a new folder.
	 */
	public DataElement handleCreateFolder(DataElement subject,
			DataElement status, String queryType) {
		CreateFolderThread createFolderThread = new CreateFolderThread(subject,  queryType, this, _dataStore, status);
		createFolderThread.start();

		updateCancellableThreads(status.getParent(), createFolderThread);

		return status;
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
			        String[] auditData = new String[] {"SET-READONLY", filename.getAbsolutePath(), null, null}; //$NON-NLS-1$
			     	UniversalServerUtilities.logAudit(auditData, _dataStore);

					done = filename.setReadOnly();
				}
				else
				{
			        String[] auditData = new String[] {"SET-READWRITE", filename.getAbsolutePath(), null, null}; //$NON-NLS-1$
			     	UniversalServerUtilities.logAudit(auditData, _dataStore);
			     	
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
						"handleSetreadOnly", e, _dataStore); //$NON-NLS-1$
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

		        String[] auditData = new String[] {"SET-LAST-MODIFIED", filename.getAbsolutePath(), null, null}; //$NON-NLS-1$
		     	UniversalServerUtilities.logAudit(auditData, _dataStore);
				

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
						"handleSetLastModified", e, _dataStore); //$NON-NLS-1$
			}
		}
		_dataStore.refresh(subject);
		return statusDone(status);
	}

	/**
	 * Method to Retrieve properties of the file or folder.
	 */
	protected DataElement handleQueryBasicProperty(DataElement subject,
			DataElement status) throws SystemMessageException {
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

	/**
	 * Method to query existence of the file or folder.
	 */
	protected DataElement handleQueryExists(DataElement subject,
			DataElement status, String queryType) throws SystemMessageException {

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
			VirtualChild child = handler.getVirtualFile(vpath.getVirtualPart(), null);
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
			DataElement status, String queryType) throws SystemMessageException {
		
		File fileobj = null;
		boolean isVirtual = false;
		boolean isFilter = false;
		String fullName = subject.getValue();
		if (queryType.equals(IUniversalDataStoreConstants.UNIVERSAL_FILTER_DESCRIPTOR))
		{
			isFilter = true;
			isVirtual = ArchiveHandlerManager.isVirtual(fullName);
			String filterValue = subject.getValue();
			// . translates to home dir
			if (filterValue.equals("."))  //$NON-NLS-1$
			{
				if (_dataStore.getClient() != null){
					filterValue = _dataStore.getClient().getProperty("user.home"); //$NON-NLS-1$
				}
				else {
					filterValue = System.getProperty("user.home"); //$NON-NLS-1$
				}				
				try {
					// "." needs canonical file
					fileobj = new File(filterValue).getCanonicalFile();
				}
				catch (Exception e){
					fileobj = new File(filterValue);
				}
				
				subject.setAttribute(DE.A_VALUE, filterValue);
			}
			else if (!isVirtual){
				fileobj = new File(filterValue);
			}
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
		else if (queryType.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR) || queryType.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR))
		{
			isVirtual = true;
		}
		else {
			UniversalServerUtilities.logError(CLASSNAME,
					"Invalid query type to handleQueryGetRemoteObject", null, _dataStore); //$NON-NLS-1$
			return statusDone(status);
		}

		if (!isVirtual && fileobj != null && fileobj.exists()) {
			
			String oldProperties = subject.getAttribute(DE.A_SOURCE);
			boolean isSymlink = oldProperties != null && (oldProperties.indexOf("symbolic link") > 0);//$NON-NLS-1$
			fullName = fileobj.getAbsolutePath();
			
			/* should not need canonical path here.  It causes bug 251729
			{
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
			}
			 */
			
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

			String properties = setProperties(fileobj);
			
			// if this is a symbolic link or a file, reclassify
			if (fileobj.isFile() || isSymlink){								 //$NON-NLS-1$
				// classify the file too
				FileClassifier classifier = new FileClassifier(subject);
				subject.setAttribute(DE.A_SOURCE, properties + "|" + classifier.classifyFile(fileobj)); //$NON-NLS-1$
			}
			else {
				subject.setAttribute(DE.A_SOURCE, properties + "|" + "directory");  //$NON-NLS-1$//$NON-NLS-2$
			}

			status.setAttribute(DE.A_SOURCE, IServiceConstants.SUCCESS);

		} else if (isVirtual) {
			try {
				String goodFullPath = ArchiveHandlerManager
						.cleanUpVirtualPath(fullName);
				String goodFullName = null;
				if (isFilter)
				{
					goodFullName = goodFullPath;
				}
				else
				{
					goodFullName = goodFullPath + "/" + subject.getName();  //$NON-NLS-1$
				}
				AbsoluteVirtualPath avp = new AbsoluteVirtualPath(goodFullName);
				VirtualChild child = _archiveHandlerManager
						.getVirtualObject(goodFullName);
				if (child.exists()) {

					if (child.isDirectory) {
						subject.setAttribute(DE.A_TYPE,
								IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR);
						subject.setAttribute(DE.A_NAME, child.name);

						subject.setAttribute(DE.A_VALUE, avp
									.getContainingArchiveString()
									+ ArchiveHandlerManager.VIRTUAL_SEPARATOR
									+ child.path);


					} else {
						subject.setAttribute(DE.A_TYPE,
								IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR);
						String name = child.name;
						String path = avp.getContainingArchiveString();

							path = path
									+ ArchiveHandlerManager.VIRTUAL_SEPARATOR
									+ child.path;


						subject.setAttribute(DE.A_NAME, name);
						subject.setAttribute(DE.A_VALUE, path);
					}

					subject.setAttribute(DE.A_SOURCE, setProperties(child));
					status.setAttribute(DE.A_SOURCE, IServiceConstants.SUCCESS);
				} else {
					UniversalServerUtilities.logWarning(CLASSNAME,
							"object does not exist", _dataStore); //$NON-NLS-1$
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
			
			if (!subject.getName().equals(subject.getValue())){
				// need to change this back into full path format
				subject.setAttribute(DE.A_NAME, fileobj.getAbsolutePath());
				subject.setAttribute(DE.A_VALUE, subject.getAttribute(DE.A_NAME));
			}
			
			status.setAttribute(DE.A_SOURCE, IServiceConstants.FAILED_WITH_DOES_NOT_EXIST);
		}

		_dataStore.refresh(subject);
		_dataStore.disconnectObject(subject);
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
	 * Method to obtain the classification string of file or folder.
	 */
	protected String getClassificationString(String s) {

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
			UniversalServerUtilities.logError(CLASSNAME, "Can not get unused port", e, _dataStore); //$NON-NLS-1$
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

		_dataStore.registerByteStreamHandler(universalHandler);

		_dataStore.refresh(_minerData);
		_dataStore.refresh(deUFSuploadlog);
		
		
		// for bug 244277
		// need backward compatibility with RSE 7.1.*
		// 1) create a miner element
		DataElement minerRoot = _dataStore.getMinerRoot();
		String oldName = "com.ibm.etools.systems.universal.miners.UniversalFileSystemMiner"; //$NON-NLS-1$
		DataElement oldMinerElement   = _dataStore.createObject(minerRoot, DataStoreResources.model_miner, oldName, oldName);
		oldMinerElement.setAttribute(DE.A_VALUE, "UniveralFileSystemMiner"); //$NON-NLS-1$
		oldMinerElement.setAttribute(DE.A_SOURCE, "7.1.0"); //$NON-NLS-1$
		
		DataElement oldMinerData      = _dataStore.createObject(oldMinerElement, DataStoreResources.model_data, DataStoreResources.model_Data, oldName);
		
		// 2) create a miner data
		DataElement oldDeUFSnode = _dataStore.createObject(oldMinerData, IUniversalDataStoreConstants.UNIVERSAL_NODE_DESCRIPTOR, "universal.node"); //$NON-NLS-1$

		DataElement oldDeUFSuploadlog = _dataStore.createObject(oldDeUFSnode, IUniversalDataStoreConstants.UNIVERSAL_NODE_DESCRIPTOR, "universal.uploadlog"); //$NON-NLS-1$

		class OldUniversalByteStreamHandler extends UniversalByteStreamHandler
		{
			public OldUniversalByteStreamHandler(DataStore dataStore, DataElement log){
				super(dataStore, log);
			}
			public String getId(){
				return "com.ibm.etools.systems.universal.miners.UniversalByteStreamHandler"; //$NON-NLS-1$
			}
		}
		
		OldUniversalByteStreamHandler olduniversalHandler = new OldUniversalByteStreamHandler(_dataStore, oldDeUFSuploadlog);
		
		_dataStore.registerByteStreamHandler(olduniversalHandler);
		
		_dataStore.refresh(minerRoot);
		_dataStore.refresh(oldMinerData);
		
	}

	public void finish() {
		try {
			if (_cancellableThreads != null) {
				Set keys = _cancellableThreads.keySet();
				Iterator iteratorKeys = keys.iterator();
				while (iteratorKeys.hasNext()) {
					Object key = iteratorKeys.next();
					ICancellableHandler thread = (ICancellableHandler) _cancellableThreads.get(key);
					if (thread != null) {
						if (!thread.isDone()) {
							thread.cancel();
						}
					}
				}
				
				_cancellableThreads.clear();
			}
		}
		catch(Throwable e) {
			e.printStackTrace();
		}
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
					null, _dataStore);

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
		_fileDescriptors._deUniversalFileObject = createObjectDescriptor(schemaRoot,
				IUniversalDataStoreConstants.UNIVERSAL_FILE_DESCRIPTOR);
		_fileDescriptors._deUniversalFolderObject = createObjectDescriptor(schemaRoot,
				IUniversalDataStoreConstants.UNIVERSAL_FOLDER_DESCRIPTOR);
		_fileDescriptors._deUniversalArchiveFileObject = createObjectDescriptor(
				schemaRoot, IUniversalDataStoreConstants.UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR);
		_fileDescriptors._deUniversalVirtualFileObject = createObjectDescriptor(
				schemaRoot, IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR);
		_fileDescriptors._deUniversalVirtualFolderObject = createObjectDescriptor(
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


		DataElement queryAllDescriptor = createCommandDescriptor(_fileDescriptors._deUniversalFolderObject, "Filter", IUniversalDataStoreConstants.C_QUERY_VIEW_ALL); //$NON-NLS-1$
		_dataStore.createReference(cancellable, queryAllDescriptor, DataStoreResources.model_abstracts, DataStoreResources.model_abstracted_by);

		DataElement queryFilesDescriptor = createCommandDescriptor(_fileDescriptors._deUniversalFolderObject, "Filter", IUniversalDataStoreConstants.C_QUERY_VIEW_FILES); //$NON-NLS-1$
		_dataStore.createReference(cancellable, queryFilesDescriptor, DataStoreResources.model_abstracts, DataStoreResources.model_abstracted_by);

		DataElement queryFolderDescriptor = createCommandDescriptor(_fileDescriptors._deUniversalFolderObject, "Filter", IUniversalDataStoreConstants.C_QUERY_VIEW_FOLDERS); //$NON-NLS-1$
		_dataStore.createReference(cancellable, queryFolderDescriptor, DataStoreResources.model_abstracts, DataStoreResources.model_abstracted_by);


		DataElement queryAllArchiveDescriptor = createCommandDescriptor(_fileDescriptors._deUniversalArchiveFileObject, "Filter", IUniversalDataStoreConstants.C_QUERY_VIEW_ALL); //$NON-NLS-1$
		_dataStore.createReference(cancellable, queryAllArchiveDescriptor, DataStoreResources.model_abstracts, DataStoreResources.model_abstracted_by);

		DataElement queryFilesArchiveDescriptor = createCommandDescriptor(_fileDescriptors._deUniversalArchiveFileObject, "Filter", IUniversalDataStoreConstants.C_QUERY_VIEW_FILES); //$NON-NLS-1$
		_dataStore.createReference(cancellable, queryFilesArchiveDescriptor, DataStoreResources.model_abstracts, DataStoreResources.model_abstracted_by);

		DataElement queryFolderArchiveDescriptor = createCommandDescriptor(_fileDescriptors._deUniversalArchiveFileObject, "Filter", IUniversalDataStoreConstants.C_QUERY_VIEW_FOLDERS); //$NON-NLS-1$
		_dataStore.createReference(cancellable, queryFolderArchiveDescriptor, DataStoreResources.model_abstracts, DataStoreResources.model_abstracted_by);

		createCommandDescriptor(UniversalFilter, "GetOSType", IUniversalDataStoreConstants.C_GET_OSTYPE); //$NON-NLS-1$
		createCommandDescriptor(UniversalFilter, "Exists", IUniversalDataStoreConstants.C_QUERY_EXISTS); //$NON-NLS-1$
		createCommandDescriptor(UniversalFilter, "GetRemoteObject", IUniversalDataStoreConstants.C_QUERY_GET_REMOTE_OBJECT); //$NON-NLS-1$
		DataElement createNewFileFromFilterDescriptor = createCommandDescriptor(UniversalFilter, "CreateNewFile", IUniversalDataStoreConstants.C_CREATE_FILE); //$NON-NLS-1$
		_dataStore.createReference(cancellable, createNewFileFromFilterDescriptor, DataStoreResources.model_abstracts, DataStoreResources.model_abstracted_by);
		DataElement createNewFolderFromFilterDescriptor = createCommandDescriptor(UniversalFilter, "CreateNewFolder", IUniversalDataStoreConstants.C_CREATE_FOLDER); //$NON-NLS-1$
		_dataStore.createReference(cancellable, createNewFolderFromFilterDescriptor, DataStoreResources.model_abstracts, DataStoreResources.model_abstracted_by);
		createCommandDescriptor(UniversalFilter, "SetLastModified", IUniversalDataStoreConstants.C_SET_LASTMODIFIED); //$NON-NLS-1$


		_dataStore.createReference(_fileDescriptors._deUniversalFileObject,
				_fileDescriptors._deUniversalArchiveFileObject, DataStoreResources.model_abstracts, DataStoreResources.model_abstracted_by);
		_dataStore.createReference(_fileDescriptors._deUniversalFolderObject,
				_fileDescriptors._deUniversalArchiveFileObject, DataStoreResources.model_abstracts, DataStoreResources.model_abstracted_by);
		_dataStore.createReference(_fileDescriptors._deUniversalFileObject,
				_fileDescriptors._deUniversalVirtualFileObject, DataStoreResources.model_abstracts, DataStoreResources.model_abstracted_by);
		_dataStore.createReference(_fileDescriptors._deUniversalFolderObject,
				_fileDescriptors._deUniversalVirtualFolderObject, DataStoreResources.model_abstracts, DataStoreResources.model_abstracted_by);

		// create the search descriptor and make it cancelable
		DataElement searchDescriptor = createCommandDescriptor(_fileDescriptors._deUniversalFolderObject, "Search", IUniversalDataStoreConstants.C_SEARCH); //$NON-NLS-1$
		_dataStore.createReference(cancellable, searchDescriptor, DataStoreResources.model_abstracts, DataStoreResources.model_abstracted_by);


		createCommandDescriptor(_fileDescriptors._deUniversalFolderObject, "GetAdvanceProperty", IUniversalDataStoreConstants.C_QUERY_ADVANCE_PROPERTY); //$NON-NLS-1$
		createCommandDescriptor(tempnode, "Filter", IUniversalDataStoreConstants.C_CREATE_TEMP); //$NON-NLS-1$
		//create deleteDescriptor and make it cancelable
		DataElement deleteFileDescriptor = createCommandDescriptor(_fileDescriptors._deUniversalFileObject, "Delete", IUniversalDataStoreConstants.C_DELETE); //$NON-NLS-1$
		_dataStore.createReference(cancellable, deleteFileDescriptor, DataStoreResources.model_abstracts, DataStoreResources.model_abstracted_by);
		//create deleteBatchDescriptor and make it cancelable
		DataElement deleteBatchFileDescriptor = createCommandDescriptor(_fileDescriptors._deUniversalFileObject, "DeleteBatch", IUniversalDataStoreConstants.C_DELETE_BATCH); //$NON-NLS-1$
		_dataStore.createReference(cancellable, deleteBatchFileDescriptor, DataStoreResources.model_abstracts, DataStoreResources.model_abstracted_by);
		//create createNewFileDescriptor and make it cancelable
		DataElement createNewFileDescriptor = createCommandDescriptor(_fileDescriptors._deUniversalFileObject, "CreateNewFile", IUniversalDataStoreConstants.C_CREATE_FILE); //$NON-NLS-1$
		_dataStore.createReference(cancellable, createNewFileDescriptor, DataStoreResources.model_abstracts, DataStoreResources.model_abstracted_by);
		//create createNewFolderDescriptor and make it cancelable
		DataElement createNewFolderDescriptor = createCommandDescriptor(_fileDescriptors._deUniversalFileObject, "CreateNewFolder", IUniversalDataStoreConstants.C_CREATE_FOLDER); //$NON-NLS-1$
		_dataStore.createReference(cancellable, createNewFolderDescriptor, DataStoreResources.model_abstracts, DataStoreResources.model_abstracted_by);
		//create renameDescriptor and make it cancelable
		DataElement renameFileDescriptor = createCommandDescriptor(_fileDescriptors._deUniversalFileObject, "Rename", IUniversalDataStoreConstants.C_RENAME); //$NON-NLS-1$
		_dataStore.createReference(cancellable, renameFileDescriptor, DataStoreResources.model_abstracts, DataStoreResources.model_abstracted_by);
		createCommandDescriptor(_fileDescriptors._deUniversalFileObject, "SetReadOnly", IUniversalDataStoreConstants.C_SET_READONLY); //$NON-NLS-1$
		createCommandDescriptor(_fileDescriptors._deUniversalFileObject, "SetLastModified", IUniversalDataStoreConstants.C_SET_LASTMODIFIED); //$NON-NLS-1$
		createCommandDescriptor(_fileDescriptors._deUniversalFileObject, "GetAdvanceProperty", IUniversalDataStoreConstants.C_QUERY_ADVANCE_PROPERTY); //$NON-NLS-1$
		createCommandDescriptor(_fileDescriptors._deUniversalFileObject, "GetBasicProperty", IUniversalDataStoreConstants.C_QUERY_BASIC_PROPERTY); //$NON-NLS-1$

		createCommandDescriptor(_fileDescriptors._deUniversalFileObject, "GetcanWriteProperty", IUniversalDataStoreConstants.C_QUERY_CAN_WRITE_PROPERTY); //$NON-NLS-1$
		createCommandDescriptor(_fileDescriptors._deUniversalFileObject, "Exists", IUniversalDataStoreConstants.C_QUERY_EXISTS); //$NON-NLS-1$

		//create deleteDescriptor and make it cancelable
		DataElement deleteFolderDescriptor = createCommandDescriptor(_fileDescriptors._deUniversalFolderObject, "Delete", IUniversalDataStoreConstants.C_DELETE); //$NON-NLS-1$
		_dataStore.createReference(cancellable, deleteFolderDescriptor, DataStoreResources.model_abstracts, DataStoreResources.model_abstracted_by);
		//create deleteBatchDescriptor and make it cancelable
		DataElement deleteBatchFolderDescriptor = createCommandDescriptor(_fileDescriptors._deUniversalFolderObject, "DeleteBatch", IUniversalDataStoreConstants.C_DELETE_BATCH); //$NON-NLS-1$
		_dataStore.createReference(cancellable, deleteBatchFolderDescriptor, DataStoreResources.model_abstracts, DataStoreResources.model_abstracted_by);
		//create renameDescriptor and make it cancelable
		DataElement renameFolderDescriptor = createCommandDescriptor(_fileDescriptors._deUniversalFolderObject, "Rename", IUniversalDataStoreConstants.C_RENAME); //$NON-NLS-1$
		_dataStore.createReference(cancellable, renameFolderDescriptor, DataStoreResources.model_abstracts, DataStoreResources.model_abstracted_by);
		//create copyDescriptor and make it cancelable
		DataElement copyFolderDescriptor = createCommandDescriptor(_fileDescriptors._deUniversalFolderObject, "Copy", IUniversalDataStoreConstants.C_COPY); //$NON-NLS-1$
		_dataStore.createReference(cancellable, copyFolderDescriptor, DataStoreResources.model_abstracts, DataStoreResources.model_abstracted_by);
		//create copyFolderBatchDescriptor and make it cancelable
		DataElement copyBatchFolderDescriptor = createCommandDescriptor(_fileDescriptors._deUniversalFolderObject, "CopyBatch", IUniversalDataStoreConstants.C_COPY_BATCH); //$NON-NLS-1$
		_dataStore.createReference(cancellable, copyBatchFolderDescriptor, DataStoreResources.model_abstracts, DataStoreResources.model_abstracted_by);
		createCommandDescriptor(_fileDescriptors._deUniversalFolderObject, "SetReadOnly", IUniversalDataStoreConstants.C_SET_READONLY); //$NON-NLS-1$
		createCommandDescriptor(_fileDescriptors._deUniversalFolderObject, "SetLastModified", IUniversalDataStoreConstants.C_SET_LASTMODIFIED); //$NON-NLS-1$
		createCommandDescriptor(_fileDescriptors._deUniversalFolderObject, "GetBasicProperty", IUniversalDataStoreConstants.C_QUERY_BASIC_PROPERTY); //$NON-NLS-1$
		createCommandDescriptor(_fileDescriptors._deUniversalFolderObject, "GetcanWriteProperty", IUniversalDataStoreConstants.C_QUERY_CAN_WRITE_PROPERTY); //$NON-NLS-1$

		createCommandDescriptor(_fileDescriptors._deUniversalFolderObject, "Exists", IUniversalDataStoreConstants.C_QUERY_EXISTS); //$NON-NLS-1$
		//create createFolderDescriptor and make it cancelable
		DataElement createNewFileInFolderDescriptor = createCommandDescriptor(_fileDescriptors._deUniversalFolderObject, "CreateNewFile", IUniversalDataStoreConstants.C_CREATE_FILE); //$NON-NLS-1$
		_dataStore.createReference(cancellable, createNewFileInFolderDescriptor, DataStoreResources.model_abstracts, DataStoreResources.model_abstracted_by);
		//create createFolderDescriptor and make it cancelable
		DataElement createNewFolderInFolderDescriptor = createCommandDescriptor(_fileDescriptors._deUniversalFolderObject, "CreateNewFolder", IUniversalDataStoreConstants.C_CREATE_FOLDER); //$NON-NLS-1$
		_dataStore.createReference(cancellable, createNewFolderInFolderDescriptor, DataStoreResources.model_abstracts, DataStoreResources.model_abstracted_by);
		createCommandDescriptor(_fileDescriptors._deUniversalFolderObject, "GetOSType", IUniversalDataStoreConstants.C_GET_OSTYPE); //$NON-NLS-1$
		createCommandDescriptor(_fileDescriptors._deUniversalFileObject, "GetOSType", IUniversalDataStoreConstants.C_GET_OSTYPE); //$NON-NLS-1$
        //make sure C_QUERY_GET_REMOTE_OBJECT command also available for file and folder objects
		createCommandDescriptor(_fileDescriptors._deUniversalFolderObject, "GetRemoteObject", IUniversalDataStoreConstants.C_QUERY_GET_REMOTE_OBJECT); //$NON-NLS-1$
		createCommandDescriptor(_fileDescriptors._deUniversalFileObject, "GetRemoteObject", IUniversalDataStoreConstants.C_QUERY_GET_REMOTE_OBJECT); //$NON-NLS-1$
		createCommandDescriptor(_fileDescriptors._deUniversalArchiveFileObject, "GetRemoteObject", IUniversalDataStoreConstants.C_QUERY_GET_REMOTE_OBJECT); //$NON-NLS-1$
		createCommandDescriptor(_fileDescriptors._deUniversalVirtualFileObject, "GetRemoteObject", IUniversalDataStoreConstants.C_QUERY_GET_REMOTE_OBJECT); //$NON-NLS-1$
		createCommandDescriptor(_fileDescriptors._deUniversalVirtualFolderObject, "GetRemoteObject", IUniversalDataStoreConstants.C_QUERY_GET_REMOTE_OBJECT); //$NON-NLS-1$

		// create a download command descriptor and make it cancelable
		DataElement downloadDescriptor = createCommandDescriptor(
				_fileDescriptors._deUniversalFileObject, "DownloadFile", IUniversalDataStoreConstants.C_DOWNLOAD_FILE); //$NON-NLS-1$
		_dataStore.createReference(cancellable, downloadDescriptor,
				DataStoreResources.model_abstracts, DataStoreResources.model_abstracted_by);


		DataElement adownloadDescriptor = createCommandDescriptor(
				_fileDescriptors._deUniversalArchiveFileObject, "DownloadFile", IUniversalDataStoreConstants.C_DOWNLOAD_FILE); //$NON-NLS-1$
		_dataStore.createReference(cancellable, adownloadDescriptor,
				DataStoreResources.model_abstracts, DataStoreResources.model_abstracted_by);


		createCommandDescriptor(tempnode, "SystemEncoding", IUniversalDataStoreConstants.C_SYSTEM_ENCODING); //$NON-NLS-1$

		createCommandDescriptor(tempnode, "UnusedPort", IUniversalDataStoreConstants.C_QUERY_UNUSED_PORT); //$NON-NLS-1$

		// command descriptor to retrieve package name for a class file
		createCommandDescriptor(_fileDescriptors._deUniversalFileObject, "GetQualifiedClassName", IUniversalDataStoreConstants.C_QUERY_CLASSNAME); //$NON-NLS-1$

		// command descriptor to retrieve qualified class name for class file
		createCommandDescriptor(_fileDescriptors._deUniversalFileObject, "GetFullClassName", //$NON-NLS-1$
				IUniversalDataStoreConstants.C_QUERY_QUALIFIED_CLASSNAME);


		// permissions and ownership not supported on windows
		if (!_isWindows) {
			// descriptors for permissions
			createCommandDescriptor(UniversalFilter, "GetPermissions", IUniversalDataStoreConstants.C_QUERY_FILE_PERMISSIONS); //$NON-NLS-1$
			createCommandDescriptor(_fileDescriptors._deUniversalFolderObject, "GetPermissions", IUniversalDataStoreConstants.C_QUERY_FILE_PERMISSIONS); //$NON-NLS-1$
			createCommandDescriptor(_fileDescriptors._deUniversalFileObject, "GetPermissions", IUniversalDataStoreConstants.C_QUERY_FILE_PERMISSIONS); //$NON-NLS-1$
			createCommandDescriptor(_fileDescriptors._deUniversalArchiveFileObject, "GetPermissions",IUniversalDataStoreConstants.C_QUERY_FILE_PERMISSIONS); //$NON-NLS-1$

			createCommandDescriptor(UniversalFilter, "SetPermissions", IUniversalDataStoreConstants.C_SET_FILE_PERMISSIONS); //$NON-NLS-1$
			createCommandDescriptor(_fileDescriptors._deUniversalFolderObject, "SetPermissions", IUniversalDataStoreConstants.C_SET_FILE_PERMISSIONS); //$NON-NLS-1$
			createCommandDescriptor(_fileDescriptors._deUniversalFileObject, "SetPermissions", IUniversalDataStoreConstants.C_SET_FILE_PERMISSIONS); //$NON-NLS-1$
			createCommandDescriptor(_fileDescriptors._deUniversalArchiveFileObject, "SetPermissions",IUniversalDataStoreConstants.C_SET_FILE_PERMISSIONS); //$NON-NLS-1$
		}
	}


	/**
	 * @since 3.0 made private method public
	 */
	public AbsoluteVirtualPath getAbsoluteVirtualPath(DataElement subject) {
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



	public DataElement handleQueryAllArchive(DataElement subject, DataElement attributes,
			DataElement status, boolean caseSensitive, boolean foldersOnly)
	{
		// do query on a thread
		ArchiveQueryThread queryThread = new ArchiveQueryThread(subject, attributes, caseSensitive, foldersOnly, showHidden, _isWindows, status);
		queryThread.start();

		updateCancellableThreads(status.getParent(), queryThread);
		return status; // query is in thread so not updating status here
	}



	public ISystemArchiveHandler getArchiveHandlerFor(String archivePath) throws SystemMessageException {
		File file = new File(archivePath);
		return _archiveHandlerManager.getRegisteredHandler(file);
	}


	public DataElement handleCopy(DataElement targetFolder, DataElement sourceFile, DataElement nameObj, DataElement status) {

		CopySingleThread copySingleThread = new CopySingleThread(targetFolder, sourceFile, nameObj, this, _isWindows, status);
		copySingleThread.start();

		updateCancellableThreads(status.getParent(), copySingleThread);

		return status;
	}

	/**
	 * Method to obtain the properties of file or folder.
	 */
	public String setProperties(File fileObj, boolean doArchiveProperties) throws SystemMessageException {
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

	public String setProperties(File fileObj) throws SystemMessageException {
		return setProperties(fileObj, false);
	}


	public String getVersion()
	{
		return "7.0.0"; //$NON-NLS-1$
	}

	private File getFileFor(DataElement subject)
	{
		File fileobj = null;
		boolean isVirtual = false;
		String fullName = subject.getValue();
		String queryType = subject.getType();
		if (queryType.equals(IUniversalDataStoreConstants.UNIVERSAL_FILTER_DESCRIPTOR))
		{
			isVirtual = ArchiveHandlerManager.isVirtual(fullName);
			String filterValue = subject.getValue();
			// . translates to home dir
			if (filterValue.equals("."))  //$NON-NLS-1$
			{
				if (_dataStore.getClient() != null){
					filterValue = _dataStore.getClient().getProperty("user.home"); //$NON-NLS-1$
				}
				else {
					filterValue = System.getProperty("user.home"); //$NON-NLS-1$
				}
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
		else if (queryType.equals(IUniversalDataStoreConstants.UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR))
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
		else if (queryType.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR) || queryType.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR))
		{
			isVirtual = true;
		}
		return fileobj;
	}


	/**
	 * Convert permissions in rwxrwxrwx form to octal
	 * @param userPermissions
	 * @return
	 */
	private String alphaPermissionsToOctal(String alphaPermissions)
	{
		StringBuffer buf = new StringBuffer();
		// permissions
		char[] chars = alphaPermissions.toCharArray();

		int offset = -1;
		for (int i = 0; i < 3; i++){
			int value = 0;

			if (chars[++offset] == 'r'){
				value = 4;
			}
			if (chars[++offset] == 'w'){
				value += 2;
			}
			if (chars[++offset] == 'x'){
				value += 1;
			}
			buf.append(value);
		}

		return buf.toString();
	}


	/**
	 * Gets file permissions in the form <octal permissions>|<user>|<group>
	 * @param subject
	 * @param status
	 * @return
	 */
	private DataElement handleQueryFilePermissions(DataElement subject, DataElement status)
	{
		File file = getFileFor(subject);
		if (file == null){
			// subject may have been a filter pointing to a virtual
			return statusCancelled(status);
		}
	

		String result = getFilePermission(file, PERMISSION_ALL);
        status.setAttribute(DE.A_SOURCE, result);
 
    	
    	// for z/os, also need to update the classification if this is a symbolic link
    	String theOS = System.getProperty("os.name"); //$NON-NLS-1$
    	boolean isZ = theOS.toLowerCase().startsWith("z");//$NON-NLS-1$	
    	if (isZ){
			String path = file.getAbsolutePath();
			try {
				String canonical = file.getCanonicalPath();
				if (!path.equals(canonical)){
					// reset the properties    					
					String properties = setProperties(file, false);
					
					// fileType
					String fileType = file.isFile() ? "file" : "directory";  //$NON-NLS-1$//$NON-NLS-2$
					
					// classification
					StringBuffer type = new StringBuffer(FileClassifier.STR_SYMBOLIC_LINK);
					type.append('(');
	                type.append(fileType);
	                type.append(')');
	                type.append(':');
	                type.append(canonical);
	                
				    StringBuffer classifiedProperties = new StringBuffer(properties);
				    classifiedProperties.append('|');
                    classifiedProperties.append(type);
					    					    					
	                subject.setAttribute(DE.A_SOURCE, classifiedProperties.toString());
	                _dataStore.refresh(subject);
				}
			}
			catch (SystemMessageException e)
			{    				
			}
			catch (IOException e)
			{    				
			}
    	}
    	
       	statusDone(status);
		return status;
	}
	
	private String getFilePermission(File file, int permission)
	{
		// permissions in form  "drwxrwxrwx ..."
		String ldStr = simpleShellCommand("ls -ld", file); //$NON-NLS-1$

		StringTokenizer tokenizer = new StringTokenizer(ldStr, " \t"); //$NON-NLS-1$

		// permissions in form "rwxrwxrwx"
		String permString = tokenizer.nextToken().substring(1);
		String octalPermissions = alphaPermissionsToOctal(permString);

		// user and group
		tokenizer.nextToken(); // nothing important
		String user = tokenizer.nextToken(); // 3rd
		String group = tokenizer.nextToken(); // 4th
		
		String result = null;
		switch (permission){
		case PERMISSION_BITS:
			result = octalPermissions;
			break;
			
		case PERMISSION_OWNER:
			result = user;
			break;
			
		case PERMISSION_GROUP:
			result = group;
			break;
			
		case PERMISSION_ALL:
		default:
			result =  octalPermissions + '|' + user + '|' + group;
			break;
		}
		
		return result;
	}
	
	
	
	

	/**
	 * Set file permissions including user and group
	 * @param subject
	 * @param newPermissions permissions in the form <octal permissions>|<user>|<group>
	 * @param status
	 * @return
	 */
	private DataElement handleSetFilePermissions(DataElement subject, DataElement newPermissions, DataElement status)
	{
		File file = getFileFor(subject);
		
        String[] auditData = new String[] {"SET-PERMISSIONS", file.getAbsolutePath(), null, null}; //$NON-NLS-1$
     	UniversalServerUtilities.logAudit(auditData, _dataStore);
     	

		String permissionsStr = newPermissions.getName();
		String[] permAttributes = permissionsStr.split("\\"+IServiceConstants.TOKEN_SEPARATOR); //$NON-NLS-1$

		// set the permissions
		String result = simpleShellCommand("chmod " + permAttributes[0], file); //$NON-NLS-1$

		String previousGroup = getFilePermission(file, PERMISSION_GROUP);
		String previousUser = getFilePermission(file, PERMISSION_OWNER);
		if (!previousUser.equals(permAttributes[1]) || !previousGroup.equals(permAttributes[2])){
			// set the user and group at once
			simpleShellCommand("chown " + permAttributes[1] + ":" + permAttributes[2], file); //$NON-NLS-1$
		}
		

		
        status.setAttribute(DE.A_SOURCE, result);
    	statusDone(status);

		return status;
	}

	/* - not used right now so commenting out
	private String simpleShellCommand(String cmd)
	{
		String result = null;
	    String args[] = new String[3];
        args[0] = "sh"; //$NON-NLS-1$
        args[1] = "-c"; //$NON-NLS-1$
        args[2] = cmd;

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

	*/

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
}
