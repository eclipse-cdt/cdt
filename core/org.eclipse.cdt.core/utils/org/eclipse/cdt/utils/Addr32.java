/*******************************************************************************
 * Copyright (c) 2004 Intel Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: Intel Corporation - Initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.utils;

import java.math.BigInteger;

import org.eclipse.cdt.core.IAddress;

public class Addr32 implements IAddress {

	private static final long MAX_ADDR = 0xffffffffL;
	
	public static final Addr32 ZERO = new Addr32(0);
	public static final Addr32 MAX = new Addr32(MAX_ADDR);

	public static final BigInteger MAX_OFFSET = BigInteger.valueOf(MAX_ADDR);

	private static final int BYTES_NUM = 4;
	private static final int DIGITS_NUM = BYTES_NUM * 2;
	private static final int CHARS_NUM = DIGITS_NUM + 2;

	private final long address;

	/*
	 * addrBytes should be 4 bytes length
	 */
	public Addr32(byte[] addrBytes) {
		if (addrBytes.length != 4)
			throw (new NumberFormatException("Invalid address array")); //$NON-NLS-1$
		/* We should mask out sign bits to have correct value */
		this.address = ( ( ((long)addrBytes[0]) << 24) & 0xFF000000L) + ( ( ((long)addrBytes[1]) << 16) & 0x00FF0000L)
				+ ( ( ((long)addrBytes[2]) << 8) & 0x0000FF00L) + (addrBytes[3] & 0x000000FFL);
	}

	public Addr32(long rawaddress) {
		if (rawaddress > MAX_ADDR || rawaddress < 0) {
			rawaddress &= MAX_ADDR; // truncate
		}
		this.address = rawaddress;
	}

	public Addr32(String addr) {
		this(Long.decode(addr).longValue());
	}

	public Addr32(String addr, int radix) {
		this(Long.parseLong(addr, radix));
	}

	public IAddress add(BigInteger offset) {
		return new Addr32(this.address + offset.longValue());
	}

	public IAddress add(long offset) {
		return new Addr32(this.address + offset);
	}

	public BigInteger getMaxOffset() {
		return MAX_OFFSET;
	}
	
	public BigInteger getValue() {
		return BigInteger.valueOf(address);
	}

	public BigInteger distanceTo(IAddress other) {
		if (!(other instanceof Addr32)) {
			throw new IllegalArgumentException();
		}
		return BigInteger.valueOf(((Addr32)other).address - address);
	}

	public int compareTo(Object other) {
		if (!(other instanceof Addr32)) {
			throw new IllegalArgumentException();
		}
		if (address > ((Addr32)other).address) {
			return 1;
		}
		if (address < ((Addr32)other).address) {
			return -1;
		}
		return 0;
	}

	public boolean isMax() {
		return address == MAX.address;
	}

	public boolean isZero() {
		return address == ZERO.address;
	}

	public String toString() {
		return toString(10);
	}

	public String toString(int radix) {
		return Long.toString(address, radix);
	}

	public boolean equals(Object x) {
		if (x == this)
			return true;
		if (!(x instanceof Addr32))
			return false;
		return this.address == ((Addr32)x).address;
	}

	public int hashCode() {
		return (int)(address ^ (address >> 32));
	}
	
	public String toHexAddressString() {
		String addressString = Long.toString(address, 16);
		StringBuffer sb = new StringBuffer(CHARS_NUM);
		int count = DIGITS_NUM - addressString.length();
		sb.append("0x"); //$NON-NLS-1$
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