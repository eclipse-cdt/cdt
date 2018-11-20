/*******************************************************************************
 * Copyright (c) 2004, 2008 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.elf.parser;

import java.io.IOException;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.utils.Addr2line;
import org.eclipse.cdt.utils.Symbol;
import org.eclipse.core.runtime.IPath;

public class GNUSymbol extends Symbol {

	public GNUSymbol(ElfBinaryObject binary, String name, int type, IAddress addr, long size, IPath sourceFile,
			int startLine, int endLine) {
		super(binary, name, type, addr, size, sourceFile, startLine, endLine);
	}

	public GNUSymbol(ElfBinaryObject binary, String name, int type, IAddress addr, long size) {
		super(binary, name, type, addr, size);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.Symbol#getLineNumber(long)
	 */
	@Override
	public int getLineNumber(long offset) {
		int line = -1;
		Addr2line addr2line = ((GNUElfBinaryObject) binary).getAddr2line(true);
		if (addr2line != null) {
			try {
				return addr2line.getLineNumber(getAddress().add(offset));
			} catch (IOException e) {
				// ignore
			}
		}
		return line;
	}
}
