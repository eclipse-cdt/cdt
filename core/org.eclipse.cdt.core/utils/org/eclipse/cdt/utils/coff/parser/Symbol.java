/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.utils.coff.parser;

import java.io.IOException;

import org.eclipse.cdt.core.IBinaryParser.ISymbol;
import org.eclipse.cdt.utils.Addr2line;

public class Symbol implements ISymbol {

	BinaryObject binary;

	public String filename;
	public int startLine;
	public int endLine;
	public long addr;
	public String name;
	public int type;

	public Symbol(BinaryObject bin) {
		binary = bin;		
	}
	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.ISymbol#getFilename()
	 */
	public String getFilename() {
		return filename;
	}


	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.ISymbol#getName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.ISymbol#getType()
	 */
	public int getType() {
		return type;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser.ISymbol#getAdress()
	 */
	public long getAddress() {
		return addr;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser.ISymbol#getEndLine()
	 */
	public int getEndLine() {
		return endLine;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser.ISymbol#getStartLine()
	 */
	public int getStartLine() {
		return startLine;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser.ISymbol#getLineNumber(long)
	 */
	public int getLineNumber(long offset) {
		int line = -1;
		try {
			Addr2line addr2line = binary.getAddr2Line();
			if (addr2line != null) {
				line = addr2line.getLineNumber(addr + offset);
				addr2line.dispose();
			}
		} catch (IOException e) {		
		}
		return line;
	}

}
