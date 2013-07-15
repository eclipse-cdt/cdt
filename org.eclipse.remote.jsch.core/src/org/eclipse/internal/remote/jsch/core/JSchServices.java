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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.remote.core.AbstractRemoteServices;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionManager;
import org.eclipse.remote.core.IRemoteFileManager;
import org.eclipse.remote.core.IRemoteProcess;
import org.eclipse.remote.core.IRemoteProcessBuilder;
import org.eclipse.remote.core.IRemoteServicesDescriptor;

public class JSchServices extends AbstractRemoteServices {
	public static final String JSCH_ID = "org.eclipse.remote.JSch"; //$NON-NLS-1$

	private final JSchConnectionManager connMgr = new JSchConnectionManager(this);
	private final Map<String, JSchFileManager> fileMgrs = new HashMap<String, JSchFileManager>();

	public JSchServices(IRemoteServicesDescriptor descriptor) {
		super(descriptor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteServices#getCommandShell(org.eclipse.remote.core.IRemoteConnection, int)
	 */
	public IRemoteProcess getCommandShell(IRemoteConnection conn, int flags) throws IOException {
		throw new IOException("Not currently implemented"); //$NON-NLS-1$
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
	 * @see
	 * org.eclipse.remote.core.IRemoteServicesDescriptor#getFileManager(
	 * org.eclipse.remote.core.IRemoteConnection)
	 */
	public IRemoteFileManager getFileManager(IRemoteConnection conn) {
		JSchFileManager fileMgr = fileMgrs.get(conn.getName());
		if (fileMgr == null) {
			fileMgr = new JSchFileManager((JSchConnection) conn);
			fileMgrs.put(conn.getName(), fileMgr);
		}
		return fileMgr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.remote.core.IRemoteServicesDescriptor#getProcessBuilder
	 * (org.eclipse.remote.core.IRemoteConnection, java.util.List)
	 */
	public IRemoteProcessBuilder getProcessBuilder(IRemoteConnection conn, List<String> command) {
		return new JSchProcessBuilder((JSchConnection) conn, (JSchFileManager) getFileManager(conn), command);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.remote.core.IRemoteServicesDescriptor#getProcessBuilder
	 * (org.eclipse.remote.core.IRemoteConnection, java.lang.String[])
	 */
	public IRemoteProcessBuilder getProcessBuilder(IRemoteConnection conn, String... command) {
		return new JSchProcessBuilder((JSchConnection) conn, (JSchFileManager) getFileManager(conn), command);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteServices#initialize(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public boolean initialize(IProgressMonitor monitor) {
		return true;
	}
}
