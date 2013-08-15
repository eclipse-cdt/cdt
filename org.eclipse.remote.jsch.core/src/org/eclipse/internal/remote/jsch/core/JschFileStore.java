/*******************************************************************************
 * Copyright (c) 2007, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   IBM Corporation - Initial API and implementation
 *   Roland Schulz, University of Tennessee
 *******************************************************************************/
package org.eclipse.internal.remote.jsch.core;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.provider.FileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.internal.remote.jsch.core.commands.ChildInfosCommand;
import org.eclipse.internal.remote.jsch.core.commands.DeleteCommand;
import org.eclipse.internal.remote.jsch.core.commands.FetchInfoCommand;
import org.eclipse.internal.remote.jsch.core.commands.MkdirCommand;
import org.eclipse.osgi.util.NLS;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionManager;
import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.remote.core.RemoteServices;

public class JschFileStore extends FileStore {
	private static Map<String, JschFileStore> instanceMap = new HashMap<String, JschFileStore>();

	/**
	 * Public factory method for obtaining JschFileStore instances.
	 * 
	 * @param uri
	 *            URI to get a fileStore for
	 * @return an JschFileStore instance for the URI.
	 */
	public static JschFileStore getInstance(URI uri) {
		synchronized (instanceMap) {
			JschFileStore store = instanceMap.get(uri.toString());
			if (store == null) {
				IRemoteServices services = RemoteServices.getRemoteServices(uri);
				assert (services instanceof JSchServices);
				if (services != null) {
					IRemoteConnectionManager manager = services.getConnectionManager();
					if (manager != null) {
						IRemoteConnection connection = manager.getConnection(uri);
						if (connection != null && connection instanceof JSchConnection) {
							String path = uri.getPath();
							store = new JschFileStore((JSchConnection) connection, path);
							instanceMap.put(uri.toString(), store);
						}
					}
				}
			}
			return store;
		}
	}

	private final JSchConnection fConnection;
	private final IPath fRemotePath;

	public JschFileStore(JSchConnection conn, String path) {
		fConnection = conn;
		fRemotePath = new Path(path);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.filesystem.provider.FileStore#childInfos(int,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IFileInfo[] childInfos(int options, IProgressMonitor monitor) throws CoreException {
		ChildInfosCommand command = new ChildInfosCommand(fConnection.getSftpChannel(), fRemotePath);
		return command.getResult(monitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.filesystem.provider.FileStore#childNames(int,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public String[] childNames(int options, IProgressMonitor monitor) throws CoreException {
		IFileInfo[] infos = childInfos(options, monitor);
		String[] names = new String[infos.length];
		for (int i = 0; i < infos.length; i++) {
			names[i] = infos[i].getName();
		}
		return names;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.filesystem.provider.FileStore#delete(int,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void delete(int options, IProgressMonitor monitor) throws CoreException {
		DeleteCommand command = new DeleteCommand(fConnection.getSftpChannel(), fRemotePath);
		command.getResult(monitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.filesystem.provider.FileStore#fetchInfo(int,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IFileInfo fetchInfo(int options, IProgressMonitor monitor) throws CoreException {
		FetchInfoCommand command = new FetchInfoCommand(fConnection.getSftpChannel(), fRemotePath);
		return command.getResult(monitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.filesystem.provider.FileStore#getChild(java.lang.String)
	 */
	@Override
	public IFileStore getChild(String name) {
		URI uri = JSchFileSystem.getURIFor(fConnection.getName(), fRemotePath.append(name).toString());
		return JschFileStore.getInstance(uri);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.filesystem.provider.FileStore#getName()
	 */
	@Override
	public String getName() {
		return getNameFromPath(fRemotePath);
	}

	/**
	 * Utility routing to get the file name from an absolute path.
	 * 
	 * @param path
	 *            path to extract file name from
	 * @return last segment of path, or the full path if it is root
	 */
	private String getNameFromPath(IPath path) {
		if (path.isRoot()) {
			return path.toString();
		}
		return path.lastSegment();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.filesystem.provider.FileStore#getParent()
	 */
	@Override
	public IFileStore getParent() {
		if (fRemotePath.isRoot()) {
			return null;
		}
		String parentPath = fRemotePath.toString();
		if (fRemotePath.segmentCount() > 0) {
			parentPath = fRemotePath.removeLastSegments(1).toString();
		}
		return JschFileStore.getInstance(JSchFileSystem.getURIFor(fConnection.getName(), parentPath));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.filesystem.provider.FileStore#mkdir(int,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IFileStore mkdir(int options, IProgressMonitor monitor) throws CoreException {
		SubMonitor progress = SubMonitor.convert(monitor, 10);

		IFileInfo info = fetchInfo(EFS.NONE, progress.newChild(10));

		if (!info.exists()) {
			if ((options & EFS.SHALLOW) == EFS.SHALLOW) {
				IFileStore parent = getParent();
				if (parent != null && !parent.fetchInfo(EFS.NONE, progress.newChild(10)).exists()) {
					throw new CoreException(new Status(IStatus.ERROR, Activator.getUniqueIdentifier(), EFS.ERROR_WRITE, NLS.bind(
							"The parent of directory {0} does not exist", fRemotePath.toString()), null));
				}
			}

			MkdirCommand command = new MkdirCommand(fConnection.getSftpChannel(), fRemotePath);
			command.getResult(monitor);
		} else if (!info.isDirectory()) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getUniqueIdentifier(), EFS.ERROR_WRONG_TYPE, NLS.bind(
					"A file of name {0} already exists", fRemotePath.toString()), null));
		}

		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.filesystem.provider.FileStore#openInputStream(int,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public InputStream openInputStream(int options, IProgressMonitor monitor) throws CoreException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.filesystem.provider.FileStore#openOutputStream(int,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public OutputStream openOutputStream(int options, IProgressMonitor monitor) throws CoreException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.filesystem.provider.FileStore#putInfo(org.eclipse.core
	 * .filesystem.IFileInfo, int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void putInfo(IFileInfo info, int options, IProgressMonitor monitor) throws CoreException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.filesystem.provider.FileStore#toURI()
	 */
	@Override
	public URI toURI() {
		return JSchFileSystem.getURIFor(fConnection.getName(), fRemotePath.toString());
	}

}
