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
import java.io.OutputStream;
import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.remote.proxy.protocol.core.SerializableFileInfo;
import org.eclipse.remote.proxy.protocol.core.StreamChannel;
import org.eclipse.remote.proxy.protocol.core.exceptions.ProxyException;

public class ServerChildInfosCommand extends AbstractServerCommand {
	private IFileInfo[] infos;

	private final URI uri;
	private final OutputStream out;

	private class CommandRunner implements Runnable {
		@Override
		public void run() {
			try {
				DataOutputStream result = new DataOutputStream(out);
				result.writeInt(infos.length);
				for (int i = 0; i < infos.length; i++) {
					SerializableFileInfo sInfo = new SerializableFileInfo(infos[i]);
					sInfo.writeObject(result);
				}
				result.flush();
			} catch (IOException e) {
				// Failed
				e.printStackTrace();
			}
		}
	}

	public ServerChildInfosCommand(StreamChannel chan, String path) {
		this.out = chan.getOutputStream();
		this.uri = URI.create("file:" + path); //$NON-NLS-1$
	}

	@Override
	public void exec() throws ProxyException {
		try {
			infos = EFS.getStore(uri).childInfos(EFS.NONE, null);
		} catch (CoreException e) {
			throw new ProxyException(e.getMessage());
		}
		new Thread(new CommandRunner()).start();
	}
}
