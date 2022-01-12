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
package org.eclipse.cdt.debug.core.memory.transport;

import java.math.BigInteger;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.MemoryByte;

/**
 * Reads an array of bytes using the given offset
 *
 * @since 0.1
 */
public interface IReadMemory {

	/**
	 * Reads an array of bytes from a memory starting from the given address. If requested to retrieve data beyond the memory
	 * boundaries, implementations should return memory bytes with the <code>READABLE</code> bit turned off for each byte outside the
	 * of the accessible range. An exception should not be thrown in this case.
	 *
	 * @param address address at which to begin retrieving bytes in terms of addressable units
	 * @param units the number of addressable units to retrieve
	 * @return the obtained data, {@link MemoryByte#isReadable()} needs to be checked
	 * @throws DebugException if unable to retrieve the specified bytes due to a failure communicating with the target
	 *
	 * @see {@link MemoryByte}
	 */
	MemoryByte[] from(BigInteger address, long units) throws DebugException;

}
