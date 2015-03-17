/********************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others. All rights reserved.
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
 * Kushal Munir (IBM) - moved to internal package
 * Martin Oberhuber (Wind River) - [181917] EFS Improvements: Avoid unclosed Streams,
 *    - Fix early startup issues by deferring FileStore evaluation and classloading,
 *    - Improve performance by RSEFileStore instance factory and caching IRemoteFile.
 *    - Also remove unnecessary class RSEFileCache and obsolete branding files.
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Martin Oberhuber (Wind River) - [188360] renamed from plugin org.eclipse.rse.eclipse.filesystem
 * Martin Oberhuber (Wind River) - [191581] clear local IRemoteFile handle cache when modifying remote
 * Martin Oberhuber (Wind River) - [197025][197167] Improved wait for model complete
 * Martin Oberhuber (Wind River) - [191589] fix Rename by adding putInfo() for RSE EFS, and fetch symlink info
 * Martin Oberhuber (Wind River) - [199552] fix deadlock with dstore-backed efs access
 * David McKnight   (IBM)        - [207178] changing list APIs for file service and subsystems
 * Kevin Doyle		(IBM)		 - [208778] [efs][api] RSEFileStore#getOutputStream() does not support EFS#APPEND
 * Kevin Doyle 		(IBM)		 - [210673] [efs][nls] Externalize Strings in RSEFileStore and RSEFileStoreImpl
 * Timur Shipilov   (Xored)      - [224540] [efs] RSEFileStore.mkdir(EFS.NONE, null) doesn't create parent folder
 * David Dykstal (IBM) [230821] fix IRemoteFileSubSystem API to be consistent with IFileService
 * Martin Oberhuber (Wind River) - [233993] Improve EFS error reporting
 * Martin Oberhuber (Wind River) - [220300] EFS Size Property not properly updated after saving
 * Martin Oberhuber (Wind River) - [234026] Clarify IFileService#createFolder() Javadocs
 * David McKnight  (IBM)         - [287185] EFS provider should interpret the URL host component as RSE connection name rather than a hostname
 * David McKnight  (IBM)         - [291738] [efs] repeated queries to RSEFileStoreImpl.fetchInfo() in short time-span should be reduced
 * Szymon Brandys  (IBM)         - [303092] [efs] RSE portion to deal with FileSystemResourceManager makes second call to efs provider on exception due to cancel
 * Martin Oberhuber (Wind River) - [314496] [efs] Symlink target not reported
 * Martin Oberhuber (Wind River) - [314433] [efs] NPE on openOutputStream to broken symlink
 * David McKnight  (IBM)         - [398006] [efs] cached remote file should be cleared if exists() is false
 * David McKnight  (IBM)         - [461940]  RSEFileStoreImpl.getRemoteFileObject doesn't check whether the cashed file is marked as stale
 ********************************************************************************/

package org.eclipse.rse.internal.efs;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.provider.FileInfo;
import org.eclipse.core.filesystem.provider.FileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.RemoteChildrenContentsType;
import org.eclipse.rse.services.clientserver.messages.SystemElementNotFoundException;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileFilterString;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.IFileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystemConfiguration;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileContext;
import org.eclipse.rse.ui.RSEUIPlugin;

/**
 * Implementation of IFileStore for RSE.
 *
 * The RSEFileStore delegates to this impl class in order to defer class
 * loading until file contents are really needed.
 */
public class RSEFileStoreImpl extends FileStore
{
	private RSEFileStore _store;

	// to help with with performance issues when eclipse makes excessing fetchInfo calls
	private long _lastFetch = 0;
	private int _fetchWaitThreshold = 1000;
	
	//cached IRemoteFile object: an Object to avoid early class loading
	private transient volatile IRemoteFile _remoteFile;

	//markup to know that RSE has been initialized
	private static boolean _initialized;

