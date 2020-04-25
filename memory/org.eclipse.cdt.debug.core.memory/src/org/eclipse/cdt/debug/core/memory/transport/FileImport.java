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
import java.util.function.Consumer;

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
	protected final Consumer<BigInteger> scroll;

	private final File file;

	protected FileImport(File input, ImportRequest request, Consumer<BigInteger> scroll) {
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
			transfer(monitor, reader, factor);
			if (!monitor.isCanceled()) {
				write.flush();
			}
		} catch (IOException ex) {
			requestFailed(Messages.FileImport_e_read_from_file, ex);
		} catch (DebugException ex) {
			requestFailed(Messages.FileImport_e_write_to_target, ex);
		} catch (CoreException ex) {
			failed(ex);
		} catch (Exception ex) {
			internalError(Messages.FileImport_e_import_from_file, ex);
		} finally {
			monitor.done();
		}
	}

	protected abstract I input(File file) throws FileNotFoundException;

	protected abstract void transfer(IProgressMonitor monitor, I input, BigInteger factor)
			throws IOException, CoreException, DebugException;

	protected void requestFailed(String message, Throwable exception) throws CoreException {
		failed(DebugException.REQUEST_FAILED, message, exception);
	}

	protected void internalError(String message, Throwable exception) throws CoreException {
		failed(DebugException.INTERNAL_ERROR, message, exception);
	}

	protected void failed(int code, String message, Throwable exception) throws CoreException {
		Status status = new Status(//
				IStatus.ERROR, //
				FrameworkUtil.getBundle(getClass()).getSymbolicName(), //
				code, //
				message, //
				exception);
		failed(new CoreException(status));
	}

	protected void failed(CoreException exception) throws CoreException {
		Platform.getLog(FrameworkUtil.getBundle(getClass())).log(exception.getStatus());
		throw exception;
	}
}
