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
