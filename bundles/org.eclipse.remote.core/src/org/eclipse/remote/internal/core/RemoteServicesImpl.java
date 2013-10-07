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
package org.eclipse.remote.internal.core;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.remote.core.RemoteServices;
import org.eclipse.remote.internal.core.services.local.LocalServices;

/**
 * Main entry point for remote services
 * 
 * @since 7.0
 */
public class RemoteServicesImpl {
	public static final String REMOTE_SERVICES_EXTENSION_POINT_ID = "remoteServices"; //$NON-NLS-1$

	// Active remote services plugins (not necessarily loaded)
	private static final Map<String, RemoteServicesDescriptor> fRemoteServicesById = Collections
			.synchronizedMap(new HashMap<String, RemoteServicesDescriptor>());
	private static final Map<String, RemoteServicesDescriptor> fRemoteServicesByScheme = Collections
			.synchronizedMap(new HashMap<String, RemoteServicesDescriptor>());

	private RemoteServicesImpl() {
		// Hide constructor
	}

	public static RemoteServicesDescriptor getRemoteServiceDescriptorById(String id) {
		retrieveRemoteServices();
		return fRemoteServicesById.get(id);
	}

	public static RemoteServicesDescriptor getRemoteServiceDescriptorByURI(URI uri) {
		String scheme = uri.getScheme();
		if (scheme != null) {
			retrieveRemoteServices();
			return fRemoteServicesByScheme.get(scheme);
		}
		return null;
	}

	/**
	 * Retrieve a sorted list of remote service descriptors. Does not return the local service provider. This must be obtained
	 * using the {@link RemoteServices#getLocalServices()} method.
	 * 
	 * @return remote service descriptors
	 */
	public static List<RemoteServicesDescriptor> getRemoteServiceDescriptors() {
		retrieveRemoteServices();
		List<RemoteServicesDescriptor> descriptors = new ArrayList<RemoteServicesDescriptor>();
		for (RemoteServicesDescriptor descriptor : fRemoteServicesById.values()) {
			if (!descriptor.getId().equals(LocalServices.LocalServicesId)) {
				descriptors.add(descriptor);
			}
		}
		Collections.sort(descriptors);
		return descriptors;
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
					RemoteServicesDescriptor proxy = new RemoteServicesDescriptor(ce);
					fRemoteServicesById.put(proxy.getId(), proxy);
					fRemoteServicesByScheme.put(proxy.getScheme(), proxy);
				}
			}
		}
	}
}
