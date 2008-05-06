/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
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
 * Martin Oberhuber (Wind River) - Fix 158534 - NPE in upload/download after conflict
 * Martin Oberhuber (Wind River) - Fix 162962 - recursive removeCachedRemoteFile()
 * Martin Oberhuber (Wind River) - [168596] FileServiceSubSystem.isCaseSensitive()
 * Martin Oberhuber (Wind River) - [177523] Unify singleton getter methods
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Martin Oberhuber (Wind River) - [183824] Forward SystemMessageException from IRemoteFileSubsystem
 * Javier Montalvo Orus (Symbian) - [199773] Default file transfer mode is ignored for some file types
 * David McKnight   (IBM)        - [207095] Implicit connect on getRemoteFileObject
 * David McKnight   (IBM)        - [207100] fire event after upload and download
 * David McKnight   (IBM)        - [207178] changing list APIs for file service and subsystems
 * David McKnight   (IBM)        - [162195] new APIs for upload multi and download multi
 * David McKnight   (IBM)        - [203114] don't treat XML files specially (no hidden prefs for bin vs text)
 * David McKnight   (IBM)        - [209552] API changes to use multiple and getting rid of deprecated
 * Kevin Doyle		(IBM)		 - [208778] [efs][api] RSEFileStore#getOutputStream() does not support EFS#APPEND
 * David McKnight   (IBM)        - [209704] added supportsEncodingConversion()
 * David Dykstal (IBM) - [197036] pulling up subsystem switch logic
 * David Dykstal (IBM) - [217556] remove service subsystem types
 * Martin Oberhuber (Wind River) - [219098][api] FileServiceSubSystem should not be final
 * David McKnight   (IBM)        - [216252] [api][nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 * Martin Oberhuber (Wind River) - [220020][api][breaking] SystemFileTransferModeRegistry should be internal
 * David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 * Kevin Doyle		(IBM)		 - [224162] SystemEditableRemoteFile.saveAs does not work because FileServiceSubSytem.upload does invalid check
 * Martin Oberhuber (Wind River) - [218304] Improve deferred adapter loading
 * David Dykstal (IBM) - [221211] fix IFileService API for batch operations
 * Martin Oberhuber (Wind River) - [221211] Fix markStale() for delete() operation with exceptions
 *******************************************************************************/

package org.eclipse.rse.subsystems.files.core.servicesubsystem;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemRemoteChangeEvents;
import org.eclipse.rse.core.events.SystemRemoteChangeEvent;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.core.subsystems.RemoteChildrenContentsType;
import org.eclipse.rse.internal.subsystems.files.core.Activator;
import org.eclipse.rse.internal.subsystems.files.core.ISystemFileMessageIds;
import org.eclipse.rse.internal.subsystems.files.core.SystemFileResources;
import org.eclipse.rse.services.clientserver.PathUtility;
import org.eclipse.rse.services.clientserver.SystemSearchString;
import org.eclipse.rse.services.clientserver.archiveutils.AbsoluteVirtualPath;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.clientserver.messages.CommonMessages;
import org.eclipse.rse.services.clientserver.messages.ICommonMessageIds;
import org.eclipse.rse.services.clientserver.messages.SimpleSystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.services.search.IHostSearchResultConfiguration;
import org.eclipse.rse.services.search.IHostSearchResultSet;
import org.eclipse.rse.services.search.ISearchService;
import org.eclipse.rse.subsystems.files.core.ILanguageUtilityFactory;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
import org.eclipse.rse.subsystems.files.core.subsystems.IHostFileToRemoteFileAdapter;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileContext;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileContext;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileSubSystem;
import org.eclipse.rse.ui.SystemBasePlugin;

/**
 * Generic Subsystem implementation for remote files.
 *
 * Clients may instantiate this class from their subsystem configurations.
 * <p>
 * Extending (overriding) this class is discouraged: configuration of the subsystem
 * behavior should be done by providing a custom {@link IFileService} implementation
 * wherever possible.
 */
public class FileServiceSubSystem extends RemoteFileSubSystem implements IFileServiceSubSystem
{

	protected ILanguageUtilityFactory _languageUtilityFactory;
	protected IFileService _hostFileService;
	protected ISearchService _hostSearchService;
	protected IHostFileToRemoteFileAdapter _hostFileToRemoteFileAdapter;
	protected IRemoteFile _userHome;
	public FileServiceSubSystem(IHost host, IConnectorService connectorService, IFileService hostFileService, IHostFileToRemoteFileAdapter fileAdapter, ISearchService searchService)
	{
		super(host, connectorService);
		_hostFileService = hostFileService;
		_hostFileToRemoteFileAdapter = fileAdapter;
		_hostSearchService = searchService;

	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileSubSystem#isCaseSensitive()
	 */
	public boolean isCaseSensitive() {
		return getFileService().isCaseSensitive();
	}

	public IRemoteFileContext getContextFor(IRemoteFile file)
	{
		return getContext(file);
	}

	public IRemoteFileContext getTheDefaultContext()
	{
		return getDefaultContextNoFilterString();
	}

	public IFileService getFileService()
	{
		return _hostFileService;
	}

	public void setFileService(IFileService service)
	{
		_hostFileService = service;
	}

	public ISearchService getSearchService()
	{
		return _hostSearchService;
	}

	public void setSearchService(ISearchService service)
	{
		_hostSearchService = service;
	}

	public IHostFileToRemoteFileAdapter getHostFileToRemoteFileAdapter()
	{
		return _hostFileToRemoteFileAdapter;
	}

	public void setHostFileToRemoteFileAdapter(IHostFileToRemoteFileAdapter hostFileAdapter)
	{
		_hostFileToRemoteFileAdapter = hostFileAdapter;
	}

	/**
	 * Constructs an IRemoteFile object given
	 * an unqualified file or folder name and its parent folder object.
	 * @param parent Folder containing the folder or file
	 * @param folderOrFileName Un-qualified folder or file name
	 * @param monitor the progress monitor
	 * @return an IRemoteFile object for the file.
	 * @see IRemoteFile
	 */
	public IRemoteFile getRemoteFileObject(IRemoteFile parent, String folderOrFileName, IProgressMonitor monitor) throws SystemMessageException
	{
		// for bug 207095, implicit connect if the connection is not connected
		checkIsConnected(monitor);

		String fullPath = parent.getAbsolutePath() + getSeparator() + folderOrFileName;
		IRemoteFile file = getCachedRemoteFile(fullPath);
		if (file != null && !file.isStale())
		{
			return file;
		}

		IHostFile node = getFile(parent.getAbsolutePath(), folderOrFileName, monitor);
		return getHostFileToRemoteFileAdapter().convertToRemoteFile(this, getDefaultContext(), parent, node);
	}



	/**
	 * Constructs and returns an IRemoteFile object given a fully-qualified
	 * file or folder name.
	 * @param folderOrFileName Fully qualified folder or file name
	 * @param monitor the progress monitor
	 * @return The constructed IRemoteFile
	 * @see IRemoteFile
	 */
	public IRemoteFile getRemoteFileObject(String folderOrFileName, IProgressMonitor monitor) throws SystemMessageException
	{

		String fofName = folderOrFileName;
		if (folderOrFileName.length() > 1)
		{
			fofName =	ArchiveHandlerManager.cleanUpVirtualPath(folderOrFileName);
		}
		IRemoteFile file = getCachedRemoteFile(fofName);
		if (file != null && !file.isStale()) {
			return file;
		}

		// for bug 207095, implicit connect if the connection is not connected
		checkIsConnected(monitor);

		if (fofName.endsWith(ArchiveHandlerManager.VIRTUAL_SEPARATOR))
		{
			fofName = fofName.substring(0, fofName.length() - ArchiveHandlerManager.VIRTUAL_SEPARATOR.length());
		}

		int j = fofName.indexOf(ArchiveHandlerManager.VIRTUAL_SEPARATOR);
		if (j == -1)
		{
			if (fofName.equals("/")) //$NON-NLS-1$
			{
				try
				{
					return listRoots(null)[0];
				}
				catch (Exception e)
				{

				}
			}

			if (fofName.equals(".")) { //$NON-NLS-1$
				IRemoteFile userHome =  getUserHome();
				if (userHome == null){

					// with 207095, it's possible that we could be trying to get user home when not connected
					SystemMessage msg = new SimpleSystemMessage(Activator.PLUGIN_ID,
							ICommonMessageIds.MSG_ERROR_UNEXPECTED,
							IStatus.ERROR,
							CommonMessages.MSG_ERROR_UNEXPECTED);
					throw new SystemMessageException(msg);
				}
				return userHome;
			}

			String sep = PathUtility.getSeparator(folderOrFileName);
			if (fofName.endsWith(sep))
			{
				fofName = fofName.substring(0, fofName.length() - sep.length());
			}

			if (fofName.endsWith(":")) //$NON-NLS-1$
			{
				try
				{
					IHostFile[] roots = getRoots(null);
					for (int i = 0; i < roots.length; i++)
						if (roots[i].getAbsolutePath().toLowerCase().startsWith(fofName.toLowerCase()))
							return getHostFileToRemoteFileAdapter().convertToRemoteFile(this, getDefaultContext(), null, roots[i]);
				}
				catch (InterruptedException e)
				{
				}
				return null;
			}

			int lastSep = fofName.lastIndexOf(sep);

			if (lastSep > -1)
			{
				String parentPath = fofName.substring(0, lastSep);


				if (parentPath.length() == 0) parentPath = "/"; //$NON-NLS-1$
				String name = fofName.substring(lastSep + 1, fofName.length());

				IHostFile node = getFile(parentPath, name, monitor);
				if (node != null)
				{
					IRemoteFile parent = null;
					if (!node.isRoot())
					{
						//parent = getRemoteFileObject(parentPath);
					}
					return getHostFileToRemoteFileAdapter().convertToRemoteFile(this, getDefaultContext(), parent, node);
				}
			}
			return null;
		}
		else
		{
			AbsoluteVirtualPath avp = new AbsoluteVirtualPath(fofName);
			IHostFile node = getFile(avp.getPath(), avp.getName(), null);
			if (node != null)
			{
				return getHostFileToRemoteFileAdapter().convertToRemoteFile(this, getDefaultContext(), null, node);
			}
			else return null;
		}
	}





	/**
	 * @return The IRemoteFile that is the user's home directory on this remote file system.
	 * The remote file system is assumed to have a concept of a home directory.
	 */
	protected IRemoteFile getUserHome()
	{
		if (_userHome != null)
		{
			return _userHome;
		}
		IRemoteFile root = getCachedRemoteFile("."); //$NON-NLS-1$
		if (root != null && !root.isStale()) {
			return root;
		}
		IHostFile userHome = getFileService().getUserHome();
		// with 207095, it's possible that user is not connected, and that userHome is null
		if (userHome == null) {
			return null;
		}

		IRemoteFile parent = null;
		if (!userHome.getParentPath().equals(".")) //$NON-NLS-1$
		{
			try
			{
				//parent = getRemoteFileObject(userHome.getParentPath());
			}
			catch (Exception e)
			{
			}
		}
		root = getHostFileToRemoteFileAdapter().convertToRemoteFile(this, getDefaultContext(), parent, userHome);
		cacheRemoteFile(root, "."); //$NON-NLS-1$
		_userHome = root;
		return root;
	}
	protected IHostFile[] internalList(String parentPath, String fileNameFilter, int fileType, IProgressMonitor monitor) throws SystemMessageException
	{
		return getFileService().list(parentPath, fileNameFilter, fileType, monitor);
	}



	protected IHostFile getFile(String parentPath, String fileName, IProgressMonitor monitor) throws SystemMessageException
	{
		return getFileService().getFile(parentPath, fileName, monitor);
	}

	protected IHostFile[] getRoots(IProgressMonitor monitor) throws InterruptedException, SystemMessageException
	{
		return getFileService().getRoots(monitor);
	}

	public IRemoteFile[] getRemoteFileObjects(String[] folderOrFileNames,
			IProgressMonitor monitor) throws SystemMessageException
	{
		// for bug 207095, implicit connect if the connection is not connected
		checkIsConnected(monitor);

		String[] parentPaths = new String[folderOrFileNames.length];
		String[] names = new String[folderOrFileNames.length];
		String sep = null;
		for (int i = 0; i < folderOrFileNames.length; i++)
		{
			String fofName = folderOrFileNames[i];
			if (sep == null)
				sep = PathUtility.getSeparator(fofName);

			String parentPath = null;
			String name = null;
			int lastSep = fofName.lastIndexOf(sep);

			if (lastSep > -1)
			{
				parentPath = fofName.substring(0, lastSep);

				if (parentPath.length() == 0) parentPath = "/"; //$NON-NLS-1$
				name = fofName.substring(lastSep + 1, fofName.length());
			}

			parentPaths[i] = parentPath;
			names[i] = name;
		}

		RemoteFileContext context = getDefaultContext();
		List hostFiles = new ArrayList(10);
		getFileService().getFileMultiple(parentPaths, names, hostFiles, monitor);
		IHostFile[] nodes = new IHostFile[hostFiles.size()];
		hostFiles.toArray(nodes);
		return getHostFileToRemoteFileAdapter().convertToRemoteFiles(this, context, null, nodes);
	}


	/**
	 * Return a list of remote folders and files in the given folder. Only file names are subsettable
	 * by the given file name filter. It can be null for no subsetting.
	 * @param parents The parent folders to list folders and files in
	 * @param fileNameFilters The name patterns to subset the file list by, or null to return all files.
	 * @param fileTypes - indicates whether to query files, folders, both or some other type
	 * @param monitor the progress monitor
	 */
	public IRemoteFile[] listMultiple(IRemoteFile[] parents, String[] fileNameFilters, int[] fileTypes,  IProgressMonitor monitor) throws SystemMessageException
	{
		String[] parentPaths = new String[parents.length];
		for (int i = 0; i < parents.length; i++)
		{
			parentPaths[i] = parents[i].getAbsolutePath();
		}

		List hostFiles = new ArrayList(10);
		getFileService().listMultiple(parentPaths, fileNameFilters, fileTypes, hostFiles, monitor);
		IHostFile[] results = new IHostFile[hostFiles.size()];
		hostFiles.toArray(results);
		RemoteFileContext context = getDefaultContext();

		IRemoteFile[] farr = getHostFileToRemoteFileAdapter().convertToRemoteFiles(this, context, null, results);

		// caching
		for (int i = 0; i < parents.length; i++)
		{
			IRemoteFile parent = parents[i];
			String parentPath = parentPaths[i];
			String filter = fileNameFilters[i];

			List underParent = new ArrayList();
			// what files are under this one?
			for (int j = 0; j < farr.length; j++)
			{
				IRemoteFile child = farr[j];
				String childParentPath = child.getParentPath();

				if (parentPath.equals(childParentPath))
				{
					underParent.add(child);
				}
			}
			if (underParent.size() > 0)
			{
				parent.setContents(RemoteChildrenContentsType.getInstance(), filter, underParent.toArray());
			}
		}

		return farr;
	}


	/**
	 * Return a list of remote folders and files in the given folder. Only file names are subsettable
	 * by the given file name filter. It can be null for no subsetting.
	 * @param parents The parent folders to list folders and files in
	 * @param fileNameFilters The name patterns to subset the file list by, or null to return all files.
	 * @param fileType - indicates whether to query files, folders, both or some other type
	 * @param monitor the progress monitor
	 */
	public IRemoteFile[] listMultiple(IRemoteFile[] parents, String[] fileNameFilters, int fileType,  IProgressMonitor monitor) throws SystemMessageException
	{
		String[] parentPaths = new String[parents.length];
		for (int i = 0; i < parents.length; i++)
		{
			parentPaths[i] = parents[i].getAbsolutePath();
		}

		List hostFiles = new ArrayList(10);
		getFileService().listMultiple(parentPaths, fileNameFilters, fileType, hostFiles, monitor);
		IHostFile[] results = new IHostFile[hostFiles.size()];
		hostFiles.toArray(results);
		RemoteFileContext context = getDefaultContext();

		IRemoteFile[] farr = getHostFileToRemoteFileAdapter().convertToRemoteFiles(this, context, null, results);

		// caching
		for (int i = 0; i < parents.length; i++)
		{
			IRemoteFile parent = parents[i];
			String parentPath = parentPaths[i];
			String filter = fileNameFilters[i];

			List underParent = new ArrayList();
			// what files are under this one?
			for (int j = 0; j < farr.length; j++)
			{
				IRemoteFile child = farr[j];
				String childParentPath = child.getParentPath();

				if (parentPath.equals(childParentPath))
				{
					underParent.add(child);
				}
			}
			if (underParent.size() > 0)
			{
				parent.setContents(RemoteChildrenContentsType.getInstance(), filter, underParent.toArray());
			}
		}

		return farr;
	}


	/**
	 * Return a list of remote folders and/or files in the given folder.
	 * <p>
	 * The files part of the list is filtered by the given file name filter.
	 * It can be null for no filtering.
	 * This version is called by RemoteFileSubSystemImpl's resolveFilterString(s).
	 * @param parent The parent folder to list folders and files in
	 * @param fileNameFilter The name pattern to subset the file list by, or null to return all files.
	 * @param context The holder of state information
	 * @param fileType indicates whether to filter files, folders, both or something else
	 * @param monitor the progress monitor
	 */
	public IRemoteFile[] list(IRemoteFile parent, String fileNameFilter, IRemoteFileContext context, int fileType, IProgressMonitor monitor) throws SystemMessageException
	{
		String parentPath = null;
		if (parent != null) {
			parentPath = parent.getAbsolutePath();
		} else {
			parentPath = "/"; //$NON-NLS-1$
		}

		if (parent != null && !parent.canRead())
		{
			String msgTxt = NLS.bind(SystemFileResources.MSG_FOLDER_UNREADABLE, parentPath);
			SystemMessage msg = new SimpleSystemMessage(Activator.PLUGIN_ID,
					ISystemFileMessageIds.MSG_FOLDER_UNREADABLE,
					IStatus.INFO, msgTxt);
			throw new SystemMessageException(msg);
		}

		IHostFile[] results = internalList(parentPath, fileNameFilter, fileType, monitor);

		IRemoteFile[] farr = getHostFileToRemoteFileAdapter().convertToRemoteFiles(this, context, parent, results);
		if (parent != null)
			parent.setContents(RemoteChildrenContentsType.getInstance(), fileNameFilter, farr);
		return farr;
	}



	public IRemoteFile[] listRoots(IRemoteFileContext context, IProgressMonitor monitor) throws InterruptedException
	{
		IHostFile[] roots = null;
		try
		{
			roots = getRoots(monitor);
		}
		catch (SystemMessageException e)
		{

		}

		IRemoteFile[] results = getHostFileToRemoteFileAdapter().convertToRemoteFiles(this, context, null, roots);
		return results;
	}

	protected boolean isBinary(String localEncoding, String hostEncoding, String remotePath)
	{
		return RemoteFileUtility.getSystemFileTransferModeRegistry().isBinary(remotePath);
	}

	protected boolean isBinary(IRemoteFile source)
	{
		return source.isBinary(); // always use preferences (whether xml or not)
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem#upload(java.lang.String, java.lang.String, java.lang.String, java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void upload(String source, String srcEncoding, String remotePath, String rmtEncoding, IProgressMonitor monitor) throws SystemMessageException {
		int slashIndex = remotePath.lastIndexOf(getSeparator());
		if (slashIndex > -1) {
			String remoteParentPath = remotePath.substring(0, slashIndex);
			String remoteFileName = remotePath.substring(slashIndex + 1, remotePath.length());
			boolean isBinary = isBinary(srcEncoding, rmtEncoding, remotePath);
			if (ArchiveHandlerManager.isVirtual(remotePath))
			{
				AbsoluteVirtualPath avp = new AbsoluteVirtualPath(remotePath);
				remoteParentPath = avp.getPath();
				remoteFileName = avp.getName();
			}
			getFileService().upload(new File(source), remoteParentPath, remoteFileName, isBinary, srcEncoding, rmtEncoding, monitor);

			// notify that the file was uploaded
			ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
			sr.fireEvent(new SystemRemoteChangeEvent(ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_UPLOADED, remotePath, remoteParentPath, this));
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem#upload(java.lang.String, org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile, java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void upload(String source, IRemoteFile destination, String encoding, IProgressMonitor monitor) throws SystemMessageException
	{
		String remoteParentPath = destination.getParentPath();
		String remoteFileName = destination.getName();
		String hostEncoding = destination.getEncoding();
		boolean isBinary = isBinary(encoding, hostEncoding, destination.getAbsolutePath());

		if ((destination.exists() && !destination.canWrite()) || (!destination.exists() && !destination.getParentRemoteFile().canWrite()))
		{
			String msgTxt = NLS.bind(SystemFileResources.MSG_FILE_CANNOT_BE_SAVED, remoteFileName, getHostName());
			String msgDetails = SystemFileResources.MSG_FILE_CANNOT_BE_SAVED_DETAILS;

			SystemMessage msg = new SimpleSystemMessage(Activator.PLUGIN_ID,
					ISystemFileMessageIds.MSG_FILE_CANNOT_BE_SAVED,
					IStatus.ERROR, msgTxt, msgDetails);
			throw new SystemMessageException(msg);
		}
		getFileService().upload(new File(source), remoteParentPath, remoteFileName, isBinary, encoding, hostEncoding, monitor);

		// notify that the file was uploaded
		ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
		sr.fireEvent(new SystemRemoteChangeEvent(ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_UPLOADED, destination, destination.getParentRemoteFile(), this));
	}

	public void uploadMultiple(String[] sources, String[] srcEncodings,
			String[] remotePaths, String[] rmtEncodings,
			IProgressMonitor monitor) throws SystemMessageException
	{
		// create list of stuff
		File[] sourceFiles = new File[sources.length];
		boolean[] isBinaries = new boolean[sources.length];
		String[] remoteParentPaths = new String[sources.length];
		String[] remoteFileNames = new String[sources.length];

		// gather info
		for (int i = 0; i < sources.length; i++)
		{
			sourceFiles[i] = new File(sources[i]);
			String remotePath = remotePaths[i];
			int slashIndex = remotePath.lastIndexOf(getSeparator());
			if (slashIndex > -1) {
				remoteParentPaths[i] = remotePath.substring(0, slashIndex);
				remoteFileNames[i] = remotePath.substring(slashIndex + 1, remotePath.length());
				isBinaries[i] = isBinary(srcEncodings[i], rmtEncodings[i], remotePath);
				if (ArchiveHandlerManager.isVirtual(remotePath))
				{
					AbsoluteVirtualPath avp = new AbsoluteVirtualPath(remotePath);
					remoteParentPaths[i] = avp.getPath();
					remoteFileNames[i] = avp.getName();
				}
			}
			else // unexpected
			{
				// throw an exception here
				//SystemMessage msg = RSEUIPlugin.getPluginMessage("RSEF5003").makeSubstitution(remoteFileNames[i], getHostName()); //$NON-NLS-1$
				//throw new SystemMessageException(msg);
			}
		}

		// upload
		getFileService().uploadMultiple(sourceFiles, remoteParentPaths, remoteFileNames, isBinaries, srcEncodings, rmtEncodings, monitor);

		// notification
		// notify that the file was uploaded
		ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
		for (int j = 0; j < remotePaths.length; j++)
		{
			String remotePath = remotePaths[j];
			String remoteParentPath = remoteParentPaths[j];
			sr.fireEvent(new SystemRemoteChangeEvent(ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_UPLOADED, remotePath, remoteParentPath, this));
		}
	}

	public void uploadMultiple(String[] sources, IRemoteFile[] destinations,
			String[] encodings, IProgressMonitor monitor)
			throws SystemMessageException
{
		// create list of stuff
		File[] sourceFiles = new File[sources.length];
		boolean[] isBinaries = new boolean[sources.length];
		String[] remoteParentPaths = new String[sources.length];
		String[] remoteFileNames = new String[sources.length];
		String[] hostEncodings = new String[sources.length];

		// gather info
		for (int i = 0; i < sources.length; i++)
		{
			sourceFiles[i] = new File(sources[i]);
			IRemoteFile destination = destinations[i];

			remoteParentPaths[i] = destination.getAbsolutePath();
			remoteFileNames[i] = destination.getName();

			if (!destination.canWrite())
			{
				String msgTxt = NLS.bind(SystemFileResources.MSG_FILE_CANNOT_BE_SAVED, remoteFileNames[i], getHostName());
				String msgDetails = SystemFileResources.MSG_FILE_CANNOT_BE_SAVED_DETAILS;

				SystemMessage msg = new SimpleSystemMessage(Activator.PLUGIN_ID,
						ISystemFileMessageIds.MSG_FILE_CANNOT_BE_SAVED,
						IStatus.ERROR, msgTxt, msgDetails);
				throw new SystemMessageException(msg);
			}

			hostEncodings[i] = destination.getEncoding();
			isBinaries[i] = isBinary(encodings[i], hostEncodings[i], destination.getAbsolutePath());

		}

		// upload
		getFileService().uploadMultiple(sourceFiles, remoteParentPaths, remoteFileNames, isBinaries, encodings, hostEncodings, monitor);

		// notification
		// notify that the file was uploaded
		ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
		for (int j = 0; j < destinations.length; j++)
		{
			IRemoteFile destination = destinations[j];
			sr.fireEvent(new SystemRemoteChangeEvent(ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_UPLOADED, destination, destination.getParentRemoteFile(), this));
		}

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem#download(org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile, java.lang.String, java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void download(IRemoteFile file, String localpath, String encoding, IProgressMonitor monitor) throws SystemMessageException
	{
		//Fixing bug 158534. TODO remove when bug 162688 is fixed.
		if (monitor==null) {
			monitor = new NullProgressMonitor();
		}
		String parentPath = file.getParentPath();
		File localFile = new File(localpath);

		// FIXME why are we using file.getEncoding() instead of the specified encoding?
		getFileService().download(parentPath, file.getName(), localFile, isBinary(file), file.getEncoding(), monitor);
		if (monitor.isCanceled())
		{
			localFile.delete();
		}
		else
		{
			// notify that the file was downloaded
			ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
			sr.fireEvent(new SystemRemoteChangeEvent(ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_DOWNLOADED, file, file.getParentRemoteFile(), this));

		}
	}

	public void downloadMultiple(IRemoteFile[] sources, String[] destinations,
			String[] encodings, IProgressMonitor monitor)
			throws SystemMessageException
	{
		//Fixing bug 158534. TODO remove when bug 162688 is fixed.
		if (monitor==null) {
			monitor = new NullProgressMonitor();
		}

		// get arrays of parent paths and local files
		String[] parentPaths = new String[sources.length];
		String[] names = new String[sources.length];
		boolean[] isBinaries = new boolean[sources.length];
		File[] localFiles = new File[sources.length];

		for (int i = 0; i < sources.length; i++)
		{
			IRemoteFile file = sources[i];
			parentPaths[i] = file.getParentPath();
			names[i] = file.getName();
			isBinaries[i] = isBinary(file);
			localFiles[i] = new File(destinations[i]);
		}

		getFileService().downloadMultiple(parentPaths, names, localFiles, isBinaries, encodings, monitor);
		if (monitor.isCanceled())
		{
			for (int d = 0; d < localFiles.length; d++)
			{
				File f = localFiles[d];
				f.delete();
			}
		}
		else
		{
			// notify that the file was downloaded
			ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();

			for (int r = 0; r < sources.length; r++)
			{
				IRemoteFile file = sources[r];
				sr.fireEvent(new SystemRemoteChangeEvent(ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_DOWNLOADED, file, file.getParentRemoteFile(), this));
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem#copy(org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile, org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile, java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public boolean copy(IRemoteFile sourceFolderOrFile, IRemoteFile targetFolder, String newName, IProgressMonitor monitor) throws SystemMessageException
	{
		IFileService service = getFileService();
		service.copy(sourceFolderOrFile.getParentPath(), sourceFolderOrFile.getName(), targetFolder.getAbsolutePath(), newName, monitor);
		return true;
	}

	public boolean copyBatch(IRemoteFile[] sourceFolderOrFiles, IRemoteFile targetFolder, IProgressMonitor monitor) throws SystemMessageException
	{
		IFileService service = getFileService();
		String[] sourceParents = new String[sourceFolderOrFiles.length];
		String[] sourceNames = new String[sourceFolderOrFiles.length];

		for (int i = 0; i < sourceFolderOrFiles.length; i++)
		{
			sourceParents[i] = sourceFolderOrFiles[i].getParentPath();
			sourceNames[i] = sourceFolderOrFiles[i].getName();
		}
		service.copyBatch(sourceParents, sourceNames, targetFolder.getAbsolutePath(), monitor);
		return true;
	}

	public IRemoteFile getParentFolder(IRemoteFile folderOrFile, IProgressMonitor monitor)
	{
		try
		{
			return getRemoteFileObject(folderOrFile.getParentPath(), monitor);
		}
		catch (Exception e)
		{
			return null;
		}
	}

	public IRemoteFile createFile(IRemoteFile fileToCreate, IProgressMonitor monitor) throws SystemMessageException
	{
		IFileService service = getFileService();
		String parent = fileToCreate.getParentPath();
		String name = fileToCreate.getName();
		IHostFile newFile = service.createFile(parent, name, monitor);
		return getHostFileToRemoteFileAdapter().convertToRemoteFile(this, getDefaultContext(), fileToCreate.getParentRemoteFile(), newFile);
	}

	public IRemoteFile createFolder(IRemoteFile folderToCreate, IProgressMonitor monitor) throws SystemMessageException
	{
		IFileService service = getFileService();
		String parent = folderToCreate.getParentPath();
		String name = folderToCreate.getName();
		IHostFile newFolder = service.createFolder(parent, name, monitor);
		return getHostFileToRemoteFileAdapter().convertToRemoteFile(this, getDefaultContext(), folderToCreate.getParentRemoteFile(), newFolder);
	}

	public IRemoteFile createFolders(IRemoteFile folderToCreate, IProgressMonitor monitor) throws SystemMessageException
	{
		return createFolder(folderToCreate, monitor);
	}

	public boolean delete(IRemoteFile folderOrFile, IProgressMonitor monitor) throws SystemMessageException
	{
		IFileService service = getFileService();
		String parent = folderOrFile.getParentPath();
		String name = folderOrFile.getName();
		try {
			service.delete(parent, name, monitor);
		} finally {
			folderOrFile.markStale(true);
		}
		return true;
	}

	public boolean deleteBatch(IRemoteFile[] folderOrFiles, IProgressMonitor monitor) throws SystemMessageException
	{

		String[] parents = new String[folderOrFiles.length];
		String[] names = new String[folderOrFiles.length];
		for (int i = 0; i < folderOrFiles.length; i++)
		{
			parents[i] = folderOrFiles[i].getParentPath();
			names[i] = folderOrFiles[i].getName();
			folderOrFiles[i].markStale(true);
			//bug 162962: need to recursively remove children from cache
			removeCachedRemoteFile(folderOrFiles[i]);
		}
		IFileService service = getFileService();
		service.deleteBatch(parents, names, monitor);
		return true;
	}

	public boolean rename(IRemoteFile folderOrFile, String newName, IProgressMonitor monitor) throws SystemMessageException
	{
		removeCachedRemoteFile(folderOrFile);
		IFileService service = getFileService();
		String srcParent = folderOrFile.getParentPath();
		String oldName = folderOrFile.getName();
		String newPath = srcParent + folderOrFile.getSeparator() + newName;
		service.rename(srcParent, oldName, newName, monitor);
		folderOrFile.getHostFile().renameTo(newPath);
		return true;
	}

	public boolean move(IRemoteFile sourceFolderOrFile, IRemoteFile targetFolder, String newName, IProgressMonitor monitor) throws SystemMessageException
	{
		IFileService service = getFileService();
		String srcParent = sourceFolderOrFile.getParentPath();
		String srcName = sourceFolderOrFile.getName();
		String tgtParent = targetFolder.getAbsolutePath();
		removeCachedRemoteFile(sourceFolderOrFile);
		try {
			service.move(srcParent, srcName, tgtParent, newName, monitor);
		} finally {
			sourceFolderOrFile.markStale(true);
			targetFolder.markStale(true);
		}
		return true;
	}

	public boolean setLastModified(IRemoteFile folderOrFile, long newDate, IProgressMonitor monitor) throws SystemMessageException
	{
		String name = folderOrFile.getName();
		String parent = folderOrFile.getParentPath();
		_hostFileService.setLastModified(parent, name, newDate, monitor);
		return true;
	}

	public boolean setReadOnly(IRemoteFile folderOrFile, boolean readOnly, IProgressMonitor monitor) throws SystemMessageException
	{
		String name = folderOrFile.getName();
		String parent = folderOrFile.getParentPath();
		_hostFileService.setReadOnly(parent, name, readOnly, monitor);
		return true;
	}

	public ILanguageUtilityFactory getLanguageUtilityFactory()
	{
		if (_languageUtilityFactory == null)
		{
			_languageUtilityFactory = ((IFileServiceSubSystemConfiguration)getParentRemoteFileSubSystemConfiguration()).getLanguageUtilityFactory(this);
		}
		return _languageUtilityFactory;
	}

	public void setLanguageUtilityFactory(ILanguageUtilityFactory factory)
	{
		_languageUtilityFactory = factory;
	}

	public void search(IHostSearchResultConfiguration searchConfig)
	{
		ISearchService searchService = getSearchService();
		if (searchService != null)
		{
			SearchJob job = new SearchJob(searchConfig, searchService, getFileService());
			job.schedule();
		}
	}

	public void cancelSearch(IHostSearchResultConfiguration searchConfig)
	{
		ISearchService searchService = getSearchService();
		if (searchService != null)
		{
			searchService.cancelSearch(searchConfig, null);
		}
	}

	public IHostSearchResultConfiguration createSearchConfiguration(IHostSearchResultSet resultSet, Object searchTarget, SystemSearchString searchString)
	{
		ISearchService searchService = getSearchService();
		if (searchService != null)
		{
			IFileServiceSubSystemConfiguration factory = (IFileServiceSubSystemConfiguration)getParentRemoteFileSubSystemConfiguration();
			if (factory != null)
			{
				return factory.createSearchConfiguration(getHost(), resultSet, searchTarget, searchString);
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SubSystem#canSwitchTo(org.eclipse.rse.core.subsystems.ISubSystemConfiguration)
	 * Overriding the super implementation to return true for any configuration that implements IFileServiceSubSystemConfiguration
	 */
	public boolean canSwitchTo(ISubSystemConfiguration configuration) {
		return (configuration instanceof IFileServiceSubSystemConfiguration);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SubSystem#internalSwitchServiceSubSystemConfiguration(org.eclipse.rse.core.subsystems.ISubSystemConfiguration)
	 * Overriding the super implementation to do switch the file subsystem bits that need to be copied or initialized in a switch
	 */
	protected void internalSwitchSubSystemConfiguration(ISubSystemConfiguration newConfig) {
		if (newConfig instanceof IFileServiceSubSystemConfiguration) {
			IHost host = getHost();
			IFileServiceSubSystemConfiguration config = (IFileServiceSubSystemConfiguration) newConfig;
			// file subsystem specific bits
			_cachedRemoteFiles.clear();
			_languageUtilityFactory = null;
			setFileService(config.getFileService(host));
			setHostFileToRemoteFileAdapter(config.getHostFileAdapter());
			setSearchService(config.getSearchService(host));
		}
	}

	public Class getServiceType()
	{
		return IFileService.class;
	}

	public void initializeSubSystem(IProgressMonitor monitor)
	{
		super.initializeSubSystem(monitor);
		getFileService().initService(monitor);
	}

	public void uninitializeSubSystem(IProgressMonitor monitor)
	{
		getFileService().uninitService(monitor);
		_userHome = null;
		super.uninitializeSubSystem(monitor);
	}

	/**
	 * Returns the encoding from the file service being used by this subsystem.
	 * @see RemoteFileSubSystem#getRemoteEncoding()
	 */
	public String getRemoteEncoding() {

		try {

			IHost host = getHost();

			// get the encoding from the host that was not set by the remote system
			String encoding = host.getDefaultEncoding(false);

			// get the encoding from the host that was set by querying a remote system
			// this allows us to pick up the host encoding that may have been set by another subsystem
			if (encoding == null) {
				encoding = getFileService().getEncoding(null);

				if (encoding != null) {
					host.setDefaultEncoding(encoding, true);
				}
			}

			if (encoding != null) {
				return encoding;
			}
			else {
				return super.getRemoteEncoding();
			}
		}
		catch (SystemMessageException e) {
			SystemBasePlugin.logMessage(e.getSystemMessage());
		}

		return super.getRemoteEncoding();
	}

	/**
	 * Defers to the file service.  The method is basically another way to do download.
	 * @see org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileSubSystem#getInputStream(java.lang.String, java.lang.String, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public InputStream getInputStream(String remoteParent, String remoteFile, boolean isBinary, IProgressMonitor monitor) throws SystemMessageException {
		return new FileSubSystemInputStream(getFileService().getInputStream(remoteParent, remoteFile, isBinary, monitor), remoteParent, remoteFile, this);
	}

	/**
	 * Defers to the file service.  The method is basically another way to do upload.
	 * @see org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileSubSystem#getOutputStream(java.lang.String, java.lang.String, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public OutputStream getOutputStream(String remoteParent, String remoteFile, boolean isBinary, IProgressMonitor monitor) throws SystemMessageException {
		return new FileSubSystemOutputStream(getFileService().getOutputStream(remoteParent, remoteFile, isBinary, monitor), remoteParent, remoteFile, this);
	}

	public OutputStream getOutputStream(String remoteParent, String remoteFile, int options, IProgressMonitor monitor) throws SystemMessageException {
		return new FileSubSystemOutputStream(getFileService().getOutputStream(remoteParent, remoteFile, options, monitor), remoteParent, remoteFile, this);
	}

	/**
	 * Defers to the file service.
	 */
	public boolean supportsEncodingConversion(){
		return getFileService().supportsEncodingConversion();
	}

}