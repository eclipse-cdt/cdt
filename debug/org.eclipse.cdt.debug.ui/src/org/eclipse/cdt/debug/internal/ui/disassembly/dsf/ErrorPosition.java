/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems and others.
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

/**
 * ErrorPosition
 */
public class ErrorPosition extends AddressRangePosition {

	public int fHashCode;

	/**
	 * @param offset
	 * @param length
	 * @param addressOffset
	 * @param addressLength
	 */
	public ErrorPosition(int offset, int length, BigInteger addressOffset, BigInteger addressLength, int hashCode) {
		super(offset, length, addressOffset, addressLength);
		fHashCode = hashCode;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return fHashCode;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.internal.ui.disassembly.model.AddressRangePosition#toString()
	 */
	@Override
	public String toString() {
		return super.toString() + "->[" + fHashCode + ']'; //$NON-NLS-1$
	}
}
