package org.eclipse.cdt.utils.elf.parser;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;

import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.IBinaryParser.IBinaryShared;
import org.eclipse.core.runtime.IPath;

/**
 */
public class BinaryShared extends BinaryExecutable implements IBinaryShared {
	String soname;

	public BinaryShared(IPath path) throws IOException {
		super(path);
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryShared#getSoName()
	 */
	public String getSoName() {
		if (hasChanged()) {
			try {
				loadInformation();
			} catch (IOException e) {
			}
		}
		if (soname != null) {
			return soname;
		}
		return "";
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryFile#getType()
	 */
	public int getType() {
		return IBinaryFile.SHARED;
	}

}
