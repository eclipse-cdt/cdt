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
package org.eclipse.internal.remote.core;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

/**
 * Main entry point for remote services
 * 
 * @since 7.0
 */
public class RemoteServicesImpl {
	public static final String REMOTE_SERVICES_EXTENSION_POINT_ID = "remoteServices"; //$NON-NLS-1$

	// Active remote services plugins (not necessarily loaded)
	private static final Map<String, RemoteServicesProxy> fRemoteServicesById = Collections
			.synchronizedMap(new HashMap<String, RemoteServicesProxy>());
	private static final Map<String, RemoteServicesProxy> fRemoteServicesByScheme = Collections
			.synchronizedMap(new HashMap<String, RemoteServicesProxy>());

	private RemoteServicesImpl() {
		// Hide constructor
	}

	private static class RemoteServicesSorter implements Comparator<RemoteServicesProxy> {
		@Override
		public int compare(RemoteServicesProxy o1, RemoteServicesProxy o2) {
			return o1.getName().compareToIgnoreCase(o2.getName());
		}
	}

	public static RemoteServicesProxy getRemoteServiceProxyById(String id) {
		retrieveRemoteServices();
		return fRemoteServicesById.get(id);
	}

	public static RemoteServicesProxy getRemoteServiceProxyByURI(URI uri) {
		String scheme = uri.getScheme();
		if (scheme != null) {
			retrieveRemoteServices();
			return fRemoteServicesByScheme.get(scheme);
		}
		return null;
	}

	/**
	 * Retrieve a sorted list of remote service proxies.
	 * 
	 * @return remote service proxies
	 */
	public static RemoteServicesProxy[] getRemoteServiceProxies() {
		retrieveRemoteServices();
		List<RemoteServicesProxy> services = new ArrayList<RemoteServicesProxy>();
		for (RemoteServicesProxy proxy : fRemoteServicesById.values()) {
			services.add(proxy);
		}
		Collections.sort(services, new RemoteServicesSorter());
		return services.toArray(new RemoteServicesProxy[0]);
	}

	/**
	 * Find and load all remoteServices plugins.
	 */
	private static void retrieveRemoteServices() {
		if (fRemoteServicesById.isEmpty()) {
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint extensionPoint = registry.getExtensionPoint(RemoteCorePlugin.getUniqueIdentifier(),
					REMOTE_SERVICES_EXTENSION_POINT_ID);
			final IExtension[] extensions = extensionPoint.getExtensions();

			for (IExtension ext : extensions) {
				final IConfigurationElement[] elements = ext.getConfigurationElements();

				for (IConfigurationElement ce : elements) {
					RemoteServicesProxy proxy = new RemoteServicesProxy(ce);
					fRemoteServicesById.put(proxy.getId(), proxy);
					fRemoteServicesByScheme.put(proxy.getScheme(), proxy);
				}
			}
		}
	}
}
