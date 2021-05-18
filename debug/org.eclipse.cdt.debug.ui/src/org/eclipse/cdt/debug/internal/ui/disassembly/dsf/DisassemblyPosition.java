/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems and others.
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
 * DisassemblyPosition
 */
public class DisassemblyPosition extends AddressRangePosition {

	public char[] fFunction;
	/**
	 * String of opcodes as it will be displayed to users. Can be null which is handled the same as empty string.
	 */
	public String fRawOpcode;

	/**
	 * @param offset
	 * @param length
	 * @param addressOffset
	 * @param addressLength
	 * @param functionOffset
	 * @param rawOpcode String of opcodes as it will be displayed to users. Can be null which is handled the same as empty string.
	 */
	public DisassemblyPosition(int offset, int length, BigInteger addressOffset, BigInteger addressLength,
			String functionOffset, String rawOpcode) {
		super(offset, length, addressOffset, addressLength);
		fRawOpcode = rawOpcode;
		fFunction = functionOffset.toCharArray();
	}

	/**
	 * @return source file
	 */
	public String getFile() {
		return null;
	}

	/**
	 * @return source line number
	 */
	public int getLine() {
		return -1;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.internal.ui.disassembly.model.AddressRangePosition#toString()
	 */
	@Override
	public String toString() {
		return super.toString() + "->[" + new String(fFunction) + ']'; //$NON-NLS-1$
	}
}
