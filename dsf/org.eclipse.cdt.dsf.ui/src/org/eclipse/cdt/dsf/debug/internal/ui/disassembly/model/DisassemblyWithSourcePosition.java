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
	 * @param rawOpcode String of opcodes as it will be displayed to users. Can be null which is handled the same as empty string.
	 */
	public DisassemblyWithSourcePosition(int offset, int length, BigInteger addressOffset, BigInteger addressLength,
			String functionOffset, String rawOpcode, String file, int lineNr) {
		super(offset, length, addressOffset, addressLength, functionOffset, rawOpcode);
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
		return super.toString() + "->[" + fFile + ':' + fLine + ']'; //$NON-NLS-1$
	}

}
