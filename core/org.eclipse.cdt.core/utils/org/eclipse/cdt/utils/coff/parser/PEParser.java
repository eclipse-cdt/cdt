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

package org.eclipse.cdt.utils.coff.parser;

import java.io.IOException;

import org.eclipse.cdt.core.AbstractCExtension;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.utils.coff.PE;
import org.eclipse.cdt.utils.coff.PE.Attribute;
import org.eclipse.core.runtime.IPath;

/**
 */
public class PEParser extends AbstractCExtension implements IBinaryParser {

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser#getBinary(IFile)
	 */
	public IBinaryFile getBinary(IPath path) throws IOException {
		if (path == null) {
			throw new IOException("path is null");
		}

		BinaryFile binary = null;
		try {
			PE.Attribute attribute = PE.getAttributes(path.toOSString());
			if (attribute != null) {
				switch (attribute.getType()) {
					case Attribute.PE_TYPE_EXE :
						binary = new BinaryExecutable(path);
					break;
 
					case Attribute.PE_TYPE_SHLIB :
						binary = new BinaryShared(path);
					break;
 
					case Attribute.PE_TYPE_OBJ :
						binary = new BinaryObject(path);
					break;
 
					case Attribute.PE_TYPE_CORE :
						BinaryObject obj = new BinaryObject(path);
						obj.setType(IBinaryFile.CORE);
						binary = obj;
					break;
				}
			}
		} catch (IOException e) {
			// Is it an Archive?
			binary = new BinaryArchive(path);
		}

		return binary;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser#getFormat()
	 */
	public String getFormat() {
		return "PE";
	}

}
