/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.elf.parser;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IBinaryParser.IBinaryArchive;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.utils.AR;
import org.eclipse.cdt.utils.BinaryFile;
import org.eclipse.cdt.utils.AR.ARHeader;
import org.eclipse.core.runtime.IPath;

/**
 */
public class ElfBinaryArchive extends BinaryFile implements IBinaryArchive {

	ArrayList children;

	public ElfBinaryArchive(IBinaryParser parser, IPath p) throws IOException {
		super(parser, p, IBinaryFile.ARCHIVE);
		new AR(p.toOSString()).dispose(); // check file type
		children = new ArrayList(5);
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryArchive#getObjects()
	 */
	public IBinaryObject[] getObjects() {
		if (hasChanged()) {
			children.clear();
			AR ar = null;
			try {
				ar = new AR(getPath().toOSString());
				AR.ARHeader[] headers = ar.getHeaders();
				addArchiveMembers(headers, children);
			} catch (IOException e) {
				//e.printStackTrace();
			}
			if (ar != null) {
				ar.dispose();
			}
			children.trimToSize();
		}
		return (IBinaryObject[]) children.toArray(new IBinaryObject[0]);
	}

	/**
	 * @param headers
	 * @param children2
	 */
	protected void addArchiveMembers(ARHeader[] headers, ArrayList children2) {
		for (int i = 0; i < headers.length; i++) {
			IBinaryObject bin = new ElfBinaryObject(getBinaryParser(), getPath(), headers[i]);
			children.add(bin);
		}
	}
}
