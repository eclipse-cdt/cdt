/*******************************************************************************
 * Copyright (c) 2006, 2020 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ted R Williams (Wind River Systems, Inc.) - initial implementation
 *     Alexander Fedorov (ArSysOp) - headless part extraction
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.memory.transport;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;

import org.eclipse.cdt.debug.core.memory.transport.ExportRequest;
import org.eclipse.cdt.debug.core.memory.transport.FileExport;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;

final class PlainTextExport extends FileExport<FileWriter> {

	protected PlainTextExport(File output, ExportRequest request) {
		super(output, request);
	}

	@Override
	public void run(IProgressMonitor monitor) throws CoreException {
		try {
			// These variables control how the output will be formatted
			// The output data is split by chunks of 1 addressable unit size.
			final BigInteger dataCellSize = BigInteger.valueOf(1);
			// show 32 bytes of data per line, total. Adjust number of columns to compensate
			// for longer addressable unit size
			final BigInteger numberOfColumns = BigInteger.valueOf(32).divide(addressable);
			// deduce the number of data chunks to be output, per line
			final BigInteger dataCellsPerLine = dataCellSize.multiply(numberOfColumns);
			BigInteger transferAddress = start;
			FileWriter writer = new FileWriter(file);
			BigInteger jobs = end.subtract(transferAddress).divide(dataCellsPerLine);
			BigInteger factor = BigInteger.ONE;
			if (jobs.compareTo(BigInteger.valueOf(0x7FFFFFFF)) > 0) {
				factor = jobs.divide(BigInteger.valueOf(0x7FFFFFFF));
				jobs = jobs.divide(factor);
			}
			monitor.beginTask(Messages.getString("Exporter.ProgressTitle"), jobs.intValue()); //$NON-NLS-1$
			BigInteger jobCount = BigInteger.ZERO;
			while (transferAddress.compareTo(end) < 0 && !monitor.isCanceled()) {
				BigInteger length = dataCellsPerLine;
				if (end.subtract(transferAddress).compareTo(length) < 0)
					length = end.subtract(transferAddress);
				monitor.subTask(String.format(Messages.getString("Exporter.Progress"), length.toString(10), //$NON-NLS-1$
						transferAddress.toString(16)));
				StringBuilder buf = new StringBuilder();
				for (int i = 0; i < length.divide(dataCellSize).intValue(); i++) {
					if (i != 0) {
						buf.append(" "); //$NON-NLS-1$
					}
					BigInteger from = transferAddress.add(dataCellSize.multiply(BigInteger.valueOf(i)));
					byte[] bytes = read.from(from);
					for (int byteIndex = 0; byteIndex < bytes.length; byteIndex++) {
						String bString = BigInteger.valueOf(0xFF & bytes[byteIndex]).toString(16);
						if (bString.length() == 1) {
							buf.append("0"); //$NON-NLS-1$
						}
						buf.append(bString);
					}
				}
				writer.write(buf.toString().toUpperCase());
				writer.write("\n"); //$NON-NLS-1$
				transferAddress = transferAddress.add(length);
				jobCount = jobCount.add(BigInteger.ONE);
				if (jobCount.compareTo(factor) == 0) {
					jobCount = BigInteger.ZERO;
					monitor.worked(1);
				}
			}
			writer.close();
			monitor.done();
		} catch (IOException ex) {
			MemoryTransportPlugin.getDefault().getLog()
					.log(new Status(IStatus.ERROR, MemoryTransportPlugin.getUniqueIdentifier(),
							DebugException.REQUEST_FAILED, Messages.getString("Exporter.ErrFile"), ex)); //$NON-NLS-1$
			throw new CoreException(new Status(IStatus.ERROR, MemoryTransportPlugin.getUniqueIdentifier(),
					DebugException.REQUEST_FAILED, Messages.getString("Exporter.ErrFile"), ex)); //$NON-NLS-1$

		} catch (DebugException ex) {
			MemoryTransportPlugin.getDefault().getLog()
					.log(new Status(IStatus.ERROR, MemoryTransportPlugin.getUniqueIdentifier(),
							DebugException.REQUEST_FAILED, Messages.getString("Exporter.ErrReadTarget"), ex)); //$NON-NLS-1$
			throw new CoreException(new Status(IStatus.ERROR, MemoryTransportPlugin.getUniqueIdentifier(),
					DebugException.REQUEST_FAILED, Messages.getString("Exporter.ErrReadTarget"), ex)); //$NON-NLS-1$
		} catch (Exception ex) {
			MemoryTransportPlugin.getDefault().getLog()
					.log(new Status(IStatus.ERROR, MemoryTransportPlugin.getUniqueIdentifier(),
							DebugException.INTERNAL_ERROR, Messages.getString("Exporter.Falure"), ex)); //$NON-NLS-1$
			throw new CoreException(new Status(IStatus.ERROR, MemoryTransportPlugin.getUniqueIdentifier(),
					DebugException.INTERNAL_ERROR, Messages.getString("Exporter.Falure"), ex)); //$NON-NLS-1$
		}
	}

}