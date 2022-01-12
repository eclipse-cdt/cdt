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

/**
 *
 * Aggregates memory import configuration
 *
 * @since 0.1
 *
 */
public final class ImportRequest {

	private final BigInteger base;
	private final BigInteger start;
	private final WriteMemory write;

	public ImportRequest(BigInteger base, BigInteger start, WriteMemory write) {
		this.base = base;
		this.start = start;
		this.write = write;
	}

	/**
	 *
	 * @return base memory address
	 */
	public BigInteger base() {
		return base;
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
