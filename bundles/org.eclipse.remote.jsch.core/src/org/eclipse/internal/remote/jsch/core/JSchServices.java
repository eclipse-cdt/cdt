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
package org.eclipse.internal.remote.jsch.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.remote.core.AbstractRemoteServices;
import org.eclipse.remote.core.IRemoteConnectionManager;
import org.eclipse.remote.core.IRemoteServicesDescriptor;

public class JSchServices extends AbstractRemoteServices {
	public static final String JSCH_ID = "org.eclipse.remote.JSch"; //$NON-NLS-1$

	private final JSchConnectionManager connMgr = new JSchConnectionManager(this);

	public JSchServices(IRemoteServicesDescriptor descriptor) {
		super(descriptor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.remote.core.IRemoteServicesDescriptor#getConnectionManager
	 * ()
	 */
	public IRemoteConnectionManager getConnectionManager() {
		return connMgr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteServices#initialize(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public boolean initialize(IProgressMonitor monitor) {
		return true;
	}

	public int getCapabilities() {
		return CAPABILITY_ADD_CONNECTIONS | CAPABILITY_EDIT_CONNECTIONS | CAPABILITY_REMOVE_CONNECTIONS
				| CAPABILITY_SUPPORTS_TCP_PORT_FORWARDING | CAPABILITY_SUPPORTS_X11_FORWARDING;
	}
}
