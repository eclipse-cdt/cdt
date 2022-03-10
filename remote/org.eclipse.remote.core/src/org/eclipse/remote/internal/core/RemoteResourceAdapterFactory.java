/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.internal.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.remote.core.IRemoteResource;
import org.eclipse.remote.internal.core.services.local.LocalResource;

public class RemoteResourceAdapterFactory implements IAdapterFactory {
	public static final String EXTENSION_POINT_ID = "remoteResources"; //$NON-NLS-1$

	public static final String ATTR_NATURE = "nature"; //$NON-NLS-1$
	public static final String ATTR_CLASS = "class"; //$NON-NLS-1$

	private Map<String, RemoteResourceFactory> fResourceFactory;

	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (adapterType == IRemoteResource.class) {
			if (adaptableObject instanceof IResource) {
				loadExtensions();
				IResource resource = (IResource) adaptableObject;
				for (String nature : fResourceFactory.keySet()) {
					try {
						if (resource.getProject().hasNature(nature)) {
							RemoteResourceFactory factory = fResourceFactory.get(nature);
							if (factory != null) {
								return adapterType.cast(factory.getRemoteResource(resource));
							}
						}
					} catch (CoreException e) {
						// Treat as failure
					}
				}
				return adapterType.cast(new LocalResource(resource));
			}
		}
		return null;
	}

	public Class<?>[] getAdapterList() {
		return new Class[] { IRemoteResource.class };
	}

	private synchronized void loadExtensions() {
		if (fResourceFactory == null) {
			fResourceFactory = new HashMap<String, RemoteResourceFactory>();

			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint extensionPoint = registry.getExtensionPoint(RemoteCorePlugin.getUniqueIdentifier(),
					EXTENSION_POINT_ID);

			for (IExtension ext : extensionPoint.getExtensions()) {
				final IConfigurationElement[] elements = ext.getConfigurationElements();

				for (IConfigurationElement ce : elements) {
					String nature = ce.getAttribute(ATTR_NATURE);
					RemoteResourceFactory factory = new RemoteResourceFactory(ce);
					fResourceFactory.put(nature, factory);
				}
			}
		}
	}

}
