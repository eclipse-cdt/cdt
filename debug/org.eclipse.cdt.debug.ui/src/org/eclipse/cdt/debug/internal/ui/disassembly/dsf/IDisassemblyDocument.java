/*******************************************************************************
 * Copyright (c) 2010, 2021 Wind River Systems, Inc. and others.
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
 *     Freescale Semiconductor - refactoring
 *     Patrick Chuong (Texas Instruments) - Bug 364405
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.disassembly.dsf;

import java.math.BigInteger;

import org.eclipse.jface.text.BadLocationException;

/**
 * Disassembly view backends need this limited access to the
 * editor/view Document. The known backends are DSF and TCF. Formerly
 * the CDI backend used it before it was removed from the CDT soure tree.
 * Formerly the DAP backend used it before it was removed from the
 * CDT soure tree (See https://github.com/eclipse-cdt/cdt/issues/139).
 */
public interface IDisassemblyDocument {

	void addInvalidAddressRange(AddressRangePosition p);

	AddressRangePosition insertLabel(AddressRangePosition pos, BigInteger address, String label, boolean showLabels)
			throws BadLocationException;

	AddressRangePosition insertDisassemblyLine(AddressRangePosition p, BigInteger address, int intValue,
			String functionOffset, String instruction, String compilationPath, int lineNumber)
			throws BadLocationException;

	/**
	 * This method. that takes opcode as a Byte[] exists solely for TCF integration.
	 */
	AddressRangePosition insertDisassemblyLine(AddressRangePosition p, BigInteger address, int length,
			String functionOffset, Byte[] opcode, String instruction, String compilationPath, int lineNumber)
			throws BadLocationException;

	/**
	 * @param rawOpcode String of opcodes as it will be displayed to users. Can be null which is handled the same as empty string.
	 */
	AddressRangePosition insertDisassemblyLine(AddressRangePosition p, BigInteger address, int length,
			String functionOffset, String rawOpcode, String instruction, String compilationPath, int lineNumber)
			throws BadLocationException;

	AddressRangePosition getDisassemblyPosition(BigInteger address);

	BigInteger getAddressOfLine(int line);

	int getNumberOfLines();
}
