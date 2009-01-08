/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.model;

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
	public AddressRangePosition(int offset, int length, BigInteger addressOffset, BigInteger addressLength, boolean valid) {
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
		return address.compareTo(fAddressOffset) >= 0
			&& address.compareTo(fAddressOffset.add(fAddressLength)) < 0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object other) {
		// identity comparison
		return this == other;
	}
}
