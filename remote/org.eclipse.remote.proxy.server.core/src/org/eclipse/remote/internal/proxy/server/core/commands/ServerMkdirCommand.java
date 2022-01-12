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

import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.remote.proxy.protocol.core.exceptions.ProxyException;

public class ServerMkdirCommand extends AbstractServerCommand {
	private final int options;
	private final URI uri;

	public ServerMkdirCommand(int options, String path) {
		this.options = options;
		this.uri = URI.create("file:" + path); //$NON-NLS-1$
	}

	public void exec() throws ProxyException {
		try {
			EFS.getStore(uri).mkdir(options, new NullProgressMonitor());
		} catch (CoreException e) {
			throw new ProxyException(e.getMessage());
		}
	}
}
