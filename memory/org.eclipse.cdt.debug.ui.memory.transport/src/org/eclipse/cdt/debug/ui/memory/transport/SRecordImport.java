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

import org.eclipse.cdt.debug.core.memory.transport.MemoryImport;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IMemoryBlockExtension;

class SRecordImport extends MemoryImport {

	private static final int BUFFER_LENGTH = 64 * 1024;

	private final IMemoryBlockExtension memoryBlock;
	private final File inputFile;
	private final BigInteger startAddress;
	private final List<BigInteger> addresses;
	private final boolean transfer;

	SRecordImport(File input, BigInteger start, IMemoryBlockExtension memoryBlock, boolean transfer) {
		this.addresses = new ArrayList<>(1);
		this.memoryBlock = memoryBlock;
		this.inputFile = input;
		this.startAddress = start;
		this.transfer = transfer;
	}

	BigInteger recordAddress() {
		return addresses.isEmpty() ? startAddress : addresses.get(0);
	}

	@Override
	public void run(IProgressMonitor monitor) throws CoreException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)))) {
			BufferedMemoryWriter memoryWriter = new BufferedMemoryWriter(memoryBlock, BUFFER_LENGTH);
			// FIXME 4 byte default
			final int CHECKSUM_LENGTH = 1;
			BigInteger scrollToAddress = null;
			BigInteger offset = null;
			if (!transfer) {
				offset = BigInteger.ZERO;
			}
			BigInteger jobs = BigInteger.valueOf(inputFile.length());
			BigInteger factor = BigInteger.ONE;
			if (jobs.compareTo(BigInteger.valueOf(0x7FFFFFFF)) > 0) {
				factor = jobs.divide(BigInteger.valueOf(0x7FFFFFFF));
				jobs = jobs.divide(factor);
			}
			monitor.beginTask(Messages.getString("Importer.ProgressTitle"), jobs.intValue()); //$NON-NLS-1$
			String line = reader.readLine();
			int lineNo = 1; // line error reporting
			while (line != null && !monitor.isCanceled()) {
				String recordType = line.substring(0, 2);
				int recordCount = 0;
				try {
					recordCount = Integer.parseInt(line.substring(2, 4), 16);
				} catch (NumberFormatException ex) {
					throw new CoreException(new Status(IStatus.ERROR, MemoryTransportPlugin.getUniqueIdentifier(),
							DebugException.REQUEST_FAILED,
							String.format(Messages.getString("SRecordImporter.InvalidLineLength"), lineNo), ex)); //$NON-NLS-1$
				}
				int bytesRead = 4 + recordCount;
				int position = 4;
				int addressSize = 0;
				BigInteger recordAddress = null;
				if ("S3".equals(recordType)) //$NON-NLS-1$
					addressSize = 4;
				else if ("S1".equals(recordType)) //$NON-NLS-1$
					addressSize = 2;
				else if ("S2".equals(recordType)) //$NON-NLS-1$
					addressSize = 3;
				else if ("S0".equals(recordType) || "S5".equals(recordType) || "S7".equals(recordType) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						|| "S8".equals(recordType) || "S9".equals(recordType)) //$NON-NLS-1$ //$NON-NLS-2$
				{ // ignore S0, S5, S7, S8 and S9 records
					line = reader.readLine();
					lineNo++;
					continue;
				}
				try {
					recordAddress = new BigInteger(line.substring(position, position + addressSize * 2), 16);
				} catch (NumberFormatException ex) {
					throw new CoreException(new Status(IStatus.ERROR, MemoryTransportPlugin.getUniqueIdentifier(),
							DebugException.REQUEST_FAILED,
							String.format(Messages.getString("SRecordImporter.InvalidAddress"), lineNo), ex)); //$NON-NLS-1$
				}
				recordCount -= addressSize;
				position += addressSize * 2;
				if (offset == null) {
					offset = startAddress.subtract(recordAddress);
				}
				recordAddress = recordAddress.add(offset);
				byte data[] = new byte[recordCount - CHECKSUM_LENGTH];
				for (int i = 0; i < data.length; i++) {
					try {
						data[i] = new BigInteger(line.substring(position++, position++ + 1), 16).byteValue();
					} catch (NumberFormatException ex) {
						throw new CoreException(new Status(IStatus.ERROR, MemoryTransportPlugin.getUniqueIdentifier(),
								DebugException.REQUEST_FAILED,
								String.format(Messages.getString("SRecordImporter.InvalidData"), lineNo), ex)); //$NON-NLS-1$
					}
				}
				/*
				 * The least significant byte of the one's complement of the sum of the values
				 * represented by the pairs of characters making up the records length, address,
				 * and the code/data fields.
				 */
				StringBuilder buf = new StringBuilder(line.substring(2));
				byte checksum = 0;
				for (int i = 0; i < buf.length(); i += 2) {
					BigInteger value = null;
					try {
						value = new BigInteger(buf.substring(i, i + 2), 16);
					} catch (NumberFormatException ex) {
						throw new CoreException(new Status(IStatus.ERROR, MemoryTransportPlugin.getUniqueIdentifier(),
								DebugException.REQUEST_FAILED,
								String.format(Messages.getString("SRecordImporter.InvalidChecksum"), lineNo), //$NON-NLS-1$
								ex));
					}
					checksum += value.byteValue();
				}
				/*
				 * Since we included the checksum in the checksum calculation the checksum
				 * ( if correct ) will always be 0xFF which is -1 using the signed byte size
				 * calculation here.
				 */
				if (checksum != (byte) -1) {
					monitor.done();
					throw new CoreException(new Status(IStatus.ERROR, MemoryTransportPlugin.getUniqueIdentifier(),
							Messages.getString("SRecordImporter.ChecksumFalure") + line)); //$NON-NLS-1$
				}
				if (scrollToAddress == null) {
					scrollToAddress = recordAddress;
				}
				// FIXME error on incorrect checksum
				memoryWriter.write(recordAddress.subtract(memoryBlock.getBigBaseAddress()), data);
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