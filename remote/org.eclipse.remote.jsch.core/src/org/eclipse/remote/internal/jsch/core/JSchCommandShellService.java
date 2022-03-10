/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
