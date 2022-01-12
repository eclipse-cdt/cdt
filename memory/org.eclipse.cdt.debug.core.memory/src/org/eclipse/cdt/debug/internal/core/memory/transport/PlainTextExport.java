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
package org.eclipse.cdt.debug.internal.core.memory.transport;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;

import org.eclipse.cdt.debug.core.memory.transport.ExportRequest;
import org.eclipse.cdt.debug.core.memory.transport.FileExport;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.MemoryByte;

public final class PlainTextExport extends FileExport {

	public PlainTextExport(File output, ExportRequest request) {
		super(output, request);
	}

	@Override
	protected BigInteger chunkSize() {
		// These variables control how the output will be formatted
		// The output data is split by chunks of 1 addressable unit size.
		BigInteger dataCellSize = BigInteger.valueOf(1);
		// show 32 bytes of data per line, total. Adjust number of columns to compensate
		// for longer addressable unit size
		BigInteger numberOfColumns = BigInteger.valueOf(32).divide(addressable);
		// deduce the number of data chunks to be output, per line
		return dataCellSize.multiply(numberOfColumns);
	}

	@Override
	protected void transfer(OutputStream output, BigInteger factor, IProgressMonitor monitor)
			throws IOException, DebugException {
		// These variables control how the output will be formatted
		// The output data is split by chunks of 1 addressable unit size.
		final BigInteger dataCellSize = BigInteger.valueOf(1);
		BigInteger transferAddress = start;
		BigInteger jobCount = BigInteger.ZERO;
		BigInteger dataCellsPerLine = chunkSize();
		while (transferAddress.compareTo(end) < 0 && !monitor.isCanceled()) {
			BigInteger length = dataCellsPerLine;
			if (end.subtract(transferAddress).compareTo(length) < 0) {
				length = end.subtract(transferAddress);
			}
			monitor.subTask(transferring(length, transferAddress));
			StringBuilder buf = new StringBuilder();
			for (int i = 0; i < length.divide(dataCellSize).intValue(); i++) {
				if (i != 0) {
					buf.append(" "); //$NON-NLS-1$
				}
				BigInteger from = transferAddress.add(dataCellSize.multiply(BigInteger.valueOf(i)));
				MemoryByte[] bytes = read.from(from, dataCellSize.longValue());
				for (int byteIndex = 0; byteIndex < bytes.length; byteIndex++) {
					//FIXME: check MemoryByte#isReadable
					String bString = BigInteger.valueOf(0xFF & bytes[byteIndex].getValue()).toString(16);
					if (bString.length() == 1) {
						buf.append("0"); //$NON-NLS-1$
					}
					buf.append(bString);
				}
			}
			output.write(buf.toString().toUpperCase().getBytes());
			output.write("\n".getBytes()); //$NON-NLS-1$
			transferAddress = transferAddress.add(length);
			jobCount = jobCount.add(BigInteger.ONE);
			if (jobCount.compareTo(factor) == 0) {
				jobCount = BigInteger.ZERO;
				monitor.worked(1);
			}
		}
	}

}