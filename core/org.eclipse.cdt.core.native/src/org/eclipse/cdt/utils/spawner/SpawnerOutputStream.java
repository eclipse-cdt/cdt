/*******************************************************************************
 * Copyright (c) 2000, 2014 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.spawner;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class SpawnerOutputStream extends OutputStream {
	private int fd;

	/**
	 * From a Unix valid file descriptor set a Reader.
	 * @param fd file descriptor.
	 */
	public SpawnerOutputStream(int fd) {
		this.fd = fd;
	}

	/**
	 * @see OutputStream#write(byte[], int, int)
	 */
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
		System.arraycopy(b, off, tmpBuf, off, len);
		write0(fd, tmpBuf, len);
	}

	/**
	 * Implementation of read for the InputStream.
	 *
	 * @exception IOException on error.
	 */
	@Override
	public void write(int b) throws IOException {
		byte[] buf = new byte[1];
		buf[0] = (byte) b;
		write(buf, 0, 1);
	}

	/**
	 * Close the Reader
	 * @exception IOException on error.
	 */
	@Override
	public void close() throws IOException {
		if (fd == -1)
			return;
		int status = close0(fd);
		if (status == -1)
			throw new IOException("close error"); //$NON-NLS-1$
		fd = -1;
	}

	@Override
	protected void finalize() throws IOException {
		close();
	}

	private native int write0(int fd, byte[] b, int len) throws IOException;

	private native int close0(int fd);

	static {
		System.loadLibrary("spawner"); //$NON-NLS-1$
	}

}
