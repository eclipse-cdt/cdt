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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.cdt.debug.core.memory.transport.MemoryImport;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IMemoryBlockExtension;

class PlainTextImport extends MemoryImport {

	private static final int BUFFER_LENGTH = 64 * 1024;

	private final File inputFile;
	private final BigInteger startAddress;
	private final IMemoryBlockExtension memoryBlock;
	private final List<BigInteger> addresses;

	PlainTextImport(File input, BigInteger start, IMemoryBlockExtension memoryBlock) {
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
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)))) {
			BufferedMemoryWriter memoryWriter = new BufferedMemoryWriter(memoryBlock, BUFFER_LENGTH);
			BigInteger scrollToAddress = null;
			BigInteger jobs = BigInteger.valueOf(inputFile.length());
			BigInteger factor = BigInteger.ONE;
			if (jobs.compareTo(BigInteger.valueOf(0x7FFFFFFF)) > 0) {
				factor = jobs.divide(BigInteger.valueOf(0x7FFFFFFF));
				jobs = jobs.divide(factor);
			}
			monitor.beginTask(Messages.getString("Importer.ProgressTitle"), jobs.intValue()); //$NON-NLS-1$
			BigInteger recordAddress = startAddress;
			String line = reader.readLine();
			int lineNo = 1; // line error reporting
			while (line != null && !monitor.isCanceled()) {
				StringTokenizer st = new StringTokenizer(line, " "); //$NON-NLS-1$
				int bytesRead = 0;
				while (st.hasMoreElements()) {
					String valueString = (String) st.nextElement();
					int position = 0;
					byte data[] = new byte[valueString.length() / 2];
					for (int i = 0; i < data.length; i++) {
						try {
							data[i] = new BigInteger(valueString.substring(position++, position++ + 1), 16).byteValue();
						} catch (NumberFormatException ex) {
							throw new CoreException(new Status(IStatus.ERROR,
									MemoryTransportPlugin.getUniqueIdentifier(), DebugException.REQUEST_FAILED,
									String.format(Messages.getString("PlainTextImporter.ErrInvalidFormat"), //$NON-NLS-1$
											lineNo),
									ex));
						}
					}
					if (scrollToAddress == null) {
						scrollToAddress = recordAddress;
					}
					BigInteger writeAddress = recordAddress.subtract(memoryBlock.getBigBaseAddress())
							.add(BigInteger.valueOf(bytesRead));
					memoryWriter.write(writeAddress, data);
					bytesRead += data.length;
				}
				recordAddress = recordAddress.add(BigInteger.valueOf(bytesRead));
				BigInteger jobCount = BigInteger.valueOf(bytesRead).divide(factor);
				monitor.worked(jobCount.intValue());
				line = reader.readLine();
				lineNo++;
			}
			if (!monitor.isCanceled()) {
				memoryWriter.flush();
			}
		} catch (IOException ex) {
			requestFailed(Messages.getString("Importer.ErrReadFile"), ex); //$NON-NLS-1$
		} catch (DebugException ex) {
			requestFailed(Messages.getString("Importer.ErrWriteTarget"), ex); //$NON-NLS-1$
		} catch (CoreException ex) {
			failed(ex);
		} catch (Exception ex) {
			internalError(Messages.getString("Importer.FalureImporting"), ex); //$NON-NLS-1$
		} finally {
			monitor.done();
		}
	}
}