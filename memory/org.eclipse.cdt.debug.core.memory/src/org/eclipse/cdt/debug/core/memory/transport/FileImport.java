/*******************************************************************************
 * Copyright (c) 2020 ArSysOp and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Fedorov (ArSysOp) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.memory.transport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;

import org.eclipse.cdt.debug.internal.core.memory.transport.Messages;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.osgi.framework.FrameworkUtil;

/**
 * Imports memory information from a given file
 *
 * @since 0.1
 */
public abstract class FileImport<I extends AutoCloseable> implements ICoreRunnable {

	protected final BigInteger base;
	protected final BigInteger start;
	protected final WriteMemory write;
	protected final IScrollMemory scroll;

	private final File file;

	protected FileImport(File input, ImportRequest request, IScrollMemory scroll) {
		this.file = input;
		this.base = request.base();
		this.start = request.start();
		this.write = request.write();
		this.scroll = scroll;
	}

	@Override
	public void run(IProgressMonitor monitor) throws CoreException {
		try (I reader = input(file)) {
			BigInteger jobs = BigInteger.valueOf(file.length());
			BigInteger factor = BigInteger.ONE;
			if (jobs.compareTo(BigInteger.valueOf(0x7FFFFFFF)) > 0) {
				factor = jobs.divide(BigInteger.valueOf(0x7FFFFFFF));
				jobs = jobs.divide(factor);
			}
			monitor.beginTask(Messages.FileImport_task_transferring, jobs.intValue());
			transfer(reader, factor, monitor);
			if (!monitor.isCanceled()) {
				write.flush();
			}
		} catch (IOException ex) {
			requestFailed(Messages.FileImport_e_read_file, ex);
		} catch (DebugException ex) {
			requestFailed(Messages.FileImport_e_write_target, ex);
		} catch (Exception ex) {
			internalError(Messages.FileImport_e_import_file, ex);
		} finally {
			monitor.done();
		}
	}

	/**
	 * Creates the reader for the given file
	 *
	 * @param file to import from
	 * @return reader instance
	 * @throws IOException
	 */
	protected abstract I input(File file) throws FileNotFoundException;

	protected abstract void transfer(I input, BigInteger factor, IProgressMonitor monitor)
			throws IOException, DebugException;

	protected void requestFailed(String message, Throwable exception) throws DebugException {
		failed(DebugException.REQUEST_FAILED, message, exception);
	}

	protected void internalError(String message, Throwable exception) throws DebugException {
		failed(DebugException.INTERNAL_ERROR, message, exception);
	}

	protected void failed(int code, String message, Throwable exception) throws DebugException {
		Status status = new Status(//
				IStatus.ERROR, //
				getClass(), //
				code, //
				message, //
				exception);
		failed(new DebugException(status));
	}

	protected void failed(DebugException exception) throws DebugException {
		if (Platform.isRunning()) {
			Platform.getLog(FrameworkUtil.getBundle(getClass())).log(exception.getStatus());
		}
		throw exception;
	}
}
