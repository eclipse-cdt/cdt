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

package org.eclipse.rse.subsystems.files.core.servicesubsystem;


import java.io.File;
import java.io.InputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.servicesubsystem.IServiceSubSystemConfiguration;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.RemoteChildrenContentsType;
import org.eclipse.rse.internal.subsystems.files.core.ILanguageUtilityFactory;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.services.clientserver.PathUtility;
import org.eclipse.rse.services.clientserver.SystemEncodingUtil;
import org.eclipse.rse.services.clientserver.SystemSearchString;
import org.eclipse.rse.services.clientserver.archiveutils.AbsoluteVirtualPath;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.services.search.IHostSearchResultConfiguration;
import org.eclipse.rse.services.search.IHostSearchResultSet;
import org.eclipse.rse.services.search.ISearchService;
import org.eclipse.rse.subsystems.files.core.model.SystemFileTransferModeRegistry;
import org.eclipse.rse.subsystems.files.core.subsystems.IHostFileToRemoteFileAdapter;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileContext;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileIOException;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileSecurityException;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFolderNotEmptyException;




public final class FileServiceSubSystem extends RemoteFileSubSystem implements IFileServiceSubSystem 
{
	protected ILanguageUtilityFactory _languageUtilityFactory;
	protected IFileService _hostFileService;
	protected ISearchService _hostSearchService;
	protected IHostFileToRemoteFileAdapter _hostFileToRemoteFileAdapter;
	
