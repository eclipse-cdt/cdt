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

/**
 * @noreference This class is not intended to be referenced by clients.
 */
public class ConPTYOutputStream extends PTYOutputStream {
	private ConPTY conPty;

	public ConPTYOutputStream(ConPTY conPty) {
		super(null, false);
		this.conPty = conPty;
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		if (b == null) {
			throw new NullPointerException();
		} else if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0)) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return;
		}
		byte[] tmpBuf = new byte[len];
		System.arraycopy(b, off, tmpBuf, 0, len);
		conPty.write(tmpBuf);
	}

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
