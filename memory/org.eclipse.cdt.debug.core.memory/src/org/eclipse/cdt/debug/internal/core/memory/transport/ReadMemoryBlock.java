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
 *     John Dallaway - use absolute memory address (bug 562164)
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.memory.transport;

import java.math.BigInteger;

import org.eclipse.cdt.debug.core.memory.transport.IReadMemory;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.core.model.MemoryByte;

/**
 * Reads memory from the given {@link IMemoryBlockExtension}
 *
 */
public final class ReadMemoryBlock implements IReadMemory {

	private final IMemoryBlockExtension memory;

	public ReadMemoryBlock(IMemoryBlockExtension memory) {
		this.memory = memory;
	}

	@Override
	public MemoryByte[] from(BigInteger address, long units) throws DebugException {
		return memory.getBytesFromAddress(address, units);
	}

}
