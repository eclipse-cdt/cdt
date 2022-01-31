/*******************************************************************************
 * Copyright (c) 2007, 2010 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.core;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.remote.internal.core.RemoteProcess;

/**
 * Abstract base class for remote process builders. Implementors can use this class to provide a default implementation of a remote
 * process builder.
 *
 * @since 5.0
 */
public abstract class AbstractRemoteProcessBuilder implements IRemoteProcessBuilder {
	private List<String> fCommandArgs;
	private IFileStore fRemoteDir;
	private boolean fRedirectErrorStream;

	private final IRemoteConnection fConnection;

	/**
	 * @since 2.0
	 */
	public AbstractRemoteProcessBuilder(IRemoteConnection connection, List<String> command) {
		fCommandArgs = command;
		fConnection = connection;
	}

	/**
	 * @since 2.0
	 */
	public AbstractRemoteProcessBuilder(IRemoteConnection connection, String... command) {
		this(connection, Arrays.asList(command));
	}

	@Override
	public List<String> command() {
		return fCommandArgs;
	}

	@Override
	public IRemoteProcessBuilder command(List<String> command) {
		fCommandArgs = command;
		return this;
	}

	@Override
	public IRemoteProcessBuilder command(String... command) {
		fCommandArgs = Arrays.asList(command);
		return this;
	}

	@Override
	public IFileStore directory() {
		return fRemoteDir;
	}

	@Override
	public IRemoteProcessBuilder directory(IFileStore directory) {
		fRemoteDir = directory;
		return this;
	}

	@Override
	public abstract Map<String, String> environment();

	/**
	 * @since 5.0
	 */
	@Override
	public abstract int getSupportedFlags();

	@Override
	public boolean redirectErrorStream() {
		return fRedirectErrorStream;
	}

	@Override
	public IRemoteProcessBuilder redirectErrorStream(boolean redirectErrorStream) {
		this.fRedirectErrorStream = redirectErrorStream;
		return this;
	}

	@Override
	public IRemoteProcess start() throws IOException {
		return start(NONE);
	}

	/**
	 * @since 5.0
	 */
	@Override
	public abstract IRemoteProcess start(int flags) throws IOException;

	@Override
	public String toString() {
		StringBuilder res = new StringBuilder();
		for (String arg : command()) {
			res.append(arg);
			res.append(" "); //$NON-NLS-1$
		}
		return res.toString();
	}

	/**
	 * @since 2.0
	 */
	@Override
	public IRemoteConnection getRemoteConnection() {
		return fConnection;
	}

	/**
	 * @since 4.0
	 */
	protected IRemoteProcess newRemoteProcess() {
		return new RemoteProcess(getRemoteConnection(), this);
	}
}
