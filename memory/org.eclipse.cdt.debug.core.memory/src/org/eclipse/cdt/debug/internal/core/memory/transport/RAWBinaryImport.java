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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.util.function.Consumer;

import org.eclipse.cdt.debug.core.memory.transport.FileImport;
import org.eclipse.cdt.debug.core.memory.transport.ImportRequest;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;

public final class RAWBinaryImport extends FileImport<FileInputStream> {

	public RAWBinaryImport(File input, ImportRequest request, Consumer<BigInteger> scroll) {
		super(input, request, scroll);
	}

	@Override
	protected FileInputStream input(File file) throws FileNotFoundException {
		return new FileInputStream(file);
	}

	@Override
	protected void transfer(FileInputStream input, BigInteger factor, IProgressMonitor monitor)
			throws IOException, DebugException {
		byte[] byteValues = new byte[1024];
		int actualByteCount = input.read(byteValues);
		BigInteger recordAddress = start;
		while (actualByteCount != -1 && !monitor.isCanceled()) {
			byte data[] = new byte[actualByteCount];
			for (int i = 0; i < data.length; i++) {
				data[i] = byteValues[i];
			}
			write.to(recordAddress.subtract(base), data);
			BigInteger jobCount = BigInteger.valueOf(actualByteCount).divide(factor);
			monitor.worked(jobCount.intValue());
			recordAddress = recordAddress.add(BigInteger.valueOf(actualByteCount));
			actualByteCount = input.read(byteValues);
		}
		scroll.accept(start);
	}

}