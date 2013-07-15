/*******************************************************************************
 * Copyright (c) 2007, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.internal.remote.core.services.local;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.remote.core.AbstractRemoteProcessBuilder;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteProcess;

public class LocalProcessBuilder extends AbstractRemoteProcessBuilder {
	private final ProcessFactory localProcessBuilder;
	private final Map<String, String> remoteEnv = new HashMap<String, String>();

	public LocalProcessBuilder(IRemoteConnection conn, List<String> command) {
		super(conn, command);
		remoteEnv.putAll(System.getenv());
		localProcessBuilder = ProcessFactory.getFactory();
	}

	public LocalProcessBuilder(IRemoteConnection conn, String... command) {
		this(conn, Arrays.asList(command));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.AbstractRemoteProcessBuilder#directory()
	 */
	@Override
	public IFileStore directory() {
		IFileStore dir = super.directory();
		if (dir == null) {
			dir = EFS.getLocalFileSystem().getStore(new Path(connection().getWorkingDirectory()));
			directory(dir);
		}
		return dir;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.remote.core.AbstractRemoteProcessBuilder#environment()
	 */
	@Override
	public Map<String, String> environment() {
		return remoteEnv;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.remote.core.AbstractRemoteProcessBuilder#getSupportedFlags
	 * ()
	 */
	@Override
	public int getSupportedFlags() {
		return NONE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteProcessBuilder#start(int)
	 */
	@Override
	public IRemoteProcess start(int flags) throws IOException {
		String commandArray[] = command().toArray(new String[0]);
		String environmentArray[] = new String[environment().size()];
		int index = 0;
		for (Entry<String, String> entry : environment().entrySet()) {
			environmentArray[index++] = entry.getKey() + "=" + entry.getValue(); //$NON-NLS-1$
		}
		Process localProc;
		if (directory() != null) {
			try {
				localProc = localProcessBuilder.exec(commandArray, environmentArray,
						directory().toLocalFile(EFS.NONE, new NullProgressMonitor()));
			} catch (CoreException e) {
				throw new IOException(e.getMessage());
			}
		} else {
			localProc = localProcessBuilder.exec(commandArray, environmentArray);
		}
		return new LocalProcess(localProc, redirectErrorStream());
	}
}
