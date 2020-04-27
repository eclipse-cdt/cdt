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
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;

import org.eclipse.cdt.debug.core.memory.transport.ExportRequest;
import org.eclipse.cdt.debug.core.memory.transport.FileExport;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;

public final class RAWBinaryExport extends FileExport<FileOutputStream> {

	public RAWBinaryExport(File input, ExportRequest request) {
		super(input, request);
	}

	@Override
	protected FileOutputStream output(File file) throws IOException {
		return new FileOutputStream(file);
	}

	@Override
	protected BigInteger chunkSize() {
		return BigInteger.valueOf(1024);
	}

	@Override
	protected void transfer(FileOutputStream output, BigInteger factor, IProgressMonitor monitor)
			throws IOException, DebugException {
		BigInteger transferAddress = start;
		BigInteger jobCount = BigInteger.ZERO;
		BigInteger chunkSize = chunkSize();
		while (transferAddress.compareTo(end) < 0 && !monitor.isCanceled()) {
			BigInteger length = chunkSize;
			if (end.subtract(transferAddress).compareTo(length) < 0) {
				length = end.subtract(transferAddress);
			}
			monitor.subTask(transferring(length, transferAddress));
			byte[] byteValues = read.from(transferAddress);
			output.write(byteValues);
			transferAddress = transferAddress.add(length);
			jobCount = jobCount.add(BigInteger.ONE);
			if (jobCount.compareTo(factor) == 0) {
				jobCount = BigInteger.ZERO;
				monitor.worked(1);
			}
		}
	}

}