package org.eclipse.cdt.internal.core.model.parser;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;

import org.eclipse.cdt.core.AbstractCExtension;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.utils.coff.PE;
import org.eclipse.cdt.utils.coff.PEArchive;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 */
public class PEParser extends AbstractCExtension implements IBinaryParser {

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser#getBinary(IFile)
	 */
	public IBinaryFile getBinary(IPath path) throws IOException {
		if (path == null) {
			path = new Path("");
		}
		try {
			PE pe = new PE(path.toOSString());
			pe.dispose();
			return new PEBinaryFile(path);
		} catch (IOException e) {
		}
		// Is it an Archive.
		PEArchive ar = new PEArchive(path.toOSString());
		ar.dispose();
		return new PEBinaryArchive(path);
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser#getFormat()
	 */
	public String getFormat() {
		return "PE";
	}

}
