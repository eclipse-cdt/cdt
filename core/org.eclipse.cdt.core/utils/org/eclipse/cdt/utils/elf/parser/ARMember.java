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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.cdt.utils.Addr2line;
import org.eclipse.cdt.utils.CPPFilt;
import org.eclipse.cdt.utils.elf.AR;
import org.eclipse.cdt.utils.elf.Elf;
import org.eclipse.cdt.utils.elf.ElfHelper;
import org.eclipse.core.runtime.IPath;

/**
 */
public class ARMember extends BinaryObject {
	AR.ARHeader header;

	public ARMember(IPath p, AR.ARHeader h) throws IOException {
		super(p, new ElfHelper(h.getElf()));
		header = h;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryFile#getContents()
	 */
	public InputStream getContents() {
		InputStream stream = null;
		if (path != null && header != null) {
			try {
				stream = new ByteArrayInputStream(header.getObjectData());
			} catch (IOException e) {
			}
		}
		if (stream == null) {
			stream = super.getContents();
		}
		return stream;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject#getName()
	 */
	public String getName() {
		if (header != null) {
			return header.getObjectName();
		}
		return "";
	}

	protected ElfHelper getElfHelper() throws IOException {
		if (header != null) {
			return new ElfHelper(header.getElf());
		}
		throw new IOException("No file assiocated with Binary");
	}

	protected void addSymbols(Elf.Symbol[] array, int type, Addr2line addr2line, CPPFilt cppfilt) {
		for (int i = 0; i < array.length; i++) {
			Symbol sym = new Symbol(this);
			sym.type = type;
			sym.name = array[i].toString();
			sym.addr = array[i].st_value;
			addSymbol(sym);
		}
	}

}
