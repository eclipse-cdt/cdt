/*******************************************************************************
 * Copyright (c) 2000, 2012 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.core;

import java.io.IOException;

public class ByteUtils {
	
	/**
	 * Concatenates two bytes to make a short
	 * 
	 * @param bytes
	 *            collection of bytes; must provide a minimum of two bytes starting at [offset]
	 * @param offset
	 *            zero-based index into [bytes], identifying the first input byte
	 * @param isle
	 *            if true, bytes are concatenated in little-endian order (the first input byte in the
	 *            collection represents the least-significant byte of the resulting short); otherwise
	 *            big-endian
	 * @return the resulting short
	 * @throws IOException
	 *             if an insufficient number of bytes are supplied
	 */
	public static short makeShort(byte[] bytes, int offset, boolean isle) throws IOException {
		if (bytes.length < offset + 2)
			throw new IOException();
		return isle ?
			(short) ((bytes[offset + 1] << 8) | (bytes[offset + 0] & 0xff)) :
			(short) ((bytes[offset + 0] << 8) | (bytes[offset + 1] & 0xff));
	}

	/**
	 * Concatenates four bytes to make an int
	 * 
	 * @param bytes
	 *            collection of bytes; must provide a minimum of four bytes starting at [offset]
	 * @param offset
	 *            zero-based index into [bytes], identifying the first input byte
	 * @param isle
	 *            if true, bytes are concatenated in little-endian order (the first input byte in the
	 *            collection represents the least-significant byte of the resulting short); otherwise
	 *            big-endian
	 * @return the resulting int
	 * @throws IOException
	 *             if an insufficient number of bytes are supplied
	 */
	public static long makeInt(byte[] bytes, int offset, boolean isle) throws IOException {
		if (bytes.length < offset + 4)
			throw new IOException();
		return isle ?
			(bytes[offset + 3] << 24) + ((bytes[offset + 2] & 0xff) << 16) + ((bytes[offset + 1] & 0xff) << 8) + (bytes[offset + 0] & 0xff) :
			(bytes[offset + 0] << 24) + ((bytes[offset + 1] & 0xff) << 16) + ((bytes[offset + 2] & 0xff) << 8) + (bytes[offset + 3] & 0xff);
	}

	/**
	 * Concatenates eight bytes to make a long
	 * 
	 * @param bytes
	 *            collection of bytes; must provide a minimum of eight bytes starting at [offset]
	 * @param offset
	 *            zero-based index into [bytes], identifying the first input byte
	 * @param isle
	 *            if true, bytes are concatenated in little-endian order (the first input byte in the
	 *            collection represents the least-significant byte of the resulting short); otherwise
	 *            big-endian
	 * @return the resulting int
	 * @throws IOException
	 *             if an insufficient number of bytes are supplied
	 */
	public static long makeLong(byte[] bytes, int offset, boolean isle) throws IOException {
		long result = 0;
		int shift = 0;
		if (isle)
			for (int i = 7; i >= 0; i--) {
				shift = i * 8;
				result += ( ((long)bytes[offset + i]) << shift) & (0xffL << shift);
			}
		else
			for (int i = 0; i <= 7; i++) {
				shift = (7 - i) * 8;
				result += ( ((long)bytes[offset + i]) << shift) & (0xffL << shift);
			}
		return result;
	}
}
