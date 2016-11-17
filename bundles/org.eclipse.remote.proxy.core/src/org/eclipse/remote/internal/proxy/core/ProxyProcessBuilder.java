/*******************************************************************************
 * Copyright (c) 2016 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.remote.internal.proxy.core;

import java.io.IOException;
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
import org.eclipse.remote.proxy.protocol.core.StreamChannel;
import org.eclipse.remote.proxy.protocol.core.exceptions.ProxyException;

public class ProxyProcessBuilder extends AbstractRemoteProcessBuilder {
	private final ProxyConnection proxyConnection;
	private Map<String, String> remoteEnv;
	
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.remote.core.AbstractRemoteProcessBuilder#environment()
	 */
	@Override
	public Map<String, String> environment() {
		if (remoteEnv == null) {
			remoteEnv = new HashMap<String, String>(proxyConnection.getEnv());
		}
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
		final List<String> cmdArgs = command();
		if (cmdArgs.size() < 1) {
			throw new IndexOutOfBoundsException();
		}
		/*
		 * If environment has not been touched, then don't send anything
		 */
		final Map<String, String> env = new HashMap<String, String>();
		if (remoteEnv != null) {
			env.putAll(remoteEnv);
		}
		final boolean append = (flags & IRemoteProcessBuilder.APPEND_ENVIRONMENT) != 0 || remoteEnv == null;
		
		final ProxyConnection conn = getRemoteConnection().getService(ProxyConnection.class);
		if (conn == null) {
			throw new IOException("Unable to located connection for this process");
		}
		
		final StreamChannel chanStdIO = conn.openChannel();
		final StreamChannel chanStdErr = conn.openChannel();
		final StreamChannel chanControl = conn.openChannel();
		
		Job job = new Job("process executor") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				ExecCommand cmd = new ExecCommand(conn, cmdArgs, env, directory().toURI().getPath(), redirectErrorStream(), append, 
						chanStdIO.getId(), chanStdErr.getId(), chanControl.getId());
				try {
					cmd.getResult(monitor);
				} catch (ProxyException e) {
					return new Status(IStatus.ERROR, Activator.getUniqueIdentifier(), e.getMessage());
				}
				return monitor.isCanceled() ? Status.CANCEL_STATUS : Status.OK_STATUS;
			}
		};
		job.schedule();
		try {
			job.join();
		} catch (InterruptedException e) {
			throw new IOException(e.getMessage());
		}
		if (!job.getResult().isOK()) {
			throw new IOException(job.getResult().getMessage());
		}

		ProxyProcess proc = new ProxyProcess(getRemoteConnection(), this, chanStdIO, chanStdErr, chanControl);
		return proc;
	}
}
