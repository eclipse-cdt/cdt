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

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.MemoryByte;

/**
 * Reads an array of bytes using the given offset
 *
 * @since 0.1
 */
public interface ReadMemory {

	/**
	 * Reads an array of bytes from a memory starting from the given offset.
	 *
	 * @param offset
	 * @param units the number of addressable units to retrieve
	 * @return the obtained data
	 * @throws DebugException
	 */
	MemoryByte[] from(BigInteger offset, long units) throws DebugException;

}
