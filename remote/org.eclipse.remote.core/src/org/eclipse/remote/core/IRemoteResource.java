/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.core;

import java.net.URI;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Abstraction of a remote resource. There are currently two types of remote resources: fully remote and synchronized. This
 * interface provides a common mechanism for accessing resource information from either type.
 * 
 * Usage:
 * 
 * <code>
 * 	IRemoteResource remoteRes = (IRemoteResource)resource.getAdapter(IRemoteResource.class);
 * 	if (remoteRes != null) {
 * 		URI location = remoteRes.getDefaultLocationURI();
 * 		...
 * 	}
 * </code>
 * 
 * @author greg
 * @since 6.0
 * 
 */
public interface IRemoteResource {
	/**
	 * Get the active location URI of the resource in the remote project. Returns null if the URI can't be obtained (@see
	 * {@link IResource#getLocationURI()}).
	 * 
	 * For fully remote projects, this is just the URI of the remote resource. For synchronized projects, this is the URI of the
	 * resource from the active synchronization target.
	 * 
	 * @return URI or null if URI can't be obtained
	 */
	public URI getActiveLocationURI();

	/**
	 * Get the platform resource corresponding to the remote resource
	 * 
	 * @return IResource
	 */
	public IResource getResource();

	/**
	 * Synchronize the resource with the underlying filesystem. Performs a refresh for local and remote projects, but causes
	 * synchronized projects to attempt to synchronize with their remote files. May synchronize more than just the single resource.
	 * Blocks until the refresh has completed, so callers should use a job if necessary.
	 * 
	 * @param monitor
	 *            progress monitor to cancel refresh
	 * @throws CoreException
	 *             if the underlying synchronization fails
	 */
	public void refresh(IProgressMonitor monitor) throws CoreException;

	/**
	 * Set the platform resource
	 * 
	 * @param resource
	 *            platform resource corresponding to this remote resource
	 */
	public void setResource(IResource resource);
}