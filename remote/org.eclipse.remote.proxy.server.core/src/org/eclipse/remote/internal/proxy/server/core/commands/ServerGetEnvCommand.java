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
import java.util.Map;

import org.eclipse.remote.proxy.protocol.core.StreamChannel;
import org.eclipse.remote.proxy.protocol.core.exceptions.ProxyException;

public class ServerGetEnvCommand extends AbstractServerCommand {

	private final DataOutputStream result;

	private class CommandRunner implements Runnable {
		@Override
		public void run() {
			try {
				Map<String, String> env = System.getenv();
				result.writeInt(env.size());
				for (Map.Entry<String, String> entry : env.entrySet()) {
					result.writeUTF(entry.getKey());
					result.writeUTF(entry.getValue());
				}
				result.flush();
			} catch (IOException e) {
				// Failed
			}
		}
	}

	public ServerGetEnvCommand(StreamChannel chan) {
		this.result = new DataOutputStream(chan.getOutputStream());
	}

	public void exec() throws ProxyException {
		new Thread(new CommandRunner()).start();
	}
}
