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
 * Aggregates memory export configuration
 *
 * @since 0.1
 *
 */
public final class ExportRequest {

	private final BigInteger start;
	private final BigInteger end;
	private final BigInteger addressable;
	private final IReadMemory read;

	public ExportRequest(BigInteger start, BigInteger end, BigInteger addressable, IReadMemory read) {
		this.start = start;
		this.end = end;
		this.addressable = addressable;
		this.read = read;
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
	 * @return ending offset
	 */
	public BigInteger end() {
		return end;
	}

	/**
	 *
	 * @return addressable size
	 */
	public BigInteger addressable() {
		return addressable;
	}

	/**
	 *
	 * @return reader
	 */
	public IReadMemory read() {
		return read;
	}
}
