/*******************************************************************************
 * Copyright (c) 2016 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.remote.internal.proxy.server.core.commands;

import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.remote.proxy.protocol.core.exceptions.ProxyException;

public class ServerPutInfoCommand extends AbstractServerCommand {
	private final IFileInfo info;
	private final int options;
	private final URI uri;
	
	public ServerPutInfoCommand(IFileInfo info, int options, String path) {
		this.info = info;
		this.options = options;
		this.uri = URI.create("file:" + path); //$NON-NLS-1$
	}

	public void exec() throws ProxyException {
		try {
			EFS.getStore(uri).putInfo(info, options, new NullProgressMonitor());;
		} catch (CoreException e) {
			throw new ProxyException(e.getMessage());
		}
	}
}
