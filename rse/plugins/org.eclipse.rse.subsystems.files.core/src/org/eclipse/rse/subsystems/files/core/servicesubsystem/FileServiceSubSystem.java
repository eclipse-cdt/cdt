/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
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
 * David Dykstal (IBM) - [230821] fix IRemoteFileSubSystem API to be consistent with IFileService
 * Martin Oberhuber (Wind River) - [234038] Mark IRemoteFile stale when changing permissions
 * Martin Oberhuber (Wind River) - [235360][ftp][ssh][local] Return proper "Root" IHostFile
 * David McKnight   (IBM)        - [233461] [Refresh][api] Refresh expanded folder under filter refreshes Filter
 * Martin Oberhuber (Wind River) - [240704] Protect against illegal API use of getRemoteFileObject() with relative path as name
 * Martin Oberhuber (Wind River) - [234026] Clarify IFileService#createFolder() Javadocs
 * David McKnight   (IBM)        - [272882] [api] Handle exceptions in IService.initService()
 * David McKnight   (IBM)        - [244041] [files] Renaming a file looses Encoding property
 * David McKnight   (IBM)        - [320713] [dstore] xml file transfer error on zOS
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
import org.eclipse.core.runtime.Status;
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
import org.eclipse.rse.subsystems.files.core.model.ISystemFileTransferModeRegistry;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
import org.eclipse.rse.subsystems.files.core.subsystems.IHostFileToRemoteFileAdapter;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileContext;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileContext;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileEncodingManager;
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
		// Consistency would be totally messed up if folderOrFileName were a relative path
		// Because IHostFiles would be incorrectly generated, getParent() would return wrong results etc
		assert folderOrFileName.indexOf(getSeparator())<0;
		// for bug 207095, implicit connect if the connection is not connected
		checkIsConnected(monitor);

		String fullPath = parent.getAbsolutePath() + getSeparator() + folderOrFileName;
		IRemoteFile file = getCachedRemoteFile(fullPath);
		if (file != null && !file.isStale())
		{
			return file;
		}
		// Fallback in case of incorrect API usage
		// TODO remove this in next release for Performance,
		// since it is just for bad clients using the API incorrectly
		if (folderOrFileName.indexOf(getSeparator()) >= 0) {
			try {
				throw new IllegalArgumentException("getRemoteFileObject: folderOrFileName must not be a relative path"); //$NON-NLS-1$
			} catch (IllegalArgumentException e) {
				Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Illegal API use: " + e.getLocalizedMessage(), e)); //$NON-NLS-1$
			}
			return getRemoteFileObject(fullPath, monitor);
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
		if (!".".equals(userHome.getParentPath())) //$NON-NLS-1$
		{
			//note: parent path can be "null" if userHome is a Root
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

	/**
     * Return a list of children from the given parent path in service layer format.
     * Was unified from the previous methods getFolders(), getFiles() and getFilesAndFolders()
     * in RSE 3.0
     * @since 3.0
     */
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
	 * {@inheritDoc}
	 * @since 3.0
	 */
	public IRemoteFile[] listMultiple(IRemoteFile[] parents, String[] fileNameFilters, int[] fileTypes,  IProgressMonitor monitor) throws SystemMessageException
	{
		String[] parentPaths = new String[parents.length];
		for (int i = 0; i < parents.length; i++)
		{
			parentPaths[i] = parents[i].getAbsolutePath();
		}

		List hostFiles = new ArrayList(10);

		// query children via the service
		getFileService().listMultiple(parentPaths, fileNameFilters, fileTypes, hostFiles, monitor);
		RemoteFileContext context = getDefaultContext();

		IHostFile[] results = (IHostFile[])hostFiles.toArray(new IHostFile[hostFiles.size()]);

		// convert the IHostFiles into AbstractRemoteFiles
		AbstractRemoteFile[] farr = getHostFileToRemoteFileAdapter().convertToRemoteFiles(this, context, null, results);

		// cache the results corresponding to each parent under each parent
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

			// update the parent with it's latest properties
			// null is passed for the second argument because we currently don't get the parent in our results query
			updateRemoteFile(parent, null, monitor);

			if (underParent.size() > 0)
			{
				Object[] qresults = underParent.toArray();
				parent.setContents(RemoteChildrenContentsType.getInstance(), filter, qresults);
			}
		}

		return farr;
	}


	/**
	 * {@inheritDoc}
	 * @since 3.0
	 */
	public IRemoteFile[] listMultiple(IRemoteFile[] parents, String[] fileNameFilters, int fileType,  IProgressMonitor monitor) throws SystemMessageException
	{
		String[] parentPaths = new String[parents.length];
		for (int i = 0; i < parents.length; i++)
		{
			parentPaths[i] = parents[i].getAbsolutePath();
		}


		List hostFiles = new ArrayList(10);
		// query children via the service
		getFileService().listMultiple(parentPaths, fileNameFilters, fileType, hostFiles, monitor);
		RemoteFileContext context = getDefaultContext();

		IHostFile[] results = (IHostFile[])hostFiles.toArray(new IHostFile[hostFiles.size()]);

		// convert the IHostFiles into AbstractRemoteFiles
		AbstractRemoteFile[] farr = getHostFileToRemoteFileAdapter().convertToRemoteFiles(this, context, null, results);

		// cache the results corresponding to each parent under each parent
		for (int i = 0; i < parents.length; i++)
		{
			IRemoteFile parent = parents[i];
			String parentPath = parentPaths[i];
			String filter = fileNameFilters[i];

			List underParent = new ArrayList();
			// what files are under this one?
			for (int j = 0; j < farr.length; j++)
			{
				AbstractRemoteFile child = farr[j];
				String childParentPath = child.getParentPath();

				if (parentPath.equals(childParentPath))
				{
					underParent.add(child);
				}
			}

			// update the parent with it's latest properties
			// null is passed for the second argument because we currently don't get the parent in our results query
			updateRemoteFile(parent, null, monitor);

			if (underParent.size() > 0)
			{
				Object[] qresults = underParent.toArray();
				parent.setContents(RemoteChildrenContentsType.getInstance(), filter, qresults);
			}
		}

		return farr;
	}


	/**
	 * {@inheritDoc}
	 * @since 3.0
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

		// query children of the parent
		IHostFile[] results = internalList(parentPath, fileNameFilter, fileType, monitor);

		// Bug 233461: update the parent with it's latest properties
		// null is passed for the second argument because we currently don't get the parent in our results query
		updateRemoteFile(parent, null, monitor);

		// convert the IHostFiles to AbstractRemoteFile[]
		AbstractRemoteFile[] farr = getHostFileToRemoteFileAdapter().convertToRemoteFiles(this, context, parent, results);
		if (parent != null)
			parent.setContents(RemoteChildrenContentsType.getInstance(), fileNameFilter, farr);
		return farr;
	}



	private void updateRemoteFile(IRemoteFile parent, IHostFile newHostParent, IProgressMonitor monitor) throws SystemMessageException
	{
		// now newHostParent file passed in so we'll assume it wasn't returned and explicitly get it
		if (newHostParent == null){
			String parentParentPath = parent.getParentPath();
			if (parentParentPath == null){
				parentParentPath = ""; //$NON-NLS-1$
			}
			newHostParent = getFileService().getFile(parentParentPath, parent.getName(), monitor);
		}

		if (newHostParent != null){
			IHostFile oldHostParent = parent.getHostFile();
			if (!newHostParent.equals(oldHostParent)){
				((AbstractRemoteFile)parent).setHostFile(newHostParent);
				parent.markStale(false);
			}
		}
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
		ISystemFileTransferModeRegistry reg = RemoteFileUtility.getSystemFileTransferModeRegistry();
		return reg.isBinary(remotePath) || reg.isXML(remotePath);
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

	/**
	 * {@inheritDoc}
	 * @since 3.0
	 */
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

	/**
	 * {@inheritDoc}
	 * @since 3.0
	 */
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

	/**
	 * {@inheritDoc}
	 * @since 3.0
	 */
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

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.0 returns void
	 */
	public void copy(IRemoteFile sourceFolderOrFile, IRemoteFile targetFolder, String newName, IProgressMonitor monitor) throws SystemMessageException
	{
		IFileService service = getFileService();
		service.copy(sourceFolderOrFile.getParentPath(), sourceFolderOrFile.getName(), targetFolder.getAbsolutePath(), newName, monitor);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.0 returns void
	 */
	public void copyBatch(IRemoteFile[] sourceFolderOrFiles, IRemoteFile targetFolder, IProgressMonitor monitor) throws SystemMessageException
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
		try {
			//As per IFileService#createFolder() API Docs, Services *may* create parent folders.
			//Therefore, first try this shortcut before actually iterating to create parents.
			return createFolder(folderToCreate, monitor);
		} catch (SystemMessageException e) {
			//Parent did not exist? Need to create parent folders on this Service
			IFileService service = getFileService();
			List parents = new ArrayList();
			IRemoteFile parent = folderToCreate;
			while (!parent.isRoot()) {
				parent = parent.getParentRemoteFile();
				IHostFile parentFile = service.getFile(parent.getParentPath(), parent.getName(), monitor);
				if (parentFile.exists()) {
					//Update cache with newest info, since we just got it
					getHostFileToRemoteFileAdapter().convertToRemoteFile(this, getDefaultContext(), parent.getParentRemoteFile(), parentFile);
					break;
				} else {
					parents.add(parent);
				}
			}
			if (parents.size()==0) {
				//No parents missing -- throw original exception
				throw e;
			}
			for (int i=parents.size()-1; i>=0; i--) {
				parent = (IRemoteFile)parents.get(i);
				// Remote side will change due to createFolder, so mark it stale
				parent.markStale(true, true);
				// Create new folder and cache the contents
				createFolder(parent, monitor);
			}
			return createFolder(folderToCreate, monitor);
		}
	}

	/**
	 * {@inheritDoc}
	 * @since 3.0 returns void
	 */
	public void delete(IRemoteFile folderOrFile, IProgressMonitor monitor) throws SystemMessageException
	{
		IFileService service = getFileService();
		String parent = folderOrFile.getParentPath();
		String name = folderOrFile.getName();
		try {
			service.delete(parent, name, monitor);
		} finally {
			folderOrFile.markStale(true);
		}
	}

	/**
	 * {@inheritDoc}
	 * @since 3.0 returns void
	 */
	public void deleteBatch(IRemoteFile[] folderOrFiles, IProgressMonitor monitor) throws SystemMessageException
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
	}

	/**
	 * {@inheritDoc}
	 * @since 3.0 returns void
	 */
	public void rename(IRemoteFile folderOrFile, String newName, IProgressMonitor monitor) throws SystemMessageException
	{
		removeCachedRemoteFile(folderOrFile);
		IFileService service = getFileService();
		String srcParent = folderOrFile.getParentPath();
		String oldName = folderOrFile.getName();
		String newPath = srcParent + folderOrFile.getSeparator() + newName;

		String originalEncoding = folderOrFile.getEncoding();

		service.rename(srcParent, oldName, newName, monitor);
		folderOrFile.getHostFile().renameTo(newPath);

		// for bug 244041 - need to set encoding to be the same as the original file
		RemoteFileEncodingManager mgr = RemoteFileEncodingManager.getInstance();
		String renamedEncoding = folderOrFile.getEncoding();
		if (!renamedEncoding.equals(originalEncoding)){
			 mgr.setEncoding(getHostName(), newPath, originalEncoding);
		}



	}

	/**
	 * {@inheritDoc}
	 * @since 3.0 returns void
	 */
	public void move(IRemoteFile sourceFolderOrFile, IRemoteFile targetFolder, String newName, IProgressMonitor monitor) throws SystemMessageException
	{
		IFileService service = getFileService();
		String srcParent = sourceFolderOrFile.getParentPath();
		String srcName = sourceFolderOrFile.getName();
		String tgtParent = targetFolder.getAbsolutePath();
		removeCachedRemoteFile(sourceFolderOrFile);

		String newPath = tgtParent + targetFolder.getSeparator() + newName;
		String originalEncoding = sourceFolderOrFile.getEncoding();

		try {
			service.move(srcParent, srcName, tgtParent, newName, monitor);
		} finally {
			sourceFolderOrFile.markStale(true);
			targetFolder.markStale(true);
		}

		// for bug 244041 - need to set encoding to be the same as the original file
		RemoteFileEncodingManager mgr = RemoteFileEncodingManager.getInstance();
		IRemoteFile movedFile = getRemoteFileObject(targetFolder, newName, monitor);
		if (movedFile != null && !movedFile.getEncoding().equals(originalEncoding)){
			 mgr.setEncoding(getHostName(), newPath, originalEncoding);
		}
	}

	/**
	 * {@inheritDoc}
	 * @since 3.0 returns void
	 */
	public void setLastModified(IRemoteFile folderOrFile, long newDate, IProgressMonitor monitor) throws SystemMessageException
	{
		String name = folderOrFile.getName();
		String parent = folderOrFile.getParentPath();
		//mark stale regardless of whether the call succeeds or not
		folderOrFile.markStale(true);
		_hostFileService.setLastModified(parent, name, newDate, monitor);
	}

	/**
	 * {@inheritDoc}
	 * @since 3.0 returns void
	 */
	public void setReadOnly(IRemoteFile folderOrFile, boolean readOnly, IProgressMonitor monitor) throws SystemMessageException
	{
		String name = folderOrFile.getName();
		String parent = folderOrFile.getParentPath();
		//mark stale regardless of whether the call succeeds or not
		folderOrFile.markStale(true);
		_hostFileService.setReadOnly(parent, name, readOnly, monitor);
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

	/**
	 * {@inheritDoc}
	 * @see IRemoteFileSubSystem#cancelSearch(IHostSearchResultConfiguration)
	 */
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

	public void initializeSubSystem(IProgressMonitor monitor) throws SystemMessageException
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
	 * {@inheritDoc} Defers to the file service. The method is basically another
	 * way to do download.
	 *
	 * @see org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileSubSystem#getInputStream(java.lang.String,
	 *      java.lang.String, boolean,
	 *      org.eclipse.core.runtime.IProgressMonitor)
	 */
	public InputStream getInputStream(String remoteParent, String remoteFile, boolean isBinary, IProgressMonitor monitor) throws SystemMessageException {
		return new FileSubSystemInputStream(getFileService().getInputStream(remoteParent, remoteFile, isBinary, monitor), remoteParent, remoteFile, this);
	}

	/**
	 * {@inheritDoc}
	 * @deprecated Use
	 *             {@link #getOutputStream(String, String, int, IProgressMonitor)}
	 *             instead
	 */
	public OutputStream getOutputStream(String remoteParent, String remoteFile, boolean isBinary, IProgressMonitor monitor) throws SystemMessageException {
		return new FileSubSystemOutputStream(getFileService().getOutputStream(remoteParent, remoteFile, isBinary, monitor), remoteParent, remoteFile, this);
	}

	/**
	 * {@inheritDoc}
	 * @since 3.0 uses int options argument
	 */
	public OutputStream getOutputStream(String remoteParent, String remoteFile, int options, IProgressMonitor monitor) throws SystemMessageException {
		return new FileSubSystemOutputStream(getFileService().getOutputStream(remoteParent, remoteFile, options, monitor), remoteParent, remoteFile, this);
	}

	/**
	 * {@inheritDoc} Defers to the file service.
	 * @since 3.0
	 */
	public boolean supportsEncodingConversion(){
		return getFileService().supportsEncodingConversion();
	}

}