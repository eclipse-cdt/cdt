/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems and others.
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

/**
 * DisassemblyPosition
 */
public class DisassemblyPosition extends AddressRangePosition {

	public char[] fFunction;
	
	/**
	 * 
	 * @param offset
	 * @param length
	 * @param addressOffset
	 * @param addressLength
	 * @param opcodes
	 */
	public DisassemblyPosition(int offset, int length, BigInteger addressOffset, BigInteger addressLength, String opcodes) {
		super(offset, length, addressOffset, addressLength);
		fFunction = opcodes.toCharArray();
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
	
}
