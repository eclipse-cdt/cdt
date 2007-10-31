/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
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
 *******************************************************************************/

package org.eclipse.rse.subsystems.files.core.servicesubsystem;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemRemoteChangeEvents;
import org.eclipse.rse.core.events.SystemRemoteChangeEvent;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.IServiceSubSystemConfiguration;
import org.eclipse.rse.core.subsystems.RemoteChildrenContentsType;
import org.eclipse.rse.services.clientserver.PathUtility;
import org.eclipse.rse.services.clientserver.SystemEncodingUtil;
import org.eclipse.rse.services.clientserver.SystemSearchString;
import org.eclipse.rse.services.clientserver.archiveutils.AbsoluteVirtualPath;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.services.search.IHostSearchResultConfiguration;
import org.eclipse.rse.services.search.IHostSearchResultSet;
import org.eclipse.rse.services.search.ISearchService;
import org.eclipse.rse.subsystems.files.core.ILanguageUtilityFactory;
import org.eclipse.rse.subsystems.files.core.model.SystemFileTransferModeRegistry;
import org.eclipse.rse.subsystems.files.core.subsystems.IHostFileToRemoteFileAdapter;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileContext;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileContext;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileSubSystem;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;

public final class FileServiceSubSystem extends RemoteFileSubSystem implements IFileServiceSubSystem 
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
					SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_ERROR_UNEXPECTED);
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
		IHostFile[] nodes = getFileService().getFileMulti(parentPaths, names, monitor);
		return getHostFileToRemoteFileAdapter().convertToRemoteFiles(this, context, null, nodes); 		
	}


	/**
	 * Return a list of remote folders and files in the given folder. Only file names are subsettable
	 * by the given file name filter. It can be null for no subsetting.
	 * @param parents The parent folders to list folders and files in
	 * @param fileNameFilters The name patterns to subset the file list by, or null to return all files.
	 * @param fileType - indicates whether to query files, folders, both or some other type
	 * @param monitor the progress monitor
	 */
	public IRemoteFile[] listMulti(IRemoteFile[] parents, String[] fileNameFilters, int fileType,  IProgressMonitor monitor) throws SystemMessageException
	{
		String[] parentPaths = new String[parents.length];
		for (int i = 0; i < parents.length; i++)
		{
			parentPaths[i] = parents[i].getAbsolutePath();
		}
		
		IHostFile[] results = getFileService().listMulti(parentPaths, fileNameFilters, fileType, monitor);
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
	 * Return a list of remote folders and files in the given folder. 
	 * <p>
	 * The files part of the list is subsetted by the given file name filter. 
	 * It can be null for no subsetting.
	 * This version is called by RemoteFileSubSystemImpl's resolveFilterString(s).
	 * @param parent The parent folder to list folders and files in
	 * @param fileNameFilter The name pattern to subset the file list by, or null to return all files.
	 * @param context The holder of state information
	 * @param fileType the type of file to query
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
			SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_FOLDER_UNREADABLE).makeSubstitution(parentPath);
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
	
	protected boolean isBinary(String localEncoding, String hostEncoding, String remotePath)
	{
		return SystemFileTransferModeRegistry.getInstance().isBinary(remotePath) ||
		       SystemEncodingUtil.getInstance().isXML(remotePath);	
	}
	
	protected boolean isBinary(IRemoteFile source)
	{
		// if binary or XML file, transfer in binary mode
		if (source.isBinary() || SystemEncodingUtil.getInstance().isXML(source.getAbsolutePath()))
		{
			return true;
		}
		else
		{
			return false;
		}
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

		if (!destination.canWrite())
		{
			SystemMessage msg = RSEUIPlugin.getPluginMessage("RSEF5003").makeSubstitution(remoteFileName, getHostName()); //$NON-NLS-1$
			throw new SystemMessageException(msg);
		}
		getFileService().upload(new File(source), remoteParentPath, remoteFileName, isBinary, encoding, hostEncoding, monitor);
		
		// notify that the file was uploaded
		ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
		sr.fireEvent(new SystemRemoteChangeEvent(ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_UPLOADED, destination, destination.getParentRemoteFile(), this));
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem#copy(org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile, org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile, java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public boolean copy(IRemoteFile sourceFolderOrFile, IRemoteFile targetFolder, String newName, IProgressMonitor monitor) throws SystemMessageException 
	{
		IFileService service = getFileService();
		return service.copy(sourceFolderOrFile.getParentPath(), sourceFolderOrFile.getName(), targetFolder.getAbsolutePath(), newName, monitor);
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
		return service.copyBatch(sourceParents, sourceNames, targetFolder.getAbsolutePath(), monitor);
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
		boolean result = service.delete(parent, name, monitor);
		folderOrFile.markStale(true);
		return result;
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
		return service.deleteBatch(parents, names, monitor);
	}

	public boolean rename(IRemoteFile folderOrFile, String newName, IProgressMonitor monitor) throws SystemMessageException 
	{
		removeCachedRemoteFile(folderOrFile);
		IFileService service = getFileService();
		String srcParent = folderOrFile.getParentPath();
		String oldName = folderOrFile.getName();
		String newPath = srcParent + folderOrFile.getSeparator() + newName;
		boolean result = service.rename(srcParent, oldName, newName, monitor);
		folderOrFile.getHostFile().renameTo(newPath);
		return result;
	}
	
	public boolean move(IRemoteFile sourceFolderOrFile, IRemoteFile targetFolder, String newName, IProgressMonitor monitor) throws SystemMessageException 
	{
		IFileService service = getFileService();
		String srcParent = sourceFolderOrFile.getParentPath();
		String srcName = sourceFolderOrFile.getName();
		String tgtParent = targetFolder.getAbsolutePath();
		removeCachedRemoteFile(sourceFolderOrFile);
		boolean result = service.move(srcParent, srcName, tgtParent, newName, monitor);
		sourceFolderOrFile.markStale(true);
		targetFolder.markStale(true);
		return result;
	}

	public boolean setLastModified(IRemoteFile folderOrFile, long newDate, IProgressMonitor monitor) throws SystemMessageException 
	{
		String name = folderOrFile.getName();
		String parent = folderOrFile.getParentPath();
		return _hostFileService.setLastModified(parent, name, newDate, monitor);
	}

	public boolean setReadOnly(IRemoteFile folderOrFile, boolean readOnly, IProgressMonitor monitor) throws SystemMessageException 
	{
		String name = folderOrFile.getName();
		String parent = folderOrFile.getParentPath();
		return _hostFileService.setReadOnly(parent, name, readOnly, monitor);
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



	/**
	 * Switch to use another protocol
	 */
	public void switchServiceFactory(IServiceSubSystemConfiguration fact)
	{
		if (fact != getSubSystemConfiguration() && fact instanceof IFileServiceSubSystemConfiguration)
		{
			IFileServiceSubSystemConfiguration factory = (IFileServiceSubSystemConfiguration)fact;
			try
			{
				_cachedRemoteFiles.clear();
				disconnect();
			}
			catch (Exception e)
			{	
			}
			
			_languageUtilityFactory = null;
			IHost host = getHost();
			setSubSystemConfiguration(factory);

			IConnectorService oldConnectorService = getConnectorService();			
			oldConnectorService.deregisterSubSystem(this);
			
			IConnectorService newConnectorService = factory.getConnectorService(host);
			setConnectorService(newConnectorService);
			
			oldConnectorService.commit();
			newConnectorService.commit();
		
			setName(factory.getName());
			setFileService(factory.getFileService(host));	
			setHostFileToRemoteFileAdapter(factory.getHostFileAdapter());
			setSearchService(factory.getSearchService(host));
			
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
		super.uninitializeSubSystem(monitor);
		getFileService().uninitService(monitor);
		_userHome = null;
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
	
	/**
	 * Return the array of IRemoteFile instances, matching the given pattern, 
	 * that are contained in the given folder.
	 * This version is called by RemoteFileSubSystemImpl's resolveFilterString(s)
	 * @param parent The parent folder to list files in
	 * @param fileNameFilter The name pattern to subset the list by, or null to return all files.
	 * @param context The holder of state information
	 * 
	 * @deprecated use list
	 */
	public IRemoteFile[] listFiles(IRemoteFile parent, String fileNameFilter, IRemoteFileContext context,IProgressMonitor monitor) throws SystemMessageException
	{
		String parentPath = null;
		if (parent != null) {
			parentPath = parent.getAbsolutePath();
		} else {
			parentPath = "/"; //$NON-NLS-1$
		}
		
		if (parent != null && !parent.canRead())
		{
			SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_FOLDER_UNREADABLE).makeSubstitution(parentPath);
			throw new SystemMessageException(msg);
		}
		
		IHostFile[] results = null;
		try
		{
			results = getFiles(parentPath, fileNameFilter, monitor);
		}
		catch (SystemMessageException e)
		{
			
		}
		
		IRemoteFile[] farr = getHostFileToRemoteFileAdapter().convertToRemoteFiles(this, context, parent, results);
		
		if (parent != null)
			parent.setContents(RemoteChildrenContentsType.getInstance(), fileNameFilter, farr);
		return farr;
	}

	/**
	 * Return a subsetted list of remote folders in the given parent folder on the remote system.
	 * This version is called by RemoteFileSubSystemImpl's resolveFilterString(s)
	 * @param parent The parent folder to list folders in
	 * @param fileNameFilter The name pattern for subsetting the file list when this folder is subsequently expanded
	 * @param context The holder of state information
	 * 
	 * @deprecated use list
	 */
	public IRemoteFile[] listFolders(IRemoteFile parent, String fileNameFilter, IRemoteFileContext context, IProgressMonitor monitor) throws SystemMessageException
	{
		String parentPath = null;
		if (parent != null) {
			parentPath = parent.getAbsolutePath();
		} else {
			parentPath = "/"; //$NON-NLS-1$
		}
		
		if (parent != null && !parent.canRead())
		{
			SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_FOLDER_UNREADABLE).makeSubstitution(parentPath);
			throw new SystemMessageException(msg);
		}
		
		IHostFile[] results = null;
		try
		{
			results = getFolders(parentPath, fileNameFilter, monitor);
		}
		catch (SystemMessageException e)
		{			
		}
		
		IRemoteFile[] farr = getHostFileToRemoteFileAdapter().convertToRemoteFiles(this, context, parent, results);
		if (parent != null)
			parent.setContents(RemoteChildrenContentsType.getInstance(), fileNameFilter, farr);
		return farr;
	}
	
	/**
	 * Return a list of remote folders and files in the given folder. 
	 * <p>
	 * The files part of the list is subsetted by the given file name filter. 
	 * It can be null for no subsetting.
	 * This version is called by RemoteFileSubSystemImpl's resolveFilterString(s).
	 * @param parent The parent folder to list folders and files in
	 * @param fileNameFilter The name pattern to subset the file list by, or null to return all files.
	 * @param context The holder of state information
	 * 
	 * @deprecated use list
	 */
	public IRemoteFile[] listFoldersAndFiles(IRemoteFile parent, String fileNameFilter, IRemoteFileContext context, IProgressMonitor monitor) throws SystemMessageException
	{
		String parentPath = null;
		if (parent != null) {
			parentPath = parent.getAbsolutePath();
		} else { 
			parentPath = "/"; //$NON-NLS-1$
		}
		
		if (parent != null && !parent.canRead())
		{
			SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_FOLDER_UNREADABLE).makeSubstitution(parentPath);
			throw new SystemMessageException(msg);
		}
		
		IHostFile[] results = getFilesAndFolders(parentPath, fileNameFilter, monitor); 

		IRemoteFile[] farr = getHostFileToRemoteFileAdapter().convertToRemoteFiles(this, context, parent, results);
		if (parent != null)
			parent.setContents(RemoteChildrenContentsType.getInstance(), fileNameFilter, farr);
		return farr;
	}
	
	/**
	 * @deprecated
	 */
	protected IHostFile[] getFolders(String parentPath, String fileNameFilter, IProgressMonitor monitor) throws SystemMessageException
	{
		return getFileService().getFolders(parentPath, fileNameFilter, monitor);
	}
	
	/**
	 * @deprecated
	 */
	protected IHostFile[] getFiles(String parentPath, String fileNameFilter, IProgressMonitor monitor) throws SystemMessageException
	{
		return getFileService().getFiles(parentPath, fileNameFilter, monitor);
	}
	
	/**
	 * @deprecated
	 */
	protected IHostFile[] getFilesAndFolders(String parentPath, String fileNameFilter, IProgressMonitor monitor) throws SystemMessageException
	{
		return getFileService().getFilesAndFolders(parentPath, fileNameFilter, monitor);
	}
}