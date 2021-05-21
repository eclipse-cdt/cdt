/*******************************************************************************
 * Copyright (c) 2021 Kichwa Coders Canada Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.utils.pty;

import java.io.IOException;
import java.io.InputStream;

/**
 * @noreference This class is not intended to be referenced by clients.
 */
public class ConPTYInputStream extends PTYInputStream {

	private ConPTY conPty;

	public ConPTYInputStream(ConPTY conPty) {
		super(null);
		this.conPty = conPty;
	}

	/**
	 * @see InputStream#read(byte[], int, int)
	 */
	@Override
	public int read(byte[] buf, int off, int len) throws IOException {
		if (buf == null) {
			throw new NullPointerException();
		} else if ((off < 0) || (off > buf.length) || (len < 0) || ((off + len) > buf.length) || ((off + len) < 0)) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return 0;
		}
		byte[] tmpBuf = new byte[len];

		len = conPty.read(tmpBuf);
		if (len <= 0)
			return -1;

		System.arraycopy(tmpBuf, 0, buf, off, len);
		return len;
	}

	/**
	 * Close the Reader
	 * @exception IOException on error.
	 */
	@Override
	public void close() throws IOException {
		if (conPty == null) {
			return;
		}
		try {
			conPty.close();
		} finally {
			conPty = null;
		}

	}

}
