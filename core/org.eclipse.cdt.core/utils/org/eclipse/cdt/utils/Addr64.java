/*******************************************************************************
 * Copyright (c) 2004, 2016 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Intel Corporation - Initial API and implementation
 *     Mark Mitchell, CodeSourcery - Bug 136896: View variables in binary format
 *     Mathias Kunter - Bug 370462: View variables in octal format
 *******************************************************************************/
package org.eclipse.cdt.utils;

import java.io.Serializable;
import java.math.BigInteger;
import java.nio.ByteBuffer;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.internal.core.Messages;

public class Addr64 implements IAddress, Serializable {

	public static final Addr64 ZERO = new Addr64("0"); //$NON-NLS-1$
	public static final Addr64 MAX = new Addr64("ffffffffffffffff", 16); //$NON-NLS-1$

	public static final BigInteger MAX_OFFSET = new BigInteger("ffffffffffffffff", 16); //$NON-NLS-1$

	private static final int BYTES_NUM = 8;
	private static final int DIGITS_NUM = BYTES_NUM * 2;
	private static final int CHARS_NUM = DIGITS_NUM + 2;
	private static final int OCTAL_DIGITS_NUM = (BYTES_NUM * 8 + 2) / 3;
	private static final int OCTAL_CHARS_NUM = OCTAL_DIGITS_NUM + 1;
	private static final int BINARY_DIGITS_NUM = BYTES_NUM * 8;
	private static final int BINARY_CHARS_NUM = BINARY_DIGITS_NUM + 2;

	private final BigInteger address;

	public Addr64(byte[] addrBytes) {
		address = checkAddress(new BigInteger(1, addrBytes), true);
	}

	public Addr64(BigInteger rawaddress) {
		this(rawaddress, true);
	}

	public Addr64(BigInteger rawaddress, boolean truncate) {
		address = checkAddress(rawaddress, truncate);
	}

	public Addr64(String addr) {
		this(addr, true);
	}

	/**
	 * Create an address represented by long bits.
	 * Signed bit will be used as unsigned extension, if you don't want it mask it before passing here.
	 *
	 * @since 5.9
	 */
	public Addr64(long addr) {
		if (addr < 0)
			address = new BigInteger(1, ByteBuffer.allocate(8).putLong(addr).array());
		else
			address = BigInteger.valueOf(addr);
	}

	public Addr64(String addr, boolean truncate) {
		addr = addr.toLowerCase();
		if (addr.startsWith("0x")) { //$NON-NLS-1$
			address = checkAddress(new BigInteger(addr.substring(2), 16), truncate);
		} else {
			address = checkAddress(new BigInteger(addr, 10), truncate);
		}
	}

	public Addr64(String addr, int radix) {
		this(addr, radix, true);
	}

	public Addr64(String addr, int radix, boolean truncate) {
		this(new BigInteger(addr, radix), truncate);
	}

	private BigInteger checkAddress(BigInteger addr, boolean truncate) {
		if (addr.signum() == -1) {
			throw new IllegalArgumentException("Invalid Address, must be positive value"); //$NON-NLS-1$
		}
		if (addr.bitLength() > 64) {
			if (truncate) {
				return addr.and(MAX.getValue()); // truncate
			} else {
				throw (new NumberFormatException(Messages.Addr_valueOutOfRange));
			}
		}
		return addr;
	}

	@Override
	public IAddress add(BigInteger offset) {
		return new Addr64(this.address.add(offset));
	}

	@Override
	public IAddress add(long offset) {
		return new Addr64(this.address.add(BigInteger.valueOf(offset)));
	}

	@Override
	public BigInteger getMaxOffset() {
		return MAX_OFFSET;
	}

	@Override
	public BigInteger distanceTo(IAddress other) {
		return other.getValue().subtract(getValue());
	}

	@Override
	public boolean isMax() {
		return address.equals(MAX.getValue());
	}

	@Override
	public boolean isZero() {
		return address.equals(ZERO.getValue());
	}

	@Override
	public BigInteger getValue() {
		return address;
	}

	@Override
	public int compareTo(Object other) {
		if (!(other instanceof IAddress)) {
			throw new IllegalArgumentException();
		}

		return getValue().compareTo(((IAddress) other).getValue());
	}

	@Override
	public boolean equals(Object x) {
		if (x == this)
			return true;
		if (!(x instanceof IAddress))
			return false;
		return getValue().equals(((IAddress) x).getValue());
	}

	@Override
	public int hashCode() {
		return address.hashCode();
	}

	@Override
	public String toString() {
		return toString(10);
	}

	@Override
	public String toString(int radix) {
		return address.toString(radix);
	}

	@Override
	public String toHexAddressString() {
		String addressString = address.toString(16);
		StringBuilder sb = new StringBuilder(CHARS_NUM);
		int count = DIGITS_NUM - addressString.length();
		sb.append("0x"); //$NON-NLS-1$
		for (int i = 0; i < count; ++i) {
			sb.append('0');
		}
		sb.append(addressString);
		return sb.toString();
	}

	/**
	 * @since 5.4
	 */
	public String toOctalAddressString() {
		String addressString = address.toString(8);
		StringBuilder sb = new StringBuilder(OCTAL_CHARS_NUM);
		int count = OCTAL_DIGITS_NUM - addressString.length();
		sb.append("0"); //$NON-NLS-1$
		for (int i = 0; i < count; ++i) {
			sb.append('0');
		}
		sb.append(addressString);
		return sb.toString();
	}

	@Override
	public String toBinaryAddressString() {
		String addressString = address.toString(2);
		StringBuilder sb = new StringBuilder(BINARY_CHARS_NUM);
		int count = BINARY_DIGITS_NUM - addressString.length();
		sb.append("0b"); //$NON-NLS-1$
		for (int i = 0; i < count; ++i) {
			sb.append('0');
		}
		sb.append(addressString);
		return sb.toString();
	}

	@Override
	public int getCharsNum() {
		return CHARS_NUM;
	}

	@Override
	public int getSize() {
		return BYTES_NUM;
	}
}
