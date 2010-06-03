/*******************************************************************************
 * Copyright (c) 2004, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - Initial API and implementation
 *     Mark Mitchell, CodeSourcery - Bug 136896: View variables in binary format
 *******************************************************************************/
package org.eclipse.cdt.utils;

import java.io.Serializable;
import java.math.BigInteger;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.internal.core.Messages;

public class Addr64 implements IAddress, Serializable {

	public static final Addr64 ZERO = new Addr64("0"); //$NON-NLS-1$
	public static final Addr64 MAX = new Addr64("ffffffffffffffff", 16); //$NON-NLS-1$

	public static final BigInteger MAX_OFFSET = new BigInteger("ffffffffffffffff", 16); //$NON-NLS-1$

	private static final int BYTES_NUM = 8;
	private static final int DIGITS_NUM = BYTES_NUM * 2;
	private static final int CHARS_NUM = DIGITS_NUM + 2;
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
		if (addr.bitLength() > 64 ) {
			if (truncate) {
				return addr.and(MAX.getValue()); // truncate
			}
			else {
				throw (new NumberFormatException(Messages.Addr_valueOutOfRange));
			}
		}
		return addr;
	}
	
	
	public IAddress add(BigInteger offset) {
		return new Addr64(this.address.add(offset));
	}

	public IAddress add(long offset) {
		return new Addr64(this.address.add(BigInteger.valueOf(offset)));
	}

	public BigInteger getMaxOffset() {
		return MAX_OFFSET;
	}

	public BigInteger distanceTo(IAddress other) {
		return other.getValue().subtract(getValue());
	}

	public boolean isMax() {
		return address.equals(MAX.getValue());
	}

	public boolean isZero() {
		return address.equals(ZERO.getValue());
	}

	public BigInteger getValue() {
		return address;
	}

	public int compareTo(Object other) {
		if (!(other instanceof IAddress)) {
			throw new IllegalArgumentException();
		}
		
		return getValue().compareTo(((IAddress)other).getValue());
	}

	@Override
	public boolean equals(Object x) {
		if (x == this)
			return true;
		if (!(x instanceof IAddress))
			return false;
		return getValue().equals(((IAddress)x).getValue());
	}

	@Override
	public int hashCode() {
		return address.hashCode();
	}

	@Override
	public String toString() {
		return toString(10);
	}

	public String toString(int radix) {
		return address.toString(radix);
	}

	public String toHexAddressString() {
		String addressString = address.toString(16);
		StringBuffer sb = new StringBuffer(CHARS_NUM);
		int count = DIGITS_NUM - addressString.length();
		sb.append("0x"); //$NON-NLS-1$
		for (int i = 0; i < count; ++i) {
			sb.append('0');
		}
		sb.append(addressString);
		return sb.toString();
	}

	public String toBinaryAddressString() {
		String addressString = address.toString(2);
		StringBuffer sb = new StringBuffer(BINARY_CHARS_NUM);
		int count = BINARY_DIGITS_NUM - addressString.length();
		sb.append("0b"); //$NON-NLS-1$
		for (int i = 0; i < count; ++i) {
			sb.append('0');
		}
		sb.append(addressString);
		return sb.toString();
	}

	public int getCharsNum() {
		return CHARS_NUM;
	}

	public int getSize() {
		return BYTES_NUM;
	}
}