	public FileServiceSubSystem(IHost host, IConnectorService connectorService, IFileService hostFileService, IHostFileToRemoteFileAdapter fileAdapter, ISearchService searchService)
	{
		super(host, connectorService);
		_hostFileService = hostFileService;
		_hostFileToRemoteFileAdapter = fileAdapter;
		_hostSearchService = searchService;
		
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
	
	
	
	public String getRemoteEncoding()
	{
		return System.getProperty("file.encoding");
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
	 * @return an IRemoteFile object for the file.
	 * @see IRemoteFile
	 */
	public IRemoteFile getRemoteFileObject(IRemoteFile parent, String folderOrFileName) throws SystemMessageException 
	{
		String fullPath = parent.getAbsolutePath() + getSeparator() + folderOrFileName;
		IRemoteFile file = getCachedRemoteFile(fullPath);
		if (file != null && !file.isStale()) 
		{
			return file;
		}
		
		IHostFile node = getFile(null, parent.getAbsolutePath(), folderOrFileName);
		return getHostFileToRemoteFileAdapter().convertToRemoteFile(this, getDefaultContext(), parent, node);
	}

	/**
	 * Constructs and returns an IRemoteFile object given a fully-qualified 
	 * file or folder name.
	 * @param folderOrFileName Fully qualified folder or file name
	 * @return The constructed IRemoteFile
	 * @see IRemoteFile
	 */
	public IRemoteFile getRemoteFileObject(String folderOrFileName) throws SystemMessageException 
	{
		String fofName = ArchiveHandlerManager.cleanUpVirtualPath(folderOrFileName);
		IRemoteFile file = getCachedRemoteFile(fofName);
		if (file != null && !file.isStale()) {
			return file;
		}
		
		if (fofName.endsWith(ArchiveHandlerManager.VIRTUAL_SEPARATOR))
		{
			fofName = fofName.substring(0, fofName.length() - ArchiveHandlerManager.VIRTUAL_SEPARATOR.length());
		}
	

		int j = fofName.indexOf(ArchiveHandlerManager.VIRTUAL_SEPARATOR);
		if (j == -1)
		{
			if (fofName.equals("/")) 
			{
				try
				{
					return listRoots()[0];
				}
				catch (Exception e)
				{
					
				}
			} 
			
			if (fofName.equals(".")) {
				return getUserHome();
			}

			String sep = PathUtility.getSeparator(folderOrFileName);
			if (fofName.endsWith(sep))
			{
				fofName = fofName.substring(0, fofName.length() - sep.length());
			}

			if (fofName.endsWith(":"))
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
			
			
				if (parentPath.length() == 0) parentPath = "/";
				String name = fofName.substring(lastSep + 1, fofName.length());
			
				IHostFile node = getFile(null, parentPath, name);
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
			IHostFile node = getFile(null, avp.getPath(), avp.getName());
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
		IRemoteFile root = getCachedRemoteFile(".");
		if (root != null && !root.isStale()) {
			return root;
		}
		IHostFile userHome = getFileService().getUserHome();
		IRemoteFile parent = null;
		if (!userHome.getParentPath().equals("."))
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
		cacheRemoteFile(root, ".");
		return root;
	}
	
	protected IHostFile[] getFolders(IProgressMonitor monitor, String parentPath, String fileNameFilter) throws SystemMessageException
	{
		return getFileService().getFolders(monitor, parentPath, fileNameFilter);
	}
	
	protected IHostFile[] getFiles(IProgressMonitor monitor, String parentPath, String fileNameFilter) throws SystemMessageException
	{
		return getFileService().getFiles(monitor, parentPath, fileNameFilter);
	}
	
	protected IHostFile[] getFilesAndFolders(IProgressMonitor monitor, String parentPath, String fileNameFilter) throws SystemMessageException
	{
		return getFileService().getFilesAndFolders(monitor, parentPath, fileNameFilter);
	}
	
	protected IHostFile getFile(IProgressMonitor monitor, String parentPath, String fileName) throws SystemMessageException
	{
		return getFileService().getFile(monitor, parentPath, fileName);
	}
	
	protected IHostFile[] getRoots(IProgressMonitor monitor) throws InterruptedException, SystemMessageException
	{
		return getFileService().getRoots(monitor);
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
	 */
	public IRemoteFile[] listFoldersAndFiles(IRemoteFile parent, String fileNameFilter, IRemoteFileContext context) 
	{
		String parentPath = null;
		if (parent != null) {
			parentPath = parent.getAbsolutePath();
		} else { 
			parentPath = "/";
		}
		IHostFile[] results = null;
		try
		{
			results = getFilesAndFolders(null, parentPath, fileNameFilter);
		}
		catch (SystemMessageException e)
		{
			
		}
		IRemoteFile[] farr = getHostFileToRemoteFileAdapter().convertToRemoteFiles(this, context, parent, results);
		parent.setContents(RemoteChildrenContentsType.getInstance(), fileNameFilter, farr);
		return farr;
	}

	/**
	 * Return the array of IRemoteFile instances, matching the given pattern, 
	 * that are contained in the given folder.
	 * This version is called by RemoteFileSubSystemImpl's resolveFilterString(s)
	 * @param parent The parent folder to list files in
	 * @param fileNameFilter The name pattern to subset the list by, or null to return all files.
	 * @param context The holder of state information
	 */
	public IRemoteFile[] listFiles(IRemoteFile parent, String fileNameFilter, IRemoteFileContext context) 
	{
		String parentPath = null;
		if (parent != null) {
			parentPath = parent.getAbsolutePath();
		} else {
			parentPath = "/";
		}
		IHostFile[] results = null;
		try
		{
			results = getFiles(null, parentPath, fileNameFilter);
		}
		catch (SystemMessageException e)
		{
			
		}
		IRemoteFile[] farr = getHostFileToRemoteFileAdapter().convertToRemoteFiles(this, context, parent, results);
		parent.setContents(RemoteChildrenContentsType.getInstance(), fileNameFilter, farr);
		return farr;
	}

	/**
	 * Return a subsetted list of remote folders in the given parent folder on the remote system.
	 * This version is called by RemoteFileSubSystemImpl's resolveFilterString(s)
	 * @param parent The parent folder to list folders in
	 * @param fileNameFilter The name pattern for subsetting the file list when this folder is subsequently expanded
	 * @param context The holder of state information
	 */
	public IRemoteFile[] listFolders(IRemoteFile parent, String fileNameFilter, IRemoteFileContext context) 
	{
		String parentPath = null;
		if (parent != null) {
			parentPath = parent.getAbsolutePath();
		} else {
			parentPath = "/";
		}
		IHostFile[] results = null;
		try
		{
			results = getFolders(null, parentPath, fileNameFilter);
		}
		catch (SystemMessageException e)
		{			
		}
		IRemoteFile[] farr = getHostFileToRemoteFileAdapter().convertToRemoteFiles(this, context, parent, results);
		parent.setContents(RemoteChildrenContentsType.getInstance(), fileNameFilter, farr);
		return farr;
	}
	
	public IRemoteFile[] listRoots(IRemoteFileContext context) throws InterruptedException 
	{
		IHostFile[] roots = null;
		try
		{
			roots = getRoots(null);
		}
		catch (SystemMessageException e)
		{
			
		}
		IRemoteFile[] results = getHostFileToRemoteFileAdapter().convertToRemoteFiles(this, context, null, roots);
		return results;
	}
	


	/**
	 * Get the remote file and save it locally. The file is saved in the encoding
	 * specified.
	 * @param source remote file that represents the file to be obtained
	 * @param destination the absolute path of the local file
	 * @param encoding the encoding of the local file
	 * @param monitor progress monitor
	 */
	public void download(IRemoteFile file, String localpath, String encoding, IProgressMonitor monitor) 
	{
		String parentPath = file.getParentPath();
		File localFile = new File(localpath);
		try
		{
			getFileService().download(monitor, parentPath, file.getName(), localFile, isBinary(file), encoding);
		}
		catch (SystemMessageException e)
		{
			// FIXME: Display message
		}
	}
	
	protected boolean isBinary(String localEncoding, String hostEncoding, String remotePath)
	{
		boolean isText = !hostEncoding.equals(localEncoding) && 
		SystemFileTransferModeRegistry.getDefault().isText(remotePath) && 
		!SystemEncodingUtil.getInstance().isXML(remotePath) ;
		return !isText;
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

	/**
	 * Put the local copy of the remote file back to the remote location. The file
	 * is assumed to be in the encoding of the local operating system
	 * @param source the absolute path of the local copy
	 * @param srcEncoding The encoding of the local copy
	 * @param remotePath remote file that represents the file on the server
	 * @param rmtEncoding The encoding of the remote file.
	 */
	public void upload(String source, String srcEncoding, String remotePath, String rmtEncoding, IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException {
		int slashIndex = remotePath.lastIndexOf(getSeparator());
		if (slashIndex > -1) {
			String remoteParentPath = remotePath.substring(0, slashIndex);
			String remoteFileName = remotePath.substring(slashIndex + 1, remotePath.length());
			boolean isBinary = isBinary(srcEncoding, rmtEncoding, remotePath);
			try
			{
				getFileService().upload(monitor, new File(source), remoteParentPath, remoteFileName, isBinary, srcEncoding, rmtEncoding);
			}
			catch (SystemMessageException e)
			{
				
			}
		}
	}
	
	/**
	 * Put the local copy of the remote file back to the remote location. The file
	 * is assumed to be in the encoding of the local operating system
	 * @param source the absolute path of the local copy
	 * @param destination location to copy to
	 * @param encoding The encoding of the local copy
	 * @param monitor progress monitor
	 */
	public void upload(String source, IRemoteFile destination, String encoding, IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException 
	{
		String remoteParentPath = destination.getParentPath();
		String remoteFileName = destination.getName();
		String hostEncoding = getRemoteEncoding(); // default host encoding
		boolean isBinary = isBinary(encoding, hostEncoding, destination.getAbsolutePath());
		try
		{
			getFileService().upload(monitor, new File(source), remoteParentPath, remoteFileName, isBinary, encoding, hostEncoding);
		}
		catch (SystemMessageException e)
		{
		
		}
	}

	public void upload(InputStream stream, long totalBytes, IRemoteFile destination, String encoding, IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException 
	{
		String remoteParentPath = destination.getParentPath();
		String remoteFileName = destination.getName();
		String hostEncoding = getRemoteEncoding(); // default host encoding
		boolean isBinary = isBinary(encoding, hostEncoding, destination.getAbsolutePath());
		try
		{
			getFileService().upload(monitor, stream, remoteParentPath, remoteFileName, isBinary, hostEncoding);
		}
		catch (SystemMessageException e)
		{
			
		}
	}

	

	public boolean copy(IRemoteFile sourceFolderOrFile, IRemoteFile targetFolder, String newName, IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException 
	{
		IFileService service = getFileService();
		try
		{
			return service.copy(monitor, sourceFolderOrFile.getParentPath(), sourceFolderOrFile.getName(), targetFolder.getAbsolutePath(), newName);
		}
		catch (SystemMessageException e)
		{
			
		}
		return false;
	}
	
	public boolean copyBatch(IRemoteFile[] sourceFolderOrFiles, IRemoteFile targetFolder, IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException 
	{
		IFileService service = getFileService();
		String[] sourceParents = new String[sourceFolderOrFiles.length];
		String[] sourceNames = new String[sourceFolderOrFiles.length];
		
		for (int i = 0; i < sourceFolderOrFiles.length; i++)
		{
			sourceParents[i] = sourceFolderOrFiles[i].getParentPath();
			sourceNames[i] = sourceFolderOrFiles[i].getName();
		}
		try
		{
			return service.copyBatch(monitor, sourceParents, sourceNames, targetFolder.getAbsolutePath());
		}
		catch (SystemMessageException e)
		{
			
		}
		return false;
	}

	public IRemoteFile getParentFolder(IRemoteFile folderOrFile) 
	{
		try
		{
			return getRemoteFileObject(folderOrFile.getParentPath());
		}
		catch (Exception e)
		{
			return null;
		}
	}

	public IRemoteFile createFile(IRemoteFile fileToCreate) throws RemoteFileSecurityException, RemoteFileIOException 
	{
		IFileService service = getFileService();
		String parent = fileToCreate.getParentPath();
		String name = fileToCreate.getName();
		IHostFile newFile = null;
		
		try
		{
			newFile = service.createFile(monitor, parent, name);
		}
		catch (SystemMessageException e)
		{
			
		}
		return getHostFileToRemoteFileAdapter().convertToRemoteFile(this, getDefaultContext(), fileToCreate.getParentRemoteFile(), newFile);
	}

	public IRemoteFile createFolder(IRemoteFile folderToCreate) throws RemoteFileSecurityException, RemoteFileIOException 
	{
		IFileService service = getFileService();
		String parent = folderToCreate.getParentPath();
		String name = folderToCreate.getName();
		IHostFile newFolder = null;
		try
		{	
			newFolder = service.createFolder(monitor, parent, name);
		}
		catch (SystemMessageException e)
		{
		
		}
		return getHostFileToRemoteFileAdapter().convertToRemoteFile(this, getDefaultContext(), folderToCreate.getParentRemoteFile(), newFolder);
	}

	public IRemoteFile createFolders(IRemoteFile folderToCreate) throws RemoteFileSecurityException, RemoteFileIOException 
	{
		return createFolder(folderToCreate);
	}

	public boolean delete(IRemoteFile folderOrFile, IProgressMonitor monitor) throws RemoteFolderNotEmptyException, RemoteFileSecurityException, RemoteFileIOException 
	{
		boolean result = false;
		IFileService service = getFileService();
		String parent = folderOrFile.getParentPath();
		String name = folderOrFile.getName();
		try
		{
			result = service.delete(monitor, parent, name);
			folderOrFile.markStale(true);
		}
		catch (SystemMessageException e)
		{
			return false;
		}
		return result;
	}
	
	public boolean deleteBatch(IRemoteFile[] folderOrFiles, IProgressMonitor monitor) throws RemoteFolderNotEmptyException, RemoteFileSecurityException, RemoteFileIOException 
	{
		boolean result = false;
		String[] parents = new String[folderOrFiles.length];
		String[] names = new String[folderOrFiles.length];
		for (int i = 0; i < folderOrFiles.length; i++)
		{
			parents[i] = folderOrFiles[i].getParentPath();
			names[i] = folderOrFiles[i].getName();
			folderOrFiles[i].markStale(true);
		}
		IFileService service = getFileService();
		try
		{
			result = service.deleteBatch(monitor, parents, names);
		}
		catch (SystemMessageException e)
		{
			return false;
		}
		return result;
	}

	public boolean rename(IRemoteFile folderOrFile, String newName) throws RemoteFileSecurityException, RemoteFileIOException 
	{
		boolean result = false;
		removeCachedRemoteFile(folderOrFile);
		IFileService service = getFileService();
		String srcParent = folderOrFile.getParentPath();
		String oldName = folderOrFile.getName();
		String newPath = srcParent + folderOrFile.getSeparator() + newName;
		try
		{
			result = service.rename(monitor, srcParent, oldName, newName);
			folderOrFile.getHostFile().renameTo(newPath);
		}
		catch (SystemMessageException e)
		{
		
		}
		return result;
	}
	
	public boolean move(IRemoteFile sourceFolderOrFile, IRemoteFile targetFolder, String newName, IProgressMonitor monitor) throws RemoteFileSecurityException, RemoteFileIOException 
	{
		boolean result = false;
		IFileService service = getFileService();
		String srcParent = sourceFolderOrFile.getParentPath();
		String srcName = sourceFolderOrFile.getName();
		String tgtParent = targetFolder.getAbsolutePath();
		try
		{
			result = service.move(monitor, srcParent, srcName, tgtParent, newName);
			sourceFolderOrFile.markStale(true);
			targetFolder.markStale(true);
		}
		catch (SystemMessageException e)
		{
		
		}
		return result;
	}

	
	public boolean setLastModified(IRemoteFile folderOrFile, long newDate) throws RemoteFileSecurityException, RemoteFileIOException 
	{
		// TODO Auto-generated method stub
		return false;
	}

	public boolean setReadOnly(IRemoteFile folderOrFile) throws RemoteFileSecurityException, RemoteFileIOException 
	{
		// TODO Auto-generated method stub
		return false;
	}

	public ILanguageUtilityFactory getLanguageUtilityFactory() 
	{
		if (_languageUtilityFactory == null)
		{
			_languageUtilityFactory = ((IFileServiceSubSystemConfiguration)getParentRemoteFileSubSystemFactory()).getLanguageUtilityFactory(this);
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
			searchService.cancelSearch(null, searchConfig);
		}
	}
	
	public IHostSearchResultConfiguration createSearchConfiguration(IHostSearchResultSet resultSet, Object searchTarget, SystemSearchString searchString)
	{
		ISearchService searchService = getSearchService();
		if (searchService != null)
		{
			IFileServiceSubSystemConfiguration factory = (IFileServiceSubSystemConfiguration)getParentRemoteFileSubSystemFactory();
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
				disconnect(SystemBasePlugin.getActiveWorkbenchShell());
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
	}
	
} 