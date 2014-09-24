/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.core;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.remote.internal.core.RemoteServicesDescriptor;
import org.eclipse.remote.internal.core.RemoteServicesImpl;
import org.eclipse.remote.internal.core.preferences.Preferences;

/**
 * Remote services utility methods.
 * 
 * @since 7.0
 */
public class RemoteServicesUtils {
	/**
	 * Utility method to get a remote connection given a remote services ID and a connection name.
	 * 
	 * @param remoteServicesId
	 *            ID of remote services providing the connection
	 * @param connectionName
	 *            name of the connection to find
	 * @param monitor
	 *            progress monitor
	 * @return remote connection or null if the ID or connection name are invalid
	 */
	public static IRemoteConnection getConnectionWithProgress(String remoteServicesId, String connectionName,
			IProgressMonitor monitor) {
		SubMonitor progress = SubMonitor.convert(monitor, 10);
		IRemoteServices remoteServices = RemoteServices.getRemoteServices(remoteServicesId, progress.newChild(1));
		if (remoteServices != null) {
			IRemoteConnectionManager remoteConnectionManager = remoteServices.getConnectionManager();
			if (remoteConnectionManager != null) {
				return remoteConnectionManager.getConnection(connectionName);
			}
		}
		return null;
	}

	/**
	 * Utility method to get a file store corresponding to the given path.
	 * 
	 * @param remoteServicesId
	 *            ID of remote services providing the connection
	 * @param connectionName
	 *            name of the remote connection to use
	 * @param path
	 *            path of file on remote system
	 * @param monitor
	 *            progress monitor
	 * @return file store corresponding to the remote file or null if the ID or connection name are invalid
	 */
	public static IFileStore getRemoteFileWithProgress(String remoteServicesId, String connectionName, String path,
			IProgressMonitor monitor) {
		SubMonitor progress = SubMonitor.convert(monitor, 10);
		IRemoteServices remoteServices = RemoteServices.getRemoteServices(remoteServicesId, progress.newChild(10));
		if (remoteServices != null) {
			IRemoteConnectionManager remoteConnectionManager = remoteServices.getConnectionManager();
			if (remoteConnectionManager != null) {
				IRemoteConnection remoteConnection = remoteConnectionManager.getConnection(connectionName);
				if (remoteConnection != null) {
					IRemoteFileManager remoteFileManager = remoteConnection.getFileManager();
					if (remoteFileManager != null) {
						return remoteFileManager.getResource(path);
					}
				}
			}
		}
		return null;
	}

	/**
	 * Convert a UNC path to a URI
	 * 
	 * Maps the UNC server component to a connection known by one of the remote service implementations. It is assumed that the
	 * server component is of the form "[service_id:]connection_name". If the "service_id:" part is omitted then the current
	 * remote services preference is used by default. If no preference is set, then each implementation is tried until
	 * a matching connection name is found.
	 * 
	 * @param path
	 *            UNC path
	 * @return corresponding URI or null if not a valid path
	 */
	public static URI toURI(IPath path) {
		if (path.isUNC()) {
			/*
			 * Split the server component if possible.
			 */
			String[] parts = path.segment(0).split(":"); //$NON-NLS-1$
			IRemoteServices services = null;
			String connName = null;
			if (parts.length == 2) {
				services = RemoteServices.getRemoteServices(parts[0]);
				connName = parts[1];
			} else if (parts.length == 1) {
				String id = Preferences.getString(IRemotePreferenceConstants.PREF_REMOTE_SERVICES_ID);
				if (id != null) {
					services = RemoteServices.getRemoteServices(id);
				}
				connName = parts[0];
			}

			/*
			 * If we've found the remote services then look up the connection, otherwise iterate through all available services
			 * checking for the connection name.
			 */
			IRemoteConnection conn = null;
			if (services != null) {
				conn = services.getConnectionManager().getConnection(connName);
			} else if (connName != null) {
				for (RemoteServicesDescriptor proxy : RemoteServicesImpl.getRemoteServiceDescriptors()) {
					services = proxy.getServices();
					if (services != null) {
						IRemoteConnectionManager connMgr = services.getConnectionManager();
						if (connMgr != null) {
							conn = connMgr.getConnection(connName);
							if (conn != null) {
								break;
							}
						}
					}
				}
			}

			/*
			 * If a connection was found then convert it to a URI.
			 */
			if (conn != null) {
				String scheme = conn.getRemoteServices().getScheme();
				String filePath = path.removeFirstSegments(1).makeAbsolute().toString();
				try {
					return new URI(scheme, connName, filePath, null);
				} catch (URISyntaxException e) {
					// Ignore
				}
			}
		}
		return null;
	}
}