	/**
	 * Constructor to use if the file store handle is known.
	 * @param store the file store handle for this implementation object.
	 */
	public RSEFileStoreImpl(RSEFileStore store) {
		_store = store;
		
		String waitStr = System.getProperty("rse_efs_fetch_wait_threshold"); //$NON-NLS-1$
		if (waitStr != null && waitStr.length() > 0){
			try {
				_fetchWaitThreshold = Integer.parseInt(waitStr);
			}
			catch (Exception e){
				_fetchWaitThreshold = 1000;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileStore#getChild(java.lang.String)
	 */
	public IFileStore getChild(String name) {
		return _store.getChild(name);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileStore#getName()
	 */
	public String getName() {
		return _store.getName();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileStore#getParent()
	 */
	public IFileStore getParent() {
		return _store.getParent();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileStore#toURI()
	 */
	public URI toURI() {
		return _store.toURI();
	}

	/**
	 * Wait for RSE persistence to fully initialize
	 */
	private static void waitForRSEInit() {
		if (!_initialized) {
			//Force activating RSEUIPlugin, which kicks off InitRSEJob
			RSEUIPlugin.getDefault();
			Job[] jobs = Job.getJobManager().find(null);
			for (int i=0; i<jobs.length; i++) {
				if ("Initialize RSE".equals(jobs[i].getName())) { //$NON-NLS-1$
					try {
						jobs[i].join();
					} catch(InterruptedException e) {
					}
					break;
				}
			}
			_initialized = true;
		}
	}

	/**
	 * Return the best RSE connection object matching the given host name and/or connection alias.
	 *
	 * @param hostNameOrAddr the host name IP address of requested host.
	 * @param aliasName the connection alias of the requested host
	 * @return RSE connection object matching the given connection alias, host name, or
	 *     <code>null</code> if no matching connection object was found.
	 */
	public static IHost getConnectionFor(String hostNameOrAddr, String aliasName, IProgressMonitor monitor) {
		if (hostNameOrAddr==null) {
			return null;
		}
		if (!_initialized) {
			waitForRSEInit();
		}
		ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
		IHost[] connections = sr.getHosts();

		IHost unconnected = null;
		
		// first look for connection alias
		if (aliasName != null){
			for (int i = 0; i < connections.length; i++) {
				IHost con = connections[i];
	
				if (aliasName.equalsIgnoreCase(con.getAliasName())){
					IRemoteFileSubSystem fss = getRemoteFileSubSystem(con);
					if (fss!=null && fss.isConnected()) {
						return con;
					} else {
						unconnected = con;
					}
				}
			}
		}
		
		if (unconnected == null){
			// if nothing matches the connection alias, fall back to hostname
			for (int i = 0; i < connections.length; i++) {
				IHost con = connections[i];	
				//TODO use more elaborate methods of checking whether two
				//host names/IP addresses are the same; or, use the host alias
				if (hostNameOrAddr.equalsIgnoreCase(con.getHostName())) {
					IRemoteFileSubSystem fss = getRemoteFileSubSystem(con);
					if (fss!=null && fss.isConnected()) {
						return con;
					} else {
						unconnected = con;
					}
				}
			}
		}

		return unconnected;
	}


	/**
	 * Return the best available remote file subsystem for a connection.
	 * Criteria are:
	 * <ol>
	 * <li>A connected FileServiceSubsystem</li>
	 * <li>A connected IRemoteFileSubSystem</li>
	 * <li>An unconnected FileServiceSubsystem</li>
	 * <li>An unconnected IRemoteFileSubSystem</li>
	 * </ol>
	 *
	 * @param host the connection to check
	 * @return an IRemoteFileSubSystem for the given connection, or
	 *         <code>null</code> if no IRemoteFileSubSystem is configured.
	 */
	public static IRemoteFileSubSystem getRemoteFileSubSystem(IHost host) {
		IRemoteFileSubSystem candidate = null;
		FileServiceSubSystem serviceCandidate = null;
		IRemoteFileSubSystem[] subSys = RemoteFileUtility.getFileSubSystems(host);
		for (int i=0; i<subSys.length; i++) {
			if (subSys[i] instanceof FileServiceSubSystem) {
				if (subSys[i].isConnected()) {
					//best candidate: service and connected
					return subSys[i];
				} else if (serviceCandidate==null) {
					serviceCandidate = (FileServiceSubSystem)subSys[i];
				}
			} else if(candidate==null) {
				candidate=subSys[i];
			} else if(subSys[i].isConnected() && !candidate.isConnected()) {
				candidate=subSys[i];
			}
		}
		//Now find the best candidate
		if (candidate!=null && candidate.isConnected()) {
			return candidate;
		} else if (serviceCandidate!=null) {
			return serviceCandidate;
		}
		return candidate;
	}

	/**
	 * Returns the best connected file subsystem for this file store.
	 * Never returns <code>null</code>.
	 * @param hostNameOrAddr host name or IP address
	 * @param aliasName the connection alias
	 * @param monitor progress monitor
	 * @return The best connected file subsystem for this file store.
	 * @throws CoreException if no file subsystem could be found or connected.
	 */
	public static IRemoteFileSubSystem getConnectedFileSubSystem(String hostNameOrAddr, String aliasName, IProgressMonitor monitor) throws CoreException
	{
		IHost con = RSEFileStoreImpl.getConnectionFor(hostNameOrAddr, aliasName, monitor);
		if (con == null) {
			throw new CoreException(new Status(IStatus.ERROR,
					Activator.getDefault().getBundle().getSymbolicName(),
					NLS.bind(Messages.CONNECTION_NOT_FOUND, hostNameOrAddr)));
		}
		IRemoteFileSubSystem subSys = RSEFileStoreImpl.getRemoteFileSubSystem(con);
		if (subSys == null) {
			throw new CoreException(new Status(IStatus.ERROR,
					Activator.getDefault().getBundle().getSymbolicName(),
					NLS.bind(Messages.NO_FILE_SUBSYSTEM, hostNameOrAddr, con.getAliasName())));
		}
		if (!subSys.isConnected()) {
			try {
				if (monitor==null) monitor=new NullProgressMonitor();
				subSys.connect(monitor, false);
			}
			catch (OperationCanceledException e) {
				throw e;
			}
			catch (Exception e) {
				throw new CoreException(new Status(IStatus.ERROR,
						Activator.getDefault().getBundle().getSymbolicName(),
						NLS.bind(Messages.COULD_NOT_CONNECT, hostNameOrAddr, subSys.getConfigurationId()), e));
			}
		}
		return subSys;
	}

	/**
	 * Return the cached IRemoteFile handle. Used only as a handle into
	 * ISubSystem operations, attributes of this handle are never considered
	 * except for exists() checking.
	 * @return
	 */
	private IRemoteFile getCachedRemoteFile() {
		return _remoteFile;
	}

	private void cacheRemoteFile(IRemoteFile remoteFile) {
		//if (_remoteFile != remoteFile) _remoteFile = remoteFile;
		if (_remoteFile != null && _remoteFile != remoteFile) {
			_remoteFile.markStale(true);
		}
		_remoteFile = remoteFile;
	}

	/**
	 * Returns an IRemoteFile for this file store.
	 * Requires that the file subsystem is connected.
	 * @param monitor progress monitor
	 * @param forceExists if <code>true</code>, throw an exception if the remote file does not exist
	 * @return an IRemoteFile for this file store
	 * @throws CoreException if connecting is not possible.
	 */
	private IRemoteFile getRemoteFileObject(IProgressMonitor monitor, boolean forceExists) throws CoreException {
		IRemoteFile remoteFile = getCachedRemoteFile();
		if (remoteFile!=null) {
			if (remoteFile.getParentRemoteFileSubSystem().isConnected() && remoteFile.exists() && !remoteFile.isStale()) {
				return remoteFile;
			} else {
				//need to re-initialize cache
				remoteFile=null;
				cacheRemoteFile(null);
			}
		}


		RSEFileStore parentStore = _store.getParentStore();
		if (parentStore!=null) {
			//Handle was created naming a parent file store
			IRemoteFile parent = parentStore.getImpl().getRemoteFileObject(monitor, forceExists);
			if (parent==null) {
				throw new CoreException(new Status(IStatus.ERROR,
						Activator.getDefault().getBundle().getSymbolicName(),
						Messages.COULD_NOT_GET_REMOTE_FILE));
			}
			try {
				remoteFile = parent.getParentRemoteFileSubSystem().getRemoteFileObject(parent, getName(), monitor);
			} catch(SystemMessageException e) {
				rethrowCoreException(e, EFS.ERROR_READ);
			}
		} else {
			//Handle was created with an absolute name
			String aliasName = _store.getAlias();
			String hostName = _store.getHost();
			IRemoteFileSubSystem subSys = RSEFileStoreImpl.getConnectedFileSubSystem(hostName, aliasName, monitor);
			
			try {
				remoteFile = subSys.getRemoteFileObject(_store.getAbsolutePath(), monitor);
			}
			catch (SystemMessageException e) {
				rethrowCoreException(e, EFS.ERROR_READ);
			}
		}

		cacheRemoteFile(remoteFile);
		if (forceExists && (remoteFile == null || !remoteFile.exists())) {
			throw new CoreException(new Status(IStatus.ERROR,
					Activator.getDefault().getBundle().getSymbolicName(),
					EFS.ERROR_NO_LOCATION,
					Messages.FILE_STORE_DOES_NOT_EXIST, null));
		}
		return remoteFile;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileStore#childNames(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public String[] childNames(int options, IProgressMonitor monitor) throws CoreException {

		String[] names;
		IRemoteFile remoteFile = getRemoteFileObject(monitor, true);
		IRemoteFileSubSystem subSys = remoteFile.getParentRemoteFileSubSystem();
		if (!remoteFile.isStale() && remoteFile.hasContents(RemoteChildrenContentsType.getInstance()) && !(subSys instanceof IFileServiceSubSystem))
		{
			Object[] children = remoteFile.getContents(RemoteChildrenContentsType.getInstance());
			names = new String[children.length];

			for (int i = 0; i < children.length; i++)
			{
				names[i] = ((IRemoteFile)children[i]).getName();
			}
		}
		else
		{
			try {
				IRemoteFile[] children = null;

				if (subSys instanceof FileServiceSubSystem) {
					FileServiceSubSystem fileServiceSubSystem = ((FileServiceSubSystem)subSys);
					IHostFile[] results = fileServiceSubSystem.getFileService().list(remoteFile.getAbsolutePath(), "*", IFileService.FILE_TYPE_FILES_AND_FOLDERS, monitor); //$NON-NLS-1$
					IRemoteFileSubSystemConfiguration config = subSys.getParentRemoteFileSubSystemConfiguration();
					RemoteFileFilterString filterString = new RemoteFileFilterString(config, remoteFile.getAbsolutePath(), "*"); //$NON-NLS-1$
					filterString.setShowFiles(true);
					filterString.setShowSubDirs(true);
					RemoteFileContext context = new RemoteFileContext(subSys, remoteFile, filterString);
					children = fileServiceSubSystem.getHostFileToRemoteFileAdapter().convertToRemoteFiles(fileServiceSubSystem, context, remoteFile, results);
				}
				else {
					children = subSys.list(remoteFile, monitor);
				}

				names = new String[children.length];

				for (int i = 0; i < children.length; i++) {
					names[i] = (children[i]).getName();
				}
			}
			catch (SystemMessageException e) {
				names = new String[0];
			}
		}

		return names;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileStore#childInfos(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IFileInfo[] childInfos(int options, IProgressMonitor monitor) throws CoreException {

		FileInfo[] infos;
		IRemoteFile remoteFile = getRemoteFileObject(monitor, true);
		IRemoteFileSubSystem subSys = remoteFile.getParentRemoteFileSubSystem();
		if (!remoteFile.isStale() && remoteFile.hasContents(RemoteChildrenContentsType.getInstance()) && !(subSys instanceof IFileServiceSubSystem))
		{
			Object[] children = remoteFile.getContents(RemoteChildrenContentsType.getInstance());

			infos = new FileInfo[children.length];

			for (int i = 0; i < children.length; i++)
			{
				IRemoteFile file = (IRemoteFile)(children[i]);
				FileInfo info = new FileInfo(file.getName());

				if (!file.exists()) {
					info.setExists(false);
				}
				else {
					info.setExists(true);
					info.setLastModified(file.getLastModified());
					boolean isDir = file.isDirectory();
					info.setDirectory(isDir);
					info.setAttribute(EFS.ATTRIBUTE_READ_ONLY, !file.canWrite());
					info.setAttribute(EFS.ATTRIBUTE_EXECUTABLE, file.isExecutable());
					info.setAttribute(EFS.ATTRIBUTE_ARCHIVE, file.isArchive());
					info.setAttribute(EFS.ATTRIBUTE_HIDDEN, file.isHidden());

					if (!isDir) {
						info.setLength(file.getLength());
					}
				}

				infos[i] = info;
			}
		}
		else
		{
			try {

				IRemoteFile[] children = null;

				if (subSys instanceof FileServiceSubSystem) {
					FileServiceSubSystem fileServiceSubSystem = ((FileServiceSubSystem)subSys);
					IHostFile[] results = fileServiceSubSystem.getFileService().list(remoteFile.getAbsolutePath(), "*", IFileService.FILE_TYPE_FILES_AND_FOLDERS, monitor); //$NON-NLS-1$
					IRemoteFileSubSystemConfiguration config = subSys.getParentRemoteFileSubSystemConfiguration();
					RemoteFileFilterString filterString = new RemoteFileFilterString(config, remoteFile.getAbsolutePath(), "*"); //$NON-NLS-1$
					filterString.setShowFiles(true);
					filterString.setShowSubDirs(true);
					RemoteFileContext context = new RemoteFileContext(subSys, remoteFile, filterString);
					children = fileServiceSubSystem.getHostFileToRemoteFileAdapter().convertToRemoteFiles(fileServiceSubSystem, context, remoteFile, results);
				}
				else {
					children = subSys.list(remoteFile, monitor);
				}

				infos = new FileInfo[children.length];

				for (int i = 0; i < children.length; i++)
				{
					IRemoteFile file = children[i];
					FileInfo info = new FileInfo(file.getName());

					if (!file.exists()) {
						info.setExists(false);
					}
					else {
						info.setExists(true);
						info.setLastModified(file.getLastModified());
						boolean isDir = file.isDirectory();
						info.setDirectory(isDir);
						info.setAttribute(EFS.ATTRIBUTE_READ_ONLY, !file.canWrite());
						info.setAttribute(EFS.ATTRIBUTE_EXECUTABLE, file.isExecutable());
						info.setAttribute(EFS.ATTRIBUTE_ARCHIVE, file.isArchive());
						info.setAttribute(EFS.ATTRIBUTE_HIDDEN, file.isHidden());
						//TODO Add symbolic link attribute

						if (!isDir) {
							info.setLength(file.getLength());
						}
					}

					infos[i] = info;
				}
			}
			catch (SystemMessageException e) {
				//TODO check whether we should not throw an exception ourselves
				infos = new FileInfo[0];
			}
		}

		return infos;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileStore#fetchInfo(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IFileInfo fetchInfo(int options, IProgressMonitor monitor) throws CoreException {
		long curTime = System.currentTimeMillis();

		
		// don't clear cache when there are several successive queries in a short time-span
		if (_lastFetch == 0 || ((curTime - _lastFetch) > _fetchWaitThreshold)){	
			// clear cache in order to query latest info
			cacheRemoteFile(null);
			_lastFetch = curTime;
		}

		// connect if needed. Will throw exception if not successful.
		IRemoteFile remoteFile = getRemoteFileObject(monitor, false);
		String classification = (remoteFile==null) ? null : remoteFile.getClassification();

		FileInfo info = new FileInfo(_store.getName());
		if (remoteFile == null) {
			info.setExists(false);
			return info;
		}
		if (classification!=null && classification.startsWith("broken symbolic link")) { //$NON-NLS-1$
			//broken symbolic link handling
			info.setExists(false);
			info.setLastModified(remoteFile.getLastModified());
			info.setAttribute(EFS.ATTRIBUTE_SYMLINK, true);
			int i1 = classification.indexOf('`');
			if (i1>0) {
				int i2 = classification.indexOf('\'');
				if (i2>i1) {
					info.setStringAttribute(EFS.ATTRIBUTE_LINK_TARGET, classification.substring(i1+1,i2));
				}
			}
			return info;
		} else if (!remoteFile.exists()) {
			info.setExists(false);
			return info;
		}

		info.setExists(true);
		info.setLastModified(remoteFile.getLastModified());
		boolean isDir = remoteFile.isDirectory();
		info.setDirectory(isDir);
		info.setAttribute(EFS.ATTRIBUTE_READ_ONLY, !remoteFile.canWrite());
		info.setAttribute(EFS.ATTRIBUTE_EXECUTABLE, remoteFile.isExecutable());
		info.setAttribute(EFS.ATTRIBUTE_ARCHIVE, remoteFile.isArchive());
		info.setAttribute(EFS.ATTRIBUTE_HIDDEN, remoteFile.isHidden());
		if (classification!=null && classification.startsWith("symbolic link")) { //$NON-NLS-1$
			info.setAttribute(EFS.ATTRIBUTE_SYMLINK, true);
			int idx = classification.indexOf(':');
			if (idx>0) {
				info.setStringAttribute(EFS.ATTRIBUTE_LINK_TARGET, classification.substring(idx+1));
			}
		}

		if (!isDir) {
			info.setLength(remoteFile.getLength());
		}

		return info;
	}

	/**
	 * Return a message for logging, built from
	 * @param item item where failure occurred
	 * @param e exception
	 * @return
	 */
    private String getExceptionMessage(String item, Throwable e) {
    	String exceptionText;
    	if (e!=null) {
    		if (e.getLocalizedMessage()!=null) {
    			exceptionText = e.getLocalizedMessage();
    		} else {
    			exceptionText = e.getClass().getName();
    		}
    	} else {
    		exceptionText = Messages.UNKNOWN_EXCEPTION;
    	}
    	if (item!=null && item.length()>0) {
    		return exceptionText + ": " + item; //$NON-NLS-1$
    	}
    	return exceptionText;
    }

	/**
	 * Re-interpret RSE internal exceptions into proper EFS CoreException.
	 *
	 * @param e Original exception from RSE SubSystems
	 * @param codeHint hint as to what the EFS Error Code might be
	 * @throws CoreException create CoreException according to EFS specification
	 */
	private void rethrowCoreException(Exception e, int codeHint) throws CoreException {
		//default pluginId to the EFS provider; override by root if possible
		String pluginId = Activator.getDefault().getBundle().getSymbolicName();
		String msg = getExceptionMessage(toString(), e);
		int code = codeHint;
		if (e instanceof SystemElementNotFoundException) {
			code = EFS.ERROR_NOT_EXISTS;
		}
		throw new CoreException(new Status(IStatus.ERROR, pluginId, code, msg, e));
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.filesystem.provider.FileStore#putInfo(org.eclipse.core.filesystem.IFileInfo, int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void putInfo(IFileInfo info, int options, IProgressMonitor monitor) throws CoreException {
		// connect if needed. Will throw exception if not successful.
		IRemoteFile remoteFile = getRemoteFileObject(monitor, false);
		IRemoteFileSubSystem subSys = remoteFile.getParentRemoteFileSubSystem();
		try {
			if ((options & EFS.SET_ATTRIBUTES) != 0) {
				// We cannot currently write isExecutable(), isHidden()
				subSys.setReadOnly(remoteFile, info.getAttribute(EFS.ATTRIBUTE_READ_ONLY), monitor);
			}
			if ((options & EFS.SET_LAST_MODIFIED) != 0) {
				subSys.setLastModified(remoteFile, info.getLastModified(), monitor);
			}
		} catch (Exception e) {
			rethrowCoreException(e, EFS.ERROR_WRITE);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileStore#openInputStream(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public InputStream openInputStream(int options, IProgressMonitor monitor) throws CoreException
	{
		IRemoteFile remoteFile = getRemoteFileObject(monitor, true);
		IRemoteFileSubSystem subSys = remoteFile.getParentRemoteFileSubSystem();

		if (remoteFile.isDirectory()) {
			throw new CoreException(new Status(IStatus.ERROR,
					Activator.getDefault().getBundle().getSymbolicName(),
					EFS.ERROR_WRONG_TYPE,
					Messages.CANNOT_OPEN_STREAM_ON_FOLDER, null));
		}

		if (remoteFile.isFile()) {
			try {
				cacheRemoteFile(null);
				return subSys.getInputStream(remoteFile.getParentPath(), remoteFile.getName(), true, monitor);
			}
			catch (SystemMessageException e) {
				cacheRemoteFile(null);
				rethrowCoreException(e, EFS.ERROR_READ);
			}
		}

		//file does not exist, apparently
		//TODO use Java MessageFormat for embedding filename in message
		throw new CoreException(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(),
		    // EFS.ERROR_NOT_EXISTS,
			EFS.ERROR_READ, Messages.FILE_STORE_DOES_NOT_EXIST + ": " + toString(), null)); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileStore#mkdir(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IFileStore mkdir(int options, IProgressMonitor monitor) throws CoreException
	{
		cacheRemoteFile(null);
		IRemoteFile remoteFile = getRemoteFileObject(monitor, false);
		if (remoteFile==null) {
			throw new CoreException(new Status(IStatus.ERROR,
					Activator.getDefault().getBundle().getSymbolicName(),
					EFS.ERROR_NOT_EXISTS,
					Messages.COULD_NOT_GET_REMOTE_FILE, null));
		}
		IRemoteFileSubSystem subSys = remoteFile.getParentRemoteFileSubSystem();
		if (!remoteFile.exists()) {
			try {
				if ((options & EFS.SHALLOW) != 0) {
					//MUST NOT create parents, so we need to check ourselves
					//here according to IRemoteFileSubSystem.createFolder() docs
					if (!remoteFile.getParentRemoteFile().exists()) {
						throw new CoreException(new Status(IStatus.ERROR,
								Activator.getDefault().getBundle().getSymbolicName(),
								EFS.ERROR_WRITE,
								Messages.FILE_STORE_DOES_NOT_EXIST, null));
					}
					remoteFile = subSys.createFolder(remoteFile, monitor);
				} else {
					remoteFile = subSys.createFolders(remoteFile, monitor);
				}
				cacheRemoteFile(remoteFile);
			}
			catch (SystemMessageException e) {
				rethrowCoreException(e, EFS.ERROR_WRITE);
			}
			return _store;
		}
		else if (remoteFile.isFile()) {
			throw new CoreException(new Status(IStatus.ERROR,
					Activator.getDefault().getBundle().getSymbolicName(),
					EFS.ERROR_WRONG_TYPE,
					Messages.FILE_NAME_EXISTS, null));
		}
		else {
			return _store;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileStore#openOutputStream(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public OutputStream openOutputStream(int options, IProgressMonitor monitor) throws CoreException {
		cacheRemoteFile(null);
		IRemoteFile remoteFile = getRemoteFileObject(monitor, false);
		if (remoteFile==null) {
			throw new CoreException(new Status(IStatus.ERROR,
					Activator.getDefault().getBundle().getSymbolicName(),
					Messages.COULD_NOT_GET_REMOTE_FILE));
		}
		IRemoteFileSubSystem subSys = remoteFile.getParentRemoteFileSubSystem();
		if (!remoteFile.exists()) {
			try {
				remoteFile = subSys.createFile(remoteFile, monitor);
				cacheRemoteFile(remoteFile);
			}
			catch (SystemMessageException e) {
				rethrowCoreException(e, EFS.ERROR_WRITE);
			}
		}

		if (remoteFile.isDirectory()) {
			throw new CoreException(new Status(IStatus.ERROR,
					Activator.getDefault().getBundle().getSymbolicName(),
					EFS.ERROR_WRONG_TYPE,
					Messages.CANNOT_OPEN_STREAM_ON_FOLDER, null));
		} else {
			//bug 314433: try opening the Stream even for non-existing items or symlinks
			//since returning null violates the API contract - better throw an Exception.
			try {
				// Convert from EFS option constants to IFileService option constants
				if ((options & EFS.APPEND) != 0) {
					options = IFileService.APPEND;
				} else {
					options = IFileService.NONE;
				}
				cacheRemoteFile(null);
				return subSys.getOutputStream(remoteFile.getParentPath(), remoteFile.getName(), options, monitor);
			}
			catch (SystemMessageException e) {
				rethrowCoreException(e, EFS.ERROR_WRITE);
			}
		}
		//file does not exist, apparently
		//TODO use Java MessageFormat for embedding filename in message
		throw new CoreException(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(),
		    // EFS.ERROR_NOT_EXISTS,
			EFS.ERROR_WRITE, Messages.FILE_STORE_DOES_NOT_EXIST + ": " + toString(), null)); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileStore#delete(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void delete(int options, IProgressMonitor monitor) throws CoreException
	{
		IRemoteFile remoteFile = getRemoteFileObject(monitor, false);
		IRemoteFileSubSystem subSys = remoteFile.getParentRemoteFileSubSystem();
		try {
			cacheRemoteFile(null);
			subSys.delete(remoteFile, monitor);
		}
		catch (SystemElementNotFoundException e) {
			/* not considered an error by EFS -- ignore */
		}
		catch (SystemMessageException e) {
			rethrowCoreException(e, EFS.ERROR_DELETE);
		}
	}
}