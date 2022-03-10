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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.remote.core.IRemoteResource;

/**
 * Factory to create an IRemoteResource
 *
 * @author greg
 *
 */
public class RemoteResourceFactory {
	private final IConfigurationElement fConfigElement;

	public RemoteResourceFactory(IConfigurationElement ce) {
		fConfigElement = ce;
	}

	/**
	 * Get the remote resource associated with the platform resource.
	 *
	 * @return IRemoteResource
	 */
	public IRemoteResource getRemoteResource(IResource resource) {
		try {
			IRemoteResource remoteRes = (IRemoteResource) fConfigElement
					.createExecutableExtension(RemoteResourceAdapterFactory.ATTR_CLASS);
			remoteRes.setResource(resource);
			return remoteRes;
		} catch (CoreException e) {
			RemoteCorePlugin.log(e);
			return null;
		}
	}
}