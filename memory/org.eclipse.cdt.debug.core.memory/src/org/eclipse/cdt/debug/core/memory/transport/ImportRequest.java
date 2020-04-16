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
package org.eclipse.cdt.debug.core.memory.transport;

import java.math.BigInteger;

import org.eclipse.debug.core.model.IMemoryBlockExtension;

/**
 *
 * Aggregates memory import configuration
 *
 * @since 0.1
 *
 */
public class ImportRequest {

	private final IMemoryBlockExtension block;
	private final BigInteger start;
	private final WriteMemory write;

	public ImportRequest(IMemoryBlockExtension block, BigInteger start, WriteMemory write) {
		this.block = block;
		this.start = start;
		this.write = write;
	}

	/**
	 *
	 * @return target memory block
	 */
	public IMemoryBlockExtension block() {
		return block;
	}

	/**
	 *
	 * @return starting offset
	 */
	public BigInteger start() {
		return start;
	}

	/**
	 *
	 * @return writer
	 */
	public WriteMemory write() {
		return write;
	}
}
