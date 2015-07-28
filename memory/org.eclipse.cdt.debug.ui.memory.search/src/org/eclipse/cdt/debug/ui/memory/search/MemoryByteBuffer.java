/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alvaro Sanchez-Leon (Ericsson AB) - [Memory] Make tests run with different values of addressable size (Bug 460241)
 *     Alvaro Sanchez-Leon (Ericsson) - Find / Replace for 16 bits addressable size systems (Bug 462073)
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.memory.search;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.debug.core.model.MemoryByte;

public class MemoryByteBuffer {
	final private ByteBuffer fBuffer;
	final private int fWordSize;

	public MemoryByteBuffer(MemoryByte[] memoryByteArr, ByteOrder bo,
			int wordSize) {

		// The number of bytes shall be divisible by the word size
		assert (memoryByteArr != null);
		assert (memoryByteArr.length % wordSize) == 0;

		// Wordsize can not be 0
		fWordSize = wordSize > 0 ? wordSize : 1;
		fBuffer = ByteBuffer.allocate(memoryByteArr.length);
		fBuffer.order(bo);
		
		// Fill with given octet values
		for (MemoryByte aByte : memoryByteArr) {
			fBuffer.put(aByte.getValue());
		}

		// Content is ready to be read from beginning
		fBuffer.flip();
	}

	public MemoryByteBuffer(ByteBuffer byteBuff, ByteOrder bo,
			int wordSize) {
		assert (byteBuff != null);

		fWordSize = wordSize;
		fBuffer = byteBuff;
		fBuffer.order(bo);
		// Content is ready to be read from beginning
		fBuffer.flip();
		
	}
	
	public long getNextWord() {
		// case x number of octets
		switch (fWordSize) {
		case 1:
			// return 1 octet
			return fBuffer.get() & 0xFF;
		case 2:
			// return 2 octets
			return fBuffer.getShort() & 0xFFFF;
		case 4:
			// return 4 octets
			return fBuffer.getInt() & 0xFFFFFFFF;
		case 8:
			// return 8 octets
			return fBuffer.getLong() & 0xFFFFFFFFFFFFFFFFl;
		default:
			assert (false);
			return 0;
		}
	}
	
	public int length() {
		return fBuffer.array().length / fWordSize;
	}
}
