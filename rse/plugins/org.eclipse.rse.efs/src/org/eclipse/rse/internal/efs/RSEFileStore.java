/********************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [188360] renamed from plugin org.eclipse.rse.eclipse.filesystem
 * Martin Oberhuber (Wind River) - [189441] fix EFS operations on Windows (Local) systems
 * Martin Oberhuber (Wind River) - [191589] fix Rename by adding putInfo() for RSE EFS, and fetch symlink info
 * Kevin Doyle 		(IBM)		 - [210673] [efs][nls] Externalize Strings in RSEFileStore and RSEFileStoreImpl
 * Timur Shipilov   (Xored)      - [224538] RSEFileStore.getParent() returns null for element which is not root of filesystem
 * David McKnight   (IBM)        - [287185] EFS provider should interpret the URL host component as RSE connection name rather than a hostname
 * David McKnight  (IBM)         - [291738] [efs] repeated queries to RSEFileStoreImpl.fetchInfo() in short time-span should be reduced
 * Szymon Brandys  (IBM)         - [303092] [efs] RSE portion to deal with FileSystemResourceManager makes second call to efs provider on exception due to cancel
 ********************************************************************************/

package org.eclipse.rse.internal.efs;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.provider.FileInfo;
import org.eclipse.core.filesystem.provider.FileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * Implementation of IFileStore for RSE.
 *
 * An RSEFileStore is an immutable object. Once created, it always
 * references the same remote item. Therefore, instances can be
 * shared.
 *
 * @since RSE 2.0
 */
public class RSEFileStore extends FileStore
{
	private RSEFileStore _parent;
	private String _host;
	private String _name;
	private String _alias;
	private IPath _absolutePath;

	//cached IRemoteFile object: an Object to avoid early class loading
	private transient RSEFileStoreImpl _impl = null;
	private static HashMap instanceMap = new HashMap();

	/**
	 * Constructor to use if the parent file store is known.
	 * @param parent the parent file store.
	 * @param name the name of the file store.
	 */
	private RSEFileStore(RSEFileStore parent, String name) {
		_parent = parent;
		_host = parent.getHost();
		_name = name;
		_absolutePath = parent._absolutePath.append(name);
	}

	/**
	 * Constructor to use if the file store is a handle.
	 * @param host the connection name for the file store.
	 * @param absolutePath an absolute path to the file, valid on the remote file system.
	 */
	private RSEFileStore(String host, String absolutePath) {
		_parent = null;
		_host = host;
		_absolutePath = new Path(absolutePath);
		_name = _absolutePath.lastSegment();
		if (_name == null) {
			//Windows Root Drive has no segments but needs a name
			_name = _absolutePath.getDevice();
			if (_name == null) {
				_name = ""; //$NON-NLS-1$
			}
		}
	}
	
	/**
	 * Constructor to use if the file store is a handle.
	 * @param host the connection name for the file store.
	 * @param absolutePath an absolute path to the file, valid on the remote file system.
	 * @param aliasName the connection alias for the file store
	 */
	private RSEFileStore(String host, String absolutePath, String aliasName) {
		_parent = null;
		_host = host;
		_absolutePath = new Path(absolutePath);
		_alias = aliasName;
		_name = _absolutePath.lastSegment();
		if (_name == null) {
			//Windows Root Drive has no segments but needs a name
			_name = _absolutePath.getDevice();
			if (_name == null) {
				_name = ""; //$NON-NLS-1$
			}
		}
	}

