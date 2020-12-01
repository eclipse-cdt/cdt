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
import java.util.StringTokenizer;
import java.util.function.Consumer;

import org.eclipse.cdt.debug.core.memory.transport.FileImport;
import org.eclipse.cdt.debug.core.memory.transport.ImportRequest;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.osgi.framework.FrameworkUtil;

public final class PlainTextImport extends FileImport<BufferedReader> {

	public PlainTextImport(File input, ImportRequest request, Consumer<BigInteger> scroll) {
		super(input, request, scroll);
	}

	@Override
	protected BufferedReader input(File file) throws FileNotFoundException {
		return new BufferedReader(new InputStreamReader(new FileInputStream(file)));
	}

	@Override
	protected void transfer(BufferedReader reader, BigInteger factor, IProgressMonitor monitor)
			throws IOException, DebugException {
		BigInteger scrollToAddress = null;
		BigInteger recordAddress = start;
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
						throw new DebugException(new Status(IStatus.ERROR,
								FrameworkUtil.getBundle(getClass()).getSymbolicName(), DebugException.REQUEST_FAILED,
								String.format(Messages.PlainTextImport_e_invalid_format, lineNo), ex));
					}
				}
				if (scrollToAddress == null) {
					scrollToAddress = recordAddress;
				}
				BigInteger writeAddress = recordAddress.subtract(base).add(BigInteger.valueOf(bytesRead));
				write.to(writeAddress, data);
				bytesRead += data.length;
			}
			recordAddress = recordAddress.add(BigInteger.valueOf(bytesRead));
			BigInteger jobCount = BigInteger.valueOf(bytesRead).divide(factor);
			monitor.worked(jobCount.intValue());
			line = reader.readLine();
			lineNo++;
		}
		scroll.accept(scrollToAddress);
	}
}