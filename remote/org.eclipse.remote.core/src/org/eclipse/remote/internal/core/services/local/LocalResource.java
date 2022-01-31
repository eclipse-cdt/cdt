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
package org.eclipse.remote.internal.core.services.local;

import java.net.URI;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.remote.core.IRemoteResource;

public class LocalResource implements IRemoteResource {
	private IResource fResource;

	public LocalResource(IResource resource) {
		fResource = resource;
	}

	@Override
	public URI getActiveLocationURI() {
		return fResource.getLocationURI();
	}

	@Override
	public IResource getResource() {
		return fResource;
	}

	@Override
	public void refresh(IProgressMonitor monitor) throws CoreException {
		fResource.refreshLocal(IResource.DEPTH_INFINITE, monitor);
	}

	@Override
	public void setResource(IResource resource) {
		fResource = resource;
	}
}
