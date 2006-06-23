/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.spawner;


import java.io.IOException;
import java.io.InputStream;

import org.eclipse.cdt.core.CCorePlugin;

class SpawnerInputStream extends InputStream {
	private int fd;

	/**
	 * Fome a Unix valid file descriptor set a Reader.
	 * @param desc file descriptor.
	 */
	public SpawnerInputStream(int fd) {
		this.fd = fd;
	}

	/**
	 * Implementation of read for the InputStream.
	 *
	 * @exception IOException on error.
	 */
	public int read() throws IOException {
		byte b[] = new byte[1];
		if (1 != read(b, 0, 1))
			return -1;
		return b[0];
	}

	/**
	 * @see InputStream#read(byte[], int, int)
	 */
	public int read(byte[] buf, int off, int len) throws IOException {
		if (buf == null) {
			throw new NullPointerException();
		} else if (
			(off < 0)
				|| (off > buf.length)
				|| (len < 0)
				|| ((off + len) > buf.length)
				|| ((off + len) < 0)) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return 0;
		}
		byte[] tmpBuf = new byte[len];

		len = read0(fd, tmpBuf, len);
		if (len <= 0)
			return -1;

		System.arraycopy(tmpBuf, 0, buf, off, len);
		return len;
	}

	/**
	 * Close the Reader
	 * @exception IOException on error.
	 */
	public void close() throws IOException {
		if (fd == -1)
			return;
		int status = close0(fd);
		if (status == -1)
			throw new IOException(CCorePlugin.getResourceString("Util.exception.closeError")); //$NON-NLS-1$
		fd = -1;
	}

	public int available() throws IOException {
		try {
			return available0(fd);
		}
		catch (UnsatisfiedLinkError e) {
			// for those platforms that do not implement available0
			return super.available();
		}
	}

	private native int read0(int fileDesc, byte[] buf, int len) throws IOException;
	private native int close0(int fileDesc) throws IOException;
	private native int available0(int fileDesc) throws IOException;

	static {
		System.loadLibrary("spawner"); //$NON-NLS-1$
	}


}
