/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core;

import java.math.BigInteger;

/**
 * Help class to specify formats.
 */
public final class MIFormat {
	public final static int HEXADECIMAL = 0;
	public final static int OCTAL = 1;
	public final static int BINARY = 2;
	public final static int DECIMAL = 3;
	public final static int RAW = 4;
	public final static int NATURAL = 5;

	public final static int FLOAT = 10;
	public final static int ADDRESS = 11;
	public final static int INSTRUCTION = 12;
	public final static int CHAR = 13;
	public final static int STRING = 14;
	public final static int UNSIGNED = 15;

	// no instanciation.
	private MIFormat() {
	}

	/**
	 * We are assuming that GDB will print the address in hex format
	 * like:
	 *  0xbfffe5f0 "hello"
	 *  (int *) 0xbfffe2b8
	 * 
	 * @param buffer
	 * @return
	 */
	public static BigInteger decodeAdress(String buffer) {
		int radix = 10;
		int cursor = 0;
		int offset = 0;
		int len = buffer.length();

		if ((offset = buffer.indexOf("0x")) != -1 || //$NON-NLS-1$
			(offset = buffer.indexOf("0X")) != -1) { //$NON-NLS-1$
			radix = 16;
			cursor = offset + 2;
		}

		while (cursor < len && Character.digit(buffer.charAt(cursor), radix) != -1) {
			cursor++;
		}

		String s = buffer.substring(offset, cursor);
		return getBigInteger(s);
	}

	public static BigInteger getBigInteger(String address) {
		int index = 0;
		int radix = 10;
		boolean negative = false;

		// Handle zero length
		address = address.trim();
		if (address.length() == 0) {
			return BigInteger.ZERO;
		}

		// Handle minus sign, if present
		if (address.startsWith("-")) { //$NON-NLS-1$
			negative = true;
			index++;
		}
		if (address.startsWith("0x", index) || address.startsWith("0X", index)) { //$NON-NLS-1$ //$NON-NLS-2$
			index += 2;
			radix = 16;
		} else if (address.startsWith("#", index)) { //$NON-NLS-1$
			index ++;
			radix = 16;
		} else if (address.startsWith("0", index) && address.length() > 1 + index) { //$NON-NLS-1$
			index ++;
			radix = 8;
		}

		if (index > 0) {
			address = address.substring(index);
		}
		if (negative) {
			address = "-" + address; //$NON-NLS-1$
		}
		try {
			return new BigInteger(address, radix);
		} catch (NumberFormatException e) {
			// ...
			// What can we do ???
		}
		return BigInteger.ZERO;
	}
}
