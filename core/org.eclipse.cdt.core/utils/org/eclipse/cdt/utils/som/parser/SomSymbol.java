/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.utils.som.parser;

import java.io.IOException;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.utils.Addr2line;
import org.eclipse.cdt.utils.BinaryObjectAdapter;
import org.eclipse.cdt.utils.Symbol;
import org.eclipse.core.runtime.IPath;

/**
 * SOM symbol specialization
 * 
 * @author vhirsl
 */
public class SomSymbol extends Symbol {

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
	public SomSymbol(BinaryObjectAdapter binary, String name, int type, IAddress addr, long size, IPath sourceFile, int startLine, int endLine) {
		super(binary, name, type, addr, size, sourceFile, startLine, endLine);
	}
	
	/**
	 * @param binary
	 * @param name
	 * @param type
	 * @param addr
	 * @param size
	 */
	public SomSymbol(BinaryObjectAdapter binary, String name, int type, IAddress addr, long size) {
		super(binary, name, type, addr, size);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser.ISymbol#getLineNumber(long)
	 */
	public int getLineNumber(long offset) {
		int line = -1;
		Addr2line addr2line = ((SOMBinaryObject)binary).getAddr2line(true);
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
