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

/**
 * Writes an array of bytes using the given offset
 *
 * @since 0.1
 */
public interface WriteMemory {

	/**
	 * Writes the given data to a memory starting from the given offset. Actual write may be delayed until the nearest {@link WriteMemory#flush()} call
	 *
	 * @param offset
	 * @param data
	 * @throws DebugException
	 */
	void to(BigInteger offset, byte[] data) throws DebugException;

	/**
	 * Forces data write to a memory.
	 *
	 * @throws DebugException
	 */
	void flush() throws DebugException;
}
