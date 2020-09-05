/*******************************************************************************
 * Copyright (c) 2020 Martin Weber.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.cmake.core.internal;

import java.io.IOException;
import java.util.Objects;

import org.eclipse.cdt.core.ConsoleOutputStream;

/**
 * Intercepts output to a console output stream and forwards it to a CMakeErrorParser for processing.
 *
 * @author Martin Weber
 */
class ParsingConsoleOutputStream extends ConsoleOutputStream {

	private final ConsoleOutputStream os;
	private final CMakeErrorParser parser;

	/**
	 * @param outputStream
	 *          the OutputStream to write to
	 * @param cmakeErrorParser
	 * 			the CMakeErrorParser for processing the output
	 */
	public ParsingConsoleOutputStream(ConsoleOutputStream outputStream, CMakeErrorParser cmakeErrorParser) {
		this.os = Objects.requireNonNull(outputStream);
		this.parser = Objects.requireNonNull(cmakeErrorParser);
	}

	@Override
	public void write(int c) throws IOException {
		write(new byte[] { (byte) c }, 0, 1);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		os.write(b, off, len);
		parser.addInput(new String(b, off, len));
	}

	// interface ConsoleOutputStream
	@Override
	public synchronized void write(String msg) throws IOException {
		os.write(msg);
		parser.addInput(msg);
	}

	@Override
	public void flush() throws IOException {
		os.flush();
	}

	@Override
	public void close() throws IOException {
		os.close();
		parser.close();
	}
}
