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
import org.eclipse.osgi.util.NLS;

/**
 * Simple emulator of memory block to control the memory transport without real device
 *
 */
final class EmulateMemory implements WriteMemory, ReadMemory {

	private BigInteger base;
	private Map<BigInteger, byte[]> storage;

	EmulateMemory(BigInteger base) {
		storage = new LinkedHashMap<>();
		this.base = base;
	}

	@Override
	public byte[] from(BigInteger offset) throws DebugException {
		return Optional.ofNullable(storage.get(offset)).orElseThrow(() -> failed(offset));
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
