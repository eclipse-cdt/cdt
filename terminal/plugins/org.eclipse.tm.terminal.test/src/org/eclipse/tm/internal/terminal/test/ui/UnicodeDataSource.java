/*******************************************************************************
 * Copyright (c) 2007, 2021 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Used VT100DataSource as a starting point
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.test.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.eclipse.tm.internal.terminal.control.impl.ITerminalControlForText;
import org.eclipse.tm.internal.terminal.emulator.VT100Emulator;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
import org.eclipse.tm.terminal.model.ITerminalTextData;

/**
 * Reads the file in an infinite loop.
 * Makes lines containing 'x' bold.
 *
 */
final class UnicodeDataSource implements IDataSource {
	private static final boolean COLOUR_ESCAPE = true;
	VT100Emulator fEmulator;
	private int fRead;
	private final String fFile;
	private final String unicodeString;

	UnicodeDataSource(String file) {
		fFile = file;
		unicodeString = null;
	}

	UnicodeDataSource() {
		fFile = null;
		StringBuilder raw = new StringBuilder();
		raw.append("Narrow - no surrogates\n");
		raw.append("x\n");
		raw.append("xx\n");
		raw.append("xxx\n");
		raw.append("Wide - no surrogates\n");
		assert "⌚".length() == 1;
		raw.append("⌚\n");
		raw.append("⌚⌚\n");
		raw.append("⌚⌚⌚\n");
		// TODO narrow character with surrogates
		raw.append("Wide - surrogates\n");
		assert "🐑".length() == 2;
		assert "🐑".codePoints().count() == 1;
		raw.append("🐑\n");
		raw.append("🐑🐑\n");
		raw.append("🐑🐑🐑\n");
		raw.append("🐑".repeat(40)); // No newline on purpose, we write to last column, so newline is implied
		raw.append("X".repeat(80)); // used to help see next line
		raw.append("X".repeat(79) + "🐑"); // The last X should be on one line, the sheep on the next line
		raw.append("\nEND\n");

		int i[] = { 0 };
		String decorated = raw.codePoints().mapToObj(cp -> {
			StringBuilder sb = new StringBuilder();
			if (!COLOUR_ESCAPE || cp == '\n') {
				sb.appendCodePoint(cp);
			} else {
				sb.append("\033[38:5:");
				sb.append(i[0]);
				i[0]++;
				i[0] = i[0] % 256;
				sb.append("m");
				sb.appendCodePoint(cp);
				sb.append("\033[0m");
			}
			return sb.toString();

		}).collect(Collectors.joining());

		unicodeString = decorated;
		System.out.println(raw.toString());
	}

	class InfiniteFileInputStream extends InputStream {
		public InfiniteFileInputStream() {
			startInputStream();

		}

		private void startInputStream() {
			if (fFile == null) {
				fInputStream = new ByteArrayInputStream(unicodeString.getBytes(StandardCharsets.UTF_8));
			} else {
				try {
					fInputStream = new FileInputStream(fFile);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		@Override
		public int available() throws IOException {
			return fRead == 0 ? 80 : 0;
		}

		private InputStream fInputStream;

		@Override
		public int read() throws IOException {
			throw new IOException();
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			int available = Math.min(available(), len);
			if (available == 0) {
				available = Math.min(1, len);
			}
			int n = fInputStream.read(b, off, available);
			if (n <= 0) {
				fInputStream.close();
				startInputStream();
				n = fInputStream.read(b, off, available);
			}
			fRead += n;
			return n;
		}

	}

	void init(ITerminalTextData terminal) {
		final Reader reader = new InputStreamReader(new InfiniteFileInputStream(), StandardCharsets.UTF_8);
		fEmulator = new VT100Emulator(terminal, new ITerminalControlForText() {

			public void disconnectTerminal() {
				// TODO Auto-generated method stub

			}

			@Override
			public OutputStream getOutputStream() {
				return new ByteArrayOutputStream();
			}

			@Override
			public TerminalState getState() {
				return TerminalState.CONNECTED;
			}

			@Override
			public ITerminalConnector getTerminalConnector() {
				return null;
			}

			@Override
			public void setState(TerminalState state) {
			}

			@Override
			public void setTerminalTitle(String title) {
			}

			@Override
			public void enableApplicationCursorKeys(boolean enable) {
			}
		}, reader);
	}

	@Override
	public int step(ITerminalTextData terminal) {
		synchronized (terminal) {
			if (fEmulator == null) {
				init(terminal);
				//				fEmulator.setDimensions(48, 132);
				fEmulator.setDimensions(24, 80);
				fEmulator.setCrAfterNewLine(true);

			}
			fRead = 0;
			fEmulator.processText();
		}
		return fRead;
	}
}
