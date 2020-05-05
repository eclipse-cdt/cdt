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

public final class SRecordExport extends FileExport {

	public SRecordExport(File input, ExportRequest request) {
		super(input, request);
	}

	@Override
	protected BigInteger chunkSize() {
		// FIXME 4 byte default
		return BigInteger.valueOf(16);
	}

	@Override
	protected void transfer(OutputStream output, BigInteger factor, IProgressMonitor monitor)
			throws IOException, DebugException {
		final BigInteger DATA_PER_RECORD = chunkSize();
		final BigInteger DATA_PER_TRANSFER = BigInteger.valueOf(4096).multiply(DATA_PER_RECORD);
		BigInteger jobCount = BigInteger.ZERO;
		BigInteger transferAddress = start;
		while (transferAddress.compareTo(end) < 0 && !monitor.isCanceled()) {
			BigInteger length = DATA_PER_TRANSFER;
			if (end.subtract(transferAddress).compareTo(length) < 0) {
				length = end.subtract(transferAddress);
			}
			monitor.subTask(transferring(length, transferAddress));
			MemoryByte[] bytes = read.from(transferAddress, length.longValue() / addressable.longValue());
			BigInteger sRecordAddress = transferAddress;
			BigInteger sRecordEndAddress = transferAddress.add(length);
			while (sRecordAddress.compareTo(sRecordEndAddress) < 0 && !monitor.isCanceled()) {
				BigInteger sRecordDataLength = DATA_PER_RECORD;
				if (sRecordEndAddress.subtract(sRecordAddress).compareTo(sRecordDataLength) < 0) {
					sRecordDataLength = end.subtract(sRecordAddress);
				}
				output.write("S3".getBytes()); // FIXME 4 byte address //$NON-NLS-1$

				StringBuilder buf = new StringBuilder();
				BigInteger sRecordLength = BigInteger.valueOf(4); // address size
				sRecordLength = sRecordLength.add(sRecordDataLength);
				sRecordLength = sRecordLength.add(BigInteger.ONE); // checksum
				String transferAddressString = sRecordAddress.toString(16);
				String lengthString = sRecordLength.toString(16);
				if (lengthString.length() == 1) {
					buf.append("0"); //$NON-NLS-1$
				}
				buf.append(lengthString);
				for (int i = 0; i < 8 - transferAddressString.length(); i++) {
					buf.append("0"); //$NON-NLS-1$
				}
				buf.append(transferAddressString);
				final int byteOffset = sRecordAddress.subtract(transferAddress).intValue();
				final int byteLength = byteOffset + sRecordDataLength.intValue();
				for (int byteIndex = byteOffset; byteIndex < byteLength; byteIndex++) {
					//FIXME: check MemoryByte#isReadable
					String bString = BigInteger.valueOf(0xFF & bytes[byteIndex].getValue()).toString(16);
					if (bString.length() == 1) {
						buf.append("0"); //$NON-NLS-1$
					}
					buf.append(bString);
				}
				/*
				 * The least significant byte of the one's complement of the sum of the values
				 * represented by the pairs of characters making up the records length, address,
				 * and the code/data fields.
				 */
				byte checksum = 0;
				for (int i = 0; i < buf.length(); i += 2) {
					BigInteger value = new BigInteger(buf.substring(i, i + 2), 16);
					checksum += value.byteValue();
				}
				String bString = BigInteger.valueOf(0xFF - checksum).and(BigInteger.valueOf(0xFF)).toString(16);
				if (bString.length() == 1) {
					buf.append("0"); //$NON-NLS-1$
				}
				buf.append(bString);
				output.write(buf.toString().toUpperCase().getBytes());
				output.write("\n".getBytes()); //$NON-NLS-1$
				sRecordAddress = sRecordAddress.add(sRecordDataLength);
				jobCount = jobCount.add(BigInteger.ONE);
				if (jobCount.compareTo(factor) == 0) {
					jobCount = BigInteger.ZERO;
					monitor.worked(1);
				}
			}
			transferAddress = transferAddress.add(length);
		}
	}

}