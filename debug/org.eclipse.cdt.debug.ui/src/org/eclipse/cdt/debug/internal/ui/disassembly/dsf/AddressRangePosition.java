/*******************************************************************************
 * Copyright (c) 2007, 2014 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.disassembly.dsf;

import java.math.BigInteger;

import org.eclipse.jface.text.Position;

/**
 * AddressRangePosition
 */
public class AddressRangePosition extends Position {

	public BigInteger fAddressOffset;
	public BigInteger fAddressLength;
	public boolean fValid;

	/**
	 * @param offset
	 * @param length
	 */
	public AddressRangePosition(int offset, int length, BigInteger addressOffset, BigInteger addressLength) {
		super(offset, length);
		fAddressOffset = addressOffset;
		fAddressLength = addressLength;
		fValid = true;
	}

	/**
	 * @param offset
	 * @param length
	 * @param valid
	 */
	public AddressRangePosition(int offset, int length, BigInteger addressOffset, BigInteger addressLength,
			boolean valid) {
		super(offset, length);
		fAddressOffset = addressOffset;
		fAddressLength = addressLength;
		fValid = valid;
	}

	/**
	 * @param address
	 * @return
	 */
	public boolean containsAddress(BigInteger address) {
		return address.compareTo(fAddressOffset) >= 0 && address.compareTo(fAddressOffset.add(fAddressLength)) < 0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object other) {
		// identity comparison
		return this == other;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.Position#toString()
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + '@' + Integer.toHexString(System.identityHashCode(this))
				+ (fValid ? "" : "[INVALID]") //$NON-NLS-1$ //$NON-NLS-2$
				+ '[' + offset + ':' + length + "]->[" + fAddressOffset.toString(16) //$NON-NLS-1$
				+ ':' + fAddressLength.toString(16) + ']';
	}
}
