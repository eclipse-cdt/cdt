package org.eclipse.cdt.internal.core.model.parser;

import org.eclipse.cdt.core.IBinaryParser.ISymbol;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public class Symbol implements ISymbol {

	public String filename;
	public int startLine;
	public int endLine;
	public long addr;
	public String name;
	public int type;

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

}
