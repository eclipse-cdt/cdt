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
package org.eclipse.internal.remote.core.services.local;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.remote.core.IRemoteConnectionManager;
import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.remote.core.IRemoteServicesDescriptor;

public class LocalServices implements IRemoteServices {
	public static final String LocalServicesId = "org.eclipse.remote.LocalServices"; //$NON-NLS-1$

	private final IRemoteConnectionManager fConnMgr = new LocalConnectionManager(this);
	private final IRemoteServicesDescriptor fDescriptor;

	public LocalServices(IRemoteServicesDescriptor descriptor) {
		fDescriptor = descriptor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.remote.core.IRemoteServicesDescriptor#canCreateConnections
	 * ()
	 */
	@Override
	public boolean canCreateConnections() {
		return fDescriptor.canCreateConnections();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.remote.core.IRemoteServicesDescriptor#getConnectionManager
	 * ()
	 */
	@Override
	public IRemoteConnectionManager getConnectionManager() {
		return fConnMgr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteServicesDescriptor#getId()
	 */
	@Override
	public String getId() {
		return fDescriptor.getId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteServicesDescriptor#getName()
	 */
	@Override
	public String getName() {
		return fDescriptor.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteServicesDescriptor#getScheme()
	 */
	@Override
	public String getScheme() {
		return fDescriptor.getScheme();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteServices#initialize(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public boolean initialize(IProgressMonitor monitor) {
		return true;
	}
}
