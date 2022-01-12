/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    John Camelon (IBM Rational Software) - Initial API and implementation
 *    Cheong, Jeong-Sik - fix for 162381
 *    Valeri Atamaniouk - fix for 170398
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.parser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

import org.eclipse.cdt.core.parser.util.CharArrayUtils;

/**
 * Reads the content of a file into a char[] buffer.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @deprecated replaced by {@link FileContent}
 */
@Deprecated
public class CodeReader {
	public static final String SYSTEM_DEFAULT_ENCODING = System.getProperty("file.encoding"); //$NON-NLS-1$

	private static final int MB = 1024 * 1024;
	private static final String NF = "<text>"; //$NON-NLS-1$
	private static final char[] NOFILE = NF.toCharArray();
	private static final int MAX_FILE_SIZE;
	static {
		MAX_FILE_SIZE = (int) Math.min(Integer.MAX_VALUE, (Runtime.getRuntime().maxMemory()) / 4);
	}
	public final char[] buffer;
	public final char[] filename;

	// If you already have the buffer, e.g. working copy
	public CodeReader(String filename, char[] buffer) {
		this.filename = filename.toCharArray();
		this.buffer = buffer;
	}

	// If you are just scanning a string
	public CodeReader(char[] buffer) {
		this(NF, buffer);
	}

	// If you are loading up a file normally
	public CodeReader(String filename) throws IOException {
		this.filename = filename.toCharArray();

		FileInputStream stream = new FileInputStream(filename);
		try {
			buffer = load(SYSTEM_DEFAULT_ENCODING, stream);
		} finally {
			stream.close();
		}
	}

	public CodeReader(String filename, String charSet) throws IOException {
		this.filename = filename.toCharArray();

		FileInputStream stream = new FileInputStream(filename);
		try {
			buffer = load(charSet, stream);
		} finally {
			stream.close();
		}
	}

	public CodeReader(String filename, InputStream stream) throws IOException {
		this(filename, SYSTEM_DEFAULT_ENCODING, stream);
	}

	public CodeReader(String fileName, String charSet, InputStream stream) throws IOException {
		filename = fileName.toCharArray();

		FileInputStream fstream = (stream instanceof FileInputStream) ? (FileInputStream) stream
				: new FileInputStream(fileName);
		try {
			buffer = load(charSet, fstream);
		} finally {
			// If we create the FileInputStream we need close to it when done,
			// if not we figure the above layer will do it.
			if (!(stream instanceof FileInputStream)) {
				fstream.close();
			}
		}
	}

	/**
	 * Load the stream content as a character array. The method loads the stream content using given
	 * character set name. In case if the character set is not supported, the default one is used.
	 */
	private char[] load(String charSet, FileInputStream stream) throws IOException {
		String encoding = Charset.isSupported(charSet) ? charSet : SYSTEM_DEFAULT_ENCODING;
		FileChannel channel = stream.getChannel();
		final long lsize = channel.size();
		final int isize = (int) lsize;
		if (lsize > MAX_FILE_SIZE) {
			throw new IOException("File '" + getPath() + "' is larger than " + MAX_FILE_SIZE / 1024 / 1024 + "mb"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		}

		CharBuffer charBuffer;
		if (isize < MB) {
			charBuffer = decodeSmallFile(channel, isize, encoding);
		} else {
			charBuffer = decodeLargeFile(channel, isize, encoding);
		}
		if (charBuffer.hasArray() && charBuffer.arrayOffset() == 0) {
			char[] buff = charBuffer.array();
			if (buff.length == charBuffer.remaining())
				return buff;
		}
		char[] buff = new char[charBuffer.remaining()];
		charBuffer.get(buff);
		return buff;
	}

	private CharBuffer decodeSmallFile(FileChannel channel, final int isize, String encoding) throws IOException {
		ByteBuffer byteBuffer = ByteBuffer.allocate(isize);
		channel.read(byteBuffer);
		byteBuffer.flip();

		return Charset.forName(encoding).decode(byteBuffer);
	}

	private CharBuffer decodeLargeFile(FileChannel channel, final int isize, String encoding) throws IOException {
		int chunk = Math.min(isize, MB);
		final ByteBuffer in = ByteBuffer.allocate(chunk);
		final Charset charset = Charset.forName(encoding);
		final CharsetDecoder decoder = charset.newDecoder().onMalformedInput(CodingErrorAction.REPLACE)
				.onUnmappableCharacter(CodingErrorAction.REPLACE);

		int n = (int) (isize * (double) decoder.averageCharsPerByte()); // avoid rounding errors.
		CharBuffer out = CharBuffer.allocate(n);

		int offset = 0;
		while (offset < isize) {
			channel.read(in);
			in.flip();
			offset += in.limit();

			CoderResult cr = decoder.decode(in, out, offset >= isize);
			final int remainingBytes = in.remaining();
			if (cr.isOverflow()) {
				int totalRemainingBytes = isize - offset + remainingBytes;
				if (totalRemainingBytes > 0) {
					n += (int) (totalRemainingBytes * (double) decoder.maxCharsPerByte()); // avoid rounding errors.
					CharBuffer o = CharBuffer.allocate(n);
					out.flip();
					o.put(out);
					out = o;
				}
			} else if (!cr.isUnderflow()) {
				cr.throwException();
			}

			if (remainingBytes == 0) {
				in.clear();
			} else {
				byte[] rest = new byte[remainingBytes];
				in.get(rest);
				in.clear();
				in.put(rest);
				offset -= remainingBytes;
			}
		}
		out.flip();
		return out;
	}

	public boolean isFile() {
		return !CharArrayUtils.equals(filename, NOFILE);
	}

	@Override
	public String toString() {
		return getPath();
	}

	public String getPath() {
		return new String(filename);
	}
}