	/**
	 * Public factory method for obtaining RSEFileStore instances.
	 * @param uri URI to get a fileStore for
	 * @return an RSEFileStore instance for the URI.
	 */
	public static RSEFileStore getInstance(URI uri) {
		synchronized(instanceMap) {
			RSEFileStore store = (RSEFileStore)instanceMap.get(uri);
			if (store==null) {
				String path = uri.getPath();
				String hostName = uri.getHost();
				String aliasName = uri.getQuery();
				store = new RSEFileStore(hostName, path, aliasName);
				instanceMap.put(uri, store);
			}
			return store;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.filesystem.provider.FileStore#getChild(java.lang.String)
	 */
	public IFileStore getChild(String name) {
		RSEFileStore tmpChild = new RSEFileStore(this, name);
		URI uri = tmpChild.toURI();
		synchronized(instanceMap) {
			RSEFileStore storedChild = (RSEFileStore)instanceMap.get(uri);
			if (storedChild==null) {
				instanceMap.put(uri, tmpChild);
			} else {
				tmpChild = storedChild;
			}
		}
		return tmpChild;
	}

	/**
	 * Returns the host name or address for this file store.
	 * @return the host name or address for this file store.
	 */
	/*package*/ String getHost() {
		//TODO consider computing this instead of storing it
		return _host;
	}
	
	String getAlias() {
		return _alias;
	}

	/**
	 * Returns an absolute path for this file store.
	 *
	 * The absolute path is in normalized form, as it can use in URIs
	 * (with separator '/').
	 *
	 * @return an absolute path for this file store in normalized form.
	 */
	/*package*/ String getAbsolutePath() {
		//TODO consider computing this instead of storing it
		return _absolutePath.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.filesystem.provider.FileStore#getName()
	 */
	public String getName() {
		return _name;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.filesystem.provider.FileStore#getParent()
	 */
	public IFileStore getParent() {
		if (_parent == null && _absolutePath.segmentCount() > 0) {
			String parentPath = _absolutePath.removeLastSegments(1).toString();
			URI parentUri = RSEFileSystem.getURIFor(_host, parentPath);
			_parent = RSEFileStore.getInstance(parentUri);
		}
		return _parent;
	}

	/**
	 * Returns the parent file store as an RSEFileStore.
	 * @return the parent file store as an RSEFileStore.
	 */
	/*package*/ RSEFileStore getParentStore() {
		return _parent;
	}

	/**
	 * Get FileStore implementation.
	 * <p>
	 * Moved implementation to separate class in order to defer class loading
	 * until resources plugin is up.
	 * </p>
	 * @return the RSEFileStoreImpl implementation.
	 */
	/*package*/ RSEFileStoreImpl getImpl() throws CoreException {
		//FIXME Workaround for Platform bug 181998 - activation problems on early startup:
		//Resources plugin opens the workspace in its start() method, triggers the
		//save manager and this in turn would activate org.eclipse.rse.ui which depends
		//on resources... this activation circle does not work, therefore throw an
		//exception if resources are not yet up.
		if (_impl==null) {
			if (!isResourcesPluginUp()) {
				throw new CoreException(new Status(IStatus.WARNING,
						Activator.getDefault().getBundle().getSymbolicName(),
						Messages.RESOURCES_NOT_LOADED));
			}
			_impl = new RSEFileStoreImpl(this);
		}
		return _impl;
	}

	private static Bundle resourcesBundle = null;
	private static boolean isResourcesPluginUp()
	{
		if (resourcesBundle==null) {
			BundleContext ctx = Activator.getDefault().getBundle().getBundleContext();
			Bundle[] bundles = ctx.getBundles();
			for (int i=0; i<bundles.length; i++) {
				if ("org.eclipse.core.resources".equals(bundles[i].getSymbolicName())) { //$NON-NLS-1$
					if (resourcesBundle==null && bundles[i].getState()==Bundle.ACTIVE) {
						resourcesBundle = bundles[i];
					}
				}
			}
		}
		return resourcesBundle != null && resourcesBundle.getState()==Bundle.ACTIVE;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.filesystem.provider.FileStore#childNames(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public String[] childNames(int options, IProgressMonitor monitor) throws CoreException {
		return getImpl().childNames(options, monitor);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.filesystem.provider.FileStore#childInfos(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IFileInfo[] childInfos(int options, IProgressMonitor monitor) throws CoreException {
		return getImpl().childInfos(options, monitor);
	}

	/**
	 * Fetch information for this file store.
	 * <p>
	 * FIXME This is a HACK to fix early startup problems until
	 * Platform bug 182006 is resolved! Returns an info
	 * object indicating a non-existing file while the Eclipse resources
	 * plugin is not fully activated. This is in order to avoid problems
	 * of activating too much of RSE while Eclipse is not yet ready.
	 * </p>
	 * @return a file info object for this file store.
	 */
	public IFileInfo fetchInfo() {
		try {
			return fetchInfo(EFS.NONE, null);
		} catch (CoreException e) {
			if (!isResourcesPluginUp()) {
				//FIXME HACK workaround for platform bug 182006:
				//Claim that files do not exist while resources
				//plugin is not yet fully up.
				return new FileInfo(getName());
			}
			//Whoa! Bad bad... wrapping a checked exception in an unchecked one...
			throw new RuntimeException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.filesystem.provider.FileStore#fetchInfo(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IFileInfo fetchInfo(int options, IProgressMonitor monitor) throws CoreException {
		return getImpl().fetchInfo(options, monitor);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.filesystem.provider.FileStore#putInfo(org.eclipse.core.filesystem.IFileInfo, int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void putInfo(IFileInfo info, int options, IProgressMonitor monitor) throws CoreException {
		getImpl().putInfo(info, options, monitor);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.filesystem.provider.FileStore#openInputStream(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public InputStream openInputStream(int options, IProgressMonitor monitor)
			throws CoreException {
		try {
			return getImpl().openInputStream(options, monitor);
		} catch (OperationCanceledException ex) {
			monitor.setCanceled(true);
			return null; //empty input stream
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.filesystem.provider.FileStore#toURI()
	 */
	public URI toURI() {
		return RSEFileSystem.getURIFor(getHost(), getAbsolutePath());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.filesystem.provider.FileStore#mkdir(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IFileStore mkdir(int options, IProgressMonitor monitor) throws CoreException
	{
		return getImpl().mkdir(options, monitor);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.filesystem.provider.FileStore#openOutputStream(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public OutputStream openOutputStream(int options, IProgressMonitor monitor) throws CoreException
	{
		return getImpl().openOutputStream(options, monitor);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.filesystem.provider.FileStore#delete(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void delete(int options, IProgressMonitor monitor) throws CoreException
	{
		getImpl().delete(options, monitor);
	}
}