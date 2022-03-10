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
package org.eclipse.remote.internal.proxy.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.remote.core.AbstractRemoteProcessBuilder;
import org.eclipse.remote.core.IRemoteFileService;
import org.eclipse.remote.core.IRemoteProcess;
import org.eclipse.remote.core.IRemoteProcessBuilder;
import org.eclipse.remote.internal.proxy.core.commands.ExecCommand;
import org.eclipse.remote.internal.proxy.core.commands.ShellCommand;
import org.eclipse.remote.internal.proxy.core.messages.Messages;
import org.eclipse.remote.proxy.protocol.core.StreamChannel;
import org.eclipse.remote.proxy.protocol.core.exceptions.ProxyException;

public class ProxyProcessBuilder extends AbstractRemoteProcessBuilder {
	private final ProxyConnection proxyConnection;
	private Map<String, String> remoteEnv;
	private List<StreamChannel> streams = new ArrayList<>();

	public ProxyProcessBuilder(ProxyConnection connection, List<String> command) {
		super(connection.getRemoteConnection(), command);
		proxyConnection = connection;
		IRemoteFileService fileSvc = proxyConnection.getRemoteConnection().getService(IRemoteFileService.class);
		if (fileSvc != null) {
			directory(fileSvc.getResource(proxyConnection.getWorkingDirectory()));
		}
	}

	public ProxyProcessBuilder(ProxyConnection connection, String... command) {
		this(connection, Arrays.asList(command));
	}

	/**
	 * Constructor for creating command shell
	 * @param connection
	 */
	public ProxyProcessBuilder(ProxyConnection connection) {
		super(connection.getRemoteConnection(), (List<String>) null);
		proxyConnection = connection;
		redirectErrorStream(true);
	}

	@Override
	public Map<String, String> environment() {
		if (remoteEnv == null) {
			remoteEnv = new HashMap<String, String>(proxyConnection.getEnv());
		}
		return remoteEnv;
	}

	@Override
	public int getSupportedFlags() {
		return NONE;
	}

	@Override
	public IRemoteProcess start(int flags) throws IOException {
		final ProxyConnection conn = getRemoteConnection().getService(ProxyConnection.class);
		if (conn == null) {
			throw new IOException(Messages.ProxyProcessBuilder_0);
		}

		Job job;

		final List<String> cmdArgs = command();
		if (cmdArgs != null) {
			if (cmdArgs.size() < 1) {
				throw new IOException(Messages.ProxyProcessBuilder_1);
			}
			/*
			 * If environment has not been touched, then don't send anything
			 */
			final Map<String, String> env = new HashMap<String, String>();
			if (remoteEnv != null) {
				env.putAll(remoteEnv);
			}

			final boolean append = (flags & IRemoteProcessBuilder.APPEND_ENVIRONMENT) != 0 || remoteEnv == null;

			streams.add(conn.openChannel());
			streams.add(conn.openChannel());
			streams.add(conn.openChannel());

			job = new Job("process executor") { //$NON-NLS-1$
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					ExecCommand cmd = new ExecCommand(conn, cmdArgs, env, directory().toURI().getPath(),
							redirectErrorStream(), append, streams.get(0).getId(), streams.get(1).getId(),
							streams.get(2).getId());
					try {
						cmd.getResult(monitor);
					} catch (ProxyException e) {
						return new Status(IStatus.ERROR, Activator.getUniqueIdentifier(), e.getMessage());
					}
					return monitor.isCanceled() ? Status.CANCEL_STATUS : Status.OK_STATUS;
				}
			};
		} else {
			/*
			 * Start command shell
			 */
			streams.add(conn.openChannel());
			streams.add(conn.openChannel());

			job = new Job("process executor") { //$NON-NLS-1$
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					ShellCommand cmd = new ShellCommand(conn, streams.get(0).getId(), streams.get(1).getId());
					try {
						cmd.getResult(monitor);
					} catch (ProxyException e) {
						return new Status(IStatus.ERROR, Activator.getUniqueIdentifier(), e.getMessage());
					}
					return monitor.isCanceled() ? Status.CANCEL_STATUS : Status.OK_STATUS;
				}
			};
		}

		job.schedule();
		try {
			job.join();
		} catch (InterruptedException e) {
			throw new IOException(e.getMessage());
		}
		if (!job.getResult().isOK()) {
			throw new IOException(job.getResult().getMessage());
		}

		return newRemoteProcess();
	}

	public List<StreamChannel> getStreams() {
		return streams;
	}
}
