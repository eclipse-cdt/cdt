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
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.memory.transport.MemoryImport;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IMemoryBlockExtension;

class RAWBinaryImport extends MemoryImport {

	private static final int BUFFER_LENGTH = 64 * 1024;

	private final File inputFile;
	private final BigInteger startAddress;
	private final IMemoryBlockExtension memoryBlock;
	private final List<BigInteger> addresses;

	RAWBinaryImport(File input, BigInteger start, IMemoryBlockExtension memoryBlock) {
		this.addresses = new ArrayList<>(1);
		this.memoryBlock = memoryBlock;
		this.inputFile = input;
		this.startAddress = start;
	}

	BigInteger recordAddress() {
		return addresses.isEmpty() ? startAddress : addresses.get(0);
	}

	@Override
	public void run(IProgressMonitor monitor) throws CoreException {
		try (FileInputStream reader = new FileInputStream(inputFile)) {
			BufferedMemoryWriter memoryWriter = new BufferedMemoryWriter(memoryBlock, BUFFER_LENGTH);
			BigInteger scrollToAddress = null;
			BigInteger jobs = BigInteger.valueOf(inputFile.length());
			BigInteger factor = BigInteger.ONE;
			if (jobs.compareTo(BigInteger.valueOf(0x7FFFFFFF)) > 0) {
				factor = jobs.divide(BigInteger.valueOf(0x7FFFFFFF));
				jobs = jobs.divide(factor);
			}
			byte[] byteValues = new byte[1024];
			monitor.beginTask(Messages.getString("Importer.ProgressTitle"), jobs.intValue()); //$NON-NLS-1$
			int actualByteCount = reader.read(byteValues);
			BigInteger recordAddress = startAddress;
			while (actualByteCount != -1 && !monitor.isCanceled()) {
				byte data[] = new byte[actualByteCount];
				for (int i = 0; i < data.length; i++) {
					data[i] = byteValues[i];
				}
				if (scrollToAddress == null) {
					scrollToAddress = recordAddress;
				}
				memoryWriter.write(recordAddress.subtract(memoryBlock.getBigBaseAddress()), data);
				BigInteger jobCount = BigInteger.valueOf(actualByteCount).divide(factor);
				monitor.worked(jobCount.intValue());
				recordAddress = recordAddress.add(BigInteger.valueOf(actualByteCount));
				actualByteCount = reader.read(byteValues);
			}
			if (!monitor.isCanceled()) {
				memoryWriter.flush();
			}
			monitor.done();
		} catch (IOException ex) {
			requestFailed(Messages.getString("Importer.ErrReadFile"), ex); //$NON-NLS-1$
		} catch (DebugException ex) {
			requestFailed(Messages.getString("Importer.ErrWriteTarget"), ex); //$NON-NLS-1$
		} catch (Exception ex) {
			internalError(Messages.getString("Importer.FalureImporting"), ex); //$NON-NLS-1$
		} finally {
			monitor.done();
		}
	}
}