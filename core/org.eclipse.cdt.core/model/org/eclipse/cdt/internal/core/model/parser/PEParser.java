package org.eclipse.cdt.internal.core.model.parser;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;

import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.utils.coff.PE;
import org.eclipse.cdt.utils.coff.PEArchive;
import org.eclipse.core.resources.IFile;

/**
 */
public class PEParser implements IBinaryParser {

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser#getBinary(IFile)
	 */
	public IBinaryFile getBinary(IFile file) throws IOException {
		try {
			PE pe = new PE(file.getLocation().toOSString());
			pe.dispose();
			return new PEBinaryFile(file);
		} catch (IOException e) {
		}
		// Is it an Archive.
		PEArchive ar = new PEArchive(file.getLocation().toOSString());
		ar.dispose();
		return new PEBinaryArchive(file);
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser#getFormat()
	 */
	public String getFormat() {
		return "PE";
	}

}
