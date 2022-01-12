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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
 * Exports memory information to a given file
 *
 * @since 0.1
 */
public abstract class FileExport implements ICoreRunnable {

	protected final BigInteger start;
	protected final BigInteger end;
	protected final BigInteger addressable;
	protected final IReadMemory read;

	private final File file;

	protected FileExport(File input, ExportRequest request) {
		this.file = input;
		this.start = request.start();
		this.end = request.end();
		this.addressable = request.addressable();
		this.read = request.read();
	}

	@Override
	public void run(IProgressMonitor monitor) throws CoreException {
		try (OutputStream output = output(file)) {
			BigInteger jobs = end.subtract(start).divide(chunkSize());
			BigInteger factor = BigInteger.ONE;
			if (jobs.compareTo(BigInteger.valueOf(0x7FFFFFFF)) > 0) {
				factor = jobs.divide(BigInteger.valueOf(0x7FFFFFFF));
				jobs = jobs.divide(factor);
			}
			monitor.beginTask(Messages.FileExport_task_transferring, jobs.intValue());
			transfer(output, factor, monitor);
			output.flush();
		} catch (IOException ex) {
			requestFailed(Messages.FileExport_e_write_file, ex);
		} catch (DebugException ex) {
			requestFailed(Messages.FileExport_e_read_target, ex);
		} catch (Exception ex) {
			internalError(Messages.FileExport_e_export_memory, ex);
		} finally {
			monitor.done();
		}
	}

	/**
	 * Creates the output stream for the given file
	 *
	 * @param file to export to
	 * @return writer instance
	 * @throws IOException
	 */
	protected OutputStream output(File file) throws IOException {
		file.getParentFile().mkdirs();
		return new BufferedOutputStream(new FileOutputStream(file));
	}

	/**
	 * Determines the data chunk to use for export
	 *
	 * @return the size of data chunk
	 */
	protected abstract BigInteger chunkSize();

	protected abstract void transfer(OutputStream output, BigInteger factor, IProgressMonitor monitor)
			throws IOException, DebugException;

	protected String transferring(BigInteger length, BigInteger address) {
		return String.format(Messages.FileExport_sub_transferring, length.toString(10), address.toString(16));
	}

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
