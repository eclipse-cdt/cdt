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
package org.eclipse.remote.internal.jsch.core;

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
import org.eclipse.osgi.util.NLS;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.remote.internal.jsch.core.commands.ChildInfosCommand;
import org.eclipse.remote.internal.jsch.core.commands.DeleteCommand;
import org.eclipse.remote.internal.jsch.core.commands.FetchInfoCommand;
import org.eclipse.remote.internal.jsch.core.commands.GetInputStreamCommand;
import org.eclipse.remote.internal.jsch.core.commands.GetOutputStreamCommand;
import org.eclipse.remote.internal.jsch.core.commands.MkdirCommand;
import org.eclipse.remote.internal.jsch.core.commands.PutInfoCommand;
import org.eclipse.remote.internal.jsch.core.messages.Messages;

public class JschFileStore extends FileStore {
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
				store = new JschFileStore(uri);
				instanceMap.put(uri.toString(), store);
			}
			return store;
		}
	}

	private static Map<String, JschFileStore> instanceMap = new HashMap<String, JschFileStore>();

	private final IPath fRemotePath;
	private final URI fURI;

	private JschFileStore(URI uri) {
		fURI = uri;
		fRemotePath = Path.forPosix(uri.getPath());
	}

	private JSchConnection checkConnection(IProgressMonitor monitor) throws RemoteConnectionException {
		IRemoteServicesManager manager = Activator.getService(IRemoteServicesManager.class);
		IRemoteConnectionType connectionType = manager.getConnectionType(fURI);
		if (connectionType == null) {
			throw new RemoteConnectionException(NLS.bind(Messages.JschFileStore_No_remote_services_found_for_URI, fURI));
		}

		try {
			IRemoteConnection connection = connectionType.getConnection(fURI);
			if (connection == null) {
				throw new RemoteConnectionException(NLS.bind(Messages.JschFileStore_Invalid_connection_for_URI, fURI));
			}
			if (!connection.isOpen()) {
				connection.open(monitor);
				if (!connection.isOpen()) {
					throw new RemoteConnectionException(Messages.JschFileStore_Connection_is_not_open);
				}
			}
			return connection.getService(JSchConnection.class);
		} catch (CoreException e) {
			throw new RemoteConnectionException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.filesystem.provider.FileStore#childInfos(int,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IFileInfo[] childInfos(int options, IProgressMonitor monitor) throws CoreException {
		SubMonitor subMon = SubMonitor.convert(monitor, 10);
		JSchConnection connection = checkConnection(subMon.newChild(1));
		ChildInfosCommand command = new ChildInfosCommand(connection, fRemotePath);
		return command.getResult(subMon.newChild(9));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.filesystem.provider.FileStore#childNames(int,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public String[] childNames(int options, IProgressMonitor monitor) throws CoreException {
		SubMonitor subMon = SubMonitor.convert(monitor, 10);
		IFileInfo[] infos = childInfos(options, subMon.newChild(10));
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
		SubMonitor subMon = SubMonitor.convert(monitor, 20);
		JSchConnection connection = checkConnection(subMon.newChild(1));
		IFileInfo info = fetchInfo(EFS.NONE, subMon.newChild(9));
		if (!subMon.isCanceled() && info.exists()) {
			DeleteCommand command = new DeleteCommand(connection, fRemotePath);
			command.getResult(subMon.newChild(10));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.filesystem.provider.FileStore#fetchInfo(int,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IFileInfo fetchInfo(int options, IProgressMonitor monitor) throws CoreException {
		SubMonitor subMon = SubMonitor.convert(monitor, 10);
		JSchConnection connection = checkConnection(subMon.newChild(1));
		FetchInfoCommand command = new FetchInfoCommand(connection, fRemotePath);
		return command.getResult(subMon.newChild(9));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.filesystem.provider.FileStore#getChild(java.lang.String)
	 */
	@Override
	public IFileStore getChild(String name) {
		URI uri = JSchFileSystem.getURIFor(JSchFileSystem.getConnectionNameFor(fURI), fRemotePath.append(name).toString());
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
		return JschFileStore.getInstance(JSchFileSystem.getURIFor(JSchFileSystem.getConnectionNameFor(fURI), parentPath));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.filesystem.provider.FileStore#mkdir(int,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IFileStore mkdir(int options, IProgressMonitor monitor) throws CoreException {
		SubMonitor subMon = SubMonitor.convert(monitor, 20);
		JSchConnection connection = checkConnection(subMon.newChild(1));

		if ((options & EFS.SHALLOW) == EFS.SHALLOW) {
			IFileStore parent = getParent();
			if (parent != null && !parent.fetchInfo(EFS.NONE, subMon.newChild(9)).exists()) {
				throw new CoreException(new Status(IStatus.ERROR, Activator.getUniqueIdentifier(), EFS.ERROR_WRITE,
						NLS.bind(Messages.JschFileStore_The_parent_of_directory_does_not_exist, fRemotePath.toString()), null));
			}
			if (subMon.isCanceled()) {
				return this;
			}
		}

		try {
			MkdirCommand command = new MkdirCommand(connection, fRemotePath);
			command.getResult(subMon.newChild(10));
		} catch (Exception e) {
			// Ignore any exceptions
		}
		if (!subMon.isCanceled()) {
			/*
			 * Check if the result exists and is a directory, throw an exception if neither.
			 */
			IFileInfo info = fetchInfo(EFS.NONE, subMon.newChild(10));
			if (!subMon.isCanceled()) {
				if (!info.exists()) {
					throw new CoreException(new Status(IStatus.ERROR, Activator.getUniqueIdentifier(), EFS.ERROR_WRITE,
							NLS.bind(Messages.JschFileStore_The_directory_could_not_be_created, fRemotePath.toString()), null));
				}
				if (!info.isDirectory()) {
					throw new CoreException(new Status(IStatus.ERROR, Activator.getUniqueIdentifier(), EFS.ERROR_WRONG_TYPE,
							NLS.bind(Messages.JschFileStore_A_file_of_name_already_exists, fRemotePath.toString()), null));
				}
			}
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
		SubMonitor subMon = SubMonitor.convert(monitor, 30);
		JSchConnection connection = checkConnection(subMon.newChild(1));
		IFileInfo info = fetchInfo(EFS.NONE, subMon.newChild(9));
		if (!subMon.isCanceled()) {
			if (!info.exists()) {
				throw new CoreException(new Status(IStatus.ERROR, Activator.getUniqueIdentifier(), EFS.ERROR_READ,
						NLS.bind(Messages.JschFileStore_File_doesnt_exist, fRemotePath.toString()), null));
			}
			if (info.isDirectory()) {
				throw new CoreException(new Status(IStatus.ERROR, Activator.getUniqueIdentifier(), EFS.ERROR_WRONG_TYPE,
						NLS.bind(Messages.JschFileStore_Is_a_directory, fRemotePath.toString()), null));
			}
			GetInputStreamCommand command = new GetInputStreamCommand(connection, fRemotePath);
			return command.getResult(subMon.newChild(10));
		}
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
		SubMonitor subMon = SubMonitor.convert(monitor, 30);
		JSchConnection connection = checkConnection(subMon.newChild(1));
		IFileInfo info = fetchInfo(EFS.NONE, subMon.newChild(9));
		if (!subMon.isCanceled()) {
			if (info.isDirectory()) {
				throw new CoreException(new Status(IStatus.ERROR, Activator.getUniqueIdentifier(), EFS.ERROR_WRONG_TYPE,
						NLS.bind(Messages.JschFileStore_Is_a_directory, fRemotePath.toString()), null));
			}
			GetOutputStreamCommand command = new GetOutputStreamCommand(connection, options, fRemotePath);
			return command.getResult(subMon.newChild(10));
		}
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
		SubMonitor subMon = SubMonitor.convert(monitor, 10);
		JSchConnection connection = checkConnection(subMon.newChild(1));
		PutInfoCommand command = new PutInfoCommand(connection, info, options, fRemotePath);
		command.getResult(subMon.newChild(9));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.filesystem.provider.FileStore#toURI()
	 */
	@Override
	public URI toURI() {
		return fURI;
	}
}
