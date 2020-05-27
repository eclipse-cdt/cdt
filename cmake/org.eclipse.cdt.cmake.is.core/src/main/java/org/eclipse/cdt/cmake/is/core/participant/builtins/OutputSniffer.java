/*******************************************************************************
 * Copyright (c) 2018-2020 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.cmake.is.core.participant.builtins;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

import org.eclipse.cdt.cmake.is.core.participant.IRawIndexerInfoCollector;

/**
 * An OutputStream that passes each line written to it to a
 * IBuiltinsOutputProcessor.
 *
 * @author Martin Weber
 *
 * @implNote this is visible for testing only
 */
public class OutputSniffer extends OutputStream {

	private static final String SEP = System.lineSeparator();
	private final StringBuilder buffer;
	private final IBuiltinsOutputProcessor processor;
	private final IRawIndexerInfoCollector infoCollector;
	private final OutputStream os;

	/**
	 * @param outputStream  the OutputStream to write to or {@code null}
	 * @param infoCollector collector for information about C preprocessor symbols
	 *                      and include paths
	 */
	public OutputSniffer(IBuiltinsOutputProcessor processor, OutputStream outputStream,
			IRawIndexerInfoCollector infoCollector) {
		this.processor = Objects.requireNonNull(processor, "processor"); //$NON-NLS-1$
		this.infoCollector = Objects.requireNonNull(infoCollector, "infoCollector"); //$NON-NLS-1$
		this.os = outputStream;
		buffer = new StringBuilder(512);
	}

	@Override
	public void write(int c) throws IOException {
		if (os != null)
			os.write(c);
		synchronized (this) {
			buffer.append(new String(new byte[] { (byte) c }));
			splitLines();
		}
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		if (os != null)
			os.write(b, off, len);
		synchronized (this) {
			buffer.append(new String(b, off, len));
			splitLines();
		}
	}

	@Override
	public void flush() throws IOException {
		if (os != null)
			os.flush();
		synchronized (this) {
			splitLines();
			// process remaining bytes
			String line = buffer.toString();
			buffer.setLength(0);
			processLine(line);
		}
	}

	@Override
	public void close() throws IOException {
		if (os != null)
			os.close();
		flush();
	}

	/**
	 * Splits the buffer into separate lines and sends these to the parsers.
	 *
	 */
	private void splitLines() {
		int idx;
		while ((idx = buffer.indexOf(SEP)) != -1) {
			String line = buffer.substring(0, idx);
			buffer.delete(0, idx + SEP.length());
			processLine(line);
		}
	}

	/**
	 * @param line
	 */
	private void processLine(String line) {
		processor.processLine(line, infoCollector);
	}
}