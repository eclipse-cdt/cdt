/*******************************************************************************
 * Copyright (c) 2008, 2009 MontaVista Software, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Anna Dushistova  (MontaVista) - initial API and implementation
 * Martin Oberhuber (Wind River) - [240523] [rseterminals] Provide a generic adapter factory that adapts any ITerminalService to an IShellService
 *******************************************************************************/

package org.eclipse.rse.internal.services.telnet.terminal;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @since 2.0
 */
public class EOFDetectingInputStreamWrapper extends FilterInputStream {

	private boolean fEOF = false;

	public EOFDetectingInputStreamWrapper(InputStream origStream) {
		super(origStream);
	}

	public synchronized boolean isEOF() {
		return fEOF;
	}

	public synchronized void setEOF(boolean eof) {
		fEOF = eof;
	}

	public int read() throws IOException {
		try {
			int result = in.read();
			if (result < 0) {
				setEOF(true);
			}
			return result;
		} catch (IOException e) {
			setEOF(true);
			throw (e);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.io.InputStream#close()
	 */
	public void close() throws IOException {
		try {
			in.close();
		} finally {
			setEOF(true);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
	public int read(byte[] b, int off, int len) throws IOException {
		try {
			int result = in.read(b, off, len);
			if (result < 0)
				setEOF(true);
			return result;
		} catch (IOException e) {
			setEOF(true);
			throw (e);
		}
	}
}
