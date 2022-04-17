/*******************************************************************************
 * Copyright (c) 2016 Oak Ridge National Laboratory and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.remote.internal.proxy.server.core.commands;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.proxy.protocol.core.StreamChannel;
import org.eclipse.remote.proxy.protocol.core.exceptions.ProxyException;

public class ServerGetPropertiesCommand extends AbstractServerCommand {

	private final DataOutputStream result;

	private class CommandRunner implements Runnable {
		@Override
		public void run() {
			try {
				Map<String, String> props = new HashMap<>();
				props.put(IRemoteConnection.FILE_SEPARATOR_PROPERTY,
						System.getProperty(IRemoteConnection.FILE_SEPARATOR_PROPERTY));
				props.put(IRemoteConnection.PATH_SEPARATOR_PROPERTY,
						System.getProperty(IRemoteConnection.PATH_SEPARATOR_PROPERTY));
				props.put(IRemoteConnection.LINE_SEPARATOR_PROPERTY,
						System.getProperty(IRemoteConnection.LINE_SEPARATOR_PROPERTY));
				props.put(IRemoteConnection.USER_HOME_PROPERTY,
						System.getProperty(IRemoteConnection.USER_HOME_PROPERTY));
				props.put(IRemoteConnection.OS_NAME_PROPERTY, System.getProperty(IRemoteConnection.OS_NAME_PROPERTY));
				props.put(IRemoteConnection.OS_VERSION_PROPERTY,
						System.getProperty(IRemoteConnection.OS_VERSION_PROPERTY));
				props.put(IRemoteConnection.OS_ARCH_PROPERTY, System.getProperty(IRemoteConnection.OS_ARCH_PROPERTY));
				props.put(IRemoteConnection.LOCALE_CHARMAP_PROPERTY, System.getProperty("file.encoding")); //$NON-NLS-1$

				result.writeInt(props.size());
				for (Map.Entry<String, String> entry : props.entrySet()) {
					result.writeUTF(entry.getKey());
					result.writeUTF(entry.getValue());
				}
				result.flush();
			} catch (IOException e) {
				// Failed
			}
		}
	}

	public ServerGetPropertiesCommand(StreamChannel chan) {
		this.result = new DataOutputStream(chan.getOutputStream());
	}

	@Override
	public void exec() throws ProxyException {
		new Thread(new CommandRunner()).start();
	}
}
