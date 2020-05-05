/*******************************************************************************
 * Copyright (c) 2020 ArSysOp and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Fedorov (ArSysOp) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.memory.tests;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.cdt.debug.core.memory.transport.IReadMemory;
import org.eclipse.cdt.debug.core.memory.transport.WriteMemory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.MemoryByte;
import org.eclipse.osgi.util.NLS;

/**
 * Simple emulator of memory block to control the memory transport without real device
 *
 */
final class EmulateMemory implements WriteMemory, IReadMemory {

	private final BigInteger addressable;
	private final BigInteger base;
	//FIXME: needs improvements, assumes identical pattern during write and read
	private final Map<BigInteger, byte[]> storage;

	EmulateMemory(BigInteger addressable, BigInteger base) {
		this.addressable = addressable;
		this.base = base;
		this.storage = new LinkedHashMap<>();
	}

	@Override
	public MemoryByte[] from(BigInteger offset, long units) throws DebugException {
		int length = (int) (units * addressable.longValue());
		MemoryByte[] result = new MemoryByte[length];
		int i = 0;
		while (i < length) {
			byte[] raw = storage.getOrDefault(offset.add(BigInteger.valueOf(i)), new byte[0]);
			int obtained = raw.length;
			if (obtained > 0) {
				for (int j = 0; j < obtained; j++) {
					result[i + j] = new MemoryByte(raw[j]);
				}
				i = i + obtained;
			} else {
				//unreachable with current test data
				MemoryByte unavailable = new MemoryByte();
				unavailable.setReadable(false);
				for (int j = i; j < length; j++) {
					result[i + j] = unavailable;
				}
				i = length;
			}
		}
		return result;
	}

	@Override
	public void to(BigInteger offset, byte[] data) throws DebugException {
		storage.put(base.add(offset), data);
	}

	@Override
	public void flush() throws DebugException {
		//do nothing
	}

	private DebugException failed(BigInteger offset) {
		return new DebugException(
				new Status(IStatus.ERROR, getClass().getName(), NLS.bind("Invalid memory access at {0}", offset))); //$NON-NLS-1$
	}

}
