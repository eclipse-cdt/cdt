/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.core;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Abstraction of a remote services provider. Clients obtain this interface using one of the static methods in
 * {@link RemoteServices}. The methods on this interface can then be used to access the full range of remote services provided.
 */
public interface IRemoteServices extends IRemoteServicesDescriptor {
	/**
	 * Get a connection manager for managing remote connections.
	 * 
	 * @return connection manager or null if services are not initialized
	 */
	public IRemoteConnectionManager getConnectionManager();

	/**
	 * Initialize the remote service. Clients should not call this method (it is called internally.)
	 * 
	 * @return true if the initialization was successful, false otherwise
	 * @since 7.0
	 */
	public boolean initialize(IProgressMonitor monitor);
}
