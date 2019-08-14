/*******************************************************************************
 * Copyright (c) 2005, 2019 Space Codesign Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Space Codesign Systems - Initial API and implementation
 *     QNX Software Systems - initial CygwinSymbol class
 *******************************************************************************/
/*
 * Created on Jul 6, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.cdt.utils.coff.parser;

import java.io.IOException;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.utils.Addr2line;
import org.eclipse.cdt.utils.Symbol;
import org.eclipse.core.runtime.IPath;

/**
 * @author DInglis
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
class CygwinSymbol64 extends Symbol {

	/**
	 * @param binary
	 * @param name
	 * @param type
	 * @param addr
	 * @param size
	 * @param sourceFile
	 * @param startLine
	 * @param endLine
	 */
	public CygwinSymbol64(CygwinPEBinaryObject64 binary, String name, int type, IAddress addr, long size,
			IPath sourceFile, int startLine, int endLine) {
		super(binary, name, type, addr, size, sourceFile, startLine, endLine);
	}

	/**
	 * @param binary
	 * @param name
	 * @param type
	 * @param addr
	 * @param size
	 */
	public CygwinSymbol64(CygwinPEBinaryObject64 binary, String name, int type, IAddress addr, long size) {
		super(binary, name, type, addr, size);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.Symbol#getLineNumber(long)
	 */
	@Override
	public int getLineNumber(long offset) {
		int line = -1;
		Addr2line addr2line = ((CygwinPEBinaryObject64) binary).getAddr2line(true);
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
