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
import org.eclipse.cdt.utils.Addr2line;
import org.eclipse.cdt.utils.CPPFilt;
import org.eclipse.cdt.utils.IToolsProvider;
import org.eclipse.cdt.utils.Objdump;
import org.eclipse.cdt.utils.ToolsProvider;
import org.eclipse.core.runtime.IPath;

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


	public Addr2line getAddr2Line(IPath path) {
		ToolsProvider provider = new ToolsProvider(this);
		return provider.getAddr2Line(path);
	}

	public Objdump getObjdump(IPath path) {
		ToolsProvider provider = new ToolsProvider(this);
		return provider.getObjdump(path);
	}

	public CPPFilt getCPPFilt() {
		return new ToolsProvider(this).getCPPFilt();
	}

}
