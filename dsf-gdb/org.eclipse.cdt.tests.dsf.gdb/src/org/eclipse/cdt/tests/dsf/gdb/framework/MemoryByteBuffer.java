/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alvaro Sanchez-Leon (Ericsson AB) - [Memory] Make tests run with different values of addressable size (Bug 460241)
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.framework;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.debug.core.model.MemoryByte;

public class MemoryByteBuffer {
	final private ByteBuffer fBuffer;
	final private int fWordSize;

	public MemoryByteBuffer(MemoryByte[] memoryByteArr, ByteOrder bo, int wordSize) {
		assert (memoryByteArr != null);

		fWordSize = wordSize;
		fBuffer = ByteBuffer.allocate(memoryByteArr.length);
		fBuffer.order(bo);
		// Fill with given octet values
		for (MemoryByte aByte : memoryByteArr) {
			fBuffer.put(aByte.getValue());
		}

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
}
