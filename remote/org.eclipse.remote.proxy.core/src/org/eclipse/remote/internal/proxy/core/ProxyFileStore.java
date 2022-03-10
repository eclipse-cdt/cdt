/*******************************************************************************
 * Copyright (c) 2016 Oak Ridge National Laboratory and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.remote.internal.proxy.core;

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
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.eclipse.remote.core.RemoteServicesUtils;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.remote.internal.proxy.core.commands.ChildInfosCommand;
import org.eclipse.remote.internal.proxy.core.commands.DeleteCommand;
import org.eclipse.remote.internal.proxy.core.commands.FetchInfoCommand;
import org.eclipse.remote.internal.proxy.core.commands.GetInputStreamCommand;
import org.eclipse.remote.internal.proxy.core.commands.GetOutputStreamCommand;
import org.eclipse.remote.internal.proxy.core.commands.MkdirCommand;
import org.eclipse.remote.internal.proxy.core.commands.PutInfoCommand;
import org.eclipse.remote.internal.proxy.core.messages.Messages;
import org.eclipse.remote.proxy.protocol.core.exceptions.ProxyException;

public class ProxyFileStore extends FileStore {
	/**
	 * Public factory method for obtaining ProxyFileStore instances.
	 *
	 * @param uri
	 *            URI to get a fileStore for
	 * @return an ProxyFileStore instance for the URI.
	 */
	public static ProxyFileStore getInstance(URI uri) {
		synchronized (instanceMap) {
			ProxyFileStore store = instanceMap.get(uri.toString());
			if (store == null) {
				store = new ProxyFileStore(uri);
				instanceMap.put(uri.toString(), store);
			}
			return store;
		}
	}

	private static Map<String, ProxyFileStore> instanceMap = new HashMap<String, ProxyFileStore>();

	private final IPath fRemotePath;
	private final URI fURI;

	private ProxyFileStore(URI uri) {
		fURI = uri;
		fRemotePath = RemoteServicesUtils.posixPath(uri.getPath());
	}

	private ProxyConnection checkConnection(IProgressMonitor monitor) throws RemoteConnectionException {
		IRemoteServicesManager manager = Activator.getService(IRemoteServicesManager.class);
		IRemoteConnectionType connectionType = manager.getConnectionType(fURI);
		if (connectionType == null) {
			throw new RemoteConnectionException(NLS.bind(Messages.ProxyFileStore_0, fURI));
		}

		try {
			IRemoteConnection connection = connectionType.getConnection(fURI);
			if (connection == null) {
				throw new RemoteConnectionException(NLS.bind(Messages.ProxyFileStore_1, fURI));
			}
			if (!connection.isOpen()) {
				connection.open(monitor);
				if (!connection.isOpen()) {
					throw new RemoteConnectionException(Messages.ProxyFileStore_2);
				}
			}
			return connection.getService(ProxyConnection.class);
		} catch (CoreException e) {
			throw new RemoteConnectionException(e);
		}
	}

	@Override
	public IFileInfo[] childInfos(int options, IProgressMonitor monitor) throws CoreException {
		SubMonitor subMon = SubMonitor.convert(monitor, 10);
		ProxyConnection connection = checkConnection(subMon.newChild(1));
		ChildInfosCommand command = new ChildInfosCommand(connection, fRemotePath.toString());
		try {
			return command.getResult(subMon.newChild(9));
		} catch (ProxyException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getUniqueIdentifier(), e.getMessage()));
		}
	}

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

	@Override
	public void delete(int options, IProgressMonitor monitor) throws CoreException {
		SubMonitor subMon = SubMonitor.convert(monitor, 20);
		ProxyConnection connection = checkConnection(subMon.newChild(1));
		IFileInfo info = fetchInfo(EFS.NONE, subMon.newChild(9));
		if (!subMon.isCanceled() && info.exists()) {
			DeleteCommand command = new DeleteCommand(connection, options, fRemotePath.toString());
			try {
				command.getResult(subMon.newChild(10));
			} catch (ProxyException e) {
				throw new CoreException(new Status(IStatus.ERROR, Activator.getUniqueIdentifier(), e.getMessage()));
			}
		}
		subMon.setWorkRemaining(0);
	}

	@Override
	public IFileInfo fetchInfo(int options, IProgressMonitor monitor) throws CoreException {
		SubMonitor subMon = SubMonitor.convert(monitor, 10);
		ProxyConnection connection = checkConnection(subMon.newChild(1));
		FetchInfoCommand command = new FetchInfoCommand(connection, fRemotePath.toString());
		try {
			return command.getResult(subMon.newChild(9));
		} catch (ProxyException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getUniqueIdentifier(), e.getMessage()));
		}
	}

	@Override
	public IFileStore getChild(String name) {
		URI uri = ProxyFileSystem.getURIFor(ProxyFileSystem.getConnectionNameFor(fURI),
				fRemotePath.append(name).toString());
		return getInstance(uri);
	}

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

	@Override
	public IFileStore getParent() {
		if (fRemotePath.isRoot()) {
			return null;
		}
		String parentPath = fRemotePath.toString();
		if (fRemotePath.segmentCount() > 0) {
			parentPath = fRemotePath.removeLastSegments(1).toString();
		}
		return getInstance(ProxyFileSystem.getURIFor(ProxyFileSystem.getConnectionNameFor(fURI), parentPath));
	}

	@Override
	public IFileStore mkdir(int options, IProgressMonitor monitor) throws CoreException {
		SubMonitor subMon = SubMonitor.convert(monitor, 16);
		ProxyConnection connection = checkConnection(subMon.newChild(1));

		if ((options & EFS.SHALLOW) == EFS.SHALLOW) {
			IFileStore parent = getParent();
			if (parent != null && !parent.fetchInfo(EFS.NONE, subMon.newChild(5)).exists()) {
				throw new CoreException(new Status(IStatus.ERROR, Activator.getUniqueIdentifier(), EFS.ERROR_WRITE,
						NLS.bind(Messages.ProxyFileStore_3, fRemotePath.toString()), null));
			}
			if (subMon.isCanceled()) {
				return this;
			}
		}
		subMon.setWorkRemaining(10);

		try {
			MkdirCommand command = new MkdirCommand(connection, options, fRemotePath.toString());
			command.getResult(subMon.newChild(5));
		} catch (Exception e) {
			// Ignore any exceptions
		}
		if (!subMon.isCanceled()) {
			/*
			 * Check if the result exists and is a directory, throw an exception if neither.
			 */
			IFileInfo info = fetchInfo(EFS.NONE, subMon.newChild(5));
			if (!subMon.isCanceled()) {
				if (!info.exists()) {
					throw new CoreException(new Status(IStatus.ERROR, Activator.getUniqueIdentifier(), EFS.ERROR_WRITE,
							NLS.bind(Messages.ProxyFileStore_4, fRemotePath.toString()), null));
				}
				if (!info.isDirectory()) {
					throw new CoreException(new Status(IStatus.ERROR, Activator.getUniqueIdentifier(),
							EFS.ERROR_WRONG_TYPE, NLS.bind(Messages.ProxyFileStore_5, fRemotePath.toString()), null));
				}
			}
		}

		return this;
	}

	@Override
	public InputStream openInputStream(int options, IProgressMonitor monitor) throws CoreException {
		SubMonitor subMon = SubMonitor.convert(monitor, 20);
		ProxyConnection connection = checkConnection(subMon.newChild(1));
		IFileInfo info = fetchInfo(EFS.NONE, subMon.newChild(9));
		if (!subMon.isCanceled()) {
			if (!info.exists()) {
				throw new CoreException(new Status(IStatus.ERROR, Activator.getUniqueIdentifier(), EFS.ERROR_READ,
						NLS.bind(Messages.ProxyFileStore_6, fRemotePath.toString()), null));
			}
			if (info.isDirectory()) {
				throw new CoreException(new Status(IStatus.ERROR, Activator.getUniqueIdentifier(), EFS.ERROR_WRONG_TYPE,
						NLS.bind(Messages.ProxyFileStore_7, fRemotePath.toString()), null));
			}
			GetInputStreamCommand command = new GetInputStreamCommand(connection, options, fRemotePath.toString());
			try {
				return command.getResult(subMon.newChild(10));
			} catch (ProxyException e) {
				throw new CoreException(new Status(IStatus.ERROR, Activator.getUniqueIdentifier(), e.getMessage()));
			}
		}
		return null;
	}

	@Override
	public OutputStream openOutputStream(int options, IProgressMonitor monitor) throws CoreException {
		SubMonitor subMon = SubMonitor.convert(monitor, 20);
		ProxyConnection connection = checkConnection(subMon.newChild(1));
		IFileInfo info = fetchInfo(EFS.NONE, subMon.newChild(9));
		if (!subMon.isCanceled()) {
			if (info.isDirectory()) {
				throw new CoreException(new Status(IStatus.ERROR, Activator.getUniqueIdentifier(), EFS.ERROR_WRONG_TYPE,
						NLS.bind(Messages.ProxyFileStore_7, fRemotePath.toString()), null));
			}
			GetOutputStreamCommand command = new GetOutputStreamCommand(connection, options, fRemotePath.toString());
			try {
				return command.getResult(subMon.newChild(10));
			} catch (ProxyException e) {
				throw new CoreException(new Status(IStatus.ERROR, Activator.getUniqueIdentifier(), e.getMessage()));
			}
		}
		return null;
	}

	@Override
	public void putInfo(IFileInfo info, int options, IProgressMonitor monitor) throws CoreException {
		SubMonitor subMon = SubMonitor.convert(monitor, 10);
		ProxyConnection connection = checkConnection(subMon.newChild(1));
		PutInfoCommand command = new PutInfoCommand(connection, info, options, fRemotePath.toString());
		try {
			command.getResult(subMon.newChild(9));
		} catch (ProxyException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getUniqueIdentifier(), e.getMessage()));
		}
	}

	@Override
	public URI toURI() {
		return fURI;
	}
}
