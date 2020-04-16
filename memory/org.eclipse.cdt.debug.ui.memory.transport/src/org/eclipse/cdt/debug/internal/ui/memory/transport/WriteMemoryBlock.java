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
package org.eclipse.cdt.debug.internal.ui.memory.transport;

import java.math.BigInteger;

import org.eclipse.cdt.debug.core.memory.transport.WriteMemory;
import org.eclipse.cdt.debug.ui.memory.transport.BufferedMemoryWriter;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IMemoryBlockExtension;

/**
 * Writes to a given {@link IMemoryBlockExtension} using {@link BufferedMemoryWriter}
 *
 *
 */
public final class WriteMemoryBlock implements WriteMemory {

	private final BufferedMemoryWriter writer;
	private final int capacity = 64 * 1024;

	public WriteMemoryBlock(IMemoryBlockExtension block) {
		this.writer = new BufferedMemoryWriter(block, capacity);
	}

	@Override
	public void to(BigInteger offset, byte[] data) throws DebugException {
		writer.write(offset, data);
	}

	@Override
	public void flush() throws DebugException {
		writer.flush();
	}

}
