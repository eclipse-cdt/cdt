/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly;

import java.math.BigInteger;

import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.AddressRangePosition;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.model.SourceFileInfo;

/**
 * SourcePosition
 */
public class SourcePosition extends AddressRangePosition {

	public SourceFileInfo fFileInfo;
	public int fLine;

	/**
	 * 
	 * @param offset
	 * @param length
	 * @param addressOffset
	 * @param fileInfo
	 * @param line
	 */
	public SourcePosition(int offset, int length, BigInteger addressOffset, SourceFileInfo fileInfo, int line) {
		this(offset, length, addressOffset, fileInfo, line, true);
	}

	/**
	 * 
	 * @param offset
	 * @param length
	 * @param addressOffset
	 * @param fileInfo
	 * @param line
	 * @param valid
	 */
	public SourcePosition(int offset, int length, BigInteger addressOffset, SourceFileInfo fileInfo, int line, boolean valid) {
		super(offset, length, addressOffset, BigInteger.ZERO, valid);
		fFileInfo = fileInfo;
		fLine = line;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.internal.ui.disassembly.model.AddressRangePosition#toString()
	 */
	@Override
	public String toString() {
		return super.toString() + "->["+fFileInfo.fFileKey + ':' + fLine + ']';  //$NON-NLS-1$
	}

}
