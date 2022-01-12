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
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly;

import java.math.BigInteger;

import org.eclipse.cdt.debug.internal.ui.disassembly.dsf.AddressRangePosition;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.model.SourceFileInfo;

/**
 * SourcePosition
 */
public class SourcePosition extends AddressRangePosition {

	public final SourceFileInfo fFileInfo;
	public int fLine;
	public int fLast = -1;

	public SourcePosition(int offset, int length, BigInteger addressOffset, SourceFileInfo fileInfo, int line,
			int last) {
		this(offset, length, addressOffset, fileInfo, line, last, true);
	}

	public SourcePosition(int offset, int length, BigInteger addressOffset, SourceFileInfo fileInfo, int line, int last,
			boolean valid) {
		super(offset, length, addressOffset, BigInteger.ZERO, valid);
		fFileInfo = fileInfo;
		fLine = line;
		fLast = last;
	}

	@Override
	public String toString() {
		return super.toString() + "->[" + fFileInfo.fFileKey + ':' + fLine + ']'; //$NON-NLS-1$
	}

}
