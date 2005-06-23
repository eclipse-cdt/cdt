/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
 
package org.eclipse.cdt.utils.coff;

public class ReadMemoryAccess {

	byte[] bytes;
	int memOffset;
	byte[] val = new byte[8];
	boolean isle;

	public ReadMemoryAccess(byte[] octets) {
		this(octets, true);
	}

	public ReadMemoryAccess(byte[] octets, boolean le) {
		bytes = octets;
		memOffset = 0;
		isle = le;
	}

	public int getSize() {
		return bytes.length - memOffset;
	}

	public void getBytes(byte[] octets) {
		getBytes(octets, memOffset);
		memOffset += octets.length;
	}

	public void getBytes(byte[] octets, int offset) {
		getBytes(octets, offset, octets.length);
	}

	public void getBytes(byte[] octets, int offset, int length) {
		System.arraycopy(bytes, offset, octets, 0, length);
	}

	public byte getByte() {
		return getByte(memOffset++);
	}

	public short getUnsignedByte() {
		return getUnsignedByte(memOffset++);
	}

	public byte getByte(int offset) {
		return bytes[offset];
	}
		
	public short getUnsignedByte(int offset) {
		return bytes[offset];
	}

	public short getShort() {
		if (isle) {
			return getShortLE();
		}
		return getShortBE();
	}

	public int getUnsignedShort() {
		if (isle) {
			return getUnsignedShortLE();
		}
		return getUnsignedShortBE();
	}

	public short getShortLE() {
		short s = getShortLE(memOffset);
		memOffset +=2;
		return s;
	}

	public int getUnsignedShortLE() {
		int i = getUnsignedShortLE(memOffset);
		memOffset +=2;
		return i;
	}

	public short getShortLE(int offset) {
		val[0] = getByte(offset);
		val[1] = getByte(offset + 1);
		return getShortLE(val);
	}

	public static short getShortLE(byte[] b) {
		return (short)(((b[1]&0xff) << 8) | (b[0]&0xff));
	}

	public int getUnsignedShortLE(int offset) {
		val[0] = getByte(offset);
		val[1] = getByte(offset + 1);
		return getUnsignedShortLE(val);
	}

	public static int getUnsignedShortLE(byte[] b) {
		return (((b[1] & 0xff) << 8) | (b[0] & 0xff));
	}

	public short getShortBE() {
		short s = getShortBE(memOffset);
		memOffset +=2;
		return s;
	}

	public int getUnsignedShortBE() {
		int i = getUnsignedShortBE(memOffset);
		memOffset +=2;
		return i;
	}

	public short getShortBE(int offset) {
		val[0] = getByte(offset);
		val[1] = getByte(offset + 1);
		return getShortBE(val);
	}

	public static short getShortBE(byte[] b) {
		return (short)(((b[0] & 0xff) << 8) | (b[1] & 0xff));
	}

	public int getUnsignedShortBE(int offset) {
		val[0] = getByte(offset);
		val[1] = getByte(offset + 1);
		return getUnsignedShortBE(val);
	}

	public static int getUnsignedShortBE(byte[] b) {
		return (((b[0] & 0xff) << 8) + (b[1] & 0xff));
	}

	public int getInt() {
		if (isle) {
			return getIntLE();
		}
		return getIntBE();
	}

	public int getIntLE() {
		int i = getIntLE(memOffset);
		memOffset += 4;
		return i;
	}

	public long getUnsignedIntLE() {
		long l = getUnsignedIntLE(memOffset);
		memOffset += 4;
		return l;
	}

	public long getUnsignedIntLE(int offset) {
		val[0] = getByte(offset);
		val[1] = getByte(offset + 1);
		val[2] = getByte(offset + 2);
		val[3] = getByte(offset + 3);
		return getUnsignedIntLE(val);
	}

	public static long getUnsignedIntLE(byte[] b) {
		return (((b[3] & 0xff) << 24) |
			      ((b[2] & 0xff) << 16) |
			      ((b[1] & 0xff) << 8)  |
			       (b[0] & 0xff));
	}

