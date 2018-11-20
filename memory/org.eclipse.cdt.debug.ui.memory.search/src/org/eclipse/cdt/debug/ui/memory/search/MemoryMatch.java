/*******************************************************************************
 * Copyright (c) 2008-2009 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ted R Williams (Wind River Systems, Inc.) - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.memory.search;

import java.math.BigInteger;

public class MemoryMatch {
	BigInteger fStartAddress;

	public BigInteger getStartAddress() {
		return fStartAddress;
	}

	public void setStartAddress(BigInteger startAddress) {
		fStartAddress = startAddress;
	}

	public BigInteger getLength() {
		return fLength;
	}

	public void setLength(BigInteger length) {
		fLength = length;
	}

	BigInteger fLength;

	public MemoryMatch(BigInteger startAddress, BigInteger length) {
		fStartAddress = startAddress;
		fLength = length;
	}

	public BigInteger getEndAddress() {
		return getStartAddress().add(getLength());
	}
}
