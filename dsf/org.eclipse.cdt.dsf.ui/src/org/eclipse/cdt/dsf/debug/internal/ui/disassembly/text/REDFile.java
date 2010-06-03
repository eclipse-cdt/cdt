/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.text;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * File with buffered character access.
 */
public final class REDFile {

	/**
	 * File cache object.
	 * TLETODO Use CharBuffer?
	 * @invariant fSize <= fcBufSize
	 * @invariant fSize >= 0
	 * @invariant fOffset <= fFile.length()
	 */
	private final static class Buffer {

		final static int fcBufSize = 2048;

		boolean fDirty;
		int fOffset = -1;
		int fPos;
		int fSize;
		char fData[] = new char[fcBufSize];

		Buffer() {
		}

		/**
		 * Check if the file offset is contained in this buffer.
		 * @param pos
		 * @return
		 */
		boolean containsOffset(int pos) {
			return fOffset < pos || fOffset + fSize <= pos;
		}

		/**
		 * @return number of available characters.
		 */
		public int avail() {
			return fSize - fPos;
		}

		/**
		 * @return number of free space.
		 */
		public int free() {
			return fcBufSize - fPos;
		}
	}

	final static private boolean DEBUG = false;

	/** The maximum number of buffers for this file. */
	final static public int fcNrBufs = 4;

	private RandomAccessFile fFile;
	private int fPosition;
	private int fLength;
	private Buffer fBuffer[] = new Buffer[fcNrBufs];
	private byte[] fByteBuffer = new byte[2 * Buffer.fcBufSize];
	private int fSwapper;
	private String fName;
	private boolean fReadonly;
	private boolean fDeleteOnDispose;

	private REDFile(File file, boolean readonly) {
		assert !readonly || file != null;
		fReadonly = readonly;
		fDeleteOnDispose = file == null;
		if (file != null) {
			try {
				setFile(file);
				fLength = (int)(fFile.length() / 2);
			} catch (IOException ioe) {
				throw new Error(ioe);
			}
		}
	}

	public REDFile() {
		this((File)null, false);
	}

	public REDFile(String name, boolean readonly) {
		this(new File(name), readonly);
	}

	public REDFile(String name) {
		this(name, false);
	}

	private void setFile(File file) throws IOException {
		assert file != null;
		if (fReadonly) {
			fFile = new RandomAccessFile(file, "r"); //$NON-NLS-1$
			fName = file.toString();
		} else if (file != null) {
			fFile = new RandomAccessFile(file, "rw"); //$NON-NLS-1$
			fName = file.toString();
		}
	}

	/**
	 * Free resources.
	 */
	public void dispose() {
		if (fFile != null) {
			try {
				close();
			} catch (IOException e) {
			}
			fFile = null;
			if (fDeleteOnDispose) {
				new File(fName).delete();
			}
		}
	}

	public void close() throws IOException {
		flush();
		if (fFile != null) {
			fFile.close();
		}
	}

	/**
	 * Flush buffers.
	 * @throws IOException
	 */
	public void flush() throws IOException {
		for (int i = 0; i < fcNrBufs; i++) {
			if (fBuffer[i] != null) {
				if (fBuffer[i].fDirty) {
					flush(fBuffer[i]);
				}
				fBuffer[i] = null;
			}
		}
	}

	/**
	 * Flush a dirty buffer.
	 * @param buffer
	 */
	private void flush(Buffer buffer) throws IOException {
		assert buffer.fDirty;
		write(buffer.fOffset, buffer.fData, 0, buffer.fSize);
		buffer.fDirty = false;
	}

	/**
	 * @return true if this file is readonly.
	 */
	public boolean isReadonly() {
		return fReadonly;
	}

	/**
	 * @return the length in char units.
	 */
	public int length() {
		if (fLength < 0) {
			if (fFile == null) {
				fLength = 0;
			} else {
				try {
					fLength = (int)(fFile.length() / 2);
				} catch (IOException e) {
					fLength = 0;
				}
			}
		}
		return fLength;
	}

