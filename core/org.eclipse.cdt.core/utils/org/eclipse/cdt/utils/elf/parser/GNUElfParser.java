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
package org.eclipse.cdt.utils.elf.parser;

import java.io.IOException;

import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.utils.*;
import org.eclipse.cdt.utils.Addr2line;
import org.eclipse.cdt.utils.CPPFilt;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 */
public class GNUElfParser extends ElfParser implements IBinaryParser, IToolsProvider {

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
		return "GNU ELF";
	}

	public IPath getAddr2LinePath() {
		ICExtensionReference ref = getExtensionReference();
		String value =  ref.getExtensionData("addr2line"); //$NON-NLS-1
		if (value == null || value.length() == 0) {
			value = "addr2line"; //$NON-NLS-1
		}
		return new Path(value);
	}

	public IPath getCPPFiltPath() {
		ICExtensionReference ref = getExtensionReference();
		String value = ref.getExtensionData("c++filt"); //$NON-NLS-1
		if (value == null || value.length() == 0) {
			value = "c++filt"; //$NON-NLS-1
		}
		return new Path(value);
	}

	public Addr2line getAddr2Line(IPath path) {
		IPath addr2LinePath = getAddr2LinePath();
		Addr2line addr2line = null;
		if (addr2LinePath != null && !addr2LinePath.isEmpty()) {
			try {
				addr2line = new Addr2line(addr2LinePath.toOSString(), path.toOSString());
			} catch (IOException e1) {
			}
		}
		return addr2line;
	}

	public CPPFilt getCPPFilt() {
		IPath cppFiltPath = getCPPFiltPath();
		CPPFilt cppfilt = null;
		if (cppFiltPath != null && ! cppFiltPath.isEmpty()) {
			try {
				cppfilt = new CPPFilt(cppFiltPath.toOSString());
			} catch (IOException e2) {
			}
		}
		return cppfilt;
	}

}
