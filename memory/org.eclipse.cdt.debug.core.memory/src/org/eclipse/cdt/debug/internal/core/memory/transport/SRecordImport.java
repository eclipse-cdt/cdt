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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.function.Consumer;

import org.eclipse.cdt.debug.core.memory.transport.FileImport;
import org.eclipse.cdt.debug.core.memory.transport.ImportRequest;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.osgi.framework.FrameworkUtil;

public class SRecordImport extends FileImport<BufferedReader> {

	private final boolean transfer;

	public SRecordImport(File input, ImportRequest request, Consumer<BigInteger> scroll, boolean transfer) {
		super(input, request, scroll);
		this.transfer = transfer;
	}

	@Override
	protected BufferedReader input(File file) throws FileNotFoundException {
		return new BufferedReader(new InputStreamReader(new FileInputStream(file)));
	}

	@Override
	protected void transfer(BufferedReader reader, BigInteger factor, IProgressMonitor monitor)
			throws IOException, DebugException {
		// FIXME 4 byte default
		final int CHECKSUM_LENGTH = 1;
		BigInteger scrollToAddress = null;
		BigInteger offset = null;
		if (!transfer) {
			offset = BigInteger.ZERO;
		}
		String line = reader.readLine();
		int lineNo = 1; // line error reporting
		while (line != null && !monitor.isCanceled()) {
			String recordType = line.substring(0, 2);
			int recordCount = 0;
			try {
				recordCount = Integer.parseInt(line.substring(2, 4), 16);
			} catch (NumberFormatException ex) {
				throw new DebugException(new Status(IStatus.ERROR,
						FrameworkUtil.getBundle(getClass()).getSymbolicName(), DebugException.REQUEST_FAILED,
						String.format(Messages.SRecordImport_e_invalid_line_length, lineNo), ex));
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
				throw new DebugException(new Status(IStatus.ERROR,
						FrameworkUtil.getBundle(getClass()).getSymbolicName(), DebugException.REQUEST_FAILED,
						String.format(Messages.SRecordImport_e_invalid_address, lineNo), ex));
			}
			recordCount -= addressSize;
			position += addressSize * 2;
			if (offset == null) {
				offset = start.subtract(recordAddress);
			}
			recordAddress = recordAddress.add(offset);
			byte data[] = new byte[recordCount - CHECKSUM_LENGTH];
			for (int i = 0; i < data.length; i++) {
				try {
					data[i] = new BigInteger(line.substring(position++, position++ + 1), 16).byteValue();
				} catch (NumberFormatException ex) {
					throw new DebugException(new Status(IStatus.ERROR,
							FrameworkUtil.getBundle(getClass()).getSymbolicName(), DebugException.REQUEST_FAILED,
							String.format(Messages.SRecordImport_e_invalid_data, lineNo), ex));
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
					throw new DebugException(new Status(IStatus.ERROR,
							FrameworkUtil.getBundle(getClass()).getSymbolicName(), DebugException.REQUEST_FAILED,
							String.format(Messages.SRecordImport_e_invalid_checksum_format, lineNo), ex));
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
				throw new DebugException(
						new Status(IStatus.ERROR, FrameworkUtil.getBundle(getClass()).getSymbolicName(),
								String.format(Messages.SRecordImport_e_checksum_failure, line)));
			}
			if (scrollToAddress == null) {
				scrollToAddress = recordAddress;
			}
			// FIXME error on incorrect checksum
			write.to(recordAddress.subtract(base), data);
			BigInteger jobCount = BigInteger.valueOf(bytesRead).divide(factor);
			monitor.worked(jobCount.intValue());
			line = reader.readLine();
			lineNo++;
		}
		scroll.accept(scrollToAddress);
	}

}