	/** erase file content
	  * @return true, if successful; false otherwise
	  * @post return == true implies length() == 0
	  */
	public boolean purge() throws IOException {
		if (isReadonly()) {
			return false;
		}
		fFile.setLength(0);
		for (int i = 0; i < fcNrBufs; i++) {
			if (fBuffer[i] != null) {
				fBuffer[i].fOffset = -1;
				fBuffer[i].fDirty = false;
				if (i > 0) {
					fBuffer[i] = null;
				}
			}
		}
		fLength = 0;
		return true;
	}

	@Override
	protected void finalize() {
		dispose();
	}

	private File createTmpFile() {
		try {
			File file = File.createTempFile("scratch", ".tmp"); //$NON-NLS-1$ //$NON-NLS-2$
			file.deleteOnExit();
			return file;
		} catch (IOException e) {
			throw new Error(e);
		}
	}

	/**
	 * copy file - Convenience method.
	 * @pre src != null
	 * @pre dest != null
	 */
	public static void copyFile(REDFile src, REDFile dest) throws IOException {
		dest.purge();
		byte buf[] = new byte[4096];
		int n = src.fFile.read(buf);
		while (n >= 0) {
			if (n > 0) {
				dest.fFile.write(buf, 0, n);
			}
			n = src.fFile.read(buf);
		}
		dest.fLength = src.length();
	}

	/**
	 * @param offset
	 */
	public void seek(int offset) throws IOException {
		if (offset < 0) {
			throw new IOException("Negative seek position"); //$NON-NLS-1$
		}
		fPosition = offset;
	}

	/**
	 * Write char array as 16-bit Unicode at absolute position.
	 * @param position File position
	 * @param data
	 * @param offset
	 * @param length
	 * @throws IOException
	 */
	private void write(int position, char[] data, int offset, int length) throws IOException {
		if (DEBUG)
			System.out.println("REDFile.write " + length + " at " + position); //$NON-NLS-1$ //$NON-NLS-2$
		if (fFile == null) {
			setFile(createTmpFile());
		}
		fFile.seek(position * 2);
		int blen = 0;
		for (int clen = 0; clen < length; ++clen) {
			char c = data[offset + clen];
			fByteBuffer[blen++] = (byte) ((c >>> 8) & 0xff);
			fByteBuffer[blen++] = (byte) ((c >>> 0) & 0xff);
			if (blen == fByteBuffer.length) {
				fFile.write(fByteBuffer, 0, blen);
				blen = 0;
			}
		}
		if (blen > 0) {
			fFile.write(fByteBuffer, 0, blen);
		}
	}

	/**
	 * Write char array as UTF-16 bytes (2 bytes each).
	 * @param data
	 * @param offset
	 * @param length
	 * @throws IOException
	 */
	public void writeBuffered(char[] data, int offset, int length) throws IOException {
		if (isReadonly()) {
			throw new IOException("Cannot write to readonly file"); //$NON-NLS-1$
		}
		Buffer buffer = null;
		int clen = 0;
		while (clen < length) {
			buffer = getBufferForOffset(fPosition, true);
			int count = Math.min(length - clen, buffer.free());
			if (count == 0) {
				break;
			}
			System.arraycopy(data, offset, buffer.fData, buffer.fPos, count);
			buffer.fPos += count;
			if (buffer.fPos > buffer.fSize) {
				buffer.fSize = buffer.fPos;
			}
			buffer.fDirty = true;
			offset += count;
			clen += count;
			fPosition += count;
		}
		if (fPosition > fLength) {
			fLength = fPosition;
		}
	}

	/**
	 * Write String as UTF-16 bytes (2 bytes each).
	 * @param data
	 * @param offset
	 * @param length
	 * @throws IOException
	 */
	public void writeBuffered(String data, int offset, int length) throws IOException {
		if (isReadonly()) {
			throw new IOException("Cannot write to readonly file"); //$NON-NLS-1$
		}
		Buffer buffer = null;
		int clen = 0;
		while (clen < length) {
			buffer = getBufferForOffset(fPosition, true);
			int count = Math.min(length - clen, buffer.free());
			if (count == 0) {
				break;
			}
			data.getChars(offset, offset + count, buffer.fData, buffer.fPos);
			buffer.fPos += count;
			if (buffer.fPos > buffer.fSize) {
				buffer.fSize = buffer.fPos;
			}
			buffer.fDirty = true;
			offset += count;
			clen += count;
			fPosition += count;
		}
		if (fPosition > fLength) {
			fLength = fPosition;
		}
	}

