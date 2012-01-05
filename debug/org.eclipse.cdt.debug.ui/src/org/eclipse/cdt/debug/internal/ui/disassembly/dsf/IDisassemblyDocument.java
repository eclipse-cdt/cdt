/*******************************************************************************
 * Copyright (c) 2010, 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * DSF Disassembly view backends (CDI and DSF) need this limited access to the
 * editor/view Document.
 */
public interface IDisassemblyDocument {

	void addInvalidAddressRange(AddressRangePosition p);

	AddressRangePosition insertLabel(AddressRangePosition pos,
			BigInteger address, String label, boolean showLabels)
			throws BadLocationException;

	AddressRangePosition insertDisassemblyLine(AddressRangePosition p,
			BigInteger address, int intValue, String functionOffset, String instruction,
			String compilationPath, int lineNumber) throws BadLocationException;

	AddressRangePosition insertDisassemblyLine(AddressRangePosition p, 
			BigInteger address, int length, String functionOffset, BigInteger opcode, 
			String instruction, String compilationPath, int lineNumber) 
			throws BadLocationException;
	
	AddressRangePosition getDisassemblyPosition(BigInteger address);
	BigInteger getAddressOfLine(int line);
	int getNumberOfLines();
}
