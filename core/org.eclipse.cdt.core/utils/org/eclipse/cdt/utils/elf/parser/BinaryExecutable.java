package org.eclipse.cdt.utils.elf.parser;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.cdt.core.IBinaryParser.IBinaryExecutable;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.utils.elf.Elf.Attribute;
import org.eclipse.cdt.utils.elf.ElfHelper.Sizes;
import org.eclipse.core.runtime.IPath;

/**
 */
public class BinaryExecutable extends BinaryObject implements IBinaryExecutable {
	long timestamp;
	String soname;
	String[] needed;
	Sizes sizes;
	Attribute attribute;
	ArrayList symbols;

	public BinaryExecutable(IPath path) throws IOException {
		super(path);
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryExecutable#getNeededSharedLibs()
	 */
	public String[] getNeededSharedLibs() {
		if (hasChanged()) {
			try {
				loadInformation();
			} catch (IOException e) {
			}
		}
		if (needed != null) {
			return needed;
		}
		return new String[0];
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryFile#getType()
	 */
	public int getType() {
		return IBinaryFile.EXECUTABLE;
	}

}
