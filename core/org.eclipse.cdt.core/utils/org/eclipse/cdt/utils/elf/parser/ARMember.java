package org.eclipse.cdt.utils.elf.parser;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.cdt.utils.elf.AR;
import org.eclipse.cdt.utils.elf.Elf;
import org.eclipse.cdt.utils.elf.ElfHelper;
import org.eclipse.core.runtime.IPath;

/**
 */
public class ARMember extends BinaryObject {
	AR.ARHeader header;

	public ARMember(IPath p, AR.ARHeader h) throws IOException {
		super(p);
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

	protected void addSymbols(Elf.Symbol[] array, int type) {
		for (int i = 0; i < array.length; i++) {
			Symbol sym = new Symbol();
			sym.type = type;
			sym.name = array[i].toString();
			sym.addr = array[i].st_value;
			addSymbol(sym);
			// This can fail if we use addr2line
			// but we can safely ignore the error.
		}
	}

}
