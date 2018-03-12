/*******************************************************************************
 * Copyright (c) 2016 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.remote.internal.proxy.core.commands;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.remote.internal.proxy.core.ProxyConnection;
import org.eclipse.remote.internal.proxy.core.messages.Messages;
import org.eclipse.remote.proxy.protocol.core.StreamChannel;
import org.eclipse.remote.proxy.protocol.core.exceptions.ProxyException;

public abstract class AbstractCommand<T> implements Callable<T> {
	private IProgressMonitor progressMonitor;

	private static ExecutorService executors = Executors.newSingleThreadExecutor();

	private final ProxyConnection connection;

	private Future<T> asyncCmdInThread() throws ProxyException {
		return executors.submit(this);
	}

	public StreamChannel openChannel() throws IOException {
		return connection.openChannel();
	}

	public IProgressMonitor getProgressMonitor() {
		return progressMonitor;
	}

	/**
	 * Function opens exec channel and then executes the exec operation. If
	 * run on the main thread it executes it on a separate thread
	 */
	public T getResult(IProgressMonitor monitor) throws ProxyException {
		Future<T> future = null;
		progressMonitor = SubMonitor.convert(monitor, 10);
		future = asyncCmdInThread();
		return waitCmdInThread(future);
	}

	private T waitCmdInThread(Future<T> future) throws ProxyException {
		boolean bInterrupted = Thread.interrupted();
		while (!getProgressMonitor().isCanceled()) {
			try {
				return future.get(100, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				bInterrupted = true;
			} catch (TimeoutException e) {
				// ignore
			} catch (ExecutionException e) {
				throw new ProxyException(e.getMessage());
			}
			getProgressMonitor().worked(1);
		}
		if (bInterrupted) {
			Thread.currentThread().interrupt(); // set current thread flag
		}
		future.cancel(true);
		throw new ProxyException(Messages.AbstractCommand_0);
	}

	@Override
	public abstract T call() throws ProxyException;

	public AbstractCommand(ProxyConnection conn) {
		this.connection = conn;
	}
}

