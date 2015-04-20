/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.internal.core.services.local;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.remote.core.IRemoteCommandShellService;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnection.Service;
import org.eclipse.remote.core.IRemoteProcess;
import org.eclipse.remote.core.IRemoteProcessBuilder;
import org.eclipse.remote.core.IRemoteProcessService;

public class LocalCommandShellService implements IRemoteCommandShellService {

	private final IRemoteConnection connection;

	public LocalCommandShellService(IRemoteConnection connection) {
		this.connection = connection;
	}

	public static class Factory implements IRemoteCommandShellService.Factory {
		@SuppressWarnings("unchecked")
		@Override
		public <T extends Service> T getService(IRemoteConnection remoteConnection, Class<T> service) {
			if (service.equals(IRemoteCommandShellService.class)) {
				return (T) new LocalCommandShellService(remoteConnection);
			}
			return null;
		}
	}
	
	@Override
	public IRemoteConnection getRemoteConnection() {
		return connection;
	}

	@SuppressWarnings("nls")
	@Override
	public IRemoteProcess getCommandShell(int flags) throws IOException {
		IRemoteProcessService procService = connection.getService(IRemoteProcessService.class);
		String[] command;
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			command = new String[] { "cmd" };
		} else {
			List<String> list = new ArrayList<>(Arrays.asList(System.getenv("SHELL").split("\\s+")));
			list.add("-l");
			command = list.toArray(new String[list.size()]);
		}
		IRemoteProcessBuilder builder = procService.getProcessBuilder(command);
		return builder.start(IRemoteProcessBuilder.ALLOCATE_PTY);
	}

}
