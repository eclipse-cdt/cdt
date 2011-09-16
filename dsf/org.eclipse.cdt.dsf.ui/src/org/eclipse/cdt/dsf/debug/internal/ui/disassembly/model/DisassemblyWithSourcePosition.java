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
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.model;

import java.math.BigInteger;

import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.DisassemblyPosition;

/**
 * DisassemblyWithSourcePosition
 */
public class DisassemblyWithSourcePosition extends DisassemblyPosition {

	private String fFile;
	private int fLine;

	/**
	 * @param offset
	 * @param length
	 * @param addressOffset
	 * @param addressLength
	 * @param functionOffset
	 * @param opcode 
	 */
	public DisassemblyWithSourcePosition(int offset, int length, BigInteger addressOffset, BigInteger addressLength, String functionOffset, BigInteger opcode, String file, int lineNr) {
		super(offset, length, addressOffset, addressLength, functionOffset, opcode);
		fFile = file;
		fLine = lineNr;
	}

	@Override
	public String getFile() {
		return fFile;
	}

	@Override
	public int getLine() {
		return fLine;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.internal.ui.disassembly.model.DisassemblyPosition#toString()
	 */
	@Override
	public String toString() {
		return super.toString() + "->["+fFile + ':' + fLine + ']';  //$NON-NLS-1$
	}
	
}
