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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.remote.proxy.protocol.core.StreamChannel;
import org.eclipse.remote.proxy.protocol.core.exceptions.ProxyException;

/**
 * TODO: Fix hang if command fails...
 *
 */
public class ServerGetOutputStreamCommand extends AbstractServerCommand {

	private final InputStream in;
	private final URI uri;
	private final int options;

	private class Forwarder implements Runnable {
		private final InputStream in;
		private final OutputStream out;

		public Forwarder(InputStream in, OutputStream out) {
			this.in = in;
			this.out = out;
		}

		@Override
		public void run() {
			byte[] buf = new byte[8192];
			int n;
			try {
				while ((n = in.read(buf)) >= 0) {
					if (n > 0) {
						out.write(buf, 0, n); // should block if no-one is reading
					}
				}
				out.flush();
			} catch (IOException e) {
				// Finish
			}
			try {
				out.close();
			} catch (IOException e) {
				// Ignore
			}
			try {
				in.close();
			} catch (IOException e) {
				// Ignore
			}
		}
	}

	public ServerGetOutputStreamCommand(StreamChannel chan, int options, String path) {
		this.in = chan.getInputStream();
		this.options = options;
		this.uri = URI.create("file:" + path); //$NON-NLS-1$
	}

	public void exec() throws ProxyException {
		try {
			OutputStream out = new BufferedOutputStream(
					EFS.getStore(uri).openOutputStream(options, new NullProgressMonitor()));
			startForwarder(in, out);
		} catch (CoreException e) {
			throw new ProxyException(e.getMessage());
		}
	}

	private void startForwarder(InputStream in, OutputStream out) {
		Forwarder forwarder = new Forwarder(in, out);
		new Thread(forwarder).start();
	}
}
