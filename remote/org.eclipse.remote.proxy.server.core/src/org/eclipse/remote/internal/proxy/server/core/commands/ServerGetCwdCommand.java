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

import org.eclipse.remote.proxy.protocol.core.StreamChannel;
import org.eclipse.remote.proxy.protocol.core.exceptions.ProxyException;

public class ServerGetCwdCommand extends AbstractServerCommand {

	private String cwd;
	private final DataOutputStream result;

	private class CommandRunner implements Runnable {
		@Override
		public void run() {
			try {
				result.writeUTF(cwd);
				result.flush();
			} catch (IOException e) {
				// Failed
			}
		}
	}

	public ServerGetCwdCommand(StreamChannel chan) {
		this.result = new DataOutputStream(chan.getOutputStream());
	}

	public void exec() throws ProxyException {
		cwd = System.getProperty("user.dir"); //$NON-NLS-1$
		new Thread(new CommandRunner()).start();
	}
}
