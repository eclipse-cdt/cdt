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
package org.eclipse.cdt.utils.elf.parser;

import org.eclipse.cdt.utils.Addr2line;
import org.eclipse.cdt.utils.CPPFilt;
import org.eclipse.cdt.utils.Objdump;
import org.eclipse.core.runtime.IPath;

/*
 * GNUBinaryObject 
 */
public class GNUElfBinaryObject extends ElfBinaryObject {

	/**
	 * @param parser
	 * @param path
	 */
	public GNUElfBinaryObject(GNUElfParser parser, IPath path) {
		super(parser, path);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.BinaryObjectAdapter#getAddr2line()
	 */
	public Addr2line getAddr2line() {
		GNUElfParser parser = (GNUElfParser)getBinaryParser();
		return parser.getAddr2line(getPath());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.BinaryObjectAdapter#getCPPFilt()
	 */
	public CPPFilt getCPPFilt() {
		GNUElfParser parser = (GNUElfParser)getBinaryParser();
		return parser.getCPPFilt();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.BinaryObjectAdapter#getObjdump()
	 */
	public Objdump getObjdump() {
		GNUElfParser parser = (GNUElfParser)getBinaryParser();
		return parser.getObjdump(getPath());
	}

}
