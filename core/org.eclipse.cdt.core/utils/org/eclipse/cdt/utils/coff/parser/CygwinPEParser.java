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

import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.utils.Addr2line;
import org.eclipse.cdt.utils.CPPFilt;
import org.eclipse.cdt.utils.CygPath;
import org.eclipse.cdt.utils.CygwinToolsProvider;
import org.eclipse.cdt.utils.ICygwinToolsProvider;
import org.eclipse.cdt.utils.Objdump;
import org.eclipse.core.runtime.IPath;

/**
 */
public class CygwinPEParser extends PEParser implements IBinaryParser, ICygwinToolsProvider {

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser#getBinary(IPath)
	 */
	public IBinaryFile getBinary(IPath path) throws IOException {
		IBinaryFile binary = super.getBinary(path);
		if (binary instanceof BinaryFile) {
			((BinaryFile)binary).setToolsProvider(this);
		}
		return binary;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser#getFormat()
	 */
	public String getFormat() {
		return "Cygwin PE"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.ICygwinToolsProvider#getCygPath()
	 */
	public CygPath getCygPath() {
		return new CygwinToolsProvider(this).getCygPath();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.IToolsProvider#getAddr2Line(org.eclipse.core.runtime.IPath)
	 */
	public Addr2line getAddr2Line(IPath path) {
		return new CygwinToolsProvider(this).getAddr2Line(path);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.IToolsProvider#getCPPFilt()
	 */
	public CPPFilt getCPPFilt() {
		return new CygwinToolsProvider(this).getCPPFilt();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.IToolsProvider#getObjdump(org.eclipse.core.runtime.IPath)
	 */
	public Objdump getObjdump(IPath path) {
		return new CygwinToolsProvider(this).getObjdump(path);
	}
}
