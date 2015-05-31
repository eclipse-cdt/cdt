/*******************************************************************************
 * Copyright (c) 2013, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 * Martin Oberhuber - [468889] Support Eclipse older than Mars
 *******************************************************************************/
package org.eclipse.remote.core;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.remote.internal.core.RemoteCorePlugin;
import org.eclipse.remote.internal.core.RemotePath;
import org.eclipse.remote.internal.core.preferences.Preferences;

/**
 * Remote services utility methods.
 */
public class RemoteServicesUtils {
	/**
	 * Constructs a new POSIX path from the given string path. The string path
	 * must represent a valid file system path on a POSIX file system. The path
	 * is canonicalized and double slashes are removed except at the beginning
	 * (to handle UNC paths). All forward slashes ('/') are treated as segment
	 * delimiters. This factory method should be used if the string path is for
	 * a POSIX file system.
	 *
	 * @param path the string path
	 * @see org.eclipse.core.runtime.Path#forPosix(String)
	 * @since 2.0
	 */
	public static IPath posixPath(String path) {
		try {
			//Use the Mars implementation of Path, see bug 454959
			return Path.forPosix(path);
		} catch(NoSuchMethodError e) {
			//TODO For older Eclipse, use the fallback below. That code should be
			//removed when support for Eclipse older than Mars is no longer needed.
		}
		/** Constant value indicating if the current platform is Windows */
		boolean RUNNING_ON_WINDOWS = java.io.File.separatorChar == '\\';
		if (! RUNNING_ON_WINDOWS) {
			return new Path(path);
		} else {
			return new RemotePath(path);	
		}
	}
	
	/**
	 * Convert a UNC path to a URI
	 * 
	 * Maps the UNC server component to a connection known by one of the remote service implementations. It is assumed that the
	 * server component is of the form "[connection_type_id:]connection_name". If the "connection_type_id:" part is omitted then the
	 * current connection type preference is used by default. If no preference is set, then each implementation is tried until
	 * a matching connection name is found.
	 * 
	 * @param path
	 *            UNC path
	 * @return corresponding URI or null if not a valid path
	 */
	public static URI toURI(IPath path) {
		if (path.isUNC()) {
			IRemoteServicesManager manager = RemoteCorePlugin.getService(IRemoteServicesManager.class);
			/*
			 * Split the server component if possible.
			 */
			String[] parts = path.segment(0).split(":"); //$NON-NLS-1$
			IRemoteConnectionType services = null;
			String connName = null;
			if (parts.length == 2) {
				services = manager.getConnectionType(parts[0]);
				connName = parts[1];
			} else if (parts.length == 1) {
				String id = Preferences.getString(IRemotePreferenceConstants.PREF_CONNECTION_TYPE_ID);
				if (id != null) {
					services = manager.getConnectionType(id);
				}
				connName = parts[0];
			}

			/*
			 * If we've found the remote services then look up the connection, otherwise iterate through all available services
			 * checking for the connection name.
			 */
			IRemoteConnection conn = null;
			if (services != null) {
				conn = services.getConnection(connName);
			} else if (connName != null) {
				for (IRemoteConnectionType s : manager.getAllConnectionTypes()) {
					if (s != null) {
						conn = s.getConnection(connName);
						if (conn != null) {
							break;
						}
					}
				}
			}

			/*
			 * If a connection was found then convert it to a URI.
			 */
			if (conn != null) {
				String scheme = conn.getConnectionType().getScheme();
				String filePath = path.removeFirstSegments(1).makeAbsolute().toString();
				try {
					return new URI(scheme, connName, filePath, null, null);
				} catch (URISyntaxException e) {
					// Ignore
				}
			}
		}
		return null;
	}
}
