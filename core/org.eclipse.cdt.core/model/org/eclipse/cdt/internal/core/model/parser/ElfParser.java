package org.eclipse.cdt.internal.core.model.parser;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.IOException;

import org.eclipse.cdt.core.model.IBinaryParser;
import org.eclipse.cdt.utils.elf.AR;
import org.eclipse.cdt.utils.elf.Elf;
import org.eclipse.core.resources.IFile;

/**
 */
public class ElfParser implements IBinaryParser {

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser#getBinary(IPath)
	 */
	public IBinaryFile getBinary(IFile file) throws IOException {
		try {
			Elf e = new Elf(file.getLocation().toOSString());
			e.dispose();
			return new ElfBinaryFile(file);
		} catch (IOException e) {
		}
		// Is it an Archive.
		AR ar = new AR(file.getLocation().toOSString()); 
		ar.dispose();
		return new ElfBinaryArchive(file);
	}

}
