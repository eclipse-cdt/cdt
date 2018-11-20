/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.internal.memory;

import java.math.BigInteger;

import org.eclipse.cdt.debug.core.model.IMemoryBlockAddressInfoRetrieval.IMemoryBlockAddressInfoItem;

/**
 * A base partial implementation of a a memory block address information item
 *
 */
public abstract class MemoryBlockAddressInfoItem implements IMemoryBlockAddressInfoItem {
	private final String fName;
	private String fLabel;
	private BigInteger fAddress;
	private BigInteger fLength = BigInteger.ONE;
	private int fColor = 0;

	private MemoryBlockAddressInfoItem(String id, BigInteger address) {
		fName = id;
		fLabel = id;
		fAddress = address;
	}

	/**
	 * @param address String representation of a memory address in hex format
	 */
	public MemoryBlockAddressInfoItem(String id, String address) {
		this(id, convertValue(address));
	}

	/**
	 * @param color int value where the lowest three octets represent the corresponding RGB value
	 */
	public MemoryBlockAddressInfoItem(String id, BigInteger address, BigInteger length, int color) {
		this(id, address);
		fLength = length;
		fColor = color;
	}

	@Override
	public String getId() {
		return fName;
	}

	@Override
	public BigInteger getRangeInAddressableUnits() {
		return fLength;
	}

	@Override
	public int getRegionRGBColor() {
		return fColor;
	}

	@Override
	public String getLabel() {
		return fLabel;
	}

	@Override
	public void setLabel(String label) {
		fLabel = label;
	}

	@Override
	public BigInteger getAddress() {
		return fAddress;
	}

	private static BigInteger convertValue(String inValue) {
		// Make sure we provide a valid hex representation or zero
		int radix = 16;
		BigInteger hexValue = null;
		String value = inValue.replaceAll("0x", ""); //$NON-NLS-1$ //$NON-NLS-2$
		try {
			hexValue = new BigInteger(value, radix);
		} catch (NumberFormatException e) {
			hexValue = BigInteger.ZERO;
		}

		return hexValue;
	}

	@Override
	public void setRangeInAddressableUnits(BigInteger length) {
		fLength = length;
	}

	@Override
	public void setRegionRGBColor(int color) {
		fColor = color;
	}

	@Override
	public void setAddress(BigInteger address) {
		fAddress = address;
	}

}