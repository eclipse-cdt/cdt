package org.eclipse.cdt.internal.core.model.parser;

import org.eclipse.cdt.core.model.IBinaryParser.ISymbol;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public class Symbol implements ISymbol {

	public String filename;
	public int lineno;
	public String name;
	public int type;

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.ISymbol#getFilename()
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.ISymbol#getLineNumber()
	 */
	public int getLineNumber() {
		return lineno;
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

}
