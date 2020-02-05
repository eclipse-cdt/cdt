/*******************************************************************************
 * Copyright (c) 2007, 2018 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Michael Scharf (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - [204796] Terminal should allow setting the encoding to use
 * Anton Leherbauer (Wind River) - [458398] Add support for normal/application cursor keys mode
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.test.ui;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

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
final class VT100DataSource implements IDataSource {
	VT100Emulator fEmulator;
	volatile int fAvailable;
	volatile int fRead;
	private final String fFile;

	VT100DataSource(String file) {
		fFile = file;
	}

	class InfiniteFileInputStream extends InputStream {
		public InfiniteFileInputStream() {
			try {
				fInputStream = new FileInputStream(fFile);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public int available() throws IOException {
			return fAvailable;
		}

		private InputStream fInputStream;

		@Override
		public int read() throws IOException {
			throw new IOException();
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			while (fAvailable == 0) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
			len = fAvailable;
			int n = fInputStream.read(b, off, len);
			if (n <= 0) {
				fInputStream.close();
				fInputStream = new FileInputStream(fFile);
				n = fInputStream.read(b, off, len);
			}
			fAvailable -= n;
			return n;
		}

	}

	void init(ITerminalTextData terminal) {
		final Reader reader = new InputStreamReader(new InfiniteFileInputStream(), StandardCharsets.ISO_8859_1);
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
			fAvailable = 80;
			fEmulator.processText();
		}
		return 80;
	}
}
