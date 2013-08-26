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
package org.eclipse.remote.core;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.filesystem.IFileStore;

/**
 * Abstract base class for remote process builders. Implementors can use this class to provide a default implementation of a remote
 * process builder.
 * 
 * @since 5.0
 */
public abstract class AbstractRemoteProcessBuilder implements IRemoteProcessBuilder {
	private List<String> fCommandArgs;
	private IFileStore fRemoteDir = null;
	private boolean fRedirectErrorStream = false;

	public AbstractRemoteProcessBuilder(List<String> command) {
		fCommandArgs = command;
	}

	public AbstractRemoteProcessBuilder(String... command) {
		this(Arrays.asList(command));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteProcessBuilder#command()
	 */
	@Override
	public List<String> command() {
		return fCommandArgs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.remote.core.IRemoteProcessBuilder#command(java.util.List)
	 */
	@Override
	public IRemoteProcessBuilder command(List<String> command) {
		fCommandArgs = command;
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.remote.core.IRemoteProcessBuilder#command(java.lang.String
	 * )
	 */
	@Override
	public IRemoteProcessBuilder command(String... command) {
		fCommandArgs = Arrays.asList(command);
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteProcessBuilder#directory()
	 */
	@Override
	public IFileStore directory() {
		return fRemoteDir;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.remote.core.IRemoteProcessBuilder#directory(org.eclipse
	 * .core.filesystem.IFileStore)
	 */
	@Override
	public IRemoteProcessBuilder directory(IFileStore directory) {
		fRemoteDir = directory;
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteProcessBuilder#environment()
	 */
	@Override
	public abstract Map<String, String> environment();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.remote.core.IRemoteProcessBuilder#getSupportedFlags()
	 */
	/**
	 * @since 5.0
	 */
	@Override
	public abstract int getSupportedFlags();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.remote.core.IRemoteProcessBuilder#redirectErrorStream()
	 */
	@Override
	public boolean redirectErrorStream() {
		return fRedirectErrorStream;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.remote.core.IRemoteProcessBuilder#redirectErrorStream
	 * (boolean)
	 */
	@Override
	public IRemoteProcessBuilder redirectErrorStream(boolean redirectErrorStream) {
		this.fRedirectErrorStream = redirectErrorStream;
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteProcessBuilder#start()
	 */
	@Override
	public IRemoteProcess start() throws IOException {
		return start(NONE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteProcessBuilder#start(int)
	 */
	/**
	 * @since 5.0
	 */
	@Override
	public abstract IRemoteProcess start(int flags) throws IOException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder res = new StringBuilder();
		for (String arg : command()) {
			res.append(arg);
			res.append(" "); //$NON-NLS-1$
		}
		return res.toString();
	}
}
