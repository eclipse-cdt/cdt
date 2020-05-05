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
import java.util.Optional;

import org.eclipse.cdt.debug.core.memory.transport.ReadMemory;
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
final class EmulateMemory implements WriteMemory, ReadMemory {

	private final BigInteger addressible;
	private final BigInteger base;
	private final Map<BigInteger, byte[]> storage;

	EmulateMemory(BigInteger addressible, BigInteger base) {
		this.addressible = addressible;
		this.base = base;
		this.storage = new LinkedHashMap<>();
	}

	@Override
	public MemoryByte[] from(BigInteger offset, long units) throws DebugException {
		int length = (int) (units * addressible.longValue());
		MemoryByte[] result = new MemoryByte[length];
		int i = 0;
		while (i < length) {
			BigInteger increment = BigInteger.valueOf(i);
			byte[] raw = Optional.ofNullable(storage.get(offset.add(increment))).orElse(new byte[0]);
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
