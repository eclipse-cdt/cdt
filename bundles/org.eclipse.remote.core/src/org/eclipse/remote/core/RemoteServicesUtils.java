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

import org.eclipse.core.runtime.IPath;
import org.eclipse.remote.internal.core.RemoteCorePlugin;
import org.eclipse.remote.internal.core.preferences.Preferences;

/**
 * Remote services utility methods.
 */
public class RemoteServicesUtils {
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
