/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.coff.parser;

import org.eclipse.cdt.utils.Addr2line;
import org.eclipse.cdt.utils.CPPFilt;
import org.eclipse.cdt.utils.Objdump;
import org.eclipse.core.runtime.IPath;

/*
 * CygwinPEBinaryObject 
 */
public class CygwinPEBinaryObject extends PEBinaryObject {

	/**
	 * @param parser
	 * @param path
	 */
	public CygwinPEBinaryObject(CygwinPEParser parser, IPath path) {
		super(parser, path);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.BinaryObjectAdapter#getAddr2line()
	 */
	public Addr2line getAddr2line() {
		CygwinPEParser parser = (CygwinPEParser)getBinaryParser();
		return parser.getAddr2line(getPath());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.BinaryObjectAdapter#getCPPFilt()
	 */
	public CPPFilt getCPPFilt() {
		CygwinPEParser parser = (CygwinPEParser)getBinaryParser();
		return parser.getCPPFilt();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.BinaryObjectAdapter#getObjdump()
	 */
	public Objdump getObjdump() {
		CygwinPEParser parser = (CygwinPEParser)getBinaryParser();
		return parser.getObjdump(getPath());
	}

}