	/**
	 * Update the content of a buffer.
	 * @param buffer
	 * @throws IOException
	 */
	private void update(Buffer buffer) throws IOException {
		assert !buffer.fDirty;
		buffer.fSize = read(buffer.fOffset, buffer.fData, 0, buffer.fData.length);
	}

	/**
	 * Read an array of characters  at absolute position.
	 * @param position File position
	 * @param data
	 * @param offset
	 * @param length
	 * @return number of characters read.
	 */
	private int read(int position, char[] data, int offset, int length) throws IOException {
		fFile.seek(position * 2);
		int blen = 0;
		int bsize = length * 2;
		while (blen < bsize) {
			int count = fFile.read(fByteBuffer, 0, Math.min(fByteBuffer.length, bsize - blen));
			if (count < 0) {
				break;
			}
			for (int i = 0; i < count; i += 2) {
				int hiByte = fByteBuffer[i] & 0xff;
				int loByte = fByteBuffer[i + 1] & 0xff;
				data[offset++] = (char) ((hiByte << 8) | (loByte << 0));
			}
			blen += count;
		}
		if (DEBUG)
			System.out.println("REDFile.read " + length + " at " + position + " = " + blen / 2); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		// convert to character length
		return blen / 2;
	}

	/**
	 * Read an array of characters.
	 * @param data
	 * @param offset
	 * @param length
	 * @return number of characters read.
	 */
	public int readBuffered(char[] data, int offset, int length) throws IOException {
		Buffer buffer;
		int clen = 0;
		while (clen < length) {
			buffer = getBufferForOffset(fPosition, false);
			int count = Math.min(length - clen, buffer.avail());
			if (count <= 0) {
				break;
			}
			System.arraycopy(buffer.fData, buffer.fPos, data, offset, count);
			buffer.fPos += count;
			offset += count;
			clen += count;
			fPosition += count;
		}
		return clen;
	}

	/**
	 * Read characters into StringBuffer.
	 * @param strBuf
	 * @param length
	 * @return number of characters read.
	 */
	public int readBuffered(StringBuffer strBuf, int length) throws IOException {
		Buffer buffer;
		int clen = 0;
		while (clen < length) {
			buffer = getBufferForOffset(fPosition, false);
			int count = Math.min(length - clen, buffer.avail());
			if (count <= 0) {
				break;
			}
			strBuf.append(buffer.fData, buffer.fPos, count);
			buffer.fPos += count;
			clen += count;
			fPosition += count;
		}
		return clen;
	}

	/**
	 * Get a REDFileBuffer for an offset.
	 * @param pos
	 * @return
	 */
	private Buffer getBufferForOffset(int offset, boolean write) throws IOException {
		Buffer buffer = null;
		int bufferOffset = (offset / Buffer.fcBufSize) * Buffer.fcBufSize;
		int i;
		for (i = 0; i < fcNrBufs && fBuffer[i] != null; ++i) {
			if (bufferOffset == fBuffer[i].fOffset) {
				buffer = fBuffer[i];
				break;
			}
		}
		if (buffer == null) {
			if (i < REDFile.fcNrBufs) {
				buffer = new Buffer();
				fBuffer[i] = buffer;
			} else {
				fSwapper = (fSwapper + 1) % REDFile.fcNrBufs;
				buffer = fBuffer[fSwapper];
				if (buffer.fDirty) {
					flush(buffer);
				}
			}
			buffer.fOffset = bufferOffset;
			buffer.fSize = 0;
		}
		buffer.fPos = offset - buffer.fOffset;
		if (write && buffer.fSize < buffer.fPos || !write && buffer.avail() <= 0) {
			if (buffer.fDirty) {
				flush(buffer);
			}
			update(buffer);
		}
		return buffer;
	}
}
