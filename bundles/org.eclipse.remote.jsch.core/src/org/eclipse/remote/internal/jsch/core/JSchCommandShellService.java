/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.internal.jsch.core;

import java.io.IOException;

import org.eclipse.remote.core.IRemoteCommandShellService;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteProcess;

public class JSchCommandShellService implements IRemoteCommandShellService {
	private IRemoteConnection fRemoteConnection;

	public JSchCommandShellService(IRemoteConnection remoteConnection) {
		fRemoteConnection = remoteConnection;
	}

	@Override
	public IRemoteConnection getRemoteConnection() {
		return fRemoteConnection;
	}

	@Override
	public IRemoteProcess getCommandShell(int flags) throws IOException {
		return new JSchProcessBuilder(getRemoteConnection()).start(flags);
	}

	public static class Factory implements IRemoteConnection.Service.Factory {
		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.remote.core.IRemoteConnection.Service.Factory#getService(org.eclipse.remote.core.IRemoteConnection,
		 * java.lang.Class)
		 */
		@Override
		@SuppressWarnings("unchecked")
		public <T extends IRemoteConnection.Service> T getService(IRemoteConnection connection, Class<T> service) {
			if (IRemoteCommandShellService.class.equals(service)) {
				return (T) new JSchCommandShellService(connection);
			}
			return null;
		}
	}
}
