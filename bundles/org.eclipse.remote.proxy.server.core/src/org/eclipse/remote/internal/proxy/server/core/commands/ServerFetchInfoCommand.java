/*******************************************************************************
 * Copyright (c) 2016 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

public class ServerFetchInfoCommand extends AbstractServerCommand {
	private IFileInfo info;
	
	private final URI uri;
	private final OutputStream out;
	
	private class CommandRunner implements Runnable {
		@Override
		public void run() {
			try {
				DataOutputStream result = new DataOutputStream(out);
				SerializableFileInfo sInfo = new SerializableFileInfo(info);
				sInfo.writeObject(result);
				result.flush();
			} catch (IOException e) {
				// Failed
				e.printStackTrace();
			}
		}
	}
	
	public ServerFetchInfoCommand(StreamChannel chan, String path) {
		this.out = chan.getOutputStream();
		this.uri = URI.create("file:" + path); //$NON-NLS-1$
	}

	public void exec() throws ProxyException {
		try {
			info = EFS.getStore(uri).fetchInfo();
		} catch (CoreException e) {
			throw new ProxyException(e.getMessage());
		}
		new Thread(new CommandRunner()).start();
	}
}