	public int getIntLE(int offset) {
		val[0] = getByte(offset);
		val[1] = getByte(offset + 1);
		val[2] = getByte(offset + 2);
		val[3] = getByte(offset + 3);
		return getIntLE(val);
	}

	public static int getIntLE(byte[] b) {
		return (((b[3] & 0xff) << 24) |
			     ((b[2] & 0xff) << 16) |
			     ((b[1] & 0xff) << 8)  |
			      (b[0] & 0xff));
	}

	public int getIntBE() {
		int i = getIntBE(memOffset);
		memOffset += 4;
		return i;
	}

	public long getUnsignedIntBE() {
		long l = getUnsignedIntBE(memOffset);
		memOffset += 4;
		return l;
	}

	public int getIntBE(int offset) {
		val[0] = getByte(offset);
		val[1] = getByte(offset + 1);
		val[2] = getByte(offset + 2);
		val[3] = getByte(offset + 3);
		return getIntBE(val);
	}

	public static int getIntBE(byte[] b) {
		return (((b[0] & 0xff) << 24) |
			     ((b[1] & 0xff) << 16) |
			     ((b[2] & 0xff) << 8)  |
			      (b[3] & 0xff));
	}

	public long getUnsignedIntBE(int offset) {
		val[0] = getByte(offset);
		val[1] = getByte(offset + 1);
		val[2] = getByte(offset + 2);
		val[3] = getByte(offset + 3);
		return getUnsignedIntBE(val);
	}

	public static long getUnsignedIntBE(byte[] b) {
		return (((b[0] & 0xff) << 24) |
			      ((b[1] & 0xff) << 16) |
			      ((b[2] & 0xff) << 8)  |
			       (b[3] & 0xff));
	}

	public long getLong() {
		if (isle) {
			return getLongLE();
		}
		return getLongBE();
	}

	public long getLongLE() {
		long l = getLongLE(memOffset);
		memOffset += 8;
		return l;
	}

	public long getLongLE(int offset) {
		val[0] = getByte(offset);
		val[1] = getByte(offset + 1);
		val[2] = getByte(offset + 2);
		val[3] = getByte(offset + 3);
		val[4] = getByte(offset + 4);
		val[5] = getByte(offset + 5);
		val[6] = getByte(offset + 6);
		val[7] = getByte(offset + 7);
		return getLongLE(val);
	}

	public long getLongLE(byte[] b) {
		return  ((long)(b[7] & 0xff) << 56) |
			((long)(b[6] & 0xff) << 48) |
			((long)(b[5] & 0xff) << 40) |
			((long)(b[4] & 0xff) << 32) |
			((long)(b[3] & 0xff) << 24) |
			((long)(b[2] & 0xff) << 16) |
			((long)(b[1] & 0xff) <<  8) |
			(b[0] & 0xff);
	}


	public long getLongBE() {
		long l = getLongBE(memOffset);
		memOffset += 8;
		return l;
	}

	public long getLongBE(int offset) {
		val[0] = getByte(offset);
		val[1] = getByte(offset + 1);
		val[2] = getByte(offset + 2);
		val[3] = getByte(offset + 3);
		val[4] = getByte(offset + 4);
		val[5] = getByte(offset + 5);
		val[6] = getByte(offset + 6);
		val[7] = getByte(offset + 7);
		return getLongBE(val);
	}

	public long getLongBE(byte[] b) {
		return  ((long)(b[0] & 0xff) << 56) |
			((long)(b[1] & 0xff) << 48) |
			((long)(b[2] & 0xff) << 40) |
			((long)(b[3] & 0xff) << 32) |
			((long)(b[4] & 0xff) << 24) |
			((long)(b[5] & 0xff) << 16) |
			((long)(b[6] & 0xff) << 8)  |
			(b[7] & 0xff);
	} 

}
