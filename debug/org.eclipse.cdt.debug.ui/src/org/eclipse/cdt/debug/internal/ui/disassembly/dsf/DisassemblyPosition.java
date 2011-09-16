/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	public BigInteger fOpcodes;

	/**
	 * @param offset
	 * @param length
	 * @param addressOffset
	 * @param addressLength
	 * @param functionOffset
	 * @param opcodes
	 */
	public DisassemblyPosition(int offset, int length, BigInteger addressOffset, BigInteger addressLength, String functionOffset, BigInteger opcodes) {
		super(offset, length, addressOffset, addressLength);
		fOpcodes = opcodes;
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
