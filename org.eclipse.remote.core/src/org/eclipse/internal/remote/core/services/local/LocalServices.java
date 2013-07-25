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

import java.io.IOException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionManager;
import org.eclipse.remote.core.IRemoteFileManager;
import org.eclipse.remote.core.IRemoteProcess;
import org.eclipse.remote.core.IRemoteProcessBuilder;
import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.remote.core.IRemoteServicesDescriptor;

public class LocalServices implements IRemoteServices {
	public static final String LocalServicesId = "org.eclipse.remote.LocalServices"; //$NON-NLS-1$

	private IRemoteFileManager fFileMgr = null;
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
	 * @see org.eclipse.remote.core.IRemoteServices#getCommandShell(org.eclipse.remote.core.IRemoteConnection, int)
	 */
	@Override
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
	@Override
	public IRemoteConnectionManager getConnectionManager() {
		return fConnMgr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.remote.core.IRemoteServicesDescriptor#getFileManager(
	 * org.eclipse.remote.core.IRemoteConnection)
	 */
	@Override
	public IRemoteFileManager getFileManager(IRemoteConnection conn) {
		if (!(conn instanceof LocalConnection)) {
			return null;
		}
		if (fFileMgr == null) {
			fFileMgr = new LocalFileManager((LocalConnection) conn);
		}
		return fFileMgr;
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
	 * @see
	 * org.eclipse.remote.core.IRemoteServicesDescriptor#getProcessBuilder
	 * (org.eclipse.remote.core.IRemoteConnection, java.util.List)
	 */
	@Override
	public IRemoteProcessBuilder getProcessBuilder(IRemoteConnection conn, List<String> command) {
		return new LocalProcessBuilder(conn, command);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.remote.core.IRemoteServicesDescriptor#getProcessBuilder
	 * (org.eclipse.remote.core.IRemoteConnection, java.lang.String[])
	 */
	@Override
	public IRemoteProcessBuilder getProcessBuilder(IRemoteConnection conn, String... command) {
		return new LocalProcessBuilder(conn, command);
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
	 * @see
	 * org.eclipse.remote.core.IRemoteServicesDescriptor#getServicesExtension
	 * (org.eclipse.remote.core.IRemoteConnection, java.lang.Class)
	 */
	@SuppressWarnings({ "rawtypes" })
	public Object getServicesExtension(IRemoteConnection conn, Class extension) {
		return null;
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
